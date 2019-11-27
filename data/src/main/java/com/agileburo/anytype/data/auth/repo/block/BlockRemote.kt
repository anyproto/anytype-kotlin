package com.agileburo.anytype.data.auth.repo.block

import com.agileburo.anytype.data.auth.model.BlockEntity
import kotlinx.coroutines.flow.Flow

interface BlockRemote {
    suspend fun openPage(id: String)
    suspend fun observeBlocks(): Flow<List<BlockEntity>>
    suspend fun observePages(): Flow<List<BlockEntity>>
    suspend fun openDashboard(contextId: String, id: String)
}