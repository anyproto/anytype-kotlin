package com.agileburo.anytype.data.auth.repo.block

import com.agileburo.anytype.data.auth.model.CommandEntity

class BlockRemoteDataStore(private val remote: BlockRemote) : BlockDataStore {

    override suspend fun getConfig() = remote.getConfig()

    override suspend fun openDashboard(contextId: String, id: String) {
        remote.openDashboard(id = id, contextId = contextId)
    }

    override suspend fun closeDashboard(id: String) {
        remote.closeDashboard(id = id)
    }

    override fun observeBlocks() = remote.observeBlocks()
    override fun observePages() = remote.observePages()
    override fun observeEvents() = remote.observeEvents()

    override suspend fun createPage(parentId: String): String = remote.createPage(parentId)
    override suspend fun openPage(id: String) {
        remote.openPage(id)
    }

    override suspend fun closePage(id: String) {
        remote.closePage(id)
    }

    override suspend fun updateText(command: CommandEntity.UpdateText) {
        remote.updateText(command)
    }

    override suspend fun updateCheckbox(command: CommandEntity.UpdateCheckbox) {
        remote.updateCheckbox(command)
    }

    override suspend fun create(command: CommandEntity.Create) {
        remote.create(command)
    }

    override suspend fun dnd(command: CommandEntity.Dnd) {
        remote.dnd(command)
    }

    override suspend fun duplicate(command: CommandEntity.Duplicate) = remote.duplicate(command)

    override suspend fun unlink(command: CommandEntity.Unlink) {
        remote.unlink(command)
    }
}