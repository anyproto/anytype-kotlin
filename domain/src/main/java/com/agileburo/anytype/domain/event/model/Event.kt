package com.agileburo.anytype.domain.event.model

import com.agileburo.anytype.domain.block.model.Block
import com.agileburo.anytype.domain.block.model.Block.Content.Text
import com.agileburo.anytype.domain.common.Id

sealed class Event {

    abstract val context: Id

    sealed class Command : Event() {

        data class ShowBlock(
            override val context: String,
            val rootId: Id,
            val blocks: List<Block>
        ) : Command()

        data class AddBlock(
            override val context: String,
            val blocks: List<Block>
        ) : Command()

        data class DeleteBlock(
            override val context: String,
            val target: Id
        ) : Command()

        data class UpdateBlockText(
            override val context: String,
            val id: Id,
            val text: String
        ) : Command()

        /**
         * Command to update block's text content.
         * @property id id of the target block
         * @property text new text (considered updated if not null)
         * @property style new style (considered updated if not null)
         * @property color new color of the whole block (considered updated if not null)
         */
        data class GranularChange(
            override val context: String,
            val id: Id,
            val text: String? = null,
            val style: Text.Style? = null,
            val color: String? = null
        ) : Command() {
            fun onlyTextChanged() = style == null && color == null && text != null
        }

        /**
         * Command to update link.
         * @property context update's context
         * @property id id of the link
         * @property target id of the linked block
         * @property fields link's fields (considered update if not null)
         */
        data class LinkGranularChange(
            override val context: String,
            val id: Id,
            val target: Id,
            val fields: Block.Fields?
        ) : Command()

        /**
         * Command to update a block structure.
         * @property context context id for this command (i.e page id, dashboard id, etc.)
         * @property id id of the block whose structure we need to update
         * @property children list of children ids for this block [id]
         */
        data class UpdateStructure(
            override val context: String,
            val id: Id,
            val children: List<Id>
        ) : Command()
    }
}