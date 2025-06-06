package com.anytypeio.anytype.domain.chats

import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.chats.Chat
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.debugging.Logger
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.launch

/**
 * Cross-space (vault-level) chat previews container
 */
interface ChatPreviewContainer {

    fun start()
    fun stop()

    suspend fun getAll(): List<Chat.Preview>
    suspend fun getPreview(space: SpaceId): Chat.Preview?
    fun observePreview(space: SpaceId) : Flow<Chat.Preview?>
    fun observePreviews() : Flow<List<Chat.Preview>>

    class Default @Inject constructor(
        private val repo: BlockRepository,
        private val events: ChatEventChannel,
        private val dispatchers: AppCoroutineDispatchers,
        private val scope: CoroutineScope,
        private val logger: Logger
    ) : ChatPreviewContainer {

        private var job: Job? = null
        private val previews = MutableStateFlow<List<Chat.Preview>>(emptyList())

        override fun start() {
            job?.cancel()
            job = scope.launch(dispatchers.io) {
                previews.value = emptyList()
                val initial = runCatching { repo.subscribeToMessagePreviews(SUBSCRIPTION_ID) }
                    .onFailure { logger.logWarning("DROID-2966 Error while getting initial previews: ${it.message}") }
                    .getOrDefault(emptyList())
                events
                    .subscribe(SUBSCRIPTION_ID)
                    .scan(initial = initial) { previews, events ->
                        events.fold(previews) { state, event ->
                            when (event) {
                                is Event.Command.Chats.Add -> {
                                    state.map { preview ->
                                        if (preview.chat == event.context) {
                                            preview.copy(message = event.message)
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
                                            preview.copy(
                                                state = event.state
                                            )
                                        } else {
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
                runCatching {
                    repo.unsubscribeFromMessagePreviews(subscription = SUBSCRIPTION_ID)
                }.onFailure {
                    logger.logException(it, "DROID-2966 Error while unsubscribing from message previews")
                }
            }
        }

        override suspend fun getAll(): List<Chat.Preview> = previews.value

        override suspend fun getPreview(space: SpaceId): Chat.Preview? {
            return previews.value.firstOrNull { preview -> preview.space.id == space.id }
        }

        override fun observePreview(space: SpaceId): Flow<Chat.Preview?> {
            return previews.map {
                it.firstOrNull { preview -> preview.space.id == space.id }
            }
        }

        override fun observePreviews(): Flow<List<Chat.Preview>> {
            return previews
        }

        companion object {
            private const val SUBSCRIPTION_ID = "chat-previews-subscription"
        }
    }
}