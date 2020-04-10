package com.agileburo.anytype.domain.block.repo

import com.agileburo.anytype.domain.block.model.Command
import com.agileburo.anytype.domain.common.Id
import com.agileburo.anytype.domain.config.Config

interface BlockRepository {
    suspend fun dnd(command: Command.Dnd)
    suspend fun duplicate(command: Command.Duplicate): Id
    suspend fun unlink(command: Command.Unlink)

    suspend fun archiveDocument(command: Command.ArchiveDocument)

    /**
     * Creates a new block.
     * @return id of the created block.
     */
    suspend fun create(command: Command.Create): Id

    /**
     * Creates a new document / page.
     * @return pair of values, where the first one is block id and the second one is target id.
     */
    suspend fun createDocument(command: Command.CreateDocument): Pair<Id, Id>

    suspend fun merge(command: Command.Merge)

    /**
     * Splits one block into two blocks.
     * @return id of the block, created as a result of splitting.
     */
    suspend fun split(command: Command.Split): Id

    /**
     * Replaces target block by a new block (created from prototype).
     * @see Command.Replace for details
     * @return id of the new block
     */
    suspend fun replace(command: Command.Replace): Id

    suspend fun updateDocumentTitle(command: Command.UpdateTitle)
    suspend fun updateText(command: Command.UpdateText)
    suspend fun updateTextStyle(command: Command.UpdateStyle)
    suspend fun updateTextColor(command: Command.UpdateTextColor)
    suspend fun updateBackgroundColor(command: Command.UpdateBackgroundColor)
    suspend fun updateCheckbox(command: Command.UpdateCheckbox)

    suspend fun getConfig(): Config

    @Deprecated("Should be replaced by createDocument() command")
    suspend fun createPage(parentId: String): Id

    suspend fun openPage(id: String)
    suspend fun closePage(id: String)
    suspend fun openDashboard(contextId: String, id: String)
    suspend fun closeDashboard(id: String)

    /**
     * Upload url for video block.
     */
    suspend fun uploadUrl(command: Command.UploadVideoBlockUrl)

    suspend fun setIconName(command: Command.SetIconName)

    suspend fun setupBookmark(command: Command.SetupBookmark)

    suspend fun undo(command: Command.Undo)
    suspend fun redo(command: Command.Redo)
}