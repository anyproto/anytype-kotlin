package com.agileburo.anytype.data.auth.model

/**
 * For documentation, please refer to domain models description.
 */
class CommandEntity {

    class UpdateText(
        val contextId: String,
        val blockId: String,
        val text: String,
        val marks: List<BlockEntity.Content.Text.Mark>
    )

    data class UpdateStyle(
        val context: String,
        val target: String,
        val style: BlockEntity.Content.Text.Style
    )

    data class UpdateTextColor(
        val context: String,
        val target: String,
        val color: String
    )

    data class UpdateBackgroundColor(
        val context: String,
        val target: String,
        val color: String
    )

    class UpdateCheckbox(
        val context: String,
        val target: String,
        val isChecked: Boolean
    )

    class Create(
        val context: String,
        val target: String,
        val position: PositionEntity,
        val prototype: BlockEntity.Prototype
    )

    class Dnd(
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
        val index: Int
    )

    data class SetIconName(
        val context: String,
        val target: String,
        val name: String
    )
}