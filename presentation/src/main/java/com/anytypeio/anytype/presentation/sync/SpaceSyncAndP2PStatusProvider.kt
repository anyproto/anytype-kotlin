package com.anytypeio.anytype.presentation.sync

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.multiplayer.P2PStatusUpdate
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncUpdate
import com.anytypeio.anytype.domain.multiplayer.ActiveSpaceMemberSubscriptionContainer
import com.anytypeio.anytype.domain.workspace.P2PStatusChannel
import com.anytypeio.anytype.domain.workspace.SpaceSyncStatusChannel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart

interface SpaceSyncAndP2PStatusProvider {

    suspend fun observe(): Flow<SpaceSyncAndP2PStatusState>

    class Impl @Inject constructor(
        private val activeSpace: ActiveSpaceMemberSubscriptionContainer,
        private val spaceSyncStatusChannel: SpaceSyncStatusChannel,
        private val p2PStatusChannel: P2PStatusChannel
    ) : SpaceSyncAndP2PStatusProvider {

        @OptIn(ExperimentalCoroutinesApi::class)
        override suspend fun observe(): Flow<SpaceSyncAndP2PStatusState> {
            return activeSpace
                .observe()
                .flatMapLatest { activeSpace ->
                    when (activeSpace) {
                        is ActiveSpaceMemberSubscriptionContainer.Store.Data -> {
                            observeSpace(spaceId = activeSpace.config.space)
                        }

                        ActiveSpaceMemberSubscriptionContainer.Store.Empty -> {
                            emptyFlow()
                        }
                    }
                }
        }

        private fun observeSpace(spaceId: Id): Flow<SpaceSyncAndP2PStatusState> {
            val syncFlow =
                spaceSyncStatusChannel.observe(spaceId).onStart { emit(SpaceSyncUpdate.Initial) }
            val p2pFlow =
                p2PStatusChannel.observe(spaceId).onStart { emit(P2PStatusUpdate.Initial) }

            return combine(syncFlow, p2pFlow) { syncStatus, p2PStatus ->
                if (syncStatus is SpaceSyncUpdate.Initial && p2PStatus is P2PStatusUpdate.Initial) {
                    SpaceSyncAndP2PStatusState.Initial
                } else {
                    SpaceSyncAndP2PStatusState.Success(
                        spaceSyncUpdate = syncStatus,
                        p2PStatusUpdate = p2PStatus
                    )
                }
            }
        }
    }
}

sealed class SpaceSyncAndP2PStatusState {
    data object Initial : SpaceSyncAndP2PStatusState()
    data class Error(val message: String) : SpaceSyncAndP2PStatusState()
    data class Success(
        val spaceSyncUpdate: SpaceSyncUpdate,
        val p2PStatusUpdate: P2PStatusUpdate
    ) : SpaceSyncAndP2PStatusState()
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
            SyncStatusWidgetState.Error(message = this.message)
        }

        SpaceSyncAndP2PStatusState.Initial -> {
            SyncStatusWidgetState.Hidden
        }

        is SpaceSyncAndP2PStatusState.Success -> {
            SyncStatusWidgetState.Success(
                spaceSyncUpdate = this.spaceSyncUpdate,
                p2PStatusUpdate = this.p2PStatusUpdate
            )
        }
    }
}

sealed class SyncStatusWidgetState {
    data object Hidden : SyncStatusWidgetState()
    data class Error(val message: String) : SyncStatusWidgetState()
    data class Success(
        val spaceSyncUpdate: SpaceSyncUpdate,
        val p2PStatusUpdate: P2PStatusUpdate
    ) : SyncStatusWidgetState()
}