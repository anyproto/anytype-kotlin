package com.agileburo.anytype.middleware.block

import com.agileburo.anytype.data.auth.model.CommandEntity
import com.agileburo.anytype.data.auth.model.ConfigEntity
import com.agileburo.anytype.data.auth.repo.block.BlockRemote
import com.agileburo.anytype.middleware.interactor.Middleware
import com.agileburo.anytype.middleware.toMiddleware

class BlockMiddleware(
    private val middleware: Middleware
) : BlockRemote {

    override suspend fun getConfig(): ConfigEntity = middleware.config

    override suspend fun openDashboard(contextId: String, id: String) {
        middleware.openDashboard(contextId, id)
    }

    override suspend fun closeDashboard(id: String) {
        middleware.closeDashboard(id)
    }

    override suspend fun createPage(parentId: String): String = middleware.createPage(parentId)

    override suspend fun openPage(id: String) {
        middleware.openBlock(id)
    }

    override suspend fun closePage(id: String) {
        middleware.closePage(id)
    }

    override suspend fun updateText(command: CommandEntity.UpdateText) {
        middleware.updateText(
            command.contextId,
            command.blockId,
            command.text,
            command.marks.map { it.toMiddleware() }
        )
    }

    override suspend fun uploadUrl(command: CommandEntity.UploadBlock) {
        middleware.uploadMediaBlockContent(command)
    }

    override suspend fun updateTextStyle(command: CommandEntity.UpdateStyle) {
        middleware.updateTextStyle(command)
    }

    override suspend fun updateTextColor(command: CommandEntity.UpdateTextColor) {
        middleware.updateTextColor(command)
    }

    override suspend fun updateBackroundColor(command: CommandEntity.UpdateBackgroundColor) {
        middleware.updateBackgroundColor(command)
    }

    override suspend fun updateCheckbox(command: CommandEntity.UpdateCheckbox) {
        middleware.updateCheckbox(
            command.context,
            command.target,
            command.isChecked
        )
    }

    override suspend fun create(command: CommandEntity.Create): String = middleware.createBlock(
        command.context,
        command.target,
        command.position,
        command.prototype
    )

    override suspend fun dnd(command: CommandEntity.Dnd) {
        middleware.dnd(command)
    }

    override suspend fun duplicate(command: CommandEntity.Duplicate): String =
        middleware.duplicate(command)

    override suspend fun unlink(command: CommandEntity.Unlink) {
        middleware.unlink(command)
    }

    override suspend fun merge(command: CommandEntity.Merge) {
        middleware.merge(command)
    }

    override suspend fun split(command: CommandEntity.Split): String = middleware.split(command)

    override suspend fun setIconName(
        command: CommandEntity.SetIconName
    ) = middleware.setIconName(command)

    override suspend fun setupBookmark(
        command: CommandEntity.SetupBookmark
    ) = middleware.setupBookmark(command)
}