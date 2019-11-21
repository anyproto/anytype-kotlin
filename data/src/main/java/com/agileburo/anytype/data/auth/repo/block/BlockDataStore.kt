package com.agileburo.anytype.data.auth.repo.block

import com.agileburo.anytype.data.auth.model.BlockEntity
import kotlinx.coroutines.flow.Flow

interface BlockDataStore {
    suspend fun openDashboard(contextId: String, id: String)
    suspend fun observeBlocks(): Flow<List<BlockEntity>>
}