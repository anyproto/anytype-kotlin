package com.agileburo.anytype.domain.markup

import com.agileburo.anytype.domain.block.model.Block.Content.Text.Mark
import com.agileburo.anytype.domain.common.MockDataFactory
import com.agileburo.anytype.domain.ext.*
import com.agileburo.anytype.domain.misc.Overlap
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MarkupExtTest {

    @Test
    fun `should sort two bold markups according to their start range`() {

        val given = listOf(
            Mark(
                type = Mark.Type.BOLD,
                range = 2..5
            ),
            Mark(
                type = Mark.Type.BOLD,
                range = 0..2
            )
        )

        val expected = given.reversed()

        val result = given.sorted()

        assertEquals(
            expected = expected,
            actual = result
        )
    }

    @Test
    fun `should sort three bold markups according to their end range`() {

        val given = listOf(
            Mark(
                type = Mark.Type.BOLD,
                range = 0..5
            ),
            Mark(
                type = Mark.Type.BOLD,
                range = 0..2
            ),
            Mark(
                type = Mark.Type.BOLD,
                range = 0..1
            )
        )

        val expected = listOf(
            Mark(
                type = Mark.Type.BOLD,
                range = 0..1
            ),
            Mark(
                type = Mark.Type.BOLD,
                range = 0..2
            ),
            Mark(
                type = Mark.Type.BOLD,
                range = 0..5
            )
        )

        val result = given.sorted()

        assertEquals(
            expected = expected,
            actual = result
        )
    }

    @Test
    fun `should sort markup according to their type`() {

        val range = 0..5

        val given = listOf(
            Mark(
                type = Mark.Type.BOLD,
                range = range
            ),
            Mark(
                type = Mark.Type.ITALIC,
                range = range
            )
        )

        val expected = given.reversed()

        val result = given.sorted()

        assertEquals(
            expected = expected,
            actual = result
        )
    }

    @Test
    fun `should sort markups according to their type regardless of ranges order`() {

        val given = listOf(
            Mark(
                type = Mark.Type.BOLD,
                range = 0..1
            ),
            Mark(
                type = Mark.Type.ITALIC,
                range = 3..5
            )
        )

        val expected = given.reversed()

        val result = given.sorted()

        assertEquals(
            expected = expected,
            actual = result
        )
    }

    @Test
    fun `should sort three bold markups according to their start and end range`() {

        val given = listOf(
            Mark(
                type = Mark.Type.BOLD,
                range = 1..5
            ),
            Mark(
                type = Mark.Type.BOLD,
                range = 0..2
            ),
            Mark(
                type = Mark.Type.BOLD,
                range = 1..2
            )
        )

        val expected = listOf(
            Mark(
                type = Mark.Type.BOLD,
                range = 0..2
            ),
            Mark(
                type = Mark.Type.BOLD,
                range = 1..2
            ),
            Mark(
                type = Mark.Type.BOLD,
                range = 1..5
            )
        )

        val result = given.sorted()

        assertEquals(
            expected = expected,
            actual = result
        )
    }

    @Test
    fun `should merge two bold markups into one`() {

        val marks = listOf(
            Mark(
                type = Mark.Type.BOLD,
                range = 0..2
            ),
            Mark(
                type = Mark.Type.BOLD,
                range = 0..5
            )
        )

        val result = marks.normalize()

        val expected = listOf(
            Mark(
                type = Mark.Type.BOLD,
                range = 0..5
            )
        )

        assertEquals(
            expected = expected,
            actual = result
        )
    }

    @Test(expected = IllegalStateException::class)
    fun `should throw an exception if list is not sorted`() {

        val marks = listOf(
            Mark(
                type = Mark.Type.BOLD,
                range = 0..5
            ),
            Mark(
                type = Mark.Type.BOLD,
                range = 0..2
            )
        )

        marks.normalize()
    }

    @Test
    fun `should add first bold, then italic markup`() {

        val initial = listOf<Mark>()

        val bold = Mark(
            type = Mark.Type.BOLD,
            range = 0..5
        )

        val italic = Mark(
            type = Mark.Type.ITALIC,
            range = 0..5
        )

        val firstMarks = initial.addMark(mark = bold)

        assertEquals(
            expected = listOf(bold),
            actual = firstMarks
        )

        val secondMarks = firstMarks.addMark(mark = italic)

        assertEquals(
            expected = listOf(bold, italic),
            actual = secondMarks
        )
    }

    @Test
    fun `should add one bold markup, than another`() {

        val initial = listOf<Mark>()

        val firstBold = Mark(
            type = Mark.Type.BOLD,
            range = 0..2
        )

        val secondBold = Mark(
            type = Mark.Type.BOLD,
            range = 4..6
        )

        val firstMarks = initial.addMark(mark = firstBold)

        assertEquals(
            expected = listOf(firstBold),
            actual = firstMarks
        )

        val secondMarks = firstMarks.addMark(mark = secondBold)

        assertEquals(
            expected = listOf(firstBold, secondBold),
            actual = secondMarks
        )
    }

    @Test
    fun `should undo bold markup`() {

        val bold = Mark(
            type = Mark.Type.BOLD,
            range = 2..5
        )

        val given = listOf(bold)

        val result = given.addMark(bold)

        assertTrue { result.isEmpty() }
    }

    @Test
    fun `should partly undo markup`() {

        val given = listOf(
            Mark(
                type = Mark.Type.BOLD,
                range = 0..5
            )
        )

        val bold = Mark(
            type = Mark.Type.BOLD,
            range = 3..5
        )

        val result = given.addMark(bold)

        val expected = listOf(
            Mark(
                type = Mark.Type.BOLD,
                range = 0..3
            )
        )

        assertEquals(
            actual = result,
            expected = expected
        )
    }

    @Test
    fun `should replace the color already present in markup by the new one`() {

        val range = 0..5

        val given = listOf(
            Mark(
                type = Mark.Type.TEXT_COLOR,
                range = range,
                param = MockDataFactory.randomString()
            )
        )

        val color = Mark(
            type = Mark.Type.TEXT_COLOR,
            range = range,
            param = MockDataFactory.randomString()
        )

        val result = given.addMark(color)

        val expected = listOf(
            Mark(
                type = Mark.Type.TEXT_COLOR,
                range = range,
                param = color.param
            )
        )

        assertEquals(
            actual = result,
            expected = expected
        )
    }

    @Test
    fun `should replace color if a new mark includes an old mark`() {

        val given = listOf(
            Mark(
                type = Mark.Type.TEXT_COLOR,
                range = 1..3,
                param = MockDataFactory.randomString()
            )
        )

        val color = Mark(
            type = Mark.Type.TEXT_COLOR,
            range = 0..4,
            param = MockDataFactory.randomString()
        )

        val result = given.addMark(color)

        val expected = listOf(
            Mark(
                type = Mark.Type.TEXT_COLOR,
                range = 0..4,
                param = color.param
            )
        )

        assertEquals(
            actual = result,
            expected = expected
        )
    }

    @Test
    fun `should colorize only inner range`() {

        val white = "white"
        val yellow = "yellow"

        val given = listOf(
            Mark(
                type = Mark.Type.TEXT_COLOR,
                range = 0..6,
                param = white
            )
        )

        val color = Mark(
            type = Mark.Type.TEXT_COLOR,
            range = 2..4,
            param = yellow
        )

        val result = given.addMark(color)

        val expected = listOf(
            Mark(
                type = Mark.Type.TEXT_COLOR,
                range = 0..2,
                param = white
            ),
            Mark(
                type = Mark.Type.TEXT_COLOR,
                range = 2..4,
                param = yellow
            ),
            Mark(
                type = Mark.Type.TEXT_COLOR,
                range = 4..6,
                param = white
            )
        )

        assertEquals(
            actual = result,
            expected = expected
        )
    }

    @Test
    fun `should not colorize inner range because colors are equal`() {

        val color = MockDataFactory.randomString()

        val given = listOf(
            Mark(
                type = Mark.Type.TEXT_COLOR,
                range = 0..6,
                param = color
            )
        )

        val new = Mark(
            type = Mark.Type.TEXT_COLOR,
            range = 2..4,
            param = color
        )

        val result = given.addMark(new)

        assertEquals(
            actual = result,
            expected = given
        )
    }

    @Test
    fun `should colorize inner-left part`() {

        val white = "white"
        val yellow = "yellow"

        val given = listOf(
            Mark(
                type = Mark.Type.TEXT_COLOR,
                range = 0..6,
                param = white
            )
        )

        val color = Mark(
            type = Mark.Type.TEXT_COLOR,
            range = 0..4,
            param = yellow
        )

        val result = given.addMark(color)

        val expected = listOf(
            Mark(
                type = Mark.Type.TEXT_COLOR,
                range = 0..4,
                param = yellow
            ),
            Mark(
                type = Mark.Type.TEXT_COLOR,
                range = 4..6,
                param = white
            )
        )

        assertEquals(
            actual = result,
            expected = expected
        )
    }

    @Test
    fun `should colorize inner-right part`() {

        val white = "white"
        val yellow = "yellow"

        val given = listOf(
            Mark(
                type = Mark.Type.TEXT_COLOR,
                range = 0..6,
                param = white
            )
        )

        val color = Mark(
            type = Mark.Type.TEXT_COLOR,
            range = 4..6,
            param = yellow
        )

        val result = given.addMark(color)

        val expected = listOf(
            Mark(
                type = Mark.Type.TEXT_COLOR,
                range = 0..4,
                param = white
            ),
            Mark(
                type = Mark.Type.TEXT_COLOR,
                range = 4..6,
                param = yellow
            )
        )

        assertEquals(
            actual = result,
            expected = expected
        )
    }

    @Test
    fun `should not colorize inner-left part because param are equal`() {

        val color = MockDataFactory.randomString()

        val given = listOf(
            Mark(
                type = Mark.Type.TEXT_COLOR,
                range = 0..6,
                param = color
            )
        )

        val new = Mark(
            type = Mark.Type.TEXT_COLOR,
            range = 0..4,
            param = color
        )

        val result = given.addMark(new)

        assertEquals(
            actual = result,
            expected = given
        )
    }

    @Test
    fun `should not colorize inner-right part because param are equal`() {

        val color = MockDataFactory.randomString()

        val given = listOf(
            Mark(
                type = Mark.Type.TEXT_COLOR,
                range = 0..6,
                param = color
            )
        )

        val new = Mark(
            type = Mark.Type.TEXT_COLOR,
            range = 4..6,
            param = color
        )

        val result = given.addMark(new)

        assertEquals(
            actual = result,
            expected = given
        )
    }

    @Test
    fun `should colorize left part because colors are not equal`() {

        val black = "black"
        val yellow = "yellow"

        val given = listOf(
            Mark(
                type = Mark.Type.TEXT_COLOR,
                range = 5..10,
                param = black
            )
        )

        val new = Mark(
            type = Mark.Type.TEXT_COLOR,
            range = 0..7,
            param = yellow
        )

        val result = given.addMark(new)

        val expected = listOf(
            Mark(
                type = Mark.Type.TEXT_COLOR,
                range = 0..7,
                param = yellow
            ),
            Mark(
                type = Mark.Type.TEXT_COLOR,
                range = 7..10,
                param = black
            )
        )

        assertEquals(
            actual = result,
            expected = expected
        )
    }

    @Test
    fun `should merge left part because colors are equal`() {

        val color = MockDataFactory.randomString()

        val given = listOf(
            Mark(
                type = Mark.Type.TEXT_COLOR,
                range = 5..10,
                param = color
            )
        )

        val new = Mark(
            type = Mark.Type.TEXT_COLOR,
            range = 0..7,
            param = color
        )

        val result = given.addMark(new)

        val expected = listOf(
            Mark(
                type = Mark.Type.TEXT_COLOR,
                range = 0..10,
                param = color
            )
        )

        assertEquals(
            actual = result,
            expected = expected
        )
    }

    @Test
    fun `should colorize right part because colors are not equal`() {

        val black = "black"
        val yellow = "yellow"

        val given = listOf(
            Mark(
                type = Mark.Type.TEXT_COLOR,
                range = 0..7,
                param = black
            )
        )

        val new = Mark(
            type = Mark.Type.TEXT_COLOR,
            range = 5..10,
            param = yellow
        )

        val result = given.addMark(new)

        val expected = listOf(
            Mark(
                type = Mark.Type.TEXT_COLOR,
                range = 0..5,
                param = black
            ),
            Mark(
                type = Mark.Type.TEXT_COLOR,
                range = 5..10,
                param = yellow
            )
        )

        assertEquals(
            actual = result,
            expected = expected
        )
    }

    @Test
    fun `should merge right part because colors are equal`() {

        val color = MockDataFactory.randomString()

        val given = listOf(
            Mark(
                type = Mark.Type.TEXT_COLOR,
                range = 0..7,
                param = color
            )
        )

        val new = Mark(
            type = Mark.Type.TEXT_COLOR,
            range = 5..10,
            param = color
        )

        val result = given.addMark(new)

        val expected = listOf(
            Mark(
                type = Mark.Type.TEXT_COLOR,
                range = 0..10,
                param = color
            )
        )

        assertEquals(
            actual = result,
            expected = expected
        )
    }

    @Test
    fun `should return correct overlap value`() {

        checkOverlap(
            pair = Pair(
                0..5,
                0..5
            ),
            expected = Overlap.EQUAL
        )

        checkOverlap(
            pair = Pair(
                0..2,
                3..5
            ),
            expected = Overlap.BEFORE
        )

        checkOverlap(
            pair = Pair(
                3..5,
                0..2
            ),
            expected = Overlap.AFTER
        )

        checkOverlap(
            pair = Pair(
                0..10,
                4..6
            ),
            expected = Overlap.OUTER
        )

        checkOverlap(
            pair = Pair(
                0..10,
                0..5
            ),
            expected = Overlap.OUTER
        )

        checkOverlap(
            pair = Pair(
                0..10,
                9..10
            ),
            expected = Overlap.OUTER
        )

        checkOverlap(
            pair = Pair(
                5..7,
                0..10
            ),
            expected = Overlap.INNER
        )

        checkOverlap(
            pair = Pair(
                0..5,
                0..10
            ),
            expected = Overlap.INNER_LEFT
        )

        checkOverlap(
            pair = Pair(
                5..10,
                0..10
            ),
            expected = Overlap.INNER_RIGHT
        )

        checkOverlap(
            pair = Pair(
                0..7,
                5..10
            ),
            expected = Overlap.LEFT
        )

        checkOverlap(
            pair = Pair(
                5..10,
                0..7
            ),
            expected = Overlap.RIGHT
        )
    }

    @Test
    fun `should correctly process inner-left markup overlapping`() {

        val marks = listOf(
            Mark(
                type = Mark.Type.BOLD,
                range = 0..10
            )
        )

        val new = Mark(
            type = Mark.Type.BOLD,
            range = 0..5
        )

        val result = marks.toggle(
            target = new
        )

        assertEquals(
            actual = result,
            expected = listOf(
                Mark(
                    type = Mark.Type.BOLD,
                    range = 5..10
                )
            )
        )
    }

    @Test
    fun `should correctly process inner-right markup overlapping`() {

        val marks = listOf(
            Mark(
                type = Mark.Type.BOLD,
                range = 0..10
            )
        )

        val new = Mark(
            type = Mark.Type.BOLD,
            range = 5..10
        )

        val result = marks.toggle(
            target = new
        )

        assertEquals(
            actual = result,
            expected = listOf(
                Mark(
                    type = Mark.Type.BOLD,
                    range = 0..5
                )
            )
        )
    }

    @Test
    fun `should correctly process equal-overlapping scenario`() {

        val marks = listOf(
            Mark(
                type = Mark.Type.BOLD,
                range = 0..10
            )
        )

        val new = Mark(
            type = Mark.Type.BOLD,
            range = 0..10
        )

        val result = marks.toggle(
            target = new
        )

        assertEquals(
            actual = result,
            expected = emptyList()
        )
    }

    @Test
    fun `should correctly process outer-overlapping scenario`() {

        val marks = listOf(
            Mark(
                type = Mark.Type.BOLD,
                range = 0..9
            )
        )

        val new = Mark(
            type = Mark.Type.BOLD,
            range = 0..10
        )

        val result = marks.toggle(
            target = new
        )

        assertEquals(
            actual = result,
            expected = listOf(new)
        )
    }

    @Test
    fun `should correctly process inner-overlapping scenario`() {

        val marks = listOf(
            Mark(
                type = Mark.Type.BOLD,
                range = 0..10
            )
        )

        val new = Mark(
            type = Mark.Type.BOLD,
            range = 5..7
        )

        val result = marks.toggle(
            target = new
        )

        val expected = listOf(
            Mark(
                type = Mark.Type.BOLD,
                range = 0..5
            ),
            Mark(
                type = Mark.Type.BOLD,
                range = 7..10
            )
        )

        assertEquals(
            actual = result,
            expected = expected
        )
    }

    @Test
    fun `should correctly process left-overlap scenario`() {

        val marks = listOf(
            Mark(
                type = Mark.Type.BOLD,
                range = 5..10
            )
        )

        val new = Mark(
            type = Mark.Type.BOLD,
            range = 0..7
        )

        val result = marks.toggle(
            target = new
        )

        val expected = listOf(
            Mark(
                type = Mark.Type.BOLD,
                range = 0..10
            )
        )

        assertEquals(
            actual = result,
            expected = expected
        )
    }

    @Test
    fun `should correctly process right-overlap scenario`() {

        val marks = listOf(
            Mark(
                type = Mark.Type.BOLD,
                range = 0..7
            )
        )

        val new = Mark(
            type = Mark.Type.BOLD,
            range = 5..10
        )

        val result = marks.toggle(
            target = new
        )

        val expected = listOf(
            Mark(
                type = Mark.Type.BOLD,
                range = 0..10
            )
        )

        assertEquals(
            actual = result,
            expected = expected
        )
    }

    @Test
    fun `should correctly process after-overlapp scenario`() {

        val marks = listOf(
            Mark(
                type = Mark.Type.BOLD,
                range = 0..2
            )
        )

        val new = Mark(
            type = Mark.Type.BOLD,
            range = 3..5
        )

        val result = marks.toggle(
            target = new
        )

        val expected = marks + listOf(new)

        assertEquals(
            actual = result,
            expected = expected
        )
    }

    @Test
    fun `should correctly process before-overlapp scenario`() {

        val marks = listOf(
            Mark(
                type = Mark.Type.BOLD,
                range = 3..5
            )
        )

        val new = Mark(
            type = Mark.Type.BOLD,
            range = 0..2
        )

        val result = marks.toggle(
            target = new
        )

        val expected = listOf(new) + marks

        assertEquals(
            actual = result,
            expected = expected
        )
    }

    private fun checkOverlap(pair: Pair<IntRange, IntRange>, expected: Overlap) {
        val (a, b) = pair
        val result = a.overlap(b)
        assertEquals(expected = expected, actual = result)
    }

}