package com.agileburo.anytype.core_ui.common

import android.graphics.Color
import android.graphics.Typeface
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.style.*

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
        val type: Type,
        val param: Any? = null
    ) {

        fun color(): Int = Color.parseColor(param as String)

    }

    /**
     * Markup types.
     */
    enum class Type {
        ITALIC,
        BOLD,
        STRIKETHROUGH,
        TEXT_COLOR,
        LINK
    }

    companion object {
        const val DEFAULT_SPANNABLE_FLAG = Spannable.SPAN_EXCLUSIVE_INCLUSIVE
    }
}

fun Markup.toSpannable() = SpannableString(body).apply {
    marks.forEach { mark ->
        when (mark.type) {
            Markup.Type.ITALIC -> setSpan(
                StyleSpan(Typeface.ITALIC),
                mark.from,
                mark.to,
                Markup.DEFAULT_SPANNABLE_FLAG
            )
            Markup.Type.BOLD -> setSpan(
                StyleSpan(Typeface.BOLD),
                mark.from,
                mark.to,
                Markup.DEFAULT_SPANNABLE_FLAG
            )
            Markup.Type.STRIKETHROUGH -> setSpan(
                StrikethroughSpan(),
                mark.from,
                mark.to,
                Markup.DEFAULT_SPANNABLE_FLAG
            )
            Markup.Type.TEXT_COLOR -> setSpan(
                ForegroundColorSpan(mark.color()),
                mark.from,
                mark.to,
                Markup.DEFAULT_SPANNABLE_FLAG
            )
            Markup.Type.LINK -> setSpan(
                URLSpan(mark.param as String),
                mark.from,
                mark.to,
                Markup.DEFAULT_SPANNABLE_FLAG
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
                Markup.DEFAULT_SPANNABLE_FLAG
            )
            Markup.Type.BOLD -> setSpan(
                StyleSpan(Typeface.BOLD),
                mark.from,
                mark.to,
                Markup.DEFAULT_SPANNABLE_FLAG
            )
            Markup.Type.STRIKETHROUGH -> setSpan(
                StrikethroughSpan(),
                mark.from,
                mark.to,
                Markup.DEFAULT_SPANNABLE_FLAG
            )
            Markup.Type.TEXT_COLOR -> setSpan(
                ForegroundColorSpan(mark.color()),
                mark.from,
                mark.to,
                Markup.DEFAULT_SPANNABLE_FLAG
            )
            Markup.Type.LINK -> {
                setSpan(
                    URLSpan(mark.param as String),
                    mark.from,
                    mark.to,
                    Markup.DEFAULT_SPANNABLE_FLAG
                )
            }
        }
    }
}