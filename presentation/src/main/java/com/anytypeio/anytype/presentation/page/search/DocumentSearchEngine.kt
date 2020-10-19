package com.anytypeio.anytype.presentation.page.search

import java.util.regex.Pattern

fun String.search(pattern: Pattern): List<IntRange> {
    val result = mutableListOf<IntRange>()
    val matcher = pattern.matcher(this)
    while (matcher.find()) {
        result.add(matcher.start()..matcher.end())
    }
    return result
}