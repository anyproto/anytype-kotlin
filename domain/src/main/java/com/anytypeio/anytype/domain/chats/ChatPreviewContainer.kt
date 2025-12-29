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
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedDeque
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Emits chat‑preview data enriched with attachment objects. The flow surfaces a
 * semantic [PreviewState] so downstream collectors (e.g. the VaultViewModel)
 * can ignore early loading emissions without relying on timing hacks.
 */
interface ChatPreviewContainer {

    fun start()
    fun stop()

    fun observePreviewsWithAttachments(): Flow<PreviewState>

    /**
     * Observes chat preview for a specific space without attachment enrichment.
     * Returns only the unread counts for the specified space.
     * @param spaceId The space ID to observe
     * @return Flow of chat preview for the space, or null if not found
     */
    fun observePreviewBySpaceId(spaceId: SpaceId): Flow<Chat.Preview?>
    fun observePreviewsBySpaceId(spaceId: SpaceId): Flow<List<Chat.Preview>>

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
    ) : ChatPreviewContainer {

        private var job: Job? = null

        /**
         * `null`  – previews not fetched yet  → emit [ChatPreviewContainer.PreviewState.Loading]
         * non‑null – subscription finished   → emit [ChatPreviewContainer.PreviewState.Ready]
         */
        private val previews = MutableStateFlow<List<Chat.Preview>?>(null)
        private val attachmentIds = MutableStateFlow<Map<SpaceId, Set<Id>>>(emptyMap())

        // Buffer of last N preview messages per chat for fallback on deletion (thread-safe)
        private val messageHistory = ConcurrentHashMap<Id, ConcurrentLinkedDeque<Chat.Message>>()

        // Hot shared state for UI collectors
        @OptIn(ExperimentalCoroutinesApi::class)
        private val previewsState: StateFlow<PreviewState> = previews
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
                logger.logException(e, "Exception in preview flow")
                emit(PreviewState.Loading)
            }
            .stateIn(scope, SharingStarted.Eagerly, PreviewState.Loading)

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

        override fun start() {
            job?.cancel()

            job = scope.launch(dispatchers.io) {

                attachmentIds.value = emptyMap() // Reset attachment tracking
                previews.value = null // Reset previews
                messageHistory.clear() // Reset message history

                val initial = runCatching {
                    repo.subscribeToMessagePreviews(SUBSCRIPTION_ID)
                }.onFailure {
                    logger.logWarning("Error while getting initial previews: ${it.message}")
                }.getOrDefault(emptyList())

                // Initialize history from initial previews
                initial.forEach { preview ->
                    preview.message?.let { message ->
                        val history = messageHistory.getOrPut(preview.chat) { ConcurrentLinkedDeque() }
                        history.addLast(message)
                    }
                }

                previews.value = initial            // ← Ready (may be empty)
                trackMissingAttachments(initial)
                collectEvents(initial)
            }
        }

        override fun stop() {
            scope.launch(dispatchers.io) {
                job?.cancel()
                job = null
                previews.value = null           // back to Loading for next start()
                messageHistory.clear()          // clear message history
                unsubscribeAll()
                attachmentFlows.clear()         // let the cached StateFlows be GC-ed
            }
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        override fun observePreviewsWithAttachments(): Flow<PreviewState> = previewsState

        /**
         * Need to be refactored in the context of multi chats,
         * since one space can have more than one chat now,
         * more than one preview
         */
        override fun observePreviewBySpaceId(spaceId: SpaceId): Flow<Chat.Preview?> =
            previews
                .map { list -> list?.find { it.space == spaceId } }
                .distinctUntilChanged()

        override fun observePreviewsBySpaceId(spaceId: SpaceId): Flow<List<Chat.Preview>> {
            return previews.map { list ->
                list?.filter { preview ->
                    preview.space == spaceId
                } ?: emptyList()
            }
        }

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
                        // Remove deleted message from history and get previous message
                        val history = messageHistory[event.context]
                        history?.removeIf { it.id == event.message }
                        val previousMessage = history?.lastOrNull()
                        preview.copy(message = previousMessage)
                    } else preview
                }

                is Event.Command.Chats.UpdateMessageSyncStatus -> state.map { preview ->
                    val message = preview.message
                    if (preview.chat == event.context &&
                        message != null &&
                        event.messages.contains(message.id)) {
                        preview.copy(message = message.copy(synced = event.isSynced))
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
            // Store message in history buffer for fallback on deletion
            val chatId = event.context
            val history = messageHistory.getOrPut(chatId) { ConcurrentLinkedDeque() }
            if (history.size >= MAX_MESSAGE_HISTORY) {
                history.removeFirst()
            }
            history.addLast(event.message)

            // Extract attachment IDs
            val messageAttachmentIds = event.message.attachments.map { it.target }.toSet()
            val dependencyIds = event.dependencies.map { it.id }.toSet()
            val missing = messageAttachmentIds - dependencyIds

            val idx = state.indexOfFirst { it.chat == event.context }
            if (idx == -1) {
                // Brand-new preview
                // Track missing attachments for later subscription
                if (missing.isNotEmpty()) {
                    val space = event.spaceId
                    val merged = (attachmentIds.value[space] ?: emptySet()) + missing
                    if (attachmentIds.value[space] != merged) {
                        logger.logInfo("DROID‑3309: Adding missing attachments for space ${space.id}: $merged")
                        attachmentIds.value = attachmentIds.value + (space to merged)
                    }
                }
                return state + Chat.Preview(
                    space = event.spaceId,
                    chat = event.context,
                    message = event.message,
                    dependencies = event.dependencies
                )
            }

            // Track missing attachments for later subscription
            state.firstOrNull { it.chat == event.context }?.let { preview ->
                if (missing.isNotEmpty()) {
                    val merged =
                        (attachmentIds.value[preview.space] ?: emptySet()) + missing
                    if (attachmentIds.value[preview.space] != merged) {
                        logger.logInfo("DROID‑3309: Adding missing attachments for space ${preview.space.id}: $merged")
                        attachmentIds.value = attachmentIds.value + (preview.space to merged)
                    }
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
            if (initialMissing.isNotEmpty() && attachmentIds.value != initialMissing) {
                logger.logInfo("DROID‑3309: Initial missing attachments: $initialMissing")
                attachmentIds.value = initialMissing
            }
        }

        private fun enrichWithAttachments(ready: PreviewState.Ready): Flow<PreviewState> {
            val spaces = ready.items.map { it.space }.distinct()
            val sharedFlows = spaces.map { attachmentFlow(it) }

            return if (sharedFlows.isEmpty()) {
                flowOf(ready)
            } else {
                combine(sharedFlows) { arrays ->
                    val attachmentsBySpace = arrays
                        .mapIndexed { idx, map -> spaces[idx] to map }
                        .toMap()

                    val enriched = ready.items.map { preview ->
                        val deps = buildUpdatedDependencies(
                            preview = preview,
                            attachments = attachmentsBySpace[preview.space].orEmpty()
                        )
                        preview.copy(dependencies = deps)
                    }
                    PreviewState.Ready(enriched)
                }
            }
        }

        private val attachmentFlows =
            ConcurrentHashMap<SpaceId, StateFlow<Map<Id, ObjectWrapper.Basic>>>()

        @OptIn(ExperimentalCoroutinesApi::class)
        private fun attachmentFlow(space: SpaceId): StateFlow<Map<Id, ObjectWrapper.Basic>> =
            attachmentFlows.getOrPut(space) {
                attachmentIds
                    .map { it[space] ?: emptySet() }
                    .distinctUntilChanged()
                    .flatMapLatest { ids ->
                        if (ids.isEmpty()) flowOf(emptyMap())
                        else subscription.subscribe(
                            searchParams = StoreSearchByIdsParams(
                                subscription = "${space.id}/$ATTACHMENT_SUBSCRIPTION_POSTFIX",
                                space = space,
                                targets = ids.toList(),
                                keys = ChatContainer.ATTACHMENT_KEYS
                            )
                        ).map { wrappers -> wrappers.associateBy { it.id } }
                    }
                    .catch { e ->
                        logger.logException(e, "DROID-3309 attachment flow error for ${space.id}")
                        emit(emptyMap())
                    }
                    // one upstream subscription, replay last value for new collectors
                    .stateIn(scope, SharingStarted.Lazily, emptyMap())
            }

        private fun buildUpdatedDependencies(
            preview: Chat.Preview,
            attachments: Map<Id, ObjectWrapper.Basic>
        ): List<ObjectWrapper.Basic> {
            val all = (preview.dependencies + attachments.values)
                .associateBy { it.id }
            return all.values.toList()
                .also { deps ->
                    logger.logInfo(
                        "DROID-3309: buildUpdatedDependencies for space ${preview.space.id}, " +
                                "found ${deps.size} dependencies for ${preview.message?.attachments?.size ?: 0} attachments"
                    )
                }
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
            private const val MAX_MESSAGE_HISTORY = 10
        }
    }
}