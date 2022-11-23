package com.anytypeio.anytype.core_ui

import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.ThemeColor
import com.anytypeio.anytype.core_ui.features.editor.BlockViewDiffUtil
import com.anytypeio.anytype.core_ui.features.editor.BlockViewDiffUtil.Companion.CALLOUT_ICON_CHANGED
import com.anytypeio.anytype.core_ui.features.editor.BlockViewDiffUtil.Companion.DECORATION_CHANGED
import com.anytypeio.anytype.core_ui.features.editor.BlockViewDiffUtil.Companion.MARKUP_CHANGED
import com.anytypeio.anytype.core_ui.features.editor.BlockViewDiffUtil.Companion.TABLE_CELLS_CHANGED
import com.anytypeio.anytype.core_ui.features.editor.BlockViewDiffUtil.Companion.TABLE_CELLS_SELECTION_CHANGED
import com.anytypeio.anytype.core_ui.features.editor.BlockViewDiffUtil.Companion.TEXT_CHANGED
import com.anytypeio.anytype.core_ui.features.editor.BlockViewDiffUtil.Payload
import com.anytypeio.anytype.presentation.editor.editor.Markup
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.relations.DocumentRelationView
import com.anytypeio.anytype.presentation.sets.model.FileView
import com.anytypeio.anytype.presentation.sets.model.ObjectView
import com.anytypeio.anytype.presentation.sets.model.StatusView
import com.anytypeio.anytype.presentation.sets.model.TagView
import com.anytypeio.anytype.test_utils.MockDataFactory
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class BlockViewDiffUtilTest {

    @Test
    fun `two blocks should be considered different based on their id`() {

        val index = 0

        val oldBlock = BlockView.Text.Paragraph(
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

        val oldBlock: BlockView = BlockView.Text.Paragraph(
            id = id,
            text = MockDataFactory.randomString()
        )

        val newBlock: BlockView = BlockView.Text.Paragraph(
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

        val oldBlock = BlockView.Text.Paragraph(
            id = id,
            text = text,
            indent = indent
        )

        val newBlock = BlockView.Text.Header.One(
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

        val oldBlock: BlockView = BlockView.Text.Paragraph(
            id = id,
            text = text,
            marks = emptyList()
        )

        val newBlock: BlockView = BlockView.Text.Paragraph(
            id = id,
            text = text,
            marks = listOf(
                Markup.Mark.Bold(
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
            Markup.Mark.Bold(
                from = MockDataFactory.randomInt(),
                to = MockDataFactory.randomInt()
            )
        )

        val oldBlock: BlockView = BlockView.Text.Paragraph(
            id = id,
            text = MockDataFactory.randomString(),
            marks = marks
        )

        val newBlock: BlockView = BlockView.Text.Paragraph(
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

        val oldBlock: BlockView = BlockView.Text.Paragraph(
            id = id,
            text = MockDataFactory.randomString(),
            marks = emptyList()
        )

        val newBlock: BlockView = BlockView.Text.Paragraph(
            id = id,
            text = MockDataFactory.randomString(),
            marks = listOf(
                Markup.Mark.Bold(
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

        val oldBlock: BlockView = BlockView.Text.Header.One(
            id = id,
            text = text,
            indent = indent
        )

        val newBlock: BlockView = BlockView.Text.Header.One(
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

        val oldBlock = BlockView.Text.Numbered(
            id = id,
            text = text,
            marks = emptyList(),
            number = 1,
            isFocused = MockDataFactory.randomBoolean(),
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

        val oldBlock = BlockView.Text.Paragraph(
            id = id,
            text = text,
            marks = emptyList(),
            isFocused = MockDataFactory.randomBoolean()
        )

        val newBlock: BlockView = oldBlock.copy(
            background = ThemeColor.BLUE
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

        val oldBlock = BlockView.Text.Paragraph(
            id = id,
            text = text,
            marks = emptyList(),
            indent = 0,
            isFocused = MockDataFactory.randomBoolean()
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

        val oldBlock = BlockView.Text.Toggle(
            id = id,
            text = text,
            marks = emptyList(),
            indent = 0,
            isFocused = MockDataFactory.randomBoolean(),
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

    @Test
    fun `should detect toggle state change`() {

        val index = 0

        val id = MockDataFactory.randomUuid()

        val text = MockDataFactory.randomString()

        val oldBlock = BlockView.Text.Toggle(
            id = id,
            text = text,
            toggled = false
        )

        val newBlock: BlockView = oldBlock.copy(
            toggled = true
        )

        val old = listOf(oldBlock)

        val new = listOf(newBlock)

        val diff = BlockViewDiffUtil(old = old, new = new)

        val payload = diff.getChangePayload(index, index)

        val expected = Payload(
            changes = listOf(BlockViewDiffUtil.TOGGLE_STATE_CHANGED)
        )

        assertEquals(
            expected = expected,
            actual = payload
        )
    }

    @Test
    fun `should detect focus change for title block`() {

        val index = 0

        val id = MockDataFactory.randomUuid()

        val text = MockDataFactory.randomString()

        val oldBlock = BlockView.Title.Basic(
            id = id,
            text = text,
            isFocused = false
        )

        val newBlock: BlockView = oldBlock.copy(
            isFocused = true
        )

        val old = listOf(oldBlock)

        val new = listOf(newBlock)

        val diff = BlockViewDiffUtil(old = old, new = new)

        val payload = diff.getChangePayload(index, index)

        val expected = Payload(
            changes = listOf(BlockViewDiffUtil.FOCUS_CHANGED)
        )

        assertEquals(
            expected = expected,
            actual = payload
        )
    }

    @Test
    fun `should detect read-write mode changes in paragraph`() {

        val index = 0

        val id = MockDataFactory.randomUuid()

        val text = MockDataFactory.randomString()

        val oldBlock = BlockView.Text.Paragraph(
            id = id,
            text = text,
            mode = BlockView.Mode.EDIT
        )

        val newBlock: BlockView = oldBlock.copy(
            mode = BlockView.Mode.READ
        )

        val old = listOf(oldBlock)

        val new = listOf(newBlock)

        val diff = BlockViewDiffUtil(old = old, new = new)

        val payload = diff.getChangePayload(index, index)

        val expected = Payload(
            changes = listOf(BlockViewDiffUtil.READ_WRITE_MODE_CHANGED)
        )

        assertEquals(
            expected = expected,
            actual = payload
        )
    }

    @Test
    fun `should detect read-write mode changes in title`() {

        val index = 0

        val id = MockDataFactory.randomUuid()

        val text = MockDataFactory.randomString()

        val oldBlock = BlockView.Title.Basic(
            id = id,
            text = text,
            mode = BlockView.Mode.EDIT,
            isFocused = false
        )

        val newBlock: BlockView = oldBlock.copy(
            mode = BlockView.Mode.READ
        )

        val old = listOf(oldBlock)

        val new = listOf(newBlock)

        val diff = BlockViewDiffUtil(old = old, new = new)

        val payload = diff.getChangePayload(index, index)

        val expected = Payload(
            changes = listOf(BlockViewDiffUtil.READ_WRITE_MODE_CHANGED)
        )

        assertEquals(
            expected = expected,
            actual = payload
        )
    }

    @Test
    fun `should detect read-write mode changes in description`() {

        val index = 0

        val id = MockDataFactory.randomUuid()

        val text = MockDataFactory.randomString()

        val oldBlock = BlockView.Description(
            id = id,
            text = text,
            mode = BlockView.Mode.EDIT
        )

        val newBlock: BlockView = oldBlock.copy(
            mode = BlockView.Mode.READ
        )

        val old = listOf(oldBlock)

        val new = listOf(newBlock)

        val diff = BlockViewDiffUtil(old = old, new = new)

        val payload = diff.getChangePayload(index, index)

        val expected = Payload(
            changes = listOf(BlockViewDiffUtil.READ_WRITE_MODE_CHANGED)
        )

        assertEquals(
            expected = expected,
            actual = payload
        )
    }

    @Test
    fun `should detect selection changes in paragraph`() {

        val index = 0

        val id = MockDataFactory.randomUuid()

        val text = MockDataFactory.randomString()

        val oldBlock = BlockView.Text.Paragraph(
            id = id,
            text = text,
            isSelected = true
        )

        val newBlock: BlockView = oldBlock.copy(
            isSelected = false
        )

        val old = listOf(oldBlock)

        val new = listOf(newBlock)

        val diff = BlockViewDiffUtil(old = old, new = new)

        val payload = diff.getChangePayload(index, index)

        val expected = Payload(
            changes = listOf(BlockViewDiffUtil.SELECTION_CHANGED)
        )

        assertEquals(
            expected = expected,
            actual = payload
        )
    }

    @Test
    fun `should detect selection changes in file view`() {

        val index = 0

        val id = MockDataFactory.randomUuid()

        val oldBlock = BlockView.Media.File(
            id = id,
            hash = MockDataFactory.randomString(),
            indent = MockDataFactory.randomInt(),
            mime = MockDataFactory.randomString(),
            size = MockDataFactory.randomLong(),
            name = MockDataFactory.randomString(),
            url = MockDataFactory.randomString(),
            isSelected = false
        )

        val newBlock: BlockView = oldBlock.copy(
            isSelected = true
        )

        val old = listOf(oldBlock)

        val new = listOf(newBlock)

        val diff = BlockViewDiffUtil(old = old, new = new)

        val payload = diff.getChangePayload(index, index)

        val expected = Payload(
            changes = listOf(BlockViewDiffUtil.SELECTION_CHANGED)
        )

        assertEquals(
            expected = expected,
            actual = payload
        )
    }

    @Test
    fun `should detect selection changes in page view`() {

        val index = 0

        val id = MockDataFactory.randomUuid()

        val oldBlock = BlockView.LinkToObject.Default.Text(
            id = id,
            indent = MockDataFactory.randomInt(),
            isSelected = false,
            icon = ObjectIcon.None
        )

        val newBlock: BlockView = oldBlock.copy(
            isSelected = true
        )

        val old = listOf(oldBlock)

        val new = listOf(newBlock)

        val diff = BlockViewDiffUtil(old = old, new = new)

        val payload = diff.getChangePayload(index, index)

        val expected = Payload(
            changes = listOf(BlockViewDiffUtil.SELECTION_CHANGED)
        )

        assertEquals(
            expected = expected,
            actual = payload
        )
    }

    @Test
    fun `should detect selection changes in bookmark view`() {

        val index = 0

        val id = MockDataFactory.randomUuid()

        val oldBlock = BlockView.Media.Bookmark(
            id = id,
            description = MockDataFactory.randomString(),
            faviconUrl = MockDataFactory.randomString(),
            imageUrl = MockDataFactory.randomString(),
            indent = MockDataFactory.randomInt(),
            title = MockDataFactory.randomString(),
            url = MockDataFactory.randomString(),
            isSelected = false,
            isPreviousBlockMedia = false
        )

        val newBlock: BlockView = oldBlock.copy(
            isSelected = true
        )

        val old = listOf(oldBlock)

        val new = listOf(newBlock)

        val diff = BlockViewDiffUtil(old = old, new = new)

        val payload = diff.getChangePayload(index, index)

        val expected = Payload(
            changes = listOf(BlockViewDiffUtil.SELECTION_CHANGED)
        )

        assertEquals(
            expected = expected,
            actual = payload
        )
    }

    @Test
    fun `should detect cursor change in paragraph view`() {

        val index = 0

        val id = MockDataFactory.randomUuid()

        val oldBlock = BlockView.Text.Paragraph(
            id = id,
            text = MockDataFactory.randomString(),
            cursor = null
        )

        val newBlock: BlockView = oldBlock.copy(
            cursor = 2
        )

        val old = listOf(oldBlock)

        val new = listOf(newBlock)

        val diff = BlockViewDiffUtil(old = old, new = new)

        val payload = diff.getChangePayload(index, index)

        val expected = Payload(
            changes = listOf(BlockViewDiffUtil.CURSOR_CHANGED)
        )

        assertEquals(
            expected = expected,
            actual = payload
        )
    }

    @Test
    fun `should detect cursor change in title view`() {

        val index = 0

        val id = MockDataFactory.randomUuid()

        val oldBlock = BlockView.Title.Basic(
            id = id,
            text = MockDataFactory.randomString(),
            cursor = null,
            isFocused = true
        )

        val newBlock: BlockView = oldBlock.copy(
            cursor = 2
        )

        val old = listOf(oldBlock)

        val new = listOf(newBlock)

        val diff = BlockViewDiffUtil(old = old, new = new)

        val payload = diff.getChangePayload(index, index)

        val expected = Payload(
            changes = listOf(BlockViewDiffUtil.CURSOR_CHANGED)
        )

        assertEquals(
            expected = expected,
            actual = payload
        )
    }

    @Test
    fun `should detect icon change in page title block`() {

        val index = 0

        val id = MockDataFactory.randomUuid()

        val oldBlock = BlockView.Title.Basic(
            id = id,
            text = MockDataFactory.randomString(),
            image = MockDataFactory.randomUuid()
        )

        val newBlock: BlockView = oldBlock.copy(
            image = null,
            emoji = MockDataFactory.randomUuid()
        )

        val old = listOf(oldBlock)

        val new = listOf(newBlock)

        val diff = BlockViewDiffUtil(old = old, new = new)

        val payload = diff.getChangePayload(index, index)

        val expected = Payload(
            changes = listOf(BlockViewDiffUtil.TITLE_ICON_CHANGED)
        )

        assertEquals(
            expected = expected,
            actual = payload
        )
    }

    @Test
    fun `should detect icon change in profile title block`() {

        val index = 0

        val id = MockDataFactory.randomUuid()

        val oldBlock = BlockView.Title.Profile(
            id = id,
            text = MockDataFactory.randomString(),
            image = MockDataFactory.randomUuid(),
            isFocused = false
        )

        val newBlock: BlockView = oldBlock.copy(
            image = null,
        )

        val old = listOf(oldBlock)

        val new = listOf(newBlock)

        val diff = BlockViewDiffUtil(old = old, new = new)

        val payload = diff.getChangePayload(index, index)

        val expected = Payload(
            changes = listOf(BlockViewDiffUtil.TITLE_ICON_CHANGED)
        )

        assertEquals(
            expected = expected,
            actual = payload
        )
    }

//    @Test
//    fun `should detect loading-state change in link block`() {
//
//        val index = 0
//
//        val id = MockDataFactory.randomUuid()
//
//        val oldBlock = BlockView.LinkToObject.Loading(
//            id = id
//        )
//
//        val newBlock: BlockView = oldBlock.copy(
//            isLoading = false
//        )
//
//        val old = listOf(oldBlock)
//
//        val new = listOf(newBlock)
//
//        val diff = BlockViewDiffUtil(old = old, new = new)
//
//        val payload = diff.getChangePayload(index, index)
//
//        val expected = Payload(
//            changes = listOf(BlockViewDiffUtil.LOADING_STATE_CHANGED)
//        )
//
//        assertEquals(
//            expected = expected,
//            actual = payload
//        )
//    }

    @Test
    fun `should detect search highlight changes in paragraph block`() {
        val index = 0

        val id = MockDataFactory.randomUuid()

        val oldBlock = BlockView.Text.Paragraph(
            id = id,
            searchFields = emptyList(),
            text = MockDataFactory.randomString()
        )

        val newBlock: BlockView = oldBlock.copy(
            searchFields = listOf(
                BlockView.Searchable.Field(
                    key = MockDataFactory.randomString(),
                    highlights = listOf(1..2, 1..3),
                    target = IntRange.EMPTY
                )
            )
        )

        val old = listOf(oldBlock)

        val new = listOf(newBlock)

        val diff = BlockViewDiffUtil(old = old, new = new)

        val payload = diff.getChangePayload(index, index)

        val expected = Payload(
            changes = listOf(BlockViewDiffUtil.SEARCH_HIGHLIGHT_CHANGED)
        )

        assertEquals(
            expected = expected,
            actual = payload
        )
    }

    @Test
    fun `should detect checkbox change for todo-layout title block`() {

        val index = 0

        val id = MockDataFactory.randomUuid()

        val text = MockDataFactory.randomString()

        val oldBlock = BlockView.Title.Todo(
            id = id,
            text = text,
            isChecked = true
        )

        val newBlock: BlockView = oldBlock.copy(
            isChecked = false
        )

        val old = listOf(oldBlock)

        val new = listOf(newBlock)

        val diff = BlockViewDiffUtil(old = old, new = new)

        val payload = diff.getChangePayload(index, index)

        val expected = Payload(
            changes = listOf(BlockViewDiffUtil.TITLE_CHECKBOX_CHANGED)
        )

        assertEquals(
            expected = expected,
            actual = payload
        )
    }

    @Test
    fun `should detect latex changes`() {

        val index = 0

        val oldBlock = BlockView.Latex(
            id = MockDataFactory.randomUuid(),
            isSelected = MockDataFactory.randomBoolean(),
            latex = MockDataFactory.randomString(),
            indent = MockDataFactory.randomInt()
        )

        val newBlock: BlockView = oldBlock.copy(
            latex = MockDataFactory.randomString()
        )

        val old = listOf(oldBlock)

        val new = listOf(newBlock)

        val diff = BlockViewDiffUtil(old = old, new = new)

        val payload = diff.getChangePayload(index, index)

        val expected = Payload(
            changes = listOf(BlockViewDiffUtil.LATEX_CHANGED)
        )

        assertEquals(
            expected = expected,
            actual = payload
        )
    }

    @Test
    fun `should detect changes in the name of the relation`() {

        val index = 0

        val view = DocumentRelationView.Default(
            relationId = MockDataFactory.randomUuid(),
            value = null,
            format = RelationFormat.values().random(),
            name = MockDataFactory.randomString()
        )

        val oldBlock = BlockView.Relation.Related(
            id = MockDataFactory.randomUuid(),
            isSelected = MockDataFactory.randomBoolean(),
            indent = MockDataFactory.randomInt(),
            view = view,
            decorations = emptyList()
        )

        val newBlock: BlockView = oldBlock.copy(
            view = view.copy(
                name = MockDataFactory.randomString()
            )
        )

        val old = listOf(oldBlock)

        val new = listOf(newBlock)

        val diff = BlockViewDiffUtil(old = old, new = new)

        val payload = diff.getChangePayload(index, index)

        val expected = Payload(
            changes = listOf(BlockViewDiffUtil.RELATION_NAME_CHANGED)
        )

        assertEquals(
            expected = expected,
            actual = payload
        )
    }

    @Test
    fun `should detect changes in default relation value`() {

        val index = 0

        val view = DocumentRelationView.Default(
            relationId = MockDataFactory.randomUuid(),
            value = null,
            format = RelationFormat.values().random(),
            name = MockDataFactory.randomString()
        )

        val oldBlock = BlockView.Relation.Related(
            id = MockDataFactory.randomUuid(),
            isSelected = MockDataFactory.randomBoolean(),
            indent = MockDataFactory.randomInt(),
            view = view,
            decorations = emptyList()
        )

        val newBlock: BlockView = oldBlock.copy(
            view = view.copy(
                value = MockDataFactory.randomString()
            )
        )

        val old = listOf(oldBlock)

        val new = listOf(newBlock)

        val diff = BlockViewDiffUtil(old = old, new = new)

        val payload = diff.getChangePayload(index, index)

        val expected = Payload(
            changes = listOf(BlockViewDiffUtil.RELATION_VALUE_CHANGED)
        )

        assertEquals(
            expected = expected,
            actual = payload
        )
    }

    @Test
    fun `should detect changes in checkbox relation value`() {

        val index = 0

        val view = DocumentRelationView.Checkbox(
            relationId = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            isChecked = MockDataFactory.randomBoolean()
        )

        val oldBlock = BlockView.Relation.Related(
            id = MockDataFactory.randomUuid(),
            isSelected = MockDataFactory.randomBoolean(),
            indent = MockDataFactory.randomInt(),
            view = view,
            decorations = emptyList()
        )

        val newBlock: BlockView = oldBlock.copy(
            view = view.copy(
                isChecked = !view.isChecked
            )
        )

        val old = listOf(oldBlock)

        val new = listOf(newBlock)

        val diff = BlockViewDiffUtil(old = old, new = new)

        val payload = diff.getChangePayload(index, index)

        val expected = Payload(
            changes = listOf(BlockViewDiffUtil.RELATION_VALUE_CHANGED)
        )

        assertEquals(
            expected = expected,
            actual = payload
        )
    }

    @Test
    fun `should detect changes in status relation value`() {

        val index = 0

        val oldStatus = StatusView(
            id = MockDataFactory.randomUuid(),
            status = MockDataFactory.randomUuid(),
            color = ""
        )

        val newStatus = StatusView(
            id = MockDataFactory.randomUuid(),
            status = MockDataFactory.randomUuid(),
            color = ""
        )

        val oldView = DocumentRelationView.Status(
            relationId = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            status = listOf(oldStatus)
        )

        val oldBlock = BlockView.Relation.Related(
            id = MockDataFactory.randomUuid(),
            isSelected = MockDataFactory.randomBoolean(),
            indent = MockDataFactory.randomInt(),
            view = oldView,
            decorations = emptyList()
        )

        val newBlock: BlockView = oldBlock.copy(
            view = oldView.copy(
                status = listOf(newStatus)
            )
        )

        val old = listOf(oldBlock)

        val new = listOf(newBlock)

        val diff = BlockViewDiffUtil(old = old, new = new)

        val payload = diff.getChangePayload(index, index)

        val expected = Payload(
            changes = listOf(BlockViewDiffUtil.RELATION_VALUE_CHANGED)
        )

        assertEquals(
            expected = expected,
            actual = payload
        )
    }

    @Test
    fun `should detect changes in tag relation value`() {

        val index = 0

        val oldTag = TagView(
            id = MockDataFactory.randomUuid(),
            tag = MockDataFactory.randomString(),
            color = ""
        )

        val newTag = TagView(
            id = MockDataFactory.randomUuid(),
            tag = MockDataFactory.randomString(),
            color = ""
        )

        val oldView = DocumentRelationView.Tags(
            relationId = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            tags = listOf(oldTag)
        )

        val oldBlock = BlockView.Relation.Related(
            id = MockDataFactory.randomUuid(),
            isSelected = MockDataFactory.randomBoolean(),
            indent = MockDataFactory.randomInt(),
            view = oldView,
            decorations = emptyList()
        )

        val newBlock: BlockView = oldBlock.copy(
            view = oldView.copy(
                tags = listOf(newTag)
            )
        )

        val old = listOf(oldBlock)

        val new = listOf(newBlock)

        val diff = BlockViewDiffUtil(old = old, new = new)

        val payload = diff.getChangePayload(index, index)

        val expected = Payload(
            changes = listOf(BlockViewDiffUtil.RELATION_VALUE_CHANGED)
        )

        assertEquals(
            expected = expected,
            actual = payload
        )
    }

    @Test
    fun `should detect changes in object relation value`() {

        val index = 0

        val oldObject = ObjectView.Default(
            id = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            types = emptyList(),
            icon = ObjectIcon.None
        )

        val newObject = oldObject.copy(
            name = MockDataFactory.randomString()
        )

        val oldView = DocumentRelationView.Object(
            relationId = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            objects = listOf(oldObject)
        )

        val oldBlock = BlockView.Relation.Related(
            id = MockDataFactory.randomUuid(),
            isSelected = MockDataFactory.randomBoolean(),
            indent = MockDataFactory.randomInt(),
            view = oldView,
            decorations = emptyList()
        )

        val newBlock: BlockView = oldBlock.copy(
            view = oldView.copy(
                objects = listOf(newObject)
            )
        )

        val old = listOf(oldBlock)

        val new = listOf(newBlock)

        val diff = BlockViewDiffUtil(old = old, new = new)

        val payload = diff.getChangePayload(index, index)

        val expected = Payload(
            changes = listOf(BlockViewDiffUtil.RELATION_VALUE_CHANGED)
        )

        assertEquals(
            expected = expected,
            actual = payload
        )
    }

    @Test
    fun `should detect changes in file-relation value`() {

        val index = 0

        val oldFile = FileView(
            id = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            mime = MockDataFactory.randomString(),
            ext = MockDataFactory.randomString()
        )

        val newFile = oldFile.copy(
            name = MockDataFactory.randomString()
        )

        val oldView = DocumentRelationView.File(
            relationId = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            files = listOf(oldFile)
        )

        val oldBlock = BlockView.Relation.Related(
            id = MockDataFactory.randomUuid(),
            isSelected = MockDataFactory.randomBoolean(),
            indent = MockDataFactory.randomInt(),
            view = oldView,
            decorations = emptyList()
        )

        val newBlock: BlockView = oldBlock.copy(
            view = oldView.copy(
                files = listOf(newFile)
            )
        )

        val old = listOf(oldBlock)

        val new = listOf(newBlock)

        val diff = BlockViewDiffUtil(old = old, new = new)

        val payload = diff.getChangePayload(index, index)

        val expected = Payload(
            changes = listOf(BlockViewDiffUtil.RELATION_VALUE_CHANGED)
        )

        assertEquals(
            expected = expected,
            actual = payload
        )
    }

    @Test
    fun `should return text change payload for title basic`() {

        val index = 0

        val id = MockDataFactory.randomUuid()
        val newText = MockDataFactory.randomString()

        val oldBlock: BlockView = BlockView.Title.Basic(
            id = id,
            text = MockDataFactory.randomString()
        )

        val newBlock: BlockView = BlockView.Title.Basic(
            id = id,
            text = newText
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
    fun `should return text change payload for title todo`() {

        val index = 0

        val id = MockDataFactory.randomUuid()
        val newText = MockDataFactory.randomString()

        val oldBlock: BlockView = BlockView.Title.Todo(
            id = id,
            text = MockDataFactory.randomString()
        )

        val newBlock: BlockView = BlockView.Title.Todo(
            id = id,
            text = newText
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
    fun `should return text change payload for title profile`() {

        val index = 0

        val id = MockDataFactory.randomUuid()
        val newText = MockDataFactory.randomString()

        val oldBlock: BlockView = BlockView.Title.Profile(
            id = id,
            text = MockDataFactory.randomString()
        )

        val newBlock: BlockView = BlockView.Title.Profile(
            id = id,
            text = newText
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
    fun `should return text change payload for title archive`() {

        val index = 0

        val id = MockDataFactory.randomUuid()
        val newText = MockDataFactory.randomString()

        val oldBlock: BlockView = BlockView.Title.Archive(
            id = id,
            text = MockDataFactory.randomString()
        )

        val newBlock: BlockView = BlockView.Title.Archive(
            id = id,
            text = newText
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
    fun `should detect decoration change for paragraph`() {
        val index = 0

        val oldBlock = StubParagraphView(
            decorations = listOf(
                BlockView.Decoration(
                    background = ThemeColor.DEFAULT
                )
            )
        )

        val newBlock = oldBlock.copy(
            decorations = listOf(
                BlockView.Decoration(
                    background = ThemeColor.YELLOW
                )
            )
        )

        val old = listOf(oldBlock)

        val new = listOf(newBlock)

        val diff = BlockViewDiffUtil(old = old, new = new)

        val payload = diff.getChangePayload(index, index)

        assertEquals(
            actual = payload,
            expected = Payload(listOf(DECORATION_CHANGED))
        )
    }

    @Test
    fun `should not detect decoration change for paragraph`() {
        val index = 0

        val oldBlock = StubParagraphView(
            decorations = listOf(
                BlockView.Decoration(
                    background = ThemeColor.DEFAULT
                )
            )
        )

        val newBlock = oldBlock.copy(
            decorations = listOf(
                BlockView.Decoration(
                    background = ThemeColor.DEFAULT
                )
            )
        )

        val old = listOf(oldBlock)

        val new = listOf(newBlock)

        val diff = BlockViewDiffUtil(old = old, new = new)

        val payload = diff.getChangePayload(index, index)

        assertEquals(
            actual = payload,
            expected = null
        )
    }

    @Test
    fun `should detect icon change for callout`() {
        val index = 0

        val oldBlock = StubCalloutView(
            icon = ObjectIcon.Basic.Emoji("stub")
        )

        val newBlock = oldBlock.copy(
            icon = ObjectIcon.None
        )

        val old = listOf(oldBlock)

        val new = listOf(newBlock)

        val diff = BlockViewDiffUtil(old = old, new = new)

        val payload = diff.getChangePayload(index, index)

        assertEquals(
            actual = payload,
            expected = Payload(listOf(CALLOUT_ICON_CHANGED))
        )
    }

    @Test
    fun `should not detect icon change for callout`() {
        val index = 0

        val oldBlock = StubCalloutView(
            icon = ObjectIcon.Basic.Emoji("stub")
        )

        val newBlock = oldBlock

        val old = listOf(oldBlock)

        val new = listOf(newBlock)

        val diff = BlockViewDiffUtil(old = old, new = new)

        val payload = diff.getChangePayload(index, index)

        assertEquals(
            actual = payload,
            expected = null
        )
    }

    @Test
    fun `when two table blocks are equal - should be no payloads`() {
        //SETUP
        val index = 0
        val tableId = MockDataFactory.randomUuid()
        val oldBlock = StubTwoRowsThreeColumnsSimpleTable(tableId = tableId)
        val newBlock = oldBlock

        //TESTING
        val diff = BlockViewDiffUtil(old = listOf(oldBlock), new = listOf(newBlock))
        val payload = diff.getChangePayload(index, index)

        //ASSERT
        assertEquals(
            actual = payload,
            expected = null
        )
    }

    @Test
    fun `when two table blocks have different texts in cell - should be table cells changed payload`() {
        //SETUP
        val tableId = MockDataFactory.randomUuid()
        val oldBlock = StubTwoRowsThreeColumnsSimpleTable(tableId = tableId)
        val oldCells = oldBlock.cells
        val newCells = oldCells.mapIndexed { index, cell ->
            if (index == 0) cell.copy(
                block = cell.block?.copy(text = MockDataFactory.randomString())
            ) else {
                cell
            }
        }
        val newBlock = oldBlock.copy(cells = newCells)

        //TESTING
        val diff = BlockViewDiffUtil(old = listOf(oldBlock), new = listOf(newBlock))
        val payload = diff.getChangePayload(0, 0)

        //EXPECTED
        val expectedPayloads = Payload(listOf(TABLE_CELLS_CHANGED))

        //ASSERT
        assertEquals(
            actual = payload,
            expected = expectedPayloads
        )
    }

    @Test
    fun `when two table blocks have different tabs - should be table cells selection changed payload`() {
        //SETUP
        val tableId = MockDataFactory.randomUuid()
        val oldBlock = StubTwoRowsThreeColumnsSimpleTable(
            tableId = tableId,
            tab = BlockView.Table.Tab.CELL
        )
        val newBlock = oldBlock.copy(
            tab = BlockView.Table.Tab.ROW
        )

        //TESTING
        val diff = BlockViewDiffUtil(old = listOf(oldBlock), new = listOf(newBlock))
        val payload = diff.getChangePayload(0, 0)

        //EXPECTED
        val expectedPayloads = Payload(listOf(TABLE_CELLS_SELECTION_CHANGED))

        //ASSERT
        assertEquals(
            actual = payload,
            expected = expectedPayloads
        )
    }

    @Test
    fun `when table block have different cells selection state - should be table cells selection changed payload`() {
        //SETUP
        val tableId = MockDataFactory.randomUuid()
        val rowId1 = MockDataFactory.randomUuid()
        val columnId1 = MockDataFactory.randomUuid()
        val columnId2 = MockDataFactory.randomUuid()

        val oldBlock = StubTwoRowsThreeColumnsSimpleTable(
            tableId = tableId,
            rowId1 = rowId1,
            columnId1 = columnId1,
            columnId2 = columnId2,
            tab = BlockView.Table.Tab.CELL,
            selectedCellsIds = listOf("$rowId1-$columnId1")
        )
        val newBlock = oldBlock.copy(
            tab = BlockView.Table.Tab.CELL,
            selectedCellsIds = listOf("$rowId1-$columnId1", "$rowId1-$columnId2")
        )

        //TESTING
        val diff = BlockViewDiffUtil(old = listOf(oldBlock), new = listOf(newBlock))
        val payload = diff.getChangePayload(0, 0)

        //EXPECTED
        val expectedPayloads = Payload(listOf(TABLE_CELLS_SELECTION_CHANGED))

        //ASSERT
        assertEquals(
            actual = payload,
            expected = expectedPayloads
        )
    }

    @Test
    fun `when table block have different row lists - should be nullable payload`() {
        //SETUP
        val tableId = MockDataFactory.randomUuid()

        val oldBlock = StubTableView(
            tableId = tableId,
            tab = BlockView.Table.Tab.CELL,
            rowSize = 10,
            columnSize = 10
        )
        val newBlock = StubTableView(
            tableId = tableId,
            tab = BlockView.Table.Tab.CELL,
            rowSize = 11,
            columnSize = 10
        )

        //TESTING
        val diff = BlockViewDiffUtil(old = listOf(oldBlock), new = listOf(newBlock))
        val payload = diff.getChangePayload(0, 0)

        //EXPECTED
        val expectedPayloads = null

        //ASSERT
        assertEquals(
            actual = payload,
            expected = expectedPayloads
        )
    }

    @Test
    fun `when table block have different column lists - should be nullable payload`() {
        //SETUP
        val tableId = MockDataFactory.randomUuid()

        val oldBlock = StubTableView(
            tableId = tableId,
            tab = BlockView.Table.Tab.CELL,
            rowSize = 10,
            columnSize = 10
        )
        val newBlock = StubTableView(
            tableId = tableId,
            tab = BlockView.Table.Tab.CELL,
            rowSize = 10,
            columnSize = 11
        )

        //TESTING
        val diff = BlockViewDiffUtil(old = listOf(oldBlock), new = listOf(newBlock))
        val payload = diff.getChangePayload(0, 0)

        //EXPECTED
        val expectedPayloads = null

        //ASSERT
        assertEquals(
            actual = payload,
            expected = expectedPayloads
        )
    }
}