package com.anytypeio.anytype.presentation.extension

import com.anytypeio.anytype.presentation.editor.editor.Markup
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MarkupExtensionKtTest {

    @Test
    fun `should be true in range with overlap EQUAL`() {
        val selection = 0..5

        val marks = listOf(Markup.Mark.Bold(0, 5))

        val result = marks.isBoldInRange(selection)

        assertTrue(result)
    }

    @Test
    fun `should be true in range with overlap INNER`() {
        val selection = 1..4

        val marks = listOf(Markup.Mark.Bold(0, 5))

        val result = marks.isBoldInRange(selection)

        assertTrue(result)
    }

    @Test
    fun `should be true in range with overlap INNER_LEFT`() {
        val selection = 0..3

        val marks = listOf(Markup.Mark.Bold(0, 5))

        val result = marks.isBoldInRange(selection)

        assertTrue(result)
    }

    @Test
    fun `should be true in range with overlap INNER_RIGHT`() {
        val selection = 3..5

        val marks = listOf(Markup.Mark.Bold(0, 5))

        val result = marks.isBoldInRange(selection)

        assertTrue(result)
    }

    @Test
    fun `should be false in range with overlap LEFT`() {
        val selection = 0..3

        val marks = listOf(Markup.Mark.Bold(1, 4))

        val result = marks.isBoldInRange(selection)

        assertFalse(result)
    }

    @Test
    fun `should be false in range with overlap RIGHT`() {
        val selection = 3..5

        val marks = listOf(Markup.Mark.Bold(1, 4))

        val result = marks.isBoldInRange(selection)

        assertFalse(result)
    }

    @Test
    fun `should be false in range with overlap OUTER`() {
        val selection = 0..5

        val marks = listOf(Markup.Mark.Bold(1, 4))

        val result = marks.isBoldInRange(selection)

        assertFalse(result)
    }

    @Test
    fun `should be false when markup is not present`() {
        val selection = 0..5

        val marks = listOf(Markup.Mark.Italic(0, 5))

        val result = marks.isBoldInRange(selection)

        assertFalse(result)
    }

    @Test
    fun `should be false when no markups`() {
        val selection = 0..5

        val marks = listOf<Markup.Mark>()

        val result = marks.isBoldInRange(selection)

        assertFalse(result)
    }

    @Test
    fun `should be false when selection is 0`() {
        val selection = 0..0

        val marks = listOf(Markup.Mark.Bold(0, 5))

        val result = marks.isBoldInRange(selection)

        assertFalse(result)
    }

    @Test
    fun `should be false when selection is EMPTY`() {
        val selection = IntRange.EMPTY

        val marks = listOf(Markup.Mark.Bold(0, 5))

        val result = marks.isBoldInRange(selection)

        assertFalse(result)
    }
}