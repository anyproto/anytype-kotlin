package com.agileburo.anytype.domain.block.repo

import com.agileburo.anytype.domain.block.model.Block
import kotlinx.coroutines.flow.Flow

interface BlockRepository {
    suspend fun openPage(id: String)
    suspend fun closePage(id: String)
    suspend fun openDashboard(contextId: String, id: String)
    suspend fun observeBlocks(): Flow<List<Block>>
    suspend fun observePages(): Flow<List<Block>>
}