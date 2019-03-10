package com.agileburo.anytype.editor

import android.graphics.Typeface
import android.text.Editable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.CharacterStyle
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan

class EditorTextWatcher : TextWatcher {

    var spannableText: SpannableString? = null
    var spanBold: StyleSpan? = null
    var spanItalic: StyleSpan? = null
    var spanStrike: StrikethroughSpan? = null

    var isBoldActive = false
    var isItalicActive = false
    var isStrokeThroughActive = false

    override fun afterTextChanged(s: Editable?) {
        spannableText?.let {
            spanBold?.let { span ->
                s?.setSpanWithCheck(it.getSpanStart(span), it.getSpanEnd(span), span)
            }
            spanItalic?.let { span ->
                s?.setSpanWithCheck(it.getSpanStart(span), it.getSpanEnd(span), span)
            }
            spanStrike?.let { span ->
                s?.setSpanWithCheck(it.getSpanStart(span), it.getSpanEnd(span), span)
            }
        }
        spannableText = null
        spanBold = null
        spanItalic = null
        spanStrike = null
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        spannableText = SpannableString(s).apply {
            if (isBoldActive) {
                spanBold = StyleSpan(Typeface.BOLD)
                setSpan(spanBold, start, start + count, Spanned.SPAN_COMPOSING)
            }
            if (isItalicActive) {
                spanItalic = StyleSpan(Typeface.ITALIC)
                setSpan(spanItalic, start, start + count, Spanned.SPAN_COMPOSING)
            }
            if (isStrokeThroughActive) {
                spanStrike = StrikethroughSpan()
                setSpan(spanStrike, start, start + count, Spanned.SPAN_COMPOSING)
            }
        }
    }
}

fun Editable.setSpanWithCheck(start: Int, end: Int, span: CharacterStyle) {
    if (start > -1 && end > -1) {
        this.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
}