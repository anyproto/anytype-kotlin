package com.anytypeio.anytype.data.auth.status

import com.anytypeio.anytype.core_models.multiplayer.P2PStatusUpdate
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncUpdate
import kotlinx.coroutines.flow.Flow

interface SyncAndP2PStatusEventsStore {
    val p2pStatus: Flow<Map<String, P2PStatusUpdate>>
    val syncStatus: Flow<Map<String, SpaceSyncUpdate>>

    fun start()
    fun stop()
}