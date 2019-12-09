package com.agileburo.anytype.domain.block.model

sealed class Command {
    class Update(
        val contextId: String,
        val blockId: String,
        val text: String
    )

    class Create(
        val contextId: String,
        val targetId: String,
        val position: Position,
        val block: Block
    )
}