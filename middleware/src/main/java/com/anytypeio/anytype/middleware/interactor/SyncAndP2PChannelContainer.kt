package com.anytypeio.anytype.middleware.interactor

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.multiplayer.P2PStatusUpdate
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncUpdate
import com.anytypeio.anytype.core_utils.ext.cancel
import com.anytypeio.anytype.data.auth.status.SyncAndP2PStatusEventsStore
import com.anytypeio.anytype.middleware.mappers.MP2PStatusUpdate
import com.anytypeio.anytype.middleware.mappers.MSyncStatusUpdate
import com.anytypeio.anytype.middleware.mappers.toCoreModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

class SyncAndP2PStatusEventsStoreImpl(
    private val scope: CoroutineScope,
    private val channel: EventHandlerChannel,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : SyncAndP2PStatusEventsStore {

    private val _p2pStatus = MutableStateFlow<MutableMap<Id, P2PStatusUpdate>>(mutableMapOf())
    private val _syncStatus = MutableStateFlow<MutableMap<Id, SpaceSyncUpdate>>(mutableMapOf())

    override val p2pStatus: Flow<Map<Id, P2PStatusUpdate>> get() = _p2pStatus
    override val syncStatus: Flow<Map<Id, SpaceSyncUpdate>> get() = _syncStatus

    private val jobs = mutableListOf<Job>()

    override fun start() {
        Timber.i("SyncAndP2PStatusEventsStoreImpl start")
        jobs.cancel()
        jobs += scope.launch(dispatcher) {
            channel.flow()
                .catch { e ->
                    Timber.e(e, "Error collecting P2P & Sync status updates")
                }
                .collect { emission ->
                    emission.messages.forEach { message ->
                        message.p2pStatusUpdate?.let { processP2PStatusUpdate(it) }
                        message.spaceSyncStatusUpdate?.let { processSpaceSyncUpdate(it) }
                    }
                }
        }
    }

    override fun stop() {
        Timber.i("SyncAndP2PStatusEventsStoreImpl stop")
        jobs.cancel()
    }

    private fun processP2PStatusUpdate(update: MP2PStatusUpdate) {
        val p2pUpdate = P2PStatusUpdate.Update(
            spaceId = update.spaceId,
            status = update.status.toCoreModel(),
            devicesCounter = update.devicesCounter
        )
        _p2pStatus.update { currentMap ->
            currentMap[update.spaceId] = p2pUpdate
            currentMap
        }
    }

    private fun processSpaceSyncUpdate(update: MSyncStatusUpdate) {
        val syncUpdate = SpaceSyncUpdate.Update(
            id = update.id,
            status = update.status.toCoreModel(),
            network = update.network.toCoreModel(),
            error = update.error.toCoreModel(),
            syncingObjectsCounter = update.syncingObjectsCounter
        )

        _syncStatus.update { currentMap ->
            currentMap[update.id] = syncUpdate
            currentMap
        }
    }
}