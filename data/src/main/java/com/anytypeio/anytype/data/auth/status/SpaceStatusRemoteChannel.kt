package com.anytypeio.anytype.data.auth.status

import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncUpdate
import kotlinx.coroutines.flow.Flow

interface SpaceStatusRemoteChannel {
    fun observe(activeSpaceId: String): Flow<List<SpaceSyncUpdate>>
}