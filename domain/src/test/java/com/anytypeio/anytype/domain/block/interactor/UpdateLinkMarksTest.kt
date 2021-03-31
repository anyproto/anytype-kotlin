package com.anytypeio.anytype.domain.block.interactor

import com.anytypeio.anytype.core_models.Block
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class UpdateLinkMarksTest {

    lateinit var updateLinkMarks: UpdateLinkMarks

    @Before
    fun setup() {
        updateLinkMarks = UpdateLinkMarks()
    }

    @Test
    fun `should return updated list without link marks with intersections`() {
        runBlocking {
            val marks = listOf(
                Block.Content.Text.Mark(
                    range = IntRange(0, 5),
                    param = "wwww.yahoo.com",
                    type = Block.Content.Text.Mark.Type.LINK
                ),
                Block.Content.Text.Mark(
                    range = IntRange(6, 16),
                    param = "wwww.google.com",
                    type = Block.Content.Text.Mark.Type.LINK
                ),
                Block.Content.Text.Mark(
                    range = IntRange(18, 24),
                    param = "wwww.yandex.ru",
                    type = Block.Content.Text.Mark.Type.LINK
                ),
                Block.Content.Text.Mark(
                    range = IntRange(21, 56),
                    param = "wwww.allmusic.com",
                    type = Block.Content.Text.Mark.Type.LINK
                )
            )

            val newMark = Block.Content.Text.Mark(
                range = IntRange(4, 20),
                param = "wwww.anytype.io",
                type = Block.Content.Text.Mark.Type.LINK
            )

            val expected = listOf(
                Block.Content.Text.Mark(
                    range = IntRange(21, 56),
                    param = "wwww.allmusic.com",
                    type = Block.Content.Text.Mark.Type.LINK
                ),
                Block.Content.Text.Mark(
                    range = IntRange(4, 20),
                    param = "wwww.anytype.io",
                    type = Block.Content.Text.Mark.Type.LINK
                )
            )

            val result = updateLinkMarks.run(params = UpdateLinkMarks.Params(marks, newMark))

            result.either(
                { Assert.fail() },
                { assertEquals(expected, it) }
            )
        }
    }

    @Test
    fun `should return updated list of marks, when no intersections`() {
        runBlocking {
            val marks = listOf(
                Block.Content.Text.Mark(
                    range = IntRange(0, 5),
                    param = "wwww.yahoo.com",
                    type = Block.Content.Text.Mark.Type.LINK
                ),
                Block.Content.Text.Mark(
                    range = IntRange(6, 16),
                    param = "wwww.google.com",
                    type = Block.Content.Text.Mark.Type.LINK
                ),
                Block.Content.Text.Mark(
                    range = IntRange(18, 24),
                    param = "wwww.yandex.ru",
                    type = Block.Content.Text.Mark.Type.LINK
                )
            )

            val newMark = Block.Content.Text.Mark(
                range = IntRange(25, 35),
                param = "wwww.anytype.io",
                type = Block.Content.Text.Mark.Type.LINK
            )

            val expected = listOf(
                Block.Content.Text.Mark(
                    range = IntRange(0, 5),
                    param = "wwww.yahoo.com",
                    type = Block.Content.Text.Mark.Type.LINK
                ),
                Block.Content.Text.Mark(
                    range = IntRange(6, 16),
                    param = "wwww.google.com",
                    type = Block.Content.Text.Mark.Type.LINK
                ),
                Block.Content.Text.Mark(
                    range = IntRange(18, 24),
                    param = "wwww.yandex.ru",
                    type = Block.Content.Text.Mark.Type.LINK
                ),
                Block.Content.Text.Mark(
                    range = IntRange(25, 35),
                    param = "wwww.anytype.io",
                    type = Block.Content.Text.Mark.Type.LINK
                )
            )

            val result = updateLinkMarks.run(params = UpdateLinkMarks.Params(marks, newMark))

            result.either(
                { Assert.fail() },
                { assertEquals(expected, it) }
            )
        }
    }

    @Test
    fun `should return updated list of marks, when no link marks and no intersections`() {
        runBlocking {
            val marks = listOf(
                Block.Content.Text.Mark(
                    range = IntRange(0, 5),
                    type = Block.Content.Text.Mark.Type.BOLD
                ),
                Block.Content.Text.Mark(
                    range = IntRange(6, 16),
                    type = Block.Content.Text.Mark.Type.STRIKETHROUGH
                ),
                Block.Content.Text.Mark(
                    range = IntRange(18, 24),
                    type = Block.Content.Text.Mark.Type.BOLD
                )
            )

            val newMark = Block.Content.Text.Mark(
                range = IntRange(25, 35),
                param = "wwww.anytype.io",
                type = Block.Content.Text.Mark.Type.LINK
            )

            val expected = listOf(
                Block.Content.Text.Mark(
                    range = IntRange(0, 5),
                    type = Block.Content.Text.Mark.Type.BOLD
                ),
                Block.Content.Text.Mark(
                    range = IntRange(6, 16),
                    type = Block.Content.Text.Mark.Type.STRIKETHROUGH
                ),
                Block.Content.Text.Mark(
                    range = IntRange(18, 24),
                    type = Block.Content.Text.Mark.Type.BOLD
                ),
                Block.Content.Text.Mark(
                    range = IntRange(25, 35),
                    param = "wwww.anytype.io",
                    type = Block.Content.Text.Mark.Type.LINK
                )
            )

            val result = updateLinkMarks.run(params = UpdateLinkMarks.Params(marks, newMark))

            result.either(
                { Assert.fail() },
                { assertEquals(expected, it) }
            )
        }
    }

    @Test
    fun `should return updated list of marks, when no link marks`() {
        runBlocking {
            val marks = listOf(
                Block.Content.Text.Mark(
                    range = IntRange(0, 5),
                    type = Block.Content.Text.Mark.Type.BOLD
                ),
                Block.Content.Text.Mark(
                    range = IntRange(6, 16),
                    type = Block.Content.Text.Mark.Type.STRIKETHROUGH
                ),
                Block.Content.Text.Mark(
                    range = IntRange(18, 24),
                    type = Block.Content.Text.Mark.Type.BOLD
                )
            )

            val newMark = Block.Content.Text.Mark(
                range = IntRange(6, 16),
                param = "wwww.anytype.io",
                type = Block.Content.Text.Mark.Type.LINK
            )

            val expected = listOf(
                Block.Content.Text.Mark(
                    range = IntRange(0, 5),
                    type = Block.Content.Text.Mark.Type.BOLD
                ),
                Block.Content.Text.Mark(
                    range = IntRange(6, 16),
                    type = Block.Content.Text.Mark.Type.STRIKETHROUGH
                ),
                Block.Content.Text.Mark(
                    range = IntRange(18, 24),
                    type = Block.Content.Text.Mark.Type.BOLD
                ),
                Block.Content.Text.Mark(
                    range = IntRange(6, 16),
                    param = "wwww.anytype.io",
                    type = Block.Content.Text.Mark.Type.LINK
                )
            )

            val result = updateLinkMarks.run(params = UpdateLinkMarks.Params(marks, newMark))

            result.either(
                { Assert.fail() },
                { assertEquals(expected, it) }
            )
        }
    }

    @Test
    fun `should return updated list when new mark param is empty`() {
        runBlocking {
            val marks = listOf(
                Block.Content.Text.Mark(
                    range = IntRange(0, 5),
                    param = "wwww.yahoo.com",
                    type = Block.Content.Text.Mark.Type.LINK
                ),
                Block.Content.Text.Mark(
                    range = IntRange(6, 16),
                    param = "wwww.google.com",
                    type = Block.Content.Text.Mark.Type.LINK
                ),
                Block.Content.Text.Mark(
                    range = IntRange(18, 24),
                    param = "wwww.yandex.ru",
                    type = Block.Content.Text.Mark.Type.LINK
                ),
                Block.Content.Text.Mark(
                    range = IntRange(21, 56),
                    param = "wwww.allmusic.com",
                    type = Block.Content.Text.Mark.Type.LINK
                )
            )

            val newMark = Block.Content.Text.Mark(
                range = IntRange(4, 20),
                param = "",
                type = Block.Content.Text.Mark.Type.LINK
            )

            val expected = listOf(
                Block.Content.Text.Mark(
                    range = IntRange(21, 56),
                    param = "wwww.allmusic.com",
                    type = Block.Content.Text.Mark.Type.LINK
                ),
                Block.Content.Text.Mark(
                    range = IntRange(4, 20),
                    param = "",
                    type = Block.Content.Text.Mark.Type.LINK
                )
            )

            val result = updateLinkMarks.run(params = UpdateLinkMarks.Params(marks, newMark))

            result.either(
                { Assert.fail() },
                { assertEquals(expected, it) }
            )
        }
    }
}