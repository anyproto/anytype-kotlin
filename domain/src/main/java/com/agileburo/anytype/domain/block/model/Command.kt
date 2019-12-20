package com.agileburo.anytype.domain.block.model

sealed class Command {

    class Update(
        val contextId: String,
        val blockId: String,
        val text: String,
        val marks: List<Block.Content.Text.Mark>
    )

    class Create(
        val contextId: String,
        val targetId: String,
        val position: Position,
        val block: Block
    )

    class Dnd(
        val contextId: String,
        val targetId: String,
        val targetContextId: String,
        val blockIds: List<String>,
        val position: Position
    )
}