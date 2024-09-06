package com.anytypeio.anytype.core_models.chats

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Id

sealed class Chat {

    data class Message(
        val id: Id,
        val order: Id,
        val creator: Id,
        val timestamp: Long,
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
    }
}