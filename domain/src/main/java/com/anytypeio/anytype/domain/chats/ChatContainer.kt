package com.anytypeio.anytype.domain.chats

import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.chats.Chat
import com.anytypeio.anytype.core_models.primitives.Space
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.debugging.Logger
import com.anytypeio.anytype.domain.library.StoreSearchByIdsParams
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.scan

class ChatContainer @Inject constructor(
    private val repo: BlockRepository,
    private val channel: ChatEventChannel,
    private val logger: Logger,
    private val subscription: StorelessSubscriptionContainer
) {

    private val lastMessages = LinkedHashMap<Id, ChatMessageMeta>()

    private val payloads = MutableSharedFlow<List<Event.Command.Chats>>()
    private val commands = MutableSharedFlow<Transformation.Commands>(replay = 0)

    private val attachments = MutableStateFlow<Set<Id>>(emptySet())
    private val replies = MutableStateFlow<Set<Id>>(emptySet())

    @OptIn(ExperimentalCoroutinesApi::class)
    fun subscribeToAttachments(chat: Id, space: Space) : Flow<Map<Id, ObjectWrapper.Basic>> {
        return attachments
            .flatMapLatest { ids ->
                subscription.subscribe(
                    searchParams = StoreSearchByIdsParams(
                        subscription = "$chat/$ATTACHMENT_SUBSCRIPTION_POSTFIX",
                        space = space,
                        targets = ids.toList(),
                        keys = ATTACHMENT_KEYS
                    )
                ).map { wrappers ->
                    wrappers.associateBy { it.id }
                }
            }
            .catch { e ->
                emit(emptyMap()).also {
                    logger.logException(e, "DROID-2966 Error in the chat attachments pub/sub flow")
                }
            }
    }

    /**
     * Subscribes to the chat object to receive updates about its state (isArchived, isDeleted, etc.)
     * Uses StorelessSubscriptionContainer instead of ObjectWatcher because archive/delete events
     * are sent via subscription channel (with spaceId as context), not via object open channel.
     */
    fun subscribeToChatObject(chat: Id, space: Space): Flow<ObjectWrapper.Basic?> {
        return subscription.subscribe(
            searchParams = StoreSearchByIdsParams(
                subscription = "$chat/$CHAT_OBJECT_SUBSCRIPTION_POSTFIX",
                space = space,
                targets = listOf(chat),
                keys = CHAT_OBJECT_KEYS
            )
        ).map { wrappers ->
            wrappers.firstOrNull()
        }.catch { e ->
            emit(null).also {
                logger.logException(e, "DROID-4200 Error in chat object subscription")
            }
        }
    }

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
            .map { messages -> messages.associateBy { it.id } }
            .catch { e ->
                emit(emptyMap()).also {
                    logger.logException(e, "DROID-2966 Error while fetching chat replies")
                }
            }
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

    suspend fun stop(chat: Id) {
        runCatching {
            repo.unsubscribeChat(chat)
            repo.cancelObjectSearchSubscription(
                listOf(
                    "$chat/$ATTACHMENT_SUBSCRIPTION_POSTFIX",
                    "$chat/$CHAT_OBJECT_SUBSCRIPTION_POSTFIX"
                )
            )
        }.onFailure {
            logger.logWarning("DROID-2966 Error while unsubscribing from chat:\n${it.message}")
        }.onSuccess {
            logger.logInfo("DROID-2966 Successfully unsubscribed from chat")
        }
    }

    fun watch(chat: Id): Flow<ChatStreamState> = flow {
        val response = repo.subscribeLastChatMessages(
            command = Command.ChatCommand.SubscribeLastMessages(
                chat = chat,
                limit = DEFAULT_CHAT_PAGING_SIZE
            )
        ).also { result ->
            cacheLastMessages(result.messages)
        }

        val initialState = response.chatState ?: Chat.State()

        var intent: Intent = Intent.None

        var initialUnreadSectionMessageId: Id? = null

        val initial = buildList {
            if (initialState.hasUnReadMessages && !initialState.oldestMessageOrderId.isNullOrEmpty()) {
                val lastUnreadMessage = response.messages.find { it.order == initialState.oldestMessageOrderId }
                if (lastUnreadMessage != null) {
                    // Last unread message is within the subscription results (the chat tail).
                    intent = Intent.ScrollToMessage(
                        id = lastUnreadMessage.id,
                        smooth = false,
                        startOfUnreadMessageSection = true
                    )
                    initialUnreadSectionMessageId = lastUnreadMessage.id
                    addAll(response.messages)
                } else {
                    // Fetching the unread-messages window — un-read message section is not within the chat tail.
                    val aroundUnread = loadAroundMessageOrder(
                        chat = chat,
                        order = initialState.oldestMessageOrderId.orEmpty()
                    ).also { messages ->
                        val target = messages.find { it.order == initialState.oldestMessageOrderId }
                        if (target != null) {
                            intent = Intent.ScrollToMessage(
                                id = target.id,
                                smooth = false,
                                startOfUnreadMessageSection = true
                            )
                            initialUnreadSectionMessageId = target.id
                        }
                    }
                    addAll(aroundUnread)
                }
            } else {
                // Starting with the latest messages.
                addAll(response.messages)
            }
        }

        val inputs: Flow<Transformation> = merge(
            channel.observe(chat).map { Transformation.Events.Payload(it) },
            payloads.map { Transformation.Events.Payload(it) },
            commands
        )

        emitAll(
            inputs.scan(
                initial = ChatStreamState(
                    messages = initial,
                    state = initialState,
                    intent = intent,
                    initialUnreadSectionMessageId = initialUnreadSectionMessageId
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
                            state = state.state,
                            initialUnreadSectionMessageId = null
                        )
                    }
                    is Transformation.Commands.LoadAround -> {
                        val messages = try {
                            loadAroundMessage(
                                chat = chat,
                                msg = transform.message
                            )
                        } catch (e: Exception) {
                            logger.logException(e, "DROID-2966 Error while loading reply context")
                            state.messages
                        }
                        ChatStreamState(
                            messages = messages,
                            intent = Intent.ScrollToMessage(
                                id = transform.message,
                                smooth = true,
                                highlight = true
                            ),
                            state = state.state
                        )
                    }
                    is Transformation.Commands.LoadEnd -> {
                        logger.logInfo("DROID-2966 intent while load end: $intent")
                        if (state.messages.isNotEmpty()) {
                            if (state.state.hasUnReadMessages) {
                                // Check if above the unread messages
                                val oldestReadOrderId = state.state.oldestMessageOrderId
                                val bottomMessage = state.messages.find {
                                    it.id == transform.lastVisibleMessage
                                }
                                if (bottomMessage != null && oldestReadOrderId != null) {
                                    if (bottomMessage.order < oldestReadOrderId) {
                                        // Scroll to the first unread message
                                        val messages = try {
                                            loadAroundMessageOrder(
                                                chat = chat,
                                                order = oldestReadOrderId
                                            )
                                        } catch (e: Exception) {
                                            logger.logException(e, "DROID-2966 Error while loading reply context")
                                            state.messages
                                        }
                                        ChatStreamState(
                                            messages = messages,
                                            intent = Intent.ScrollToBottom,
                                            state = state.state,
                                            initialUnreadSectionMessageId = initialUnreadSectionMessageId
                                        )
                                    } else {
                                        val messages = try {
                                            loadToEnd(chat)
                                        } catch (e: Exception) {
                                            state.messages.also {
                                                logger.logException(e, "DROID-2966 Error while scrolling to bottom")
                                            }
                                        }
                                        ChatStreamState(
                                            messages = messages,
                                            intent = Intent.ScrollToBottom,
                                            state = state.state,
                                            initialUnreadSectionMessageId = initialUnreadSectionMessageId
                                        )
                                    }
                                } else {
                                    val messages = try {
                                        loadToEnd(chat)
                                    } catch (e: Exception) {
                                        state.messages.also {
                                            logger.logException(e, "DROID-2966 Error while scrolling to bottom")
                                        }
                                    }
                                    ChatStreamState(
                                        messages = messages,
                                        intent = Intent.ScrollToBottom,
                                        state = state.state,
                                        initialUnreadSectionMessageId = null
                                    )
                                }
                            } else {
                                // TODO optimise by checking last message and last message in state
                                if (lastMessages.contains(transform.lastVisibleMessage)) {
                                    // No need to paginate, just scroll to bottom.
                                    state.copy(
                                        intent = Intent.ScrollToBottom
                                    )
                                } else {
                                    val messages = try {
                                        loadToEnd(chat).also {
                                            logger.logInfo("DROID-2966 Loaded chat tail because last message did not contained last visible message")
                                        }
                                    } catch (e: Exception) {
                                        state.messages.also {
                                            logger.logException(e, "DROID-2966 Error while scrolling to bottom")
                                        }
                                    }
                                    ChatStreamState(
                                        messages = messages,
                                        intent = Intent.ScrollToBottom,
                                        state = state.state,
                                        initialUnreadSectionMessageId = null
                                    )
                                }
                            }
                        } else {
                            state
                        }
                    }
                    is Transformation.Commands.GoToMention -> {
                        if (state.state.hasUnReadMentions) {
                            val oldestMentionOrderId = state.state.oldestMentionMessageOrderId
                            val messages = try {
                                loadAroundMessageOrder(
                                    chat = chat,
                                    order = oldestMentionOrderId.orEmpty()
                                )
                            } catch (e: Exception) {
                                state.messages.also {
                                    logger.logException(e, "DROID-2966 Error while loading mention context")
                                }
                            }
                            runCatching {
                                repo.readChatMessages(
                                    command = Command.ChatCommand.ReadMessages(
                                        chat = chat,
                                        beforeOrderId = oldestMentionOrderId,
                                        lastStateId = state.state.lastStateId,
                                        isMention = true
                                    )
                                )
                            }.onFailure {
                                logger.logWarning("DROID-2966 Error while reading mentions: ${it.message}")
                            }.onSuccess {
                                logger.logInfo("DROID-2966 Read mentions with success")
                            }
                            val target = messages.find { it.order == oldestMentionOrderId }
                            ChatStreamState(
                                messages = messages,
                                intent = if (target != null)
                                    Intent.ScrollToMessage(target.id, highlight = true)
                                else
                                    Intent.None,
                                state = state.state
                            )
                        } else {
                            state
                        }
                    }
                    is Transformation.Commands.ClearIntent -> {
                        state.copy(
                            intent = Intent.None
                        )
                    }
                    is Transformation.Commands.UpdateVisibleRange -> {
                        val counterState = state.state
                        val bottomVisibleMessage = state.messages.find { it.id == transform.from }
                        if (bottomVisibleMessage != null) {
                            // Reading messages older than bottomVisibleMessage
                            readMessagesWithinVisibleRange(counterState, bottomVisibleMessage, chat)
                            // Reading mentions older than bottomVisibleMessage
                            readMentionsWithinVisibleRange(counterState, bottomVisibleMessage, chat)
                        }
                        state
                    }
                    is Transformation.Events.Payload -> {
                        state.reduce(transform.events)
                    }
                }
            }.onEach {
                logger.logInfo("DROID-2966 New emission with intent: ${it.intent}")
            }.distinctUntilChanged()
        )
    }.catch { e ->
        emit(
            value = ChatStreamState(emptyList())
        ).also {
            logger.logException(e, "DROID-2966 Exception occurred in the chat container: $chat")
        }
    }

    /**
     * Marks unread mention messages as read if they fall within the currently visible message range.
     *
     * This function checks whether there are any unread mention messages in the current chat state,
     * and if the bottom-most visible message has an order ID greater than or equal to the order ID
     * of the oldest unread mention. If so, it sends a command to mark those mentions as read.
     *
     * @param countersState The current state of the chat, including unread mention metadata.
     * @param bottomVisibleMessage The lowest visible message in the current viewport.
     * @param chat The ID of the chat where the messages are being read.
     */
    private suspend fun readMentionsWithinVisibleRange(
        countersState: Chat.State,
        bottomVisibleMessage: Chat.Message,
        chat: Id
    ) {
        val oldestMentionOrderId = countersState.oldestMentionMessageOrderId
        val bottomOrder = bottomVisibleMessage.order

        if (
            countersState.hasUnReadMentions &&
            !oldestMentionOrderId.isNullOrEmpty() &&
            bottomOrder >= oldestMentionOrderId
        ) {
            runCatching {
                repo.readChatMessages(
                    command = Command.ChatCommand.ReadMessages(
                        chat = chat,
                        beforeOrderId = bottomOrder,
                        lastStateId = countersState.lastStateId.orEmpty(),
                        isMention = true
                    )
                )
            }.onFailure {
                logger.logWarning("DROID-2966 Error while reading mentions: ${it.message}")
            }.onSuccess {
                logger.logInfo("DROID-2966 Read mentions with success")
            }
        }
    }

    /**
     * Marks unread messages as read if they fall within the currently visible message range.
     *
     * This function checks whether there are any unread messages in the current chat state,
     * and if the bottom-most visible message has an order ID greater than or equal to the order ID
     * of the oldest unread message. If so, it sends a command to mark those messages as read.
     *
     * @param countersState The current state of the chat, including unread message metadata.
     * @param bottomVisibleMessage The lowest visible message in the current viewport.
     * @param chat The ID of the chat where the messages are being read.
     */
    private suspend fun readMessagesWithinVisibleRange(
        countersState: Chat.State,
        bottomVisibleMessage: Chat.Message,
        chat: Id
    ) {
        val oldestMessageOrderId = countersState.oldestMessageOrderId
        val bottomOrder = bottomVisibleMessage.order

        if (
            countersState.hasUnReadMessages &&
            !oldestMessageOrderId.isNullOrEmpty() &&
            bottomOrder >= oldestMessageOrderId
        ) {
            runCatching {
                repo.readChatMessages(
                    command = Command.ChatCommand.ReadMessages(
                        chat = chat,
                        beforeOrderId = bottomOrder,
                        lastStateId = countersState.lastStateId.orEmpty()
                    )
                )
            }.onFailure {
                logger.logWarning("DROID-2966 Error while reading messages: ${it.message}")
            }.onSuccess {
                logger.logInfo("DROID-2966 Read messages with success")
            }
        }
    }

    @Throws
    private suspend fun loadAroundMessage(
        chat: Id,
        msg: Id
    ): List<Chat.Message> {

        val replyMessage = repo.getChatMessagesByIds(
            Command.ChatCommand.GetMessagesByIds(
                chat = chat,
                messages = listOf(msg)
            )
        ).firstOrNull()

        if (replyMessage != null) {
            val loadedMessagesBefore = repo.getChatMessages(
                Command.ChatCommand.GetMessages(
                    chat = chat,
                    beforeOrderId = replyMessage.order,
                    limit = DEFAULT_CHAT_PAGING_SIZE / 2
                )
            ).messages

            val loadedMessagesAfter = repo.getChatMessages(
                Command.ChatCommand.GetMessages(
                    chat = chat,
                    afterOrderId = replyMessage.order,
                    limit = DEFAULT_CHAT_PAGING_SIZE / 2
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

    @Throws
    private suspend fun loadAroundMessageOrder(
        chat: Id,
        order: Id
    ): List<Chat.Message> {
        val loadedMessagesBefore = repo.getChatMessages(
            Command.ChatCommand.GetMessages(
                chat = chat,
                beforeOrderId = order,
                limit = DEFAULT_CHAT_PAGING_SIZE / 2
            )
        ).messages
        val loadedMessagesAfter = repo.getChatMessages(
            Command.ChatCommand.GetMessages(
                chat = chat,
                afterOrderId = order,
                limit = DEFAULT_CHAT_PAGING_SIZE / 2,
                includeBoundary = true
            )
        ).messages

        return buildList {
            addAll(loadedMessagesBefore)
            addAll(loadedMessagesAfter)
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
                        val index = messageList.indexOfFirst { it.id == event.message.id }
                        messageList[index] = event.message
                    }
                    // Tracking the last message in the chat tail
                    cacheLastMessage(event.message)
                }

                is Event.Command.Chats.Delete -> {
                    if (messageList.isInCurrentWindow(event.message)) {
                        val index = messageList.indexOfFirst { it.id == event.message }
                        messageList.removeAt(index)
                    }
                    // Tracking the last message in the chat tail
                    lastMessages.remove(event.message)
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
                is Event.Command.Chats.UpdateMessageSyncStatus -> {
                    val idsInWindow = event.messages.filter { messageList.isInCurrentWindow(it) }
                    idsInWindow.forEach { id ->
                        val index = messageList.indexOfFirst { it.id == id }
                        if (messageList[index].synced != event.isSynced) {
                            messageList[index] = messageList[index].copy(synced = event.isSynced)
                        }
                    }
                }
                is Event.Command.Chats.UpdateState -> {
                    logger.logWarning(
                        "DROID-2966 Updating chat state, " +
                                "last state: ${this.state.lastStateId}, " +
                                "new state: ${event.state?.lastStateId}"
                    )
                    val newState = event.state ?: Chat.State()
                    if (ChatStateUtils.shouldApplyNewChatState(
                            newOrder = newState.order,
                            currentOrder = countersState.order
                        )
                    ) {
                        logger.logInfo("DROID-3799 Applying new chat state with order: ${newState.order}")
                        countersState = newState
                    } else {
                        logger.logInfo("DROID-3799 Skipping chat state update due to order comparison")
                    }
                }
            }
        }

        return ChatStreamState(
            messages = messageList,
            state = countersState,
            initialUnreadSectionMessageId = initialUnreadSectionMessageId
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

    suspend fun onLoadChatTail(msg: Id?) {
        logger.logInfo("DROID-2966 emitting onLoadEnd")
        commands.emit(Transformation.Commands.LoadEnd(msg))
    }

    suspend fun onVisibleRangeChanged(from: Id, to: Id) {
        logger.logInfo("DROID-2966 onVisibleRangeChanged")
        commands.emit(Transformation.Commands.UpdateVisibleRange(from, to))
    }

    suspend fun onGoToMention() {
        logger.logInfo("DROID-2966 onGoToMention")
        commands.emit(Transformation.Commands.GoToMention)
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

    suspend fun onClearIntent() {
        logger.logInfo("DROID-2966 onClearIntent called")
        commands.emit(Transformation.Commands.ClearIntent)
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
            data class LoadEnd(val lastVisibleMessage: Id?): Commands()

            data class UpdateVisibleRange(val from: Id, val to: Id) : Commands()

            data object ClearIntent : Commands()

            data object GoToMention : Commands()
        }
    }

    companion object {
        const val DEFAULT_CHAT_PAGING_SIZE = 100
        // TODO reduce message size to reduce UI and VM overload.
        private const val MAX_CHAT_CACHE_SIZE = 1000
        private const val LAST_MESSAGES_MAX_SIZE = 10
        private const val ATTACHMENT_SUBSCRIPTION_POSTFIX = "attachments"
        private const val CHAT_OBJECT_SUBSCRIPTION_POSTFIX = "chat-object-details-subscription"

        val CHAT_OBJECT_KEYS = listOf(
            Relations.ID,
            Relations.SPACE_ID,
            Relations.NAME,
            Relations.ICON_IMAGE,
            Relations.ICON_EMOJI,
            Relations.ICON_NAME,
            Relations.ICON_OPTION,
            Relations.TYPE,
            Relations.LAYOUT,
            Relations.IS_ARCHIVED,
            Relations.IS_DELETED,
            Relations.SYNC_STATUS
        )

        val ATTACHMENT_KEYS = listOf(
            Relations.ID,
            Relations.SPACE_ID,
            Relations.PICTURE,
            Relations.SOURCE,
            Relations.DESCRIPTION,
            Relations.NAME,
            Relations.ICON_IMAGE,
            Relations.ICON_EMOJI,
            Relations.ICON_NAME,
            Relations.ICON_OPTION,
            Relations.TYPE,
            Relations.LAYOUT,
            Relations.IS_ARCHIVED,
            Relations.IS_DELETED,
            Relations.DONE,
            Relations.SNIPPET,
            Relations.SIZE_IN_BYTES,
            Relations.FILE_MIME_TYPE,
            Relations.FILE_EXT,
            Relations.SYNC_STATUS
        )
    }

    data class ChatMessageMeta(val id: Id, val order: String)

    /**
     * Messages sorted — from the oldest to the latest.
     * @property [initialUnreadSectionMessageId] used when opening chat with unread messages.
     */
    data class ChatStreamState(
        val messages: List<Chat.Message>,
        val state: Chat.State = Chat.State(),
        val intent: Intent = Intent.None,
        val initialUnreadSectionMessageId: String? = null
    )

    sealed class Intent {
        /**
         * Represents an intent to scroll to a specific message in the chat.
         *
         * @param id The unique identifier of the message to scroll to.
         * @param smooth Determines whether the scrolling should be smooth (animated) or instantaneous.
         *               Defaults to `false` for performance reasons, as smooth scrolling may introduce
         *               delays or unnecessary animations in certain scenarios.
         */
        data class ScrollToMessage(
            val id: Id,
            val smooth: Boolean = false,
            val startOfUnreadMessageSection: Boolean = false,
            val highlight: Boolean = false
        ) : Intent()
        data object ScrollToBottom : Intent()
        data object None : Intent()
    }
}

private fun List<Chat.Message>.isInCurrentWindow(id: Id): Boolean {
    return this.any { it.id == id }
}