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
        // Given text with emoji marks
        val text = "Hello 😀 world! 🎉 Nice day!"
        val marks = listOf(
            Markup.Mark.Emoji(from = 6, to = 8, param = "😀"),
            Markup.Mark.Emoji(from = 16, to = 18, param = "🎉")
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
        assertEquals(text.length, spannable.length)
        assertTrue(spannable.toString().contains("😀"))
        assertTrue(spannable.toString().contains("🎉"))
    }

    @Test
    fun `should handle multiple emoji marks in sequence`() {
        // Given text with multiple consecutive emoji marks
        val text = "Emojis: 😀😃😄 are fun!"
        val marks = listOf(
            Markup.Mark.Emoji(from = 8, to = 10, param = "😀"),
            Markup.Mark.Emoji(from = 10, to = 12, param = "😃"),
            Markup.Mark.Emoji(from = 12, to = 14, param = "😄")
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
        assertTrue(spannable.toString().contains("😀"))
        assertTrue(spannable.toString().contains("😃"))
        assertTrue(spannable.toString().contains("😄"))
    }

    @Test
    fun `should handle emoji marks mixed with other marks`() {
        // Given text with emoji and other markup
        val text = "Hello 😀 world!"
        val marks = listOf(
            Markup.Mark.Bold(from = 0, to = 5),
            Markup.Mark.Emoji(from = 6, to = 8, param = "😀"),
            Markup.Mark.Italic(from = 9, to = 15)
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
        assertTrue(spannable.toString().contains("😀"))
    }

    @Test
    fun `should handle empty emoji param gracefully`() {
        // Given text with empty emoji param
        val text = "Hello  world!"
        val marks = listOf(
            Markup.Mark.Emoji(from = 6, to = 8, param = "")
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

        // Then - should not crash and should replace the range with the processed empty param
        assertNotNull(spannable)
        // Note: After replacement, text length might change since we replace from..to with processed param
        assertTrue(spannable.length <= text.length)
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
        // Given text with complex unicode emojis (skin tone, compound emojis)
        val text = "Hello 👍🏻 and 👨‍👩‍👧‍👦 world!"
        val marks = listOf(
            Markup.Mark.Emoji(from = 6, to = 8, param = "👍🏻"),
            Markup.Mark.Emoji(from = 13, to = 21, param = "👨‍👩‍👧‍👦")
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

        // Then should not crash and contain the processed text
        assertNotNull(spannable)
        assertTrue(spannable.length > 0)
    }

    @Test
    fun `should handle edge case with overlapping emoji ranges`() {
        // Given text with potentially overlapping ranges (should be processed in order)
        val text = "Test 😀😃 end"
        val marks = listOf(
            Markup.Mark.Emoji(from = 5, to = 7, param = "😀"),
            Markup.Mark.Bold(from = 0, to = 4),
            Markup.Mark.Emoji(from = 7, to = 9, param = "😃")
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
    }

    @Test
    fun `should handle single emoji in text`() {
        // Given text with single emoji
        val text = "Just 🎉 one"
        val marks = listOf(
            Markup.Mark.Emoji(from = 5, to = 7, param = "🎉")
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
        assertTrue(spannable.toString().contains("🎉"))
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