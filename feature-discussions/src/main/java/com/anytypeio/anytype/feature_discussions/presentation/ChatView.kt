package com.anytypeio.anytype.feature_discussions.presentation

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Hash
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.search.GlobalSearchItemView

sealed interface ChatView {

    data class DateSection(
        val formattedDate: String,
        val timeInMillis: Long
    ) : ChatView

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
    ) : ChatView {

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
                val isCode = styles.any { it.type == Block.Content.Text.Mark.Type.KEYBOARD }
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
                val url: String,
                val name: String,
                val ext: String
            ): Attachment()

            data class Link(
                val target: Id,
                val wrapper: ObjectWrapper.Basic?,
                val icon: ObjectIcon = ObjectIcon.None,
                val typeName: String
            ): Attachment()
        }

        sealed class ChatBoxAttachment {
            data class Media(
                val uri: String
            ): ChatBoxAttachment()
            data class File(
                val uri: String,
                val name: String,
                val size: Int
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

object ChatConfig {
    const val MAX_ATTACHMENT_COUNT = 10
}