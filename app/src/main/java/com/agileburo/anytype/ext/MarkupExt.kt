package com.agileburo.anytype.ext

import android.graphics.Typeface
import android.text.Editable
import android.text.style.ForegroundColorSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import com.agileburo.anytype.core_ui.common.ThemeColor
import com.agileburo.anytype.domain.block.model.Block.Content.Text.Mark

fun Editable.extractMarks(): List<Mark> = getSpans(0, length, Any::class.java).mapNotNull { span ->
    when (span) {
        is StrikethroughSpan -> {
            Mark(
                range = getSpanStart(span)..getSpanEnd(span),
                type = Mark.Type.STRIKETHROUGH
            )
        }
        is ForegroundColorSpan -> {
            Mark(
                range = getSpanStart(span)..getSpanEnd(span),
                type = Mark.Type.TEXT_COLOR,
                param = span.foregroundColor.let { background ->
                    ThemeColor.text[background]
                }
            )
        }
        is StyleSpan -> {
            Mark(
                range = getSpanStart(span)..getSpanEnd(span),
                type = when (span.style) {
                    Typeface.BOLD -> Mark.Type.BOLD
                    Typeface.ITALIC -> Mark.Type.ITALIC
                    else -> TODO()
                }
            )
        }
        else -> null
    }
}