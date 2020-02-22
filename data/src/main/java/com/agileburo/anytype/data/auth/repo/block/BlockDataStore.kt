package com.agileburo.anytype.data.auth.repo.block

import com.agileburo.anytype.data.auth.model.CommandEntity
import com.agileburo.anytype.data.auth.model.ConfigEntity
import com.agileburo.anytype.domain.common.Id

interface BlockDataStore {
    suspend fun create(command: CommandEntity.Create): Id
    suspend fun updateText(command: CommandEntity.UpdateText)
    suspend fun updateTextStyle(command: CommandEntity.UpdateStyle)
    suspend fun updateTextColor(command: CommandEntity.UpdateTextColor)
    suspend fun updateBackroundColor(command: CommandEntity.UpdateBackgroundColor)
    suspend fun updateCheckbox(command: CommandEntity.UpdateCheckbox)
    suspend fun dnd(command: CommandEntity.Dnd)
    suspend fun duplicate(command: CommandEntity.Duplicate): Id
    suspend fun merge(command: CommandEntity.Merge)
    suspend fun split(command: CommandEntity.Split)
    suspend fun unlink(command: CommandEntity.Unlink)
    suspend fun getConfig(): ConfigEntity
    suspend fun createPage(parentId: String): String
    suspend fun openPage(id: String)
    suspend fun closePage(id: String)
    suspend fun openDashboard(contextId: String, id: String)
    suspend fun closeDashboard(id: String)
}