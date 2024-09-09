package com.anytypeio.anytype.middleware.interactor.events

import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.data.auth.event.ChatEventRemoteChannel
import com.anytypeio.anytype.middleware.EventProxy
import com.anytypeio.anytype.middleware.mappers.core
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull

class ChatEventMiddlewareChannel(
    private val eventProxy: EventProxy
): ChatEventRemoteChannel {

    override fun observe(chat: Id): Flow<List<Event.Command.Chats>> {
        return eventProxy.flow().mapNotNull { item ->
            item.messages.mapNotNull { msg ->
                when {
                    msg.chatAdd != null -> {
                        val event = msg.chatAdd
                        checkNotNull(event)
                        Event.Command.Chats.Add(
                            context = item.contextId,
                            order = event.orderId,
                            id = event.id,
                            message = requireNotNull(event.message?.core())
                        )
                    }
                    msg.chatUpdate != null -> {
                        val event = msg.chatUpdate
                        checkNotNull(event)
                        Event.Command.Chats.Update(
                            context = item.contextId,
                            id = event.id,
                            message = requireNotNull(event.message?.core())
                        )
                    }
                    msg.chatDelete != null -> {
                        val event = msg.chatDelete
                        checkNotNull(event)
                        Event.Command.Chats.Delete(
                            context = item.contextId,
                            id = event.id
                        )
                    }
                    msg.chatUpdateReactions != null -> {
                        val event = msg.chatUpdateReactions
                        checkNotNull(event)
                        Event.Command.Chats.UpdateReactions(
                            context = item.contextId,
                            id = event.id,
                            reactions = event.reactions?.reactions?.mapValues { (unicode, identities) ->
                                identities.ids
                            } ?: emptyMap()
                        )
                    }
                    else -> {
                        null
                    }
                }
            }
        }
    }
}