package com.anytypeio.anytype.presentation.extension

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.ext.overlap
import com.anytypeio.anytype.core_models.misc.Overlap
import com.anytypeio.anytype.core_utils.ext.mapInPlace
import com.anytypeio.anytype.presentation.editor.editor.Markup
import com.anytypeio.anytype.presentation.editor.markup.MarkupStyleDescriptor

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

/**
 * Check if list of marks contains [Markup.Type] in selection range
 *
 * @param marks list of marks
 * @param selection range to check if mark type is included
 */
fun Block.Content.Text.Mark.Type.isInRange(marks: List<Block.Content.Text.Mark>, selection: IntRange): Boolean {
    if (selection.first >= selection.last) return false
    val filtered = marks.filter { it.type == this }
    if (filtered.isEmpty()) return false
    filtered.forEach { mark ->
        val range = mark.range
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

fun Block.style(selection: IntRange): MarkupStyleDescriptor {

    var isBold = false
    var isItalic = false
    var isStrike = false
    var isCode = false
    var isLinked = false

    var markupUrl: String? = null
    var markupTextColor: String? = null
    var markupHighlightColor: String? = null

    var blockTextColor: String? = null
    var blockBackgroundColor: String? = null

    val data = content
    if (data is Block.Content.Text) {
        blockTextColor = data.color
        blockBackgroundColor = data.backgroundColor
        data.marks.forEach { mark ->
            when (mark.type) {
                Block.Content.Text.Mark.Type.BOLD -> {
                    if (!isBold) {
                        val overlap = selection.overlap(mark.range)
                        if (overlap.inside()) isBold = true
                    }
                }
                Block.Content.Text.Mark.Type.ITALIC -> {
                    if (!isItalic) {
                        val overlap = selection.overlap(mark.range)
                        if (overlap.inside()) isItalic = true
                    }
                }
                Block.Content.Text.Mark.Type.STRIKETHROUGH -> {
                    if (!isStrike) {
                        val overlap = selection.overlap(mark.range)
                        if (overlap.inside()) isStrike = true
                    }
                }
                Block.Content.Text.Mark.Type.KEYBOARD -> {
                    if (!isCode) {
                        val overlap = selection.overlap(mark.range)
                        if (overlap.inside()) isCode = true
                    }
                }
                Block.Content.Text.Mark.Type.LINK -> {
                    if (!isLinked) {
                        val overlap = selection.overlap(mark.range)
                        if (overlap.inside()) {
                            isLinked = true
                            markupUrl = mark.param
                        }
                    }
                }
                Block.Content.Text.Mark.Type.TEXT_COLOR -> {
                    if (markupTextColor == null) {
                        val overlap = selection.overlap(mark.range)
                        if (overlap.inside()) markupTextColor = mark.param
                    }
                }
                Block.Content.Text.Mark.Type.BACKGROUND_COLOR -> {
                    if (markupHighlightColor == null) {
                        val overlap = selection.overlap(mark.range)
                        if (overlap.inside()) markupHighlightColor = mark.param
                    }
                }
            }
        }
    }
    return MarkupStyleDescriptor.Default(
        range = selection,
        isBold = isBold,
        isItalic = isItalic,
        isStrikethrough = isStrike,
        isCode = isCode,
        isLinked = isLinked,
        markupTextColor = markupTextColor,
        markupHighlightColor = markupHighlightColor,
        markupUrl = markupUrl,
        blockTextColor = blockTextColor,
        blockBackroundColor = blockBackgroundColor
    )
}

fun Overlap.inside(): Boolean =
    this == Overlap.INNER ||
    this == Overlap.INNER_LEFT ||
    this == Overlap.INNER_RIGHT ||
    this == Overlap.EQUAL

/**
 * Recalculate marks ranges
 *
 * @param from start position for shifting markup ranges
 * @param length defines the number of positions to shift, could be negative
 * @return shifted Marks
 */
fun List<Markup.Mark>.shift(from: Int, length: Int): List<Markup.Mark> =
    map { it.updateRanges(start = from, length = length) }

/**
 * Recalculate marks ranges in mutable list
 *
 * @param start start position for shifting markup ranges
 * @param length defines the number of positions to shift, could be negative
 * @return Same mutable list with shifted Marks
 */
fun MutableList<Markup.Mark>.shift(start: Int, length: Int) {
    this.mapInPlace { it.updateRanges(start = start, length = length) }
}

private fun Markup.Mark.updateRanges(start: Int, length: Int): Markup.Mark {
    var newFrom = this.from
    var newTo = this.to
    if ((newFrom <= start) && (newTo > start)) {
        newTo += length
    } else {
        if (newFrom >= start) {
            newFrom += length
            newTo += length
        }
    }
    return this.copy(
        from = newFrom,
        to = newTo
    )
}