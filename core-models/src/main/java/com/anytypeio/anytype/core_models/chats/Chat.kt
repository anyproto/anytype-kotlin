package com.anytypeio.anytype.core_models.chats

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Id

sealed class Chat {

    /**
     * @property [id] message id
     */
    data class Message(
        val id: Id,
        val order: Id,
        val creator: Id,
        val createdAt: Long,
        val modifiedAt: Long,
        val content: Content?,
        val attachments: List<Attachment> = emptyList(),
        val reactions: Map<String, List<String>>,
        val replyToMessageId: Id? = null,
    ) {
        data class Content(
            val text: String,
            val style: Block.Content.Text.Style,
            val marks: List<Block.Content.Text.Mark>
        )
        data class Attachment(
            val target: Id,
            val type: Type
        ) {
            sealed class Type {
                data object File: Type()
                data object Image: Type()
                data object Link: Type()
            }
        }

        companion object {
            /**
             * New message builder.
             */
            fun new(
                text: String,
                attachments: List<Attachment> = emptyList()
            ) : Message = Chat.Message(
                id = "",
                createdAt = 0L,
                modifiedAt = 0L,
                attachments = attachments,
                reactions = emptyMap(),
                creator = "",
                replyToMessageId = "",
                content = Chat.Message.Content(
                    text = text,
                    marks = emptyList(),
                    style = Block.Content.Text.Style.P
                ),
                order = ""
            )

            /**
             * Updated message builder.
             */
            fun updated(
                id: Id,
                text: String
            ) : Message = Chat.Message(
                id = id,
                createdAt = 0L,
                modifiedAt = 0L,
                attachments = emptyList(),
                reactions = emptyMap(),
                creator = "",
                replyToMessageId = "",
                content = Chat.Message.Content(
                    text = text,
                    marks = emptyList(),
                    style = Block.Content.Text.Style.P
                ),
                order = ""
            )
        }
    }
}