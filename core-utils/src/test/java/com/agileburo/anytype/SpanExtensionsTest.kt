package com.agileburo.anytype

import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.BackgroundColorSpan
import android.text.style.URLSpan
import android.text.style.UnderlineSpan
import com.agileburo.anytype.core_utils.ext.hasSpan
import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertTrue
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
}