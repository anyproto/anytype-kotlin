package com.anytypeio.anytype.middleware.interactor.events

import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.primitives.SpaceId
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

    override fun subscribe(subscription: Id): Flow<List<Event.Command.Chats>> {
        return eventProxy
            .flow()
            .mapNotNull { item ->
                item.messages.mapNotNull { msg ->
                    msg.payload(subscription = subscription, contextId = item.contextId)
                }
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
                spaceId = SpaceId(spaceId),
                context = contextId,
                order = event.orderId,
                id = event.id,
                message = requireNotNull(event.message?.core()),
                dependencies = event.dependencies
                    .map { ObjectWrapper.Basic(it.orEmpty()) }
                    .filter { it.isValid }
            )
        }
        chatStateUpdate != null -> {
            val event = chatStateUpdate
            checkNotNull(event)
            Event.Command.Chats.UpdateState(
                context = contextId,
                state = event.state?.core()
            )
        }
        chatUpdateMessageReadStatus != null -> {
            val event = chatUpdateMessageReadStatus
            checkNotNull(event)
            Event.Command.Chats.UpdateMessageReadStatus(
                context = contextId,
                messages = event.ids,
                isRead = event.isRead
            )
        }
        chatUpdateMentionReadStatus != null -> {
            val event = chatUpdateMentionReadStatus
            checkNotNull(event)
            Event.Command.Chats.UpdateMentionReadStatus(
                context = contextId,
                messages = event.ids,
                isRead = event.isRead
            )
        }
        chatUpdateMessageSyncStatus != null -> {
            val event = chatUpdateMessageSyncStatus
            checkNotNull(event)
            Event.Command.Chats.UpdateMessageSyncStatus(
                context = contextId,
                messages = event.ids,
                isSynced = event.isSynced,
                subscriptions = event.subIds
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
                message = event.id
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

fun MEventMessage.payload(subscription: Id, contextId: Id) : Event.Command.Chats? {
    return when {
        chatAdd != null -> {
            val event = chatAdd
            checkNotNull(event)
            if (event.subIds.contains(subscription)) {
                Event.Command.Chats.Add(
                    spaceId = SpaceId(spaceId),
                    context = contextId,
                    order = event.orderId,
                    id = event.id,
                    message = requireNotNull(event.message?.core()),
                    dependencies = event.dependencies
                        .map { ObjectWrapper.Basic(it.orEmpty()) }
                        .filter { it.isValid }
                )
            } else {
                null
            }
        }
        chatStateUpdate != null -> {
            val event = chatStateUpdate
            checkNotNull(event)
            if (event.subIds.contains(subscription)) {
                Event.Command.Chats.UpdateState(
                    context = contextId,
                    state = event.state?.core()
                )
            } else {
                null
            }
        }
        chatUpdateMessageReadStatus != null -> {
            val event = chatUpdateMessageReadStatus
            checkNotNull(event)
            if (event.subIds.contains(subscription)) {
                Event.Command.Chats.UpdateMessageReadStatus(
                    context = contextId,
                    messages = event.ids,
                    isRead = event.isRead
                )
            } else {
                null
            }
        }
        chatUpdateMentionReadStatus != null -> {
            val event = chatUpdateMentionReadStatus
            checkNotNull(event)
            if (event.subIds.contains(subscription)) {
                Event.Command.Chats.UpdateMentionReadStatus(
                    context = contextId,
                    messages = event.ids,
                    isRead = event.isRead
                )
            } else {
                null
            }
        }
        chatUpdateMessageSyncStatus != null -> {
            val event = chatUpdateMessageSyncStatus
            checkNotNull(event)
            Event.Command.Chats.UpdateMessageSyncStatus(
                context = contextId,
                messages = event.ids,
                isSynced = event.isSynced,
                subscriptions = event.subIds
            )
        }
        chatUpdate != null -> {
            val event = chatUpdate
            checkNotNull(event)
            if (event.subIds.contains(subscription)) {
                Event.Command.Chats.Update(
                    context = contextId,
                    id = event.id,
                    message = requireNotNull(event.message?.core())
                )
            } else {
                null
            }
        }
        chatDelete != null -> {
            val event = chatDelete
            checkNotNull(event)
            if (event.subIds.contains(subscription)) {
                Event.Command.Chats.Delete(
                    context = contextId,
                    message = event.id
                )
            } else {
                null
            }
        }
        chatUpdateReactions != null -> {
            val event = chatUpdateReactions
            checkNotNull(event)
            if (event.subIds.contains(subscription)) {
                Event.Command.Chats.UpdateReactions(
                    context = contextId,
                    id = event.id,
                    reactions = event.reactions?.reactions?.mapValues { (unicode, identities) ->
                        identities.ids
                    } ?: emptyMap()
                )
            } else {
                null
            }
        }
        else -> {
            null
        }
    }
}