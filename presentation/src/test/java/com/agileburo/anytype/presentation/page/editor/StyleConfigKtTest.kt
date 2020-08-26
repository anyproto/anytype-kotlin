package com.agileburo.anytype.presentation.page.editor

import MockDataFactory
import com.agileburo.anytype.core_ui.common.Alignment
import com.agileburo.anytype.core_ui.features.page.styling.StylingType
import com.agileburo.anytype.domain.block.model.Block
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.test.assertEquals

class StyleConfigKtTest {

    @Test
    fun `should return empty style config when block is divider`() {

        val child = MockDataFactory.randomUuid()

        val given = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Divider,
            children = emptyList()
        )
        val result = given.getStyleConfig(focus = null, selection = null)

        val expected = StyleConfig(
            visibleTypes = listOf(),
            enabledMarkup = listOf(),
            enabledAlignment = listOf()
        )

        assertEquals(expected, result)
    }

    @Test
    fun `should return empty style config when block is page`() {

        val child = MockDataFactory.randomUuid()

        val given = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Page(style = Block.Content.Page.Style.EMPTY),
            children = emptyList()
        )

        val result = given.getStyleConfig(focus = null, selection = null)

        val expected = StyleConfig(
            visibleTypes = listOf(),
            enabledMarkup = listOf(),
            enabledAlignment = listOf()
        )

        assertEquals(expected, result)
    }

    @Test
    fun `should return empty style config when block is smart`() {

        val child = MockDataFactory.randomUuid()

        val given = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(type = Block.Content.Smart.Type.HOME),
            children = emptyList()
        )

        val result = given.getStyleConfig(focus = null, selection = null)

        val expected = StyleConfig(
            visibleTypes = listOf(),
            enabledMarkup = listOf(),
            enabledAlignment = listOf()
        )

        assertEquals(expected, result)
    }

    @Test
    fun `should return empty style config when block is icon`() {

        val child = MockDataFactory.randomUuid()

        val given = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Icon(name = MockDataFactory.randomString()),
            children = emptyList()
        )

        val result = given.getStyleConfig(focus = null, selection = null)

        val expected = StyleConfig(
            visibleTypes = listOf(),
            enabledMarkup = listOf(),
            enabledAlignment = listOf()
        )

        assertEquals(expected, result)
    }

    @Test
    fun `should return empty style config when block is layout`() {

        val child = MockDataFactory.randomUuid()

        val given = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Layout(type = Block.Content.Layout.Type.DIV),
            children = emptyList()
        )

        val result = given.getStyleConfig(focus = null, selection = null)

        val expected = StyleConfig(
            visibleTypes = listOf(),
            enabledMarkup = listOf(),
            enabledAlignment = listOf()
        )

        assertEquals(expected, result)
    }

    @Test
    fun `should return style with background config when block is link`() {

        val child = MockDataFactory.randomUuid()

        val given = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Link(
                type = Block.Content.Link.Type.PAGE,
                target = MockDataFactory.randomUuid(),
                fields = Block.Fields.empty()
            ),
            children = emptyList()
        )

        val result = given.getStyleConfig(focus = null, selection = null)

        val expected = StyleConfig(
            visibleTypes = listOf(StylingType.BACKGROUND),
            enabledMarkup = listOf(),
            enabledAlignment = listOf()
        )

        assertEquals(expected, result)
    }

    @Test(expected = IllegalStateException::class)
    fun `should return exception when block type bookmark and focus true`() {

        val child = MockDataFactory.randomUuid()

        val given = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Bookmark(
                null, null, null,
                null, null
            ),
            children = emptyList()
        )

        given.getStyleConfig(focus = false, selection = null)
    }

    @Test(expected = IllegalStateException::class)
    fun `should return exception when block type bookmark and focus false`() {

        val child = MockDataFactory.randomUuid()

        val given = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Bookmark(
                null, null, null,
                null, null
            ),
            children = emptyList()
        )

        given.getStyleConfig(focus = false, selection = null)
    }

    @Test
    fun `should return empty config when block type bookmark and focus null`() {

        val child = MockDataFactory.randomUuid()

        val given = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Bookmark(
                null, null, null,
                null, null
            ),
            children = emptyList()
        )

        val result = given.getStyleConfig(focus = null, selection = null)

        val expected = StyleConfig.emptyState()
        assertEquals(
            expected = expected,
            actual = result
        )
    }

    @Test(expected = IllegalStateException::class)
    fun `should return exception when block type file and focus true`() {

        val child = MockDataFactory.randomUuid()

        val given = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.File(
                type = Block.Content.File.Type.FILE
            ),
            children = emptyList()
        )

        given.getStyleConfig(focus = false, selection = null)
    }

    @Test(expected = IllegalStateException::class)
    fun `should return exception when block type file and focus false`() {

        val child = MockDataFactory.randomUuid()

        val given = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.File(
                type = Block.Content.File.Type.FILE
            ),
            children = emptyList()
        )

        given.getStyleConfig(focus = false, selection = null)
    }

    @Test
    fun `should return style config with background and no align when block type file`() {

        val child = MockDataFactory.randomUuid()

        val given = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.File(
                type = Block.Content.File.Type.FILE
            ),
            children = emptyList()
        )

        val result = given.getStyleConfig(focus = null, selection = null)

        val expected = StyleConfig(
            visibleTypes = listOf(StylingType.BACKGROUND),
            enabledMarkup = listOf(),
            enabledAlignment = listOf()
        )

        assertEquals(expected, result)
    }

    @Test
    fun `should return style config with background and align when block type image`() {

        val child = MockDataFactory.randomUuid()

        val given = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.File(
                type = Block.Content.File.Type.IMAGE
            ),
            children = emptyList()
        )

        val result = given.getStyleConfig(focus = null, selection = null)

        val expected = StyleConfig(
            visibleTypes = listOf(StylingType.BACKGROUND),
            enabledMarkup = listOf(),
            enabledAlignment = listOf()
        )

        assertEquals(expected, result)
    }

    @Test
    fun `should return style config with background and align when block type video`() {

        val child = MockDataFactory.randomUuid()

        val given = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.File(
                type = Block.Content.File.Type.VIDEO
            ),
            children = emptyList()
        )


        val result = given.getStyleConfig(focus = null, selection = null)

        val expected = StyleConfig(
            visibleTypes = listOf(StylingType.BACKGROUND),
            enabledMarkup = listOf(),
            enabledAlignment = listOf()
        )

        assertEquals(expected, result)
    }

    @Test
    fun `should return empty style config when block type bookmark`() {

        val child = MockDataFactory.randomUuid()

        val given = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Bookmark(
                null, null, null,
                null, null
            ),
            children = emptyList()
        )

        val result = given.getStyleConfig(focus = null, selection = null)

        val expected = StyleConfig(
            visibleTypes = listOf(),
            enabledMarkup = listOf(),
            enabledAlignment = listOf()
        )

        assertEquals(expected, result)
    }

    @Test(expected = IllegalStateException::class)
    fun `should return exception when block type text and focus null`() {

        val child = MockDataFactory.randomUuid()

        val given = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                style = Block.Content.Text.Style.P,
                marks = listOf(),
                text = "test",
                isChecked = null,
                backgroundColor = null,
                color = null,
                align = null
            ),
            children = emptyList()
        )

        given.getStyleConfig(focus = null, selection = null)
    }

    @Test
    fun `should return style config when block type code`() {

        val child = MockDataFactory.randomUuid()

        val given = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                style = Block.Content.Text.Style.CODE_SNIPPET,
                marks = listOf(),
                text = "test",
                isChecked = null,
                backgroundColor = null,
                color = null,
                align = null
            ),
            children = emptyList()
        )

        val result = given.getStyleConfig(focus = false, selection = null)

        val expected = StyleConfig(
            visibleTypes = listOf(StylingType.BACKGROUND),
            enabledMarkup = listOf(),
            enabledAlignment = listOf()
        )

        assertEquals(expected, result)
    }

    @Test
    fun `should return style config when block type paragraph and focus false`() {

        val child = MockDataFactory.randomUuid()

        val given = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                style = Block.Content.Text.Style.P,
                marks = listOf(),
                text = "test",
                isChecked = null,
                backgroundColor = null,
                color = null,
                align = null
            ),
            children = emptyList()
        )

        val result = given.getStyleConfig(focus = false, selection = null)

        val expected = StyleConfig(
            visibleTypes = listOf(StylingType.STYLE, StylingType.TEXT_COLOR, StylingType.BACKGROUND),
            enabledMarkup = listOf(
                Block.Content.Text.Mark.Type.BOLD,
                Block.Content.Text.Mark.Type.ITALIC,
                Block.Content.Text.Mark.Type.STRIKETHROUGH,
                Block.Content.Text.Mark.Type.KEYBOARD,
                Block.Content.Text.Mark.Type.LINK
            ),
            enabledAlignment = listOf(Alignment.START, Alignment.CENTER, Alignment.END)
        )

        assertEquals(expected, result)
    }

    @Test
    fun `should return style config when block type paragraph and range is 0`() {

        val child = MockDataFactory.randomUuid()

        val given = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                style = Block.Content.Text.Style.P,
                marks = listOf(),
                text = "test",
                isChecked = null,
                backgroundColor = null,
                color = null,
                align = null
            ),
            children = emptyList()
        )

        val result = given.getStyleConfig(focus = true, selection = IntRange(11, 11))

        val expected = StyleConfig(
            visibleTypes = listOf(StylingType.STYLE, StylingType.TEXT_COLOR, StylingType.BACKGROUND),
            enabledMarkup = listOf(
                Block.Content.Text.Mark.Type.BOLD,
                Block.Content.Text.Mark.Type.ITALIC,
                Block.Content.Text.Mark.Type.STRIKETHROUGH,
                Block.Content.Text.Mark.Type.KEYBOARD,
                Block.Content.Text.Mark.Type.LINK
            ),
            enabledAlignment = listOf(Alignment.START, Alignment.CENTER, Alignment.END)
        )

        assertEquals(expected, result)
    }

    @Test
    fun `should return style config when block type paragraph and range is not 0`() {

        val child = MockDataFactory.randomUuid()

        val given = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                style = Block.Content.Text.Style.P,
                marks = listOf(),
                text = "test",
                isChecked = null,
                backgroundColor = null,
                color = null,
                align = null
            ),
            children = emptyList()
        )

        val result = given.getStyleConfig(focus = true, selection = IntRange(11, 12))

        val expected = StyleConfig(
            visibleTypes = listOf(StylingType.STYLE, StylingType.TEXT_COLOR, StylingType.BACKGROUND),
            enabledMarkup = listOf(
                Block.Content.Text.Mark.Type.BOLD,
                Block.Content.Text.Mark.Type.ITALIC,
                Block.Content.Text.Mark.Type.STRIKETHROUGH,
                Block.Content.Text.Mark.Type.KEYBOARD,
                Block.Content.Text.Mark.Type.LINK
            ),
            enabledAlignment = emptyList()
        )

        assertEquals(expected, result)
    }

    @Test
    fun `should return style config when block type header and focus false`() {

        val child = MockDataFactory.randomUuid()

        val given = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                style = Block.Content.Text.Style.H1,
                marks = listOf(),
                text = "test",
                isChecked = null,
                backgroundColor = null,
                color = null,
                align = null
            ),
            children = emptyList()
        )

        val result = given.getStyleConfig(focus = false, selection = null)

        val expected = StyleConfig(
            visibleTypes = listOf(StylingType.STYLE, StylingType.TEXT_COLOR, StylingType.BACKGROUND),
            enabledMarkup = listOf(
                Block.Content.Text.Mark.Type.ITALIC,
                Block.Content.Text.Mark.Type.STRIKETHROUGH,
                Block.Content.Text.Mark.Type.KEYBOARD,
                Block.Content.Text.Mark.Type.LINK
            ),
            enabledAlignment = listOf(Alignment.START, Alignment.CENTER, Alignment.END)
        )

        assertEquals(expected, result)
    }

    @Test
    fun `should return style config when block type header and range is 0`() {

        val child = MockDataFactory.randomUuid()

        val given = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                style = Block.Content.Text.Style.H1,
                marks = listOf(),
                text = "test",
                isChecked = null,
                backgroundColor = null,
                color = null,
                align = null
            ),
            children = emptyList()
        )

        val result = given.getStyleConfig(focus = true, selection = IntRange(11, 11))

        val expected = StyleConfig(
            visibleTypes = listOf(StylingType.STYLE, StylingType.TEXT_COLOR, StylingType.BACKGROUND),
            enabledMarkup = listOf(
                Block.Content.Text.Mark.Type.ITALIC,
                Block.Content.Text.Mark.Type.STRIKETHROUGH,
                Block.Content.Text.Mark.Type.KEYBOARD,
                Block.Content.Text.Mark.Type.LINK
            ),
            enabledAlignment = listOf(Alignment.START, Alignment.CENTER, Alignment.END)
        )

        assertEquals(expected, result)
    }

    @Test
    fun `should return style config when block type header and range is not 0`() {

        val child = MockDataFactory.randomUuid()

        val given = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                style = Block.Content.Text.Style.H1,
                marks = listOf(),
                text = "test",
                isChecked = null,
                backgroundColor = null,
                color = null,
                align = null
            ),
            children = emptyList()
        )

        val result = given.getStyleConfig(focus = true, selection = IntRange(11, 12))

        val expected = StyleConfig(
            visibleTypes = listOf(StylingType.STYLE, StylingType.TEXT_COLOR, StylingType.BACKGROUND),
            enabledMarkup = listOf(
                Block.Content.Text.Mark.Type.ITALIC,
                Block.Content.Text.Mark.Type.STRIKETHROUGH,
                Block.Content.Text.Mark.Type.KEYBOARD,
                Block.Content.Text.Mark.Type.LINK
            ),
            enabledAlignment = emptyList()
        )

        assertEquals(expected, result)
    }

    @Test
    fun `should return style config when block type bullet and focus false`() {

        val child = MockDataFactory.randomUuid()

        val given = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                style = Block.Content.Text.Style.BULLET,
                marks = listOf(),
                text = "test",
                isChecked = null,
                backgroundColor = null,
                color = null,
                align = null
            ),
            children = emptyList()
        )

        val result = given.getStyleConfig(focus = false, selection = null)

        val expected = StyleConfig(
            visibleTypes = listOf(StylingType.STYLE, StylingType.TEXT_COLOR, StylingType.BACKGROUND),
            enabledMarkup = listOf(
                Block.Content.Text.Mark.Type.BOLD,
                Block.Content.Text.Mark.Type.ITALIC,
                Block.Content.Text.Mark.Type.STRIKETHROUGH,
                Block.Content.Text.Mark.Type.KEYBOARD,
                Block.Content.Text.Mark.Type.LINK
            ),
            enabledAlignment = listOf()
        )

        assertEquals(expected, result)
    }

    @Test
    fun `should return style config when block type bullet and range is 0`() {

        val child = MockDataFactory.randomUuid()

        val given = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                style = Block.Content.Text.Style.BULLET,
                marks = listOf(),
                text = "test",
                isChecked = null,
                backgroundColor = null,
                color = null,
                align = null
            ),
            children = emptyList()
        )

        val result = given.getStyleConfig(focus = true, selection = IntRange(11, 11))

        val expected = StyleConfig(
            visibleTypes = listOf(StylingType.STYLE, StylingType.TEXT_COLOR, StylingType.BACKGROUND),
            enabledMarkup = listOf(
                Block.Content.Text.Mark.Type.BOLD,
                Block.Content.Text.Mark.Type.ITALIC,
                Block.Content.Text.Mark.Type.STRIKETHROUGH,
                Block.Content.Text.Mark.Type.KEYBOARD,
                Block.Content.Text.Mark.Type.LINK
            ),
            enabledAlignment = listOf()
        )

        assertEquals(expected, result)
    }

    @Test
    fun `should return style config when block type bullet and range is not 0`() {

        val child = MockDataFactory.randomUuid()

        val given = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                style = Block.Content.Text.Style.BULLET,
                marks = listOf(),
                text = "test",
                isChecked = null,
                backgroundColor = null,
                color = null,
                align = null
            ),
            children = emptyList()
        )

        val result = given.getStyleConfig(focus = true, selection = IntRange(11, 12))

        val expected = StyleConfig(
            visibleTypes = listOf(StylingType.STYLE, StylingType.TEXT_COLOR, StylingType.BACKGROUND),
            enabledMarkup = listOf(
                Block.Content.Text.Mark.Type.BOLD,
                Block.Content.Text.Mark.Type.ITALIC,
                Block.Content.Text.Mark.Type.STRIKETHROUGH,
                Block.Content.Text.Mark.Type.KEYBOARD,
                Block.Content.Text.Mark.Type.LINK
            ),
            enabledAlignment = emptyList()
        )

        assertEquals(expected, result)
    }

    @Test
    fun `should return style config when block type title and focus false`() {

        val child = MockDataFactory.randomUuid()

        val given = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                style = Block.Content.Text.Style.TITLE,
                marks = listOf(),
                text = "test",
                isChecked = null,
                backgroundColor = null,
                color = null,
                align = null
            ),
            children = emptyList()
        )

        val result = given.getStyleConfig(focus = false, selection = null)

        val expected = StyleConfig(
            visibleTypes = listOf(StylingType.BACKGROUND),
            enabledMarkup = emptyList(),
            enabledAlignment = listOf(Alignment.START, Alignment.CENTER, Alignment.END)
        )

        assertEquals(expected, result)
    }

    @Test
    fun `should return style config when block type title and range is 0`() {

        val child = MockDataFactory.randomUuid()

        val given = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                style = Block.Content.Text.Style.TITLE,
                marks = listOf(),
                text = "test",
                isChecked = null,
                backgroundColor = null,
                color = null,
                align = null
            ),
            children = emptyList()
        )

        val result = given.getStyleConfig(focus = true, selection = IntRange(11, 11))

        val expected = StyleConfig(
            visibleTypes = listOf(StylingType.BACKGROUND),
            enabledMarkup = emptyList(),
            enabledAlignment = listOf(Alignment.START, Alignment.CENTER, Alignment.END)
        )
        assertEquals(expected, result)
    }

    @Test
    fun `should return style config when block type title and range is not 0`() {

        val child = MockDataFactory.randomUuid()

        val given = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                style = Block.Content.Text.Style.TITLE,
                marks = listOf(),
                text = "test",
                isChecked = null,
                backgroundColor = null,
                color = null,
                align = null
            ),
            children = emptyList()
        )

        val result = given.getStyleConfig(focus = true, selection = IntRange(11, 12))

        val expected = StyleConfig(
            visibleTypes = listOf(StylingType.BACKGROUND),
            enabledMarkup = emptyList(),
            enabledAlignment = emptyList()
        )

        assertEquals(expected, result)
    }

    @Test
    fun `should return style config when block type quote and focus false`() {

        val child = MockDataFactory.randomUuid()

        val given = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                style = Block.Content.Text.Style.QUOTE,
                marks = listOf(),
                text = "test",
                isChecked = null,
                backgroundColor = null,
                color = null,
                align = null
            ),
            children = emptyList()
        )

        val result = given.getStyleConfig(focus = false, selection = null)

        val expected = StyleConfig(
            visibleTypes = listOf(
                StylingType.STYLE,
                StylingType.TEXT_COLOR,
                StylingType.BACKGROUND
            ),
            enabledMarkup = listOf(
                Block.Content.Text.Mark.Type.BOLD,
                Block.Content.Text.Mark.Type.ITALIC,
                Block.Content.Text.Mark.Type.STRIKETHROUGH,
                Block.Content.Text.Mark.Type.KEYBOARD,
                Block.Content.Text.Mark.Type.LINK
            ),
            enabledAlignment = listOf(Alignment.START, Alignment.END)
        )

        assertEquals(expected, result)
    }

    @Test
    fun `should return style config when block type qoute and range is 0`() {

        val child = MockDataFactory.randomUuid()

        val given = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                style = Block.Content.Text.Style.QUOTE,
                marks = listOf(),
                text = "test",
                isChecked = null,
                backgroundColor = null,
                color = null,
                align = null
            ),
            children = emptyList()
        )

        val result = given.getStyleConfig(focus = true, selection = IntRange(11, 11))

        val expected = StyleConfig(
            visibleTypes = listOf(
                StylingType.STYLE,
                StylingType.TEXT_COLOR,
                StylingType.BACKGROUND
            ),
            enabledMarkup = listOf(
                Block.Content.Text.Mark.Type.BOLD,
                Block.Content.Text.Mark.Type.ITALIC,
                Block.Content.Text.Mark.Type.STRIKETHROUGH,
                Block.Content.Text.Mark.Type.KEYBOARD,
                Block.Content.Text.Mark.Type.LINK
            ),
            enabledAlignment = listOf(Alignment.START, Alignment.END)
        )
        assertEquals(expected, result)
    }

    @Test
    fun `should return style config when block type qoute and range is not 0`() {

        val child = MockDataFactory.randomUuid()

        val given = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                style = Block.Content.Text.Style.QUOTE,
                marks = listOf(),
                text = "test",
                isChecked = null,
                backgroundColor = null,
                color = null,
                align = null
            ),
            children = emptyList()
        )

        val result = given.getStyleConfig(focus = true, selection = IntRange(11, 12))

        val expected = StyleConfig(
            visibleTypes = listOf(
                StylingType.STYLE,
                StylingType.TEXT_COLOR,
                StylingType.BACKGROUND
            ),
            enabledMarkup = listOf(
                Block.Content.Text.Mark.Type.BOLD,
                Block.Content.Text.Mark.Type.ITALIC,
                Block.Content.Text.Mark.Type.STRIKETHROUGH,
                Block.Content.Text.Mark.Type.KEYBOARD,
                Block.Content.Text.Mark.Type.LINK
            ),
            enabledAlignment = listOf()
        )

        assertEquals(expected, result)
    }

    @Test
    fun `should return style config when block type code and focus false`() {

        val child = MockDataFactory.randomUuid()

        val given = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                style = Block.Content.Text.Style.CODE_SNIPPET,
                marks = listOf(),
                text = "test",
                isChecked = null,
                backgroundColor = null,
                color = null,
                align = null
            ),
            children = emptyList()
        )

        val result = given.getStyleConfig(focus = false, selection = null)

        val expected = StyleConfig(
            visibleTypes = listOf(StylingType.BACKGROUND),
            enabledMarkup = listOf(),
            enabledAlignment = listOf()
        )

        assertEquals(expected, result)
    }

    @Test
    fun `should return style config when block type code and range is 0`() {

        val child = MockDataFactory.randomUuid()

        val given = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                style = Block.Content.Text.Style.CODE_SNIPPET,
                marks = listOf(),
                text = "test",
                isChecked = null,
                backgroundColor = null,
                color = null,
                align = null
            ),
            children = emptyList()
        )

        val result = given.getStyleConfig(focus = true, selection = IntRange(11, 11))

        val expected = StyleConfig(
            visibleTypes = listOf(StylingType.BACKGROUND),
            enabledMarkup = listOf(),
            enabledAlignment = listOf()
        )
        assertEquals(expected, result)
    }

    @Test
    fun `should return style config when block type code and range is not 0`() {

        val child = MockDataFactory.randomUuid()

        val given = Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                style = Block.Content.Text.Style.CODE_SNIPPET,
                marks = listOf(),
                text = "test",
                isChecked = null,
                backgroundColor = null,
                color = null,
                align = null
            ),
            children = emptyList()
        )

        val result = given.getStyleConfig(focus = true, selection = IntRange(11, 12))

        val expected = StyleConfig(
            visibleTypes = listOf(StylingType.BACKGROUND),
            enabledMarkup = listOf(),
            enabledAlignment = listOf()
        )

        assertEquals(expected, result)
    }
}