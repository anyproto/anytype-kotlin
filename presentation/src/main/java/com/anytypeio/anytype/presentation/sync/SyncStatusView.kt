package com.anytypeio.anytype.presentation.sync

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.NetworkMode
import com.anytypeio.anytype.core_models.SyncStatus
import com.anytypeio.anytype.core_models.NetworkModeConst.NODE_STAGING_ID

sealed class SyncStatusView {
    object Unknown : SyncStatusView()
    object Offline : SyncStatusView()
    object Syncing : SyncStatusView()
    sealed class Synced : SyncStatusView() {
        object AnyNetwork : Synced()
        object StagingNetwork : Synced()
        object LocalOnly : Synced()
        object SelfHostedNetwork : Synced()
    }
    object Failed : SyncStatusView()
    object IncompatibleVersion : SyncStatusView()
}

fun SyncStatus.toView(networkId: Id?, networkMode: NetworkMode): SyncStatusView {
    return when (this) {
        SyncStatus.UNKNOWN -> {
            when (networkMode) {
                NetworkMode.LOCAL -> SyncStatusView.Synced.LocalOnly
                else -> SyncStatusView.Unknown
            }
        }
        SyncStatus.OFFLINE -> SyncStatusView.Offline
        SyncStatus.SYNCING -> SyncStatusView.Syncing
        SyncStatus.SYNCED -> {
            networkMode.syncedStatusToView(networkId)
        }
        SyncStatus.FAILED -> SyncStatusView.Failed
        SyncStatus.INCOMPATIBLE_VERSION -> SyncStatusView.IncompatibleVersion
    }
}

fun NetworkMode.syncedStatusToView(networkId: String?): SyncStatusView {
    when (this) {
        NetworkMode.DEFAULT -> return SyncStatusView.Synced.AnyNetwork
        NetworkMode.LOCAL -> {
            return if (networkId.isNullOrEmpty()) {
                SyncStatusView.Synced.LocalOnly
            } else {
                SyncStatusView.Unknown
            }
        }
        NetworkMode.CUSTOM -> {
            return if (networkId == NODE_STAGING_ID) {
                SyncStatusView.Synced.StagingNetwork
            } else {
                SyncStatusView.Synced.SelfHostedNetwork
            }
        }
    }
}
