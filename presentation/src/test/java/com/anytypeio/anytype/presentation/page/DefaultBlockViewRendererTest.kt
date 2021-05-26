package com.anytypeio.anytype.presentation.page

import MockDataFactory
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.SmartBlockType
import com.anytypeio.anytype.core_models.ext.asMap
import com.anytypeio.anytype.core_models.ext.content
import com.anytypeio.anytype.core_utils.tools.Counter
import com.anytypeio.anytype.domain.config.Gateway
import com.anytypeio.anytype.domain.editor.Editor
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.presentation.page.cover.CoverImageHashProvider
import com.anytypeio.anytype.presentation.page.editor.model.Alignment
import com.anytypeio.anytype.presentation.page.editor.model.BlockView
import com.anytypeio.anytype.presentation.page.render.BlockViewRenderer
import com.anytypeio.anytype.presentation.page.render.DefaultBlockViewRenderer
import com.anytypeio.anytype.presentation.page.toggle.ToggleStateHolder
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub
import kotlin.test.assertEquals

class DefaultBlockViewRendererTest {

    class BlockViewRenderWrapper(
        private val blocks: Map<Id, List<Block>>,
        private val renderer: BlockViewRenderer
    ) : BlockViewRenderer by renderer {
        suspend fun render(
            root: Block,
            anchor: Id,
            focus: Editor.Focus,
            indent: Int,
            details: Block.Details
        ): List<BlockView> = blocks.render(
            root = root,
            anchor = anchor,
            focus = focus,
            indent = indent,
            details = details,
            relations = emptyList()
        )
    }

    @Mock
    lateinit var toggleStateHolder: ToggleStateHolder

    @Mock
    lateinit var gateway: Gateway

    @Mock
    lateinit var coverImageHashProvider: CoverImageHashProvider

    private lateinit var renderer: DefaultBlockViewRenderer

