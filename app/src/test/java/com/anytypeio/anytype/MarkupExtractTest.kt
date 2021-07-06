package com.anytypeio.anytype

import android.os.Build
import android.text.Spannable
import android.text.SpannableString
import com.anytypeio.anytype.core_models.Block.Content.Text.Mark
import com.anytypeio.anytype.core_ui.common.Span
import com.anytypeio.anytype.core_ui.common.toSpannable
import com.anytypeio.anytype.ext.extractMarks
import com.anytypeio.anytype.ext.isSpanInRange
import com.anytypeio.anytype.presentation.page.editor.Markup
import com.anytypeio.anytype.presentation.page.editor.ThemeColor
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub
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
        MockitoAnnotations.openMocks(this)
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

        val textColor = 11

        stubMarkup(source, mark)

        val editable = markup.toSpannable(textColor = textColor)

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

        val editable = markup.toSpannable(textColor = 11)

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

        val editable = markup.toSpannable(textColor = 11)

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

        val editable = markup.toSpannable(textColor = 11)

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

        val editable = markup.toSpannable(textColor = 11)

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

        val editable = markup.toSpannable(textColor = 11)

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

        val editable = markup.toSpannable(textColor = 11)

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

    @Test
    fun `span should be in range with INNER overlap`() {

        // SETUP

        val source = SpannableString("Everything was in confusion in the Oblonskys’ house")
        source.setSpan(Span.Bold(), 10, 20, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        // TESTING

        val textRange = IntRange(11, 19)
        val result = source.isSpanInRange(
            textRange = textRange,
            type = Span.Bold::class.java
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
        val result = source.isSpanInRange(
            textRange = textRange,
            type = Span.Bold::class.java
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
        val result = source.isSpanInRange(
            textRange = textRange,
            type = Span.Bold::class.java
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
        val result = source.isSpanInRange(
            textRange = textRange,
            type = Span.Bold::class.java
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
        val result = source.isSpanInRange(
            textRange = textRange,
            type = Span.Bold::class.java
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
        val result = source.isSpanInRange(
            textRange = textRange,
            type = Span.Bold::class.java
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
        val result = source.isSpanInRange(
            textRange = textRange,
            type = Span.Bold::class.java
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
        val result = source.isSpanInRange(
            textRange = textRange,
            type = Span.Bold::class.java
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
        val result = source.isSpanInRange(
            textRange = textRange,
            type = Span.Bold::class.java
        )

        assertFalse(result)
    }

    @Test
    fun `should return false when text is empty`() {

        // SETUP

        val source = SpannableString("")

        // TESTING

        val textRange = IntRange(0, 0)
        val result = source.isSpanInRange(
            textRange = textRange,
            type = Span.Bold::class.java
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
        val result = source.isSpanInRange(
            textRange = textRange,
            type = Span.Italic::class.java
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
        val result = source.isSpanInRange(
            textRange = textRange,
            type = Span.Italic::class.java
        )

        assertFalse(result)
    }
}