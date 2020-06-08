package com.agileburo.anytype

import android.os.Build
import android.text.Spannable
import android.text.SpannableString
import com.agileburo.anytype.core_ui.common.Markup
import com.agileburo.anytype.core_ui.common.Span
import com.agileburo.anytype.core_ui.common.ThemeColor
import com.agileburo.anytype.core_ui.common.toSpannable
import com.agileburo.anytype.domain.block.model.Block.Content.Text.Mark
import com.agileburo.anytype.ext.extractMarks
import com.agileburo.anytype.ext.isSpanInRange
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.stub
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@Config(sdk = [Build.VERSION_CODES.P])
@RunWith(RobolectricTestRunner::class)
class MarkupExtractTest {

    @Mock
    lateinit var markup : Markup

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun `should extract italic span`() {

        // SETUP

        val source = "Happy families are all alike; every unhappy family is unhappy in its own way"

        val mark = Markup.Mark(
            from = 0,
            to = 5,
            param = null,
            type = Markup.Type.ITALIC
        )

        stubMarkup(source, mark)

        val editable = markup.toSpannable()

        // TESTING

        val marks = editable.extractMarks()

        assertEquals(expected = 1, actual = marks.size)
        assertEquals(
            expected = Mark(
                range = mark.from..mark.to,
                param = null,
                type = Mark.Type.ITALIC
            ),
            actual = marks.first()
        )
    }

    @Test
    fun `should extract bold span`() {

        // SETUP

        val source = "Everything was in confusion in the Oblonskys’ house"

        val mark = Markup.Mark(
            from = 0,
            to = 5,
            param = null,
            type = Markup.Type.BOLD
        )

        stubMarkup(source, mark)

        val editable = markup.toSpannable()

        // TESTING

        val marks = editable.extractMarks()

        assertEquals(expected = 1, actual = marks.size)
        assertEquals(
            expected = Mark(
                range = mark.from..mark.to,
                param = null,
                type = Mark.Type.BOLD
            ),
            actual = marks.first()
        )
    }

    @Test
    fun `should extract strikethrough span`() {

        // SETUP

        val source = "Everything was in confusion in the Oblonskys’ house"

        val mark = Markup.Mark(
            from = 0,
            to = 5,
            param = null,
            type = Markup.Type.STRIKETHROUGH
        )

        stubMarkup(source, mark)

        val editable = markup.toSpannable()

        // TESTING

        val marks = editable.extractMarks()

        assertEquals(expected = 1, actual = marks.size)
        assertEquals(
            expected = Mark(
                range = mark.from..mark.to,
                param = null,
                type = Mark.Type.STRIKETHROUGH
            ),
            actual = marks.first()
        )
    }

    @Test
    fun `should extract keyboard span`() {

        // SETUP

        val source = "Everything was in confusion in the Oblonskys’ house"

        val mark = Markup.Mark(
            from = 0,
            to = 5,
            param = null,
            type = Markup.Type.KEYBOARD
        )

        stubMarkup(source, mark)

        val editable = markup.toSpannable()

        // TESTING

        val marks = editable.extractMarks()

        assertEquals(expected = 1, actual = marks.size)
        assertEquals(
            expected = Mark(
                range = mark.from..mark.to,
                param = null,
                type = Mark.Type.KEYBOARD
            ),
            actual = marks.first()
        )
    }

    @Test
    fun `should extract text color span`() {

        // SETUP

        val source = "Everything was in confusion in the Oblonskys’ house"

        val mark = Markup.Mark(
            from = 0,
            to = 5,
            param = ThemeColor.BLUE.title,
            type = Markup.Type.TEXT_COLOR
        )

        stubMarkup(source, mark)

        val editable = markup.toSpannable()

        // TESTING

        val marks = editable.extractMarks()

        assertEquals(expected = 1, actual = marks.size)
        assertEquals(
            expected = Mark(
                range = mark.from..mark.to,
                param = ThemeColor.BLUE.title,
                type = Mark.Type.TEXT_COLOR
            ),
            actual = marks.first()
        )
    }

    @Test
    fun `should extract background color span`() {

        // SETUP

        val source = "Everything was in confusion in the Oblonskys’ house"

        val mark = Markup.Mark(
            from = 0,
            to = 5,
            param = ThemeColor.BLUE.title,
            type = Markup.Type.BACKGROUND_COLOR
        )

        stubMarkup(source, mark)

        val editable = markup.toSpannable()

        // TESTING

        val marks = editable.extractMarks()

        assertEquals(expected = 1, actual = marks.size)
        assertEquals(
            expected = Mark(
                range = mark.from..mark.to,
                param = ThemeColor.BLUE.title,
                type = Mark.Type.BACKGROUND_COLOR
            ),
            actual = marks.first()
        )
    }

    @Test
    fun `should extract url span`() {

        // SETUP

        val source = "Everything was in confusion in the Oblonskys’ house"

        val mark = Markup.Mark(
            from = 0,
            to = 5,
            param = DataFactory.randomString(),
            type = Markup.Type.LINK
        )

        stubMarkup(source, mark)

        val editable = markup.toSpannable()

        // TESTING

        val marks = editable.extractMarks()

        assertEquals(expected = 1, actual = marks.size)
        assertEquals(
            expected = Mark(
                range = mark.from..mark.to,
                param = mark.param,
                type = Mark.Type.LINK
            ),
            actual = marks.first()
        )
    }

