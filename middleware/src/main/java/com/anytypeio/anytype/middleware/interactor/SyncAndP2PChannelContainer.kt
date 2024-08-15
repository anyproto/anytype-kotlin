package com.anytypeio.anytype.middleware.interactor

import com.anytypeio.anytype.core_models.multiplayer.P2PStatusUpdate
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncUpdate
import com.anytypeio.anytype.middleware.mappers.MP2PStatusUpdate
import com.anytypeio.anytype.middleware.mappers.MSyncStatusUpdate
import com.anytypeio.anytype.middleware.mappers.toCoreModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber


interface SyncAndP2PStatusEventsStore {
    fun start()
    fun stop()
}

class SyncAndP2PStatusEventsStoreImpl(
    private val scope: CoroutineScope,
    private val channel: AppEventChannel
) : SyncAndP2PStatusEventsStore {

    private val _p2pStatus = MutableStateFlow<MutableMap<String, P2PStatusUpdate>>(mutableMapOf())
    private val _syncStatus = MutableStateFlow<MutableMap<String, SpaceSyncUpdate>>(mutableMapOf())

    val p2pStatus: Flow<Map<String, P2PStatusUpdate>> get() = _p2pStatus
    val syncStatus: Flow<Map<String, SpaceSyncUpdate>> get() = _syncStatus

    private val jobs = mutableListOf<Job>()

    override fun start() {
        Timber.i("SyncAndP2PStatusEventsStoreImpl start")
        jobs.forEach { it.cancel() }
        jobs.clear()
        jobs += scope.launch(Dispatchers.IO) {
            try {
                channel.channel()
                    .collect { emission ->
                        emission.messages.forEach { message ->
                            message.p2pStatusUpdate?.let { processP2PStatusUpdate(it) }
                        }
                    }
            } catch (e: Exception) {
                Timber.e(e, "Error collecting P2P status updates")
            }
        }
        jobs += scope.launch(Dispatchers.IO) {
            try {
                channel.channel()
                    .collect { emission ->
                        emission.messages.forEach { message ->
                            message.spaceSyncStatusUpdate?.let { processSpaceSyncUpdate(it) }
                        }
                    }
            } catch (e: Exception) {
                Timber.e(e, "Error collecting Sync status updates")
            }
        }
    }

    override fun stop() {
        Timber.i("SyncAndP2PStatusEventsStoreImpl stop")
        jobs.forEach { it.cancel() }
        jobs.clear()
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
        Timber.d("Processed P2P status update: $update")
    }

    private fun processSpaceSyncUpdate(update: MSyncStatusUpdate) {
        val syncUpdate = SpaceSyncUpdate(
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
        Timber.d("Processed Sync status update: $update")
    }
}