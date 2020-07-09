package com.agileburo.anytype.core_ui.features.navigation

data class PageLinkView(
    val id: String,
    val title: String,
    val subtitle: String,
    val image: String?,
    val emoji: String?
)

fun PageLinkView.isContainsText(text: String): Boolean = title.contains(text, true) ||
        subtitle.contains(text, true)