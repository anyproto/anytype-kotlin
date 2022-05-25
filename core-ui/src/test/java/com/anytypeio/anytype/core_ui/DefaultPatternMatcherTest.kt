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
}