package com.anytypeio.anytype.middleware.interactor

import com.anytypeio.anytype.core_models.SyncStatus
import com.anytypeio.anytype.data.auth.status.ThreadStatusRemoteChannel
import com.anytypeio.anytype.middleware.EventProxy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.mapNotNull
import anytype.Event.Status.Thread.SyncStatus as MSyncStatus

class ThreadStatusMiddlewareChannel(
    private val events: EventProxy,
) : ThreadStatusRemoteChannel {

    override fun observe(ctx: String): Flow<SyncStatus> = events.flow()
        .filter { it.contextId == ctx }
        .mapNotNull { emission ->
            emission
                .messages
                .lastOrNull { it.threadStatus != null }
                ?.threadStatus
                ?.summary
                ?.status
        }
        .mapLatest { status ->
            when (status) {
                MSyncStatus.Unknown -> SyncStatus.UNKNOWN
                MSyncStatus.Offline -> SyncStatus.OFFLINE
                MSyncStatus.Syncing -> SyncStatus.SYNCING
                MSyncStatus.Synced -> SyncStatus.SYNCED
                MSyncStatus.Failed -> SyncStatus.FAILED
                MSyncStatus.IncompatibleVersion -> SyncStatus.INCOMPATIBLE_VERSION
            }
        }
}