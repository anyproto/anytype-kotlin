package com.anytypeio.anytype.domain.block.interactor

import com.anytypeio.anytype.core_models.Block
import kotlinx.coroutines.runBlocking
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

class RemoveLinkMarkTest {

    lateinit var removeLinkMark: RemoveLinkMark

    @Before
    fun setup() {
        removeLinkMark = RemoveLinkMark()
    }

    @Test
    fun `should remove link mark with range`() {
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

            val range = IntRange(6, 16)

            val expected = listOf(
                Block.Content.Text.Mark(
                    range = IntRange(0, 5),
                    param = "wwww.yahoo.com",
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

            val result = removeLinkMark.run(params = RemoveLinkMark.Params(marks, range))

            result.either(
                { fail() },
                { kotlin.test.assertEquals(expected, it) }
            )
        }
    }

    @Test
    fun `should not remove link mark`() {
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
                    type = Block.Content.Text.Mark.Type.BOLD
                ),
                Block.Content.Text.Mark(
                    range = IntRange(110, 220),
                    type = Block.Content.Text.Mark.Type.STRIKETHROUGH
                )
            )

            val range = IntRange(110, 220)

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
                    type = Block.Content.Text.Mark.Type.BOLD
                ),
                Block.Content.Text.Mark(
                    range = IntRange(110, 220),
                    type = Block.Content.Text.Mark.Type.STRIKETHROUGH
                )
            )

            val result = removeLinkMark.run(params = RemoveLinkMark.Params(marks, range))

            result.either(
                { fail() },
                { kotlin.test.assertEquals(expected, it) }
            )
        }
    }

    @Test
    fun `should remove object mark with range`() {
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
                    type = Block.Content.Text.Mark.Type.OBJECT
                ),
                Block.Content.Text.Mark(
                    range = IntRange(18, 24),
                    param = "wwww.yandex.ru",
                    type = Block.Content.Text.Mark.Type.LINK
                ),
                Block.Content.Text.Mark(
                    range = IntRange(21, 56),
                    type = Block.Content.Text.Mark.Type.BOLD
                )
            )

            val range = IntRange(6, 16)

            val expected = listOf(
                Block.Content.Text.Mark(
                    range = IntRange(0, 5),
                    param = "wwww.yahoo.com",
                    type = Block.Content.Text.Mark.Type.LINK
                ),
                Block.Content.Text.Mark(
                    range = IntRange(18, 24),
                    param = "wwww.yandex.ru",
                    type = Block.Content.Text.Mark.Type.LINK
                ),
                Block.Content.Text.Mark(
                    range = IntRange(21, 56),
                    type = Block.Content.Text.Mark.Type.BOLD
                )
            )

            val result = removeLinkMark.run(params = RemoveLinkMark.Params(marks, range))

            result.either(
                { fail() },
                { kotlin.test.assertEquals(expected, it) }
            )
        }
    }
}