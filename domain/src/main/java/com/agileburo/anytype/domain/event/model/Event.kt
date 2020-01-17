package com.agileburo.anytype.domain.event.model

import com.agileburo.anytype.domain.block.model.Block
import com.agileburo.anytype.domain.common.Id

sealed class Event {

    sealed class Command : Event() {

        data class ShowBlock(
            val rootId: String,
            val blocks: List<Block>
        ) : Command()

        data class AddBlock(
            val blocks: List<Block>
        ) : Command()

        data class DeleteBlock(
            val target: Id
        ) : Command()

        data class UpdateBlockText(
            val id: String,
            val text: String
        ) : Command()

        /**
         * Command to update a block structure.
         * @property context context id for this command (i.e page id, dashboard id, etc.)
         * @property id id of the block whose structure we need to update
         * @property children list of children ids for this block [id]
         */
        data class UpdateStructure(
            val context: String,
            val id: String,
            val children: List<String>
        ) : Command()
    }
}