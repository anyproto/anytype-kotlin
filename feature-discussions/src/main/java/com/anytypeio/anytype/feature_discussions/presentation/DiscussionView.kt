package com.anytypeio.anytype.feature_discussions.presentation

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Hash
import com.anytypeio.anytype.core_models.Id

sealed interface DiscussionView {

    data class Comment(
        val id: String,
        val content: Content,
        val author: String,
        val creator: Id?,
        val timestamp: Long,
        val formattedDate: String? = null,
        val reactions: List<Reaction> = emptyList(),
        val replyCount: Int = 0,
        val avatar: Avatar = Avatar.Initials(),
        val isOwn: Boolean = false
    ) : DiscussionView

    data class Reply(
        val id: String,
        val content: Content,
        val author: String,
        val creator: Id?,
        val timestamp: Long,
        val formattedDate: String? = null,
        val reactions: List<Reaction> = emptyList(),
        val avatar: Avatar = Avatar.Initials(),
        val depth: Int = 1,
        val isOwn: Boolean = false
    ) : DiscussionView

    data class ReplyDivider(val replyId: String, val depth: Int = 1) : DiscussionView

    data class ThreadDivider(val threadId: String) : DiscussionView

    data class Content(val msg: String, val parts: List<Part>) {
        data class Part(
            val part: String,
            val styles: List<Block.Content.Text.Mark> = emptyList()
        ) {
            val isBold: Boolean = styles.any { it.type == Block.Content.Text.Mark.Type.BOLD }
            val isItalic: Boolean = styles.any { it.type == Block.Content.Text.Mark.Type.ITALIC }
            val isStrike = styles.any { it.type == Block.Content.Text.Mark.Type.STRIKETHROUGH }
            val underline = styles.any { it.type == Block.Content.Text.Mark.Type.UNDERLINE }
            val isCode = styles.any { it.type == Block.Content.Text.Mark.Type.KEYBOARD }
            val link = styles.find { it.type == Block.Content.Text.Mark.Type.LINK }
            val mention = styles.find { it.type == Block.Content.Text.Mark.Type.MENTION }
        }
    }

    data class Reaction(
        val emoji: String,
        val count: Int,
        val isSelected: Boolean = false
    )

    sealed class Avatar {
        data class Initials(val initial: String = "") : Avatar()
        data class Image(val hash: Hash, val fallbackInitial: String = "") : Avatar()
    }
}

data class DiscussionHeader(
    val title: String = "",
    val commentCount: Int = 0
)
