package com.anytypeio.anytype.ext

import android.text.Editable
import android.text.Spanned
import com.anytypeio.anytype.core_models.Block.Content.Text.Mark
import com.anytypeio.anytype.core_models.ext.overlap
import com.anytypeio.anytype.core_models.misc.Overlap
import com.anytypeio.anytype.core_ui.common.Span
import com.anytypeio.anytype.core_ui.widgets.text.MentionSpan

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
            param = span.value
        )
        is Span.Highlight -> Mark(
            range = getSpanStart(span)..getSpanEnd(span),
            type = Mark.Type.BACKGROUND_COLOR,
            param = span.value
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
        is MentionSpan -> Mark(
            range = getSpanStart(span)..getSpanEnd(span),
            type = Mark.Type.MENTION,
            param = span.param
        )
        is Span.ObjectLink -> Mark(
            range = getSpanStart(span)..getSpanEnd(span),
            type = Mark.Type.OBJECT,
            param = span.link
        )
        else -> null
    }
}

fun <T> Spanned.isSpanInRange(textRange: IntRange, type: Class<T>): Boolean {
    val list = listOf(
        Overlap.INNER,
        Overlap.INNER_LEFT,
        Overlap.INNER_RIGHT,
        Overlap.EQUAL
    )
    getSpans(textRange.first, textRange.last, type).forEach {
        val overlap = textRange.overlap(
            IntRange(
                start = getSpanStart(it),
                endInclusive = getSpanEnd(it)
            )
        )
        if (overlap in list) {
            return true
        }
    }
    return false
}