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
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.launch

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

    /**
     * Reply-target messages resolved for the currently tracked reply ids.
     * Entries are kept in sync by [reduce]: an Update/Delete event for a cached
     * reply target updates/removes the entry and bumps [replyCacheInvalidations],
     * so [fetchReplies] re-emits the refreshed map even though the id set is unchanged.
     */
    private val replyMessageCache = ConcurrentHashMap<Id, Chat.Message>()
    private val replyCacheInvalidations = MutableStateFlow(0L)

    /**
     * Reply-target ids the middleware is known not to return (e.g. quoted messages
     * deleted before they were cached). Without this negative cache, such ids would
     * stay "missing" forever and [fetchReplies] would re-issue a GetMessagesByIds
     * round-trip for them on every emission. Pruned alongside [replyMessageCache]
     * to the currently tracked ids.
     */
    private val knownMissingReplyTargets = ConcurrentHashMap.newKeySet<Id>()

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
        // Incremental cache: only newly tracked reply ids are fetched from the middleware
        // instead of re-fetching the whole set whenever it changes. Edits and deletions
        // of already-cached reply targets are applied directly by [reduce], which then
        // bumps [replyCacheInvalidations] to trigger a re-emission of the map.
        return combine(replies, replyCacheInvalidations) { ids, _ -> ids }
            .map { ids ->
                val missing = ids.filter { id ->
                    !replyMessageCache.containsKey(id) && id !in knownMissingReplyTargets
                }
                if (missing.isNotEmpty()) {
                    val fetched = repo.getChatMessagesByIds(
                        command = Command.ChatCommand.GetMessagesByIds(
                            chat = chat,
                            messages = missing
                        )
                    )
                    fetched.forEach { msg ->
                        replyMessageCache[msg.id] = msg
                    }
                    // Ids the middleware did not return can never resolve: tombstone
                    // them so they are not re-requested on every subsequent emission.
                    val returned = fetched.mapTo(mutableSetOf()) { it.id }
                    missing.forEach { id ->
                        if (id !in returned) knownMissingReplyTargets.add(id)
                    }
                }
                // Drop replies which are no longer tracked by the current window.
                replyMessageCache.keys.retainAll(ids)
                knownMissingReplyTargets.retainAll(ids)
                replyMessageCache.toMap()
            }
            .distinctUntilChanged()
            .catch { e ->
                emit(emptyMap()).also {
                    logger.logException(e, "DROID-2966 Error while fetching chat replies")
                }
            }
    }

    fun watchWhileTrackingAttachments(chat: Id, startAtMessage: Id? = null): Flow<ChatStreamState> {
        return watch(chat, startAtMessage)
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

    fun watch(chat: Id, startAtMessage: Id? = null): Flow<ChatStreamState> = flow {
        coroutineScope {
            val scope = this

            // Attach the event collector BEFORE the subscribe RPC: events arriving while the
            // initial window is being built are buffered and replayed into the fold below
            // instead of being dropped — events are only delivered to already-attached
            // collectors. Replayed events which are already reflected in the initial window
            // are deduplicated by the fold itself (see [reduce]).
            //
            // The buffer is deliberately UNLIMITED: a bounded buffer would have to drop
            // events, which is never acceptable for sync-critical chat state. In practice
            // the buffer only grows while the fold is busy (e.g. a paging RPC is in
            // flight), so its size is bounded by the event rate during that short window.
            val bufferedEvents = Channel<List<Event.Command.Chats>>(capacity = Channel.UNLIMITED)
            scope.launch(start = CoroutineStart.UNDISPATCHED) {
                channel.observe(chat).collect { events ->
                    bufferedEvents.send(events)
                }
            }

            // Read receipts are side effects that must not block the event fold, but
            // firing one coroutine per visible-range change floods the middleware with
            // overlapping round-trips while the user scrolls. A CONFLATED channel
            // drained by a single worker keeps only the newest range and at most one
            // in-flight round-trip.
            val visibleRangeReads = Channel<Pair<Chat.State, Chat.Message>>(capacity = Channel.CONFLATED)
            scope.launch {
                for ((counterState, bottomVisibleMessage) in visibleRangeReads) {
                    // Reading messages older than bottomVisibleMessage
                    readMessagesWithinVisibleRange(counterState, bottomVisibleMessage, chat)
                    // Reading mentions older than bottomVisibleMessage
                    readMentionsWithinVisibleRange(counterState, bottomVisibleMessage, chat)
                }
            }

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

            // The window front for which history is known to be exhausted — avoids repeated
            // empty round-trips when the user keeps bouncing off the top of the chat.
            var noMoreMessagesBeforeOrder: Id? = null

            // The newest message the UI last reported as visible, or null while no visible
            // range has been reported yet. Scoped to this collection so it resets on
            // re-subscription and can never leak between chats.
            var newestVisibleMessageId: Id? = null

            // A requested start position (e.g. opening a search result at its message)
            // wins over the unread-section positioning; on failure (message deleted
            // between search and open) fall through to the default flow.
            val aroundStart = if (startAtMessage != null) {
                runCatching { loadAroundMessage(chat = chat, msg = startAtMessage) }
                    .onFailure { logger.logWarning("DROID-2966 Could not load window around start message:\n${it.message}") }
                    .getOrNull()
            } else {
                null
            }

            val initial = buildList {
                if (aroundStart != null) {
                    intent = Intent.ScrollToMessage(
                        id = startAtMessage.orEmpty(),
                        smooth = false,
                        highlight = true
                    )
                    addAll(aroundStart)
                } else if (initialState.hasUnReadMessages && !initialState.oldestMessageOrderId.isNullOrEmpty()) {
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
                bufferedEvents.consumeAsFlow().map { Transformation.Events.Payload(it) },
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
                            val first = state.messages.firstOrNull()
                            if (first != null && first.order == noMoreMessagesBeforeOrder) {
                                // Beginning of history was already reached for this window front —
                                // skip the round-trip.
                                state.copy(intent = Intent.None)
                            } else {
                                val previousPage = loadThePreviousPage(first, chat)
                                if (previousPage != null && previousPage.isEmpty() && first != null) {
                                    noMoreMessagesBeforeOrder = first.order
                                }
                                // See loadTheNextPage: overlapping pages must not produce
                                // duplicate ids in the window.
                                val known = state.messages.mapTo(HashSet()) { it.id }
                                ChatStreamState(
                                    messages = (
                                            previousPage.orEmpty().filter { known.add(it.id) } + state.messages
                                            ).trimKeepingOldest(),
                                    intent = Intent.None,
                                    state = state.state,
                                    initialUnreadSectionMessageId = state.initialUnreadSectionMessageId
                                )
                            }
                        }
                        Transformation.Commands.LoadNext -> {
                            ChatStreamState(
                                messages = loadTheNextPage(state.messages, chat).trimKeepingNewest(),
                                intent = Intent.None,
                                state = state.state,
                                // Preserved, mirroring LoadPrevious: the first forward page is
                                // precisely where the unread messages are. The divider clears
                                // itself once its anchor leaves the window, since the view model
                                // only renders it for a message the window still holds.
                                initialUnreadSectionMessageId = state.initialUnreadSectionMessageId
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
                            // [from] is the NEWEST visible message: the UI list is reversed,
                            // so the lowest visible index is the most recent message.
                            newestVisibleMessageId = transform.from
                            val counterState = state.state
                            val bottomVisibleMessage = state.messages.find { it.id == transform.from }
                            if (bottomVisibleMessage != null) {
                                // Conflated hand-off: never blocks the fold, and rapid
                                // scroll bursts collapse to the newest range instead of
                                // one read-receipt round-trip per emission.
                                visibleRangeReads.trySend(counterState to bottomVisibleMessage)
                            }
                            state
                        }
                        is Transformation.Events.Payload -> {
                            var strandedTailMessage = false
                            val reduced = state.reduce(transform.events) {
                                strandedTailMessage = true
                            }
                            if (strandedTailMessage &&
                                isParkedAtWindowTail(reduced.messages, newestVisibleMessageId)
                            ) {
                                // DROID-4556: the user is parked at the newest edge of a window
                                // detached from the chat tail. Nothing is below them to scroll
                                // into and the window size never changes, so the UI's
                                // bottom-reach detector never re-arms and the stranded message
                                // can never enter the window through user-driven LoadNext
                                // paging — the chat looks frozen until the user scrolls up and
                                // back down.
                                //
                                // Extend the window forward here, through the same contiguous
                                // paging path LoadNext uses, so no range is skipped.
                                //
                                // Exactly ONE page: a successful extension moves the window
                                // tail past [newestVisibleMessageId], so a burst of stranded
                                // messages cannot cascade into a round-trip per event, and the
                                // size change re-arms the UI detector — handing paging back to
                                // the user. Looping until attached would instead block the fold
                                // on N round-trips and let trimKeepingNewest evict the history
                                // under the user's scroll anchor.
                                val extended = loadTheNextPage(reduced.messages, chat)
                                if (extended.size != reduced.messages.size) {
                                    reduced.copy(messages = extended.trimKeepingNewest())
                                } else {
                                    // Empty page or a failed round-trip: the window tail has not
                                    // moved, so the next incoming message retries.
                                    reduced
                                }
                            } else {
                                reduced
                            }
                        }
                    }
                }.onEach {
                    logger.logInfo("DROID-2966 New emission with intent: ${it.intent}")
                }.distinctUntilChanged()
            )
        }
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
            // The window is rendered by a LazyColumn keyed on message id, and duplicate
            // keys throw — so a page overlapping the window (e.g. one echoing the boundary
            // message) must never introduce a duplicate.
            val known = state.mapTo(HashSet()) { it.id }
            state + next.messages.filter { known.add(it.id) }
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

    /**
     * Loads the page of messages preceding [first].
     *
     * @return the fetched page — possibly empty, meaning the beginning of history was reached —
     * or null if there is no anchor message or the page could not be fetched.
     */
    private suspend fun loadThePreviousPage(
        first: Chat.Message?,
        chat: Id
    ): List<Chat.Message>? = try {
        if (first != null) {
            repo.getChatMessages(
                Command.ChatCommand.GetMessages(
                    chat = chat,
                    beforeOrderId = first.order,
                    limit = DEFAULT_CHAT_PAGING_SIZE
                )
            ).messages
        } else {
            logger.logWarning("DROID-2966 The first message not found in chat")
            null
        }
    } catch (e: Exception) {
        logger.logException(e, "DROID-2966 Error while loading next page in chat: $chat")
        null
    }

    /**
     * Keeps the message window bounded by [MAX_CHAT_CACHE_SIZE]: every fold copy, equality
     * check and UI mapping downstream scales with the window size, so the window must not
     * grow unboundedly as pages are loaded.
     */
    private fun List<Chat.Message>.trimKeepingOldest(): List<Chat.Message> =
        if (size > MAX_CHAT_CACHE_SIZE) take(MAX_CHAT_CACHE_SIZE) else this

    private fun List<Chat.Message>.trimKeepingNewest(): List<Chat.Message> =
        if (size > MAX_CHAT_CACHE_SIZE) takeLast(MAX_CHAT_CACHE_SIZE) else this

    /**
     * Whether the current message window includes the newest known chat message.
     *
     * The newest known message is taken from [lastMessages] — the cache of tail
     * messages seen at subscribe time or via Add/Update events. When the window
     * does not contain it — e.g. after [trimKeepingOldest] evicted the tail while
     * paging back, or after [loadAroundMessage] replaced the window — appending a
     * fresh message to the window would display it directly after a much older
     * message, silently hiding the not-loaded range in between.
     */
    private fun isWindowAttachedToChatTail(window: List<Chat.Message>): Boolean {
        if (window.isEmpty()) return true
        val newestKnown = lastMessages.values.maxByOrNull { it.order } ?: return true
        return window.any { it.id == newestKnown.id }
    }

    /**
     * Whether the user is parked at the newest edge of [window] — with nothing below them
     * left to scroll into.
     *
     * Conservative by design: while no visible range has been reported (the chat has just
     * opened, or an initial scroll intent is still being applied) [newestVisibleMessageId]
     * is null and the window is never auto-extended. A user reading old history is therefore
     * never yanked by [trimKeepingNewest] evicting the messages under their scroll anchor.
     */
    private fun isParkedAtWindowTail(
        window: List<Chat.Message>,
        newestVisibleMessageId: Id?
    ): Boolean {
        if (newestVisibleMessageId == null || window.isEmpty()) return false
        return window.last().id == newestVisibleMessageId
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

    /**
     * @param onTailMessageStranded invoked when a newly added message could not enter the
     * window because the window is detached from the chat tail. The caller decides how to
     * recover — see DROID-4556.
     */
    fun ChatStreamState.reduce(
        events: List<Event.Command.Chats>,
        onTailMessageStranded: () -> Unit = {}
    ): ChatStreamState {
        val messageList = this.messages.toMutableList()
        var countersState = this.state
        events.forEach { event ->
            when (event) {
                is Event.Command.Chats.Add -> {
                    if (messageList.none { it.id == event.message.id }) {
                        val insertIndex = messageList.indexOfFirst { it.order > event.order }
                        if (insertIndex >= 0) {
                            messageList.add(insertIndex, event.message)
                        } else if (isWindowAttachedToChatTail(messageList)) {
                            messageList.add(event.message)
                        } else {
                            // The window is detached from the chat tail (older history is
                            // being browsed after the newest edge was trimmed or the window was
                            // replaced by a jump-to-message). Appending here would render the new
                            // message right after a much older one, hiding the not-yet-loaded
                            // range between them — and LoadNext would then paginate from the
                            // appended message, permanently skipping that range. The message is
                            // still tracked in [lastMessages] and will enter the window through
                            // contiguous LoadNext paging or a LoadEnd reload of the tail.
                            //
                            // Neither of those is dispatched while the user sits at the window's
                            // newest edge, so the caller is told the tail was stranded.
                            onTailMessageStranded()
                        }
                    }
                    // A (re)added message is fetchable again: clear any tombstone so
                    // fetchReplies may resolve it as a reply target.
                    knownMissingReplyTargets.remove(event.message.id)
                    // Tracking the last message in the chat tail
                    cacheLastMessage(event.message)
                }

                is Event.Command.Chats.Update -> {
                    val index = messageList.indexOfFirst { it.id == event.message.id }
                    if (index != -1) {
                        messageList[index] = event.message
                    }
                    // Keep the reply-preview cache in sync when a quoted message is edited.
                    if (replyMessageCache.replace(event.message.id, event.message) != null) {
                        replyCacheInvalidations.value++
                    }
                    // Tracking the last message in the chat tail
                    cacheLastMessage(event.message)
                }

                is Event.Command.Chats.Delete -> {
                    val index = messageList.indexOfFirst { it.id == event.message }
                    if (index != -1) {
                        messageList.removeAt(index)
                    }
                    // Drop the reply-preview cache entry when a quoted message is deleted,
                    // and tombstone the id: the middleware can never return it again, so
                    // fetchReplies must not keep re-requesting it.
                    knownMissingReplyTargets.add(event.message)
                    if (replyMessageCache.remove(event.message) != null) {
                        replyCacheInvalidations.value++
                    }
                    // Tracking the last message in the chat tail
                    lastMessages.remove(event.message)
                }

                is Event.Command.Chats.UpdateReactions -> {
                    val index = messageList.indexOfFirst { it.id == event.id }
                    if (index != -1 && messageList[index].reactions != event.reactions) {
                        messageList[index] = messageList[index].copy(reactions = event.reactions)
                    }
                }

                is Event.Command.Chats.UpdateMentionReadStatus -> {
                    event.messages.forEach { id ->
                        val index = messageList.indexOfFirst { it.id == id }
                        if (index != -1 && messageList[index].mentionRead != event.isRead) {
                            messageList[index] = messageList[index].copy(mentionRead = event.isRead)
                        }
                    }
                }

                is Event.Command.Chats.UpdateMessageReadStatus -> {
                    event.messages.forEach { id ->
                        val index = messageList.indexOfFirst { it.id == id }
                        if (index != -1 && messageList[index].read != event.isRead) {
                            messageList[index] = messageList[index].copy(read = event.isRead)
                        }
                    }
                }
                is Event.Command.Chats.UpdateMessageSyncStatus -> {
                    event.messages.forEach { id ->
                        val index = messageList.indexOfFirst { it.id == id }
                        if (index != -1 && messageList[index].synced != event.isSynced) {
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