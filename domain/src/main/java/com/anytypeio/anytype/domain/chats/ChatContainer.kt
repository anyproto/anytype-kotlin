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

    private val lastMessages = LinkedHashMap<Id, ChatMessageMeta>()

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

    fun watchWhileTrackingAttachments(chat: Id): Flow<ChatStreamState> {
        return watch(chat)
            .onEach { state ->
                val messages = state.messages
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

    fun watch(chat: Id): Flow<ChatStreamState> = flow {
        val initial = repo.subscribeLastChatMessages(
            command = Command.ChatCommand.SubscribeLastMessages(
                chat = chat,
                limit = DEFAULT_CHAT_PAGING_SIZE
            )
        ).also { result ->
            cacheLastMessages(result.messages)
        }

        val inputs: Flow<Transformation> = merge(
            channel.observe(chat).map { Transformation.Events.Payload(it) },
            payloads.map { Transformation.Events.Payload(it) },
            commands
        )

        emitAll(
            inputs.scan(
                initial = ChatStreamState(
                    messages = initial.messages,
                    state = initial.chatState ?: Chat.State()
                )
            ) { state, transform ->
                when (transform) {
                    Transformation.Commands.LoadPrevious -> {
                        ChatStreamState(
                            messages = loadThePreviousPage(state.messages, chat),
                            intent = Intent.None,
                            state = state.state
                        )
                    }
                    Transformation.Commands.LoadNext -> {
                        ChatStreamState(
                            messages = loadTheNextPage(state.messages, chat),
                            intent = Intent.None,
                            state = state.state
                        )
                    }
                    is Transformation.Commands.LoadAround -> {
                        val messages = try {
                            loadToMessage(chat, transform)
                        } catch (e: Exception) {
                            logger.logException(e, "DROID-2966 Error while loading reply context")
                            state.messages
                        }
                        ChatStreamState(
                            messages = messages,
                            intent = Intent.ScrollToMessage(transform.message),
                            state = state.state
                        )
                    }
                    is Transformation.Commands.LoadEnd -> {
                        if (state.messages.isNotEmpty()) {
                            val lastShown = state.messages.last()
                            val lastTracked = lastMessages.entries.first().value
                            if (lastShown.id == lastTracked.id) {
                                // No need to paginate.
                                if (state.state.hasUnReadMessages) {
                                    runCatching {
                                        repo.readChatMessages(
                                            Command.ChatCommand.ReadMessages(
                                                chat = chat,
                                                beforeOrderId = lastTracked.order,
                                                lastStateId = state.state.lastStateId
                                            )
                                        )
                                    }.onFailure {
                                        logger.logException(it, "DROID-2966 Error while reading messages")
                                    }
                                }
                                state.copy(
                                    intent = Intent.ScrollToBottom
                                )
                            } else {
                                val messages = try {
                                    loadToEnd(chat)
                                } catch (e: Exception) {
                                    state.messages.also {
                                        logger.logException(e, "DROID-2966 Error while scrolling to bottom")
                                    }
                                }
                                if (messages.isNotEmpty() && state.state.hasUnReadMessages) {
                                    runCatching {
                                        repo.readChatMessages(
                                            Command.ChatCommand.ReadMessages(
                                                chat = chat,
                                                beforeOrderId = messages.last().order,
                                                lastStateId = state.state.lastStateId
                                            )
                                        )
                                    }.onFailure {
                                        logger.logException(it, "DROID-2966 Error while reading messages")
                                    }
                                }
                                ChatStreamState(
                                    messages = messages,
                                    intent = Intent.ScrollToBottom,
                                    state = state.state
                                )
                            }
                        } else {
                            state
                        }
                    }
                    is Transformation.Events.Payload -> {
                        state.reduce(transform.events)
                    }
                }
            }.distinctUntilChanged()
        )
    }.catch { e ->
        emit(
            value = ChatStreamState(emptyList())
        ).also {
            logger.logException(e, "DROID-2966 Exception occurred in the chat container: $chat")
        }
    }

    @Throws
    private suspend fun loadToMessage(
        chat: Id,
        transform: Transformation.Commands.LoadAround
    ): List<Chat.Message> {

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
    private suspend fun loadToEnd(chat: Id): List<Chat.Message> {
        return repo.getChatMessages(
            Command.ChatCommand.GetMessages(
                chat = chat,
                beforeOrderId = null,
                afterOrderId = null,
                limit = DEFAULT_CHAT_PAGING_SIZE
            )
        ).messages
    }

    suspend fun onPayload(events: List<Event.Command.Chats>) {
        payloads.emit(events)
    }

    fun ChatStreamState.reduce(
        events: List<Event.Command.Chats>
    ): ChatStreamState {
        val messageList = this.messages.toMutableList()
        var countersState = this.state
        events.forEach { event ->
            when (event) {
                is Event.Command.Chats.Add -> {
                    if (!messageList.isInCurrentWindow(event.message.id)) {
                        val insertIndex = messageList.indexOfFirst { it.order > event.order }
                        if (insertIndex >= 0) {
                            messageList.add(insertIndex, event.message)
                        } else {
                            messageList.add(event.message)
                        }
                    }
                    // Tracking the last message in the chat tail
                    cacheLastMessage(event.message)
                }

                is Event.Command.Chats.Update -> {
                    if (messageList.isInCurrentWindow(event.id)) {
                        val index = messageList.indexOfFirst { it.id == event.id }
                        messageList[index] = event.message
                    }
                    // Tracking the last message in the chat tail
                    cacheLastMessage(event.message)
                }

                is Event.Command.Chats.Delete -> {
                    if (messageList.isInCurrentWindow(event.id)) {
                        val index = messageList.indexOfFirst { it.id == event.id }
                        messageList.removeAt(index)
                    }
                    // Tracking the last message in the chat tail
                    lastMessages.remove(event.id)
                }

                is Event.Command.Chats.UpdateReactions -> {
                    if (messageList.isInCurrentWindow(event.id)) {
                        val index = messageList.indexOfFirst { it.id == event.id }
                        if (messageList[index].reactions != event.reactions) {
                            messageList[index] = messageList[index].copy(reactions = event.reactions)
                        }
                    }
                }

                is Event.Command.Chats.UpdateMentionReadStatus -> {
                    val idsInWindow = event.messages.filter { messageList.isInCurrentWindow(it) }
                    idsInWindow.forEach { id ->
                        val index = messageList.indexOfFirst { it.id == id }
                        if (messageList[index].mentionRead != event.isRead) {
                            messageList[index] = messageList[index].copy(mentionRead = event.isRead)
                        }
                    }
                }

                is Event.Command.Chats.UpdateMessageReadStatus -> {
                    val idsInWindow = event.messages.filter { messageList.isInCurrentWindow(it) }
                    idsInWindow.forEach { id ->
                        val index = messageList.indexOfFirst { it.id == id }
                        if (messageList[index].read != event.isRead) {
                            messageList[index] = messageList[index].copy(read = event.isRead)
                        }
                    }
                }

                is Event.Command.Chats.UpdateState -> {
                    logger.logWarning(
                        "DROID-2966 Updating chat state, " +
                                "last state: ${this.state.lastStateId}, " +
                                "new state: ${event.state?.lastStateId}"
                    )
                    countersState = event.state ?: Chat.State()
                }
            }
        }

        return ChatStreamState(
            messages = messageList,
            state = countersState
        )
    }

    suspend fun onLoadPrevious() {
        commands.emit(Transformation.Commands.LoadPrevious)
    }

    suspend fun onLoadNext() {
        commands.emit(Transformation.Commands.LoadNext)
    }

    suspend fun onLoadToReply(replyMessage: Id) {
        logger.logInfo("DROID-2966 emitting onLoadToReply")
        commands.emit(Transformation.Commands.LoadAround(message = replyMessage))
    }

    suspend fun onLoadChatTail() {
        logger.logInfo("DROID-2966 emitting onLoadEnd")
        commands.emit(Transformation.Commands.LoadEnd)
    }

    private fun cacheLastMessages(messages: List<Chat.Message>) {
        messages.sortedByDescending { it.order } // Newest first
            .take(LAST_MESSAGES_MAX_SIZE)
            .forEach { cacheLastMessage(it) }
    }

    private fun cacheLastMessage(message: Chat.Message) {
        lastMessages[message.id] = ChatMessageMeta(message.id, message.order)
        // Ensure insertion order is preserved while trimming old entries
        if (lastMessages.size > LAST_MESSAGES_MAX_SIZE) {
            val oldestEntry = lastMessages.entries.first()
            lastMessages.remove(oldestEntry.key)
        }
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

            /**
             * Scroll-to-bottom behavior.
             */
            data object LoadEnd: Commands()
        }
    }

    companion object {
        const val DEFAULT_CHAT_PAGING_SIZE = 10
        private const val MAX_CHAT_CACHE_SIZE = 1000
        private const val LAST_MESSAGES_MAX_SIZE = 10
    }

    data class ChatMessageMeta(val id: Id, val order: String)

    /**
     * Messages sorted — from the oldest to the latest.
     */
    data class ChatStreamState(
        val messages: List<Chat.Message>,
        val state: Chat.State = Chat.State(),
        val intent: Intent = Intent.None
    )

    sealed class Intent {
        data class ScrollToMessage(val id: Id) : Intent()
        data class Highlight(val id: Id) : Intent()
        data object ScrollToBottom : Intent()
        data object None : Intent()
    }
}

private fun List<Chat.Message>.isInCurrentWindow(id: Id): Boolean {
    return this.any { it.id == id }
}