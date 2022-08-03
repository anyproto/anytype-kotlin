package com.anytypeio.anytype.presentation.editor.editor.ext

import com.anytypeio.anytype.core_models.ThemeColor
import com.anytypeio.anytype.presentation.editor.editor.Markup
import com.anytypeio.anytype.presentation.editor.editor.model.Alignment
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlin.test.Test
import kotlin.test.assertEquals

class BlockViewCutTextTest {

    @Test
    fun `should cut text and shift marks from texted block view`() {

        val blockView = BlockView.Text.Paragraph(
            id = MockDataFactory.randomUuid(),
            text = "Anytype is a next generation software",
            marks = listOf(
                Markup.Mark.Bold(from = 0, to = 3),
                Markup.Mark.Italic(from = 14, to = 17)
            ),
            isFocused = true,
            cursor = 2,
            color = "red",
            background = ThemeColor.BLUE,
            isSelected = false,
            alignment = Alignment.CENTER,
            indent = 100,
            mode = BlockView.Mode.EDIT
        )

        //TESTING

        val result = blockView.cutPartOfText(
            from = 8,
            partLength = 2
        )

        val expected = BlockView.Text.Paragraph(
            id = blockView.id,
            text = "Anytype  a next generation software",
            marks = listOf(
                Markup.Mark.Bold(from = 0, to = 3),
                Markup.Mark.Italic(from = 12, to = 15)
            ),
            isFocused = true,
            cursor = 8,
            color = "red",
            background = ThemeColor.BLUE,
            isSelected = false,
            alignment = Alignment.CENTER,
            indent = 100,
            mode = BlockView.Mode.EDIT
        )

        assertEquals(expected = expected, actual = result)
    }

    @Test
    fun `should cut text and shift marks from texted block view 2`() {

        val blockView = BlockView.Text.Paragraph(
            id = MockDataFactory.randomUuid(),
            text = "Anytype",
            marks = listOf(),
            isFocused = true,
            cursor = null,
            color = "red",
            background = ThemeColor.BLUE,
            isSelected = false,
            alignment = Alignment.CENTER,
            indent = 100,
            mode = BlockView.Mode.EDIT
        )

        //TESTING

        val result = blockView.cutPartOfText(
            from = 0,
            partLength = 7
        )

        val expected = BlockView.Text.Paragraph(
            id = blockView.id,
            text = "",
            marks = listOf(),
            isFocused = true,
            cursor = 0,
            color = "red",
            background = ThemeColor.BLUE,
            isSelected = false,
            alignment = Alignment.CENTER,
            indent = 100,
            mode = BlockView.Mode.EDIT
        )

        assertEquals(expected = expected, actual = result)
    }

    @Test
    fun `should not cut text and shift marks when part length is 0`() {

        val blockView = BlockView.Text.Paragraph(
            id = MockDataFactory.randomUuid(),
            text = "Anytype is a next generation software",
            marks = listOf(
                Markup.Mark.Bold(from = 0, to = 3),
                Markup.Mark.Italic(from = 14, to = 17)
            ),
            isFocused = true,
            cursor = 2,
            color = "red",
            background = ThemeColor.BLUE,
            isSelected = false,
            alignment = Alignment.CENTER,
            indent = 100,
            mode = BlockView.Mode.EDIT
        )

        //TESTING

        val result = blockView.cutPartOfText(
            from = 8,
            partLength = 0
        )

        val expected = BlockView.Text.Paragraph(
            id = blockView.id,
            text = "Anytype is a next generation software",
            marks = listOf(
                Markup.Mark.Bold(from = 0, to = 3),
                Markup.Mark.Italic(from = 14, to = 17)
            ),
            isFocused = true,
            cursor = 8,
            color = "red",
            background = ThemeColor.BLUE,
            isSelected = false,
            alignment = Alignment.CENTER,
            indent = 100,
            mode = BlockView.Mode.EDIT
        )

        assertEquals(expected = expected, actual = result)
    }

    @Test(expected = IllegalStateException::class)
    fun `should throw exception when partLength is negative`() {

        val blockView = BlockView.Text.Paragraph(
            id = MockDataFactory.randomUuid(),
            text = "Anytype is a next generation software",
            marks = listOf(
                Markup.Mark.Bold(from = 0, to = 3),
                Markup.Mark.Italic(from = 14, to = 17)
            ),
            isFocused = true,
            cursor = 2,
            color = "red",
            background = ThemeColor.BLUE,
            isSelected = false,
            alignment = Alignment.CENTER,
            indent = 100,
            mode = BlockView.Mode.EDIT
        )

        //TESTING

        blockView.cutPartOfText(
            from = 8,
            partLength = -2
        )
    }

    @Test
    fun `should return original text and marks when from is less zero`() {

        val blockView = BlockView.Text.Paragraph(
            id = MockDataFactory.randomUuid(),
            text = "Anytype",
            marks = listOf(
                Markup.Mark.Bold(from = 0, to = 3),
                Markup.Mark.Italic(from = 3, to = 5)
            ),
            isFocused = true,
            cursor = null,
            color = "red",
            background = ThemeColor.BLUE,
            isSelected = false,
            alignment = Alignment.CENTER,
            indent = 100,
            mode = BlockView.Mode.EDIT
        )

        //TESTING

        val result = blockView.cutPartOfText(
            from = -1,
            partLength = 7
        )

        assertEquals(expected = blockView, actual = result)
    }

    @Test
    fun `should return original text and marks when from is bigger then text length`() {

        val blockView = BlockView.Text.Paragraph(
            id = MockDataFactory.randomUuid(),
            text = "Anytype",
            marks = listOf(
                Markup.Mark.Bold(from = 0, to = 3),
                Markup.Mark.Italic(from = 3, to = 5)
            ),
            isFocused = true,
            cursor = null,
            color = "red",
            background = ThemeColor.BLUE,
            isSelected = false,
            alignment = Alignment.CENTER,
            indent = 100,
            mode = BlockView.Mode.EDIT
        )

        //TESTING

        val result = blockView.cutPartOfText(
            from = 8,
            partLength = 1
        )

        assertEquals(expected = blockView, actual = result)
    }

    @Test
    fun `should return original text and marks when to is bigger then text length`() {

        val blockView = BlockView.Text.Paragraph(
            id = MockDataFactory.randomUuid(),
            text = "Anytype",
            marks = listOf(
                Markup.Mark.Bold(from = 0, to = 3),
                Markup.Mark.Italic(from = 3, to = 5)
            ),
            isFocused = true,
            cursor = null,
            color = "red",
            background = ThemeColor.BLUE,
            isSelected = false,
            alignment = Alignment.CENTER,
            indent = 100,
            mode = BlockView.Mode.EDIT
        )

        //TESTING

        val result = blockView.cutPartOfText(
            from = 5,
            partLength = 3
        )

        assertEquals(expected = blockView, actual = result)
    }
}