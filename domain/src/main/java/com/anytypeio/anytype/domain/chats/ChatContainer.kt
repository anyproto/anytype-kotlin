package com.anytypeio.anytype.domain.chats

import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.chats.Chat
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.scan

class ChatContainer @Inject constructor(
    private val repo: BlockRepository,
    private val channel: ChatEventChannel
) {

    fun watch(chat: Id): Flow<List<Chat.Message>> = flow {
        val initial = repo.subscribeLastChatMessages(
            command = Command.ChatCommand.SubscribeLastMessages(
                chat = chat,
                limit = DEFAULT_LAST_MESSAGE_COUNT
            )
        ).messages

        emitAll(
            channel.observe(chat = chat).scan(initial) { state, events ->
                state.reduce(events)
            }.catch {
                emit(emptyList())
            }
        )

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
            }
        }
        return result
    }

    companion object {
        const val DEFAULT_LAST_MESSAGE_COUNT = 50
    }
}