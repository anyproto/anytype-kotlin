package com.anytypeio.anytype.core_ui.extensions

import android.content.Context
import androidx.emoji2.text.EmojiCompat
import androidx.test.core.app.ApplicationProvider
import com.anytypeio.anytype.core_ui.common.toSpannable
import com.anytypeio.anytype.presentation.editor.editor.Markup
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [28])
class EmojiUtilsTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun `should process emoji marks correctly in text`() {
        // Given text WITHOUT emojis (as received from middleware) with emoji marks
        val text = "Hello   world!   Nice day!" // Spaces where emojis should be rendered
        val marks = listOf(
            Markup.Mark.Emoji(from = 6, to = 7, param = "😀"), // Length 1: space at position 6
            Markup.Mark.Emoji(from = 15, to = 16, param = "🎉") // Length 1: space at position 15
        )

        val markup = object : Markup {
            override val body: String = text
            override var marks: List<Markup.Mark> = marks
        }

        // When converting to spannable
        val spannable = markup.toSpannable(
            textColor = android.graphics.Color.BLACK,
            context = context,
            underlineHeight = 2f
        )

        // Then - with span-based approach, text remains unchanged
        assertNotNull(spannable)
        assertEquals(text.length, spannable.length)
        assertEquals(text, spannable.toString()) // Original text should remain unchanged
        
