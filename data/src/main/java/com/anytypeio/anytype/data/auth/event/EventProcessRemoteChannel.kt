package com.anytypeio.anytype.data.auth.event

import com.anytypeio.anytype.core_models.Process
import com.anytypeio.anytype.domain.workspace.EventProcessChannel
import kotlinx.coroutines.flow.Flow

interface EventProcessRemoteChannel {
    fun observe(): Flow<List<Process.Event>>
}

class EventProcessDateChannel(
    private val channel: EventProcessRemoteChannel
) : EventProcessChannel {

    override fun observe(): Flow<List<Process.Event>> {
        return channel.observe()
    }
}