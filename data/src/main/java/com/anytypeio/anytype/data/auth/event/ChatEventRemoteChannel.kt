package com.anytypeio.anytype.data.auth.event

import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.chats.ChatEventChannel
import kotlinx.coroutines.flow.Flow

interface ChatEventRemoteChannel {
    fun observe(chat: Id): Flow<List<Event.Command.Chats>>
    fun subscribe(subscription: Id): Flow<List<Event.Command.Chats>>
    class Default(
        private val channel: ChatEventRemoteChannel
    ) : ChatEventChannel {
        override fun observe(chat: Id): Flow<List<Event.Command.Chats>> {
            return channel.observe(chat)
        }
    }
}