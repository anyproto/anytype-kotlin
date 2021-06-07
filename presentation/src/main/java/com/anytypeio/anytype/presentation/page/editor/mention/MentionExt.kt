package com.anytypeio.anytype.presentation.page.editor.mention

fun String.getMentionName(untitled: String): String = if (this.isBlank()) untitled else this

/**
 * Filter all mentions by text without symbol @
 *
 */
fun List<Mention>.filterMentionsBy(text: String): List<Mention> {
    val filter = text.removePrefix(Mention.MENTION_PREFIX)
    return if (filter.isNotEmpty()) this.filter {
        it.title.contains(
            filter,
            ignoreCase = true
        )
    } else this
}