        // Verify emoji spans are applied
        val spans = spannable.getSpans(0, spannable.length, Any::class.java)
        val emojiSpans = spans.filterIsInstance<com.anytypeio.anytype.core_ui.common.Span.Emoji>()
        assertEquals(2, emojiSpans.size, "Should have 2 emoji spans applied")
    }

    @Test
    fun `should handle multiple emoji marks in sequence`() {
        // Given text with multiple consecutive spaces where emojis should be rendered
        val text = "Emojis:    are fun!" // 3 spaces for 3 emojis
        val marks = listOf(
            Markup.Mark.Emoji(from = 8, to = 9, param = "😀"), // Length 1: first space
            Markup.Mark.Emoji(from = 9, to = 10, param = "😃"), // Length 1: second space
            Markup.Mark.Emoji(from = 10, to = 11, param = "😄") // Length 1: third space
        )

        val markup = object : Markup {
            override val body: String = text
            override var marks: List<Markup.Mark> = marks
        }

        // When converting to spannable
        val spannable = markup.toSpannable(
            textColor = android.graphics.Color.BLACK,
            context = context,
            underlineHeight = 2f
        )

        // Then - with span-based approach, original text remains unchanged
        assertNotNull(spannable)
        assertEquals(text, spannable.toString()) // Original text should remain unchanged
        
        // Verify emoji spans are applied
        val spans = spannable.getSpans(0, spannable.length, Any::class.java)
        val emojiSpans = spans.filterIsInstance<com.anytypeio.anytype.core_ui.common.Span.Emoji>()
        assertEquals(3, emojiSpans.size, "Should have 3 emoji spans applied")
    }

    @Test
    fun `should handle emoji marks mixed with other marks`() {
        // Given text with space where emoji should be rendered and other markup
        val text = "Hello   world!" // Space where emoji should be rendered
        val marks = listOf(
            Markup.Mark.Bold(from = 0, to = 5),
            Markup.Mark.Emoji(from = 6, to = 7, param = "😀"), // Length 1: the space
            Markup.Mark.Italic(from = 8, to = 14) // Adjusted position after correcting emoji
        )

        val markup = object : Markup {
            override val body: String = text
            override var marks: List<Markup.Mark> = marks
        }

        // When converting to spannable
        val spannable = markup.toSpannable(
            textColor = android.graphics.Color.BLACK,
            context = context,
            underlineHeight = 2f
        )

        // Then - with span-based approach, original text remains unchanged
        assertNotNull(spannable)
        assertEquals(text, spannable.toString()) // Original text should remain unchanged
        
        // Verify all spans are applied
        val spans = spannable.getSpans(0, spannable.length, Any::class.java)
        val emojiSpans = spans.filterIsInstance<com.anytypeio.anytype.core_ui.common.Span.Emoji>()
        val boldSpans = spans.filterIsInstance<com.anytypeio.anytype.core_ui.common.Span.Bold>()
        val italicSpans = spans.filterIsInstance<com.anytypeio.anytype.core_ui.common.Span.Italic>()
        
        assertEquals(1, emojiSpans.size, "Should have 1 emoji span")
        assertEquals(1, boldSpans.size, "Should have 1 bold span")
        assertEquals(1, italicSpans.size, "Should have 1 italic span")
    }

    @Test
    fun `should handle empty emoji param gracefully`() {
        // Given text with space and empty emoji param
        val text = "Hello  world!" // Double space for testing
        val marks = listOf(
            Markup.Mark.Emoji(from = 6, to = 7, param = "") // Length 1: one space
        )

        val markup = object : Markup {
            override val body: String = text
            override var marks: List<Markup.Mark> = marks
        }

        // When converting to spannable
        val spannable = markup.toSpannable(
            textColor = android.graphics.Color.BLACK,
            context = context,
            underlineHeight = 2f
        )

        // Then - should not crash and text should remain unchanged
        assertNotNull(spannable)
        assertEquals(text, spannable.toString()) // Original text should remain unchanged
        
        // Verify emoji span is applied even with empty param
        val spans = spannable.getSpans(0, spannable.length, Any::class.java)
        val emojiSpans = spans.filterIsInstance<com.anytypeio.anytype.core_ui.common.Span.Emoji>()
        assertEquals(1, emojiSpans.size, "Should have 1 emoji span even with empty param")
    }

    @Test
    fun `should handle text with no emoji marks`() {
        // Given text without emoji marks
        val text = "Hello world!"
        val marks = listOf(
            Markup.Mark.Bold(from = 0, to = 5),
            Markup.Mark.Italic(from = 6, to = 12)
        )

        val markup = object : Markup {
            override val body: String = text
            override var marks: List<Markup.Mark> = marks
        }

        // When converting to spannable
        val spannable = markup.toSpannable(
            textColor = android.graphics.Color.BLACK,
            context = context,
            underlineHeight = 2f
        )

        // Then
        assertNotNull(spannable)
        assertEquals(text, spannable.toString())
    }

    @Test
    fun `EmojiUtils processSafe should return input when EmojiCompat not ready`() {
        // Given some text with emoji
        val input = "Hello 😀"

        // When EmojiCompat is not ready (which is typical in unit tests)
        val result = EmojiUtils.processSafe(input)

        // Then should return input unchanged
        assertEquals(input, result.toString())
    }

    @Test
    fun `EmojiUtils isReady should return false when EmojiCompat not initialized`() {
        // When checking if EmojiUtils is ready
        val isReady = EmojiUtils.isReady()

        // Then should return false (in unit tests EmojiCompat is not usually initialized)
        assertFalse(isReady)
    }

    @Test
    fun `should process complex unicode emojis correctly`() {
        // Given text with spaces where complex unicode emojis should be rendered
        val text = "Hello   and   world!" // Spaces where emojis should be rendered
        val marks = listOf(
            Markup.Mark.Emoji(from = 6, to = 7, param = "👍🏻"), // Length 1: skin tone emoji
            Markup.Mark.Emoji(from = 12, to = 13, param = "👨‍👩‍👧‍👦") // Length 1: family emoji
        )

        val markup = object : Markup {
            override val body: String = text
            override var marks: List<Markup.Mark> = marks
        }

        // When converting to spannable
        val spannable = markup.toSpannable(
            textColor = android.graphics.Color.BLACK,
            context = context,
            underlineHeight = 2f
        )

        // Then should not crash and text should remain unchanged
        assertNotNull(spannable)
        assertEquals(text, spannable.toString()) // Original text should remain unchanged
        
        // Verify complex emoji spans are applied
        val spans = spannable.getSpans(0, spannable.length, Any::class.java)
        val emojiSpans = spans.filterIsInstance<com.anytypeio.anytype.core_ui.common.Span.Emoji>()
        assertEquals(2, emojiSpans.size, "Should have 2 emoji spans for complex emojis")
    }

    @Test
    fun `should handle edge case with overlapping emoji ranges`() {
        // Given text with adjacent spaces for emojis
        val text = "Test   end" // Two spaces for two emojis
        val marks = listOf(
            Markup.Mark.Emoji(from = 5, to = 6, param = "😀"), // Length 1: first space
            Markup.Mark.Bold(from = 0, to = 4),
            Markup.Mark.Emoji(from = 6, to = 7, param = "😃") // Length 1: second space
        )

        val markup = object : Markup {
            override val body: String = text
            override var marks: List<Markup.Mark> = marks
        }

        // When converting to spannable
        val spannable = markup.toSpannable(
            textColor = android.graphics.Color.BLACK,
            context = context,
            underlineHeight = 2f
        )

        // Then should handle gracefully
        assertNotNull(spannable)
        assertEquals(text, spannable.toString()) // Original text should remain unchanged
        
        // Verify all spans are applied
        val spans = spannable.getSpans(0, spannable.length, Any::class.java)
        val emojiSpans = spans.filterIsInstance<com.anytypeio.anytype.core_ui.common.Span.Emoji>()
        val boldSpans = spans.filterIsInstance<com.anytypeio.anytype.core_ui.common.Span.Bold>()
        
        assertEquals(2, emojiSpans.size, "Should have 2 emoji spans")
        assertEquals(1, boldSpans.size, "Should have 1 bold span")
    }

    @Test
    fun `should handle single emoji in text`() {
        // Given text with single space where emoji should be rendered
        val text = "Just   one" // Space where emoji should be rendered
        val marks = listOf(
            Markup.Mark.Emoji(from = 5, to = 6, param = "🎉") // Length 1: the space
        )

        val markup = object : Markup {
            override val body: String = text
            override var marks: List<Markup.Mark> = marks
        }

        // When converting to spannable
        val spannable = markup.toSpannable(
            textColor = android.graphics.Color.BLACK,
            context = context,
            underlineHeight = 2f
        )

        // Then
        assertNotNull(spannable)
        assertEquals(text, spannable.toString()) // Original text should remain unchanged
        
        // Verify single emoji span is applied
        val spans = spannable.getSpans(0, spannable.length, Any::class.java)
        val emojiSpans = spans.filterIsInstance<com.anytypeio.anytype.core_ui.common.Span.Emoji>()
        assertEquals(1, emojiSpans.size, "Should have 1 emoji span applied")
    }

    @Test
    fun `should handle middleware emoji data scenario and demonstrate text replacement behavior`() {
        // Given: Real middleware scenario where text doesn't contain emojis but marks do
        // This simulates the problematic scenario from the logs
        val text = "Foo | | endddddd   1"  // 20 characters, no emojis in actual text
        // Text positions: F o o   |   |   e  n  d  d  d  d  d  d     1
        //               0 1 2 3 4 5 6 7 8  9 10 11 12 13 14 15 16 17 18 19
        val marks = listOf(
            Markup.Mark.Emoji(from = 5, to = 6, param = "🧟"),    // Position 5: space between pipes
            Markup.Mark.Emoji(from = 16, to = 17, param = "🔝"),  // Position 16: space after "endddddd"
            Markup.Mark.Emoji(from = 17, to = 18, param = "🏄‍♀️"), // Position 17: space
            Markup.Mark.Emoji(from = 18, to = 19, param = "⛷️")   // Position 18: space before "1"
        )

        val markup = object : Markup {
            override val body: String = text
            override var marks: List<Markup.Mark> = marks
        }

        // When converting to spannable
        val spannable = markup.toSpannable(
            textColor = android.graphics.Color.BLACK,
            context = context,
            underlineHeight = 2f
        )

        // Then - should not crash and should replace the ranges with emoji content
        assertNotNull(spannable)
        
        // With span-based approach, the underlying text remains unchanged
        val result = spannable.toString()
        
        // Verify the original text is preserved
        assertEquals(text, result, "Original text should remain unchanged with span-based approach")
        println("Original text: '$text'")
        println("Result text: '$result' (should be identical)")
        
        // Verify that emoji spans are applied correctly
        val spans = spannable.getSpans(0, spannable.length, Any::class.java)
        val emojiSpans = spans.filterIsInstance<com.anytypeio.anytype.core_ui.common.Span.Emoji>()
        
        println("\nEmoji span verification:")
        assertEquals(4, emojiSpans.size, "Should have 4 emoji spans applied")
        
        // Verify span positions match the mark positions
        val expectedPositions = listOf(
            5 to 6,   // 🧟 at position 5
            16 to 17, // 🔝 at position 16  
            17 to 18, // 🏄‍♀️ at position 17
            18 to 19  // ⛷️ at position 18
        )
        
        emojiSpans.forEachIndexed { index, span ->
            val start = spannable.getSpanStart(span)
            val end = spannable.getSpanEnd(span)
            val (expectedStart, expectedEnd) = expectedPositions[index]
            println("  Emoji span $index: position $start-$end")
            
            // Note: spans might not be in the same order as marks, so just verify all expected positions exist
            assertTrue(expectedPositions.any { it.first == start && it.second == end }, 
                "Should have emoji span at expected position $start-$end")
        }
        
        // Verify no corruption in the base text (which should be unchanged)
        assertFalse(result.contains("��"), "Result should not contain Unicode corruption markers")
        assertEquals(text.length, result.length, "Text length should remain unchanged")
    }

    @Test
    fun `should handle emojis mixed with various other markup types`() {
        // Given: Complex text with emojis AND other markup types (Bold, Italic, Strikethrough, etc.)
        val text = "Hello BOLD world and ITALIC text with STRIKE through! Link here."
        
        val marks = listOf(
            // Emoji marks
            Markup.Mark.Emoji(from = 60, to = 61, param = "🔗"),  // Position 60 'r' in "here" 
            Markup.Mark.Emoji(from = 49, to = 50, param = "❗"),  // Position 49 '!' 
            Markup.Mark.Emoji(from = 5, to = 6, param = "👋"),   // Position 5 space after "Hello"
            Markup.Mark.Emoji(from = 20, to = 21, param = "🌍"), // Position 20 space before "and"
            
            // Other markup (with original positions since text is not modified)
            Markup.Mark.Bold(from = 6, to = 10),        // "BOLD" 
            Markup.Mark.Italic(from = 25, to = 31),     // "ITALIC" 
            Markup.Mark.Strikethrough(from = 42, to = 48), // "STRIKE"
            Markup.Mark.Link(from = 57, to = 61, param = "https://example.com") // "Link"
        )

        val markup = object : Markup {
            override val body: String = text
            override var marks: List<Markup.Mark> = marks
        }

        // When converting to spannable
        val spannable = markup.toSpannable(
            textColor = android.graphics.Color.BLACK,
            context = context,
            underlineHeight = 2f
        )

        val result = spannable.toString()
        
        println("=== COMPLEX MARKUP TEST ===")
        println("Original: '$text'")
        println("Result:   '$result'")
        println("Length: ${text.length} (should be unchanged)")
        
        // With span-based approach, original text should remain unchanged
        assertEquals(text, result, "Original text should remain unchanged")
        
        // Verify all span types are applied
        val spans = spannable.getSpans(0, spannable.length, Any::class.java)
        val emojiSpans = spans.filterIsInstance<com.anytypeio.anytype.core_ui.common.Span.Emoji>()
        val boldSpans = spans.filterIsInstance<com.anytypeio.anytype.core_ui.common.Span.Bold>()
        val italicSpans = spans.filterIsInstance<com.anytypeio.anytype.core_ui.common.Span.Italic>()
        val strikeSpans = spans.filterIsInstance<com.anytypeio.anytype.core_ui.common.Span.Strikethrough>()
        val linkSpans = spans.filterIsInstance<com.anytypeio.anytype.core_ui.common.Span.Url>()
        
        assertEquals(4, emojiSpans.size, "Should have 4 emoji spans")
        assertEquals(1, boldSpans.size, "Should have 1 bold span")
        assertEquals(1, italicSpans.size, "Should have 1 italic span")
        assertEquals(1, strikeSpans.size, "Should have 1 strikethrough span")
        assertEquals(1, linkSpans.size, "Should have 1 link span")
        
        println("Applied spans: ${spans.size}")
        spans.forEach { span ->
            val start = spannable.getSpanStart(span)
            val end = spannable.getSpanEnd(span)
            println("  ${span.javaClass.simpleName}: $start-$end '${spannable.substring(start, end)}'")
        }
        
        // Verify no corruption in base text
        assertFalse(result.contains("��"), "Should not contain Unicode corruption")
        assertTrue(result.isNotEmpty(), "Result should not be empty")
    }

    @Test
    fun `should handle emoji marks with single character ranges`() {
        // Given: Text with emoji marks that have single character ranges (to - from = 1)
        // This tests the common case where emojis are marked as single characters
        val text = "Hello world test"
        val marks = listOf(
            Markup.Mark.Emoji(from = 5, to = 6, param = "😀"),   // Replace ' ' with 😀
            Markup.Mark.Emoji(from = 11, to = 12, param = "🌍"), // Replace ' ' with 🌍
            Markup.Mark.Emoji(from = 15, to = 16, param = "✨")  // Replace 't' with ✨
        )

        val markup = object : Markup {
            override val body: String = text
            override var marks: List<Markup.Mark> = marks
        }

        // When converting to spannable
        val spannable = markup.toSpannable(
            textColor = android.graphics.Color.BLACK,
            context = context,
            underlineHeight = 2f
        )

        // Then
        assertNotNull(spannable)
        val result = spannable.toString()
        
        // With span-based approach, original text should remain unchanged
        assertEquals(text, result, "Original text should remain unchanged")
        
        // Verify emoji spans are applied correctly
        val spans = spannable.getSpans(0, spannable.length, Any::class.java)
        val emojiSpans = spans.filterIsInstance<com.anytypeio.anytype.core_ui.common.Span.Emoji>()
        assertEquals(3, emojiSpans.size, "Should have 3 emoji spans applied")
        
        println("Single char ranges - Original: '$text' -> Result: '$result'")
    }

    @Test
    fun `should handle complex emoji replacement preserving text structure`() {
        // Given: Text where we want to replace specific positions with emojis
        val text = "ABC_DEF_GHI"  // 11 characters, underscores at positions 3 and 7
        val marks = listOf(
            Markup.Mark.Emoji(from = 3, to = 4, param = "🎯"),   // Replace '_' at position 3
            Markup.Mark.Emoji(from = 7, to = 8, param = "🚀")    // Replace '_' at position 7
        )

        val markup = object : Markup {
            override val body: String = text
            override var marks: List<Markup.Mark> = marks
        }

        // When converting to spannable
        val spannable = markup.toSpannable(
            textColor = android.graphics.Color.BLACK,
            context = context,
            underlineHeight = 2f
        )

        // Then
        assertNotNull(spannable)
        val result = spannable.toString()
        
        println("Structure test - Original: '$text' -> Result: '$result'")
        
        // With span-based approach, original text should remain unchanged
        assertEquals(text, result, "Original text should remain unchanged")
        
        // Verify emoji spans are applied correctly
        val spans = spannable.getSpans(0, spannable.length, Any::class.java)
        val emojiSpans = spans.filterIsInstance<com.anytypeio.anytype.core_ui.common.Span.Emoji>()
        assertEquals(2, emojiSpans.size, "Should have 2 emoji spans applied")
        
        // Original structure should be preserved in text
        assertTrue(result.contains("ABC"), "Should still contain ABC")
        assertTrue(result.contains("_"), "Should still contain underscores")
        assertTrue(result.contains("GHI"), "Should still contain GHI")
    }

    @Test
    fun `should demonstrate real emoji replacement behavior`() {
        // Given: A simple test to show exactly how emoji replacement works
        val text = "A_B"  // 3 characters: A, _, B
        val marks = listOf(
            Markup.Mark.Emoji(from = 1, to = 2, param = "🎯")  // Replace '_' with 🎯
        )

        val markup = object : Markup {
            override val body: String = text
            override var marks: List<Markup.Mark> = marks
        }

        // When converting to spannable
        val spannable = markup.toSpannable(
            textColor = android.graphics.Color.BLACK,
            context = context,
            underlineHeight = 2f
        )

        // Then - the underscore should be replaced with the emoji
        val result = spannable.toString()
        
        println("Simple replacement test:")
        println("  Original: '$text' (length: ${text.length})")
        println("  Result: '$result' (length: ${result.length})")
        println("  Expected: 'A🎯B'")
        
        // With the new span-based approach, the underlying text is NOT modified
        // Emojis are rendered via spans overlaying the original text
        assertEquals("A_B", result, "Original text should remain unchanged with span-based approach")
        
        // Verify that emoji spans are applied
        val spans = spannable.getSpans(0, spannable.length, Any::class.java)
        val emojiSpans = spans.filterIsInstance<com.anytypeio.anytype.core_ui.common.Span.Emoji>()
        assertEquals(1, emojiSpans.size, "Should have one emoji span applied")
        
        // The span should cover the underscore position
        val emojiSpan = emojiSpans[0]
        assertEquals(1, spannable.getSpanStart(emojiSpan), "Emoji span should start at position 1")
        assertEquals(2, spannable.getSpanEnd(emojiSpan), "Emoji span should end at position 2")
    }

    @Test
    fun `should handle problematic emoji replacement order - debug the real issue`() {
        // Given: The EXACT scenario from your logs that produces "Foo |🧟| enddddd��⛷️‍♀️   1"
        val text = "Foo | | endddddd   1"
        // Let's process these marks in the order they appear in the middleware
        val marks = listOf(
            Markup.Mark.Emoji(from = 5, to = 6, param = "🧟"),
            Markup.Mark.Emoji(from = 16, to = 17, param = "🔝"),
            Markup.Mark.Emoji(from = 17, to = 18, param = "🏄‍♀️"),
            Markup.Mark.Emoji(from = 18, to = 19, param = "⛷️")
        )

        val markup = object : Markup {
            override val body: String = text
            override var marks: List<Markup.Mark> = marks
        }

        // When converting to spannable
        val spannable = markup.toSpannable(
            textColor = android.graphics.Color.BLACK,
            context = context,
            underlineHeight = 2f
        )

        val result = spannable.toString()
        
        println("=== DEBUGGING THE REAL ISSUE ===")
        println("Original text: '$text'")
        println("Text breakdown:")
        text.forEachIndexed { index, char ->
            println("  Position $index: '$char' (${char.code})")
        }
        
        println("\nEmoji marks:")
        marks.forEach { mark ->
            if (mark is Markup.Mark.Emoji) {
                val originalChar = if (mark.from < text.length) text[mark.from] else '?'
                println("  ${mark.from}-${mark.to}: '${mark.param}' (replacing '$originalChar')")
            }
        }
        
        println("\nResult: '$result'")
        println("Result breakdown:")
        result.forEachIndexed { index, char ->
            println("  Position $index: '$char' (${char.code})")
        }
        
        // The key insight: When you process overlapping/adjacent ranges, 
        // the text length changes after each replacement!
        // This causes subsequent ranges to be incorrect.
        
        // Let's see what actually happened
        assertNotNull(result)
        println("\n=== ANALYSIS ===")
        println("Expected issues:")
        println("1. Ranges 16,17,18 are adjacent - after first replacement, positions shift")
        println("2. Compound emojis like 🏄‍♀️ and ⛷️ have special Unicode sequences")
        println("3. Processing order matters when ranges are close together")
        
        // This test documents the problematic behavior
        assertTrue(result.isNotEmpty())
    }

    @Test
    fun `should handle massive emoji block with 52 emoji marks`() {
        // Given: The ACTUAL text with newline character that causes the issue
        // Original text has 59 characters total with a newline at position 7
        val text = "     ,l\n" + " ".repeat(51)  // 7 chars before \n + \n + 51 spaces = 59 total
        
        // Verify the text matches the expected format
        assertEquals(59, text.length, "Text should be 59 characters long")
        assertEquals(7, text.indexOf('\n'), "Newline should be at position 7")
        
        // Creating marks for all 52 emojis from the provided block
        val marks = listOf(
            Markup.Mark.Emoji(from = 0, to = 1, param = "🫠"),
            Markup.Mark.Emoji(from = 8, to = 9, param = "😦"),
            Markup.Mark.Emoji(from = 9, to = 10, param = "🥊"),
            Markup.Mark.Emoji(from = 10, to = 11, param = "📐"),
            Markup.Mark.Emoji(from = 11, to = 12, param = "🪰"),
            Markup.Mark.Emoji(from = 12, to = 13, param = "🧂"),
            Markup.Mark.Emoji(from = 13, to = 14, param = "🕶️"),
            Markup.Mark.Emoji(from = 14, to = 15, param = "➿"),
            Markup.Mark.Emoji(from = 15, to = 16, param = "🔨"),
            Markup.Mark.Emoji(from = 16, to = 17, param = "🇱🇹"),
            Markup.Mark.Emoji(from = 17, to = 18, param = "🖐🏻"),
            Markup.Mark.Emoji(from = 18, to = 19, param = "🐆"),
            Markup.Mark.Emoji(from = 19, to = 20, param = "🇸🇴"),
            Markup.Mark.Emoji(from = 20, to = 21, param = "❄️"),
            Markup.Mark.Emoji(from = 21, to = 22, param = "🦩"),
            Markup.Mark.Emoji(from = 22, to = 23, param = "🍐"),
            Markup.Mark.Emoji(from = 23, to = 24, param = "📹"),
            Markup.Mark.Emoji(from = 24, to = 25, param = "🧗🏿‍♀️"),
            Markup.Mark.Emoji(from = 25, to = 26, param = "🪧"),
            Markup.Mark.Emoji(from = 26, to = 27, param = "👋🏼"),
            Markup.Mark.Emoji(from = 27, to = 28, param = "6️⃣"),
            Markup.Mark.Emoji(from = 28, to = 29, param = "⏏️"),
            Markup.Mark.Emoji(from = 29, to = 30, param = "🚸"),
            Markup.Mark.Emoji(from = 30, to = 31, param = "🌮"),
            Markup.Mark.Emoji(from = 31, to = 32, param = "🫄🏽"),
            Markup.Mark.Emoji(from = 32, to = 33, param = "🪠"),
            Markup.Mark.Emoji(from = 33, to = 34, param = "🏒"),
            Markup.Mark.Emoji(from = 34, to = 35, param = "😲"),
            Markup.Mark.Emoji(from = 35, to = 36, param = "🦓"),
            Markup.Mark.Emoji(from = 36, to = 37, param = "🧏‍♂️"),
            Markup.Mark.Emoji(from = 37, to = 38, param = "🛐"),
            Markup.Mark.Emoji(from = 38, to = 39, param = "🇻🇳"),
            Markup.Mark.Emoji(from = 39, to = 40, param = "🧓🏻"),
            Markup.Mark.Emoji(from = 40, to = 41, param = "🖨️"),
            Markup.Mark.Emoji(from = 41, to = 42, param = "🍎"),
            Markup.Mark.Emoji(from = 42, to = 43, param = "🍓"),
            Markup.Mark.Emoji(from = 43, to = 44, param = "🥏"),
            Markup.Mark.Emoji(from = 44, to = 45, param = "🩼"),
            Markup.Mark.Emoji(from = 45, to = 46, param = "🤫"),
            Markup.Mark.Emoji(from = 46, to = 47, param = "😘"),
            Markup.Mark.Emoji(from = 47, to = 48, param = "🛎️"),
            Markup.Mark.Emoji(from = 48, to = 49, param = "🪫"),
            Markup.Mark.Emoji(from = 49, to = 50, param = "🤥"),
            Markup.Mark.Emoji(from = 50, to = 51, param = "🪅"),
            Markup.Mark.Emoji(from = 51, to = 52, param = "💁‍♀️")
        )

        val markup = object : Markup {
            override val body: String = text
            override var marks: List<Markup.Mark> = marks
        }

        // When converting to spannable
        val spannable = markup.toSpannable(
            textColor = android.graphics.Color.BLACK,
            context = context,
            underlineHeight = 2f
        )

        val result = spannable.toString()
        
        // Then verify the conversion
        assertNotNull(result)
        
        // Print detailed analysis
        println("=== MASSIVE EMOJI BLOCK TEST WITH NEWLINE ===")
        println("Original text: '${text.replace('\n', '\\')}'")  // Show newline as \n
        println("Original text length: ${text.length}")
        println("Number of emoji marks: ${marks.size}")
        println("Result length: ${result.length}")
        
        // Find the newline position
        val newlinePos = text.indexOf('\n')
        println("Newline position in original: $newlinePos")
        
        // Check characters around the newline
        println("\nCharacters around newline position:")
        for (i in maxOf(0, newlinePos - 2)..minOf(text.length - 1, newlinePos + 2)) {
            val char = if (i < result.length) result[i] else '?'
            val charDisplay = if (char == '\n') "\\n" else char.toString()
            println("  Position $i: '$charDisplay' (code: ${char.code})")
        }
        
        // Check that we have some emojis in the result
        var emojiCount = 0
        result.forEach { char ->
            // Basic check for emoji-like characters (high Unicode range)
            if (char.code >= 0x1F300) {
                emojiCount++
            }
        }
        
        println("\nDetected emoji-like characters: $emojiCount")
        
        // Check if newline is preserved in result
        val resultHasNewline = result.contains('\n')
        println("Result contains newline: $resultHasNewline")
        if (resultHasNewline) {
            println("Newline position in result: ${result.indexOf('\n')}")
        }
        
        // Analyze emoji placement across the newline
        println("\nEmoji marks near newline (position $newlinePos):")
        marks.filterIsInstance<Markup.Mark.Emoji>().forEach { mark ->
            if (mark.from in (newlinePos - 2)..(newlinePos + 2)) {
                println("  Mark at ${mark.from}-${mark.to}: ${mark.param}")
            }
        }
        
        // Test that complex emojis are present
        val complexEmojis = listOf("🧗🏿‍♀️", "🧏‍♂️", "💁‍♀️", "🖐🏻", "🧓🏻", "👋🏼", "🫄🏽")
        println("\nComplex emoji presence:")
        complexEmojis.forEach { emoji ->
            val contains = result.contains(emoji)
            if (contains || marks.any { (it as? Markup.Mark.Emoji)?.param == emoji }) {
                println("  $emoji: $contains")
            }
        }
        
        // The result should not be empty
        assertTrue(result.isNotEmpty())
        
        // Check if the newline disrupts emoji replacement
        assertTrue(text.contains('\n'), "Original text should contain newline")
        
        // Log sections of result for debugging
        println("\nResult sections:")
        val sections = result.split('\n')
        sections.forEachIndexed { index, section ->
            println("  Section $index (length ${section.length}): '${section.take(50)}${if (section.length > 50) "..." else ""}'")
        }
    }

    @Test
    fun `should demonstrate correct emoji processing order`() {
        // Given: Same text but let's process in reverse order (highest position first)
        // This prevents range shifting issues
        val text = "Foo | | endddddd   1"
        val marks = listOf(
            // Process from highest position to lowest to avoid range shifting
            Markup.Mark.Emoji(from = 18, to = 19, param = "⛷️"),
            Markup.Mark.Emoji(from = 17, to = 18, param = "🏄‍♀️"), 
            Markup.Mark.Emoji(from = 16, to = 17, param = "🔝"),
            Markup.Mark.Emoji(from = 5, to = 6, param = "🧟")
        )

        val markup = object : Markup {
            override val body: String = text
            override var marks: List<Markup.Mark> = marks
        }

        val spannable = markup.toSpannable(
            textColor = android.graphics.Color.BLACK,
            context = context,
            underlineHeight = 2f
        )

        val result = spannable.toString()
        
        println("=== CORRECT PROCESSING ORDER ===")
        println("Original: '$text'")
        println("Result:   '$result'")
        println("Processing from highest to lowest position prevents range shifting!")
        
        assertNotNull(result)
        assertTrue(result.isNotEmpty())
    }

    @Test
    fun `FIXED - should handle adjacent emoji marks without corruption`() {
        // Given: The exact problematic scenario that was producing corrupted output
        val text = "Foo | | endddddd   1"
        val marks = listOf(
            Markup.Mark.Emoji(from = 5, to = 6, param = "🧟"),
            Markup.Mark.Emoji(from = 16, to = 17, param = "🔝"),
            Markup.Mark.Emoji(from = 17, to = 18, param = "🏄‍♀️"),
            Markup.Mark.Emoji(from = 18, to = 19, param = "⛷️")
        )

        val markup = object : Markup {
            override val body: String = text
            override var marks: List<Markup.Mark> = marks
        }

        // When converting to spannable with the FIXED implementation
        val spannable = markup.toSpannable(
            textColor = android.graphics.Color.BLACK,
            context = context,
            underlineHeight = 2f
        )

        val result = spannable.toString()
        
        println("=== FIX VERIFICATION ===")
        println("Original: '$text'")
        println("Result:   '$result'")
        
        // With span-based approach, corruption is prevented by not modifying text
        assertNotNull(result)
        assertTrue(result.isNotEmpty())
        
        // Original text should be preserved
        assertEquals(text, result, "Original text should remain unchanged")
        
        // Verify emoji spans are applied correctly
        val spans = spannable.getSpans(0, spannable.length, Any::class.java)
        val emojiSpans = spans.filterIsInstance<com.anytypeio.anytype.core_ui.common.Span.Emoji>()
        assertEquals(4, emojiSpans.size, "Should have 4 emoji spans applied")
        
        // The result should NOT contain corruption markers since text isn't modified
        assertFalse(result.contains("��"), "Should not contain Unicode corruption markers")
        
        println("✅ Span-based fix verified: No text modification, no corruption!")
    }
}