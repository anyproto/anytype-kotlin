package com.anytypeio.anytype.middleware.block

import com.anytypeio.anytype.data.auth.model.*
import com.anytypeio.anytype.data.auth.repo.block.BlockRemote
import com.anytypeio.anytype.middleware.converters.mark
import com.anytypeio.anytype.middleware.converters.toEntity
import com.anytypeio.anytype.middleware.interactor.Middleware

class BlockMiddleware(
    private val middleware: Middleware
) : BlockRemote {

    override suspend fun getConfig(): ConfigEntity = middleware.config

    override suspend fun openDashboard(
        contextId: String,
        id: String
    ): PayloadEntity = middleware.openDashboard(contextId, id)

    override suspend fun closeDashboard(id: String) {
        middleware.closeDashboard(id)
    }

    override suspend fun createPage(parentId: String, emoji: String?): String =
        middleware.createPage(parentId, emoji)

    override suspend fun openPage(id: String): PayloadEntity = middleware.openBlock(id)
    override suspend fun openProfile(id: String): PayloadEntity = middleware.openBlock(id)

    override suspend fun closePage(id: String) {
        middleware.closePage(id)
    }

    override suspend fun updateDocumentTitle(command: CommandEntity.UpdateTitle) {
        middleware.updateDocumentTitle(command)
    }

    override suspend fun updateText(command: CommandEntity.UpdateText) {
        middleware.updateText(
            command.contextId,
            command.blockId,
            command.text,
            command.marks.map { it.mark() }
        )
    }

    override suspend fun uploadBlock(command: CommandEntity.UploadBlock) : PayloadEntity =
        middleware.uploadBlock(command)

    override suspend fun updateTextStyle(
        command: CommandEntity.UpdateStyle
    ) : PayloadEntity = middleware.updateTextStyle(command)

    override suspend fun updateTextColor(
        command: CommandEntity.UpdateTextColor
    ): PayloadEntity = middleware.updateTextColor(command)

    override suspend fun updateBackgroundColor(
        command: CommandEntity.UpdateBackgroundColor
    ): PayloadEntity = middleware.updateBackgroundColor(command)

    override suspend fun updateAlignment(
        command: CommandEntity.UpdateAlignment
    ) : PayloadEntity = middleware.updateAlignment(command)

    override suspend fun updateCheckbox(
        command: CommandEntity.UpdateCheckbox
    ): PayloadEntity = middleware.updateCheckbox(
        command.context,
        command.target,
        command.isChecked
    )

    override suspend fun create(
        command: CommandEntity.Create
    ): Pair<String, PayloadEntity> = middleware.createBlock(
        command.context,
        command.target,
        command.position,
        command.prototype
    )

    override suspend fun createDocument(
        command: CommandEntity.CreateDocument
    ): Triple<String, String, PayloadEntity> = middleware.createDocument(command)

    override suspend fun duplicate(
        command: CommandEntity.Duplicate
    ): Pair<String, PayloadEntity> = middleware.duplicate(command)

    override suspend fun move(command: CommandEntity.Move): PayloadEntity {
        return middleware.move(command)
    }

    override suspend fun unlink(
        command: CommandEntity.Unlink
    ): PayloadEntity = middleware.unlink(command)

    override suspend fun merge(
        command: CommandEntity.Merge
    ): PayloadEntity = middleware.merge(command)

    override suspend fun split(
        command: CommandEntity.Split
    ): Pair<String, PayloadEntity> = middleware.split(command)

    override suspend fun setDocumentEmojiIcon(
        command: CommandEntity.SetDocumentEmojiIcon
    ) = middleware.setDocumentEmojiIcon(command)

    override suspend fun setDocumentImageIcon(
        command: CommandEntity.SetDocumentImageIcon
    ) = middleware.setDocumentImageIcon(command)

    override suspend fun setupBookmark(
        command: CommandEntity.SetupBookmark
    ): PayloadEntity = middleware.setupBookmark(command)

    override suspend fun undo(
        command: CommandEntity.Undo
    ) : PayloadEntity = middleware.undo(command)

    override suspend fun redo(
        command: CommandEntity.Redo
    ) : PayloadEntity = middleware.redo(command)

    override suspend fun archiveDocument(
        command: CommandEntity.ArchiveDocument
    ) = middleware.archiveDocument(command)

    override suspend fun turnIntoDocument(
        command: CommandEntity.TurnIntoDocument
    ): List<String> = middleware.turnIntoDocument(command)

    override suspend fun replace(
        command: CommandEntity.Replace
    ): Pair<String, PayloadEntity> = middleware.replace(command)

    override suspend fun paste(
        command: CommandEntity.Paste
    ): Response.Clipboard.Paste = middleware.paste(command)

    override suspend fun copy(
        command: CommandEntity.Copy
    ): Response.Clipboard.Copy = middleware.copy(command)

    override suspend fun uploadFile(
        command: CommandEntity.UploadFile
    ): String = middleware.uploadFile(command).hash

    override suspend fun getPageInfoWithLinks(pageId: String): PageInfoWithLinksEntity =
        middleware.getPageInfoWithLinks(pageId).toEntity()

    override suspend fun getListPages(): List<DocumentInfoEntity> =
        middleware.listPages.map { it.toEntity() }

    override suspend fun linkToObject(
        context: String,
        target: String,
        block: String,
        replace: Boolean,
        position: PositionEntity
    ): PayloadEntity = middleware.linkToObject(context, target, block, replace, position)
}