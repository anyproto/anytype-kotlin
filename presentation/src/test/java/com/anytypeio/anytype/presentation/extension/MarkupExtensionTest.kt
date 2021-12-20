package com.anytypeio.anytype.presentation.extension

import com.anytypeio.anytype.presentation.editor.editor.Markup
import org.junit.Test
import kotlin.test.assertEquals

class MarkupExtensionTest {

    @Test
    fun `should not update marks ranges when length is zero`() {
        val given = listOf(
            Markup.Mark.Bold(
                from = 10,
                to = 15
            )
        )

        val from = 0
        val length = 0

        val result = given.shift(
            from = from,
            length = length
        )

        val expected = listOf(
            Markup.Mark.Bold(
                from = 10,
                to = 15
            )
        )

        assertEquals(
            expected = expected,
            actual = result
        )
    }

    @Test
    fun `should not update marks ranges when no marks after from`() {
        val given = listOf(
            Markup.Mark.Bold(
                from = 0,
                to = 5
            )
        )

        val from = 6
        val length = 13

        val result = given.shift(
            from = from,
            length = length
        )

        val expected = listOf(
            Markup.Mark.Bold(
                from = 0,
                to = 5
            )
        )

        assertEquals(
            expected = expected,
            actual = result
        )
    }

    @Test
    fun `should update marks ranges with length`() {
        val given = listOf(
            Markup.Mark.Bold(
                from = 0,
                to = 5
            ),
            Markup.Mark.Strikethrough(
                from = 23,
                to = 31
            ),
            Markup.Mark.Italic(
                from = 23,
                to = 31
            ),
            Markup.Mark.Link(
                from = 32,
                to = 43,
                param = "https://anytype.io/"
            )
        )

        val from = 6
        val length = 13

        val result = given.shift(
            from = from,
            length = length
        )

        val expected = listOf(
            Markup.Mark.Bold(
                from = 0,
                to = 5
            ),
            Markup.Mark.Strikethrough(
                from = 36,
                to = 44
            ),
            Markup.Mark.Italic(
                from = 36,
                to = 44
            ),
            Markup.Mark.Link(
                from = 45,
                to = 56,
                param = "https://anytype.io/"
            )
        )

        assertEquals(
            expected = expected,
            actual = result
        )
    }

    @Test
    fun `should update marks ranges with length add overlay`() {
        val given = listOf(
            Markup.Mark.Bold(
                from = 0,
                to = 10
            ),
            Markup.Mark.Italic(
                from = 3,
                to = 8
            ),
            Markup.Mark.Strikethrough(
                from = 23,
                to = 31
            ),
            Markup.Mark.Link(
                from = 32,
                to = 43,
                param = "https://anytype.io/"
            )
        )

        val from = 4
        val length = 5

        val result = given.shift(
            from = from,
            length = length
        )

        val expected = listOf(
            Markup.Mark.Bold(
                from = 0,
                to = 15
            ),
            Markup.Mark.Italic(
                from = 3,
                to = 13
            ),
            Markup.Mark.Strikethrough(
                from = 28,
                to = 36
            ),
            Markup.Mark.Link(
                from = 37,
                to = 48,
                param = "https://anytype.io/"
            )
        )

        assertEquals(
            expected = expected,
            actual = result
        )
    }

    @Test
    fun `should update marks ranges with negative length 1`() {
        val given = listOf(
            Markup.Mark.Bold(
                from = 10,
                to = 15
            )
        )

        val from = 4
        val length = -3

        val result = given.shift(
            from = from,
            length = length
        )

        val expected = listOf(
            Markup.Mark.Bold(
                from = 7,
                to = 12
            )
        )

        assertEquals(
            expected = expected,
            actual = result
        )
    }

    @Test
    fun `should update marks ranges with negative length 2`() {
        val given = listOf(
            Markup.Mark.Italic(
                from = 0,
                to = 8
            ),
            Markup.Mark.Bold(
                from = 10,
                to = 15
            )
        )

        val from = 2
        val length = -3

        val result = given.shift(
            from = from,
            length = length
        )

        val expected = listOf(
            Markup.Mark.Italic(
                from = 0,
                to = 5
            ),
            Markup.Mark.Bold(
                from = 7,
                to = 12
            )
        )

        assertEquals(
            expected = expected,
            actual = result
        )
    }

    @Test
    fun `should update marks ranges with negative length 3`() {
        val given = listOf(
            Markup.Mark.Bold(
                from = 0,
                to = 10
            ),
            Markup.Mark.Italic(
                from = 3,
                to = 8
            ),
            Markup.Mark.Strikethrough(
                from = 23,
                to = 31
            ),
            Markup.Mark.Link(
                from = 32,
                to = 43,
                param = "https://anytype.io/"
            )
        )

        val from = 4
        val length = -3

        val result = given.shift(
            from = from,
            length = length
        )

        val expected = listOf(
            Markup.Mark.Bold(
                from = 0,
                to = 7
            ),
            Markup.Mark.Italic(
                from = 3,
                to = 5
            ),
            Markup.Mark.Strikethrough(
                from = 20,
                to = 28
            ),
            Markup.Mark.Link(
                from = 29,
                to = 40,
                param = "https://anytype.io/"
            )
        )

        assertEquals(
            expected = expected,
            actual = result
        )
    }

    @Test
    fun `should update marks ranges with negative length 4`() {
        val given = listOf(
            Markup.Mark.Bold(
                from = 0,
                to = 18
            ),
            Markup.Mark.Mention.Base(
                from = 6,
                to = 12,
                param = "3M6"
            )
        )

        val from = 6
        val length = -1

        val result = given.shift(
            from = from,
            length = length
        )

        val expected = listOf(
            Markup.Mark.Bold(
                from = 0,
                to = 17
            ),
            Markup.Mark.Mention.Base(
                from = 6,
                to = 11,
                param = "3M6"
            )
        )

        assertEquals(
            expected = expected,
            actual = result
        )
    }

    @Test
    fun shouldUpdateMutableListMentions() {

        val mentionTarget = MockDataFactory.randomUuid()

        val marks = mutableListOf(
            Markup.Mark.Mention.WithEmoji(
                from = 6,
                to = 9,
                param = mentionTarget,
                emoji = "\uD83D\uDE01"
            ),
            Markup.Mark.Bold(
                from = 10,
                to = 13
            )
        )

        marks.shift(start = 6, length = 1)

        val expected = mutableListOf(
            Markup.Mark.Mention.WithEmoji(
                from = 6,
                to = 10,
                param = mentionTarget,
                emoji = "\uD83D\uDE01"
            ),
            Markup.Mark.Bold(
                from = 11,
                to = 14
            )
        )

        assertEquals(expected, marks)
    }
}