package com.anytypeio.anytype.presentation.sync

import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncError
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncStatus
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncUpdate

@Deprecated("to delete")
sealed class SyncStatusView {
    data object Init : SyncStatusView()
    data object Offline : SyncStatusView()
    data object Syncing : SyncStatusView()
    data object Synced : SyncStatusView()
    data object Failed : SyncStatusView()
    data object ConnectToPeers : SyncStatusView()
}

@Deprecated("to delete")
fun SpaceSyncUpdate.Update.toView(): SyncStatusView {
    val error = this.error
    if (error != SpaceSyncError.NULL) {
        return SyncStatusView.Failed
    } else {
        val status = this.status
        return when (status) {
            SpaceSyncStatus.SYNCED -> SyncStatusView.Synced
            SpaceSyncStatus.SYNCING -> SyncStatusView.Syncing
            SpaceSyncStatus.ERROR -> SyncStatusView.Failed
            SpaceSyncStatus.OFFLINE -> SyncStatusView.Offline
        }
    }
}
