package com.anytypeio.anytype.presentation.extension

import com.anytypeio.anytype.core_ui.common.Markup
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MarkupExtensionKtTest {

    @Test
    fun `should be true in range with overlap EQUAL`() {
        val bold = Markup.Type.BOLD

        val selection = 0..5

        val marks = listOf(Markup.Mark(0, 5, Markup.Type.BOLD))

        val result = bold.isInRange(marks, selection)

        assertTrue(result)
    }

    @Test
    fun `should be true in range with overlap INNER`() {
        val bold = Markup.Type.BOLD

        val selection = 1..4

        val marks = listOf(Markup.Mark(0, 5, Markup.Type.BOLD))

        val result = bold.isInRange(marks, selection)

        assertTrue(result)
    }

    @Test
    fun `should be true in range with overlap INNER_LEFT`() {
        val bold = Markup.Type.BOLD

        val selection = 0..3

        val marks = listOf(Markup.Mark(0, 5, Markup.Type.BOLD))

        val result = bold.isInRange(marks, selection)

        assertTrue(result)
    }

    @Test
    fun `should be true in range with overlap INNER_RIGHT`() {
        val bold = Markup.Type.BOLD

        val selection = 3..5

        val marks = listOf(Markup.Mark(0, 5, Markup.Type.BOLD))

        val result = bold.isInRange(marks, selection)

        assertTrue(result)
    }

    @Test
    fun `should be false in range with overlap LEFT`() {
        val bold = Markup.Type.BOLD

        val selection = 0..3

        val marks = listOf(Markup.Mark(1, 4, Markup.Type.BOLD))

        val result = bold.isInRange(marks, selection)

        assertFalse(result)
    }

    @Test
    fun `should be false in range with overlap RIGHT`() {
        val bold = Markup.Type.BOLD

        val selection = 3..5

        val marks = listOf(Markup.Mark(1, 4, Markup.Type.BOLD))

        val result = bold.isInRange(marks, selection)

        assertFalse(result)
    }

    @Test
    fun `should be false in range with overlap OUTER`() {
        val bold = Markup.Type.BOLD

        val selection = 0..5

        val marks = listOf(Markup.Mark(1, 4, Markup.Type.BOLD))

        val result = bold.isInRange(marks, selection)

        assertFalse(result)
    }

    @Test
    fun `should be false when markup is not present`() {
        val bold = Markup.Type.ITALIC

        val selection = 0..5

        val marks = listOf(Markup.Mark(0, 5, Markup.Type.BOLD))

        val result = bold.isInRange(marks, selection)

        assertFalse(result)
    }

    @Test
    fun `should be false when no markups`() {
        val bold = Markup.Type.ITALIC

        val selection = 0..5

        val marks = listOf<Markup.Mark>()

        val result = bold.isInRange(marks, selection)

        assertFalse(result)
    }

    @Test
    fun `should be false when selection is 0`() {
        val bold = Markup.Type.BOLD

        val selection = 0..0

        val marks = listOf(Markup.Mark(0, 5, Markup.Type.BOLD))

        val result = bold.isInRange(marks, selection)

        assertFalse(result)
    }

    @Test
    fun `should be false when selection is EMPTY`() {
        val bold = Markup.Type.BOLD

        val selection = IntRange.EMPTY

        val marks = listOf(Markup.Mark(0, 5, Markup.Type.BOLD))

        val result = bold.isInRange(marks, selection)

        assertFalse(result)
    }
}