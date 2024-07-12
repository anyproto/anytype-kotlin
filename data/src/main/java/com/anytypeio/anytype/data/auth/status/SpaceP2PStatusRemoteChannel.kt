package com.anytypeio.anytype.data.auth.status

import com.anytypeio.anytype.core_models.multiplayer.P2PStatusUpdate
import kotlinx.coroutines.flow.Flow

interface SpaceP2PStatusRemoteChannel {
    fun observe(activeSpaceId: String): Flow<List<P2PStatusUpdate>>
}