package com.agileburo.anytype.data.auth.repo.block

import com.agileburo.anytype.data.auth.model.*
import com.agileburo.anytype.domain.common.Id

interface BlockRemote {

    suspend fun create(command: CommandEntity.Create): Pair<String, PayloadEntity>
    suspend fun replace(command: CommandEntity.Replace): Pair<String, PayloadEntity>
    suspend fun duplicate(command: CommandEntity.Duplicate): Pair<String, PayloadEntity>
    suspend fun split(command: CommandEntity.Split): Pair<Id, PayloadEntity>

    suspend fun merge(command: CommandEntity.Merge): PayloadEntity
    suspend fun unlink(command: CommandEntity.Unlink): PayloadEntity
    suspend fun updateTextColor(command: CommandEntity.UpdateTextColor): PayloadEntity
    suspend fun updateBackgroundColor(command: CommandEntity.UpdateBackgroundColor): PayloadEntity
    suspend fun updateAlignment(command: CommandEntity.UpdateAlignment) : PayloadEntity

    suspend fun createDocument(command: CommandEntity.CreateDocument): Triple<String, String, PayloadEntity>
    suspend fun updateDocumentTitle(command: CommandEntity.UpdateTitle)
    suspend fun updateText(command: CommandEntity.UpdateText)
    suspend fun updateTextStyle(command: CommandEntity.UpdateStyle) : PayloadEntity

    suspend fun updateCheckbox(command: CommandEntity.UpdateCheckbox): PayloadEntity
    suspend fun move(command: CommandEntity.Move): PayloadEntity
    suspend fun getConfig(): ConfigEntity
    suspend fun createPage(parentId: String, emoji: String?): String
    suspend fun openPage(id: String): PayloadEntity
    suspend fun openProfile(id: String): PayloadEntity
    suspend fun closePage(id: String)
    suspend fun openDashboard(contextId: String, id: String): PayloadEntity
    suspend fun closeDashboard(id: String)
    suspend fun setDocumentEmojiIcon(command: CommandEntity.SetDocumentEmojiIcon)
    suspend fun setDocumentImageIcon(command: CommandEntity.SetDocumentImageIcon)
    suspend fun uploadBlock(command: CommandEntity.UploadBlock): PayloadEntity
    suspend fun setupBookmark(command: CommandEntity.SetupBookmark) : PayloadEntity
    suspend fun undo(command: CommandEntity.Undo) : PayloadEntity
    suspend fun redo(command: CommandEntity.Redo) : PayloadEntity
    suspend fun archiveDocument(command: CommandEntity.ArchiveDocument)
    suspend fun turnIntoDocument(command: CommandEntity.TurnIntoDocument): List<Id>
    suspend fun paste(command: CommandEntity.Paste) : Response.Clipboard.Paste
    suspend fun copy(command: CommandEntity.Copy) : Response.Clipboard.Copy

    suspend fun uploadFile(command: CommandEntity.UploadFile): String

    suspend fun getPageInfoWithLinks(pageId: String): PageInfoWithLinksEntity

    suspend fun getListPages(): List<PageInfoEntity>

    suspend fun linkToObject(
        context: String,
        target: String,
        block: String,
        replace: Boolean
    ): PayloadEntity
}