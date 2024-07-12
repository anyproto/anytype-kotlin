package com.anytypeio.anytype.data.auth.status

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.multiplayer.P2PStatusUpdate
import com.anytypeio.anytype.domain.workspace.P2PStatusChannel
import kotlinx.coroutines.flow.Flow

interface P2PStatusRemoteChannel {
    fun observe(activeSpaceId: Id): Flow<List<P2PStatusUpdate>>
}

class P2PStatusDataChannel(
    private val channel: P2PStatusRemoteChannel
) : P2PStatusChannel {

    override fun observe(activeSpaceId: Id): Flow<List<P2PStatusUpdate>> {
        return channel.observe(activeSpaceId)
    }
}