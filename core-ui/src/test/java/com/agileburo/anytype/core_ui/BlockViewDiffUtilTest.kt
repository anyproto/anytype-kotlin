package com.agileburo.anytype.core_ui

import com.agileburo.anytype.core_ui.common.Markup
import com.agileburo.anytype.core_ui.features.page.BlockView
import com.agileburo.anytype.core_ui.features.page.BlockViewDiffUtil
import com.agileburo.anytype.core_ui.features.page.BlockViewDiffUtil.Companion.MARKUP_CHANGED
import com.agileburo.anytype.core_ui.features.page.BlockViewDiffUtil.Companion.TEXT_CHANGED
import com.agileburo.anytype.core_ui.features.page.BlockViewDiffUtil.Payload
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class BlockViewDiffUtilTest {

    @Test
    fun `two blocks should be considered different based on their id`() {

        val index = 0

        val oldBlock = BlockView.Paragraph(
            id = MockDataFactory.randomUuid(),
            text = MockDataFactory.randomString()
        )

        val newBlock = oldBlock.copy(id = MockDataFactory.randomUuid())

        val old = listOf(oldBlock)

        val new = listOf(newBlock)

        val diff = BlockViewDiffUtil(old = old, new = new)

        assertEquals(expected = false, actual = diff.areItemsTheSame(index, index))
    }

    @Test
    fun `two blocks should be considered the same by their id but different by their content`() {

        val index = 0

        val id = MockDataFactory.randomUuid()

        val oldBlock: BlockView = BlockView.Paragraph(
            id = id,
            text = MockDataFactory.randomString()
        )

        val newBlock: BlockView = BlockView.Paragraph(
            id = id,
            text = MockDataFactory.randomString()
        )

        val old = listOf(oldBlock)

        val new = listOf(newBlock)

        val diff = BlockViewDiffUtil(old = old, new = new)

        assertEquals(expected = true, actual = diff.areItemsTheSame(index, index))
        assertEquals(expected = false, actual = diff.areContentsTheSame(index, index))
    }

    @Test
    fun `two blocks should be considered different based only on their UI-representation`() {

        val index = 0
        val indent = 0

        val id = MockDataFactory.randomUuid()
        val text = MockDataFactory.randomString()

        val oldBlock = BlockView.Paragraph(
            id = id,
            text = text,
            indent = indent
        )

        val newBlock = BlockView.HeaderOne(
            id = id,
            text = text,
            indent = indent
        )

        val old = listOf(oldBlock)

        val new = listOf(newBlock)

        val diff = BlockViewDiffUtil(old = old, new = new)

        assertEquals(expected = true, actual = diff.areItemsTheSame(index, index))
        assertEquals(expected = false, actual = diff.areContentsTheSame(index, index))
    }

    @Test
    fun `should return change payload containing only marks because text did not change`() {

        val index = 0

        val id = MockDataFactory.randomUuid()

        val text = MockDataFactory.randomString()

        val oldBlock: BlockView = BlockView.Paragraph(
            id = id,
            text = text,
            marks = emptyList()
        )

        val newBlock: BlockView = BlockView.Paragraph(
            id = id,
            text = text,
            marks = listOf(
                Markup.Mark(
                    type = Markup.Type.BOLD,
                    from = MockDataFactory.randomInt(),
                    to = MockDataFactory.randomInt()
                )
            )
        )

        val old = listOf(oldBlock)

        val new = listOf(newBlock)

        val diff = BlockViewDiffUtil(old = old, new = new)

        val payload = diff.getChangePayload(index, index)

        assertEquals(
            actual = payload,
            expected = Payload(listOf(MARKUP_CHANGED))
        )
    }

    @Test
    fun `should return change payload containing only text because marks did not change`() {

        val index = 0

        val id = MockDataFactory.randomUuid()

        val marks = listOf(
            Markup.Mark(
                type = Markup.Type.BOLD,
                from = MockDataFactory.randomInt(),
                to = MockDataFactory.randomInt()
            )
        )

        val oldBlock: BlockView = BlockView.Paragraph(
            id = id,
            text = MockDataFactory.randomString(),
            marks = marks
        )

        val newBlock: BlockView = BlockView.Paragraph(
            id = id,
            text = MockDataFactory.randomString(),
            marks = marks
        )

        val old = listOf(oldBlock)

        val new = listOf(newBlock)

        val diff = BlockViewDiffUtil(old = old, new = new)

        val payload = diff.getChangePayload(index, index)

        assertEquals(
            actual = payload,
            expected = Payload(listOf(TEXT_CHANGED))
        )
    }

    @Test
    fun `should return change payload containing text and marks because both changed`() {

        val index = 0

        val id = MockDataFactory.randomUuid()

        val oldBlock: BlockView = BlockView.Paragraph(
            id = id,
            text = MockDataFactory.randomString(),
            marks = emptyList()
        )

        val newBlock: BlockView = BlockView.Paragraph(
            id = id,
            text = MockDataFactory.randomString(),
            marks = listOf(
                Markup.Mark(
                    type = Markup.Type.BOLD,
                    from = MockDataFactory.randomInt(),
                    to = MockDataFactory.randomInt()
                )
            )
        )

        val old = listOf(oldBlock)

        val new = listOf(newBlock)

        val diff = BlockViewDiffUtil(old = old, new = new)

        val payload = diff.getChangePayload(index, index)

        assertEquals(
            actual = payload,
            expected = Payload(listOf(TEXT_CHANGED, MARKUP_CHANGED))
        )
    }

    @Test
    fun `should return empty payload if types differ`() {

        val index = 0

        val id = MockDataFactory.randomUuid()

        val indent = 0

        val text = MockDataFactory.randomString()

        val oldBlock: BlockView = BlockView.HeaderOne(
            id = id,
            text = text,
            indent = indent
        )

        val newBlock: BlockView = BlockView.HeaderOne(
            id = id,
            text = text,
            indent = indent
        )

        val old = listOf(oldBlock)

        val new = listOf(newBlock)

        val diff = BlockViewDiffUtil(old = old, new = new)

        val payload = diff.getChangePayload(index, index)

        assertNull(actual = payload)
    }

    @Test
    fun `there should be a number change detected`() {

        val index = 0

        val id = MockDataFactory.randomUuid()

        val text = MockDataFactory.randomString()

        val oldBlock = BlockView.Numbered(
            id = id,
            text = text,
            marks = emptyList(),
            number = 1,
            focused = MockDataFactory.randomBoolean(),
            indent = MockDataFactory.randomInt()
        )

        val newBlock: BlockView = oldBlock.copy(
            number = 2
        )

        val old = listOf(oldBlock)

        val new = listOf(newBlock)

        val diff = BlockViewDiffUtil(old = old, new = new)

        val payload = diff.getChangePayload(index, index)

        val expected = Payload(
            changes = listOf(BlockViewDiffUtil.NUMBER_CHANGED)
        )

        assertEquals(
            expected = expected,
            actual = payload
        )
    }

    @Test
    fun `should return change payload containing background-color update`() {

        val index = 0

        val id = MockDataFactory.randomUuid()

        val text = MockDataFactory.randomString()

        val oldBlock = BlockView.Paragraph(
            id = id,
            text = text,
            marks = emptyList(),
            focused = MockDataFactory.randomBoolean(),
            backgroundColor = null
        )

        val newBlock: BlockView = oldBlock.copy(
            backgroundColor = MockDataFactory.randomString()
        )

        val old = listOf(oldBlock)

        val new = listOf(newBlock)

        val diff = BlockViewDiffUtil(old = old, new = new)

        val payload = diff.getChangePayload(index, index)

        val expected = Payload(
            changes = listOf(BlockViewDiffUtil.BACKGROUND_COLOR_CHANGED)
        )

        assertEquals(
            expected = expected,
            actual = payload
        )
    }

    @Test
    fun `should detect indent change for a paragraph`() {

        val index = 0

        val id = MockDataFactory.randomUuid()

        val text = MockDataFactory.randomString()

        val oldBlock = BlockView.Paragraph(
            id = id,
            text = text,
            marks = emptyList(),
            indent = 0,
            focused = MockDataFactory.randomBoolean(),
            backgroundColor = null,
            color = null
        )

        val newBlock: BlockView = oldBlock.copy(
            indent = 1
        )

        val old = listOf(oldBlock)

        val new = listOf(newBlock)

        val diff = BlockViewDiffUtil(old = old, new = new)

        val payload = diff.getChangePayload(index, index)

        val expected = Payload(
            changes = listOf(BlockViewDiffUtil.INDENT_CHANGED)
        )

        assertEquals(
            expected = expected,
            actual = payload
        )
    }

    @Test
    fun `should detect toggle empty state change for a paragraph`() {

        val index = 0

        val id = MockDataFactory.randomUuid()

        val text = MockDataFactory.randomString()

        val oldBlock = BlockView.Toggle(
            id = id,
            text = text,
            marks = emptyList(),
            indent = 0,
            focused = MockDataFactory.randomBoolean(),
            backgroundColor = null,
            color = null,
            isEmpty = true
        )

        val newBlock: BlockView = oldBlock.copy(
            isEmpty = false
        )

        val old = listOf(oldBlock)

        val new = listOf(newBlock)

        val diff = BlockViewDiffUtil(old = old, new = new)

        val payload = diff.getChangePayload(index, index)

        val expected = Payload(
            changes = listOf(BlockViewDiffUtil.TOGGLE_EMPTY_STATE_CHANGED)
        )

        assertEquals(
            expected = expected,
            actual = payload
        )
    }
}