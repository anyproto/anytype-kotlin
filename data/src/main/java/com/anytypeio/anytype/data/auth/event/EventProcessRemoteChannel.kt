package com.anytypeio.anytype.data.auth.event

import com.anytypeio.anytype.core_models.ProcessEvent
import kotlinx.coroutines.flow.Flow

interface EventProcessRemoteChannel {
    fun observe(): Flow<List<ProcessEvent>>
}

class EventProcessDateChannel(
    private val channel: EventProcessRemoteChannel
) : EventProcessRemoteChannel {

    override fun observe(): Flow<List<ProcessEvent>> {
        return channel.observe()
    }
}