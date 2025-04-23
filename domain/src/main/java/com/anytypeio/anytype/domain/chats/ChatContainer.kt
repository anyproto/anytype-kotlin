package com.anytypeio.anytype.domain.chats

import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.chats.Chat
import com.anytypeio.anytype.core_models.primitives.Space
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.debugging.Logger
import javax.inject.Inject
import kotlin.collections.isNotEmpty
import kotlin.collections.toList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.scan

class ChatContainer @Inject constructor(
    private val repo: BlockRepository,
    private val channel: ChatEventChannel,
    private val logger: Logger
) {

    private val _replyContextState = MutableStateFlow<ReplyContextState>(ReplyContextState.Idle)
    val replyContextState: StateFlow<ReplyContextState> = _replyContextState

    private val payloads = MutableSharedFlow<List<Event.Command.Chats>>()
    private val commands = MutableSharedFlow<Transformation.Commands>(replay = 0)

    private val attachments = MutableStateFlow<Set<Id>>(emptySet())
    private val replies = MutableStateFlow<Set<Id>>(emptySet())

    // TODO Naive implementation. Add caching logic
    fun fetchAttachments(space: Space) : Flow<Map<Id, ObjectWrapper.Basic>> {
        return attachments
            .map { ids ->
                if (ids.isNotEmpty()) {
                    repo.searchObjects(
                        sorts = emptyList(),
                        limit = 0,
                        filters = buildList {
                            DVFilter(
                                relation = Relations.ID,
                                value = ids.toList(),
                                condition = DVFilterCondition.IN
                            )
                        },
                        keys = emptyList(),
                        space = space
                    ).mapNotNull {
                        val wrapper = ObjectWrapper.Basic(it)
                        if (wrapper.isValid) wrapper else null
                    }
                } else {
                    emptyList()
                }
            }
            .distinctUntilChanged()
            .map { wrappers -> wrappers.associate { it.id to it } }
    }

    // TODO Naive implementation. Add caching logic
    fun fetchReplies(chat: Id) : Flow<Map<Id, Chat.Message>> {
        return replies
            .map { ids ->
                if (ids.isNotEmpty()) {
                    repo.getChatMessagesByIds(
                        command = Command.ChatCommand.GetMessagesByIds(
                            chat = chat,
                            messages = ids.toList()
                        )
                    )
                } else {
                    emptyList()
                }
            }
            .distinctUntilChanged()
            .map { messages -> messages.associate { it.id to it } }
    }

    fun watchWhileTrackingAttachments(chat: Id): Flow<List<Chat.Message>> {
        return watch(chat)
            .onEach { messages ->
                val repliesIds = mutableSetOf<Id>()
                val attachmentsIds = mutableSetOf<Id>()
                messages.forEach { msg ->
                    attachmentsIds.addAll(msg.attachments.map { it.target })
                    if (!msg.replyToMessageId.isNullOrEmpty()) {
                        repliesIds.add(msg.replyToMessageId.orEmpty())
                    }
                }
                attachments.value = attachmentsIds
                replies.value = repliesIds
            }
    }

    fun watch(chat: Id): Flow<List<Chat.Message>> = flow {
        val initial = repo.subscribeLastChatMessages(
            command = Command.ChatCommand.SubscribeLastMessages(
                chat = chat,
                limit = DEFAULT_CHAT_PAGING_SIZE
            )
        )

        val inputs: Flow<Transformation> = merge(
            channel.observe(chat).map { Transformation.Events.Payload(it) },
            payloads.map { Transformation.Events.Payload(it) },
            commands
        )

        emitAll(
            inputs.scan(initial = initial.messages) { state, transform ->
                when(transform) {
                    Transformation.Commands.LoadPrevious -> {
                        loadThePreviousPage(state, chat)
                    }
                    Transformation.Commands.LoadNext -> {
                        loadTheNextPage(state, chat)
                    }
                    is Transformation.Commands.LoadAround -> {
                        try {
                            loadToMessage(chat, transform)
                        } catch (e: Exception) {
                            state.also {
                                logger.logException(e, "DROID-2966 Error while loading reply context")
                            }
                        }
                    }
                    is Transformation.Events.Payload -> {
                        state.reduce(transform.events)
                    }
                    is Transformation.Commands.LoadEnd -> {
                        try {
                            loadToEnd(chat = chat)
                        } catch (e: Exception) {
                            state.also {
                                logger.logException(e, "DROID-2966 Error while scrolling to bottom")
                            }
                        }
                    }
                }
            }.distinctUntilChanged()
        )
    }.catch { e ->
            emit(value = emptyList()).also {
                logger.logException(e, "DROID-2966 Exception occurred in the chat container: $chat")
            }
    }

    @Throws
    private suspend fun loadToMessage(
        chat: Id,
        transform: Transformation.Commands.LoadAround
    ): List<Chat.Message> {

        _replyContextState.value = ReplyContextState.Loading(target = transform.message)

        val replyMessage = repo.getChatMessagesByIds(
            Command.ChatCommand.GetMessagesByIds(
                chat = chat,
                messages = listOf(transform.message)
            )
        ).firstOrNull()

        if (replyMessage != null) {
            val loadedMessagesBefore = repo.getChatMessages(
                Command.ChatCommand.GetMessages(
                    chat = chat,
                    beforeOrderId = replyMessage.order,
                    limit = DEFAULT_CHAT_PAGING_SIZE
                )
            ).messages

            val loadedMessagesAfter = repo.getChatMessages(
                Command.ChatCommand.GetMessages(
                    chat = chat,
                    afterOrderId = replyMessage.order,
                    limit = DEFAULT_CHAT_PAGING_SIZE
                )
            ).messages

            _replyContextState.value = ReplyContextState.Loaded(target = transform.message)

            return buildList {
                addAll(loadedMessagesBefore)
                add(replyMessage)
                addAll(loadedMessagesAfter)
            }

        } else {
            throw IllegalStateException("DROID-2966 Could not fetch replyMessage")
        }
    }

    private suspend fun loadTheNextPage(
        state: List<Chat.Message>,
        chat: Id
    ): List<Chat.Message> = try {
        val last = state.lastOrNull()
        if (last != null) {
            val next = repo.getChatMessages(
                Command.ChatCommand.GetMessages(
                    chat = chat,
                    afterOrderId = last.order,
                    limit = DEFAULT_CHAT_PAGING_SIZE
                )
            )
            state + next.messages
        } else {
            state.also {
                logger.logWarning("DROID-2966 The last message not found in chat")
            }
        }
    } catch (e: Exception) {
        state.also {
            logger.logException(e, "DROID-2966 Error while loading previous page in chat $chat")
        }
    }

    private suspend fun loadThePreviousPage(
        state: List<Chat.Message>,
        chat: Id
    ): List<Chat.Message> = try {
        val first = state.firstOrNull()
        if (first != null) {
            val previous = repo.getChatMessages(
                Command.ChatCommand.GetMessages(
                    chat = chat,
                    beforeOrderId = first.order,
                    limit = DEFAULT_CHAT_PAGING_SIZE
                )
            )
            previous.messages + state
        } else {
            state.also {
                logger.logWarning("DROID-2966 The first message not found in chat")
            }
        }
    } catch (e: Exception) {
        state.also {
            logger.logException(e, "DROID-2966 Error while loading next page in chat: $chat")
        }
    }

    @Throws
    private suspend fun loadToEnd(chat: Id) : List<Chat.Message> {
        val lastFetched = repo.getChatMessages(
            Command.ChatCommand.GetMessages(
                chat = chat,
                beforeOrderId = null,
                afterOrderId = null,
                limit = 1
            )
        ).messages.firstOrNull()

        if (lastFetched == null) {
            // Chat is empty
            return emptyList()
        } else {
            val previous = repo.getChatMessages(
                Command.ChatCommand.GetMessages(
                    chat = chat,
                    beforeOrderId = lastFetched.order,
                    afterOrderId = null,
                    limit = DEFAULT_CHAT_PAGING_SIZE
                )
            )
            return buildList {
                addAll(previous.messages)
                add(lastFetched)
                _replyContextState.value = ReplyContextState.Loaded(target = lastFetched.id)
            }
        }
    }

    suspend fun onPayload(events: List<Event.Command.Chats>) {
        payloads.emit(events)
    }

    fun List<Chat.Message>.reduce(events: List<Event.Command.Chats>): List<Chat.Message> {
        val result = this.toMutableList()
        events.forEach { event ->
            when (event) {
                is Event.Command.Chats.Add -> {
                    if (result.none { it.id == event.message.id }) {
                        val insertIndex = result.indexOfFirst { it.order > event.order }
                        if (insertIndex >= 0) {
                            result.add(insertIndex, event.message)
                        } else {
                            result.add(event.message)
                        }
                    }
                }
                is Event.Command.Chats.Delete -> {
                    val index = result.indexOfFirst { it.id == event.id }
                    if (index >= 0) result.removeAt(index)
                }
                is Event.Command.Chats.Update -> {
                    val index = result.indexOfFirst { it.id == event.id }
                    if (index >= 0 && result[index] != event.message) {
                        result[index] = event.message
                    }
                }
                is Event.Command.Chats.UpdateReactions -> {
                    val index = result.indexOfFirst { it.id == event.id }
                    if (index >= 0 && result[index].reactions != event.reactions) {
                        result[index] = result[index].copy(reactions = event.reactions)
                    }
                }
                is Event.Command.Chats.UpdateMentionReadStatus -> {
                    event.messages.forEach { id ->
                        val index = result.indexOfFirst { it.id == id }
                        if (index >= 0 && result[index].mentionRead != event.isRead) {
                            result[index] = result[index].copy(mentionRead = event.isRead)
                        }
                    }
                }
                is Event.Command.Chats.UpdateMessageReadStatus -> {
                    event.messages.forEach { id ->
                        val index = result.indexOfFirst { it.id == id }
                        if (index >= 0 && result[index].read != event.isRead) {
                            result[index] = result[index].copy(read = event.isRead)
                        }
                    }
                }
                is Event.Command.Chats.UpdateState -> {
                   // TODO handle event
                }
            }
        }
        return result
    }

    suspend fun onLoadPrevious() {
        if (replyContextState.value !is ReplyContextState.Loading) {
            logger.logInfo("DROID-2966 emitting onLoadNextPage")
            commands.emit(Transformation.Commands.LoadPrevious)
        } else {
            logger.logInfo("DROID-2966 onLoadNextPage: scroll suppressed, state: ${replyContextState.value}")
        }
    }

    suspend fun onLoadNext() {
        if (replyContextState.value is ReplyContextState.Idle) {
            logger.logInfo("DROID-2966 emitting onLoadPreviousPage")
            commands.emit(Transformation.Commands.LoadNext)
        } else {
            logger.logInfo("DROID-2966 onLoadPreviousPage: scroll suppressed, state: ${replyContextState.value} ")
        }
    }

    suspend fun onLoadToReply(replyMessage: Id) {
        logger.logInfo("DROID-2966 emitting onLoadToReply")
        commands.emit(Transformation.Commands.LoadAround(message = replyMessage))
    }

    suspend fun onLoadEnd() {
        logger.logInfo("DROID-2966 emitting onLoadToEnd")
        commands.emit(Transformation.Commands.LoadEnd)
    }

    fun onResetReplyToContext() {
        logger.logInfo("DROID-2966 resetting reply context")
        _replyContextState.value = ReplyContextState.Idle
    }

    internal sealed class Transformation {
        sealed class Events : Transformation() {
            data class Payload(val events: List<Event.Command.Chats>) : Events()
        }
        sealed class Commands : Transformation() {
            /**
             * Loading next — older — messages in history.
             * Loading the previous page if it exists.
             */
            data object LoadPrevious : Commands()

            /**
             * Loading next — more recent — messages in history.
             * Loading the next page if it exists.
             */
            data object LoadNext : Commands()

            /**
             * Loading message before and current given (reply) message.
             */
            data class LoadAround(val message: Id) : Commands()

            data object LoadEnd: Commands()
        }
    }

    companion object {
        const val DEFAULT_CHAT_PAGING_SIZE = 10
    }
}

sealed class ReplyContextState {
    object Idle : ReplyContextState()
    data class Loading(val target: Id) : ReplyContextState()
    data class Loaded(val target: Id) : ReplyContextState()
    data class Error(val target: Id, val throwable: Throwable) : ReplyContextState()
}