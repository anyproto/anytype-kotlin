package com.anytypeio.anytype.domain.workspace

import com.anytypeio.anytype.core_models.multiplayer.P2PStatusUpdate
import kotlinx.coroutines.flow.Flow

interface P2PStatusChannel {
    fun observe(): Flow<P2PStatusUpdate>
}