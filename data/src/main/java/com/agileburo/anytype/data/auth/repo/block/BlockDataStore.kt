package com.agileburo.anytype.data.auth.repo.block

import com.agileburo.anytype.data.auth.model.*
import com.agileburo.anytype.domain.common.Id

interface BlockDataStore {

    suspend fun create(command: CommandEntity.Create): Pair<Id, PayloadEntity>
    suspend fun replace(command: CommandEntity.Replace): Pair<Id, PayloadEntity>
    suspend fun duplicate(command: CommandEntity.Duplicate): Pair<Id, PayloadEntity>
    suspend fun split(command: CommandEntity.Split): Pair<Id, PayloadEntity>

    suspend fun merge(command: CommandEntity.Merge): PayloadEntity
    suspend fun updateTextColor(command: CommandEntity.UpdateTextColor): PayloadEntity
    suspend fun updateBackroundColor(command: CommandEntity.UpdateBackgroundColor): PayloadEntity
    suspend fun updateAlignment(command: CommandEntity.UpdateAlignment) : PayloadEntity
    suspend fun openDashboard(contextId: String, id: String): PayloadEntity

    suspend fun createDocument(command: CommandEntity.CreateDocument): Pair<Id, Id>
    suspend fun updateDocumentTitle(command: CommandEntity.UpdateTitle)
    suspend fun updateText(command: CommandEntity.UpdateText)
    suspend fun updateTextStyle(command: CommandEntity.UpdateStyle) : PayloadEntity

    suspend fun updateCheckbox(command: CommandEntity.UpdateCheckbox)
    suspend fun uploadBlock(command: CommandEntity.UploadBlock): PayloadEntity
    suspend fun dnd(command: CommandEntity.Dnd)
    suspend fun unlink(command: CommandEntity.Unlink): PayloadEntity
    suspend fun getConfig(): ConfigEntity
    suspend fun createPage(parentId: String): String
    suspend fun openPage(id: String): PayloadEntity
    suspend fun openProfile(id: String): PayloadEntity
    suspend fun closePage(id: String)
    suspend fun closeDashboard(id: String)
    suspend fun setDocumentEmojiIcon(command: CommandEntity.SetDocumentEmojiIcon)
    suspend fun setDocumentImageIcon(command: CommandEntity.SetDocumentImageIcon)
    suspend fun setupBookmark(command: CommandEntity.SetupBookmark) : PayloadEntity
    suspend fun undo(command: CommandEntity.Undo) : PayloadEntity
    suspend fun redo(command: CommandEntity.Redo) : PayloadEntity
    suspend fun archiveDocument(command: CommandEntity.ArchiveDocument)
    suspend fun paste(command: CommandEntity.Paste) : Response.Clipboard.Paste
    suspend fun copy(command: CommandEntity.Copy) : Response.Clipboard.Copy

    suspend fun uploadFile(command: CommandEntity.UploadFile): String

    suspend fun getPageInfoWithLinks(pageId: String): PageInfoWithLinksEntity

    suspend fun getListPages(): List<PageInfoEntity>
}