package com.anytypeio.anytype.feature_discussions.presentation

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Hash
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.chats.Chat

sealed interface DiscussionView {
    data class Message(
        val id: String,
        val content: Content,
        val author: String,
        val timestamp: Long,
        val attachments: List<Attachment> = emptyList(),
        val reactions: List<Reaction> = emptyList(),
        val isUserAuthor: Boolean = false,
        val isEdited: Boolean = false,
        val avatar: Avatar = Avatar.Initials()
    ) : DiscussionView {

        data class Content(val msg: String, val parts: List<Part>) {
            data class Part(
                val part: String,
                val styles: List<Block.Content.Text.Mark> = emptyList()
            ) {
                val isBold: Boolean = styles.any { it.type == Block.Content.Text.Mark.Type.BOLD }
                val isItalic: Boolean = styles.any { it.type == Block.Content.Text.Mark.Type.ITALIC }
                val isStrike = styles.any { it.type == Block.Content.Text.Mark.Type.STRIKETHROUGH }
                val underline = styles.any { it.type == Block.Content.Text.Mark.Type.UNDERLINE }
                val link = styles.find { it.type == Block.Content.Text.Mark.Type.LINK }
            }
        }

        sealed class Attachment {
            data class Image(
                val target: Id,
                val url: String
            ): Attachment()

            data class Link(
                val target: Id
            ): Attachment()
        }

        data class Reaction(
            val emoji: String,
            val count: Int,
            val isSelected: Boolean = false
        )
        sealed class Avatar {
            data class Initials(val initial: String = ""): Avatar()
            data class Image(val hash: Hash): Avatar()
        }
    }
}