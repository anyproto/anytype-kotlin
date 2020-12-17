package com.anytypeio.anytype.domain.status

import kotlinx.coroutines.flow.Flow

interface ThreadStatusChannel {
    fun observe(ctx: String): Flow<SyncStatus>
}