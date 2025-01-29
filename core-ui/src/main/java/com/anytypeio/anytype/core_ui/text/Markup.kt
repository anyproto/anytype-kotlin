package com.anytypeio.anytype.core_ui.text

import com.anytypeio.anytype.core_models.Block

fun String.splitByMarks(
    marks: List<Block.Content.Text.Mark>
): List<Pair<String, List<Block.Content.Text.Mark>>> {
    if (isEmpty()) return emptyList()

    val styleMap = mutableMapOf<Int, MutableList<Block.Content.Text.Mark>>()
    marks.forEach { mark ->
        for (index in mark.range.first until mark.range.last.coerceAtMost(length)) {
            styleMap.computeIfAbsent(index) { mutableListOf() }.add(mark)
        }
    }

    val result = mutableListOf<Pair<String, List<Block.Content.Text.Mark>>>()
    var currentIndex = 0

    while (currentIndex < length) {
        val currentStyles = styleMap[currentIndex] ?: emptyList()
        var endIndex = currentIndex

        while (endIndex < length && (styleMap[endIndex] ?: emptyList()) == currentStyles) {
            endIndex++
        }

        result.add(substring(currentIndex until endIndex) to currentStyles)
        currentIndex = endIndex
    }

    return result
}
