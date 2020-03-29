package com.agileburo.anytype.data.auth.repo.block

import com.agileburo.anytype.data.auth.model.CommandEntity
import com.agileburo.anytype.data.auth.model.ConfigEntity
import com.agileburo.anytype.domain.common.Id

interface BlockRemote {
    suspend fun create(command: CommandEntity.Create): Id
    suspend fun createDocument(command: CommandEntity.CreateDocument): Pair<String, String>
    suspend fun updateText(command: CommandEntity.UpdateText)
    suspend fun updateTextStyle(command: CommandEntity.UpdateStyle)
    suspend fun updateTextColor(command: CommandEntity.UpdateTextColor)
    suspend fun updateBackroundColor(command: CommandEntity.UpdateBackgroundColor)
    suspend fun updateCheckbox(command: CommandEntity.UpdateCheckbox)
    suspend fun dnd(command: CommandEntity.Dnd)
    suspend fun merge(command: CommandEntity.Merge)
    suspend fun split(command: CommandEntity.Split): Id
    suspend fun duplicate(command: CommandEntity.Duplicate): Id
    suspend fun unlink(command: CommandEntity.Unlink)
    suspend fun getConfig(): ConfigEntity
    suspend fun createPage(parentId: String): String
    suspend fun openPage(id: String)
    suspend fun closePage(id: String)
    suspend fun openDashboard(contextId: String, id: String)
    suspend fun closeDashboard(id: String)
    suspend fun setIconName(command: CommandEntity.SetIconName)
    suspend fun uploadUrl(command: CommandEntity.UploadBlock)
    suspend fun setupBookmark(command: CommandEntity.SetupBookmark)
    suspend fun undo(command: CommandEntity.Undo)
    suspend fun redo(command: CommandEntity.Redo)
    suspend fun archiveDocument(command: CommandEntity.ArchiveDocument)
}