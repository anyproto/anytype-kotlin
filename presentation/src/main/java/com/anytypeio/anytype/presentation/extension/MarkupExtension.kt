package com.anytypeio.anytype.presentation.extension

import com.anytypeio.anytype.core_ui.common.Markup
import com.anytypeio.anytype.domain.ext.overlap
import com.anytypeio.anytype.domain.misc.Overlap

/**
 * Check if list of marks contains [Markup.Type] in selection range
 *
 * @param marks list of marks
 * @param selection range to check if mark type is included
 */
fun Markup.Type.isInRange(marks: List<Markup.Mark>, selection: IntRange): Boolean {
    if (selection.first >= selection.last) return false
    val filtered = marks.filter { it.type == this }
    if (filtered.isEmpty()) return false
    filtered.forEach { mark ->
        val range = mark.from..mark.to
        val overlap = selection.overlap(range)
        if (overlap in listOf(
                Overlap.INNER,
                Overlap.INNER_LEFT,
                Overlap.INNER_RIGHT,
                Overlap.EQUAL
            )
        ) {
            return true
        }
    }
    return false
}