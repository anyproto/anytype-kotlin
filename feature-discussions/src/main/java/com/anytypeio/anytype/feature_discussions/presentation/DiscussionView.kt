package com.anytypeio.anytype.feature_discussions.presentation

sealed interface DiscussionView {
    data class Message(
        val id: String,
        val msg: String,
        val author: String,
        val timestamp: Long
    ) : DiscussionView
}