    private fun stubMarkup(
        source: String,
        mark: Markup.Mark
    ) {
        markup.stub {
            on { body } doReturn source
            on { marks } doReturn listOf(mark)
        }
    }

    private fun stubMarkup(
        source: String,
        markss: List<Markup.Mark>
    ) {
        markup.stub {
            on { body } doReturn source
            on { marks } doReturn markss
        }
    }

//    @Test
//    fun `should find 2 Bold Spans`() {
//
//        // SETUP
//
//        val source = "Everything was in confusion in the Oblonskys’ house"
//
//        val mark = Markup.Mark(
//            from = 0,
//            to = 4,
//            param = null,
//            type = Markup.Type.BOLD
//        )
//
//        val mark2 = Markup.Mark(
//            from = 7,
//            to = 10,
//            param = null,
//            type = Markup.Type.BOLD
//        )
//
//        stubMarkup(source, listOf(mark, mark2))
//
//        val editable = markup.toSpannable()
//
//        // TESTING
//
//        val result = editable.extractSpans(IntRange(0, 16), Span.Bold::class.java)
//
//        assertEquals(expected = 2, actual = result.size)
//
//    }
//
//    @Test
//    fun `should find no spans when selected at the end`() {
//
//        // SETUP
//
//        val source = "Everything was in confusion in the Oblonskys’ house"
//
//        val mark1 = Markup.Mark(
//            from = 0,
//            to = 4,
//            param = null,
//            type = Markup.Type.BOLD
//        )
//
//        val mark2 = Markup.Mark(
//            from = 7,
//            to = 10,
//            param = null,
//            type = Markup.Type.BOLD
//        )
//
//        val mark3 = Markup.Mark(
//            from = 10,
//            to = 18,
//            param = null,
//            type = Markup.Type.ITALIC
//        )
//
//        stubMarkup(source, listOf(mark1, mark2, mark3))
//
//        val editable = markup.toSpannable()
//
//        // TESTING
//
//        val result = editable.extractSpans(IntRange(18, source.length), Span.Bold::class.java)
//
//        assertEquals(expected = 0, actual = result.size)
//
//    }
//
//    @Test
//    fun `should find no spans when selected at the start`() {
//
//        // SETUP
//
//        val source = "Everything was in confusion in the Oblonskys’ house"
//
//        val mark1 = Markup.Mark(
//            from = 10,
//            to = 34,
//            param = null,
//            type = Markup.Type.BOLD
//        )
//
//        val mark2 = Markup.Mark(
//            from = 7,
//            to = 10,
//            param = null,
//            type = Markup.Type.BOLD
//        )
//
//        val mark3 = Markup.Mark(
//            from = 10,
//            to = 18,
//            param = null,
//            type = Markup.Type.ITALIC
//        )
//
//        stubMarkup(source, listOf(mark1, mark2, mark3))
//
//        val editable = markup.toSpannable()
//
//        // TESTING
//
//        val result = editable.extractSpans(IntRange(0, 7), Span.Bold::class.java)
//
//        assertEquals(expected = 0, actual = result.size)
//
//    }
//
//    @Test
//    fun `should get bold span with proper start, end`() {
//
//        // SETUP
//
//        val source = "Everything was in confusion in the Oblonskys’ house"
//
//        val mark = Markup.Mark(
//            from = 0,
//            to = 4,
//            param = null,
//            type = Markup.Type.BOLD
//        )
//
//        stubMarkup(source, listOf(mark))
//
//        val editable = markup.toSpannable()
//
//        // TESTING
//
//        val intRange = IntRange(2, source.length)
//        val result = editable.extractSpans(intRange, Span.Bold::class.java)
//
//        val boldSpanStart = editable.getSpanStart(result[0])
//        val boldSpanEnd = editable.getSpanEnd(result[0])
//
//        assertEquals(expected = 0, actual = boldSpanStart)
//        assertEquals(expected = 4, actual = boldSpanEnd)
//    }
//
//    @Test
//    fun `should be inner overlap`() {
//
//        // SETUP
//
//        val source = "Everything was in confusion in the Oblonskys’ house"
//
//        val mark = Markup.Mark(
//            from = 19,
//            to = 29,
//            param = null,
//            type = Markup.Type.BOLD
//        )
//
//        stubMarkup(source, listOf(mark))
//
//        val editable = markup.toSpannable()
//
//        // TESTING
//
//        val textSelection = IntRange(20, 25)
//        val spans = editable.getSpans(textSelection, Span.Bold::class.java)
//
//        val boldSpan = spans[0]
//        val boldStart = editable.getSpanStart(boldSpan)
//        val boldEnd = editable.getSpanEnd(boldSpan)
//
//        val result = textSelection.overlap(IntRange(boldStart, boldEnd))
//
//        assertEquals(expected = Overlap.INNER, actual = result)
//
//    }

