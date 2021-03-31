package com.anytypeio.anytype.data.auth.event

import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.event.interactor.EventChannel
import kotlinx.coroutines.flow.Flow

class EventDataChannel(private val remote: EventRemoteChannel) : EventChannel {

    override fun observeEvents(context: Id?): Flow<List<Event>> = remote.observeEvents(context)

}