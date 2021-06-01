package com.anytypeio.anytype.presentation.page.editor.ext

import MockDataFactory
import com.anytypeio.anytype.presentation.page.editor.Markup
import com.anytypeio.anytype.presentation.page.editor.model.Alignment
import com.anytypeio.anytype.presentation.page.editor.model.BlockView
import kotlin.test.Test
import kotlin.test.assertEquals

class BlockViewCutTextTest {

    @Test
    fun `should cut text and shift marks from texted block view`() {

        val blockView = BlockView.Text.Paragraph(
            id = MockDataFactory.randomUuid(),
            text = "Anytype is a next generation software",
            marks = listOf(
                Markup.Mark(from = 0, to = 3, type = Markup.Type.BOLD),
                Markup.Mark(from = 14, to = 17, type = Markup.Type.ITALIC)
            ),
            isFocused = true,
            cursor = 2,
            color = "red",
            backgroundColor = "blue",
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
                Markup.Mark(from = 0, to = 3, type = Markup.Type.BOLD),
                Markup.Mark(from = 12, to = 15, type = Markup.Type.ITALIC)
            ),
            isFocused = true,
            cursor = 2,
            color = "red",
            backgroundColor = "blue",
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
            backgroundColor = "blue",
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
            cursor = null,
            color = "red",
            backgroundColor = "blue",
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
                Markup.Mark(from = 0, to = 3, type = Markup.Type.BOLD),
                Markup.Mark(from = 14, to = 17, type = Markup.Type.ITALIC)
            ),
            isFocused = true,
            cursor = 2,
            color = "red",
            backgroundColor = "blue",
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
                Markup.Mark(from = 0, to = 3, type = Markup.Type.BOLD),
                Markup.Mark(from = 14, to = 17, type = Markup.Type.ITALIC)
            ),
            isFocused = true,
            cursor = 2,
            color = "red",
            backgroundColor = "blue",
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
                Markup.Mark(from = 0, to = 3, type = Markup.Type.BOLD),
                Markup.Mark(from = 14, to = 17, type = Markup.Type.ITALIC)
            ),
            isFocused = true,
            cursor = 2,
            color = "red",
            backgroundColor = "blue",
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

    @Test(expected = IllegalStateException::class)
    fun `should throw exception when from is negative`() {

        val blockView = BlockView.Text.Paragraph(
            id = MockDataFactory.randomUuid(),
            text = "Anytype",
            marks = listOf(),
            isFocused = true,
            cursor = null,
            color = "red",
            backgroundColor = "blue",
            isSelected = false,
            alignment = Alignment.CENTER,
            indent = 100,
            mode = BlockView.Mode.EDIT
        )

        //TESTING

        blockView.cutPartOfText(
            from = -1,
            partLength = 7
        )
    }
}