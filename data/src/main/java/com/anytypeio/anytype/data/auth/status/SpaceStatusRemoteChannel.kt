package com.anytypeio.anytype.data.auth.status

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncUpdate
import com.anytypeio.anytype.domain.workspace.SpaceSyncStatusChannel
import kotlinx.coroutines.flow.Flow

interface SpaceStatusRemoteChannel {
    fun observe(activeSpaceId: String): Flow<List<SpaceSyncUpdate>>
}

class SpaceStatusDataChannel(
    private val channel: SpaceStatusRemoteChannel
) : SpaceSyncStatusChannel {

    override fun observe(activeSpaceId: Id): Flow<List<SpaceSyncUpdate>> {
        return channel.observe(activeSpaceId)
    }
}