package com.agileburo.anytype.domain.block.repo

import com.agileburo.anytype.domain.block.model.Command
import com.agileburo.anytype.domain.common.Id
import com.agileburo.anytype.domain.config.Config

interface BlockRepository {
    suspend fun dnd(command: Command.Dnd)
    suspend fun duplicate(command: Command.Duplicate): Id
    suspend fun unlink(command: Command.Unlink)
    suspend fun create(command: Command.Create)
    suspend fun merge(command: Command.Merge)
    suspend fun updateText(command: Command.UpdateText)
    suspend fun updateTextStyle(command: Command.UpdateStyle)
    suspend fun updateTextColor(command: Command.UpdateTextColor)
    suspend fun updateCheckbox(command: Command.UpdateCheckbox)
    suspend fun getConfig(): Config
    suspend fun createPage(parentId: String): Id
    suspend fun openPage(id: String)
    suspend fun closePage(id: String)
    suspend fun openDashboard(contextId: String, id: String)
    suspend fun closeDashboard(id: String)
}