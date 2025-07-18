package com.anytypeio.anytype.domain.chats

import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.chats.Chat
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.debugging.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.launch

/**
 * Space-level chat previews container
 * 
 * This container handles chat previews for a single space and focuses on:
 * - Lightweight preview updates
 * - No attachment handling or dependencies
 * - Single-space event processing
 * 
 * For vault-level previews with attachments, use [VaultChatPreviewContainer] instead.
 */
interface SpaceChatPreviewContainer {
    
    fun start(space: SpaceId)
    fun stop()

    fun observePreview(space: SpaceId) : Flow<Chat.Preview?>
    
    class Default(
        private val repo: BlockRepository,
        private val events: ChatEventChannel,
        private val dispatchers: AppCoroutineDispatchers,
        private val scope: CoroutineScope,
        private val logger: Logger
    ) : SpaceChatPreviewContainer {
        
        private var job: Job? = null
        private val previews = MutableStateFlow<List<Chat.Preview>>(emptyList())
        private var currentSpace: SpaceId? = null

        override fun start(space: SpaceId) {
            job?.cancel()
            currentSpace = space
            job = scope.launch(dispatchers.io) {
                previews.value = emptyList()
                
                val initial = runCatching { 
                    repo.subscribeToMessagePreviews("${space.id}/$SUBSCRIPTION_ID") 
                        .filter { it.space == space }
                }
                    .onFailure { 
                        logger.logWarning("DROID-2966 Error while getting initial previews for space ${space.id}: ${it.message}") 
                    }
                    .getOrDefault(emptyList())
                
                events
                    .subscribe("${space.id}/$SUBSCRIPTION_ID")
                    .scan(initial = initial) { previews, events ->
                        events.fold(previews) { state, event ->
                            when (event) {
                                is Event.Command.Chats.Add -> {
                                    state.map { preview ->
                                        if (preview.chat == event.context) {
                                            preview.copy(
                                                message = event.message,
                                                // Don't include dependencies for space-level container
                                                dependencies = emptyList()
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
                                                logger.logInfo("DROID-3799 Applying new chat preview state for space ${space.id} with order: ${newState.order}")
                                                preview.copy(state = newState)
                                            } else {
                                                logger.logInfo("DROID-3799 Skipping chat preview state update for space ${space.id} due to order comparison")
                                                preview
                                            }
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
                                    logger.logInfo("DROID-2966 Ignoring event for space ${space.id}: $event")
                                }
                            }
                        }
                    }
                    .flowOn(dispatchers.io)
                    .catch { 
                        logger.logException(it, "DROID-2966 Exception in space-level chat preview flow for space ${space.id}") 
                    }
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
                
                currentSpace?.let { space ->
                    runCatching {
                        repo.unsubscribeFromMessagePreviews("${space.id}/$SUBSCRIPTION_ID")
                    }.onFailure {
                        logger.logException(it, "DROID-3309 Error while unsubscribing from message previews for space ${space.id}")
                    }
                }
                
                currentSpace = null
            }
        }

        override fun observePreview(space: SpaceId): Flow<Chat.Preview?> {
            return previews.map {
                it.firstOrNull { preview -> preview.space.id == space.id }
            }
        }
        
        companion object {
            private const val SUBSCRIPTION_ID = "space-chat-previews"
        }
    }
}