    private lateinit var wrapper: BlockViewRenderWrapper


    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        renderer = DefaultBlockViewRenderer(
            urlBuilder = UrlBuilder(gateway),
            toggleStateHolder = toggleStateHolder,
            counter = Counter.Default(),
            coverImageHashProvider = coverImageHashProvider
        )
    }

    @Test
    fun `should return title, paragraph, toggle with its indented inner checkbox`() {

        val title = Block(
            id = MockDataFactory.randomUuid(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                style = Block.Content.Text.Style.TITLE,
                marks = emptyList()
            ),
            children = emptyList(),
            fields = Block.Fields.empty()
        )

        val header = Block(
            id = MockDataFactory.randomUuid(),
            content = Block.Content.Layout(
                type = Block.Content.Layout.Type.HEADER
            ),
            fields = Block.Fields.empty(),
            children = listOf(title.id)
        )

        val paragraph = Block(
            id = MockDataFactory.randomUuid(),
            children = emptyList(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                style = Block.Content.Text.Style.P,
                marks = emptyList(),
                align = Block.Align.AlignLeft
            ),
            fields = Block.Fields.empty()
        )

        val checkbox = Block(
            id = MockDataFactory.randomUuid(),
            children = emptyList(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                style = Block.Content.Text.Style.CHECKBOX,
                marks = emptyList()
            ),
            fields = Block.Fields.empty()
        )

        val toggle = Block(
            id = MockDataFactory.randomUuid(),
            children = listOf(checkbox.id),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                style = Block.Content.Text.Style.TOGGLE,
                marks = emptyList()
            ),
            fields = Block.Fields.empty()
        )

        val page = Block(
            id = MockDataFactory.randomUuid(),
            children = listOf(header.id, paragraph.id, toggle.id),
            fields = Block.Fields.empty(),
            content = Block.Content.Smart()
        )

        val blocks = listOf(page, header, title, paragraph, toggle, checkbox)

        val map = blocks.asMap()

        wrapper = BlockViewRenderWrapper(
            blocks = map,
            renderer = renderer
        )

        toggleStateHolder.stub {
            on { isToggled(toggle.id) } doReturn false
        }

        val result = runBlocking {
            wrapper.render(
                root = page,
                anchor = page.id,
                focus = Editor.Focus.id(paragraph.id),
                indent = 0,
                details = Block.Details()
            )
        }

        val expected = listOf(
            BlockView.Title.Basic(
                id = title.id,
                isFocused = false,
                text = title.content<Block.Content.Text>().text,
                emoji = null
            ),
            BlockView.Text.Paragraph(
                isFocused = true,
                id = paragraph.id,
                marks = emptyList(),
                backgroundColor = paragraph.content<Block.Content.Text>().backgroundColor,
                color = paragraph.content<Block.Content.Text>().color,
                text = paragraph.content<Block.Content.Text>().text,
                alignment = Alignment.START
            ),
            BlockView.Text.Toggle(
                isEmpty = false,
                isFocused = false,
                toggled = false,
                id = toggle.id,
                marks = emptyList(),
                backgroundColor = toggle.content<Block.Content.Text>().backgroundColor,
                color = toggle.content<Block.Content.Text>().color,
                text = toggle.content<Block.Content.Text>().text,
                indent = 0
            )
        )

        assertEquals(expected = expected, actual = result)
    }

    @Test
    fun `should return title, paragraph, toggle without its inner checkbox`() {

        val title = Block(
            id = MockDataFactory.randomUuid(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                style = Block.Content.Text.Style.TITLE,
                marks = emptyList()
            ),
            children = emptyList(),
            fields = Block.Fields.empty()
        )

        val header = Block(
            id = MockDataFactory.randomUuid(),
            content = Block.Content.Layout(
                type = Block.Content.Layout.Type.HEADER
            ),
            fields = Block.Fields.empty(),
            children = listOf(title.id)
        )

        val paragraph = Block(
            id = MockDataFactory.randomUuid(),
            children = emptyList(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                style = Block.Content.Text.Style.P,
                marks = emptyList(),
                align = Block.Align.AlignRight
            ),
            fields = Block.Fields.empty()
        )

        val checkbox = Block(
            id = MockDataFactory.randomUuid(),
            children = emptyList(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                style = Block.Content.Text.Style.CHECKBOX,
                marks = emptyList()
            ),
            fields = Block.Fields.empty()
        )

        val toggle = Block(
            id = MockDataFactory.randomUuid(),
            children = listOf(checkbox.id),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                style = Block.Content.Text.Style.TOGGLE,
                marks = emptyList()
            ),
            fields = Block.Fields.empty()
        )

        val page = Block(
            id = MockDataFactory.randomUuid(),
            children = listOf(header.id, paragraph.id, toggle.id),
            fields = Block.Fields.empty(),
            content = Block.Content.Smart()
        )

        val blocks = listOf(page, header, title, paragraph, toggle, checkbox)

        val map = blocks.asMap()

        wrapper = BlockViewRenderWrapper(
            blocks = map,
            renderer = renderer
        )

        toggleStateHolder.stub {
            on { isToggled(toggle.id) } doReturn true
        }

        val result = runBlocking {
            wrapper.render(
                root = page,
                anchor = page.id,
                focus = Editor.Focus.id(paragraph.id),
                indent = 0,
                details = Block.Details()
            )
        }

        val expected = listOf(
            BlockView.Title.Basic(
                id = title.id,
                isFocused = false,
                text = title.content<Block.Content.Text>().text,
                emoji = null
            ),
            BlockView.Text.Paragraph(
                isFocused = true,
                id = paragraph.id,
                marks = emptyList(),
                backgroundColor = paragraph.content<Block.Content.Text>().backgroundColor,
                color = paragraph.content<Block.Content.Text>().color,
                text = paragraph.content<Block.Content.Text>().text,
                alignment = Alignment.END
            ),
            BlockView.Text.Toggle(
                isEmpty = false,
                isFocused = false,
                toggled = true,
                id = toggle.id,
                marks = emptyList(),
                backgroundColor = toggle.content<Block.Content.Text>().backgroundColor,
                color = toggle.content<Block.Content.Text>().color,
                text = toggle.content<Block.Content.Text>().text,
                indent = 0
            ),
            BlockView.Text.Checkbox(
                isFocused = false,
                id = checkbox.id,
                marks = emptyList(),
                backgroundColor = checkbox.content<Block.Content.Text>().backgroundColor,
                color = checkbox.content<Block.Content.Text>().color,
                text = checkbox.content<Block.Content.Text>().text,
                indent = 1
            )
        )

        assertEquals(expected = expected, actual = result)
    }

    @Test
    fun `should return paragraph with null alignment`() {

        val title = Block(
            id = MockDataFactory.randomUuid(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                style = Block.Content.Text.Style.TITLE,
                marks = emptyList()
            ),
            children = emptyList(),
            fields = Block.Fields.empty()
        )

        val header = Block(
            id = MockDataFactory.randomUuid(),
            content = Block.Content.Layout(
                type = Block.Content.Layout.Type.HEADER
            ),
            fields = Block.Fields.empty(),
            children = listOf(title.id)
        )

        val paragraph = Block(
            id = MockDataFactory.randomUuid(),
            children = emptyList(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                style = Block.Content.Text.Style.P,
                marks = emptyList(),
                align = null
            ),
            fields = Block.Fields.empty()
        )

        val page = Block(
            id = MockDataFactory.randomUuid(),
            children = listOf(header.id, paragraph.id),
            fields = Block.Fields.empty(),
            content = Block.Content.Smart()
        )

        val blocks = listOf(page, header, title, paragraph)

        val map = blocks.asMap()

        wrapper = BlockViewRenderWrapper(
            blocks = map,
            renderer = renderer
        )

        val result = runBlocking {
            wrapper.render(
                root = page,
                anchor = page.id,
                focus = Editor.Focus.id(paragraph.id),
                indent = 0,
                details = Block.Details()
            )
        }

        val expected = listOf(
            BlockView.Title.Basic(
                id = title.id,
                isFocused = false,
                text = title.content<Block.Content.Text>().text,
                emoji = null
            ),
            BlockView.Text.Paragraph(
                isFocused = true,
                id = paragraph.id,
                marks = emptyList(),
                backgroundColor = paragraph.content<Block.Content.Text>().backgroundColor,
                color = paragraph.content<Block.Content.Text>().color,
                text = paragraph.content<Block.Content.Text>().text,
                alignment = null
            )
        )

        assertEquals(expected = expected, actual = result)
    }

    @Test
    fun `should return paragraph with proper alignment`() {

        val title = Block(
            id = MockDataFactory.randomUuid(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                style = Block.Content.Text.Style.TITLE,
                marks = emptyList()
            ),
            children = emptyList(),
            fields = Block.Fields.empty()
        )

        val header = Block(
            id = MockDataFactory.randomUuid(),
            content = Block.Content.Layout(
                type = Block.Content.Layout.Type.HEADER
            ),
            fields = Block.Fields.empty(),
            children = listOf(title.id)
        )

        val paragraph = Block(
            id = MockDataFactory.randomUuid(),
            children = emptyList(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                style = Block.Content.Text.Style.P,
                marks = emptyList(),
                align = Block.Align.AlignCenter
            ),
            fields = Block.Fields.empty()
        )

        val page = Block(
            id = MockDataFactory.randomUuid(),
            children = listOf(header.id, paragraph.id),
            fields = Block.Fields.empty(),
            content = Block.Content.Smart()
        )

        val blocks = listOf(page, header, title, paragraph)

        val map = blocks.asMap()

        wrapper = BlockViewRenderWrapper(
            blocks = map,
            renderer = renderer
        )

        val result = runBlocking {
            wrapper.render(
                root = page,
                anchor = page.id,
                focus = Editor.Focus.id(paragraph.id),
                indent = 0,
                details = Block.Details()
            )
        }

        val expected = listOf(
            BlockView.Title.Basic(
                id = title.id,
                isFocused = false,
                text = title.content<Block.Content.Text>().text,
                emoji = null
            ),
            BlockView.Text.Paragraph(
                isFocused = true,
                id = paragraph.id,
                marks = emptyList(),
                backgroundColor = paragraph.content<Block.Content.Text>().backgroundColor,
                color = paragraph.content<Block.Content.Text>().color,
                text = paragraph.content<Block.Content.Text>().text,
                alignment = Alignment.CENTER
            )
        )

        assertEquals(expected = expected, actual = result)
    }

    @Test
    fun `should add profile title when smart block is profile`() {

        val title = Block(
            id = MockDataFactory.randomUuid(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                style = Block.Content.Text.Style.TITLE,
                marks = emptyList()
            ),
            children = emptyList(),
            fields = Block.Fields.empty()
        )

        val header = Block(
            id = MockDataFactory.randomUuid(),
            content = Block.Content.Layout(
                type = Block.Content.Layout.Type.HEADER
            ),
            fields = Block.Fields.empty(),
            children = listOf(title.id)
        )

        val paragraph = Block(
            id = MockDataFactory.randomUuid(),
            children = emptyList(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                style = Block.Content.Text.Style.P,
                marks = emptyList(),
                align = Block.Align.AlignCenter
            ),
            fields = Block.Fields.empty()
        )

        val name = MockDataFactory.randomString()
        val imageName = MockDataFactory.randomString()
        val pageId = MockDataFactory.randomUuid()
        val fields = Block.Fields(
            map = mapOf(
                "name" to name,
                "iconImage" to imageName
            )
        )
        val details = mapOf(pageId to fields)

        val page = Block(
            id = pageId,
            children = listOf(header.id, paragraph.id),
            fields = fields,
            content = Block.Content.Smart(SmartBlockType.PROFILE_PAGE)
        )

        val blocks = listOf(page, header, title, paragraph)

        val map = blocks.asMap()

        wrapper = BlockViewRenderWrapper(
            blocks = map,
            renderer = renderer
        )

        val result = runBlocking {
            wrapper.render(
                root = page,
                anchor = page.id,
                focus = Editor.Focus.id(paragraph.id),
                indent = 0,
                details = Block.Details(details)
            )
        }

        val expected = listOf(
            BlockView.Title.Profile(
                id = title.id,
                isFocused = false,
                text = title.content<Block.Content.Text>().text,
                image = UrlBuilder(gateway).thumbnail(imageName)
            ),
            BlockView.Text.Paragraph(
                isFocused = true,
                id = paragraph.id,
                marks = emptyList(),
                backgroundColor = paragraph.content<Block.Content.Text>().backgroundColor,
                color = paragraph.content<Block.Content.Text>().color,
                text = paragraph.content<Block.Content.Text>().text,
                alignment = Alignment.CENTER
            )
        )

        assertEquals(expected = expected, actual = result)
    }

    @Test
    fun `should add title when smart block is page`() {

        val title = Block(
            id = MockDataFactory.randomUuid(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                style = Block.Content.Text.Style.TITLE,
                marks = emptyList()
            ),
            children = emptyList(),
            fields = Block.Fields.empty()
        )

        val header = Block(
            id = MockDataFactory.randomUuid(),
            content = Block.Content.Layout(
                type = Block.Content.Layout.Type.HEADER
            ),
            fields = Block.Fields.empty(),
            children = listOf(title.id)
        )

        val paragraph = Block(
            id = MockDataFactory.randomUuid(),
            children = emptyList(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                style = Block.Content.Text.Style.P,
                marks = emptyList(),
                align = Block.Align.AlignCenter
            ),
            fields = Block.Fields.empty()
        )

        val name = MockDataFactory.randomString()
        val imageName = MockDataFactory.randomString()
        val pageId = MockDataFactory.randomUuid()
        val fields = Block.Fields(
            map = mapOf(
                "name" to name,
                "iconImage" to imageName
            )
        )
        val details = mapOf(pageId to fields)

        val page = Block(
            id = pageId,
            children = listOf(header.id, paragraph.id),
            fields = fields,
            content = Block.Content.Smart()
        )

        val blocks = listOf(page, header, title, paragraph)

        val map = blocks.asMap()

        wrapper = BlockViewRenderWrapper(
            blocks = map,
            renderer = renderer
        )

        val result = runBlocking {
            wrapper.render(
                root = page,
                anchor = page.id,
                focus = Editor.Focus.id(paragraph.id),
                indent = 0,
                details = Block.Details(details)
            )
        }

        val expected = listOf(
            BlockView.Title.Basic(
                id = title.id,
                isFocused = false,
                text = title.content<Block.Content.Text>().text,
                image = UrlBuilder(gateway).thumbnail(imageName)
            ),
            BlockView.Text.Paragraph(
                isFocused = true,
                id = paragraph.id,
                marks = emptyList(),
                backgroundColor = paragraph.content<Block.Content.Text>().backgroundColor,
                color = paragraph.content<Block.Content.Text>().color,
                text = paragraph.content<Block.Content.Text>().text,
                alignment = Alignment.CENTER
            )
        )

        assertEquals(expected = expected, actual = result)
    }

    @Test(expected = IllegalStateException::class)
    fun `should throw exception when smart block type is unexpected`() {

        val title = Block(
            id = MockDataFactory.randomUuid(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                style = Block.Content.Text.Style.TITLE,
                marks = emptyList()
            ),
            children = emptyList(),
            fields = Block.Fields.empty()
        )

        val header = Block(
            id = MockDataFactory.randomUuid(),
            content = Block.Content.Layout(
                type = Block.Content.Layout.Type.HEADER
            ),
            fields = Block.Fields.empty(),
            children = listOf(title.id)
        )

        val paragraph = Block(
            id = MockDataFactory.randomUuid(),
            children = emptyList(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                style = Block.Content.Text.Style.P,
                marks = emptyList(),
                align = Block.Align.AlignCenter
            ),
            fields = Block.Fields.empty()
        )

        val name = MockDataFactory.randomString()
        val imageName = MockDataFactory.randomString()
        val pageId = MockDataFactory.randomUuid()

        val fields = Block.Fields(
            map = mapOf(
                "name" to name,
                "iconImage" to imageName
            )
        )

        val details = mapOf(pageId to fields)

        val page = Block(
            id = pageId,
            children = listOf(header.id, paragraph.id),
            fields = fields,
            content = Block.Content.Page(style = Block.Content.Page.Style.TASK)
        )

        val blocks = listOf(page, header, title, paragraph)

        val map = blocks.asMap()

        wrapper = BlockViewRenderWrapper(
            blocks = map,
            renderer = renderer
        )

        runBlocking {
            wrapper.render(
                root = page,
                anchor = page.id,
                focus = Editor.Focus.id(paragraph.id),
                indent = 0,
                details = Block.Details(details)
            )
        }
    }

    @Test
    fun `should render nested paragraphs`() {

        val title = Block(
            id = MockDataFactory.randomUuid(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                style = Block.Content.Text.Style.TITLE,
                marks = emptyList()
            ),
            children = emptyList(),
            fields = Block.Fields.empty()
        )

        val header = Block(
            id = MockDataFactory.randomUuid(),
            content = Block.Content.Layout(
                type = Block.Content.Layout.Type.HEADER
            ),
            fields = Block.Fields.empty(),
            children = listOf(title.id)
        )

        val c = Block(
            id = "C",
            children = listOf(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                style = Block.Content.Text.Style.P,
                marks = emptyList(),
                align = Block.Align.AlignCenter
            ),
            fields = Block.Fields.empty()
        )

        val b = Block(
            id = "B",
            children = listOf(c.id),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                style = Block.Content.Text.Style.P,
                marks = emptyList(),
                align = Block.Align.AlignCenter
            ),
            fields = Block.Fields.empty()
        )

        val a = Block(
            id = "A",
            children = listOf(b.id),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                style = Block.Content.Text.Style.P,
                marks = emptyList(),
                align = Block.Align.AlignCenter
            ),
            fields = Block.Fields.empty()
        )

        val fields = Block.Fields.empty()

        val page = Block(
            id = MockDataFactory.randomUuid(),
            children = listOf(header.id, a.id),
            fields = fields,
            content = Block.Content.Smart()
        )

        val details = mapOf(page.id to fields)

        val blocks = listOf(page, header, title, a, b, c)

        val map = blocks.asMap()

        wrapper = BlockViewRenderWrapper(
            blocks = map,
            renderer = renderer
        )

        val result = runBlocking {
            wrapper.render(
                root = page,
                anchor = page.id,
                focus = Editor.Focus.id(a.id),
                indent = 0,
                details = Block.Details(details)
            )
        }

        val expected = listOf(
            BlockView.Title.Basic(
                id = title.id,
                isFocused = false,
                text = title.content<Block.Content.Text>().text,
                image = null
            ),
            BlockView.Text.Paragraph(
                indent = 0,
                isFocused = true,
                id = a.id,
                marks = emptyList(),
                backgroundColor = a.content<Block.Content.Text>().backgroundColor,
                color = a.content<Block.Content.Text>().color,
                text = a.content<Block.Content.Text>().text,
                alignment = Alignment.CENTER
            ),
            BlockView.Text.Paragraph(
                indent = 1,
                isFocused = false,
                id = b.id,
                marks = emptyList(),
                backgroundColor = b.content<Block.Content.Text>().backgroundColor,
                color = b.content<Block.Content.Text>().color,
                text = b.content<Block.Content.Text>().text,
                alignment = Alignment.CENTER
            ),
            BlockView.Text.Paragraph(
                indent = 2,
                isFocused = false,
                id = c.id,
                marks = emptyList(),
                backgroundColor = c.content<Block.Content.Text>().backgroundColor,
                color = c.content<Block.Content.Text>().color,
                text = c.content<Block.Content.Text>().text,
                alignment = Alignment.CENTER
            )
        )

        assertEquals(expected = expected, actual = result)
    }

    @Test
    fun `should render nested checkboxes`() {

        val title = Block(
            id = MockDataFactory.randomUuid(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                style = Block.Content.Text.Style.TITLE,
                marks = emptyList()
            ),
            children = emptyList(),
            fields = Block.Fields.empty()
        )

        val header = Block(
            id = MockDataFactory.randomUuid(),
            content = Block.Content.Layout(
                type = Block.Content.Layout.Type.HEADER
            ),
            fields = Block.Fields.empty(),
            children = listOf(title.id)
        )

        val c = Block(
            id = "C",
            children = listOf(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                style = Block.Content.Text.Style.CHECKBOX,
                marks = emptyList(),
                align = Block.Align.AlignCenter
            ),
            fields = Block.Fields.empty()
        )

        val b = Block(
            id = "B",
            children = listOf(c.id),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                style = Block.Content.Text.Style.CHECKBOX,
                marks = emptyList(),
                align = Block.Align.AlignCenter
            ),
            fields = Block.Fields.empty()
        )

        val a = Block(
            id = "A",
            children = listOf(b.id),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                style = Block.Content.Text.Style.CHECKBOX,
                marks = emptyList(),
                align = Block.Align.AlignCenter
            ),
            fields = Block.Fields.empty()
        )

        val fields = Block.Fields.empty()

        val page = Block(
            id = MockDataFactory.randomUuid(),
            children = listOf(header.id, a.id),
            fields = fields,
            content = Block.Content.Smart()
        )

        val details = mapOf(page.id to fields)

        val blocks = listOf(page, header, title, a, b, c)

        val map = blocks.asMap()

        wrapper = BlockViewRenderWrapper(
            blocks = map,
            renderer = renderer
        )

        val result = runBlocking {
            wrapper.render(
                root = page,
                anchor = page.id,
                focus = Editor.Focus.id(a.id),
                indent = 0,
                details = Block.Details(details)
            )
        }

        val expected = listOf(
            BlockView.Title.Basic(
                id = title.id,
                isFocused = false,
                text = title.content<Block.Content.Text>().text,
                image = null
            ),
            BlockView.Text.Checkbox(
                indent = 0,
                isFocused = true,
                id = a.id,
                marks = emptyList(),
                backgroundColor = a.content<Block.Content.Text>().backgroundColor,
                color = a.content<Block.Content.Text>().color,
                text = a.content<Block.Content.Text>().text
            ),
            BlockView.Text.Checkbox(
                indent = 1,
                isFocused = false,
                id = b.id,
                marks = emptyList(),
                backgroundColor = b.content<Block.Content.Text>().backgroundColor,
                color = b.content<Block.Content.Text>().color,
                text = b.content<Block.Content.Text>().text
            ),
            BlockView.Text.Checkbox(
                indent = 2,
                isFocused = false,
                id = c.id,
                marks = emptyList(),
                backgroundColor = c.content<Block.Content.Text>().backgroundColor,
                color = c.content<Block.Content.Text>().color,
                text = c.content<Block.Content.Text>().text
            )
        )

        assertEquals(expected = expected, actual = result)
    }

    @Test
    fun `should render nested bulleted items`() {

        val title = Block(
            id = MockDataFactory.randomUuid(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                style = Block.Content.Text.Style.TITLE,
                marks = emptyList()
            ),
            children = emptyList(),
            fields = Block.Fields.empty()
        )

        val header = Block(
            id = MockDataFactory.randomUuid(),
            content = Block.Content.Layout(
                type = Block.Content.Layout.Type.HEADER
            ),
            fields = Block.Fields.empty(),
            children = listOf(title.id)
        )

        val c = Block(
            id = "C",
            children = listOf(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                style = Block.Content.Text.Style.BULLET,
                marks = emptyList(),
                align = Block.Align.AlignCenter
            ),
            fields = Block.Fields.empty()
        )

        val b = Block(
            id = "B",
            children = listOf(c.id),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                style = Block.Content.Text.Style.BULLET,
                marks = emptyList(),
                align = Block.Align.AlignCenter
            ),
            fields = Block.Fields.empty()
        )

        val a = Block(
            id = "A",
            children = listOf(b.id),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                style = Block.Content.Text.Style.BULLET,
                marks = emptyList(),
                align = Block.Align.AlignCenter
            ),
            fields = Block.Fields.empty()
        )

        val fields = Block.Fields.empty()

        val page = Block(
            id = MockDataFactory.randomUuid(),
            children = listOf(header.id, a.id),
            fields = fields,
            content = Block.Content.Smart()
        )

        val details = mapOf(page.id to fields)

        val blocks = listOf(page, header, title, a, b, c)

        val map = blocks.asMap()

        wrapper = BlockViewRenderWrapper(
            blocks = map,
            renderer = renderer
        )

        val result = runBlocking {
            wrapper.render(
                root = page,
                anchor = page.id,
                focus = Editor.Focus.id(a.id),
                indent = 0,
                details = Block.Details(details)
            )
        }

        val expected = listOf(
            BlockView.Title.Basic(
                id = title.id,
                isFocused = false,
                text = title.content<Block.Content.Text>().text,
                image = null
            ),
            BlockView.Text.Bulleted(
                indent = 0,
                isFocused = true,
                id = a.id,
                marks = emptyList(),
                backgroundColor = a.content<Block.Content.Text>().backgroundColor,
                color = a.content<Block.Content.Text>().color,
                text = a.content<Block.Content.Text>().text
            ),
            BlockView.Text.Bulleted(
                indent = 1,
                isFocused = false,
                id = b.id,
                marks = emptyList(),
                backgroundColor = b.content<Block.Content.Text>().backgroundColor,
                color = b.content<Block.Content.Text>().color,
                text = b.content<Block.Content.Text>().text
            ),
            BlockView.Text.Bulleted(
                indent = 2,
                isFocused = false,
                id = c.id,
                marks = emptyList(),
                backgroundColor = c.content<Block.Content.Text>().backgroundColor,
                color = c.content<Block.Content.Text>().color,
                text = c.content<Block.Content.Text>().text
            )
        )

        assertEquals(expected = expected, actual = result)
    }

    @Test
    fun `should render nested numbered lists`() {

        val title = Block(
            id = MockDataFactory.randomUuid(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                style = Block.Content.Text.Style.TITLE,
                marks = emptyList()
            ),
            children = emptyList(),
            fields = Block.Fields.empty()
        )

        val header = Block(
            id = MockDataFactory.randomUuid(),
            content = Block.Content.Layout(
                type = Block.Content.Layout.Type.HEADER
            ),
            fields = Block.Fields.empty(),
            children = listOf(title.id)
        )

        val d = Block(
            id = "D",
            children = listOf(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                style = Block.Content.Text.Style.NUMBERED,
                marks = emptyList(),
                align = Block.Align.AlignCenter
            ),
            fields = Block.Fields.empty()
        )

        val c = Block(
            id = "C",
            children = listOf(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                style = Block.Content.Text.Style.NUMBERED,
                marks = emptyList(),
                align = Block.Align.AlignCenter
            ),
            fields = Block.Fields.empty()
        )

        val b = Block(
            id = "B",
            children = listOf(c.id, d.id),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                style = Block.Content.Text.Style.NUMBERED,
                marks = emptyList(),
                align = Block.Align.AlignCenter
            ),
            fields = Block.Fields.empty()
        )

        val a = Block(
            id = "A",
            children = listOf(b.id),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                style = Block.Content.Text.Style.NUMBERED,
                marks = emptyList(),
                align = Block.Align.AlignCenter
            ),
            fields = Block.Fields.empty()
        )

        val fields = Block.Fields.empty()

        val page = Block(
            id = MockDataFactory.randomUuid(),
            children = listOf(header.id, a.id),
            fields = fields,
            content = Block.Content.Smart()
        )

        val details = mapOf(page.id to fields)

        val blocks = listOf(page, header, title, a, b, c, d)

        val map = blocks.asMap()

        wrapper = BlockViewRenderWrapper(
            blocks = map,
            renderer = renderer
        )

        val result = runBlocking {
            wrapper.render(
                root = page,
                anchor = page.id,
                focus = Editor.Focus.id(a.id),
                indent = 0,
                details = Block.Details(details)
            )
        }

        val expected = listOf(
            BlockView.Title.Basic(
                id = title.id,
                isFocused = false,
                text = title.content<Block.Content.Text>().text,
                image = null
            ),
            BlockView.Text.Numbered(
                indent = 0,
                isFocused = true,
                id = a.id,
                marks = emptyList(),
                backgroundColor = a.content<Block.Content.Text>().backgroundColor,
                color = a.content<Block.Content.Text>().color,
                text = a.content<Block.Content.Text>().text,
                number = 1
            ),
            BlockView.Text.Numbered(
                indent = 1,
                isFocused = false,
                id = b.id,
                marks = emptyList(),
                backgroundColor = b.content<Block.Content.Text>().backgroundColor,
                color = b.content<Block.Content.Text>().color,
                text = b.content<Block.Content.Text>().text,
                number = 1
            ),
            BlockView.Text.Numbered(
                indent = 2,
                isFocused = false,
                id = c.id,
                marks = emptyList(),
                backgroundColor = c.content<Block.Content.Text>().backgroundColor,
                color = c.content<Block.Content.Text>().color,
                text = c.content<Block.Content.Text>().text,
                number = 1
            ),
            BlockView.Text.Numbered(
                indent = 2,
                isFocused = false,
                id = d.id,
                marks = emptyList(),
                backgroundColor = d.content<Block.Content.Text>().backgroundColor,
                color = d.content<Block.Content.Text>().color,
                text = d.content<Block.Content.Text>().text,
                number = 2
            )
        )

        assertEquals(expected = expected, actual = result)
    }
}