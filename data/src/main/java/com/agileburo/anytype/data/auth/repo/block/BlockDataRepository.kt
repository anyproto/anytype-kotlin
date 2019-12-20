package com.agileburo.anytype.data.auth.repo.block

import com.agileburo.anytype.data.auth.mapper.toDomain
import com.agileburo.anytype.data.auth.mapper.toEntity
import com.agileburo.anytype.domain.block.model.Command
import com.agileburo.anytype.domain.block.repo.BlockRepository
import kotlinx.coroutines.flow.map

class BlockDataRepository(
    private val factory: BlockDataStoreFactory
) : BlockRepository {

    override suspend fun getConfig() = factory.remote.getConfig().toDomain()

    override suspend fun openDashboard(contextId: String, id: String) {
        factory.remote.openDashboard(id = id, contextId = contextId)
    }

    override suspend fun closeDashboard(id: String) {
        factory.remote.closeDashboard(id)
    }

    override suspend fun createPage(parentId: String) = factory.remote.createPage(parentId)

    override suspend fun openPage(id: String) {
        factory.remote.openPage(id)
    }

    override suspend fun closePage(id: String) {
        factory.remote.closePage(id)
    }

    override fun observeBlocks() =
        factory.remote.observeBlocks().map { blocks -> blocks.map { it.toDomain() } }

    override fun observeEvents() = factory.remote.observeEvents().map { it.toDomain() }
    override fun observePages() =
        factory.remote.observePages().map { blocks -> blocks.map { it.toDomain() } }

    override suspend fun update(update: Command.Update) {
        factory.remote.update(update.toEntity())
    }

    override suspend fun create(command: Command.Create) {
        factory.remote.create(command.toEntity())
    }

    override suspend fun dnd(command: Command.Dnd) {
        factory.remote.dnd(command.toEntity())
    }
}