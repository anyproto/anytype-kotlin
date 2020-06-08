package com.agileburo.anytype.ext

import android.text.Editable
import android.text.Spanned
import com.agileburo.anytype.core_ui.common.Span
import com.agileburo.anytype.core_ui.common.ThemeColor
import com.agileburo.anytype.domain.block.model.Block.Content.Text.Mark
import com.agileburo.anytype.domain.ext.overlap
import com.agileburo.anytype.domain.misc.Overlap

fun Editable.extractMarks(): List<Mark> = getSpans(0, length, Span::class.java).mapNotNull { span ->
    when (span) {
        is Span.Strikethrough -> {
            Mark(
                range = getSpanStart(span)..getSpanEnd(span),
                type = Mark.Type.STRIKETHROUGH
            )
        }
        is Span.TextColor-> Mark(
            range = getSpanStart(span)..getSpanEnd(span),
            type = Mark.Type.TEXT_COLOR,
            param = span.foregroundColor.let { color ->
                ThemeColor.text[color]
            }
        )
        is Span.Highlight -> Mark(
            range = getSpanStart(span)..getSpanEnd(span),
            type = Mark.Type.BACKGROUND_COLOR,
            param = span.value.let { background ->
                ThemeColor.background[background.toInt()]
            }
        )
        is Span.Italic -> Mark(
            range = getSpanStart(span)..getSpanEnd(span),
            type = Mark.Type.ITALIC
        )
        is Span.Bold -> Mark(
            range = getSpanStart(span)..getSpanEnd(span),
            type = Mark.Type.BOLD
        )
        is Span.Keyboard -> Mark(
            range = getSpanStart(span)..getSpanEnd(span),
            type = Mark.Type.KEYBOARD
        )
        is Span.Url -> Mark(
            range = getSpanStart(span)..getSpanEnd(span),
            type = Mark.Type.LINK,
            param = span.url
        )
        else -> null
    }
}

fun <T> Spanned.extractSpans(range: IntRange, type: Class<T>): List<T> {
    val spans = this.getSpans(range.first, range.last, type)
    return spans.toList()
}

fun <T> isSpanInRange(textRange: IntRange, text: Spanned, type: Class<T>): Boolean {
    val spans = text.extractSpans(textRange, type)

    for (span in spans) {
        val start = text.getSpanStart(span)
        val end = text.getSpanEnd(span)
        val spanRange = IntRange(start, end)
        val overlap = textRange.overlap(spanRange)
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