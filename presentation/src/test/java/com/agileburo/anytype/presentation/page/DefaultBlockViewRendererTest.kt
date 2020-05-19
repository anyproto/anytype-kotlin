package com.agileburo.anytype.presentation.page

import MockDataFactory
import com.agileburo.anytype.core_ui.features.page.BlockView
import com.agileburo.anytype.core_utils.tools.Counter
import com.agileburo.anytype.domain.block.model.Block
import com.agileburo.anytype.domain.common.Id
import com.agileburo.anytype.domain.config.Config
import com.agileburo.anytype.domain.emoji.Emojifier
import com.agileburo.anytype.domain.ext.asMap
import com.agileburo.anytype.domain.ext.content
import com.agileburo.anytype.domain.misc.UrlBuilder
import com.agileburo.anytype.presentation.page.render.BlockViewRenderer
import com.agileburo.anytype.presentation.page.render.DefaultBlockViewRenderer
import com.agileburo.anytype.presentation.page.toggle.ToggleStateHolder
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.stub
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import kotlin.test.assertEquals

class DefaultBlockViewRendererTest {

    class BlockViewRenderWrapper(
        private val blocks: Map<Id, List<Block>>,
        private val renderer: BlockViewRenderer
    ) : BlockViewRenderer by renderer {
        suspend fun render(
            root: Block,
            anchor: Id,
            focus: Id,
            indent: Int
        ): List<BlockView> = blocks.render(
            root = root,
            anchor = anchor,
            focus = focus,
            indent = indent
        )
    }

    @Mock
    lateinit var emojifier: Emojifier

    @Mock
    lateinit var toggleStateHolder: ToggleStateHolder

    private lateinit var renderer: DefaultBlockViewRenderer

    private val config = Config(
        home = MockDataFactory.randomUuid(),
        gateway = MockDataFactory.randomString(),
        profile = MockDataFactory.randomUuid()
    )

    private lateinit var wrapper: BlockViewRenderWrapper

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        renderer = DefaultBlockViewRenderer(
            urlBuilder = UrlBuilder(config),
            toggleStateHolder = toggleStateHolder,
            counter = Counter.Default()
        )
    }

    @Test
    fun `should return title, paragraph, toggle with its indented inner checkbox`() {

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
            children = listOf(paragraph.id, toggle.id),
            fields = Block.Fields.empty(),
            content = Block.Content.Page(
                style = Block.Content.Page.Style.SET
            )
        )

        val blocks = listOf(page, paragraph, toggle, checkbox)

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
                focus = paragraph.id,
                indent = 0
            )
        }

        val expected = listOf(
            BlockView.Title(
                id = page.id,
                focused = false,
                text = null,
                emoji = null
            ),
            BlockView.Paragraph(
                focused = true,
                id = paragraph.id,
                marks = emptyList(),
                backgroundColor = paragraph.content<Block.Content.Text>().backgroundColor,
                color = paragraph.content<Block.Content.Text>().color,
                text = paragraph.content<Block.Content.Text>().text,
                alignment = BlockView.Alignment.START
            ),
            BlockView.Toggle(
                isEmpty = false,
                focused = false,
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
            children = listOf(paragraph.id, toggle.id),
            fields = Block.Fields.empty(),
            content = Block.Content.Page(
                style = Block.Content.Page.Style.SET
            )
        )

        val blocks = listOf(page, paragraph, toggle, checkbox)

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
                focus = paragraph.id,
                indent = 0
            )
        }

        val expected = listOf(
            BlockView.Title(
                id = page.id,
                focused = false,
                text = null,
                emoji = null
            ),
            BlockView.Paragraph(
                focused = true,
                id = paragraph.id,
                marks = emptyList(),
                backgroundColor = paragraph.content<Block.Content.Text>().backgroundColor,
                color = paragraph.content<Block.Content.Text>().color,
                text = paragraph.content<Block.Content.Text>().text,
                alignment = BlockView.Alignment.END
            ),
            BlockView.Toggle(
                isEmpty = false,
                focused = false,
                toggled = true,
                id = toggle.id,
                marks = emptyList(),
                backgroundColor = toggle.content<Block.Content.Text>().backgroundColor,
                color = toggle.content<Block.Content.Text>().color,
                text = toggle.content<Block.Content.Text>().text,
                indent = 0
            ),
            BlockView.Checkbox(
                focused = false,
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
            children = listOf(paragraph.id),
            fields = Block.Fields.empty(),
            content = Block.Content.Page(
                style = Block.Content.Page.Style.SET
            )
        )

        val blocks = listOf(page, paragraph)

        val map = blocks.asMap()

        wrapper = BlockViewRenderWrapper(
            blocks = map,
            renderer = renderer
        )

        val result = runBlocking {
            wrapper.render(
                root = page,
                anchor = page.id,
                focus = paragraph.id,
                indent = 0
            )
        }

        val expected = listOf(
            BlockView.Title(
                id = page.id,
                focused = false,
                text = null,
                emoji = null
            ),
            BlockView.Paragraph(
                focused = true,
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
            children = listOf(paragraph.id),
            fields = Block.Fields.empty(),
            content = Block.Content.Page(
                style = Block.Content.Page.Style.SET
            )
        )

        val blocks = listOf(page, paragraph)

        val map = blocks.asMap()

        wrapper = BlockViewRenderWrapper(
            blocks = map,
            renderer = renderer
        )

        val result = runBlocking {
            wrapper.render(
                root = page,
                anchor = page.id,
                focus = paragraph.id,
                indent = 0
            )
        }

        val expected = listOf(
            BlockView.Title(
                id = page.id,
                focused = false,
                text = null,
                emoji = null
            ),
            BlockView.Paragraph(
                focused = true,
                id = paragraph.id,
                marks = emptyList(),
                backgroundColor = paragraph.content<Block.Content.Text>().backgroundColor,
                color = paragraph.content<Block.Content.Text>().color,
                text = paragraph.content<Block.Content.Text>().text,
                alignment = BlockView.Alignment.CENTER
            )
        )

        assertEquals(expected = expected, actual = result)
    }
}