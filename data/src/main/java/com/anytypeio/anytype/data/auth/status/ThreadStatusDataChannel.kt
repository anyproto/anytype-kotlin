package com.anytypeio.anytype.data.auth.status

import com.anytypeio.anytype.core_models.SyncStatus
import com.anytypeio.anytype.domain.status.ThreadStatusChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ThreadStatusDataChannel(
    private val remote: ThreadStatusRemoteChannel,
) : ThreadStatusChannel {

    override fun observe(ctx: String): Flow<SyncStatus> {
        return remote.observe(ctx)
    }
}