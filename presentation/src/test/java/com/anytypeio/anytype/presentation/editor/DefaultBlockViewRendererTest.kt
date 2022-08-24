package com.anytypeio.anytype.presentation.editor

import android.util.Log
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Block.Content.Link
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.SmartBlockType
import com.anytypeio.anytype.core_models.StubBookmark
import com.anytypeio.anytype.core_models.StubCallout
import com.anytypeio.anytype.core_models.StubFile
import com.anytypeio.anytype.core_models.StubParagraph
import com.anytypeio.anytype.core_models.StubSmartBlock
import com.anytypeio.anytype.core_models.ext.asMap
import com.anytypeio.anytype.core_models.ext.content
import com.anytypeio.anytype.core_models.restrictions.ObjectRestriction
import com.anytypeio.anytype.core_utils.const.DetailsKeys
import com.anytypeio.anytype.domain.config.Gateway
import com.anytypeio.anytype.domain.editor.Editor
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.emojifier.data.DefaultDocumentEmojiIconProvider
import com.anytypeio.anytype.presentation.BuildConfig
import com.anytypeio.anytype.presentation.MockBlockContentFactory.StubLinkContent
import com.anytypeio.anytype.presentation.MockBlockFactory.link
import com.anytypeio.anytype.presentation.MockTypicalDocumentFactory
import com.anytypeio.anytype.presentation.editor.cover.CoverImageHashProvider
import com.anytypeio.anytype.presentation.editor.editor.Markup
import com.anytypeio.anytype.presentation.editor.editor.Markup.Companion.NON_EXISTENT_OBJECT_MENTION_NAME
import com.anytypeio.anytype.core_models.ThemeColor
import com.anytypeio.anytype.core_models.ext.parseThemeTextColor
import com.anytypeio.anytype.presentation.editor.editor.model.Alignment
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.render.BlockViewRenderer
import com.anytypeio.anytype.presentation.editor.render.DefaultBlockViewRenderer
import com.anytypeio.anytype.presentation.editor.render.NestedDecorationData
import com.anytypeio.anytype.presentation.editor.render.parseThemeBackgroundColor
import com.anytypeio.anytype.presentation.editor.toggle.ToggleStateHolder
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.util.TXT
import com.anytypeio.anytype.test_utils.MockDataFactory
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
            details: Block.Details,
            schema: NestedDecorationData = emptyList()
        ): List<BlockView> = blocks.render(
            root = root,
            focus = focus,
            anchor = anchor,
            indent = indent,
            details = details,
            relations = emptyList(),
            restrictions = restrictions,
            selection = emptySet(),
            objectTypes = emptyList(),
            parentScheme = schema
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
    fun `should return title, paragraph, toggle without its indented inner checkbox`() {

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
                background = paragraph.parseThemeBackgroundColor(),
                color = paragraph.content<Block.Content.Text>().parseThemeTextColor(),
                text = paragraph.content<Block.Content.Text>().text,
                alignment = Alignment.START,
                indent = 0,
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(
                            background = paragraph.parseThemeBackgroundColor()
                        )
                    )
                } else {
                    emptyList()
                }
            ),
            BlockView.Text.Toggle(
                isEmpty = false,
                isFocused = false,
                toggled = false,
                id = toggle.id,
                marks = emptyList(),
                background = toggle.parseThemeBackgroundColor(),
                color = toggle.content<Block.Content.Text>().parseThemeTextColor(),
                text = toggle.content<Block.Content.Text>().text,
                indent = 0,
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(
                            background = toggle.parseThemeBackgroundColor()
                        )
                    )
                } else {
                    emptyList()
                }
            )
        )

        assertEquals(expected = expected.size, actual = result.size)
        assertEquals(expected = expected, actual = result)
    }

    @Test
    fun `should return title, paragraph, toggle with its inner checkbox`() {

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
                background = paragraph.parseThemeBackgroundColor(),
                color = paragraph.content<Block.Content.Text>().parseThemeTextColor(),
                text = paragraph.content<Block.Content.Text>().text,
                alignment = Alignment.END,
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(
                            background = paragraph.parseThemeBackgroundColor()
                        )
                    )
                } else {
                    emptyList()
                }
            ),
            BlockView.Text.Toggle(
                isEmpty = false,
                isFocused = false,
                toggled = true,
                id = toggle.id,
                marks = emptyList(),
                background = toggle.parseThemeBackgroundColor(),
                color = toggle.content<Block.Content.Text>().parseThemeTextColor(),
                text = toggle.content<Block.Content.Text>().text,
                indent = 0,
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(
                            background = toggle.parseThemeBackgroundColor()
                        )
                    )
                } else {
                    emptyList()
                }
            ),
            BlockView.Text.Checkbox(
                isFocused = false,
                id = checkbox.id,
                marks = emptyList(),
                background = checkbox.parseThemeBackgroundColor(),
                color = checkbox.content<Block.Content.Text>().parseThemeTextColor(),
                text = checkbox.content<Block.Content.Text>().text,
                indent = 1,
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(
                            background = paragraph.parseThemeBackgroundColor()
                        ),
                        BlockView.Decoration(
                            background = toggle.parseThemeBackgroundColor()
                        )
                    )
                } else {
                    emptyList()
                }
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
                background = paragraph.parseThemeBackgroundColor(),
                color = paragraph.content<Block.Content.Text>().parseThemeTextColor(),
                text = paragraph.content<Block.Content.Text>().text,
                alignment = null,
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(
                            background = paragraph.parseThemeBackgroundColor()
                        )
                    )
                } else {
                    emptyList()
                }
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
                background = paragraph.parseThemeBackgroundColor(),
                color = paragraph.content<Block.Content.Text>().parseThemeTextColor(),
                text = paragraph.content<Block.Content.Text>().text,
                alignment = Alignment.CENTER,
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(
                            background = paragraph.parseThemeBackgroundColor()
                        )
                    )
                } else {
                    emptyList()
                }
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
                background = paragraph.parseThemeBackgroundColor(),
                color = paragraph.content<Block.Content.Text>().parseThemeTextColor(),
                text = paragraph.content<Block.Content.Text>().text,
                alignment = Alignment.CENTER,
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(
                            background = paragraph.parseThemeBackgroundColor()
                        )
                    )
                } else {
                    emptyList()
                }
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
                background = paragraph.parseThemeBackgroundColor(),
                color = paragraph.content<Block.Content.Text>().parseThemeTextColor(),
                text = paragraph.content<Block.Content.Text>().text,
                alignment = Alignment.CENTER,
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(
                            background = paragraph.parseThemeBackgroundColor()
                        )
                    )
                } else {
                    emptyList()
                }
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
    fun `should render three paragraphs nested one inside another`() {

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
                background = a.parseThemeBackgroundColor(),
                color = a.content<Block.Content.Text>().parseThemeTextColor(),
                text = a.content<Block.Content.Text>().text,
                alignment = Alignment.CENTER,
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(
                            background = a.parseThemeBackgroundColor()
                        )
                    )
                } else {
                    emptyList()
                }
            ),
            BlockView.Text.Paragraph(
                indent = 1,
                isFocused = false,
                id = b.id,
                marks = emptyList(),
                background = b.parseThemeBackgroundColor(),
                color = b.content<Block.Content.Text>().parseThemeTextColor(),
                text = b.content<Block.Content.Text>().text,
                alignment = Alignment.CENTER,
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(
                            background = a.parseThemeBackgroundColor()
                        ),
                        BlockView.Decoration(
                            background = b.parseThemeBackgroundColor()
                        )
                    )
                } else {
                    emptyList()
                }
            ),
            BlockView.Text.Paragraph(
                indent = 2,
                isFocused = false,
                id = c.id,
                marks = emptyList(),
                background = c.parseThemeBackgroundColor(),
                color = c.content<Block.Content.Text>().parseThemeTextColor(),
                text = c.content<Block.Content.Text>().text,
                alignment = Alignment.CENTER,
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(
                            background = a.parseThemeBackgroundColor()
                        ),
                        BlockView.Decoration(
                            background = b.parseThemeBackgroundColor()
                        ),
                        BlockView.Decoration(
                            background = c.parseThemeBackgroundColor()
                        )
                    )
                } else {
                    emptyList()
                }
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
                background = a.parseThemeBackgroundColor(),
                color = a.content<Block.Content.Text>().parseThemeTextColor(),
                text = a.content<Block.Content.Text>().text,
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(
                            background = a.parseThemeBackgroundColor()
                        )
                    )
                } else {
                    emptyList()
                }
            ),
            BlockView.Text.Checkbox(
                indent = 1,
                isFocused = false,
                id = b.id,
                marks = emptyList(),
                background = b.parseThemeBackgroundColor(),
                color = b.content<Block.Content.Text>().parseThemeTextColor(),
                text = b.content<Block.Content.Text>().text,
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(
                            background = a.parseThemeBackgroundColor()
                        ),
                        BlockView.Decoration(
                            background = b.parseThemeBackgroundColor()
                        )
                    )
                } else {
                    emptyList()
                }
            ),
            BlockView.Text.Checkbox(
                indent = 2,
                isFocused = false,
                id = c.id,
                marks = emptyList(),
                background = c.parseThemeBackgroundColor(),
                color = c.content<Block.Content.Text>().parseThemeTextColor(),
                text = c.content<Block.Content.Text>().text,
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(
                            background = a.parseThemeBackgroundColor()
                        ),
                        BlockView.Decoration(
                            background = b.parseThemeBackgroundColor()
                        ),
                        BlockView.Decoration(
                            background = c.parseThemeBackgroundColor()
                        )
                    )
                } else {
                    emptyList()
                }
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
                background = a.parseThemeBackgroundColor(),
                color = a.content<Block.Content.Text>().parseThemeTextColor(),
                text = a.content<Block.Content.Text>().text,
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(
                            background = a.parseThemeBackgroundColor()
                        )
                    )
                } else {
                    emptyList()
                }
            ),
            BlockView.Text.Bulleted(
                indent = 1,
                isFocused = false,
                id = b.id,
                marks = emptyList(),
                background = b.parseThemeBackgroundColor(),
                color = b.content<Block.Content.Text>().parseThemeTextColor(),
                text = b.content<Block.Content.Text>().text,
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(
                            background = a.parseThemeBackgroundColor()
                        ),
                        BlockView.Decoration(
                            background = b.parseThemeBackgroundColor()
                        )
                    )
                } else {
                    emptyList()
                }
            ),
            BlockView.Text.Bulleted(
                indent = 2,
                isFocused = false,
                id = c.id,
                marks = emptyList(),
                background = c.parseThemeBackgroundColor(),
                color = c.content<Block.Content.Text>().parseThemeTextColor(),
                text = c.content<Block.Content.Text>().text,
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(
                            background = a.parseThemeBackgroundColor()
                        ),
                        BlockView.Decoration(
                            background = b.parseThemeBackgroundColor()
                        ),
                        BlockView.Decoration(
                            background = c.parseThemeBackgroundColor()
                        )
                    )
                } else {
                    emptyList()
                }
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
                        emoji = randomEmoji1,
                        isArchived = false
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
                        emoji = randomEmoji2,
                        isArchived = false
                    ),
                    Markup.Mark.Link(
                        from = 56,
                        to = 62,
                        param = link2
                    )
                ),
                isFocused = true,
                alignment = Alignment.START,
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(
                            background = a.parseThemeBackgroundColor()
                        )
                    )
                } else {
                    emptyList()
                }
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
                        param = mentionTarget1,
                        isArchived = false
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
                        param = mentionTarget2,
                        isArchived = false
                    ),
                    Markup.Mark.Link(
                        from = 56,
                        to = 62,
                        param = link2
                    )
                ),
                isFocused = true,
                alignment = Alignment.START,
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(
                            background = a.parseThemeBackgroundColor()
                        )
                    )
                } else {
                    emptyList()
                }
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
                        param = mentionTarget1,
                        isArchived = false
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
                        emoji = randomEmoji2,
                        isArchived = false
                    ),
                    Markup.Mark.Link(
                        from = 56,
                        to = 62,
                        param = link2
                    )
                ),
                isFocused = true,
                alignment = Alignment.START,
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(
                            background = a.parseThemeBackgroundColor()
                        )
                    )
                } else {
                    emptyList()
                }
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
                        emoji = randomEmoji1,
                        isArchived = false
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
                        emoji = randomEmoji2,
                        isArchived = false
                    ),
                    Markup.Mark.Link(
                        from = 52,
                        to = 58,
                        param = link2
                    )
                ),
                isFocused = true,
                alignment = Alignment.START,
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(
                            background = a.parseThemeBackgroundColor()
                        )
                    )
                } else {
                    emptyList()
                }
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
                        color = textColor
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
                alignment = Alignment.START,
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(
                            background = a.parseThemeBackgroundColor()
                        )
                    )
                } else {
                    emptyList()
                }
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

        val detailsAmend = mapOf(mentionTarget1 to fieldsUpdated1)

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
                        param = mentionTarget1,
                        isArchived = false
                    )
                ),
                isFocused = true,
                alignment = Alignment.START,
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(
                            background = a.parseThemeBackgroundColor()
                        )
                    )
                } else {
                    emptyList()
                }
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
                        emoji = randomEmoji2,
                        isArchived = false
                    ),
                    Markup.Mark.Link(
                        from = 62,
                        to = 68,
                        param = link2
                    )
                ),
                isFocused = true,
                alignment = Alignment.START,
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(
                            background = a.parseThemeBackgroundColor()
                        )
                    )
                } else {
                    emptyList()
                }
            )
        )

        assertEquals(expected = expected, actual = result)
    }

    @Test
    fun `should render linkToObjectCard with proper params`() {
        val title = MockTypicalDocumentFactory.title
        val header = MockTypicalDocumentFactory.header
        val target = MockDataFactory.randomUuid()

        val a = link(
            content = StubLinkContent(
                target = target,
                cardStyle = Link.CardStyle.CARD,
                iconSize = Link.IconSize.NONE,
                description = Link.Description.ADDED,
                relations = setOf(Link.Relation.NAME)
            ),
            backgroundColor = "red"
        )

        val page = Block(
            id = MockDataFactory.randomUuid(),
            children = listOf(header.id, a.id),
            fields = Block.Fields.empty(),
            content = Block.Content.Smart()
        )

        val blocks = listOf(page, header, title, a)

        val map = blocks.asMap()

        val snippet = MockDataFactory.randomString()
        val name = MockDataFactory.randomString()

        val details = Block.Details(
            mapOf(
                target to Block.Fields(
                    mapOf(
                        "name" to name,
                        "description" to "",
                        "snippet" to snippet,
                        "layout" to ObjectType.Layout.BASIC.code.toDouble()
                    )
                )
            )
        )

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
                details = details
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
            BlockView.LinkToObject.Default.Card(
                id = a.id,
                icon = ObjectIcon.None,
                text = name,
                description = "",
                indent = 0,
                isSelected = false,
                coverColor = null,
                coverImage = null,
                coverGradient = null,
                background = a.parseThemeBackgroundColor(),
                isPreviousBlockMedia = false,
                objectTypeName = null,
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(
                            background = a.parseThemeBackgroundColor(),
                            style = BlockView.Decoration.Style.Card
                        )
                    )
                } else {
                    emptyList()
                }
            )
        )

        assertEquals(expected, result)
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
                background = a.parseThemeBackgroundColor(),
                color = a.content<Block.Content.Text>().parseThemeTextColor(),
                text = a.content<Block.Content.Text>().text,
                number = 1,
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(
                            background = a.parseThemeBackgroundColor()
                        )
                    )
                } else {
                    emptyList()
                }
            ),
            BlockView.Text.Numbered(
                indent = 1,
                isFocused = false,
                id = b.id,
                marks = emptyList(),
                background = b.parseThemeBackgroundColor(),
                color = b.content<Block.Content.Text>().parseThemeTextColor(),
                text = b.content<Block.Content.Text>().text,
                number = 1,
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(
                            background = a.parseThemeBackgroundColor()
                        ),
                        BlockView.Decoration(
                            background = b.parseThemeBackgroundColor()
                        )
                    )
                } else {
                    emptyList()
                }
            ),
            BlockView.Text.Numbered(
                indent = 2,
                isFocused = false,
                id = c.id,
                marks = emptyList(),
                background = c.parseThemeBackgroundColor(),
                color = c.content<Block.Content.Text>().parseThemeTextColor(),
                text = c.content<Block.Content.Text>().text,
                number = 1,
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(
                            background = a.parseThemeBackgroundColor()
                        ),
                        BlockView.Decoration(
                            background = b.parseThemeBackgroundColor()
                        ),
                        BlockView.Decoration(
                            background = b.parseThemeBackgroundColor()
                        )
                    )
                } else {
                    emptyList()
                }
            ),
            BlockView.Text.Numbered(
                indent = 2,
                isFocused = false,
                id = d.id,
                marks = emptyList(),
                background = d.parseThemeBackgroundColor(),
                color = d.content<Block.Content.Text>().parseThemeTextColor(),
                text = d.content<Block.Content.Text>().text,
                number = 2,
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(
                            background = a.parseThemeBackgroundColor()
                        ),
                        BlockView.Decoration(
                            background = b.parseThemeBackgroundColor()
                        ),
                        BlockView.Decoration(
                            background = b.parseThemeBackgroundColor()
                        )
                    )
                } else {
                    emptyList()
                }
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
                background = a1.parseThemeBackgroundColor(),
                color = a1.content<Block.Content.Text>().parseThemeTextColor(),
                text = a1.content<Block.Content.Text>().text,
                number = 1,
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(
                            background = a1.parseThemeBackgroundColor()
                        )
                    )
                } else {
                    emptyList()
                }
            ),
            BlockView.Text.Numbered(
                indent = 0,
                isFocused = false,
                id = a2.id,
                marks = emptyList(),
                background = a2.parseThemeBackgroundColor(),
                color = a2.content<Block.Content.Text>().parseThemeTextColor(),
                text = a2.content<Block.Content.Text>().text,
                number = 2,
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(
                            background = a2.parseThemeBackgroundColor()
                        )
                    )
                } else {
                    emptyList()
                }
            ),
            BlockView.Text.Numbered(
                indent = 1,
                isFocused = false,
                id = b1.id,
                marks = emptyList(),
                background = b1.parseThemeBackgroundColor(),
                color = b1.content<Block.Content.Text>().parseThemeTextColor(),
                text = b1.content<Block.Content.Text>().text,
                number = 1,
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(
                            background = a2.parseThemeBackgroundColor()
                        ),
                        BlockView.Decoration(
                            background = b1.parseThemeBackgroundColor()
                        )
                    )
                } else {
                    emptyList()
                }
            ),
            BlockView.Text.Numbered(
                indent = 1,
                isFocused = false,
                id = b2.id,
                marks = emptyList(),
                background = b2.parseThemeBackgroundColor(),
                color = b2.content<Block.Content.Text>().parseThemeTextColor(),
                text = b2.content<Block.Content.Text>().text,
                number = 2,
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(
                            background = a2.parseThemeBackgroundColor()
                        ),
                        BlockView.Decoration(
                            background = b2.parseThemeBackgroundColor()
                        )
                    )
                } else {
                    emptyList()
                }
            ),
            BlockView.Text.Numbered(
                indent = 1,
                isFocused = false,
                id = b3.id,
                marks = emptyList(),
                background = b3.parseThemeBackgroundColor(),
                color = b3.content<Block.Content.Text>().parseThemeTextColor(),
                text = b3.content<Block.Content.Text>().text,
                number = 3,
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(
                            background = a2.parseThemeBackgroundColor()
                        ),
                        BlockView.Decoration(
                            background = b3.parseThemeBackgroundColor()
                        )
                    )
                } else {
                    emptyList()
                }
            ),
            BlockView.Text.Numbered(
                indent = 0,
                isFocused = false,
                id = a3.id,
                marks = emptyList(),
                background = a3.parseThemeBackgroundColor(),
                color = a3.content<Block.Content.Text>().parseThemeTextColor(),
                text = a3.content<Block.Content.Text>().text,
                number = 3,
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(
                            background = a3.parseThemeBackgroundColor()
                        )
                    )
                } else {
                    emptyList()
                }
            ),
            BlockView.Text.Numbered(
                indent = 0,
                isFocused = false,
                id = a4.id,
                marks = emptyList(),
                background = a4.parseThemeBackgroundColor(),
                color = a4.content<Block.Content.Text>().parseThemeTextColor(),
                text = a4.content<Block.Content.Text>().text,
                number = 4,
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(
                            background = a4.parseThemeBackgroundColor()
                        )
                    )
                } else {
                    emptyList()
                }
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
                background = a1.parseThemeBackgroundColor(),
                color = a1.content<Block.Content.Text>().parseThemeTextColor(),
                text = a1.content<Block.Content.Text>().text,
                number = 1,
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(
                            background = a1.parseThemeBackgroundColor()
                        )
                    )
                } else {
                    emptyList()
                }
            ),
            BlockView.Text.Numbered(
                indent = 0,
                isFocused = false,
                id = a2.id,
                marks = emptyList(),
                background = a2.parseThemeBackgroundColor(),
                color = a2.content<Block.Content.Text>().parseThemeTextColor(),
                text = a2.content<Block.Content.Text>().text,
                number = 2,
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(
                            background = a2.parseThemeBackgroundColor()
                        )
                    )
                } else {
                    emptyList()
                }
            ),
            BlockView.Text.Numbered(
                indent = 1,
                isFocused = false,
                id = b1.id,
                marks = emptyList(),
                background = b1.parseThemeBackgroundColor(),
                color = b1.content<Block.Content.Text>().parseThemeTextColor(),
                text = b1.content<Block.Content.Text>().text,
                number = 1,
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(
                            background = a2.parseThemeBackgroundColor()
                        ),
                        BlockView.Decoration(
                            background = b1.parseThemeBackgroundColor()
                        )
                    )
                } else {
                    emptyList()
                }
            ),
            BlockView.Text.Numbered(
                indent = 0,
                isFocused = false,
                id = a3.id,
                marks = emptyList(),
                background = a3.parseThemeBackgroundColor(),
                color = a3.content<Block.Content.Text>().parseThemeTextColor(),
                text = a3.content<Block.Content.Text>().text,
                number = 3,
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(
                            background = a3.parseThemeBackgroundColor()
                        )
                    )
                } else {
                    emptyList()
                }
            ),
            BlockView.Text.Numbered(
                indent = 0,
                isFocused = false,
                id = a4.id,
                marks = emptyList(),
                background = a4.parseThemeBackgroundColor(),
                color = a4.content<Block.Content.Text>().parseThemeTextColor(),
                text = a4.content<Block.Content.Text>().text,
                number = 4,
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(
                            background = a4.parseThemeBackgroundColor()
                        )
                    )
                } else {
                    emptyList()
                }
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
                number = idx.inc(),
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(
                            background = block.parseThemeBackgroundColor()
                        )
                    )
                } else {
                    emptyList()
                }
            )
        }

        assertEquals(expected = expected, actual = result)
    }

    @Test
    fun `should render numbered blocks with correct number after divs containing also numbered blocks`() {

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

        repeat(25) { idx ->
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
            children = numbered.subList(0, 5).map { it.id },
            content = Block.Content.Layout(
                type = Block.Content.Layout.Type.DIV
            ),
            fields = Block.Fields.empty()
        )

        val div2 = Block(
            id = "div2Id",
            children = numbered.subList(5, 10).map { it.id },
            content = Block.Content.Layout(
                type = Block.Content.Layout.Type.DIV
            ),
            fields = Block.Fields.empty()
        )

        val div3 = Block(
            id = "div3Id",
            children = numbered.subList(10, 15).map { it.id },
            content = Block.Content.Layout(
                type = Block.Content.Layout.Type.DIV
            ),
            fields = Block.Fields.empty()
        )

        val div4 = Block(
            id = "div4Id",
            children = numbered.subList(15, 20).map { it.id },
            content = Block.Content.Layout(
                type = Block.Content.Layout.Type.DIV
            ),
            fields = Block.Fields.empty()
        )

        val page = Block(
            id = "root",
            children = listOf(header.id) + listOf(
                div1.id,
                div2.id,
                div3.id,
                div4.id
            ) + numbered.subList(20, 25).map { it.id },
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
                number = idx.inc(),
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(
                            background = block.parseThemeBackgroundColor()
                        )
                    )
                } else {
                    emptyList()
                }
            )
        }

        assertEquals(expected = expected, actual = result)
    }

    @Test
    fun `should render numbered blocks with correct number after divs containing also numbered blocks with inner lists`() {

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

        val div1num1 = Block(
            id = "div1num1",
            content = Block.Content.Text(
                text = "div1num1",
                marks = emptyList(),
                style = Block.Content.Text.Style.NUMBERED
            ),
            children = emptyList(),
            fields = Block.Fields.empty()
        )

        val div1num2 = Block(
            id = "div1num2",
            content = Block.Content.Text(
                text = "div1num2",
                marks = emptyList(),
                style = Block.Content.Text.Style.NUMBERED
            ),
            children = emptyList(),
            fields = Block.Fields.empty()
        )

        val div2num1 = Block(
            id = "div2num1",
            content = Block.Content.Text(
                text = "div2num1",
                marks = emptyList(),
                style = Block.Content.Text.Style.NUMBERED
            ),
            children = emptyList(),
            fields = Block.Fields.empty()
        )

        val div2num2num1 = Block(
            id = "div2num2num1",
            content = Block.Content.Text(
                text = "div2num2num1",
                marks = emptyList(),
                style = Block.Content.Text.Style.NUMBERED
            ),
            children = emptyList(),
            fields = Block.Fields.empty()
        )

        val div2num2num2 = Block(
            id = "div2num2num2",
            content = Block.Content.Text(
                text = "div2num2num2",
                marks = emptyList(),
                style = Block.Content.Text.Style.NUMBERED
            ),
            children = emptyList(),
            fields = Block.Fields.empty()
        )

        val div2num2 = Block(
            id = "div2num2",
            content = Block.Content.Text(
                text = "div2num2",
                marks = emptyList(),
                style = Block.Content.Text.Style.NUMBERED
            ),
            children = listOf(div2num2num1.id, div2num2num2.id),
            fields = Block.Fields.empty()
        )

        val fields = Block.Fields.empty()


        val div1 = Block(
            id = "div1Id",
            children = listOf(div1num1.id, div1num2.id),
            content = Block.Content.Layout(
                type = Block.Content.Layout.Type.DIV
            ),
            fields = Block.Fields.empty()
        )

        val div2 = Block(
            id = "div2Id",
            children = listOf(div2num1.id, div2num2.id),
            content = Block.Content.Layout(
                type = Block.Content.Layout.Type.DIV
            ),
            fields = Block.Fields.empty()
        )

        val afterDiv2Num1 = Block(
            id = "afterDiv2Num1",
            content = Block.Content.Text(
                text = "afterDiv2Num1",
                marks = emptyList(),
                style = Block.Content.Text.Style.NUMBERED
            ),
            children = emptyList(),
            fields = Block.Fields.empty()
        )

        val afterDiv2Num2 = Block(
            id = "afterDiv2Num2",
            content = Block.Content.Text(
                text = "afterDiv2Num2",
                marks = emptyList(),
                style = Block.Content.Text.Style.NUMBERED
            ),
            children = emptyList(),
            fields = Block.Fields.empty()
        )

        val page = Block(
            id = "root",
            children = listOf(header.id) + listOf(
                div1.id,
                div2.id
            ) + listOf(afterDiv2Num1.id, afterDiv2Num2.id),
            fields = fields,
            content = Block.Content.Smart()
        )

        val details = mapOf(page.id to fields)

        val blocks = listOf(page, header, title) + listOf(
            div1,
            div2
        ) + listOf(
            div1num1,
            div1num2,
            div2num1,
            div2num2,
            div2num2num1,
            div2num2num2,
            afterDiv2Num1,
            afterDiv2Num2
        )

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
        ) + listOf(
            BlockView.Text.Numbered(
                id = div1num1.id,
                text = div1num1.content<TXT>().text,
                number = 1,
                indent = 0,
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(
                            background = div1num1.parseThemeBackgroundColor()
                        )
                    )
                } else {
                    emptyList()
                }
            ),
            BlockView.Text.Numbered(
                id = div1num2.id,
                text = div1num2.content<TXT>().text,
                number = 2,
                indent = 0,
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(
                            background = div1num2.parseThemeBackgroundColor()
                        )
                    )
                } else {
                    emptyList()
                }
            ),
            BlockView.Text.Numbered(
                id = div2num1.id,
                text = div2num1.content<TXT>().text,
                number = 3,
                indent = 0,
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(
                            background = div2num1.parseThemeBackgroundColor()
                        )
                    )
                } else {
                    emptyList()
                }
            ),
            BlockView.Text.Numbered(
                id = div2num2.id,
                text = div2num2.content<TXT>().text,
                number = 4,
                indent = 0,
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(
                            background = div2num2.parseThemeBackgroundColor()
                        )
                    )
                } else {
                    emptyList()
                }
            ),
            BlockView.Text.Numbered(
                id = div2num2num1.id,
                text = div2num2num1.content<TXT>().text,
                number = 1,
                indent = 1,
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(
                            background = div2num2.parseThemeBackgroundColor()
                        ),
                        BlockView.Decoration(
                            background = div2num2num1.parseThemeBackgroundColor()
                        )
                    )
                } else {
                    emptyList()
                }
            ),
            BlockView.Text.Numbered(
                id = div2num2num2.id,
                text = div2num2num2.content<TXT>().text,
                number = 2,
                indent = 1,
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(
                            background = div2num2.parseThemeBackgroundColor()
                        ),
                        BlockView.Decoration(
                            background = div2num2num2.parseThemeBackgroundColor()
                        )
                    )
                } else {
                    emptyList()
                }
            ),
            BlockView.Text.Numbered(
                id = afterDiv2Num1.id,
                text = afterDiv2Num1.content<TXT>().text,
                number = 5,
                indent = 0,
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(
                            background = afterDiv2Num1.parseThemeBackgroundColor()
                        )
                    )
                } else {
                    emptyList()
                }
            ),
            BlockView.Text.Numbered(
                id = afterDiv2Num2.id,
                text = afterDiv2Num2.content<TXT>().text,
                number = 6,
                indent = 0,
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(
                            background = afterDiv2Num2.parseThemeBackgroundColor()
                        )
                    )
                } else {
                    emptyList()
                }
            )
        )

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
                background = a1.parseThemeBackgroundColor(),
                color = a1.content<Block.Content.Text>().parseThemeTextColor(),
                text = a1.content<Block.Content.Text>().text,
                number = 1,
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(
                            background = a1.parseThemeBackgroundColor()
                        )
                    )
                } else {
                    emptyList()
                }
            ),
            BlockView.Text.Numbered(
                indent = 0,
                isFocused = false,
                id = a2.id,
                marks = emptyList(),
                background = a2.parseThemeBackgroundColor(),
                color = a2.content<Block.Content.Text>().parseThemeTextColor(),
                text = a2.content<Block.Content.Text>().text,
                number = 2,
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(
                            background = a2.parseThemeBackgroundColor()
                        )
                    )
                } else {
                    emptyList()
                }
            ),
            BlockView.Text.Numbered(
                indent = 1,
                isFocused = false,
                id = b1.id,
                marks = emptyList(),
                background = b1.parseThemeBackgroundColor(),
                color = b1.content<Block.Content.Text>().parseThemeTextColor(),
                text = b1.content<Block.Content.Text>().text,
                number = 1,
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(
                            background = a2.parseThemeBackgroundColor()
                        ),
                        BlockView.Decoration(
                            background = b1.parseThemeBackgroundColor()
                        )
                    )
                } else {
                    emptyList()
                }
            ),
            BlockView.Text.Paragraph(
                indent = 2,
                isFocused = false,
                id = c1.id,
                marks = emptyList(),
                background = c1.parseThemeBackgroundColor(),
                color = c1.content<Block.Content.Text>().parseThemeTextColor(),
                text = c1.content<Block.Content.Text>().text,
                alignment = Alignment.CENTER,
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(
                            background = a2.parseThemeBackgroundColor()
                        ),
                        BlockView.Decoration(
                            background = b1.parseThemeBackgroundColor()
                        ),
                        BlockView.Decoration(
                            background = c1.parseThemeBackgroundColor()
                        )
                    )
                } else {
                    emptyList()
                }
            ),
            BlockView.Text.Numbered(
                indent = 2,
                isFocused = false,
                id = c2.id,
                marks = emptyList(),
                background = c2.parseThemeBackgroundColor(),
                color = c2.content<Block.Content.Text>().parseThemeTextColor(),
                text = c2.content<Block.Content.Text>().text,
                number = 1,
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(
                            background = a2.parseThemeBackgroundColor()
                        ),
                        BlockView.Decoration(
                            background = b1.parseThemeBackgroundColor()
                        ),
                        BlockView.Decoration(
                            background = c2.parseThemeBackgroundColor()
                        )
                    )
                } else {
                    emptyList()
                }
            ),
            BlockView.Text.Numbered(
                indent = 2,
                isFocused = false,
                id = c3.id,
                marks = emptyList(),
                background = c3.parseThemeBackgroundColor(),
                color = c3.content<Block.Content.Text>().parseThemeTextColor(),
                text = c3.content<Block.Content.Text>().text,
                number = 2,
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(
                            background = a2.parseThemeBackgroundColor()
                        ),
                        BlockView.Decoration(
                            background = b1.parseThemeBackgroundColor()
                        ),
                        BlockView.Decoration(
                            background = c3.parseThemeBackgroundColor()
                        )
                    )
                } else {
                    emptyList()
                }
            ),
            BlockView.Text.Numbered(
                indent = 2,
                isFocused = false,
                id = c4.id,
                marks = emptyList(),
                background = c4.parseThemeBackgroundColor(),
                color = c4.content<Block.Content.Text>().parseThemeTextColor(),
                text = c4.content<Block.Content.Text>().text,
                number = 3,
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(
                            background = a2.parseThemeBackgroundColor()
                        ),
                        BlockView.Decoration(
                            background = b1.parseThemeBackgroundColor()
                        ),
                        BlockView.Decoration(
                            background = c4.parseThemeBackgroundColor()
                        )
                    )
                } else {
                    emptyList()
                }
            ),
            BlockView.Text.Numbered(
                indent = 0,
                isFocused = false,
                id = a3.id,
                marks = emptyList(),
                background = a3.parseThemeBackgroundColor(),
                color = a3.content<Block.Content.Text>().parseThemeTextColor(),
                text = a3.content<Block.Content.Text>().text,
                number = 3,
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(
                            background = a3.parseThemeBackgroundColor()
                        )
                    )
                } else {
                    emptyList()
                }
            ),
            BlockView.Text.Numbered(
                indent = 0,
                isFocused = false,
                id = a4.id,
                marks = emptyList(),
                background = a4.parseThemeBackgroundColor(),
                color = a4.content<Block.Content.Text>().parseThemeTextColor(),
                text = a4.content<Block.Content.Text>().text,
                number = 4,
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(
                            background = a4.parseThemeBackgroundColor()
                        )
                    )
                } else {
                    emptyList()
                }
            ),
        )

        assertEquals(expected = expected, actual = result)
    }

    //endregion

    // Quote nesting

    /**
     * Q with background
     * ...P with background
     */
    @Test
    fun `should return blocks with expected decoration - when a quote contains a paragraph`() {
        val child = Block(
            id = "a1-id",
            children = listOf(),
            content = Block.Content.Text(
                text = "A1",
                style = Block.Content.Text.Style.P,
                marks = emptyList()
            ),
            fields = Block.Fields.empty(),
            backgroundColor = ThemeColor.ORANGE.code
        )

        val quote = Block(
            id = "c1-id",
            children = listOf(child.id),
            content = Block.Content.Text(
                text = "C1",
                style = Block.Content.Text.Style.QUOTE,
                marks = emptyList()
            ),
            fields = Block.Fields.empty(),
            backgroundColor = ThemeColor.BLUE.code
        )

        val page = Block(
            id = "objectId",
            children = listOf(quote.id),
            fields = Block.Fields.empty(),
            content = Block.Content.Smart()
        )

        val details = mapOf(page.id to Block.Fields.empty())

        val blocks = listOf(page, quote, child)

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
            BlockView.Text.Highlight(
                indent = 0,
                isFocused = false,
                id = quote.id,
                marks = emptyList(),
                background = quote.parseThemeBackgroundColor(),
                color = quote.content<Block.Content.Text>().parseThemeTextColor(),
                text = quote.content<Block.Content.Text>().text,
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(
                            background = quote.parseThemeBackgroundColor()
                        )
                    )
                } else {
                    emptyList()
                }
            ),
            BlockView.Text.Paragraph(
                indent = 1,
                isFocused = false,
                id = child.id,
                marks = emptyList(),
                background = child.parseThemeBackgroundColor(),
                color = child.content<Block.Content.Text>().parseThemeTextColor(),
                text = child.content<Block.Content.Text>().text,
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(
                            background = quote.parseThemeBackgroundColor(),
                            style = BlockView.Decoration.Style.Highlight.End
                        ),
                        BlockView.Decoration(
                            background = child.parseThemeBackgroundColor()
                        )
                    )
                } else {
                    emptyList()
                }
            ),
        )

        assertEquals(expected = expected, actual = result)
    }

    /**
     * Q
     * ...Q
     */
    @Test
    fun `should return blocks with expected decoration - when a quote contains a quote`() {
        val quote2 = Block(
            id = "quote-1-id",
            children = listOf(),
            content = Block.Content.Text(
                text = "C1",
                style = Block.Content.Text.Style.QUOTE,
                marks = emptyList()
            ),
            fields = Block.Fields.empty(),
            backgroundColor = ThemeColor.BLUE.code
        )

        val quote1 = Block(
            id = "quote-2-id",
            children = listOf(quote2.id),
            content = Block.Content.Text(
                text = "C1",
                style = Block.Content.Text.Style.QUOTE,
                marks = emptyList()
            ),
            fields = Block.Fields.empty(),
            backgroundColor = ThemeColor.BLUE.code
        )

        val page = Block(
            id = "objectId",
            children = listOf(quote1.id),
            fields = Block.Fields.empty(),
            content = Block.Content.Smart()
        )

        val details = mapOf(page.id to Block.Fields.empty())

        val blocks = listOf(page, quote1, quote2)

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
            BlockView.Text.Highlight(
                indent = 0,
                isFocused = false,
                id = quote1.id,
                marks = emptyList(),
                background = quote1.parseThemeBackgroundColor(),
                color = quote1.content<Block.Content.Text>().parseThemeTextColor(),
                text = quote1.content<Block.Content.Text>().text,
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(
                            background = quote1.parseThemeBackgroundColor()
                        )
                    )
                } else {
                    emptyList()
                }
            ),
            BlockView.Text.Highlight(
                indent = 1,
                isFocused = false,
                id = quote2.id,
                marks = emptyList(),
                background = quote2.parseThemeBackgroundColor(),
                color = quote2.content<Block.Content.Text>().parseThemeTextColor(),
                text = quote2.content<Block.Content.Text>().text,
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(
                            background = quote1.parseThemeBackgroundColor(),
                            style = BlockView.Decoration.Style.Highlight.End
                        ),
                        BlockView.Decoration(
                            background = quote2.parseThemeBackgroundColor()
                        )
                    )
                } else {
                    emptyList()
                }
            )
        )

        assertEquals(expected = expected, actual = result)
    }

    /**
     * Q
     * ...Q
     * ......P
     */
    @Test
    fun `should return blocks with expected decoration - when a quote contains a quote, which contains a paragraph`() {
        val paragraph = Block(
            id = "paragraph-id",
            children = listOf(),
            content = Block.Content.Text(
                text = "A1",
                style = Block.Content.Text.Style.P,
                marks = emptyList()
            ),
            fields = Block.Fields.empty(),
            backgroundColor = ThemeColor.ORANGE.code
        )

        val quote2 = Block(
            id = "quote-1-id",
            children = listOf(paragraph.id),
            content = Block.Content.Text(
                text = "C1",
                style = Block.Content.Text.Style.QUOTE,
                marks = emptyList()
            ),
            fields = Block.Fields.empty(),
            backgroundColor = ThemeColor.BLUE.code
        )

        val quote1 = Block(
            id = "quote-2-id",
            children = listOf(quote2.id),
            content = Block.Content.Text(
                text = "C1",
                style = Block.Content.Text.Style.QUOTE,
                marks = emptyList()
            ),
            fields = Block.Fields.empty(),
            backgroundColor = ThemeColor.BLUE.code
        )

        val page = Block(
            id = "objectId",
            children = listOf(quote1.id),
            fields = Block.Fields.empty(),
            content = Block.Content.Smart()
        )

        val details = mapOf(page.id to Block.Fields.empty())

        val blocks = listOf(page, quote1, quote2, paragraph)

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
            BlockView.Text.Highlight(
                indent = 0,
                isFocused = false,
                id = quote1.id,
                marks = emptyList(),
                background = quote1.parseThemeBackgroundColor(),
                color = quote1.content<Block.Content.Text>().parseThemeTextColor(),
                text = quote1.content<Block.Content.Text>().text,
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(
                            background = quote1.parseThemeBackgroundColor()
                        )
                    )
                } else {
                    emptyList()
                }
            ),
            BlockView.Text.Highlight(
                indent = 1,
                isFocused = false,
                id = quote2.id,
                marks = emptyList(),
                background = quote2.parseThemeBackgroundColor(),
                color = quote2.content<Block.Content.Text>().parseThemeTextColor(),
                text = quote2.content<Block.Content.Text>().text,
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(
                            background = quote1.parseThemeBackgroundColor(),
                            style = BlockView.Decoration.Style.Highlight.Middle
                        ),
                        BlockView.Decoration(
                            background = quote2.parseThemeBackgroundColor()
                        )
                    )
                } else {
                    emptyList()
                }
            ),
            BlockView.Text.Paragraph(
                indent = 2,
                isFocused = false,
                id = paragraph.id,
                marks = emptyList(),
                background = paragraph.parseThemeBackgroundColor(),
                color = paragraph.content<Block.Content.Text>().parseThemeTextColor(),
                text = paragraph.content<Block.Content.Text>().text,
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(
                            background = quote1.parseThemeBackgroundColor(),
                            style = BlockView.Decoration.Style.Highlight.End
                        ),
                        BlockView.Decoration(
                            background = quote2.parseThemeBackgroundColor(),
                            style = BlockView.Decoration.Style.Highlight.End
                        ),
                        BlockView.Decoration(
                            background = paragraph.parseThemeBackgroundColor()
                        )
                    )
                } else {
                    emptyList()
                }
            ),
        )

        assertEquals(expected = expected, actual = result)
    }

    /**
     * Q
     * ...Q
     * ......P
     * ......P
     */
    @Test
    fun `should return blocks with expected decoration - when a quote contains a quote, which contains two paragraphs`() {
        val paragraph1 = Block(
            id = "paragraph-1-id",
            children = listOf(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                style = Block.Content.Text.Style.P,
                marks = emptyList()
            ),
            fields = Block.Fields.empty(),
            backgroundColor = ThemeColor.YELLOW.code
        )

        val paragraph2 = Block(
            id = "paragraph-2-id",
            children = listOf(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                style = Block.Content.Text.Style.P,
                marks = emptyList()
            ),
            fields = Block.Fields.empty(),
            backgroundColor = ThemeColor.ORANGE.code
        )

        val quote2 = Block(
            id = "quote-1-id",
            children = listOf(paragraph1.id, paragraph2.id),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                style = Block.Content.Text.Style.QUOTE,
                marks = emptyList()
            ),
            fields = Block.Fields.empty(),
            backgroundColor = ThemeColor.BLUE.code
        )

        val quote1 = Block(
            id = "quote-2-id",
            children = listOf(quote2.id),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                style = Block.Content.Text.Style.QUOTE,
                marks = emptyList()
            ),
            fields = Block.Fields.empty(),
            backgroundColor = ThemeColor.BLUE.code
        )

        val page = Block(
            id = "objectId",
            children = listOf(quote1.id),
            fields = Block.Fields.empty(),
            content = Block.Content.Smart()
        )

        val details = mapOf(page.id to Block.Fields.empty())

        val blocks = listOf(page, quote1, quote2, paragraph1, paragraph2)

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
            BlockView.Text.Highlight(
                indent = 0,
                isFocused = false,
                id = quote1.id,
                marks = emptyList(),
                background = quote1.parseThemeBackgroundColor(),
                color = quote1.content<Block.Content.Text>().parseThemeTextColor(),
                text = quote1.content<Block.Content.Text>().text,
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(
                            background = quote1.parseThemeBackgroundColor()
                        )
                    )
                } else {
                    emptyList()
                }
            ),
            BlockView.Text.Highlight(
                indent = 1,
                isFocused = false,
                id = quote2.id,
                marks = emptyList(),
                background = quote2.parseThemeBackgroundColor(),
                color = quote2.content<Block.Content.Text>().parseThemeTextColor(),
                text = quote2.content<Block.Content.Text>().text,
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(
                            background = quote1.parseThemeBackgroundColor(),
                            style = BlockView.Decoration.Style.Highlight.Middle
                        ),
                        BlockView.Decoration(
                            background = quote2.parseThemeBackgroundColor()
                        )
                    )
                } else {
                    emptyList()
                }
            ),
            BlockView.Text.Paragraph(
                indent = 2,
                isFocused = false,
                id = paragraph1.id,
                marks = emptyList(),
                background = paragraph1.parseThemeBackgroundColor(),
                color = paragraph1.content<Block.Content.Text>().parseThemeTextColor(),
                text = paragraph1.content<Block.Content.Text>().text,
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(
                            background = quote1.parseThemeBackgroundColor(),
                            style = BlockView.Decoration.Style.Highlight.Middle
                        ),
                        BlockView.Decoration(
                            background = quote2.parseThemeBackgroundColor(),
                            style = BlockView.Decoration.Style.Highlight.Middle
                        ),
                        BlockView.Decoration(
                            background = paragraph1.parseThemeBackgroundColor()
                        )
                    )
                } else {
                    emptyList()
                }
            ),
            BlockView.Text.Paragraph(
                indent = 2,
                isFocused = false,
                id = paragraph2.id,
                marks = emptyList(),
                background = paragraph2.parseThemeBackgroundColor(),
                color = paragraph2.content<Block.Content.Text>().parseThemeTextColor(),
                text = paragraph2.content<Block.Content.Text>().text,
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(
                            background = quote1.parseThemeBackgroundColor(),
                            style = BlockView.Decoration.Style.Highlight.End
                        ),
                        BlockView.Decoration(
                            background = quote2.parseThemeBackgroundColor(),
                            style = BlockView.Decoration.Style.Highlight.End
                        ),
                        BlockView.Decoration(
                            background = paragraph2.parseThemeBackgroundColor()
                        )
                    )
                } else {
                    emptyList()
                }
            )
        )

        assertEquals(expected = expected, actual = result)
    }

    /**
     * P
     * ...Q
     * ......P
     * ......P
     */
    @Test
    fun `should return blocks with expected decoration - when a paragraph contains a quote, which contains two paragraphs`() {
        val paragraph1 = Block(
            id = "paragraph-1-id",
            children = listOf(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                style = Block.Content.Text.Style.P,
                marks = emptyList()
            ),
            fields = Block.Fields.empty(),
            backgroundColor = ThemeColor.YELLOW.code
        )

        val paragraph2 = Block(
            id = "paragraph-2-id",
            children = listOf(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                style = Block.Content.Text.Style.P,
                marks = emptyList()
            ),
            fields = Block.Fields.empty(),
            backgroundColor = ThemeColor.ORANGE.code
        )

        val quote = Block(
            id = "quote-1-id",
            children = listOf(paragraph1.id, paragraph2.id),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                style = Block.Content.Text.Style.QUOTE,
                marks = emptyList()
            ),
            fields = Block.Fields.empty(),
            backgroundColor = ThemeColor.BLUE.code
        )

        val paragraph = Block(
            id = "quote-2-id",
            children = listOf(quote.id),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                style = Block.Content.Text.Style.P,
                marks = emptyList()
            ),
            fields = Block.Fields.empty(),
            backgroundColor = ThemeColor.BLUE.code
        )

        val page = Block(
            id = "objectId",
            children = listOf(paragraph.id),
            fields = Block.Fields.empty(),
            content = Block.Content.Smart()
        )

        val details = mapOf(page.id to Block.Fields.empty())

        val blocks = listOf(page, paragraph, quote, paragraph1, paragraph2)

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
            BlockView.Text.Paragraph(
                indent = 0,
                isFocused = false,
                id = paragraph.id,
                marks = emptyList(),
                background = paragraph.parseThemeBackgroundColor(),
                color = paragraph.content<Block.Content.Text>().parseThemeTextColor(),
                text = paragraph.content<Block.Content.Text>().text,
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(
                            background = paragraph.parseThemeBackgroundColor()
                        )
                    )
                } else {
                    emptyList()
                }
            ),
            BlockView.Text.Highlight(
                indent = 1,
                isFocused = false,
                id = quote.id,
                marks = emptyList(),
                background = quote.parseThemeBackgroundColor(),
                color = quote.content<Block.Content.Text>().parseThemeTextColor(),
                text = quote.content<Block.Content.Text>().text,
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(
                            background = paragraph.parseThemeBackgroundColor()
                        ),
                        BlockView.Decoration(
                            background = quote.parseThemeBackgroundColor()
                        )
                    )
                } else {
                    emptyList()
                }
            ),
            BlockView.Text.Paragraph(
                indent = 2,
                isFocused = false,
                id = paragraph1.id,
                marks = emptyList(),
                background = paragraph1.parseThemeBackgroundColor(),
                color = paragraph1.content<Block.Content.Text>().parseThemeTextColor(),
                text = paragraph1.content<Block.Content.Text>().text,
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(
                            background = paragraph.parseThemeBackgroundColor(),
                        ),
                        BlockView.Decoration(
                            background = quote.parseThemeBackgroundColor(),
                            style = BlockView.Decoration.Style.Highlight.Middle
                        ),
                        BlockView.Decoration(
                            background = paragraph1.parseThemeBackgroundColor()
                        )
                    )
                } else {
                    emptyList()
                }
            ),
            BlockView.Text.Paragraph(
                indent = 2,
                isFocused = false,
                id = paragraph2.id,
                marks = emptyList(),
                background = paragraph2.parseThemeBackgroundColor(),
                color = paragraph2.content<Block.Content.Text>().parseThemeTextColor(),
                text = paragraph2.content<Block.Content.Text>().text,
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(
                            background = paragraph.parseThemeBackgroundColor()
                        ),
                        BlockView.Decoration(
                            background = quote.parseThemeBackgroundColor(),
                            style = BlockView.Decoration.Style.Highlight.End
                        ),
                        BlockView.Decoration(
                            background = paragraph2.parseThemeBackgroundColor()
                        )
                    )
                } else {
                    emptyList()
                }
            )
        )

        assertEquals(expected = expected, actual = result)
    }

    /**
     * P with background
     * ...Bookmark with background
     */
    @Test
    fun `should return blocks with expected decoration - when a paragraph with background contains a bookmark with background`() {

        val bookmarkObjectId = MockDataFactory.randomUuid()
        val bookmarkDescription = MockDataFactory.randomString()
        val bookmarkTitle = MockDataFactory.randomString()
        val bookmarkUrl = MockDataFactory.randomString()

        val bookmark = StubBookmark(
            backgroundColor = ThemeColor.ORANGE.code,
            state = Block.Content.Bookmark.State.DONE,
            targetObjectId = bookmarkObjectId
        )

        val paragraph = StubParagraph(
            children = listOf(bookmark.id),
            backgroundColor = ThemeColor.YELLOW.code
        )

        val page = StubSmartBlock(children = listOf(paragraph.id))

        val blocks = listOf(page, paragraph, bookmark)

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
                details = Block.Details(
                    mapOf(
                        bookmarkObjectId to Block.Fields(
                            mapOf(
                                Relations.NAME to bookmarkTitle,
                                Relations.DESCRIPTION to bookmarkDescription,
                                Relations.SOURCE to bookmarkUrl
                            )
                        )
                    )
                )
            )
        }

        val expected = listOf(
            BlockView.Text.Paragraph(
                indent = 0,
                id = paragraph.id,
                marks = emptyList(),
                background = paragraph.parseThemeBackgroundColor(),
                color = paragraph.content<Block.Content.Text>().parseThemeTextColor(),
                text = paragraph.content<Block.Content.Text>().text,
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(background = ThemeColor.YELLOW)
                    )
                } else {
                    emptyList()
                }
            ),
            BlockView.Media.Bookmark(
                id = bookmark.id,
                indent = 1,
                title = bookmarkTitle,
                url = bookmarkUrl,
                description = bookmarkDescription,
                isPreviousBlockMedia = false,
                background = bookmark.parseThemeBackgroundColor(),
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(background = ThemeColor.YELLOW),
                        BlockView.Decoration(
                            style = BlockView.Decoration.Style.Card,
                            background = ThemeColor.ORANGE
                        )
                    )
                } else {
                    emptyList()
                }
            )
        )

        assertEquals(expected = expected, actual = result)
    }

    /**
     * P without background
     * ...Bookmark with background
     */
    @Test
    fun `should return blocks with expected decoration - when a paragraph without background contains a bookmark with background`() {

        val bookmark = StubBookmark(
            backgroundColor = ThemeColor.ORANGE.code
        )

        val paragraph = StubParagraph(
            children = listOf(bookmark.id)
        )

        val page = StubSmartBlock(children = listOf(paragraph.id))

        val blocks = listOf(page, paragraph, bookmark)

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
                details = Block.Details()
            )
        }

        val expected = listOf(
            BlockView.Text.Paragraph(
                indent = 0,
                id = paragraph.id,
                marks = emptyList(),
                background = paragraph.parseThemeBackgroundColor(),
                color = paragraph.content<Block.Content.Text>().parseThemeTextColor(),
                text = paragraph.content<Block.Content.Text>().text,
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(background = ThemeColor.DEFAULT)
                    )
                } else {
                    emptyList()
                }
            ),
            BlockView.MediaPlaceholder.Bookmark(
                id = bookmark.id,
                indent = 1,
                isPreviousBlockMedia = false,
                background = bookmark.parseThemeBackgroundColor(),
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(background = ThemeColor.DEFAULT),
                        BlockView.Decoration(
                            style = BlockView.Decoration.Style.Card,
                            background = ThemeColor.ORANGE
                        )
                    )
                } else {
                    emptyList()
                }
            )
        )

        assertEquals(expected = expected, actual = result)
    }

    /**
     * P without background
     * ...Bookmark without background
     */
    @Test
    fun `should return blocks with expected decoration - when a paragraph without background contains a bookmark without background`() {

        val bookmark = StubBookmark()

        val paragraph = StubParagraph(
            children = listOf(bookmark.id)
        )

        val page = StubSmartBlock(children = listOf(paragraph.id))

        val blocks = listOf(page, paragraph, bookmark)

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
                details = Block.Details()
            )
        }

        val expected = listOf(
            BlockView.Text.Paragraph(
                indent = 0,
                id = paragraph.id,
                marks = emptyList(),
                background = paragraph.parseThemeBackgroundColor(),
                color = paragraph.content<Block.Content.Text>().parseThemeTextColor(),
                text = paragraph.content<Block.Content.Text>().text,
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(background = ThemeColor.DEFAULT)
                    )
                } else {
                    emptyList()
                }
            ),
            BlockView.MediaPlaceholder.Bookmark(
                id = bookmark.id,
                indent = 1,
                isPreviousBlockMedia = false,
                background = bookmark.parseThemeBackgroundColor(),
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(background = ThemeColor.DEFAULT),
                        BlockView.Decoration(
                            style = BlockView.Decoration.Style.Card,
                            background = ThemeColor.DEFAULT
                        )
                    )
                } else {
                    emptyList()
                }
            )
        )

        assertEquals(expected = expected, actual = result)
    }

    //region Callout nesting

    /**
     * C with background. No children.
     */

    @Test
    fun `should return blocks with expected decoration - when a callout without children has background`() {

        val callout = StubCallout(backgroundColor = ThemeColor.BLUE.code)
        val page = StubSmartBlock(children = listOf(callout.id))
        val details = mapOf(page.id to Block.Fields.empty())
        val blocks = listOf(page, callout)

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
            BlockView.Text.Callout(
                indent = 0,
                isFocused = false,
                id = callout.id,
                marks = emptyList(),
                background = callout.parseThemeBackgroundColor(),
                text = callout.content<Block.Content.Text>().text,
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(
                            style = BlockView.Decoration.Style.Callout.Full,
                            background = callout.parseThemeBackgroundColor()
                        )
                    )
                } else {
                    emptyList()
                },
                icon = ObjectIcon.Basic.Emoji("💡")
            )
        )

        assertEquals(expected = expected, actual = result)
    }


    /**
     * C with background
     * ...P with background
     */
    @Test
    fun `should return blocks with expected decoration - when a callout contains a paragraph`() {

        val child = StubParagraph(backgroundColor = ThemeColor.ORANGE.code)

        val callout = StubCallout(
            children = listOf(child.id),
            backgroundColor = ThemeColor.BLUE.code
        )

        val page = StubSmartBlock(children = listOf(callout.id))

        val details = mapOf(page.id to Block.Fields.empty())

        val blocks = listOf(page, callout, child)

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
            BlockView.Text.Callout(
                indent = 0,
                isFocused = false,
                id = callout.id,
                marks = emptyList(),
                background = callout.parseThemeBackgroundColor(),
                text = callout.content<Block.Content.Text>().text,
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(
                            style = BlockView.Decoration.Style.Callout.Start,
                            background = callout.parseThemeBackgroundColor()
                        )
                    )
                } else {
                    emptyList()
                },
                icon = ObjectIcon.Basic.Emoji("💡")
            ),
            BlockView.Text.Paragraph(
                indent = 1,
                isFocused = false,
                id = child.id,
                marks = emptyList(),
                background = child.parseThemeBackgroundColor(),
                color = child.content<Block.Content.Text>().parseThemeTextColor(),
                text = child.content<Block.Content.Text>().text,
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(
                            background = callout.parseThemeBackgroundColor(),
                            style = BlockView.Decoration.Style.Callout.End
                        ),
                        BlockView.Decoration(
                            background = child.parseThemeBackgroundColor()
                        )
                    )
                } else {
                    emptyList()
                }
            )
        )

        assertEquals(expected = expected, actual = result)
    }

    /**
     * C with background
     * ...P with background
     * ...P with background
     */
    @Test
    fun `should return blocks with expected decoration - when a callout with background contains two paragraphs, each paragraph has its own background`() {

        val child1 = StubParagraph(backgroundColor = ThemeColor.ORANGE.code)
        val child2 = StubParagraph(backgroundColor = ThemeColor.ORANGE.code)

        val callout = StubCallout(
            children = listOf(child1.id, child2.id),
            backgroundColor = ThemeColor.BLUE.code
        )

        val page = StubSmartBlock(children = listOf(callout.id))

        val details = mapOf(page.id to Block.Fields.empty())

        val blocks = listOf(page, callout, child1, child2)

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
            BlockView.Text.Callout(
                indent = 0,
                isFocused = false,
                id = callout.id,
                marks = emptyList(),
                background = callout.parseThemeBackgroundColor(),
                text = callout.content<Block.Content.Text>().text,
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(
                            style = BlockView.Decoration.Style.Callout.Start,
                            background = callout.parseThemeBackgroundColor()
                        )
                    )
                } else {
                    emptyList()
                },
                icon = ObjectIcon.Basic.Emoji("💡")
            ),
            BlockView.Text.Paragraph(
                indent = 1,
                isFocused = false,
                id = child1.id,
                marks = emptyList(),
                background = child1.parseThemeBackgroundColor(),
                color = child1.content<Block.Content.Text>().parseThemeTextColor(),
                text = child1.content<Block.Content.Text>().text,
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(
                            background = callout.parseThemeBackgroundColor(),
                            style = BlockView.Decoration.Style.Callout.Middle
                        ),
                        BlockView.Decoration(
                            background = child1.parseThemeBackgroundColor()
                        )
                    )
                } else {
                    emptyList()
                }
            ),
            BlockView.Text.Paragraph(
                indent = 1,
                isFocused = false,
                id = child2.id,
                marks = emptyList(),
                background = child2.parseThemeBackgroundColor(),
                color = child2.content<Block.Content.Text>().parseThemeTextColor(),
                text = child2.content<Block.Content.Text>().text,
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(
                            background = callout.parseThemeBackgroundColor(),
                            style = BlockView.Decoration.Style.Callout.End
                        ),
                        BlockView.Decoration(
                            background = child2.parseThemeBackgroundColor()
                        )
                    )
                } else {
                    emptyList()
                }
            )
        )

        assertEquals(expected = expected, actual = result)
    }

    /**
     * C with background
     * ...P with background
     * ...P with background
     */
    @Test
    fun `should return blocks with expected decoration - when a callout with background contains one file in unploading state`() {

        val child1 = StubFile(
            backgroundColor = null,
            state = Block.Content.File.State.UPLOADING,
            type = Block.Content.File.Type.FILE
        )

        val callout = StubCallout(
            children = listOf(child1.id),
            backgroundColor = ThemeColor.BLUE.code
        )

        val page = StubSmartBlock(children = listOf(callout.id))

        val details = mapOf(page.id to Block.Fields.empty())

        val blocks = listOf(page, callout, child1)

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
            BlockView.Text.Callout(
                indent = 0,
                isFocused = false,
                id = callout.id,
                marks = emptyList(),
                background = callout.parseThemeBackgroundColor(),
                text = callout.content<Block.Content.Text>().text,
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(
                            style = BlockView.Decoration.Style.Callout.Start,
                            background = callout.parseThemeBackgroundColor()
                        )
                    )
                } else {
                    emptyList()
                },
                icon = ObjectIcon.Basic.Emoji("💡")
            ),
            BlockView.Upload.File(
                indent = 1,
                id = child1.id,
                background = child1.parseThemeBackgroundColor(),
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(
                            background = callout.parseThemeBackgroundColor(),
                            style = BlockView.Decoration.Style.Callout.End
                        ),
                        BlockView.Decoration(
                            style = BlockView.Decoration.Style.Card,
                            background = child1.parseThemeBackgroundColor()
                        )
                    )
                } else {
                    emptyList()
                }
            )
        )

        assertEquals(expected = expected, actual = result)
    }

    /**
     * C with background
     * ...P with background
     * ...P with background
     */
    @Test
    fun `should return blocks with expected decoration - when a callout with background contains one file in empty state`() {

        val child1 = StubFile(
            backgroundColor = null,
            state = Block.Content.File.State.EMPTY,
            type = Block.Content.File.Type.FILE
        )

        val callout = StubCallout(
            children = listOf(child1.id),
            backgroundColor = ThemeColor.BLUE.code
        )

        val page = StubSmartBlock(children = listOf(callout.id))

        val details = mapOf(page.id to Block.Fields.empty())

        val blocks = listOf(page, callout, child1)

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
            BlockView.Text.Callout(
                indent = 0,
                isFocused = false,
                id = callout.id,
                marks = emptyList(),
                background = callout.parseThemeBackgroundColor(),
                text = callout.content<Block.Content.Text>().text,
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(
                            style = BlockView.Decoration.Style.Callout.Start,
                            background = callout.parseThemeBackgroundColor()
                        )
                    )
                } else {
                    emptyList()
                },
                icon = ObjectIcon.Basic.Emoji("💡")
            ),
            BlockView.MediaPlaceholder.File(
                indent = 1,
                id = child1.id,
                background = child1.parseThemeBackgroundColor(),
                decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                    listOf(
                        BlockView.Decoration(
                            background = callout.parseThemeBackgroundColor(),
                            style = BlockView.Decoration.Style.Callout.End
                        ),
                        BlockView.Decoration(
                            style = BlockView.Decoration.Style.Card,
                            background = child1.parseThemeBackgroundColor()
                        )
                    )
                } else {
                    emptyList()
                },
                isPreviousBlockMedia = false
            )
        )

        assertEquals(expected = expected, actual = result)
    }

    //endregion
}