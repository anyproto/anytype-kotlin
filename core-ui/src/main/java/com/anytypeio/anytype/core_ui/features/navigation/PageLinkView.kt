package com.anytypeio.anytype.core_ui.features.navigation

data class PageLinkView(
    val id: String,
    val title: String,
    val subtitle: String,
    val image: String?,
    val emoji: String?
)

fun PageLinkView.isContainsText(text: String): Boolean = title.contains(text, true) ||
        subtitle.contains(text, true)

fun List<PageLinkView>.filterBy(text: String): List<PageLinkView> =
    if (text.isNotEmpty()) this.filter { it.isContainsText(text) } else this