package com.anytypeio.anytype.domain.block.repo

import com.anytypeio.anytype.domain.base.Result
import com.anytypeio.anytype.domain.block.model.Command
import com.anytypeio.anytype.domain.block.model.Position
import com.anytypeio.anytype.domain.clipboard.Copy
import com.anytypeio.anytype.domain.clipboard.Paste
import com.anytypeio.anytype.domain.common.Hash
import com.anytypeio.anytype.domain.common.Id
import com.anytypeio.anytype.domain.config.Config
import com.anytypeio.anytype.domain.event.model.Payload
import com.anytypeio.anytype.domain.page.navigation.DocumentInfo
import com.anytypeio.anytype.domain.page.navigation.PageInfoWithLinks

interface BlockRepository {

    suspend fun uploadFile(command: Command.UploadFile): Hash

    suspend fun move(command: Command.Move): Payload
    suspend fun unlink(command: Command.Unlink): Payload

    suspend fun archiveDocument(command: Command.ArchiveDocument)

    suspend fun turnIntoDocument(command: Command.TurnIntoDocument): List<Id>

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
    suspend fun createDocument(command: Command.CreateDocument): Triple<Id, Id, Payload>

    /**
     * Creates a new document / page, without positioning and targets.
     * @return block id of the new document.
     */
    suspend fun createNewDocument(command: Command.CreateNewDocument): Id

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
    suspend fun updateTextStyle(command: Command.UpdateStyle) : Payload

    suspend fun updateTextColor(command: Command.UpdateTextColor): Payload
    suspend fun updateBackgroundColor(command: Command.UpdateBackgroundColor): Payload

    suspend fun updateCheckbox(command: Command.UpdateCheckbox): Payload
    suspend fun updateAlignment(command: Command.UpdateAlignment) : Payload

    suspend fun getConfig(): Config

    @Deprecated("Should be replaced by createDocument() command")
    suspend fun createPage(parentId: String, emoji: String? = null): Id

    suspend fun openPage(id: String): Result<Payload>
    suspend fun openProfile(id: String) : Payload

    suspend fun closePage(id: String)
    suspend fun openDashboard(contextId: String, id: String): Payload
    suspend fun closeDashboard(id: String)

    /**
     * Upload media or file block by path or url.
     */
    suspend fun uploadBlock(command: Command.UploadBlock): Payload

    suspend fun setDocumentEmojiIcon(command: Command.SetDocumentEmojiIcon)
    suspend fun setDocumentImageIcon(command: Command.SetDocumentImageIcon)

    suspend fun setupBookmark(command: Command.SetupBookmark): Payload

    suspend fun undo(command: Command.Undo): Payload
    suspend fun redo(command: Command.Redo): Payload

    suspend fun copy(command: Command.Copy): Copy.Response
    suspend fun paste(command: Command.Paste): Paste.Response

    suspend fun getPageInfoWithLinks(pageId: String): PageInfoWithLinks
    suspend fun getListPages(): List<DocumentInfo>

    suspend fun linkToObject(
        context: Id,
        target: Id,
        block: Id,
        replace: Boolean,
        position: Position
    ): Payload
}