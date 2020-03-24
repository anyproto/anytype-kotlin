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

    override suspend fun updateTextStyle(command: CommandEntity.UpdateStyle) {
        remote.updateTextStyle(command)
    }

    override suspend fun updateTextColor(command: CommandEntity.UpdateTextColor) {
        remote.updateTextColor(command)
    }

    override suspend fun updateBackroundColor(command: CommandEntity.UpdateBackgroundColor) {
        remote.updateBackroundColor(command)
    }

    override suspend fun updateCheckbox(command: CommandEntity.UpdateCheckbox) {
        remote.updateCheckbox(command)
    }

    override suspend fun uploadUrl(command: CommandEntity.UploadBlock) {
        remote.uploadUrl(command)
    }

    override suspend fun create(command: CommandEntity.Create): String = remote.create(command)

    override suspend fun dnd(command: CommandEntity.Dnd) {
        remote.dnd(command)
    }

    override suspend fun duplicate(command: CommandEntity.Duplicate) = remote.duplicate(command)

    override suspend fun unlink(command: CommandEntity.Unlink) {
        remote.unlink(command)
    }

    override suspend fun merge(command: CommandEntity.Merge) {
        remote.merge(command)
    }

    override suspend fun split(command: CommandEntity.Split): String = remote.split(command)

    override suspend fun setIconName(
        command: CommandEntity.SetIconName
    ) = remote.setIconName(command)

    override suspend fun setupBookmark(
        command: CommandEntity.SetupBookmark
    ) = remote.setupBookmark(command)

    override suspend fun undo(command: CommandEntity.Undo) = remote.undo(command)

    override suspend fun redo(command: CommandEntity.Redo) = remote.redo(command)
}