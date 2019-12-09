package com.agileburo.anytype.data.auth.repo.block

import com.agileburo.anytype.data.auth.model.BlockEntity
import com.agileburo.anytype.data.auth.model.ConfigEntity
import kotlinx.coroutines.flow.Flow

interface BlockRemote {
    suspend fun getConfig(): ConfigEntity
    suspend fun createPage(parentId: String): String
    suspend fun openPage(id: String)
    suspend fun closePage(id: String)
    suspend fun observeBlocks(): Flow<List<BlockEntity>>
    suspend fun observePages(): Flow<List<BlockEntity>>
    suspend fun openDashboard(contextId: String, id: String)
    suspend fun closeDashboard(id: String)
}