package com.anytypeio.anytype.core_ui.common

import android.content.Context
import android.os.Build
import android.text.SpannableStringBuilder
import android.text.style.ClickableSpan
import android.view.View
import androidx.core.text.getSpans
import androidx.test.core.app.ApplicationProvider
import com.anytypeio.anytype.presentation.editor.editor.Markup
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@Config(sdk = [Build.VERSION_CODES.P])
@RunWith(RobolectricTestRunner::class)
class SetMarkupTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    /**
     * Simple test implementation of Markup interface
     */
    private class TestMarkup(
        override val body: String,
        override var marks: List<Markup.Mark>
    ) : Markup

    @Test
    fun `should replace text and apply bold span`() {
        // Given
        val editable = SpannableStringBuilder("old text")
        val markup = TestMarkup(
            body = "new text with bold",
            marks = listOf(
                Markup.Mark.Bold(from = 14, to = 18)  // "bold"
            )
        )

        // When
        editable.setMarkup(
            markup = markup,
            context = context,
            mentionCheckedIcon = null,
            mentionUncheckedIcon = null,
            mentionInitialsSize = 0f,
            textColor = 0xFF000000.toInt(),
            underlineHeight = 1f
        )

        // Then
        assertEquals("new text with bold", editable.toString())
        val boldSpans = editable.getSpans<Span.Bold>()
        assertEquals(1, boldSpans.size)
        assertEquals(14, editable.getSpanStart(boldSpans[0]))
        assertEquals(18, editable.getSpanEnd(boldSpans[0]))
    }

    @Test
    fun `should replace text and apply multiple span types`() {
        // Given
        val editable = SpannableStringBuilder("initial")
        val markup = TestMarkup(
            body = "bold italic strike",
            marks = listOf(
                Markup.Mark.Bold(from = 0, to = 4),           // "bold"
                Markup.Mark.Italic(from = 5, to = 11),        // "italic"
                Markup.Mark.Strikethrough(from = 12, to = 18) // "strike"
            )
        )

        // When
        editable.setMarkup(
            markup = markup,
            context = context,
            mentionCheckedIcon = null,
            mentionUncheckedIcon = null,
            mentionInitialsSize = 0f,
            textColor = 0xFF000000.toInt(),
            underlineHeight = 1f
        )

        // Then
        assertEquals("bold italic strike", editable.toString())
        assertEquals(1, editable.getSpans<Span.Bold>().size)
        assertEquals(1, editable.getSpans<Span.Italic>().size)
        assertEquals(1, editable.getSpans<Span.Strikethrough>().size)
    }

    @Test
    fun `should clear all existing spans including non-Span types`() {
        // Given - editable with existing ClickableSpan (which doesn't implement Span interface)
        val editable = SpannableStringBuilder("text with click")
        val existingClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {}
        }
        editable.setSpan(existingClickableSpan, 0, 4, Markup.DEFAULT_SPANNABLE_FLAG)

        // Verify existing span is there
        assertEquals(1, editable.getSpans<ClickableSpan>().size)

        val markup = TestMarkup(
            body = "new clean text",
            marks = emptyList()
        )

        // When
        editable.setMarkup(
            markup = markup,
            context = context,
            mentionCheckedIcon = null,
            mentionUncheckedIcon = null,
            mentionInitialsSize = 0f,
            textColor = 0xFF000000.toInt(),
            underlineHeight = 1f
        )

        // Then - ClickableSpan should be removed
        assertEquals("new clean text", editable.toString())
        assertEquals(0, editable.getSpans<ClickableSpan>().size)
    }

    @Test
    fun `should clear existing Span types and apply new ones`() {
        // Given - editable with existing Bold span
        val editable = SpannableStringBuilder("old bold text")
        editable.setSpan(Span.Bold(), 4, 8, Markup.DEFAULT_SPANNABLE_FLAG)

        assertEquals(1, editable.getSpans<Span.Bold>().size)

        val markup = TestMarkup(
            body = "new italic text",
            marks = listOf(
                Markup.Mark.Italic(from = 4, to = 10)  // "italic"
            )
        )

        // When
        editable.setMarkup(
            markup = markup,
            context = context,
            mentionCheckedIcon = null,
            mentionUncheckedIcon = null,
            mentionInitialsSize = 0f,
            textColor = 0xFF000000.toInt(),
            underlineHeight = 1f
        )

        // Then - Bold should be replaced with Italic
        assertEquals("new italic text", editable.toString())
        assertEquals(0, editable.getSpans<Span.Bold>().size)
        assertEquals(1, editable.getSpans<Span.Italic>().size)
    }

    @Test
    fun `should handle empty markup`() {
        // Given
        val editable = SpannableStringBuilder("some existing text")
        val markup = TestMarkup(
            body = "",
            marks = emptyList()
        )

        // When
        editable.setMarkup(
            markup = markup,
            context = context,
            mentionCheckedIcon = null,
            mentionUncheckedIcon = null,
            mentionInitialsSize = 0f,
            textColor = 0xFF000000.toInt(),
            underlineHeight = 1f
        )

        // Then
        assertEquals("", editable.toString())
    }

    @Test
    fun `should handle markup with same text but different spans`() {
        // Given
        val text = "hello world"
        val editable = SpannableStringBuilder(text)
        editable.setSpan(Span.Bold(), 0, 5, Markup.DEFAULT_SPANNABLE_FLAG)

        val markup = TestMarkup(
            body = text,
            marks = listOf(
                Markup.Mark.Italic(from = 6, to = 11)  // "world" - italic instead of bold on "hello"
            )
        )

        // When
        editable.setMarkup(
            markup = markup,
            context = context,
            mentionCheckedIcon = null,
            mentionUncheckedIcon = null,
            mentionInitialsSize = 0f,
            textColor = 0xFF000000.toInt(),
            underlineHeight = 1f
        )

        // Then
        assertEquals(text, editable.toString())
        assertEquals(0, editable.getSpans<Span.Bold>().size)
        assertEquals(1, editable.getSpans<Span.Italic>().size)
    }

    @Test
    fun `should apply underline span`() {
        // Given
        val editable = SpannableStringBuilder("")
        val markup = TestMarkup(
            body = "underlined text",
            marks = listOf(
                Markup.Mark.Underline(from = 0, to = 10)  // "underlined"
            )
        )

        // When
        editable.setMarkup(
            markup = markup,
            context = context,
            mentionCheckedIcon = null,
            mentionUncheckedIcon = null,
            mentionInitialsSize = 0f,
            textColor = 0xFF000000.toInt(),
            underlineHeight = 2f
        )

        // Then
        assertEquals("underlined text", editable.toString())
        val underlineSpans = editable.getSpans<Underline>()
        assertEquals(1, underlineSpans.size)
    }

    @Test
    fun `should apply keyboard span`() {
        // Given
        val editable = SpannableStringBuilder("")
        val markup = TestMarkup(
            body = "code here",
            marks = listOf(
                Markup.Mark.Keyboard(from = 0, to = 4)  // "code"
            )
        )

        // When
        editable.setMarkup(
            markup = markup,
            context = context,
            mentionCheckedIcon = null,
            mentionUncheckedIcon = null,
            mentionInitialsSize = 0f,
            textColor = 0xFF000000.toInt(),
            underlineHeight = 1f
        )

        // Then
        assertEquals("code here", editable.toString())
        val fontSpans = editable.getSpans<Span.Font>()
        val keyboardSpans = editable.getSpans<Span.Keyboard>()
        assertEquals(1, fontSpans.size)
        assertEquals(1, keyboardSpans.size)
    }

    @Test
    fun `should handle many spans efficiently`() {
        // Given - create markup with many spans to verify no performance issues
        val text = "a".repeat(1000)
        val marks = (0 until 100).map { i ->
            Markup.Mark.Bold(from = i * 10, to = i * 10 + 5)
        }
        val markup = TestMarkup(body = text, marks = marks)
        val editable = SpannableStringBuilder("old")

        // When - this should complete quickly without ANR
        val startTime = System.currentTimeMillis()
        editable.setMarkup(
            markup = markup,
            context = context,
            mentionCheckedIcon = null,
            mentionUncheckedIcon = null,
            mentionInitialsSize = 0f,
            textColor = 0xFF000000.toInt(),
            underlineHeight = 1f
        )
        val duration = System.currentTimeMillis() - startTime

        // Then
        assertEquals(text, editable.toString())
        assertEquals(100, editable.getSpans<Span.Bold>().size)
        // Should complete in reasonable time (less than 1 second for 100 spans)
        assertTrue(duration < 1000, "setMarkup took too long: ${duration}ms")
    }

    @Test
    fun `should apply text color span`() {
        // Given
        val editable = SpannableStringBuilder("")
        val markup = TestMarkup(
            body = "colored text",
            marks = listOf(
                Markup.Mark.TextColor(from = 0, to = 7, color = "red")  // "colored"
            )
        )

        // When
        editable.setMarkup(
            markup = markup,
            context = context,
            mentionCheckedIcon = null,
            mentionUncheckedIcon = null,
            mentionInitialsSize = 0f,
            textColor = 0xFF000000.toInt(),
            underlineHeight = 1f
        )

        // Then
        assertEquals("colored text", editable.toString())
        val colorSpans = editable.getSpans<Span.TextColor>()
        assertEquals(1, colorSpans.size)
    }

    @Test
    fun `should apply highlight span`() {
        // Given
        val editable = SpannableStringBuilder("")
        val markup = TestMarkup(
            body = "highlighted text",
            marks = listOf(
                Markup.Mark.BackgroundColor(from = 0, to = 11, background = "yellow")  // "highlighted"
            )
        )

        // When
        editable.setMarkup(
            markup = markup,
            context = context,
            mentionCheckedIcon = null,
            mentionUncheckedIcon = null,
            mentionInitialsSize = 0f,
            textColor = 0xFF000000.toInt(),
            underlineHeight = 1f
        )

        // Then
        assertEquals("highlighted text", editable.toString())
        val highlightSpans = editable.getSpans<Span.Highlight>()
        assertEquals(1, highlightSpans.size)
    }

    @Test
    fun `should apply link span`() {
        // Given
        val editable = SpannableStringBuilder("")
        val markup = TestMarkup(
            body = "click here",
            marks = listOf(
                Markup.Mark.Link(from = 0, to = 5, param = "https://example.com")  // "click"
            )
        )

        // When
        editable.setMarkup(
            markup = markup,
            context = context,
            mentionCheckedIcon = null,
            mentionUncheckedIcon = null,
            mentionInitialsSize = 0f,
            textColor = 0xFF000000.toInt(),
            underlineHeight = 1f
        )

        // Then
        assertEquals("click here", editable.toString())
        val urlSpans = editable.getSpans<Span.Url>()
        assertEquals(1, urlSpans.size)
        assertEquals("https://example.com", urlSpans[0].url)
    }
}
