package com.anytypeio.anytype.core_ui

import com.anytypeio.anytype.presentation.editor.editor.pattern.DefaultPatternMatcher
import com.anytypeio.anytype.presentation.editor.editor.pattern.Pattern
import org.junit.Test
import kotlin.test.assertEquals

class DefaultPatternMatcherTest {

    private val matcher = DefaultPatternMatcher()

    @Test
    fun `should detect bullet pattern`() {
        val strings = listOf("* ", "- ", "+ ")

        strings.forEach { text ->
            val result = matcher.match(text)
            assertEquals(
                expected = listOf(Pattern.BULLET),
                actual = result
            )
        }
    }

    @Test
    fun `should not detect bullet pattern - when empty space is missing after pattern`() {
        val strings = listOf("*", "-", "+")

        strings.forEach { text ->
            val result = matcher.match(text)
            assertEquals(
                expected = emptyList(),
                actual = result
            )
        }
    }

    @Test
    fun `should detect numbered pattern`() {
        val strings = listOf("1. ")

        strings.forEach { text ->
            val result = matcher.match(text)
            assertEquals(
                expected = listOf(Pattern.NUMBERED),
                actual = result
            )
        }
    }

    @Test
    fun `should not detect numbered pattern`() {
        val strings = listOf("1", "1.", "1 .", "1,", "1, ","1* ","1# ")

        strings.forEach { text ->
            val result = matcher.match(text)
            print("matching: $text")
            assertEquals(
                expected = emptyList(),
                actual = result
            )
        }
    }

    @Test
    fun `should detect line-divider pattern`() {
        val strings = listOf("--- ")

        strings.forEach { text ->
            val result = matcher.match(text)
            assertEquals(
                expected = listOf(Pattern.DIVIDER_LINE),
                actual = result
            )
        }
    }

    @Test
    fun `should not detect line-divider pattern - when empty space is missing after pattern`() {
        val strings = listOf("---")

        strings.forEach { text ->
            val result = matcher.match(text)
            assertEquals(
                expected = emptyList(),
                actual = result
            )
        }
    }

    @Test
    fun `should detect dots-divider pattern`() {
        val strings = listOf("*** ")

        strings.forEach { text ->
            val result = matcher.match(text)
            assertEquals(
                expected = listOf(Pattern.DIVIDER_DOTS),
                actual = result
            )
        }
    }

    @Test
    fun `should not detect dots-divider pattern - when empty space is missing after pattern`() {
        val strings = listOf("***")

        strings.forEach { text ->
            val result = matcher.match(text)
            assertEquals(
                expected = emptyList(),
                actual = result
            )
        }
    }

    @Test
    fun `should detect quote pattern`() {
        val strings = listOf("\" ", "« ", "' ", "“ ")

        strings.forEach { text ->
            val result = matcher.match(text)
            assertEquals(
                expected = listOf(Pattern.QUOTE),
                actual = result
            )
        }
    }

    @Test
    fun `should not detect quote pattern - when empty space is missing after pattern`() {
        val strings = listOf("\"", "«", "'", "“")

        strings.forEach { text ->
            val result = matcher.match(text)
            assertEquals(
                expected = emptyList(),
                actual = result
            )
        }
    }

    @Test
    fun `should detect checkbox pattern`() {
        val strings = listOf("[] ")

        strings.forEach { text ->
            val result = matcher.match(text)
            assertEquals(
                expected = listOf(Pattern.CHECKBOX),
                actual = result
            )
        }
    }

    @Test
    fun `should not detect checkbox pattern - when empty space is missing after pattern`() {
        val strings = listOf("[]")

        strings.forEach { text ->
            val result = matcher.match(text)
            assertEquals(
                expected = emptyList(),
                actual = result
            )
        }
    }

    @Test
    fun `should detect code snippet pattern`() {
        val strings = listOf("``` ")

        strings.forEach { text ->
            val result = matcher.match(text)
            assertEquals(
                expected = listOf(Pattern.SNIPPET),
                actual = result
            )
        }
    }

    @Test
    fun `should not detect code snippet pattern - when empty space is missing after pattern`() {
        val strings = listOf("```")

        strings.forEach { text ->
            val result = matcher.match(text)
            assertEquals(
                expected = emptyList(),
                actual = result
            )
        }
    }
}