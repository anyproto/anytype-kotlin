package com.anytypeio.anytype.presentation.editor

import MockDataFactory
import android.util.Log
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.SmartBlockType
import com.anytypeio.anytype.core_models.ext.asMap
import com.anytypeio.anytype.core_models.ext.content
import com.anytypeio.anytype.core_models.restrictions.ObjectRestriction
import com.anytypeio.anytype.core_utils.const.DetailsKeys
import com.anytypeio.anytype.domain.config.Gateway
import com.anytypeio.anytype.domain.editor.Editor
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.emojifier.data.DefaultDocumentEmojiIconProvider
import com.anytypeio.anytype.presentation.MockTypicalDocumentFactory
import com.anytypeio.anytype.presentation.editor.cover.CoverImageHashProvider
import com.anytypeio.anytype.presentation.editor.editor.Markup
import com.anytypeio.anytype.presentation.editor.editor.Markup.Companion.NON_EXISTENT_OBJECT_MENTION_NAME
import com.anytypeio.anytype.presentation.editor.editor.model.Alignment
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.render.BlockViewRenderer
import com.anytypeio.anytype.presentation.editor.render.DefaultBlockViewRenderer
import com.anytypeio.anytype.presentation.editor.toggle.ToggleStateHolder
import com.anytypeio.anytype.presentation.util.TXT
import kotlinx.coroutines.runBlocking
import net.lachlanmckee.timberjunit.TimberTestRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub
import kotlin.test.assertEquals

class DefaultBlockViewRendererTest {

