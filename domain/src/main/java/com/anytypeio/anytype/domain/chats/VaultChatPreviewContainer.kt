package com.anytypeio.anytype.domain.chats

import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.chats.Chat
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.debugging.Logger
import com.anytypeio.anytype.domain.library.StoreSearchByIdsParams
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.launch

interface VaultChatPreviewContainer {

    fun start()
    fun stop()
    fun observePreviewsWithAttachments(): Flow<List<Chat.Preview>>

    class Default @Inject constructor(
        private val repo: BlockRepository,
        private val events: ChatEventChannel,
        private val dispatchers: AppCoroutineDispatchers,
        private val scope: CoroutineScope,
        private val logger: Logger,
        private val subscription: StorelessSubscriptionContainer
    ) : VaultChatPreviewContainer {

        private var job: Job? = null
        private val previews = MutableStateFlow<List<Chat.Preview>>(emptyList())
        private val attachmentIds = MutableStateFlow<Map<SpaceId, Set<Id>>>(emptyMap())

        override fun start() {
            job?.cancel()
            job = scope.launch(dispatchers.io) {
                previews.value = emptyList()
                val initial = runCatching { repo.subscribeToMessagePreviews(SUBSCRIPTION_ID) }
                    .onFailure { logger.logWarning("DROID-2966 Error while getting initial previews: ${it.message}") }
                    .getOrDefault(emptyList())

                // Check initial previews for missing attachments
                val initialAttachmentIds = mutableMapOf<SpaceId, Set<Id>>()
                initial.forEach { preview ->
                    preview.message?.let { message ->
                        // Extract attachment IDs from the message
                        val messageAttachmentIds = message.attachments.map { it.target }.toSet()
                        val dependencyIds = preview.dependencies.map { it.id }.toSet()

                        // Find attachments that are NOT in dependencies - these need subscription
                        val missingAttachmentIds = messageAttachmentIds - dependencyIds

                        if (missingAttachmentIds.isNotEmpty()) {
                            logger.logInfo("DROID-3309 Found missing attachments for space: ${preview.space.id}, ids: $missingAttachmentIds")
                            initialAttachmentIds[preview.space] = missingAttachmentIds
                        }
                    }
                }

                // Update attachment tracking with initial missing attachments
                if (initialAttachmentIds.isNotEmpty()) {
                    attachmentIds.value = initialAttachmentIds
                }

                events
                    .subscribe(SUBSCRIPTION_ID)
                    .scan(initial = initial) { previews, events ->
                        events.fold(previews) { state, event ->
                            when (event) {
                                is Event.Command.Chats.Add -> {
                                    // Extract attachment IDs from the message
                                    val messageAttachmentIds =
                                        event.message.attachments.map { it.target }.toSet()
                                    val dependencyIds = event.dependencies.map { it.id }.toSet()

                                    // Find attachments that are NOT in dependencies - these need subscription
                                    val missingAttachmentIds = messageAttachmentIds - dependencyIds

                                    // Update attachment tracking for this space (only missing ones)
                                    state.firstOrNull { it.chat == event.context }
                                        ?.let { matchingPreview ->
                                            if (missingAttachmentIds.isNotEmpty()) {
                                                logger.logInfo("DROID-3309 Found missing attachments for space: ${matchingPreview.space.id}, ids: $missingAttachmentIds")
                                                val currentAttachmentIds = attachmentIds.value
                                                val existingIds =
                                                    currentAttachmentIds[matchingPreview.space]
                                                        ?: emptySet()
                                                val mergedIds = existingIds + missingAttachmentIds
                                                attachmentIds.value =
                                                    currentAttachmentIds + (matchingPreview.space to mergedIds)
                                            }
                                        }

                                    state.map { preview ->
                                        if (preview.chat == event.context) {
                                            preview.copy(
                                                message = event.message,
                                                dependencies = event.dependencies
                                            )
                                        } else {
                                            preview
                                        }
                                    }
                                }

                                is Event.Command.Chats.Update -> {
                                    state.map { preview ->
                                        if (preview.chat == event.context && preview.message?.id == event.id) {
                                            preview.copy(message = event.message)
                                        } else {
                                            preview
                                        }
                                    }
                                }

                                is Event.Command.Chats.UpdateState -> {
                                    state.map { preview ->
                                        if (preview.chat == event.context) {
                                            val newState = event.state
                                            if (newState != null && ChatStateUtils.shouldApplyNewChatState(
                                                    newOrder = newState.order,
                                                    currentOrder = preview.state?.order
                                                )
                                            ) {
                                                logger.logInfo("DROID-3799 Applying new chat preview state with order: ${newState.order}")
                                                preview.copy(state = newState)
                                            } else {
                                                logger.logInfo("DROID-3799 Skipping chat preview state update due to order comparison")
                                                preview
                                            }
                                        } else {
                                            logger.logInfo("Skipping chat preview state update for non-matching chat: ${event.context}")
                                            preview
                                        }
                                    }
                                }

                                is Event.Command.Chats.Delete -> {
                                    state.map { preview ->
                                        if (preview.chat == event.context && preview.message?.id == event.message) {
                                            preview.copy(message = null)
                                        } else {
                                            preview
                                        }
                                    }
                                }

                                else -> state.also {
                                    logger.logInfo("DROID-2966 Ignoring event: $event")
                                }
                            }
                        }
                    }
                    .flowOn(dispatchers.io)
                    .catch { logger.logException(it, "DROID-2966 Exception in chat preview flow") }
                    .collect {
                        previews.value = it
                    }
            }
        }

        override fun stop() {
            job?.cancel()
            job = null
            scope.launch(dispatchers.io) {
                previews.value = emptyList()

                // Unsubscribe from attachment subscriptions
                val attachmentSubscriptions = attachmentIds.value.keys.map { space ->
                    "${space.id}/$ATTACHMENT_SUBSCRIPTION_POSTFIX"
                }
                attachmentIds.value = emptyMap()

                runCatching {
                    repo.unsubscribeFromMessagePreviews(subscription = SUBSCRIPTION_ID)
                }.onFailure {
                    logger.logException(
                        it,
                        "DROID-3309 Error while unsubscribing from message previews"
                    )
                }
                runCatching {
                    if (attachmentSubscriptions.isNotEmpty()) {
                        repo.cancelObjectSearchSubscription(attachmentSubscriptions)
                    }
                }.onFailure {
                    logger.logException(
                        it,
                        "DROID-3309 Error while unsubscribing from attachment subscriptions"
                    )
                }
            }
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        private fun subscribeToAttachments(space: SpaceId): Flow<Map<Id, ObjectWrapper.Basic>> {
            return attachmentIds
                .map { it[space] ?: emptySet() }
                .distinctUntilChanged()
                .flatMapLatest { ids ->
                    if (ids.isEmpty()) {
                        flowOf(emptyMap())
                    } else {
                        logger.logInfo("DROID-3309 Subscribing to attachments for space: ${space.id}, ids: $ids")
                        subscription.subscribe(
                            searchParams = StoreSearchByIdsParams(
                                subscription = "${space.id}/$ATTACHMENT_SUBSCRIPTION_POSTFIX",
                                space = space,
                                targets = ids.toList(),
                                keys = ChatContainer.Companion.ATTACHMENT_KEYS
                            )
                        ).map { wrappers ->
                            wrappers.associateBy { it.id }
                        }
                    }
                }
                .catch { e ->
                    emit(emptyMap()).also {
                        logger.logException(
                            e,
                            "DROID-3309 Error in the chat preview attachments pub/sub flow"
                        )
                    }
                }
        }

        private fun buildUpdatedDependencies(
            preview: Chat.Preview,
            attachments: Map<Id, ObjectWrapper.Basic>
        ): List<ObjectWrapper.Basic> {
            // Start with existing dependencies
            val existingDependencies = preview.dependencies.associateBy { it.id }

            // Add attachment details to dependencies
            val attachmentDependencies = attachments.values.associateBy { it.id }

            // Combine existing dependencies with attachment details
            val allDependencies = existingDependencies + attachmentDependencies

            return allDependencies.values.toList()
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        override fun observePreviewsWithAttachments(): Flow<List<Chat.Preview>> {
            return previews.flatMapLatest { previews ->
                if (previews.isEmpty()) {
                    flowOf(emptyList())
                } else {
                    // Get unique spaces from previews
                    val spaces = previews.map { it.space }.distinct()

                    // Combine all space attachment flows
                    val attachmentFlows = spaces.map { space ->
                        subscribeToAttachments(space).map { attachments -> space to attachments }
                    }

                    // If no spaces, return empty list
                    if (attachmentFlows.isEmpty()) {
                        flowOf(emptyList())
                    } else {
                        // Combine all attachment flows
                        combine(attachmentFlows) { attachmentPairs ->
                            val allAttachments = attachmentPairs.toMap()

                            // Update each preview with its attachment details
                            previews.map { preview ->
                                val spaceAttachments = allAttachments[preview.space] ?: emptyMap()
                                val updatedDependencies =
                                    buildUpdatedDependencies(preview, spaceAttachments)
                                preview.copy(dependencies = updatedDependencies)
                            }
                        }
                    }
                }
            }
        }

        companion object {
            private const val SUBSCRIPTION_ID = "chat-previews-subscription"
            private const val ATTACHMENT_SUBSCRIPTION_POSTFIX = "chat-previews-attachments"
        }
    }
}