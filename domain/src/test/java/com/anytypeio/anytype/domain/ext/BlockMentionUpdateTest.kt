package com.anytypeio.anytype.domain.ext

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.domain.common.MockDataFactory
import com.anytypeio.anytype.core_models.ext.addMention
import com.anytypeio.anytype.core_models.ext.replaceRangeWithWord
import org.junit.Test
import kotlin.test.assertEquals

class BlockMentionUpdateTest {

    @Test
    fun `should replace mentionTrigger with mention`() {

        val mentionTrigger = "@r"
        val given = "$mentionTrigger"
        val mention = "replace"
        val from = 0
        val to = from + mentionTrigger.length

        val result = given.replaceRangeWithWord(
            replace = mention,
            from = from,
            to = to
        )

        val expected = "replace"

        assertEquals(
            expected = expected,
            actual = result
        )
    }

    @Test
    fun `should add mention and return block with proper color,background, align props`() {

        val mentionTrigger = "@m"
        val from = 0
        val givenText = "ne"
        val mentionText = "NewPage"
        val mentionHash = MockDataFactory.randomUuid()

        val block = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            content = Block.Content.Text(
                marks = listOf(),
                style = Block.Content.Text.Style.CHECKBOX,
                text = givenText,
                color = "red",
                backgroundColor = "lime",
                align = Block.Align.AlignCenter,
                isChecked = true
            ),
            children = emptyList()
        )

        val result = block.addMention(
            mentionText = mentionText,
            from = from,
            mentionId = mentionHash,
            mentionTrigger = mentionTrigger
        )

        val expected = Block(
            id = block.id,
            fields = block.fields,
            content = Block.Content.Text(
                marks = listOf(
                    Block.Content.Text.Mark(
                        range = IntRange(
                            start = from,
                            endInclusive = from + mentionText.length
                        ),
                        type = Block.Content.Text.Mark.Type.MENTION,
                        param = mentionHash
                    )
                ),
                style = Block.Content.Text.Style.CHECKBOX,
                text = "NewPage ",
                color = "red",
                backgroundColor = "lime",
                align = Block.Align.AlignCenter,
                isChecked = true
            ),
            children = emptyList()
        )

