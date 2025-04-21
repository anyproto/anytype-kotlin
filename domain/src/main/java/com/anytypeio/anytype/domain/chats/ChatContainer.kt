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
    private val payloads = MutableSharedFlow<List<Event.Command.Chats>>()
    private val commands = MutableSharedFlow<Transformation.Commands>(replay = 0)

    private val attachments = MutableStateFlow<Set<Id>>(emptySet())
    private val replies = MutableStateFlow<Set<Id>>(emptySet())

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

    @Deprecated("Naive implementation. Add caching logic")
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
                    Transformation.Commands.LoadBefore -> {
                        loadThePreviousPage(state, chat)
                    }
                    Transformation.Commands.LoadAfter -> {
                        loadTheNextPage(state, chat)
                    }
                    is Transformation.Commands.LoadTo -> {
                        loadToMessage(chat, transform)
                    }
                    is Transformation.Events.Payload -> {
                        state.reduce(transform.events)
                    }
                }
            }
        )
    }.catch { e ->
        emit(value = emptyList()).also {
            logger.logException(e, "Exception in chat container")
        }
    }

    private suspend fun loadToMessage(
        chat: Id,
        transform: Transformation.Commands.LoadTo
    ): List<Chat.Message> {
        val replyMessage = repo.getChatMessagesByIds(
            Command.ChatCommand.GetMessagesByIds(
                chat = chat,
                messages = listOf(transform.message)
            )
        )

        val loadedMessagesBefore = repo.getChatMessages(
            Command.ChatCommand.GetMessages(
                chat = chat,
                beforeOrderId = transform.message,
                afterOrderId = null,
                limit = DEFAULT_CHAT_PAGING_SIZE
            )
        ).messages

        val loadedMessagesAfter = repo.getChatMessages(
            Command.ChatCommand.GetMessages(
                chat = chat,
                beforeOrderId = null,
                afterOrderId = transform.message,
                limit = DEFAULT_CHAT_PAGING_SIZE
            )
        ).messages

        return buildList {
            addAll(loadedMessagesBefore)
            addAll(replyMessage)
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
                    beforeOrderId = null,
                    afterOrderId = last.order,
                    limit = DEFAULT_CHAT_PAGING_SIZE
                )
            )
            state + next.messages
        } else {
            state.also {
                logger.logWarning("The last message not found in chat")
            }
        }
    } catch (e: Exception) {
        state.also {
            logger.logException(e, "Error while loading previous page in chat $chat")
        }
    }

    private suspend fun loadThePreviousPage(
        state: List<Chat.Message>,
        chat: Id
    ): List<Chat.Message> = try {
        val first = state.firstOrNull()
        if (first != null) {
            val next = repo.getChatMessages(
                Command.ChatCommand.GetMessages(
                    chat = chat,
                    beforeOrderId = first.order,
                    afterOrderId = null,
                    limit = DEFAULT_CHAT_PAGING_SIZE
                )
            )
            next.messages + state
        } else {
            state.also {
                logger.logWarning("The first message not found in chat")
            }
        }
    } catch (e: Exception) {
        state.also {
            logger.logException(e, "Error while loading next page in chat: $chat")
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

    suspend fun onLoadNextPage() {
        commands.emit(Transformation.Commands.LoadBefore)
    }

    suspend fun onLoadPreviousPage() {
        commands.emit(Transformation.Commands.LoadAfter)
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
            data object LoadBefore : Commands()

            /**
             * Loading next — more recent — messages in history.
             * Loading the next page if it exists.
             */
            data object LoadAfter : Commands()

            /**
             * Loading message before and current given (reply) message.
             */
            data class LoadTo(val message: Id) : Commands()
        }
    }

    companion object {
        const val DEFAULT_CHAT_PAGING_SIZE = 100
    }
}