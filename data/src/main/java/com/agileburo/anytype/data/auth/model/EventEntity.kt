package com.agileburo.anytype.data.auth.model

sealed class EventEntity {

    abstract val context: String

    sealed class Command : EventEntity() {

        data class ShowBlock(
            override val context: String,
            val rootId: String,
            val blocks: List<BlockEntity>
        ) : Command()

        data class AddBlock(
            override val context: String,
            val blocks: List<BlockEntity>
        ) : Command()

        data class UpdateBlockText(
            override val context: String,
            val id: String,
            val text: String
        ) : Command()

        data class GranularChange(
            override val context: String,
            val id: String,
            val text: String? = null,
            val style: BlockEntity.Content.Text.Style? = null,
            val color: String? = null,
            val backgroundColor: String? = null,
            val marks: List<BlockEntity.Content.Text.Mark>? = null
        ) : Command()

        data class LinkGranularChange(
            override val context: String,
            val id: String,
            val target: String,
            val fields: BlockEntity.Fields?
        ) : Command()

        data class UpdateStructure(
            override val context: String,
            val id: String,
            val children: List<String>
        ) : Command()

        data class DeleteBlock(
            override val context: String,
            val targets: List<String>
        ) : Command()

        data class UpdateFields(
            override val context: String,
            val target: String,
            val fields: BlockEntity.Fields
        ) : Command()
    }
}