package com.anytypeio.anytype.core_ui.features.editor

import android.text.Editable
import android.util.Log
import com.anytypeio.anytype.core_ui.common.Span
import com.anytypeio.anytype.core_ui.common.Underline
import com.anytypeio.anytype.core_ui.widgets.text.MentionTextWithIconSpan
import com.anytypeio.anytype.core_ui.widgets.text.MentionTextWithoutIconSpan
import com.anytypeio.anytype.presentation.editor.editor.Markup

private fun removeZeroWidthSpaces(editable: Editable, placeholders: List<Int>) {
    // Sort descending so we don't shift the positions of earlier placeholders
    for (pos in placeholders.sortedDescending()) {
        if (pos >= 0 && pos < editable.length) {
            if (editable[pos] == '\u200B') {
                editable.delete(pos, pos + 1)
            }
        }
    }
}

fun extractStringAndMarksFromEditable(editable: Editable): Pair<String, List<Markup.Mark>> {

    // 1) Extract all marks + icon placeholder indices
    val (newMarks, placeholderPositions) = editable.marksAndPositions()

    // 2) Remove those placeholders in descending index order
    if (placeholderPositions.isNotEmpty()) {
        removeZeroWidthSpaces(editable, placeholderPositions)
        //3 We need to re-check or re-extract spans AFTER spaces removal
        val (finalMarks, _) = editable.marksAndPositions()
        return editable.toString() to finalMarks
    } else {
        return editable.toString() to newMarks
    }
}

fun Editable.marksAndPositions(): Pair<List<Markup.Mark>, List<Int>> {

    val placeholderPositions = mutableListOf<Int>()

    val editableSpans = getSpans(0, length, Span::class.java)

    val newMarks = editableSpans.mapNotNull { span ->
        Log.d("Test1983", "Span: $span")
        when (span) {
            is Span.Strikethrough -> Markup.Mark.Strikethrough(
                from = getSpanStart(span),
                to = getSpanEnd(span)
            )

            is Span.TextColor -> Markup.Mark.TextColor(
                from = getSpanStart(span),
                to = getSpanEnd(span),
                color = span.value
            )

            is Span.Highlight -> Markup.Mark.BackgroundColor(
                from = getSpanStart(span),
                to = getSpanEnd(span),
                background = span.value
            )

            is Span.Italic -> Markup.Mark.Italic(
                from = getSpanStart(span),
                to = getSpanEnd(span)
            )

            is Underline -> Markup.Mark.Underline(
                from = getSpanStart(span),
                to = getSpanEnd(span)
            )

            is Span.Bold -> Markup.Mark.Bold(
                from = getSpanStart(span),
                to = getSpanEnd(span)
            )

            is Span.Keyboard -> Markup.Mark.Keyboard(
                from = getSpanStart(span),
                to = getSpanEnd(span)
            )

            is Span.Url -> Markup.Mark.Link(
                from = getSpanStart(span),
                to = getSpanEnd(span),
                param = span.url
            )

            is Span.ObjectLink -> Markup.Mark.Object(
                from = getSpanStart(span),
                to = getSpanEnd(span),
                param = span.link.orEmpty(),
                isArchived = false
            )

            is MentionTextWithIconSpan -> {
                Log.d("Test1983", "MentionTextWithIconSpan: $span")
                val start = getSpanStart(span)
                val end = getSpanEnd(span)
                val placeholderIndex =
                    (start).takeIf { it >= 0 } // might be -1 if the mention is at 0
                // If placeholderIndex is valid and indeed a \u200B, we record it
                if (placeholderIndex != null && placeholderIndex < length) {
                    if (this[placeholderIndex] == '\u200B') {
                        placeholderPositions += placeholderIndex
                    }
                }
                Log.d("Test1983", "MentionTextWithIconSpan: $start, $end, placeholderPositions: $placeholderPositions")
                Markup.Mark.Mention.Base(
                    from = start,
                    to = end,
                    param = span.param,
                    isArchived = false
                )
            }

            is MentionTextWithoutIconSpan -> Markup.Mark.Mention.Base(
                from = getSpanStart(span),
                to = getSpanEnd(span),
                param = span.param,
                isArchived = false
            )

            else -> null
        }
    }
    return newMarks to placeholderPositions
}