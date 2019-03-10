package com.agileburo.anytype.editor

import android.graphics.Typeface
import android.text.Editable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.CharacterStyle
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan

class EditorTextWatcher(
    private val codeBlockTypeface : Typeface
) : TextWatcher {

    private var spannableText: SpannableString? = null

    private var spanBold: StyleSpan? = null
    private var spanItalic: StyleSpan? = null
    private var spanStrike: StrikethroughSpan? = null
    private var spanUnderline : UnderlineSpan? = null
    private var spanCodeBlock : CodeBlockSpan? = null

    var isBoldActive = false
    var isItalicActive = false
    var isStrokeThroughActive = false
    var isUnderlineActive = false
    var isCodeBlockActive = false

    override fun afterTextChanged(s: Editable?) {

        spannableText?.let { spannable ->
            spanBold?.let { span ->
                s?.setSpanWithCheck(spannable.getSpanStart(span), spannable.getSpanEnd(span), span)
            }
            spanItalic?.let { span ->
                s?.setSpanWithCheck(spannable.getSpanStart(span), spannable.getSpanEnd(span), span)
            }
            spanStrike?.let { span ->
                s?.setSpanWithCheck(spannable.getSpanStart(span), spannable.getSpanEnd(span), span)
            }
            spanUnderline?.let { span ->
                s?.setSpanWithCheck(spannable.getSpanStart(span), spannable.getSpanEnd(span), span)
            }
            spanCodeBlock?.let { span ->
                s?.setSpanWithCheck(spannable.getSpanStart(span), spannable.getSpanEnd(span), span)
            }
        }

        spannableText = null

        clearSpans()
    }

    private fun clearSpans() {
        spanBold = null
        spanItalic = null
        spanStrike = null
        spanUnderline = null
        spanCodeBlock = null
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
            if (isUnderlineActive) {
                spanUnderline = UnderlineSpan()
                setSpan(spanUnderline, start, start + count, Spanned.SPAN_COMPOSING)
            }
            if (isCodeBlockActive) {
                spanCodeBlock = CodeBlockSpan(codeBlockTypeface)
                setSpan(spanCodeBlock, start, start + count, Spanned.SPAN_COMPOSING)
            }

        }
    }
}

fun Editable.setSpanWithCheck(start: Int, end: Int, span: CharacterStyle) {
    if (start > -1 && end > -1) {
        this.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
}