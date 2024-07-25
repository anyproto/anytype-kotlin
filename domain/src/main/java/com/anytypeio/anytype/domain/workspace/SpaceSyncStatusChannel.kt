package com.anytypeio.anytype.domain.workspace

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncUpdate
import kotlinx.coroutines.flow.Flow

interface SpaceSyncStatusChannel {
    fun observe(activeSpaceId: Id): Flow<SpaceSyncUpdate>
}