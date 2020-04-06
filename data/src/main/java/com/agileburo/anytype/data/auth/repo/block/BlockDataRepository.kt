package com.agileburo.anytype.data.auth.repo.block

import com.agileburo.anytype.data.auth.mapper.toDomain
import com.agileburo.anytype.data.auth.mapper.toEntity
import com.agileburo.anytype.domain.block.model.Command
import com.agileburo.anytype.domain.block.repo.BlockRepository
import com.agileburo.anytype.domain.common.Id

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

    override suspend fun updateText(command: Command.UpdateText) {
        factory.remote.updateText(command.toEntity())
    }

    override suspend fun updateTextStyle(command: Command.UpdateStyle) {
        factory.remote.updateTextStyle(command.toEntity())
    }

    override suspend fun updateTextColor(command: Command.UpdateTextColor) {
        factory.remote.updateTextColor(command.toEntity())
    }

    override suspend fun updateBackgroundColor(command: Command.UpdateBackgroundColor) {
        factory.remote.updateBackroundColor(command.toEntity())
    }

    override suspend fun updateCheckbox(command: Command.UpdateCheckbox) {
        factory.remote.updateCheckbox(command.toEntity())
    }

    override suspend fun create(command: Command.Create) = factory.remote.create(command.toEntity())

    override suspend fun createDocument(
        command: Command.CreateDocument
    ) = factory.remote.createDocument(command.toEntity())

    override suspend fun dnd(command: Command.Dnd) {
        factory.remote.dnd(command.toEntity())
    }

    override suspend fun duplicate(command: Command.Duplicate) =
        factory.remote.duplicate(command.toEntity())

    override suspend fun unlink(command: Command.Unlink) {
        factory.remote.unlink(command.toEntity())
    }

    override suspend fun merge(command: Command.Merge) {
        factory.remote.merge(command.toEntity())
    }

    override suspend fun split(command: Command.Split) = factory.remote.split(command.toEntity())

    override suspend fun setIconName(
        command: Command.SetIconName
    ) = factory.remote.setIconName(command.toEntity())

    override suspend fun setupBookmark(
        command: Command.SetupBookmark
    ) = factory.remote.setupBookmark(command.toEntity())

    override suspend fun uploadUrl(command: Command.UploadVideoBlockUrl) {
        factory.remote.uploadUrl(command.toEntity())
    }

    override suspend fun undo(command: Command.Undo) = factory.remote.undo(command.toEntity())

    override suspend fun redo(command: Command.Redo) = factory.remote.redo(command.toEntity())

    override suspend fun archiveDocument(
        command: Command.ArchiveDocument
    ) = factory.remote.archiveDocument(command.toEntity())

    override suspend fun replace(
        command: Command.Replace
    ): Id = factory.remote.replace(command.toEntity())
}