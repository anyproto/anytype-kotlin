package com.anytypeio.anytype.core_ui.features.page

import android.text.Editable
import com.anytypeio.anytype.core_ui.common.Markup
import com.anytypeio.anytype.core_ui.common.Span
import com.anytypeio.anytype.core_ui.common.ThemeColor
import com.anytypeio.anytype.core_ui.widgets.text.MentionSpan

fun Editable.marks(): List<Markup.Mark> = getSpans(0, length, Span::class.java).mapNotNull { span ->
    when (span) {
        is Span.Strikethrough -> Markup.Mark(
            from = getSpanStart(span),
            to = getSpanEnd(span),
            type = Markup.Type.STRIKETHROUGH,
            param = null
        )
        is Span.TextColor -> Markup.Mark(
            from = getSpanStart(span),
            to = getSpanEnd(span),
            type = Markup.Type.TEXT_COLOR,
            param = span.foregroundColor.let { color ->
                ThemeColor.text[color]
            }
        )
        is Span.Highlight -> Markup.Mark(
            from = getSpanStart(span),
            to = getSpanEnd(span),
            type = Markup.Type.BACKGROUND_COLOR,
            param = span.value.let { background ->
                ThemeColor.background[background.toInt()]
            }
        )
        is Span.Italic -> Markup.Mark(
            from = getSpanStart(span),
            to = getSpanEnd(span),
            type = Markup.Type.ITALIC
        )
        is Span.Bold -> Markup.Mark(
            from = getSpanStart(span),
            to = getSpanEnd(span),
            type = Markup.Type.BOLD
        )
        is Span.Keyboard -> Markup.Mark(
            from = getSpanStart(span),
            to = getSpanEnd(span),
            type = Markup.Type.KEYBOARD
        )
        is Span.Url -> Markup.Mark(
            from = getSpanStart(span),
            to = getSpanEnd(span),
            type = Markup.Type.LINK,
            param = span.url
        )
        is MentionSpan -> Markup.Mark(
            from = getSpanStart(span),
            to = getSpanEnd(span),
            type = Markup.Type.MENTION,
            param = span.param
        )
        else -> null
    }
}