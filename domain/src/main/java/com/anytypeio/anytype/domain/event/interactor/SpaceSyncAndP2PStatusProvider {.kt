package com.anytypeio.anytype.domain.event.interactor

import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncAndP2PStatusState
import kotlinx.coroutines.flow.Flow

interface SpaceSyncAndP2PStatusProvider {
    fun onStart()
    fun onStop()
    fun getState(): Flow<SpaceSyncAndP2PStatusState>
}