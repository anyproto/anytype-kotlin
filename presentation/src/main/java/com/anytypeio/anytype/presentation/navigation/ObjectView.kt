package com.anytypeio.anytype.presentation.navigation

data class ObjectView(
    val id: String,
    val title: String,
    val subtitle: String,
    val image: String?,
    val emoji: String?
)

fun ObjectView.isContainsText(text: String): Boolean = title.contains(text, true) ||
        subtitle.contains(text, true)

fun List<ObjectView>.filterBy(text: String): List<ObjectView> =
    if (text.isNotEmpty()) this.filter { it.isContainsText(text) } else this