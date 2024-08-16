package com.anytypeio.anytype.presentation.sync

import com.anytypeio.anytype.core_models.multiplayer.P2PStatusUpdate
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncAndP2PStatusState
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncUpdate
import com.anytypeio.anytype.domain.event.interactor.SpaceSyncAndP2PStatusProvider
import com.anytypeio.anytype.domain.multiplayer.ActiveSpaceMemberSubscriptionContainer
import com.anytypeio.anytype.domain.workspace.SyncAndP2PStatusChannel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import timber.log.Timber

class SpaceSyncAndP2PStatusProviderImpl @Inject constructor(
    private val activeSpace: ActiveSpaceMemberSubscriptionContainer,
    private val spaceSyncStatusChannel: SyncAndP2PStatusChannel,
) : SpaceSyncAndP2PStatusProvider {

    override fun observe(): Flow<SpaceSyncAndP2PStatusState> =
        combine(
            activeSpace.observe().catch { e ->
                Timber.e(e, "Error observing active space")
                emit(ActiveSpaceMemberSubscriptionContainer.Store.Empty)
            },
            spaceSyncStatusChannel.p2pStatus(),
            spaceSyncStatusChannel.syncStatus()
        ) { activeSpace, p2pStatus, syncStatus ->
            when (activeSpace) {
                is ActiveSpaceMemberSubscriptionContainer.Store.Data -> {
                    val activeSpaceId = activeSpace.config.space
                    val p2PStatusUpdate = p2pStatus[activeSpaceId]
                    val spaceSyncUpdate = syncStatus[activeSpaceId]

                    if (p2PStatusUpdate == null && spaceSyncUpdate == null) {
                        SpaceSyncAndP2PStatusState.Initial
                    } else {
                        SpaceSyncAndP2PStatusState.Success(
                            spaceSyncUpdate = spaceSyncUpdate ?: SpaceSyncUpdate.Initial,
                            p2PStatusUpdate = p2PStatusUpdate ?: P2PStatusUpdate.Initial
                        )
                    }
                }
                ActiveSpaceMemberSubscriptionContainer.Store.Empty -> {
                    SpaceSyncAndP2PStatusState.Initial
                }
            }
        }.catch { e ->
            Timber.e(e, "Error observing sync and P2P status")
            emit(SpaceSyncAndP2PStatusState.Error("Error observing sync and P2P status, ${e.message}"))
        }
    }

fun SyncStatusWidgetState.updateStatus(newState: SpaceSyncAndP2PStatusState): SyncStatusWidgetState {
    return when (this) {
        is SyncStatusWidgetState.Error -> {
            newState.toSyncStatusWidgetState()
        }

        SyncStatusWidgetState.Hidden -> SyncStatusWidgetState.Hidden
        is SyncStatusWidgetState.Success -> {
            newState.toSyncStatusWidgetState()
        }
    }
}

fun SpaceSyncAndP2PStatusState.toSyncStatusWidgetState(): SyncStatusWidgetState {
    return when (this) {
        is SpaceSyncAndP2PStatusState.Error -> {
            SyncStatusWidgetState.Error(message = message)
        }

        SpaceSyncAndP2PStatusState.Initial -> {
            SyncStatusWidgetState.Hidden
        }

        is SpaceSyncAndP2PStatusState.Success -> {
            SyncStatusWidgetState.Success(
                spaceSyncUpdate = spaceSyncUpdate,
                p2PStatusUpdate = p2PStatusUpdate
            )
        }
    }
}

sealed class SyncStatusWidgetState {
    data object Hidden : SyncStatusWidgetState()
    data class Error(val message: String) : SyncStatusWidgetState()
    data class Success(
        val spaceSyncUpdate: SpaceSyncUpdate = SpaceSyncUpdate.Initial,
        val p2PStatusUpdate: P2PStatusUpdate = P2PStatusUpdate.Initial
    ) : SyncStatusWidgetState()
}