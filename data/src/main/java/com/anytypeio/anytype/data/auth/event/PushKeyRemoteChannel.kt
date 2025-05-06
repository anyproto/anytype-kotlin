package com.anytypeio.anytype.data.auth.event

import com.anytypeio.anytype.core_models.chats.PushKeyUpdate
import com.anytypeio.anytype.domain.chats.PushKeyChannel
import kotlinx.coroutines.flow.Flow

interface PushKeyRemoteChannel {
    fun start()
    fun stop()
    fun observe(): Flow<PushKeyUpdate>
}

class PushKeyDateChannel(
    private val channel: PushKeyRemoteChannel
) : PushKeyChannel {

    override fun observe(): Flow<PushKeyUpdate> {
        return channel.observe()
    }
}