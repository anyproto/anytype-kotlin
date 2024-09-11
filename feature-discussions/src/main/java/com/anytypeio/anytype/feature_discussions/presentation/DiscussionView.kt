package com.anytypeio.anytype.feature_discussions.presentation

import com.anytypeio.anytype.core_models.chats.Chat

sealed interface DiscussionView {
    data class Message(
        val id: String,
        val msg: String,
        val author: String,
        val timestamp: Long,
        val attachments: List<Chat.Message.Attachment> = emptyList(),
        val reactions: List<Reaction> = emptyList(),
        val isUserAuthor: Boolean = false,
    ) : DiscussionView {
        data class Reaction(
            val emoji: String,
            val count: Int,
            val isSelected: Boolean = false
        )
    }
}