package com.agileburo.anytype.domain.block.model

sealed class Command {

    class UpdateText(
        val contextId: String,
        val blockId: String,
        val text: String,
        val marks: List<Block.Content.Text.Mark>
    )

    class UpdateCheckbox(
        val context: String,
        val target: String,
        val isChecked: Boolean
    )

    /**
     * Params for creating a block
     * @property contextId id of the context of the block (i.e. page, dashboard or something else)
     * @property targetId id of the block associated with the block we need to create
     * @property position position of the block that we need to create in relation with the target block
     * @property prototype a prototype of the block we would like to create
     */
    class Create(
        val contextId: String,
        val targetId: String,
        val position: Position,
        val prototype: Block.Prototype
    )

    class Dnd(
        val contextId: String,
        val targetId: String,
        val targetContextId: String,
        val blockIds: List<String>,
        val position: Position
    )
}