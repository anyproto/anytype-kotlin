package com.anytypeio.anytype.middleware.interactor

import anytype.Event.Status.Thread.SyncStatus
import com.anytypeio.anytype.data.auth.model.SyncStatusEntity
import com.anytypeio.anytype.data.auth.status.ThreadStatusRemoteChannel
import com.anytypeio.anytype.middleware.EventProxy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.mapNotNull

class ThreadStatusMiddlewareChannel(
    private val events: EventProxy,
) : ThreadStatusRemoteChannel {

    override fun observe(ctx: String): Flow<SyncStatusEntity> = events.flow()
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
                SyncStatus.Unknown -> SyncStatusEntity.UNKNOWN
                SyncStatus.Offline -> SyncStatusEntity.OFFLINE
                SyncStatus.Syncing -> SyncStatusEntity.SYNCING
                SyncStatus.Synced -> SyncStatusEntity.SYNCED
                SyncStatus.Failed -> SyncStatusEntity.FAILED
            }
        }
}