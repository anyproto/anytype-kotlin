package com.agileburo.anytype.domain.event.model

import com.agileburo.anytype.domain.block.model.Block
import com.agileburo.anytype.domain.block.model.Block.Content.Text
import com.agileburo.anytype.domain.common.Hash
import com.agileburo.anytype.domain.common.Id
import com.agileburo.anytype.domain.common.Url

sealed class Event {

    abstract val context: Id

    sealed class Command : Event() {

        data class ShowBlock(
            override val context: Id,
            val root: Id,
            val details: Block.Details = Block.Details(emptyMap()),
            val blocks: List<Block>
        ) : Command()

        data class AddBlock(
            override val context: String,
            val blocks: List<Block>,
            val details: Block.Details = Block.Details(emptyMap())
        ) : Command()

        /**
         * Command to delete blocks
         * @property context id of the context
         * @property targets id of the target blocks, which we need to delete
         */
        data class DeleteBlock(
            override val context: String,
            val targets: List<Id>
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
         * @property backgroundColor background color of the whole block (considered updated if not null)
         */
        data class GranularChange(
            override val context: String,
            val id: Id,
            val text: String? = null,
            val style: Text.Style? = null,
            val color: String? = null,
            val backgroundColor: String? = null,
            val marks: List<Text.Mark>? = null,
            val alignment: Block.Align? = null
        ) : Command()

        /**
         * Command to update link.
         * @property context update's context
         * @property id id of the link
         * @property target id of the linked block
         * @property fields link's fields (considered updated if not null)
         */
        data class LinkGranularChange(
            override val context: String,
            val id: Id,
            val target: Id,
            val fields: Block.Fields?
        ) : Command()

        /**
         * Command to update bookmark
         * @property context id of the context
         * @property target id of the bookmark block
         * @property url bookmark's url (considered updated if not null)
         * @property title bookmark's title (considered updated if not null)
         * @property description bookmark's description (considered updated if not null)
         * @property image bookmark's image hash (considered updated if not null)
         * @property favicon bookmark's favicon hash (considered updated if not null)
         */
        data class BookmarkGranularChange(
            override val context: Id,
            val target: Id,
            val url: Url?,
            val title: String?,
            val description: String?,
            val image: Hash?,
            val favicon: Hash?
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

        data class UpdateFields(
            override val context: Id,
            val target: Id,
            val fields: Block.Fields
        ) : Command()

        /**
         * Command to update details (metadata) of the target block
         * @property context id of the context
         * @property target id of the target block, whose details we need to update
         * @property details details of the target block
         */
        data class UpdateDetails(
            override val context: Id,
            val target: Id,
            val details: Block.Fields
        ) : Command()

        /**
         * Command to update file block content
         */
        data class UpdateFileBlock(
            override val context: String,
            val id: Id,
            val state: Block.Content.File.State? = null,
            val type: Block.Content.File.Type? = null,
            val name: String? = null,
            val hash: String? = null,
            val mime: String? = null,
            val size: Long? = null
        ) : Command()
    }
}