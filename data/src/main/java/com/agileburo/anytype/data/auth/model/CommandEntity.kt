package com.agileburo.anytype.data.auth.model

/**
 * For documentation, please refer to domain models description.
 */
class CommandEntity {

    class TurnIntoDocument(
        val context: String,
        val targets: List<String>
    )

    class UploadFile(
        val path: String,
        val type: BlockEntity.Content.File.Type
    )

    class ArchiveDocument(
        val context: String,
        val target: String
    )

    class UpdateText(
        val contextId: String,
        val blockId: String,
        val text: String,
        val marks: List<BlockEntity.Content.Text.Mark>
    )

    class UpdateTitle(
        val context: String,
        val title: String
    )

    data class UpdateStyle(
        val context: String,
        val targets: List<String>,
        val style: BlockEntity.Content.Text.Style
    )

    data class UpdateTextColor(
        val context: String,
        val target: String,
        val color: String
    )

    data class UpdateAlignment(
        val context: String,
        val targets: List<String>,
        val alignment: BlockEntity.Align
    )

    data class UpdateBackgroundColor(
        val context: String,
        val targets: List<String>,
        val color: String
    )

    class UpdateCheckbox(
        val context: String,
        val target: String,
        val isChecked: Boolean
    )

    class UploadBlock(
        val contextId: String,
        val blockId: String,
        val url: String,
        val filePath: String
    )

    class Create(
        val context: String,
        val target: String,
        val position: PositionEntity,
        val prototype: BlockEntity.Prototype
    )

    class CreateDocument(
        val context: String,
        val target: String,
        val position: PositionEntity,
        val prototype: BlockEntity.Prototype.Page,
        val emoji: String?
    )

    class Move(
        val contextId: String,
        val dropTargetId: String,
        val dropTargetContextId: String,
        val blockIds: List<String>,
        val position: PositionEntity
    )

    class Duplicate(
        val context: String,
        val original: String
    )

    class Unlink(
        val context: String,
        val targets: List<String>
    )

    data class Merge(
        val context: String,
        val pair: Pair<String, String>
    )

    data class Split(
        val context: String,
        val target: String,
        val style: BlockEntity.Content.Text.Style,
        val index: Int
    )

    data class SetDocumentEmojiIcon(
        val context: String,
        val target: String,
        val emoji: String
    )

    data class SetDocumentImageIcon(
        val context: String,
        val hash: String
    )

    data class SetupBookmark(
        val context: String,
        val target: String,
        val url: String
    )

    data class Undo(val context: String)

    data class Redo(val context: String)

    data class Replace(
        val context: String,
        val target: String,
        val prototype: BlockEntity.Prototype
    )

    data class Paste(
        val context: String,
        val focus: String,
        val selected: List<String>,
        val range: IntRange,
        val text: String,
        val html: String?,
        val blocks: List<BlockEntity>
    )

    data class Copy(
        val context: String,
        val range: IntRange?,
        val blocks: List<BlockEntity>
    )
}