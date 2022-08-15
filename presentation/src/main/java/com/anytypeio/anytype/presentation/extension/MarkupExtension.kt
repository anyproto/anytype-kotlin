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
fun List<Markup.Mark>.isBoldInRange(selection: IntRange): Boolean {
    if (selection.first >= selection.last) return false
    return filterIsInstance(Markup.Mark.Bold::class.java).isInRange(selection)
}

fun List<Markup.Mark>.isItalicInRange(selection: IntRange): Boolean {
    if (selection.first >= selection.last) return false
    return filterIsInstance(Markup.Mark.Italic::class.java).isInRange(selection)
}

fun List<Markup.Mark>.isStrikethroughInRange(selection: IntRange): Boolean {
    if (selection.first >= selection.last) return false
    return filterIsInstance(Markup.Mark.Strikethrough::class.java).isInRange(selection)
}

fun List<Markup.Mark>.isKeyboardInRange(selection: IntRange): Boolean {
    if (selection.first >= selection.last) return false
    return filterIsInstance(Markup.Mark.Keyboard::class.java).isInRange(selection)
}

fun List<Markup.Mark>.isLinkInRange(selection: IntRange): Boolean {
    if (selection.first >= selection.last) return false
    return filterIsInstance(Markup.Mark.Link::class.java).isInRange(selection)
}

private fun List<Markup.Mark>.isInRange(selection: IntRange): Boolean {
    if (isEmpty()) return false
    forEach { mark ->
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

fun Block.style(selection: IntRange): MarkupStyleDescriptor {

    var isBold = false
    var isItalic = false
    var isStrike = false
    var isUnderline = false
    var isCode = false
    var isLinked = false

    var markupUrl: String? = null
    var markupTextColor: String? = null
    var markupHighlightColor: String? = null

    var blockTextColor: String? = null
    val blockBackgroundColor: String? = backgroundColor

    val data = content
    if (data is Block.Content.Text) {
        blockTextColor = data.color
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
                Block.Content.Text.Mark.Type.UNDERLINE -> {
                    if (!isUnderline) {
                        val overlap = selection.overlap(mark.range)
                        if (overlap.inside()) isUnderline = true
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
        blockBackroundColor = blockBackgroundColor,
        isUnderline = isUnderline
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
    return when (this) {
        is Markup.Mark.BackgroundColor -> copy(from = newFrom, to = newTo)
        is Markup.Mark.Bold -> copy(from = newFrom, to = newTo)
        is Markup.Mark.Italic -> copy(from = newFrom, to = newTo)
        is Markup.Mark.Underline -> copy(from = newFrom, to = newTo)
        is Markup.Mark.Keyboard -> copy(from = newFrom, to = newTo)
        is Markup.Mark.Link -> copy(from = newFrom, to = newTo)
        is Markup.Mark.Mention.Base -> copy(from = newFrom, to = newTo)
        is Markup.Mark.Mention.Deleted -> copy(from = newFrom, to = newTo)
        is Markup.Mark.Mention.Loading -> copy(from = newFrom, to = newTo)
        is Markup.Mark.Mention.WithEmoji -> copy(from = newFrom, to = newTo)
        is Markup.Mark.Mention.WithImage -> copy(from = newFrom, to = newTo)
        is Markup.Mark.Object -> copy(from = newFrom, to = newTo)
        is Markup.Mark.Strikethrough -> copy(from = newFrom, to = newTo)
        is Markup.Mark.TextColor -> copy(from = newFrom, to = newTo)
        is Markup.Mark.Mention.Task.Checked -> copy(from = newFrom, to = newTo)
        is Markup.Mark.Mention.Task.Unchecked -> copy(from = newFrom, to = newTo)
        is Markup.Mark.Mention.Profile.WithImage -> copy(from = newFrom, to = newTo)
        is Markup.Mark.Mention.Profile.WithInitials -> copy(from = newFrom, to = newTo)
    }
}