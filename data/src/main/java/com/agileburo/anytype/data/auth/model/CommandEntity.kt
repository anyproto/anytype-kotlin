package com.agileburo.anytype.data.auth.model

class CommandEntity {

    class UpdateText(
        val contextId: String,
        val blockId: String,
        val text: String,
        val marks: List<BlockEntity.Content.Text.Mark>
    )

    class UpdateCheckbox(
        val context: String,
        val target: String,
        val isChecked: Boolean
    )

    class Create(
        val contextId: String,
        val targetId: String,
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
}