package com.agileburo.anytype.domain.block.repo

import com.agileburo.anytype.domain.block.model.Block
import com.agileburo.anytype.domain.block.model.Command
import com.agileburo.anytype.domain.common.Id
import com.agileburo.anytype.domain.config.Config
import com.agileburo.anytype.domain.event.model.Event
import kotlinx.coroutines.flow.Flow

interface BlockRepository {
    suspend fun dnd(command: Command.Dnd)
    suspend fun create(command: Command.Create)
    suspend fun update(update: Command.Update)
    suspend fun getConfig(): Config
    suspend fun createPage(parentId: String): Id
    suspend fun openPage(id: String)
    suspend fun closePage(id: String)
    suspend fun openDashboard(contextId: String, id: String)
    suspend fun closeDashboard(id: String)

    @Deprecated("Will be removed and replaced by observeEvents()")
    fun observeBlocks(): Flow<List<Block>>

    fun observeEvents(): Flow<Event>
    fun observePages(): Flow<List<Block>>
}