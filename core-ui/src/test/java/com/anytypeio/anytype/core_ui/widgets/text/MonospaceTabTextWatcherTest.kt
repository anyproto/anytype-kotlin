package com.anytypeio.anytype.core_ui.widgets.text

import android.os.Build
import android.text.SpannableStringBuilder
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.KArgumentCaptor
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.atLeast
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(sdk = [Build.VERSION_CODES.P])
@RunWith(RobolectricTestRunner::class)
class MonospaceTabTextWatcherTest {

    private val letterWidth = 1
    private val watcher = MonospaceTabTextWatcher(letterWidth.toFloat())

    @Test
    fun `should fill tab indent - when less than TAB_SIZE characters before`() {
        (0 until TAB_SIZE).forEach { n ->
            val text = "a".repeat(n) + "\t"
            val spannable = givenSpannable(text)
            val captured = captureSpannableChanges(spannable)
            val actualTabWidth = captured.firstValue.tabWidth
            val expectedTabWidth = TAB_SIZE - n * letterWidth
            assert(actualTabWidth == expectedTabWidth) {
                "Error with index $n for expected tab size ${TAB_SIZE - n * letterWidth} " +
                        "when actual tab size is ${captured.lastValue.tabWidth}"
            }
        }
    }

    @Test
    fun `should not fill tab indent - when TAB_SIZE characters before`() {
        val text = "a".repeat(TAB_SIZE) + "\t"
        val spannable = givenSpannable(text)
        val captured = captureSpannableChanges(spannable)
        assert(captured.firstValue.tabWidth == TAB_SIZE)
    }

    @Test
    fun `should not fill tab indent for second tab - after indented tab before`() {
        (0..TAB_SIZE).forEach { n ->
            val text = "a".repeat(n) + "\t\t"
            val spannable = givenSpannable(text)
            val captured = captureSpannableChanges(spannable)
            assert(captured.lastValue.tabWidth == TAB_SIZE)
        }
    }

    @Test
    fun `should fill second tab indent - when less than TAB_SIZE characters before`() {
        (0 until TAB_SIZE).forEach { n ->
            val text = "ab\t" + "a".repeat(n) + "\t"
            val spannable = givenSpannable(text)
            val captured = captureSpannableChanges(spannable)
            val actualTabWidth = captured.lastValue.tabWidth
            val expectedTabWidth = TAB_SIZE - n * letterWidth
            assert(actualTabWidth == expectedTabWidth) {
                "Error with index $n for expected tab size ${TAB_SIZE - n * letterWidth} " +
                        "when actual tab size is ${captured.lastValue.tabWidth}"
            }
        }
    }

    @Test
    fun `should not fill any indent - when no tabs`() {
        listOf("", " ", "\n", "\n\n", "\n \n").forEach { text ->
            val spannable = givenSpannable(text)
            val captured = captureSpannableChanges(spannable)

            assert(captured.allValues.isEmpty())
        }
    }

    @Test
    fun `should fill indent properly - when multiple lines`() {
        val text = "\nabc\t\nd\t\n"
        val spannable = givenSpannable(text)
        val captured = captureSpannableChanges(spannable)

        assert(captured.firstValue.tabWidth == 1)
        assert(captured.lastValue.tabWidth == 3)
    }

    @Test
    fun `should fill indent only in substring - when range provided`() {
        val text = "\nabc\t\nd\t\n\t"
        val start = text.indexOf('a')
        val end = text.indexOf('d')
        val spannable = givenSpannable(text, start, end - start + 1)
        val captured = captureSpannableChanges(spannable)

        assert(captured.allValues.size == 2)
        assert(captured.firstValue.tabWidth == 1)
        assert(captured.lastValue.tabWidth == 3)
    }

    @Test
    fun `should fill indent only in last tab - when range provided`() {
        val text = "\nabc\t\nd\t\n\t"
        val spannable = givenSpannable(text, text.length - 1, 1)
        val captured = captureSpannableChanges(spannable)

        assert(captured.allValues.size == 1)
        assert(captured.firstValue.tabWidth == 4)
    }

    @Test
    fun `should fill indent only in first tab - when range provided`() {
        val text = "\t\nabc\t\nd\t\n\t"
        val spannable = givenSpannable(text, 0, 1)
        val captured = captureSpannableChanges(spannable)

        assert(captured.allValues.size == 1)
        assert(captured.firstValue.tabWidth == 4)
    }

    private fun captureSpannableChanges(spannable: SpannableStringBuilder): KArgumentCaptor<CustomTabWidthSpan> {
        val captor = argumentCaptor<CustomTabWidthSpan>()
        verify(spannable, atLeast(0)).setSpan(captor.capture(), any(), any(), any())
        return captor
    }

    private fun givenSpannable(
        text: String,
        start: Int = 0,
        count: Int = text.length
    ): SpannableStringBuilder {
        val spannable = spy(SpannableStringBuilder(text))
        watcher.onTextChanged(spannable, start, 0, count)
        watcher.afterTextChanged(spannable)
        return spannable
    }
}

private const val TAB_SIZE = 4