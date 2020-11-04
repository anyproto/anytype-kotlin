package com.anytypeio.anytype.core_ui

import com.anytypeio.anytype.presentation.page.editor.pattern.DefaultPatternMatcher
import com.anytypeio.anytype.presentation.page.editor.pattern.Pattern
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
}