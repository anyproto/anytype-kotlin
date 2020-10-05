package com.anytypeio.anytype.domain.block.model

import com.anytypeio.anytype.domain.common.Hash
import com.anytypeio.anytype.domain.common.Id

sealed class Command {

    class UploadFile(
        val path: String,
        val type: Block.Content.File.Type
    )

    /**
     * Command for archiving a document
     * @property context id of the context
     * @property target ids of the targets
     */
    class ArchiveDocument(
        val context: Id,
        val target: List<Id>,
        val isArchived: Boolean
    )

    /**
     * Command for turning simple blocks into documents
     * @property context id of the context
     * @property targets id of the targets
     */
    class TurnIntoDocument(
        val context: Id,
        val targets: List<Id>
    )

    /**
     * @property contextId context id
     * @property blockId target block id
     * @property text updated text
     * @property marks marks of the updated text
     */
    class UpdateText(
        val contextId: Id,
        val blockId: Id,
        val text: String,
        val marks: List<Block.Content.Text.Mark>
    ) : Command()

    /**
     * Commands for updating document's title
     * @property context id of the context
     * @property title new title for the document
     */
    class UpdateTitle(
        val context: Id,
        val title: String
    )

    /**
     * Command for replacing target block by a new block (created from prototype)
     * @property context id of the context
     * @property target id of the block, which we need to replace
     * @property prototype prototype of the new block
     */
    data class Replace(
        val context: Id,
        val target: Id,
        val prototype: Block.Prototype
    )

    /**
     * Command for updating the whole block's text color.
     * @property context context id
     * @property target id of the target block, whose color we need to update.
     * @property color new color (hex)
     */
    data class UpdateTextColor(
        val context: Id,
        val target: Id,
        val color: String
    )

    /**
     * Command for updating background color for the whole block.
     * @property context context id
     * @property targets id of the target blocks, whose background color we need to update.
     * @property color new color (hex)
     */
    data class UpdateBackgroundColor(
        val context: Id,
        val targets: List<Id>,
        val color: String
    )

    /**
     * Command for updating alignment for the block.
     * @property context context id
     * @property targets id of the target blocks, whose alignments we need to update.
     * @property alignment new alignment
     */
    data class UpdateAlignment(
        val context: Id,
        val targets: List<Id>,
        val alignment: Block.Align
    )

    /**
     * @property context context id
     * @property target id of the target checkbox block
     * @property isChecked new checked/unchecked state for this checkbox block
     */
    class UpdateCheckbox(
        val context: Id,
        val target: Id,
        val isChecked: Boolean
    )

    /**
     * Command for updating style for one textual block.
     * @property context context id
     * @property targets id of the target blocks, whose style we need to update.
     * @property style new style for the target block.
     */
    data class UpdateStyle(
        val context: Id,
        val targets: List<Id>,
        val style: Block.Content.Text.Style
    )

    /**
     * Command for creating a block
     * @property context id of the context of the block (i.e. page, dashboard or something else)
     * @property target id of the block associated with the block we need to create
     * @property position position of the block that we need to create in relation with the target block
     * @property prototype a prototype of the block we would like to create
     */
    class Create(
        val context: Id,
        val target: Id,
        val position: Position,
        val prototype: Block.Prototype
    )

    /**
     * Command for creating a new document / page
     * @property context id of the context of the block (i.e. page, dashboard or something else)
     * @property target id of the block associated with the block we need to create
     * @property position position of the block that we need to create in relation with the target block
     * @property emoji random emoji for new page
     */
    class CreateDocument(
        val context: Id,
        val target: Id,
        val position: Position,
        val emoji: String?
    )

    /**
     * Command for creating a new document / page
     */
    class CreateNewDocument(
        val name: String,
        val emoji: String?
    )

    class Move(
        val contextId: Id,
        val targetId: Id,
        val targetContextId: Id,
        val blockIds: List<String>,
        val position: Position
    )

    /**
     * Command for block duplication
     * @property context context id
     * @property original id of the original block, which we need to duplicate
     */
    class Duplicate(
        val context: Id,
        val original: Id
    )

    /**
     * Command for unlinking a set of blocks from its context (i.e. page)
     * @property context context id
     * @property targets ids of the blocks, which we need to unlink from its [context]
     */
    class Unlink(
        val context: Id,
        val targets: List<Id>
    )

    /**
     * Command for merging two blocks into one block
     * @property context context id
     * @property pair pair of the blocks, which we need to merge
     */
    data class Merge(
        val context: Id,
        val pair: Pair<Id, Id>
    )

    /**
     * Command for splitting one block into two blocks
     * @property context context id
     * @property target id of the target block, which we need to split
     * @property style target block text style
     * @property index index or cursor position
     */
    data class Split(
        val context: Id,
        val target: Id,
        val style: Block.Content.Text.Style,
        val range: IntRange,
        val mode: BlockSplitMode
    )

    /**
     * Command for uploading media or file block
     * @property contextId context id
     * @property blockId id of the block
     * @property url valid url
     * @property filePath file uri
     */
    data class UploadBlock(
        val contextId: Id,
        val blockId: Id,
        val url: String,
        val filePath: String
    )

    /**
     * Command for setting document's emoji icon
     * @property emoji emoji's unicode
     * @property target id of the target block (icon)
     * @property context id of the context for this operation
     */
    data class SetDocumentEmojiIcon(
        val context: Id,
        val target: Id,
        val emoji: String
    )

    /**
     * Command for setting document's image icon
     * @property hash image hash
     * @property context id of the context for this operation
     */
    data class SetDocumentImageIcon(
        val context: Id,
        val hash: Hash
    )

    /**
     * Command for setting up a bookmark from [url]
     * @property context id of the context
     * @property target id of the target block (future bookmark block)
     * @property url bookmark url
     */
    data class SetupBookmark(
        val context: Id,
        val target: Id,
        val url: String
    )

    /**
     * Command for undoing latest changes in document
     * @property context id of the context
     */
    data class Undo(val context: Id)

    /**
     * Command for redoing latest changes in document
     * @property context id of the context
     */
    data class Redo(val context: Id)

    /**
     * Command for clipboard paste operation
     * @property context id of the context
     * @property focus id of the focused/target block
     * @property selected id of currently selected blocks
     * @property range selected text range
     * @property text plain text to paste
     * @property html optional html to paste
     * @property blocks blocks currently contained in clipboard
     */
    data class Paste(
        val context: Id,
        val focus: Id,
        val selected: List<Id>,
        val range: IntRange,
        val text: String,
        val html: String?,
        val blocks: List<Block>
    )

    /**
     * Command for clipboard copy operation.
     * @param context id of the context
     * @param range selected text range
     * @param blocks associated blocks
     */
    data class Copy(
        val context: Id,
        val range: IntRange?,
        val blocks: List<Block>
    )
}