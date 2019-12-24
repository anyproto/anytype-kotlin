package com.agileburo.anytype.core_ui.common

import android.graphics.Typeface
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.style.CharacterStyle
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan

/**
 * Classes implementing this interface should support markup rendering.
 */
interface Markup {

    /**
     * A text body that this markup should be applied to.
     */
    val body: String

    /**
     * List of marks associated with the text body.
     */
    val marks: List<Mark>

    /**
     * @property from caracter index where this markup starts (inclusive)
     * @property to caracter index where this markup ends (inclusive)
     * @property type markup's type
     */
    data class Mark(
        val from: Int,
        val to: Int,
        val type: Type
    )

    /**
     * Markup types.
     */
    enum class Type {
        ITALIC,
        BOLD,
        STRIKETHROUGH
    }
}

fun Markup.toSpannable() = SpannableString(body).apply {
    marks.forEach { mark ->
        when (mark.type) {
            Markup.Type.ITALIC -> setSpan(
                StyleSpan(Typeface.ITALIC),
                mark.from,
                mark.to,
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )
            Markup.Type.BOLD -> setSpan(
                StyleSpan(Typeface.BOLD),
                mark.from,
                mark.to,
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )
            Markup.Type.STRIKETHROUGH -> setSpan(
                StrikethroughSpan(),
                mark.from,
                mark.to,
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )
        }
    }
}

fun Editable.setMarkup(markup: Markup) {
    getSpans(0, length, CharacterStyle::class.java).forEach { span ->
        removeSpan(span)
    }
    markup.marks.forEach { mark ->
        when (mark.type) {
            Markup.Type.ITALIC -> setSpan(
                StyleSpan(Typeface.ITALIC),
                mark.from,
                mark.to,
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )
            Markup.Type.BOLD -> setSpan(
                StyleSpan(Typeface.BOLD),
                mark.from,
                mark.to,
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )
            Markup.Type.STRIKETHROUGH -> setSpan(
                StrikethroughSpan(),
                mark.from,
                mark.to,
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )
        }
    }
}