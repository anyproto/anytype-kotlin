package com.agileburo.anytype.data.auth.event

import com.agileburo.anytype.data.auth.mapper.toDomain
import com.agileburo.anytype.domain.event.interactor.EventChannel
import kotlinx.coroutines.flow.map

class EventDataChannel(private val remote: EventRemoteChannel) : EventChannel {
    override fun observeEvents() =
        remote.observeEvents().map { events -> events.map { it.toDomain() } }
}