package com.anytypeio.anytype.presentation.sync

import android.util.Log
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.multiplayer.P2PStatusUpdate
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncAndP2PStatusState
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncUpdate
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.debugging.Logger
import com.anytypeio.anytype.domain.event.interactor.SpaceSyncAndP2PStatusProvider
import com.anytypeio.anytype.domain.multiplayer.ActiveSpaceMemberSubscriptionContainer
import com.anytypeio.anytype.domain.workspace.P2PStatusChannel
import com.anytypeio.anytype.domain.workspace.SpaceSyncStatusChannel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import timber.log.Timber

class SpaceSyncAndP2PStatusProviderImpl @Inject constructor(
    private val activeSpace: ActiveSpaceMemberSubscriptionContainer,
    private val spaceSyncStatusChannel: SpaceSyncStatusChannel,
    private val p2PStatusChannel: P2PStatusChannel,
    private val dispatchers: AppCoroutineDispatchers,
    private val scope: CoroutineScope,
    private val logger: Logger
) : SpaceSyncAndP2PStatusProvider {

    private val members = MutableStateFlow<SpaceSyncAndP2PStatusState>(SpaceSyncAndP2PStatusState.Initial)
    private val jobs = mutableListOf<Job>()

    override fun getState(): Flow<SpaceSyncAndP2PStatusState> {
        return members
    }

    override fun onStart() {
        logger.logInfo("SpaceSyncAndP2PStatusProviderImpl start")
        clear()
        jobs += scope.launch(dispatchers.io) {
            observe().collect { state ->
                logger.logInfo("SpaceSyncAndP2PStatusProviderImpl state: $state")
                Log.d("Test1983", "Provider|Sync status: $state")
                members.value = state
            }
        }
    }

    override fun onStop() {
        logger.logInfo("SpaceSyncAndP2PStatusProviderImpl stop")
        clear()
    }

    private fun clear() {
        jobs.forEach { it.cancel() }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun observe(): Flow<SpaceSyncAndP2PStatusState> {
        val p2pFlow =
            p2PStatusChannel.observe().onStart { emit(P2PStatusUpdate.Initial) }

        val syncFlow = activeSpace
            .observe()
            .flatMapLatest { activeSpace ->
                when (activeSpace) {
                    is ActiveSpaceMemberSubscriptionContainer.Store.Data -> {
                        observeSpaceSyncStatus(spaceId = activeSpace.config.space)
                    }

                    ActiveSpaceMemberSubscriptionContainer.Store.Empty -> {
                        emptyFlow()
                    }
                }
            }
        return combine(syncFlow, p2pFlow) { syncStatus, p2PStatus ->
            Timber.d("SpaceSyncAndP2PStatusState: $syncStatus, $p2PStatus")
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

    private fun observeSpaceSyncStatus(spaceId: Id): Flow<SpaceSyncUpdate> {
        return spaceSyncStatusChannel.observe(spaceId).onStart { emit(SpaceSyncUpdate.Initial) }
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
        val spaceSyncUpdate: SpaceSyncUpdate,
        val p2PStatusUpdate: P2PStatusUpdate
    ) : SyncStatusWidgetState()
}