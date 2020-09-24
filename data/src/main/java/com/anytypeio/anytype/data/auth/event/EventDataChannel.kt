package com.anytypeio.anytype.data.auth.event

import com.anytypeio.anytype.data.auth.mapper.toDomain
import com.anytypeio.anytype.domain.common.Id
import com.anytypeio.anytype.domain.event.interactor.EventChannel
import kotlinx.coroutines.flow.map

class EventDataChannel(private val remote: EventRemoteChannel) : EventChannel {

    override fun observeEvents(
        context: Id?
    ) = remote.observeEvents(context).map { events -> events.map { it.toDomain() } }
}