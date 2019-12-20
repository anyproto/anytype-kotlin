package com.agileburo.anytype.data.auth.model

class CommandEntity {

    class Update(
        val contextId: String,
        val blockId: String,
        val text: String,
        val marks: List<BlockEntity.Content.Text.Mark>
    )

    class Create(
        val contextId: String,
        val targetId: String,
        val position: PositionEntity,
        val block: BlockEntity
    )

    class Dnd(
        val contextId: String,
        val dropTargetId: String,
        val dropTargetContextId: String,
        val blockIds: List<String>,
        val position: PositionEntity
    )
}