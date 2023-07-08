package com.anytypeio.anytype.data.auth.status

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.SyncStatus
import kotlinx.coroutines.flow.Flow

interface ThreadStatusRemoteChannel {
    fun observe(ctx: Id): Flow<SyncStatus>
}