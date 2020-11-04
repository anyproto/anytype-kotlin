package com.anytypeio.anytype.presentation.page.editor.mention

data class Mention(
    val id: String,
    val title: String,
    val emoji: String?,
    val image: String?
) {
    companion object {
        const val MENTION_PREFIX = "@"
    }
}