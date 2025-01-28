package com.anytypeio.anytype.core_ui.text

import com.anytypeio.anytype.core_models.Block

fun String.splitByMarks(
    marks: List<Block.Content.Text.Mark>
) : List<Pair<String, List<Block.Content.Text.Mark>>> {

    if (this.isEmpty()) return emptyList()

    // Create a map to track styles applied to each character index
    val styleMap = mutableMapOf<Int, MutableList<Block.Content.Text.Mark>>()

    // Populate the style map with styles for each index in the ranges
    for (styledRange in marks) {
        for (index in styledRange.range.first until styledRange.range.last) {
            if (index in indices) {
                // Add the style to the current index, initializing the list if needed
                styleMap.computeIfAbsent(index) { mutableListOf() }.add(styledRange)
            }
        }
    }

    val result = mutableListOf<Pair<String, List<Block.Content.Text.Mark>>>()
    var currentIndex = 0

    while (currentIndex < length) {
        // Get the styles at the current index (or an empty list if none)
        val stylesAtCurrentIndex = styleMap[currentIndex] ?: emptyList()

        // Find the extent of this style group (i.e., where styles change)
        var endIndex = currentIndex
        while (endIndex < length && (styleMap[endIndex] ?: emptyList()) == stylesAtCurrentIndex) {
            endIndex++
        }

        // Add the substring and its associated styles to the result
        result.add(substring(currentIndex until endIndex) to stylesAtCurrentIndex)

        // Move to the next group
        currentIndex = endIndex
    }

    return result
}