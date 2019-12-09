package com.agileburo.anytype.domain.block.repo

import com.agileburo.anytype.domain.block.model.Block
import com.agileburo.anytype.domain.common.Id
import com.agileburo.anytype.domain.config.Config
import kotlinx.coroutines.flow.Flow

interface BlockRepository {
    suspend fun getConfig(): Config
    suspend fun createPage(parentId: String): Id
    suspend fun openPage(id: String)
    suspend fun closePage(id: String)
    suspend fun openDashboard(contextId: String, id: String)
    suspend fun closeDashboard(id: String)
    suspend fun observeBlocks(): Flow<List<Block>>
    suspend fun observePages(): Flow<List<Block>>
}