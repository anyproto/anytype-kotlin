package com.anytypeio.anytype.middleware.interactor.events

import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.data.auth.event.ChatEventRemoteChannel
import com.anytypeio.anytype.middleware.EventProxy
import com.anytypeio.anytype.middleware.mappers.MEventMessage
import com.anytypeio.anytype.middleware.mappers.core
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.mapNotNull

class ChatEventMiddlewareChannel(
    private val eventProxy: EventProxy
): ChatEventRemoteChannel {

    override fun observe(chat: Id): Flow<List<Event.Command.Chats>> {
        return eventProxy
            .flow()
            .filter { it.contextId == chat }
            .mapNotNull { item ->
                item.messages.mapNotNull { msg -> msg.payload(contextId = item.contextId) }
            }.filter { events ->
                events.isNotEmpty()
            }
    }
}

fun MEventMessage.payload(contextId: Id) : Event.Command.Chats? {
    return when {
        chatAdd != null -> {
            val event = chatAdd
            checkNotNull(event)
            Event.Command.Chats.Add(
                context = contextId,
                order = event.orderId,
                id = event.id,
                message = requireNotNull(event.message?.core())
            )
        }

        chatUpdate != null -> {
            val event = chatUpdate
            checkNotNull(event)
            Event.Command.Chats.Update(
                context = contextId,
                id = event.id,
                message = requireNotNull(event.message?.core())
            )
        }

        chatDelete != null -> {
            val event = chatDelete
            checkNotNull(event)
            Event.Command.Chats.Delete(
                context = contextId,
                id = event.id
            )
        }

        chatUpdateReactions != null -> {
            val event = chatUpdateReactions
            checkNotNull(event)
            Event.Command.Chats.UpdateReactions(
                context = contextId,
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