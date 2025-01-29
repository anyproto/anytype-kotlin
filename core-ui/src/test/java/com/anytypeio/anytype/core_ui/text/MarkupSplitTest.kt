package com.anytypeio.anytype.core_ui.text

import com.anytypeio.anytype.core_models.Block.Content.Text.Mark
import kotlin.test.Test
import kotlin.test.assertEquals

class SplitByMarkTests {

    @Test
    fun testNoMarks() {
        val input = "Hello, world!"
        val marks = emptyList<Mark>()
        val result = input.splitByMarks(marks)
        val expected = listOf(input to emptyList<Mark>())
        assertEquals(expected, result)
    }

    @Test
    fun testSingleMark() {
        val input = "Hello, world!"
        val marks = listOf(
            Mark(0..5, Mark.Type.BOLD) // "Hello" styled as bold
        )
        val result = input.splitByMarks(marks)
        val expected = listOf(
            "Hello" to listOf(marks[0]),
            ", world!" to emptyList()
        )
        assertEquals(expected, result)
    }

    @Test
    fun testOverlappingMarks() {
        val input = "Hello, world!"
        val marks = listOf(
            Mark(0..5, Mark.Type.BOLD),  // "Hello" styled as bold
            Mark(3..8, Mark.Type.ITALIC) // Overlaps "lo, w" with italic
        )
        val result = input.splitByMarks(marks)
        val expected = listOf(
            "Hel" to listOf(marks[0]),
            "lo" to listOf(marks[0], marks[1]),
            ", w" to listOf(marks[1]),
            "orld!" to emptyList()
        )
        assertEquals(expected, result)
    }

    @Test
    fun testMultipleAdjacentMarks() {
        val input = "Hello, world!"
        val marks = listOf(
            Mark(0..5, Mark.Type.BOLD),   // "Hello" styled as bold
            Mark(7..12, Mark.Type.ITALIC) // "world" styled as italic
        )
        val result = input.splitByMarks(marks)
        val expected = listOf(
            "Hello" to listOf(marks[0]),
            ", " to emptyList(),
            "world" to listOf(marks[1]),
            "!" to emptyList()
        )
        assertEquals(expected, result)
    }

    @Test
    fun testOutOfBoundsMarks() {
        val input = "Short text"
        val marks = listOf(
            Mark(0..5, Mark.Type.BOLD),    // Valid range
            Mark(10..15, Mark.Type.ITALIC) // Out-of-bounds, should be ignored
        )
        val result = input.splitByMarks(marks)
        val expected = listOf(
            "Short" to listOf(marks[0]),
            " text" to emptyList()
        )
        assertEquals(expected, result)
    }

    @Test
    fun testEmptyString() {
        val input = ""
        val marks = listOf(
            Mark(0..5, Mark.Type.BOLD) // Should be ignored since the input is empty
        )
        val result = input.splitByMarks(marks)
        val expected = emptyList<Pair<String, List<Mark>>>()
        assertEquals(expected, result)
    }

    @Test
    fun testFullyOverlappingMarks() {
        val input = "Overlap test"
        val marks = listOf(
            Mark(0..12, Mark.Type.BOLD),   // Full range
            Mark(0..12, Mark.Type.ITALIC) // Fully overlaps with another style
        )
        val result = input.splitByMarks(marks)
        val expected = listOf(
            "Overlap test" to marks
        )
        assertEquals(expected, result)
    }

    @Test
    fun testNestedMarks() {
        val input = "Nested styles"
        val marks = listOf(
            Mark(0..6, Mark.Type.BOLD),    // "Nested" styled as bold
            Mark(4..13, Mark.Type.ITALIC) // Overlaps "ed styles" with italic
        )
        val result = input.splitByMarks(marks)
        val expected = listOf(
            "Nest" to listOf(marks[0]),
            "ed" to listOf(marks[0], marks[1]),
            " styles" to listOf(marks[1])
        )
        assertEquals(expected, result)
    }
}
