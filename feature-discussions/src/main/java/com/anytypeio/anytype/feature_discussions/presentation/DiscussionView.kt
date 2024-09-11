package com.anytypeio.anytype.feature_discussions.presentation

import com.anytypeio.anytype.presentation.search.GlobalSearchItemView

sealed interface DiscussionView {
    data class Message(
        val id: String,
        val msg: String,
        val author: String,
        val timestamp: Long,
        val attachments: List<Attachment> = emptyList(),
        val reactions: List<Pair<String, Int>> = emptyList(),
        val isUserAuthor: Boolean = false,
    ) : DiscussionView {
        data class Attachment(
            val item: GlobalSearchItemView
        )
    }
}