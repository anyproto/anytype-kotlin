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
                limit = DEFAULT_LAST_MESSAGE_COUNT
            )
        )
        emitAll(
            merge(
                channel.observe(chat = chat),
                payloads
            ).scan(initial.messages) { state, events ->
                state.reduce(events)
            }
        )
    }.catch {
        logger.logException(it)
        emit(emptyList())
    }

    suspend fun onPayload(events: List<Event.Command.Chats>) {
        payloads.emit(events)
    }

    fun List<Chat.Message>.reduce(events: List<Event.Command.Chats>): List<Chat.Message> {
        // Naive implementation
        var result = this
        events.forEach { event ->
            when(event) {
                is Event.Command.Chats.Add -> {
                    if (result.isNotEmpty()) {
                        val last = result.last()
                        result = if (last.order < event.order)
                            result + listOf(event.message)
                        else {
                            buildList {
                                addAll(result)
                                add(event.message)
                            }.sortedBy { it.order }
                        }
                    } else {
                        result = listOf(event.message)
                    }
                }
                is Event.Command.Chats.Delete -> {
                    result = result.filter { msg ->
                        msg.id != event.id
                    }
                }
                is Event.Command.Chats.Update -> {
                    result = result.map { msg ->
                        if (msg.id == event.id)
                            event.message
                        else
                            msg
                    }
                }
                is Event.Command.Chats.UpdateReactions -> {
                    result = result.map { msg ->
                        if (msg.id == event.id)
                            msg.copy(
                                reactions = event.reactions
                            )
                        else
                            msg
                    }
                }

                is Event.Command.Chats.UpdateMentionReadStatus -> {
                    // TODO handle event
                }
                is Event.Command.Chats.UpdateMessageReadStatus -> {
                    // TODO handle event
                }
                is Event.Command.Chats.UpdateState -> {
                    // TODO handle event
                }
            }
        }
        return result
    }

    companion object {
        const val DEFAULT_LAST_MESSAGE_COUNT = 0
    }
}