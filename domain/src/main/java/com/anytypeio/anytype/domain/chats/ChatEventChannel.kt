package com.anytypeio.anytype.domain.chats

import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.Id
import kotlinx.coroutines.flow.Flow

interface ChatEventChannel {
    fun observe(chat: Id): Flow<List<Event.Command.Chats>>
    fun subscribe(subscribe: Id): Flow<List<Event.Command.Chats>>
}