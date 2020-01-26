package com.agileburo.anytype.data.auth.model

sealed class EventEntity {

    sealed class Command : EventEntity() {

        data class ShowBlock(
            val rootId: String,
            val blocks: List<BlockEntity>
        ) : Command()

        data class AddBlock(
            val blocks: List<BlockEntity>
        ) : Command()

        data class UpdateBlockText(
            val id: String,
            val text: String
        ) : Command()

        data class GranularChange(
            val id: String,
            val text: String? = null,
            val style: BlockEntity.Content.Text.Style? = null
        ) : Command()

        data class UpdateStructure(
            val context: String,
            val id: String,
            val children: List<String>
        ) : Command()

        data class DeleteBlock(
            val target: String
        ) : Command()
    }
}