package com.agileburo.anytype

import android.graphics.Color
import android.graphics.Typeface
import android.text.Annotation
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.style.*
import com.agileburo.anytype.core_utils.ext.KEY_ROUNDED
import com.agileburo.anytype.core_utils.ext.VALUE_ROUNDED
import com.agileburo.anytype.core_utils.ext.hasSpan
import com.agileburo.anytype.core_utils.ext.removeRoundedSpans
import junit.framework.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner::class)
class SpanExtensionsTest {

    @Test
    fun `should find spans`() {
        val text = "Testing Spans"
        val spannable = SpannableString(text)
        spannable.setSpan(
            URLSpan("https://anytype.io/"),
            0,
            text.lastIndex,
            Spannable.SPAN_EXCLUSIVE_INCLUSIVE
        )
        spannable.setSpan(
            BackgroundColorSpan(Color.RED),
            0,
            6,
            Spannable.SPAN_EXCLUSIVE_INCLUSIVE
        )

        assertTrue(hasSpan(spannable, URLSpan::class.java))
        assertTrue(hasSpan(spannable, BackgroundColorSpan::class.java))
        assertFalse(hasSpan(spannable, UnderlineSpan::class.java))
    }

    @Test
    fun `should not find spans`() {
        val text = "Testing Spans"
        val spannable = SpannableString(text)

        assertFalse(hasSpan(spannable, URLSpan::class.java))
    }

    @Test
    fun `should remove only rounded spans`() {
        val editable = Editable.Factory.getInstance().newEditable("should remove rounded spans")
        editable.setSpan(
            Annotation(KEY_ROUNDED, VALUE_ROUNDED),
            0,
            6,
            Spannable.SPAN_EXCLUSIVE_INCLUSIVE
        )
        editable.setSpan(
            Annotation("Other", "Span"),
            8,
            12,
            Spannable.SPAN_EXCLUSIVE_INCLUSIVE
        )
        editable.setSpan(
            Annotation(KEY_ROUNDED, VALUE_ROUNDED),
            14,
            21,
            Spannable.SPAN_EXCLUSIVE_INCLUSIVE
        )
        editable.setSpan(StrikethroughSpan(), 23, 26, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
        editable.setSpan(StyleSpan(Typeface.ITALIC), 0, 10, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)

        val resultAnnotationSpans =
            editable.removeRoundedSpans().getSpans(0, editable.length, Annotation::class.java)
        val resultOtherSpans =
            editable.removeRoundedSpans().getSpans(0, editable.length, CharacterStyle::class.java)

        assertEquals(1, resultAnnotationSpans.count())
        assertEquals(2, resultOtherSpans.count())
    }
}