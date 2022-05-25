package com.anytypeio.anytype.domain.status

import com.anytypeio.anytype.core_models.SyncStatus
import kotlinx.coroutines.flow.Flow

interface ThreadStatusChannel {
    fun observe(ctx: String): Flow<SyncStatus>
}