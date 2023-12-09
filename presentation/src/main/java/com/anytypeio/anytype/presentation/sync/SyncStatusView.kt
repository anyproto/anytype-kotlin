package com.anytypeio.anytype.presentation.sync

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.SyncStatus
import com.anytypeio.anytype.presentation.BuildConfig

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
fun SyncStatus.toView(networkId: Id?): SyncStatusView {
    val syncedStatus = when (networkId) {
        BuildConfig.NODE_NETWORK_ID -> SyncStatusView.Synced.AnyNetwork
        BuildConfig.STAGING_NETWORK_ID -> SyncStatusView.Synced.StagingNetwork
        "" -> SyncStatusView.Synced.LocalOnly
        else -> SyncStatusView.Synced.SelfHostedNetwork
    }
    return when (this) {
        SyncStatus.UNKNOWN -> SyncStatusView.Unknown
        SyncStatus.OFFLINE -> SyncStatusView.Offline
        SyncStatus.SYNCING -> SyncStatusView.Syncing
        SyncStatus.SYNCED -> syncedStatus
        SyncStatus.FAILED -> SyncStatusView.Failed
        SyncStatus.INCOMPATIBLE_VERSION -> SyncStatusView.IncompatibleVersion
    }
}