        assertEquals(
            expected = expected,
            actual = result
        )
    }

    @Test
    fun `should replace mentionTrigger with mention 2`() {

        val mentionTrigger = "@r"
        val given = " $mentionTrigger"
        val mention = "replace"
        val from = 1
        val to = from + mentionTrigger.length

        val result = given.replaceRangeWithWord(
            replace = mention,
            from = from,
            to = to
        )

        val expected = " replace"

        assertEquals(
            expected = expected,
            actual = result
        )
    }

    @Test
    fun `should replace mentionTrigger with mention 3`() {

        val mentionTrigger = "@r"
        val given = " $mentionTrigger end"
        val mention = "replace"
        val from = 1
        val to = from + mentionTrigger.length

        val result = given.replaceRangeWithWord(
            replace = mention,
            from = from,
            to = to
        )

        val expected = " replace end"

        assertEquals(
            expected = expected,
            actual = result
        )
    }

    @Test
    fun `should replace mentionTrigger with mention 4`() {

        val mentionTrigger = "@r"
        val given = "start $mentionTrigger"
        val mention = "replace"
        val from = 6
        val to = from + mentionTrigger.length

        val result = given.replaceRangeWithWord(
            replace = mention,
            from = from,
            to = to
        )

        val expected = "start replace"

        assertEquals(
            expected = expected,
            actual = result
        )
    }

    @Test
    fun `should replace mentionTrigger with mention 5`() {

        val mentionTrigger = "@r"
        val given = "start $mentionTrigger end"
        val mention = "replacebigmention"
        val from = 6
        val to = from + mentionTrigger.length

        val result = given.replaceRangeWithWord(
            replace = mention,
            from = from,
            to = to
        )

        val expected = "start replacebigmention end"

        assertEquals(
            expected = expected,
            actual = result
        )
    }

    @Test
    fun `should replace bigger length mentionTrigger`() {

        val mentionTrigger = "@mentionTriggerBiggerThenReplace"
        val given = "start $mentionTrigger end"
        val mention = "replace"
        val from = 6
        val to = from + mentionTrigger.length

        val result = given.replaceRangeWithWord(
            replace = mention,
            from = from,
            to = to
        )

        val expected = "start replace end"

        assertEquals(
            expected = expected,
            actual = result
        )
    }

    @Test(expected = IllegalStateException::class)
    fun `should throw error when ranges is wrong`() {

        val mentionTrigger = "@r"
        val given = " $mentionTrigger end"
        val replace = "replace"
        val from = 10
        val to = from + 10

        given.replaceRangeWithWord(
            replace = replace,
            from = from,
            to = to
        )
    }

    @Test(expected = IllegalStateException::class)
    fun `should throw error when from is wrong`() {

        val mentionTrigger = "@r"
        val given = " $mentionTrigger end"
        val replace = "replace"
        val from = 10
        val to = 2

        given.replaceRangeWithWord(
            replace = replace,
            from = from,
            to = to
        )
    }

    @Test(expected = IllegalStateException::class)
    fun `should throw error when to is wrong`() {

        val mentionTrigger = "@r"
        val given = " $mentionTrigger end"
        val replace = "replace"
        val from = 1
        val to = 22

        given.replaceRangeWithWord(
            replace = replace,
            from = from,
            to = to
        )
    }

    @Test
    fun `should add mention in the middle`() {

        val mentionTrigger = "@m"
        val from = 11
        val givenText = "page about $mentionTrigger music"
        val mentionText = "Avant-Garde Jazz"
        val mentionHash = MockDataFactory.randomUuid()

        val block = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            content = Block.Content.Text(
                marks = listOf(
                    Block.Content.Text.Mark(
                        range = IntRange(
                            start = 0,
                            endInclusive = 3
                        ),
                        type = Block.Content.Text.Mark.Type.BOLD
                    ),
                    Block.Content.Text.Mark(
                        range = IntRange(
                            start = 5,
                            endInclusive = 9
                        ),
                        type = Block.Content.Text.Mark.Type.ITALIC
                    ),
                    Block.Content.Text.Mark(
                        range = IntRange(
                            start = 14,
                            endInclusive = 18
                        ),
                        type = Block.Content.Text.Mark.Type.STRIKETHROUGH
                    )
                ),
                style = Block.Content.Text.Style.P,
                text = givenText
            ),
            children = emptyList()
        )

        val result = block.addMention(
            mentionText = mentionText,
            from = from,
            mentionId = mentionHash,
            mentionTrigger = mentionTrigger
        )

        val expected = Block(
            id = block.id,
            fields = block.fields,
            content = Block.Content.Text(
                marks = listOf(
                    Block.Content.Text.Mark(
                        range = IntRange(
                            start = 0,
                            endInclusive = 3
                        ),
                        type = Block.Content.Text.Mark.Type.BOLD
                    ),
                    Block.Content.Text.Mark(
                        range = IntRange(
                            start = 5,
                            endInclusive = 9
                        ),
                        type = Block.Content.Text.Mark.Type.ITALIC
                    ),
                    Block.Content.Text.Mark(
                        range = IntRange(
                            start = 29,
                            endInclusive = 33
                        ),
                        type = Block.Content.Text.Mark.Type.STRIKETHROUGH
                    ),
                    Block.Content.Text.Mark(
                        range = IntRange(
                            start = from,
                            endInclusive = from + mentionText.length
                        ),
                        type = Block.Content.Text.Mark.Type.MENTION,
                        param = mentionHash
                    )
                ),
                style = Block.Content.Text.Style.P,
                text = "page about Avant-Garde Jazz  music"
            ),
            children = emptyList()
        )

        assertEquals(
            expected = expected,
            actual = result
        )
    }

    @Test
    fun `should add mention at the start`() {

        val mentionTrigger = "@men"
        val from = 0
        val givenText = "$mentionTrigger page about music"
        val mentionText = "Avant-Garde Jazz"
        val mentionHash = MockDataFactory.randomUuid()

        val block = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            content = Block.Content.Text(
                marks = listOf(
                    Block.Content.Text.Mark(
                        range = IntRange(
                            start = 5,
                            endInclusive = 8
                        ),
                        type = Block.Content.Text.Mark.Type.BOLD
                    ),
                    Block.Content.Text.Mark(
                        range = IntRange(
                            start = 10,
                            endInclusive = 14
                        ),
                        type = Block.Content.Text.Mark.Type.ITALIC
                    ),
                    Block.Content.Text.Mark(
                        range = IntRange(
                            start = 16,
                            endInclusive = 20
                        ),
                        type = Block.Content.Text.Mark.Type.STRIKETHROUGH
                    )
                ),
                style = Block.Content.Text.Style.P,
                text = givenText
            ),
            children = emptyList()
        )

        val result = block.addMention(
            mentionText = mentionText,
            from = from,
            mentionId = mentionHash,
            mentionTrigger = mentionTrigger
        )

        val expected = Block(
            id = block.id,
            fields = block.fields,
            content = Block.Content.Text(
                marks = listOf(
                    Block.Content.Text.Mark(
                        range = IntRange(
                            start = 18,
                            endInclusive = 21
                        ),
                        type = Block.Content.Text.Mark.Type.BOLD
                    ),
                    Block.Content.Text.Mark(
                        range = IntRange(
                            start = 23,
                            endInclusive = 27
                        ),
                        type = Block.Content.Text.Mark.Type.ITALIC
                    ),
                    Block.Content.Text.Mark(
                        range = IntRange(
                            start = 29,
                            endInclusive = 33
                        ),
                        type = Block.Content.Text.Mark.Type.STRIKETHROUGH
                    ),
                    Block.Content.Text.Mark(
                        range = IntRange(
                            start = from,
                            endInclusive = from + mentionText.length
                        ),
                        type = Block.Content.Text.Mark.Type.MENTION,
                        param = mentionHash
                    )
                ),
                style = Block.Content.Text.Style.P,
                text = "Avant-Garde Jazz  page about music"
            ),
            children = emptyList()
        )

        assertEquals(
            expected = expected,
            actual = result
        )
    }

    @Test
    fun `should add mention at the start when mentionTrigger bigger then mention`() {

        val mentionTrigger = "@mention"
        val from = 0
        val givenText = "$mentionTrigger page about music"
        val mentionText = "Avant"
        val mentionHash = MockDataFactory.randomUuid()

        val block = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            content = Block.Content.Text(
                marks = listOf(
                    Block.Content.Text.Mark(
                        range = IntRange(
                            start = 9,
                            endInclusive = 12
                        ),
                        type = Block.Content.Text.Mark.Type.BOLD
                    ),
                    Block.Content.Text.Mark(
                        range = IntRange(
                            start = 14,
                            endInclusive = 18
                        ),
                        type = Block.Content.Text.Mark.Type.ITALIC
                    ),
                    Block.Content.Text.Mark(
                        range = IntRange(
                            start = 20,
                            endInclusive = 24
                        ),
                        type = Block.Content.Text.Mark.Type.STRIKETHROUGH
                    )
                ),
                style = Block.Content.Text.Style.P,
                text = givenText
            ),
            children = emptyList()
        )

        val result = block.addMention(
            mentionText = mentionText,
            from = from,
            mentionId = mentionHash,
            mentionTrigger = mentionTrigger
        )

        val expected = Block(
            id = block.id,
            fields = block.fields,
            content = Block.Content.Text(
                marks = listOf(
                    Block.Content.Text.Mark(
                        range = IntRange(
                            start = 7,
                            endInclusive = 10
                        ),
                        type = Block.Content.Text.Mark.Type.BOLD
                    ),
                    Block.Content.Text.Mark(
                        range = IntRange(
                            start = 12,
                            endInclusive = 16
                        ),
                        type = Block.Content.Text.Mark.Type.ITALIC
                    ),
                    Block.Content.Text.Mark(
                        range = IntRange(
                            start = 18,
                            endInclusive = 22
                        ),
                        type = Block.Content.Text.Mark.Type.STRIKETHROUGH
                    ),
                    Block.Content.Text.Mark(
                        range = IntRange(
                            start = from,
                            endInclusive = from + mentionText.length
                        ),
                        type = Block.Content.Text.Mark.Type.MENTION,
                        param = mentionHash
                    )
                ),
                style = Block.Content.Text.Style.P,
                text = "Avant  page about music"
            ),
            children = emptyList()
        )

        assertEquals(
            expected = expected,
            actual = result
        )
    }

    @Test
    fun `should add mention at the end`() {

        val mentionTrigger = "@avan"
        val from = 11
        val givenText = "page about $mentionTrigger"
        val mentionText = "Avant-Garde Jazz"
        val mentionHash = MockDataFactory.randomUuid()

        val block = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            content = Block.Content.Text(
                marks = listOf(
                    Block.Content.Text.Mark(
                        range = IntRange(
                            start = 0,
                            endInclusive = 3
                        ),
                        type = Block.Content.Text.Mark.Type.BOLD
                    ),
                    Block.Content.Text.Mark(
                        range = IntRange(
                            start = 5,
                            endInclusive = 9
                        ),
                        type = Block.Content.Text.Mark.Type.ITALIC
                    )
                ),
                style = Block.Content.Text.Style.P,
                text = givenText
            ),
            children = emptyList()
        )

        val result = block.addMention(
            mentionText = mentionText,
            from = from,
            mentionId = mentionHash,
            mentionTrigger = mentionTrigger
        )

        val expected = Block(
            id = block.id,
            fields = block.fields,
            content = Block.Content.Text(
                marks = listOf(
                    Block.Content.Text.Mark(
                        range = IntRange(
                            start = 0,
                            endInclusive = 3
                        ),
                        type = Block.Content.Text.Mark.Type.BOLD
                    ),
                    Block.Content.Text.Mark(
                        range = IntRange(
                            start = 5,
                            endInclusive = 9
                        ),
                        type = Block.Content.Text.Mark.Type.ITALIC
                    ),
                    Block.Content.Text.Mark(
                        range = IntRange(
                            start = from,
                            endInclusive = from + mentionText.length
                        ),
                        type = Block.Content.Text.Mark.Type.MENTION,
                        param = mentionHash
                    )
                ),
                style = Block.Content.Text.Style.P,
                text = "page about Avant-Garde Jazz "
            ),
            children = emptyList()
        )

        assertEquals(
            expected = expected,
            actual = result
        )
    }
}