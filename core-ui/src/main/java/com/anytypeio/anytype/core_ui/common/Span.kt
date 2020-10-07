package com.anytypeio.anytype.core_ui.common

import android.graphics.Color
import android.graphics.Typeface
import android.text.Annotation
import android.text.TextPaint
import android.text.style.*

interface Span {
    class Bold : StyleSpan(Typeface.BOLD), Span
    class Italic : StyleSpan(Typeface.ITALIC), Span
    class Strikethrough : StrikethroughSpan(), Span
    class TextColor(color: Int) : ForegroundColorSpan(color), Span
    class Url(url: String, val color: Int) : URLSpan(url), Span {

        override fun updateDrawState(ds: TextPaint) {
            super.updateDrawState(ds)
            ds.color = color
        }
    }
    class Font(family: String) : TypefaceSpan(family), Span

    class Keyboard(value: String) : Annotation(KEYBOARD_KEY, value), Span {
        companion object {
            const val KEYBOARD_KEY = "keyboard"
        }
    }

    class Highlight(color: String) : Annotation(HIGHLIGHT_KEY, color), Span {
        companion object {
            const val HIGHLIGHT_KEY = "highlight"
        }
    }
}