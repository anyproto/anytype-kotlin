package com.anytypeio.anytype.feature_discussions.presentation

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Hash
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.presentation.search.GlobalSearchItemView

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
        val avatar: Avatar = Avatar.Initials(),
        val reply: Reply? = null
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

        data class Reply(
            val msg: Id,
            val text: String,
            val author: String
        )

        sealed class Attachment {
            data class Image(
                val target: Id,
                val url: String
            ): Attachment()

            data class Link(
                val target: Id,
                val wrapper: ObjectWrapper.Basic?
            ): Attachment()
        }

        sealed class ChatBoxAttachment {
            data class Media(
                val uri: String
            ): ChatBoxAttachment()
            data class Link(
                val target: Id,
                val wrapper: GlobalSearchItemView
            ): ChatBoxAttachment()
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