    class BlockViewRenderWrapper(
        private val blocks: Map<Id, List<Block>>,
        private val renderer: BlockViewRenderer,
        private val restrictions: List<ObjectRestriction> = emptyList()
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
            relations = emptyList(),
            restrictions = restrictions,
            selection = emptySet()
        )
    }

    @get:Rule
    val timberTestRule: TimberTestRule = TimberTestRule.builder()
        .minPriority(Log.DEBUG)
        .showThread(true)
        .showTimestamp(false)
        .onlyLogWhenTestFails(true)
        .build()

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
        MockitoAnnotations.openMocks(this)
        renderer = DefaultBlockViewRenderer(
            urlBuilder = UrlBuilder(gateway),
            toggleStateHolder = toggleStateHolder,
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
    fun `should render title with read mode when restriction details present`() {

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

        val fields = Block.Fields.empty()

        val page = Block(
            id = MockDataFactory.randomUuid(),
            children = listOf(header.id),
            fields = fields,
            content = Block.Content.Smart()
        )

        val details = mapOf(page.id to fields)

        val blocks = listOf(page, header, title)

        val map = blocks.asMap()

        wrapper = BlockViewRenderWrapper(
            blocks = map,
            renderer = renderer,
            restrictions = listOf(ObjectRestriction.DETAILS)
        )

        val result = runBlocking {
            wrapper.render(
                root = page,
                anchor = page.id,
                focus = Editor.Focus.empty(),
                indent = 0,
                details = Block.Details(details),

            )
        }

        val expected = listOf(
            BlockView.Title.Basic(
                id = title.id,
                isFocused = false,
                text = title.content<Block.Content.Text>().text,
                image = null,
                mode = BlockView.Mode.READ
            )
        )

        assertEquals(expected = expected, actual = result)
    }

    @Test
    fun `should render title with edit mode when restriction details is not present`() {

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

        val fields = Block.Fields.empty()

        val page = Block(
            id = MockDataFactory.randomUuid(),
            children = listOf(header.id),
            fields = fields,
            content = Block.Content.Smart()
        )

        val details = mapOf(page.id to fields)

        val blocks = listOf(page, header, title)

        val map = blocks.asMap()

        wrapper = BlockViewRenderWrapper(
            blocks = map,
            renderer = renderer,
            restrictions = listOf(ObjectRestriction.RELATIONS)
        )

        val result = runBlocking {
            wrapper.render(
                root = page,
                anchor = page.id,
                focus = Editor.Focus.empty(),
                indent = 0,
                details = Block.Details(details)
            )
        }

        val expected = listOf(
            BlockView.Title.Basic(
                id = title.id,
                isFocused = false,
                text = title.content<Block.Content.Text>().text,
                image = null,
                mode = BlockView.Mode.EDIT
            )
        )

        assertEquals(expected = expected, actual = result)
    }

    @Test
    fun `should update mention text in text blocks and shift all markups`() {

        val title = MockTypicalDocumentFactory.title
        val header = MockTypicalDocumentFactory.header

        val mentionText1 = "Foobar"
        val mentionTextUpdated1 = "FoobaraPpJd20"
        val mentionText2 = "Anytype"
        val mentionTextUpdated2 = "Anyt"
        val source = "Start $mentionText1 middle end Hdm5K 6511 xFMoTKqe $mentionText2 sNmO2f"
        val sourceUpdated =
            "Start $mentionTextUpdated1 middle end Hdm5K 6511 xFMoTKqe $mentionTextUpdated2 sNmO2f"
        val textColor = "F0So"
        val mentionTarget1 = "mc412Q8"
        val mentionTarget2 = "zd4h0852"
        val link1 = "zH45s"
        val link2 = "73EnYa"

        val marks: List<Block.Content.Text.Mark> = listOf(
            Block.Content.Text.Mark(
                range = 0..5,
                type = Block.Content.Text.Mark.Type.TEXT_COLOR,
                param = textColor
            ),
            Block.Content.Text.Mark(
                range = 6..12,
                type = Block.Content.Text.Mark.Type.MENTION,
                param = mentionTarget1
            ),
            Block.Content.Text.Mark(
                range = 13..19,
                type = Block.Content.Text.Mark.Type.BOLD
            ),
            Block.Content.Text.Mark(
                range = 20..23,
                type = Block.Content.Text.Mark.Type.LINK,
                param = link1
            ),
            Block.Content.Text.Mark(
                range = 24..29,
                type = Block.Content.Text.Mark.Type.ITALIC
            ),
            Block.Content.Text.Mark(
                range = 30..34,
                type = Block.Content.Text.Mark.Type.STRIKETHROUGH
            ),
            Block.Content.Text.Mark(
                range = 44..51,
                type = Block.Content.Text.Mark.Type.MENTION,
                param = mentionTarget2
            ),
            Block.Content.Text.Mark(
                range = 52..58,
                type = Block.Content.Text.Mark.Type.LINK,
                param = link2
            )
        )

        val a = Block(
            id = MockDataFactory.randomUuid(),
            children = listOf(),
            content = Block.Content.Text(
                text = source,
                style = Block.Content.Text.Style.P,
                marks = marks,
                align = Block.Align.AlignLeft
            ),
            fields = Block.Fields.empty()
        )

        val page = Block(
            id = MockDataFactory.randomUuid(),
            children = listOf(header.id, a.id),
            fields = Block.Fields.empty(),
            content = Block.Content.Smart()
        )

        val randomEmoji1 = DefaultDocumentEmojiIconProvider.DOCUMENT_SET.random()
        val fieldsUpdated1 = Block.Fields(
            mapOf(
                Block.Fields.NAME_KEY to mentionTextUpdated1,
                DetailsKeys.ICON_EMOJI to randomEmoji1
            )
        )

        val randomEmoji2 = DefaultDocumentEmojiIconProvider.DOCUMENT_SET.random()
        val fieldsUpdated2 = Block.Fields(
            mapOf(
                Block.Fields.NAME_KEY to mentionTextUpdated2,
                DetailsKeys.ICON_EMOJI to randomEmoji2
            )
        )

        val detailsAmend = mapOf(
            mentionTarget1 to fieldsUpdated1,
            mentionTarget2 to fieldsUpdated2
        )

        val blocks = listOf(page, header, title, a)

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
                details = Block.Details(detailsAmend)
            )
        }

        val expected = listOf(
            BlockView.Title.Basic(
                id = title.id,
                isFocused = false,
                text = title.content<Block.Content.Text>().text,
                image = null,
                mode = BlockView.Mode.EDIT
            ),
            BlockView.Text.Paragraph(
                id = a.id,
                text = sourceUpdated,
                marks = listOf(
                    Markup.Mark.TextColor(
                        from = 0,
                        to = 5,
                        color = textColor
                    ),
                    Markup.Mark.Mention.WithEmoji(
                        from = 6,
                        to = 19,
                        param = mentionTarget1,
                        emoji = randomEmoji1
                    ),
                    Markup.Mark.Bold(
                        from = 20,
                        to = 26
                    ),
                    Markup.Mark.Link(
                        from = 27,
                        to = 30,
                        param = link1
                    ),
                    Markup.Mark.Italic(
                        from = 31,
                        to = 36
                    ),
                    Markup.Mark.Strikethrough(
                        from = 37,
                        to = 41
                    ),
                    Markup.Mark.Mention.WithEmoji(
                        from = 51,
                        to = 55,
                        param = mentionTarget2,
                        emoji = randomEmoji2
                    ),
                    Markup.Mark.Link(
                        from = 56,
                        to = 62,
                        param = link2
                    )
                ),
                isFocused = true,
                alignment = Alignment.START
            )
        )

        assertEquals(expected = expected, actual = result)
    }

    @Test
    fun `should update mention text in text blocks, sort marks and shift`() {

        val title = MockTypicalDocumentFactory.title
        val header = MockTypicalDocumentFactory.header

        val mentionText1 = "Foobar"
        val mentionTextUpdated1 = "FoobaraPpJd20"
        val mentionText2 = "Anytype"
        val mentionTextUpdated2 = "Anyt"
        val source = "Start $mentionText1 middle end Hdm5K 6511 xFMoTKqe $mentionText2 sNmO2f"
        val sourceUpdated =
            "Start $mentionTextUpdated1 middle end Hdm5K 6511 xFMoTKqe $mentionTextUpdated2 sNmO2f"
        val textColor = "F0So"
        val mentionTarget1 = "mc412Q8"
        val mentionTarget2 = "zd4h0852"
        val link1 = "zH45s"
        val link2 = "73EnYa"

        val marks: List<Block.Content.Text.Mark> = listOf(
            Block.Content.Text.Mark(
                range = 20..23,
                type = Block.Content.Text.Mark.Type.LINK,
                param = link1
            ),
            Block.Content.Text.Mark(
                range = 52..58,
                type = Block.Content.Text.Mark.Type.LINK,
                param = link2
            ),
            Block.Content.Text.Mark(
                range = 24..29,
                type = Block.Content.Text.Mark.Type.ITALIC
            ),
            Block.Content.Text.Mark(
                range = 0..5,
                type = Block.Content.Text.Mark.Type.TEXT_COLOR,
                param = textColor
            ),
            Block.Content.Text.Mark(
                range = 30..34,
                type = Block.Content.Text.Mark.Type.STRIKETHROUGH
            ),
            Block.Content.Text.Mark(
                range = 44..51,
                type = Block.Content.Text.Mark.Type.MENTION,
                param = mentionTarget2
            ),
            Block.Content.Text.Mark(
                range = 6..12,
                type = Block.Content.Text.Mark.Type.MENTION,
                param = mentionTarget1
            ),
            Block.Content.Text.Mark(
                range = 13..19,
                type = Block.Content.Text.Mark.Type.BOLD
            )
        )

        val a = Block(
            id = MockDataFactory.randomUuid(),
            children = listOf(),
            content = Block.Content.Text(
                text = source,
                style = Block.Content.Text.Style.P,
                marks = marks,
                align = Block.Align.AlignLeft
            ),
            fields = Block.Fields.empty()
        )

        val page = Block(
            id = MockDataFactory.randomUuid(),
            children = listOf(header.id, a.id),
            fields = Block.Fields.empty(),
            content = Block.Content.Smart()
        )

        val randomEmoji1 = DefaultDocumentEmojiIconProvider.DOCUMENT_SET.random()
        val fieldsUpdated1 = Block.Fields(
            mapOf(
                Block.Fields.NAME_KEY to mentionTextUpdated1,
                DetailsKeys.ICON_EMOJI to randomEmoji1
            )
        )

        val randomEmoji2 = DefaultDocumentEmojiIconProvider.DOCUMENT_SET.random()
        val fieldsUpdated2 = Block.Fields(
            mapOf(
                Block.Fields.NAME_KEY to mentionTextUpdated2,
                DetailsKeys.ICON_EMOJI to randomEmoji2
            )
        )

        val detailsAmend = mapOf(
            mentionTarget1 to fieldsUpdated1,
            mentionTarget2 to fieldsUpdated2
        )

        val blocks = listOf(page, header, title, a)

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
                details = Block.Details(detailsAmend)
            )
        }

        val expected = listOf(
            BlockView.Title.Basic(
                id = title.id,
                isFocused = false,
                text = title.content<Block.Content.Text>().text,
                image = null,
                mode = BlockView.Mode.EDIT
            ),
            BlockView.Text.Paragraph(
                id = a.id,
                text = sourceUpdated,
                marks = listOf(
                    Markup.Mark.TextColor(
                        from = 0,
                        to = 5,
                        color = textColor
                    ),
                    Markup.Mark.Mention.WithEmoji(
                        from = 6,
                        to = 19,
                        emoji = randomEmoji1,
                        param = mentionTarget1
                    ),
                    Markup.Mark.Bold(
                        from = 20,
                        to = 26
                    ),
                    Markup.Mark.Link(
                        from = 27,
                        to = 30,
                        param = link1
                    ),
                    Markup.Mark.Italic(
                        from = 31,
                        to = 36
                    ),
                    Markup.Mark.Strikethrough(
                        from = 37,
                        to = 41
                    ),
                    Markup.Mark.Mention.WithEmoji(
                        from = 51,
                        to = 55,
                        emoji = randomEmoji2,
                        param = mentionTarget2
                    ),
                    Markup.Mark.Link(
                        from = 56,
                        to = 62,
                        param = link2
                    )
                ),
                isFocused = true,
                alignment = Alignment.START
            )
        )

        assertEquals(expected = expected, actual = result)
    }

    @Test
    fun `should update mentions text as snippet and as name, depending on layout in text blocks, sort marks and shift`() {

        val title = MockTypicalDocumentFactory.title
        val header = MockTypicalDocumentFactory.header

        val mentionText1 = "Foobar"
        val mentionTextUpdated1 = "FoobaraPpJd20"
        val mentionText2 = "Anytype"
        val mentionTextUpdated2 = "Anyt"
        val source = "Start $mentionText1 middle end Hdm5K 6511 xFMoTKqe $mentionText2 sNmO2f"
        val sourceUpdated =
            "Start $mentionTextUpdated1 middle end Hdm5K 6511 xFMoTKqe $mentionTextUpdated2 sNmO2f"
        val textColor = "F0So"
        val mentionTarget1 = "mc412Q8"
        val mentionTarget2 = "zd4h0852"
        val link1 = "zH45s"
        val link2 = "73EnYa"

        val marks: List<Block.Content.Text.Mark> = listOf(
            Block.Content.Text.Mark(
                range = 20..23,
                type = Block.Content.Text.Mark.Type.LINK,
                param = link1
            ),
            Block.Content.Text.Mark(
                range = 52..58,
                type = Block.Content.Text.Mark.Type.LINK,
                param = link2
            ),
            Block.Content.Text.Mark(
                range = 24..29,
                type = Block.Content.Text.Mark.Type.ITALIC
            ),
            Block.Content.Text.Mark(
                range = 0..5,
                type = Block.Content.Text.Mark.Type.TEXT_COLOR,
                param = textColor
            ),
            Block.Content.Text.Mark(
                range = 30..34,
                type = Block.Content.Text.Mark.Type.STRIKETHROUGH
            ),
            Block.Content.Text.Mark(
                range = 44..51,
                type = Block.Content.Text.Mark.Type.MENTION,
                param = mentionTarget2
            ),
            Block.Content.Text.Mark(
                range = 6..12,
                type = Block.Content.Text.Mark.Type.MENTION,
                param = mentionTarget1
            ),
            Block.Content.Text.Mark(
                range = 13..19,
                type = Block.Content.Text.Mark.Type.BOLD
            )
        )

        val a = Block(
            id = MockDataFactory.randomUuid(),
            children = listOf(),
            content = Block.Content.Text(
                text = source,
                style = Block.Content.Text.Style.P,
                marks = marks,
                align = Block.Align.AlignLeft
            ),
            fields = Block.Fields.empty()
        )

        val page = Block(
            id = MockDataFactory.randomUuid(),
            children = listOf(header.id, a.id),
            fields = Block.Fields.empty(),
            content = Block.Content.Smart()
        )

        val randomEmoji1 = DefaultDocumentEmojiIconProvider.DOCUMENT_SET.random()
        val fieldsUpdated1 = Block.Fields(
            mapOf(
                "name" to "XmN34",
                "snippet" to mentionTextUpdated1,
                "layout" to 9.0
            )
        )

        val randomEmoji2 = DefaultDocumentEmojiIconProvider.DOCUMENT_SET.random()
        val fieldsUpdated2 = Block.Fields(
            mapOf(
                "name" to mentionTextUpdated2,
                "iconEmoji" to randomEmoji2,
                "layout" to 0.0
            )
        )

        val detailsAmend = mapOf(
            mentionTarget1 to fieldsUpdated1,
            mentionTarget2 to fieldsUpdated2
        )

        val blocks = listOf(page, header, title, a)

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
                details = Block.Details(detailsAmend)
            )
        }

        val expected = listOf(
            BlockView.Title.Basic(
                id = title.id,
                isFocused = false,
                text = title.content<Block.Content.Text>().text,
                image = null,
                mode = BlockView.Mode.EDIT
            ),
            BlockView.Text.Paragraph(
                id = a.id,
                text = sourceUpdated,
                marks = listOf(
                    Markup.Mark.TextColor(
                        from = 0,
                        to = 5,
                        color = textColor
                    ),
                    Markup.Mark.Mention.Base(
                        from = 6,
                        to = 19,
                        param = mentionTarget1
                    ),
                    Markup.Mark.Bold(
                        from = 20,
                        to = 26
                    ),
                    Markup.Mark.Link(
                        from = 27,
                        to = 30,
                        param = link1
                    ),
                    Markup.Mark.Italic(
                        from = 31,
                        to = 36
                    ),
                    Markup.Mark.Strikethrough(
                        from = 37,
                        to = 41
                    ),
                    Markup.Mark.Mention.WithEmoji(
                        from = 51,
                        to = 55,
                        param = mentionTarget2,
                        emoji = randomEmoji2
                    ),
                    Markup.Mark.Link(
                        from = 56,
                        to = 62,
                        param = link2
                    )
                ),
                isFocused = true,
                alignment = Alignment.START
            )
        )

        assertEquals(expected = expected, actual = result)
    }

    @Test//Proper test
    fun `should return same text and marks`() {

        val title = MockTypicalDocumentFactory.title
        val header = MockTypicalDocumentFactory.header

        val mentionText1 = "Foobar"
        val mentionText2 = "Anytype"
        val source = "Start $mentionText1 middle end Hdm5K 6511 xFMoTKqe $mentionText2 sNmO2f"
        val textColor = "F0So"
        val mentionTarget1 = "mc412Q8"
        val mentionTarget2 = "zd4h0852"
        val link1 = "zH45s"
        val link2 = "73EnYa"

        val marks: List<Block.Content.Text.Mark> = listOf(
            Block.Content.Text.Mark(
                range = 0..5,
                type = Block.Content.Text.Mark.Type.TEXT_COLOR,
                param = textColor
            ),
            Block.Content.Text.Mark(
                range = 6..12,
                type = Block.Content.Text.Mark.Type.MENTION,
                param = mentionTarget1
            ),
            Block.Content.Text.Mark(
                range = 13..19,
                type = Block.Content.Text.Mark.Type.BOLD
            ),
            Block.Content.Text.Mark(
                range = 20..23,
                type = Block.Content.Text.Mark.Type.LINK,
                param = link1
            ),
            Block.Content.Text.Mark(
                range = 24..29,
                type = Block.Content.Text.Mark.Type.ITALIC
            ),
            Block.Content.Text.Mark(
                range = 30..34,
                type = Block.Content.Text.Mark.Type.STRIKETHROUGH
            ),
            Block.Content.Text.Mark(
                range = 44..51,
                type = Block.Content.Text.Mark.Type.MENTION,
                param = mentionTarget2
            ),
            Block.Content.Text.Mark(
                range = 52..58,
                type = Block.Content.Text.Mark.Type.LINK,
                param = link2
            )
        )

        val a = Block(
            id = MockDataFactory.randomUuid(),
            children = listOf(),
            content = Block.Content.Text(
                text = source,
                style = Block.Content.Text.Style.P,
                marks = marks,
                align = Block.Align.AlignLeft
            ),
            fields = Block.Fields.empty()
        )

        val page = Block(
            id = MockDataFactory.randomUuid(),
            children = listOf(header.id, a.id),
            fields = Block.Fields.empty(),
            content = Block.Content.Smart()
        )

        val randomEmoji1 = DefaultDocumentEmojiIconProvider.DOCUMENT_SET.random()
        val fieldsUpdated1 = Block.Fields(
            mapOf(
                DetailsKeys.ICON_EMOJI to randomEmoji1
            )
        )

        val randomEmoji2 = DefaultDocumentEmojiIconProvider.DOCUMENT_SET.random()
        val fieldsUpdated2 = Block.Fields(
            mapOf(
                DetailsKeys.ICON_EMOJI to randomEmoji2
            )
        )

        val detailsAmend = mapOf(
            mentionTarget1 to fieldsUpdated1,
            mentionTarget2 to fieldsUpdated2
        )

        val blocks = listOf(page, header, title, a)

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
                details = Block.Details(detailsAmend)
            )
        }

        val expected = listOf(
            BlockView.Title.Basic(
                id = title.id,
                isFocused = false,
                text = title.content<Block.Content.Text>().text,
                image = null,
                mode = BlockView.Mode.EDIT
            ),
            BlockView.Text.Paragraph(
                id = a.id,
                text = source,
                marks = listOf(
                    Markup.Mark.TextColor(
                        from = 0,
                        to = 5,
                        color = textColor
                    ),
                    Markup.Mark.Mention.WithEmoji(
                        from = 6,
                        to = 12,
                        param = mentionTarget1,
                        emoji = randomEmoji1
                    ),
                    Markup.Mark.Bold(
                        from = 13,
                        to = 19
                    ),
                    Markup.Mark.Link(
                        from = 20,
                        to = 23,
                        param = link1
                    ),
                    Markup.Mark.Italic(
                        from = 24,
                        to = 29
                    ),
                    Markup.Mark.Strikethrough(
                        from = 30,
                        to = 34
                    ),
                    Markup.Mark.Mention.WithEmoji(
                        from = 44,
                        to = 51,
                        param = mentionTarget2,
                        emoji = randomEmoji2
                    ),
                    Markup.Mark.Link(
                        from = 52,
                        to = 58,
                        param = link2
                    )
                ),
                isFocused = true,
                alignment = Alignment.START
            )
        )

        assertEquals(expected = expected, actual = result)
    }

    @Test//Proper test
    fun `should not update text and marks when no mentions present`() {

        val title = MockTypicalDocumentFactory.title
        val header = MockTypicalDocumentFactory.header

        val mentionText1 = "Foobar"
        val mentionText2 = "Anytype"
        val source = "Start $mentionText1 middle end Hdm5K 6511 xFMoTKqe $mentionText2 sNmO2f"
        val textColor = "F0So"
        val mentionTarget1 = "mc412Q8"
        val mentionTarget2 = "zd4h0852"
        val link1 = "zH45s"
        val link2 = "73EnYa"

        val marks: List<Block.Content.Text.Mark> = listOf(
            Block.Content.Text.Mark(
                range = 0..5,
                type = Block.Content.Text.Mark.Type.TEXT_COLOR,
                param = textColor
            ),
            Block.Content.Text.Mark(
                range = 13..19,
                type = Block.Content.Text.Mark.Type.BOLD
            ),
            Block.Content.Text.Mark(
                range = 20..23,
                type = Block.Content.Text.Mark.Type.LINK,
                param = link1
            ),
            Block.Content.Text.Mark(
                range = 24..29,
                type = Block.Content.Text.Mark.Type.ITALIC
            ),
            Block.Content.Text.Mark(
                range = 30..34,
                type = Block.Content.Text.Mark.Type.STRIKETHROUGH
            ),
            Block.Content.Text.Mark(
                range = 52..58,
                type = Block.Content.Text.Mark.Type.LINK,
                param = link2
            )
        )

        val a = Block(
            id = MockDataFactory.randomUuid(),
            children = listOf(),
            content = Block.Content.Text(
                text = source,
                style = Block.Content.Text.Style.P,
                marks = marks,
                align = Block.Align.AlignLeft
            ),
            fields = Block.Fields.empty()
        )

        val page = Block(
            id = MockDataFactory.randomUuid(),
            children = listOf(header.id, a.id),
            fields = Block.Fields.empty(),
            content = Block.Content.Smart()
        )

        val randomEmoji1 = DefaultDocumentEmojiIconProvider.DOCUMENT_SET.random()
        val fieldsUpdated1 = Block.Fields(
            mapOf(
                DetailsKeys.ICON_EMOJI to randomEmoji1
            )
        )

        val randomEmoji2 = DefaultDocumentEmojiIconProvider.DOCUMENT_SET.random()
        val fieldsUpdated2 = Block.Fields(
            mapOf(
                DetailsKeys.ICON_EMOJI to randomEmoji2
            )
        )

        val detailsAmend = mapOf(
            mentionTarget1 to fieldsUpdated1,
            mentionTarget2 to fieldsUpdated2
        )

        val blocks = listOf(page, header, title, a)

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
                details = Block.Details(detailsAmend)
            )
        }

        val expected = listOf(
            BlockView.Title.Basic(
                id = title.id,
                isFocused = false,
                text = title.content<Block.Content.Text>().text,
                image = null,
                mode = BlockView.Mode.EDIT
            ),
            BlockView.Text.Paragraph(
                id = a.id,
                text = source,
                marks = listOf(
                    Markup.Mark.TextColor(
                        from = 0,
                        to = 5,
                        color= textColor
                    ),
                    Markup.Mark.Bold(
                        from = 13,
                        to = 19
                    ),
                    Markup.Mark.Link(
                        from = 20,
                        to = 23,
                        param = link1
                    ),
                    Markup.Mark.Italic(
                        from = 24,
                        to = 29
                    ),
                    Markup.Mark.Strikethrough(
                        from = 30,
                        to = 34
                    ),
                    Markup.Mark.Link(
                        from = 52,
                        to = 58,
                        param = link2
                    )
                ),
                isFocused = true,
                alignment = Alignment.START
            )
        )

        assertEquals(expected = expected, actual = result)
    }

    @Test//Proper test
    fun `should update also marks before mention`() {

        val title = MockTypicalDocumentFactory.title
        val header = MockTypicalDocumentFactory.header

        val mentionText1 = "FooBar"
        val mentionText2 = "FooBa"
        val source = "Start $mentionText1 end"
        val mentionTarget1 = "mc412Q8"

        val marks: List<Block.Content.Text.Mark> = listOf(
            Block.Content.Text.Mark(
                range = 0..16,
                type = Block.Content.Text.Mark.Type.BOLD
            ),
            Block.Content.Text.Mark(
                range = 6..12,
                type = Block.Content.Text.Mark.Type.MENTION,
                param = mentionTarget1
            )
        )

        val a = Block(
            id = MockDataFactory.randomUuid(),
            children = listOf(),
            content = Block.Content.Text(
                text = source,
                style = Block.Content.Text.Style.P,
                marks = marks,
                align = Block.Align.AlignLeft
            ),
            fields = Block.Fields.empty()
        )

        val page = Block(
            id = MockDataFactory.randomUuid(),
            children = listOf(header.id, a.id),
            fields = Block.Fields.empty(),
            content = Block.Content.Smart()
        )

        val fieldsUpdated1 = Block.Fields(mapOf(Relations.NAME to mentionText2))

        val detailsAmend = mapOf(mentionTarget1 to fieldsUpdated1,)

        val blocks = listOf(page, header, title, a)

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
                details = Block.Details(detailsAmend)
            )
        }

        val expected = listOf(
            BlockView.Title.Basic(
                id = title.id,
                isFocused = false,
                text = title.content<Block.Content.Text>().text,
                image = null,
                mode = BlockView.Mode.EDIT
            ),
            BlockView.Text.Paragraph(
                id = a.id,
                text = "Start $mentionText2 end",
                marks = listOf(
                    Markup.Mark.Bold(
                        from = 0,
                        to = 15
                    ),
                    Markup.Mark.Mention.Base(
                        from = 6,
                        to = 11,
                        param = mentionTarget1
                    )
                ),
                isFocused = true,
                alignment = Alignment.START
            )
        )

        assertEquals(expected = expected, actual = result)
    }

    @Test
    fun `should set not existed mention in text and shift all markups`() {

        val title = MockTypicalDocumentFactory.title
        val header = MockTypicalDocumentFactory.header

        val mentionText1 = "Foobar"
        val mentionTextUpdated1 = NON_EXISTENT_OBJECT_MENTION_NAME //Non-existent object
        val mentionText2 = "Anytype"
        val mentionTextUpdated2 = "Anyt"
        val source = "Start $mentionText1 middle end Hdm5K 6511 xFMoTKqe $mentionText2 sNmO2f"
        val sourceUpdated =
            "Start $mentionTextUpdated1 middle end Hdm5K 6511 xFMoTKqe $mentionTextUpdated2 sNmO2f"
        val textColor = "F0So"
        val mentionTarget1 = "mc412Q8"
        val mentionTarget2 = "zd4h0852"
        val link1 = "zH45s"
        val link2 = "73EnYa"

        val marks: List<Block.Content.Text.Mark> = listOf(
            Block.Content.Text.Mark(
                range = 0..5,
                type = Block.Content.Text.Mark.Type.TEXT_COLOR,
                param = textColor
            ),
            Block.Content.Text.Mark(
                range = 6..12,
                type = Block.Content.Text.Mark.Type.MENTION,
                param = mentionTarget1
            ),
            Block.Content.Text.Mark(
                range = 13..19,
                type = Block.Content.Text.Mark.Type.BOLD
            ),
            Block.Content.Text.Mark(
                range = 20..23,
                type = Block.Content.Text.Mark.Type.LINK,
                param = link1
            ),
            Block.Content.Text.Mark(
                range = 24..29,
                type = Block.Content.Text.Mark.Type.ITALIC
            ),
            Block.Content.Text.Mark(
                range = 30..34,
                type = Block.Content.Text.Mark.Type.STRIKETHROUGH
            ),
            Block.Content.Text.Mark(
                range = 44..51,
                type = Block.Content.Text.Mark.Type.MENTION,
                param = mentionTarget2
            ),
            Block.Content.Text.Mark(
                range = 52..58,
                type = Block.Content.Text.Mark.Type.LINK,
                param = link2
            )
        )

        val a = Block(
            id = MockDataFactory.randomUuid(),
            children = listOf(),
            content = Block.Content.Text(
                text = source,
                style = Block.Content.Text.Style.P,
                marks = marks,
                align = Block.Align.AlignLeft
            ),
            fields = Block.Fields.empty()
        )

        val page = Block(
            id = MockDataFactory.randomUuid(),
            children = listOf(header.id, a.id),
            fields = Block.Fields.empty(),
            content = Block.Content.Smart()
        )

        val randomEmoji1 = DefaultDocumentEmojiIconProvider.DOCUMENT_SET.random()
        val fieldsUpdated1 = Block.Fields(
            mapOf(
                Block.Fields.NAME_KEY to mentionTextUpdated1,
                Relations.IS_DELETED to true,
                DetailsKeys.ICON_EMOJI to randomEmoji1
            )
        )

        val randomEmoji2 = DefaultDocumentEmojiIconProvider.DOCUMENT_SET.random()
        val fieldsUpdated2 = Block.Fields(
            mapOf(
                Block.Fields.NAME_KEY to mentionTextUpdated2,
                DetailsKeys.ICON_EMOJI to randomEmoji2
            )
        )

        val detailsAmend = mapOf(
            mentionTarget1 to fieldsUpdated1,
            mentionTarget2 to fieldsUpdated2
        )

        val blocks = listOf(page, header, title, a)

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
                details = Block.Details(detailsAmend)
            )
        }

        val expected = listOf(
            BlockView.Title.Basic(
                id = title.id,
                isFocused = false,
                text = title.content<Block.Content.Text>().text,
                image = null,
                mode = BlockView.Mode.EDIT
            ),
            BlockView.Text.Paragraph(
                id = a.id,
                text = sourceUpdated,
                marks = listOf(
                    Markup.Mark.TextColor(
                        from = 0,
                        to = 5,
                        color = textColor
                    ),
                    Markup.Mark.Mention.Deleted(
                        from = 6,
                        to = 25,
                        param = mentionTarget1
                    ),
                    Markup.Mark.Bold(
                        from = 26,
                        to = 32
                    ),
                    Markup.Mark.Link(
                        from = 33,
                        to = 36,
                        param = link1
                    ),
                    Markup.Mark.Italic(
                        from = 37,
                        to = 42
                    ),
                    Markup.Mark.Strikethrough(
                        from = 43,
                        to = 47
                    ),
                    Markup.Mark.Mention.WithEmoji(
                        from = 57,
                        to = 61,
                        param = mentionTarget2,
                        emoji = randomEmoji2
                    ),
                    Markup.Mark.Link(
                        from = 62,
                        to = 68,
                        param = link2
                    )
                ),
                isFocused = true,
                alignment = Alignment.START
            )
        )

        assertEquals(expected = expected, actual = result)
    }

    //region NUMBERED

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

    @Test
    fun `should render list, nested list, then continue list`() {

        val title = Block(
            id = "titleId",
            content = Block.Content.Text(
                text = "Title",
                style = Block.Content.Text.Style.TITLE,
                marks = emptyList()
            ),
            children = emptyList(),
            fields = Block.Fields.empty()
        )

        val header = Block(
            id = "headerId",
            content = Block.Content.Layout(
                type = Block.Content.Layout.Type.HEADER
            ),
            fields = Block.Fields.empty(),
            children = listOf(title.id)
        )

        val a1 = Block(
            id = "a1",
            children = listOf(),
            content = Block.Content.Text(
                text = "A1",
                style = Block.Content.Text.Style.NUMBERED,
                marks = emptyList(),
                align = Block.Align.AlignCenter
            ),
            fields = Block.Fields.empty()
        )

        val b1 = Block(
            id = "b1",
            children = listOf(),
            content = Block.Content.Text(
                text = "B1",
                style = Block.Content.Text.Style.NUMBERED,
                marks = emptyList(),
                align = Block.Align.AlignCenter
            ),
            fields = Block.Fields.empty()
        )

        val b2 = Block(
            id = "b2",
            children = listOf(),
            content = Block.Content.Text(
                text = "B2",
                style = Block.Content.Text.Style.NUMBERED,
                marks = emptyList(),
                align = Block.Align.AlignCenter
            ),
            fields = Block.Fields.empty()
        )

        val b3 = Block(
            id = "b3",
            children = listOf(),
            content = Block.Content.Text(
                text = "B3",
                style = Block.Content.Text.Style.NUMBERED,
                marks = emptyList(),
                align = Block.Align.AlignCenter
            ),
            fields = Block.Fields.empty()
        )

        val a2 = Block(
            id = "a2",
            children = listOf(b1.id, b2.id, b3.id),
            content = Block.Content.Text(
                text = "A2",
                style = Block.Content.Text.Style.NUMBERED,
                marks = emptyList(),
                align = Block.Align.AlignCenter
            ),
            fields = Block.Fields.empty()
        )

        val a3 = Block(
            id = "a3",
            children = listOf(),
            content = Block.Content.Text(
                text = "A3",
                style = Block.Content.Text.Style.NUMBERED,
                marks = emptyList(),
                align = Block.Align.AlignCenter
            ),
            fields = Block.Fields.empty()
        )

        val a4 = Block(
            id = "a4",
            children = listOf(),
            content = Block.Content.Text(
                text = "A4",
                style = Block.Content.Text.Style.NUMBERED,
                marks = emptyList(),
                align = Block.Align.AlignCenter
            ),
            fields = Block.Fields.empty()
        )

        val page = Block(
            id = "objectId",
            children = listOf(header.id, a1.id, a2.id, a3.id, a4.id),
            fields = Block.Fields.empty(),
            content = Block.Content.Smart()
        )

        val details = mapOf(page.id to Block.Fields.empty())

        val blocks = listOf(page, header, title, a1, a2, a3, a4, b1, b2, b3)

        val map = blocks.asMap()

        wrapper = BlockViewRenderWrapper(
            blocks = map,
            renderer = renderer
        )

        val result = runBlocking {
            wrapper.render(
                root = page,
                anchor = page.id,
                focus = Editor.Focus.empty(),
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
                isFocused = false,
                id = a1.id,
                marks = emptyList(),
                backgroundColor = a1.content<Block.Content.Text>().backgroundColor,
                color = a1.content<Block.Content.Text>().color,
                text = a1.content<Block.Content.Text>().text,
                number = 1
            ),
            BlockView.Text.Numbered(
                indent = 0,
                isFocused = false,
                id = a2.id,
                marks = emptyList(),
                backgroundColor = a2.content<Block.Content.Text>().backgroundColor,
                color = a2.content<Block.Content.Text>().color,
                text = a2.content<Block.Content.Text>().text,
                number = 2
            ),
            BlockView.Text.Numbered(
                indent = 1,
                isFocused = false,
                id = b1.id,
                marks = emptyList(),
                backgroundColor = b1.content<Block.Content.Text>().backgroundColor,
                color = b1.content<Block.Content.Text>().color,
                text = b1.content<Block.Content.Text>().text,
                number = 1
            ),
            BlockView.Text.Numbered(
                indent = 1,
                isFocused = false,
                id = b2.id,
                marks = emptyList(),
                backgroundColor = b2.content<Block.Content.Text>().backgroundColor,
                color = b2.content<Block.Content.Text>().color,
                text = b2.content<Block.Content.Text>().text,
                number = 2
            ),
            BlockView.Text.Numbered(
                indent = 1,
                isFocused = false,
                id = b3.id,
                marks = emptyList(),
                backgroundColor = b3.content<Block.Content.Text>().backgroundColor,
                color = b3.content<Block.Content.Text>().color,
                text = b3.content<Block.Content.Text>().text,
                number = 3
            ),
            BlockView.Text.Numbered(
                indent = 0,
                isFocused = false,
                id = a3.id,
                marks = emptyList(),
                backgroundColor = a3.content<Block.Content.Text>().backgroundColor,
                color = a3.content<Block.Content.Text>().color,
                text = a3.content<Block.Content.Text>().text,
                number = 3
            ),
            BlockView.Text.Numbered(
                indent = 0,
                isFocused = false,
                id = a4.id,
                marks = emptyList(),
                backgroundColor = a4.content<Block.Content.Text>().backgroundColor,
                color = a4.content<Block.Content.Text>().color,
                text = a4.content<Block.Content.Text>().text,
                number = 4
            ),
        )

        assertEquals(expected = expected, actual = result)
    }

    @Test
    fun `should render list, nested list with only one child, then continue list`() {

        val title = Block(
            id = "titleId",
            content = Block.Content.Text(
                text = "Title",
                style = Block.Content.Text.Style.TITLE,
                marks = emptyList()
            ),
            children = emptyList(),
            fields = Block.Fields.empty()
        )

        val header = Block(
            id = "headerId",
            content = Block.Content.Layout(
                type = Block.Content.Layout.Type.HEADER
            ),
            fields = Block.Fields.empty(),
            children = listOf(title.id)
        )

        val a1 = Block(
            id = "a1",
            children = listOf(),
            content = Block.Content.Text(
                text = "A1",
                style = Block.Content.Text.Style.NUMBERED,
                marks = emptyList(),
                align = Block.Align.AlignCenter
            ),
            fields = Block.Fields.empty()
        )

        val b1 = Block(
            id = "b1",
            children = listOf(),
            content = Block.Content.Text(
                text = "B1",
                style = Block.Content.Text.Style.NUMBERED,
                marks = emptyList(),
                align = Block.Align.AlignCenter
            ),
            fields = Block.Fields.empty()
        )

        val a2 = Block(
            id = "a2",
            children = listOf(b1.id),
            content = Block.Content.Text(
                text = "A2",
                style = Block.Content.Text.Style.NUMBERED,
                marks = emptyList(),
                align = Block.Align.AlignCenter
            ),
            fields = Block.Fields.empty()
        )

        val a3 = Block(
            id = "a3",
            children = listOf(),
            content = Block.Content.Text(
                text = "A3",
                style = Block.Content.Text.Style.NUMBERED,
                marks = emptyList(),
                align = Block.Align.AlignCenter
            ),
            fields = Block.Fields.empty()
        )

        val a4 = Block(
            id = "a4",
            children = listOf(),
            content = Block.Content.Text(
                text = "A4",
                style = Block.Content.Text.Style.NUMBERED,
                marks = emptyList(),
                align = Block.Align.AlignCenter
            ),
            fields = Block.Fields.empty()
        )

        val page = Block(
            id = "objectId",
            children = listOf(header.id, a1.id, a2.id, a3.id, a4.id),
            fields = Block.Fields.empty(),
            content = Block.Content.Smart()
        )

        val details = mapOf(page.id to Block.Fields.empty())

        val blocks = listOf(page, header, title, a1, a2, a3, a4, b1)

        val map = blocks.asMap()

        wrapper = BlockViewRenderWrapper(
            blocks = map,
            renderer = renderer
        )

        val result = runBlocking {
            wrapper.render(
                root = page,
                anchor = page.id,
                focus = Editor.Focus.empty(),
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
                isFocused = false,
                id = a1.id,
                marks = emptyList(),
                backgroundColor = a1.content<Block.Content.Text>().backgroundColor,
                color = a1.content<Block.Content.Text>().color,
                text = a1.content<Block.Content.Text>().text,
                number = 1
            ),
            BlockView.Text.Numbered(
                indent = 0,
                isFocused = false,
                id = a2.id,
                marks = emptyList(),
                backgroundColor = a2.content<Block.Content.Text>().backgroundColor,
                color = a2.content<Block.Content.Text>().color,
                text = a2.content<Block.Content.Text>().text,
                number = 2
            ),
            BlockView.Text.Numbered(
                indent = 1,
                isFocused = false,
                id = b1.id,
                marks = emptyList(),
                backgroundColor = b1.content<Block.Content.Text>().backgroundColor,
                color = b1.content<Block.Content.Text>().color,
                text = b1.content<Block.Content.Text>().text,
                number = 1
            ),
            BlockView.Text.Numbered(
                indent = 0,
                isFocused = false,
                id = a3.id,
                marks = emptyList(),
                backgroundColor = a3.content<Block.Content.Text>().backgroundColor,
                color = a3.content<Block.Content.Text>().color,
                text = a3.content<Block.Content.Text>().text,
                number = 3
            ),
            BlockView.Text.Numbered(
                indent = 0,
                isFocused = false,
                id = a4.id,
                marks = emptyList(),
                backgroundColor = a4.content<Block.Content.Text>().backgroundColor,
                color = a4.content<Block.Content.Text>().color,
                text = a4.content<Block.Content.Text>().text,
                number = 4
            ),
        )

        assertEquals(expected = expected, actual = result)
    }

    @Test
    fun `should render numbered list divided by divs`() {

        val title = Block(
            id = "titleId",
            content = Block.Content.Text(
                text = "Title",
                style = Block.Content.Text.Style.TITLE,
                marks = emptyList()
            ),
            children = emptyList(),
            fields = Block.Fields.empty()
        )

        val header = Block(
            id = "headerId",
            content = Block.Content.Layout(
                type = Block.Content.Layout.Type.HEADER
            ),
            fields = Block.Fields.empty(),
            children = listOf(title.id)
        )

        val numbered = mutableListOf<Block>()

        repeat(100) { idx ->
            numbered.add(
                Block(
                    id = "block-${idx.inc()}",
                    content = Block.Content.Text(
                        text = idx.inc().toString(),
                        marks = emptyList(),
                        style = Block.Content.Text.Style.NUMBERED
                    ),
                    children = emptyList(),
                    fields = Block.Fields.empty()
                )
            )
        }

        val fields = Block.Fields.empty()


        val div1 = Block(
            id = "div1Id",
            children = numbered.subList(0, 25).map { it.id },
            content = Block.Content.Layout(
                type = Block.Content.Layout.Type.DIV
            ),
            fields = Block.Fields.empty()
        )

        val div2 = Block(
            id = "div2Id",
            children = numbered.subList(25, 50).map { it.id },
            content = Block.Content.Layout(
                type = Block.Content.Layout.Type.DIV
            ),
            fields = Block.Fields.empty()
        )

        val div3 = Block(
            id = "div3Id",
            children = numbered.subList(50, 75).map { it.id },
            content = Block.Content.Layout(
                type = Block.Content.Layout.Type.DIV
            ),
            fields = Block.Fields.empty()
        )

        val div4 = Block(
            id = "div4Id",
            children = numbered.subList(75, 100).map { it.id },
            content = Block.Content.Layout(
                type = Block.Content.Layout.Type.DIV
            ),
            fields = Block.Fields.empty()
        )

        val page = Block(
            id = "root",
            children = listOf(header.id) + listOf(div1.id, div2.id, div3.id, div4.id),
            fields = fields,
            content = Block.Content.Smart()
        )

        val details = mapOf(page.id to fields)

        val blocks = listOf(page, header, title) + listOf(div1, div2, div3, div4) + numbered

        val map = blocks.asMap()

        wrapper = BlockViewRenderWrapper(
            blocks = map,
            renderer = renderer
        )

        val result = runBlocking {
            wrapper.render(
                root = page,
                anchor = page.id,
                focus = Editor.Focus.empty(),
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
            )
        ) + numbered.mapIndexed { idx, block ->
            BlockView.Text.Numbered(
                id = block.id,
                text = block.content<TXT>().text,
                number = idx.inc()
            )
        }

        assertEquals(expected = expected, actual = result)
    }

    @Test
    fun `should render nested numbered list including other text blocks`() {

        val title = Block(
            id = "titleId",
            content = Block.Content.Text(
                text = "Title",
                style = Block.Content.Text.Style.TITLE,
                marks = emptyList()
            ),
            children = emptyList(),
            fields = Block.Fields.empty()
        )

        val header = Block(
            id = "headerId",
            content = Block.Content.Layout(
                type = Block.Content.Layout.Type.HEADER
            ),
            fields = Block.Fields.empty(),
            children = listOf(title.id)
        )

        val a1 = Block(
            id = "a1",
            children = listOf(),
            content = Block.Content.Text(
                text = "A1",
                style = Block.Content.Text.Style.NUMBERED,
                marks = emptyList(),
                align = Block.Align.AlignCenter
            ),
            fields = Block.Fields.empty()
        )

        val c1 = Block(
            id = "c1",
            children = listOf(),
            content = Block.Content.Text(
                text = "C1",
                style = Block.Content.Text.Style.P,
                marks = emptyList(),
                align = Block.Align.AlignCenter
            ),
            fields = Block.Fields.empty()
        )

        val c2 = Block(
            id = "c2",
            children = listOf(),
            content = Block.Content.Text(
                text = "C2",
                style = Block.Content.Text.Style.NUMBERED,
                marks = emptyList(),
                align = Block.Align.AlignCenter
            ),
            fields = Block.Fields.empty()
        )

        val c3 = Block(
            id = "c3",
            children = listOf(),
            content = Block.Content.Text(
                text = "C3",
                style = Block.Content.Text.Style.NUMBERED,
                marks = emptyList(),
                align = Block.Align.AlignCenter
            ),
            fields = Block.Fields.empty()
        )

        val c4 = Block(
            id = "c4",
            children = listOf(),
            content = Block.Content.Text(
                text = "C4",
                style = Block.Content.Text.Style.NUMBERED,
                marks = emptyList(),
                align = Block.Align.AlignCenter
            ),
            fields = Block.Fields.empty()
        )

        val b1 = Block(
            id = "b1",
            children = listOf(c1.id, c2.id, c3.id, c4.id),
            content = Block.Content.Text(
                text = "B1",
                style = Block.Content.Text.Style.NUMBERED,
                marks = emptyList(),
                align = Block.Align.AlignCenter
            ),
            fields = Block.Fields.empty()
        )

        val a2 = Block(
            id = "a2",
            children = listOf(b1.id),
            content = Block.Content.Text(
                text = "A2",
                style = Block.Content.Text.Style.NUMBERED,
                marks = emptyList(),
                align = Block.Align.AlignCenter
            ),
            fields = Block.Fields.empty()
        )

        val a3 = Block(
            id = "a3",
            children = listOf(),
            content = Block.Content.Text(
                text = "A3",
                style = Block.Content.Text.Style.NUMBERED,
                marks = emptyList(),
                align = Block.Align.AlignCenter
            ),
            fields = Block.Fields.empty()
        )

        val a4 = Block(
            id = "a4",
            children = listOf(),
            content = Block.Content.Text(
                text = "A4",
                style = Block.Content.Text.Style.NUMBERED,
                marks = emptyList(),
                align = Block.Align.AlignCenter
            ),
            fields = Block.Fields.empty()
        )

        val page = Block(
            id = "objectId",
            children = listOf(header.id, a1.id, a2.id, a3.id, a4.id),
            fields = Block.Fields.empty(),
            content = Block.Content.Smart()
        )

        val details = mapOf(page.id to Block.Fields.empty())

        val blocks = listOf(page, header, title, a1, a2, a3, a4, b1, c1, c2, c3, c4)

        val map = blocks.asMap()

        wrapper = BlockViewRenderWrapper(
            blocks = map,
            renderer = renderer
        )

        val result = runBlocking {
            wrapper.render(
                root = page,
                anchor = page.id,
                focus = Editor.Focus.empty(),
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
                isFocused = false,
                id = a1.id,
                marks = emptyList(),
                backgroundColor = a1.content<Block.Content.Text>().backgroundColor,
                color = a1.content<Block.Content.Text>().color,
                text = a1.content<Block.Content.Text>().text,
                number = 1
            ),
            BlockView.Text.Numbered(
                indent = 0,
                isFocused = false,
                id = a2.id,
                marks = emptyList(),
                backgroundColor = a2.content<Block.Content.Text>().backgroundColor,
                color = a2.content<Block.Content.Text>().color,
                text = a2.content<Block.Content.Text>().text,
                number = 2
            ),
            BlockView.Text.Numbered(
                indent = 1,
                isFocused = false,
                id = b1.id,
                marks = emptyList(),
                backgroundColor = b1.content<Block.Content.Text>().backgroundColor,
                color = b1.content<Block.Content.Text>().color,
                text = b1.content<Block.Content.Text>().text,
                number = 1
            ),
            BlockView.Text.Paragraph(
                indent = 2,
                isFocused = false,
                id = c1.id,
                marks = emptyList(),
                backgroundColor = c1.content<Block.Content.Text>().backgroundColor,
                color = c1.content<Block.Content.Text>().color,
                text = c1.content<Block.Content.Text>().text,
                alignment = Alignment.CENTER
            ),
            BlockView.Text.Numbered(
                indent = 2,
                isFocused = false,
                id = c2.id,
                marks = emptyList(),
                backgroundColor = c2.content<Block.Content.Text>().backgroundColor,
                color = c2.content<Block.Content.Text>().color,
                text = c2.content<Block.Content.Text>().text,
                number = 1
            ),
            BlockView.Text.Numbered(
                indent = 2,
                isFocused = false,
                id = c3.id,
                marks = emptyList(),
                backgroundColor = c3.content<Block.Content.Text>().backgroundColor,
                color = c3.content<Block.Content.Text>().color,
                text = c3.content<Block.Content.Text>().text,
                number = 2
            ),
            BlockView.Text.Numbered(
                indent = 2,
                isFocused = false,
                id = c4.id,
                marks = emptyList(),
                backgroundColor = c4.content<Block.Content.Text>().backgroundColor,
                color = c4.content<Block.Content.Text>().color,
                text = c4.content<Block.Content.Text>().text,
                number = 3
            ),
            BlockView.Text.Numbered(
                indent = 0,
                isFocused = false,
                id = a3.id,
                marks = emptyList(),
                backgroundColor = a3.content<Block.Content.Text>().backgroundColor,
                color = a3.content<Block.Content.Text>().color,
                text = a3.content<Block.Content.Text>().text,
                number = 3
            ),
            BlockView.Text.Numbered(
                indent = 0,
                isFocused = false,
                id = a4.id,
                marks = emptyList(),
                backgroundColor = a4.content<Block.Content.Text>().backgroundColor,
                color = a4.content<Block.Content.Text>().color,
                text = a4.content<Block.Content.Text>().text,
                number = 4
            ),
        )

        assertEquals(expected = expected, actual = result)
    }

    //endregion
}