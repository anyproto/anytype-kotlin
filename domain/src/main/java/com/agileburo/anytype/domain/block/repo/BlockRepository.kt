package com.agileburo.anytype.domain.block.repo

import com.agileburo.anytype.domain.block.model.Command
import com.agileburo.anytype.domain.common.Id
import com.agileburo.anytype.domain.config.Config
import com.agileburo.anytype.domain.event.model.Payload

interface BlockRepository {
    suspend fun dnd(command: Command.Dnd)
    suspend fun unlink(command: Command.Unlink): Payload

    suspend fun archiveDocument(command: Command.ArchiveDocument)

    /**
     * Duplicates target block
     * @return id of the new block and payload events.
     */
    suspend fun duplicate(command: Command.Duplicate): Pair<Id, Payload>

    /**
     * Creates a new block.
     * @return id of the created block with event payload.
     */
    suspend fun create(command: Command.Create): Pair<Id, Payload>

    /**
     * Creates a new document / page.
     * @return pair of values, where the first one is block id and the second one is target id.
     */
    suspend fun createDocument(command: Command.CreateDocument): Pair<Id, Id>

    suspend fun merge(command: Command.Merge): Payload

    /**
     * Splits one block into two blocks.
     * @return id of the block, created as a result of splitting.
     */
    suspend fun split(command: Command.Split): Pair<Id, Payload>

    /**
     * Replaces target block by a new block (created from prototype).
     * @see Command.Replace for details
     * @return id of the new block
     */
    suspend fun replace(command: Command.Replace): Pair<Id, Payload>

    suspend fun updateDocumentTitle(command: Command.UpdateTitle)
    suspend fun updateText(command: Command.UpdateText)
    suspend fun updateTextStyle(command: Command.UpdateStyle)

    suspend fun updateTextColor(command: Command.UpdateTextColor): Payload
    suspend fun updateBackgroundColor(command: Command.UpdateBackgroundColor): Payload

    suspend fun updateCheckbox(command: Command.UpdateCheckbox)
    suspend fun updateAlignment(command: Command.UpdateAlignment) : Payload

    suspend fun getConfig(): Config

    @Deprecated("Should be replaced by createDocument() command")
    suspend fun createPage(parentId: String): Id

    suspend fun openPage(id: String): Payload
    suspend fun closePage(id: String)
    suspend fun openDashboard(contextId: String, id: String): Payload
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