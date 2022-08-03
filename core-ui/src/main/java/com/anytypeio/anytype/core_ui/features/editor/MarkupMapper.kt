package com.anytypeio.anytype.core_ui.features.editor

import android.text.Editable
import com.anytypeio.anytype.core_ui.common.Span
import com.anytypeio.anytype.core_ui.widgets.text.MentionSpan
import com.anytypeio.anytype.presentation.editor.editor.Markup

fun Editable.marks(): List<Markup.Mark> = getSpans(0, length, Span::class.java).mapNotNull { span ->
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
        is MentionSpan -> Markup.Mark.Mention.Base(
            from = getSpanStart(span),
            to = getSpanEnd(span),
            param = span.param.orEmpty()
        )
        is Span.ObjectLink -> Markup.Mark.Object(
            from = getSpanStart(span),
            to = getSpanEnd(span),
            param = span.link.orEmpty()
        )
        else -> null
    }
}