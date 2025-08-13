package com.anytypeio.anytype.core_ui.features.editor

import android.text.SpannableStringBuilder
import com.anytypeio.anytype.core_ui.common.Span
import com.anytypeio.anytype.core_ui.common.Underline
import com.anytypeio.anytype.presentation.editor.editor.Markup
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [28])
class MarkupMapperTest {

    @Test
    fun `should extract emoji marks from Editable`() {
        // Given
        val text = "Hello  world!"
        val editable = SpannableStringBuilder(text)
        
        // Apply emoji spans at positions where spaces are (positions 5 and 6)
        editable.setSpan(
            Span.Emoji("😀"),
            5,
            6,
            android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        
        editable.setSpan(
            Span.Emoji("🌍"),
            6,
            7,
            android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        
        // When
        val marks = editable.marks()
        
        // Then
        assertEquals(2, marks.size)
        
        val emojiMarks = marks.filterIsInstance<Markup.Mark.Emoji>()
        assertEquals(2, emojiMarks.size)
        
        assertEquals(5, emojiMarks[0].from)
        assertEquals(6, emojiMarks[0].to)
        assertEquals("😀", emojiMarks[0].param)
        
        assertEquals(6, emojiMarks[1].from)
        assertEquals(7, emojiMarks[1].to)
        assertEquals("🌍", emojiMarks[1].param)
    }

    @Test
    fun `should extract mixed markup including emojis`() {
        // Given
        val text = "Bold  italic"
        val editable = SpannableStringBuilder(text)
        
        // Apply bold span
        editable.setSpan(
            Span.Bold(),
            0,
            4,
            android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        
        // Apply emoji span
        editable.setSpan(
            Span.Emoji("🎉"),
            4,
            5,
            android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        
        // Apply italic span
        editable.setSpan(
            Span.Italic(),
            7,
            12,
            android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        
        // When
        val marks = editable.marks()
        
        // Then
        assertEquals(3, marks.size)
        
        val boldMarks = marks.filterIsInstance<Markup.Mark.Bold>()
        assertEquals(1, boldMarks.size)
        assertEquals(0, boldMarks[0].from)
        assertEquals(4, boldMarks[0].to)
        
        val emojiMarks = marks.filterIsInstance<Markup.Mark.Emoji>()
        assertEquals(1, emojiMarks.size)
        assertEquals(4, emojiMarks[0].from)
        assertEquals(5, emojiMarks[0].to)
        assertEquals("🎉", emojiMarks[0].param)
        
        val italicMarks = marks.filterIsInstance<Markup.Mark.Italic>()
        assertEquals(1, italicMarks.size)
        assertEquals(7, italicMarks[0].from)
        assertEquals(12, italicMarks[0].to)
    }

    @Test
    fun `should extract all span types correctly`() {
        // Given
        val text = "Sample text with various marks"
        val editable = SpannableStringBuilder(text)
        
        // Apply various spans
        editable.setSpan(Span.Bold(), 0, 6, android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        editable.setSpan(Span.Italic(), 7, 11, android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        editable.setSpan(Span.Strikethrough(), 12, 16, android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        editable.setSpan(Span.TextColor(0xFF0000, "red"), 17, 21, android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        editable.setSpan(Underline(2.0f), 22, 27, android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        editable.setSpan(Span.Emoji("🚀"), 28, 29, android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        
        // When
        val marks = editable.marks()
        
        // Then
        assertEquals(6, marks.size)
        
        assertTrue(marks.any { it is Markup.Mark.Bold && it.from == 0 && it.to == 6 })
        assertTrue(marks.any { it is Markup.Mark.Italic && it.from == 7 && it.to == 11 })
        assertTrue(marks.any { it is Markup.Mark.Strikethrough && it.from == 12 && it.to == 16 })
        assertTrue(marks.any { it is Markup.Mark.TextColor && it.from == 17 && it.to == 21 && it.color == "red" })
        assertTrue(marks.any { it is Markup.Mark.Underline && it.from == 22 && it.to == 27 })
        
        val emojiMark = marks.filterIsInstance<Markup.Mark.Emoji>().firstOrNull()
        assertTrue(emojiMark != null && emojiMark.from == 28 && emojiMark.to == 29 && emojiMark.param == "🚀")
    }

    @Test
    fun `should handle empty text with no spans`() {
        // Given
        val editable = SpannableStringBuilder("")
        
        // When
        val marks = editable.marks()
        
        // Then
        assertEquals(0, marks.size)
    }

    @Test
    fun `should handle text with no spans`() {
        // Given
        val text = "Plain text without any markup"
        val editable = SpannableStringBuilder(text)
        
        // When
        val marks = editable.marks()
        
        // Then
        assertEquals(0, marks.size)
    }

    @Test
    fun `should extract complex emoji with multiple unicode codepoints`() {
        // Given
        val text = "Test  end"
        val editable = SpannableStringBuilder(text)
        
        // Apply emoji span with complex emoji
        editable.setSpan(
            Span.Emoji("👨‍👩‍👧‍👦"), // Family emoji with multiple codepoints
            4,
            5,
            android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        
        // When
        val marks = editable.marks()
        
        // Then
        assertEquals(1, marks.size)
        
        val emojiMark = marks.first() as Markup.Mark.Emoji
        assertEquals(4, emojiMark.from)
        assertEquals(5, emojiMark.to)
        assertEquals("👨‍👩‍👧‍👦", emojiMark.param)
    }
}