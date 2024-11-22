package com.anytypeio.anytype.feature_discussions.presentation

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Hash
import com.anytypeio.anytype.core_models.chats.Chat

sealed interface DiscussionView {
    data class Message(
        val id: String,
        val content: List<Content.Part>,
        val author: String,
        val timestamp: Long,
        val attachments: List<Chat.Message.Attachment> = emptyList(),
        val reactions: List<Reaction> = emptyList(),
        val isUserAuthor: Boolean = false,
        val isEdited: Boolean = false,
        val avatar: Avatar = Avatar.Initials()
    ) : DiscussionView {

        interface Content {
            data class Part(
                val part: String,
                val styles: List<Block.Content.Text.Mark>
            ) : Content {
                val isBold: Boolean = styles.any { it.type == Block.Content.Text.Mark.Type.BOLD }
                val isItalic: Boolean = styles.any { it.type == Block.Content.Text.Mark.Type.ITALIC }
                val isStrike = styles.any { it.type == Block.Content.Text.Mark.Type.STRIKETHROUGH }
                val underline = styles.any { it.type == Block.Content.Text.Mark.Type.UNDERLINE }
                val link = styles.find { it.type == Block.Content.Text.Mark.Type.LINK }
            }
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