    @Test
    fun `span should be in range with INNER overlap`() {

        // SETUP

        val source = SpannableString("Everything was in confusion in the Oblonskys’ house")
        source.setSpan(Span.Bold(), 10, 20, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        // TESTING

        val textRange = IntRange(11, 19)
        val result = isSpanInRange(
            textRange = textRange,
            type = Span.Bold::class.java,
            text = source
        )

        assertTrue(result)
    }

    @Test
    fun `span should be in range with INNER_LEFT overlap`() {

        // SETUP

        val source = SpannableString("Everything was in confusion in the Oblonskys’ house")
        source.setSpan(Span.Bold(), 10, 20, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        // TESTING

        val textRange = IntRange(10, 15)
        val result = isSpanInRange(
            textRange = textRange,
            type = Span.Bold::class.java,
            text = source
        )

        assertTrue(result)
    }

    @Test
    fun `span should be in range with INNER_RIGHT overlap`() {

        // SETUP

        val source = SpannableString("Everything was in confusion in the Oblonskys’ house")
        source.setSpan(Span.Bold(), 10, 20, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        // TESTING

        val textRange = IntRange(15, 20)
        val result = isSpanInRange(
            textRange = textRange,
            type = Span.Bold::class.java,
            text = source
        )

        assertTrue(result)
    }

    @Test
    fun `span should be in range with EQUAL overlap`() {

        // SETUP

        val source = SpannableString("Everything was in confusion in the Oblonskys’ house")
        source.setSpan(Span.Bold(), 10, 20, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        // TESTING

        val textRange = IntRange(10, 20)
        val result = isSpanInRange(
            textRange = textRange,
            type = Span.Bold::class.java,
            text = source
        )

        assertTrue(result)
    }

    @Test
    fun `span should not be in range with OUTER overlap`() {

        // SETUP

        val source = SpannableString("Everything was in confusion in the Oblonskys’ house")
        source.setSpan(Span.Bold(), 10, 20, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        // TESTING

        val textRange = IntRange(10, 21)
        val result = isSpanInRange(
            textRange = textRange,
            type = Span.Bold::class.java,
            text = source
        )

        assertFalse(result)
    }

    @Test
    fun `span should not be in range with LEFT overlap`() {

        // SETUP

        val source = SpannableString("Everything was in confusion in the Oblonskys’ house")
        source.setSpan(Span.Bold(), 10, 20, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        // TESTING

        val textRange = IntRange(9, 13)
        val result = isSpanInRange(
            textRange = textRange,
            type = Span.Bold::class.java,
            text = source
        )

        assertFalse(result)
    }

    @Test
    fun `span should not be in range with RIGHT overlap`() {

        // SETUP

        val source = SpannableString("Everything was in confusion in the Oblonskys’ house")
        source.setSpan(Span.Bold(), 10, 20, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        // TESTING

        val textRange = IntRange(14, 21)
        val result = isSpanInRange(
            textRange = textRange,
            type = Span.Bold::class.java,
            text = source
        )

        assertFalse(result)
    }

    @Test
    fun `span should not be in range with BEFORE overlap`() {

        // SETUP

        val source = SpannableString("Everything was in confusion in the Oblonskys’ house")
        source.setSpan(Span.Bold(), 10, 20, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        // TESTING

        val textRange = IntRange(1, 9)
        val result = isSpanInRange(
            textRange = textRange,
            type = Span.Bold::class.java,
            text = source
        )

        assertFalse(result)
    }

    @Test
    fun `span should not be in range with AFTER overlap`() {

        // SETUP

        val source = SpannableString("Everything was in confusion in the Oblonskys’ house")
        source.setSpan(Span.Bold(), 10, 20, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        // TESTING

        val textRange = IntRange(21, 30)
        val result = isSpanInRange(
            textRange = textRange,
            type = Span.Bold::class.java,
            text = source
        )

        assertFalse(result)
    }

    @Test
    fun `should return false when text is empty`() {

        // SETUP

        val source = SpannableString("")

        // TESTING

        val textRange = IntRange(0, 0)
        val result = isSpanInRange(
            textRange = textRange,
            type = Span.Bold::class.java,
            text = source
        )

        assertFalse(result)
    }

    @Test
    fun `should return false when no matching spans`() {

        // SETUP

        val source = SpannableString("Everything was in confusion in the Oblonskys’ house")
        source.setSpan(Span.Bold(), 10, 20, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        // TESTING

        val textRange = IntRange(12, 18)
        val result = isSpanInRange(
            textRange = textRange,
            type = Span.Italic::class.java,
            text = source
        )

        assertFalse(result)
    }

    @Test
    fun `should return false when text range is wrong`() {

        // SETUP

        val source = SpannableString("Everything was in confusion in the Oblonskys’ house")
        source.setSpan(Span.Bold(), 10, 20, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        // TESTING

        val textRange = IntRange(12, 180)
        val result = isSpanInRange(
            textRange = textRange,
            type = Span.Italic::class.java,
            text = source
        )

        assertFalse(result)
    }
}