package com.anytypeio.anytype.domain.chats

import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.chats.Chat
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.account.AwaitAccountStartManager
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
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.launch

/**
 * Emits chat‑preview data enriched with attachment objects. The flow surfaces a
 * semantic [PreviewState] so downstream collectors (e.g. the VaultViewModel)
 * can ignore early loading emissions without relying on timing hacks.
 */
interface VaultChatPreviewContainer {

    fun start()
    fun stop()

    fun observePreviewsWithAttachments(): Flow<PreviewState>

    sealed interface PreviewState {
        object Loading : PreviewState
        data class Ready(val items: List<Chat.Preview>) : PreviewState
    }

    class Default @Inject constructor(
        private val repo: BlockRepository,
        private val events: ChatEventChannel,
        private val dispatchers: AppCoroutineDispatchers,
        private val scope: CoroutineScope,
        private val logger: Logger,
        private val subscription: StorelessSubscriptionContainer,
        private val awaitAccountStart: AwaitAccountStartManager
    ) : VaultChatPreviewContainer {

        private var job: Job? = null
        /**
         * `null`  – previews not fetched yet  → emit [com.anytypeio.anytype.domain.chats.VaultChatPreviewContainer.PreviewState.Loading]
         * non‑null – subscription finished   → emit [com.anytypeio.anytype.domain.chats.VaultChatPreviewContainer.PreviewState.Ready]
         */
        private val previews = MutableStateFlow<List<Chat.Preview>?>(null)
        private val attachmentIds = MutableStateFlow<Map<SpaceId, Set<Id>>>(emptyMap())

        init {
            // Auto start/stop together with account lifecycle
            scope.launch {
                awaitAccountStart.state().collect { state ->
                    when (state) {
                        AwaitAccountStartManager.State.Init -> Unit
                        AwaitAccountStartManager.State.Started -> start()
                        AwaitAccountStartManager.State.Stopped -> stop()
                    }
                }
            }
        }

        // ------------------------------------------------------------
        //  Public API
        // ------------------------------------------------------------

        override fun start() {
            job?.cancel()
            job = scope.launch(dispatchers.io) {

                previews.value = null               // ← back to Loading

                val initial = runCatching {
                    repo.subscribeToMessagePreviews(SUBSCRIPTION_ID)
                }.onFailure {
                    logger.logWarning("Error while getting initial previews: ${it.message}")
                }.getOrDefault(emptyList())

                previews.value = initial            // ← Ready (may be empty)
                trackMissingAttachments(initial)
                collectEvents(initial)
            }
        }

        override fun stop() {
            job?.cancel()
            job = null
            scope.launch(dispatchers.io) {
                previews.value = null           // back to Loading for next start()
                unsubscribeAll()
            }
        }

        // ------------------------------------------------------------
        //  Preview stream
        // ------------------------------------------------------------

        @OptIn(ExperimentalCoroutinesApi::class)
        override fun observePreviewsWithAttachments(): Flow<PreviewState> =
            previews
                .map { list ->
                    if (list == null) PreviewState.Loading else PreviewState.Ready(list)
                }
                .flatMapLatest { state ->
                    when (state) {
                        PreviewState.Loading -> flowOf(state)
                        is PreviewState.Ready -> enrichWithAttachments(state)
                    }
                }
                .distinctUntilChanged()
                .catch { e ->
                    logger.logException(e, "DROID‑2966 Exception in preview flow")
                    emit(PreviewState.Loading)
                }
                .onEach { logger.logInfo("VaultChatPreviewContainer emit → ${it::class.java.simpleName}") }
                .flowOn(dispatchers.io)

        // ------------------------------------------------------------
        //  Private helpers
        // ------------------------------------------------------------

        private suspend fun collectEvents(initial: List<Chat.Preview>) {
            events.subscribe(SUBSCRIPTION_ID)
                .scan(initial = initial) { previews, batch ->
                    batch.fold(previews) { state, event ->
                        applyEvent(state, event)
                    }
                }
                .flowOn(dispatchers.io)
                .catch { logger.logException(it, "Event stream error") }
                .collect { previews.value = it }
        }

        private fun applyEvent(
            state: List<Chat.Preview>,
            event: Event.Command.Chats
        ): List<Chat.Preview> {
            return when (event) {
                is Event.Command.Chats.Add -> handleAdd(state, event)

                is Event.Command.Chats.Update -> state.map { preview ->
                    if (preview.chat == event.context && preview.message?.id == event.id) {
                        preview.copy(message = event.message)
                    } else preview
                }

                is Event.Command.Chats.UpdateState -> state.map { preview ->
                    if (preview.chat == event.context) {
                        val newState = event.state
                        if (newState != null && ChatStateUtils.shouldApplyNewChatState(
                                newOrder = newState.order,
                                currentOrder = preview.state?.order
                            )
                        ) {
                            logger.logInfo("Applying new chat preview state with order: ${newState.order}")
                            preview.copy(state = newState)
                        } else preview
                    } else preview
                }

                is Event.Command.Chats.Delete -> state.map { preview ->
                    if (preview.chat == event.context && preview.message?.id == event.message) {
                        preview.copy(message = null)
                    } else preview
                }

                else -> state.also {
                    logger.logInfo("Ignoring event: $event")
                }
            }
        }

        private fun handleAdd(
            state: List<Chat.Preview>,
            event: Event.Command.Chats.Add
        ): List<Chat.Preview> {
            // Extract attachment IDs
            val messageAttachmentIds = event.message.attachments.map { it.target }.toSet()
            val dependencyIds = event.dependencies.map { it.id }.toSet()
            val missing = messageAttachmentIds - dependencyIds

            // Track missing attachments for later subscription
            state.firstOrNull { it.chat == event.context }?.let { preview ->
                if (missing.isNotEmpty()) {
                    val merged =
                        (attachmentIds.value[preview.space] ?: emptySet()) + missing
                    attachmentIds.value = attachmentIds.value + (preview.space to merged)
                }
            }

            return state.map { preview ->
                if (preview.chat == event.context) {
                    preview.copy(message = event.message, dependencies = event.dependencies)
                } else preview
            }
        }

        private fun trackMissingAttachments(previews: List<Chat.Preview>) {
            val initialMissing = mutableMapOf<SpaceId, Set<Id>>()
            previews.forEach { preview ->
                preview.message?.let { message ->
                    val messageAttachments = message.attachments.map { it.target }.toSet()
                    val dependencyIds = preview.dependencies.map { it.id }.toSet()
                    val missing = messageAttachments - dependencyIds
                    if (missing.isNotEmpty()) {
                        initialMissing[preview.space] = missing
                    }
                }
            }
            if (initialMissing.isNotEmpty()) attachmentIds.value = initialMissing
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        private fun enrichWithAttachments(ready: PreviewState.Ready): Flow<PreviewState> {
            val current = ready.items
            if (current.isEmpty()) return flowOf(ready) // user has no chats

            val spaces = current.map { it.space }.distinct()
            val attachmentStreams = spaces.map { space ->
                subscribeToAttachments(space).map { space to it }
            }

            return if (attachmentStreams.isEmpty()) {
                flowOf(ready)
            } else {
                combine(attachmentStreams) { pairs ->
                    val attachmentsBySpace = pairs.toMap()
                    val enriched = current.map { preview ->
                        val deps = buildUpdatedDependencies(
                            preview,
                            attachmentsBySpace[preview.space].orEmpty()
                        )
                        preview.copy(dependencies = deps)
                    }
                    PreviewState.Ready(enriched)
                }
            }
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        private fun subscribeToAttachments(space: SpaceId): Flow<Map<Id, ObjectWrapper.Basic>> =
            attachmentIds
                .map { it[space] ?: emptySet() }
                .distinctUntilChanged()
                .flatMapLatest { ids ->
                    if (ids.isEmpty()) {
                        flowOf(emptyMap())
                    } else {
                        subscription.subscribe(
                            searchParams = StoreSearchByIdsParams(
                                subscription = "${space.id}/$ATTACHMENT_SUBSCRIPTION_POSTFIX",
                                space = space,
                                targets = ids.toList(),
                                keys = ChatContainer.ATTACHMENT_KEYS
                            )
                        ).map { wrappers -> wrappers.associateBy { it.id } }
                    }
                }
                .catch { e ->
                    logger.logException(e, "DROID‑3309 Error in attachment flow for ${space.id}")
                    emit(emptyMap())
                }

        private fun buildUpdatedDependencies(
            preview: Chat.Preview,
            attachments: Map<Id, ObjectWrapper.Basic>
        ): List<ObjectWrapper.Basic> {
            val existing = preview.dependencies.associateBy { it.id }
            val all = existing + attachments
            return all.values.toList()
        }

        private suspend fun unsubscribeAll() {
            val attachmentSubs =
                attachmentIds.value.keys.map { "${it.id}/$ATTACHMENT_SUBSCRIPTION_POSTFIX" }
            attachmentIds.value = emptyMap()

            runCatching { repo.unsubscribeFromMessagePreviews(SUBSCRIPTION_ID) }
                .onFailure { logger.logException(it, "DROID‑3309 Error unsubscribing previews") }

            runCatching {
                if (attachmentSubs.isNotEmpty()) repo.cancelObjectSearchSubscription(attachmentSubs)
            }.onFailure { logger.logException(it, "DROID‑3309 Error unsubscribing attachments") }
        }

        companion object {
            private const val SUBSCRIPTION_ID = "chat-previews-subscription"
            private const val ATTACHMENT_SUBSCRIPTION_POSTFIX = "chat-previews-attachments"
        }
    }
}