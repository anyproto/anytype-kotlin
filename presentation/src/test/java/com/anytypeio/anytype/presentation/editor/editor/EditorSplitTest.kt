package com.anytypeio.anytype.presentation.editor.editor

import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Position
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.ext.content
import com.anytypeio.anytype.domain.block.interactor.CreateBlock
import com.anytypeio.anytype.domain.block.interactor.SplitBlock
import com.anytypeio.anytype.domain.block.interactor.UpdateText
import com.anytypeio.anytype.presentation.editor.EditorViewModel
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlin.test.assertEquals
import net.lachlanmckee.timberjunit.TimberTestRule
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.eq
import org.mockito.kotlin.times
import org.mockito.kotlin.verifyBlocking
import org.mockito.kotlin.verifyNoInteractions

class EditorSplitTest : EditorPresentationTestSetup() {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    @get:Rule
    val timberTestRule: TimberTestRule = TimberTestRule.builder()
        .minPriority(Log.DEBUG)
        .showThread(true)
        .showTimestamp(false)
        .onlyLogWhenTestFails(true)
        .build()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

    @After
    fun after() {
        coroutineTestRule.advanceTime(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)
    }

    /**
     * Testing split in Editor
     *
     * {TITLE, BACKSPACE} +
     * 1. Empty title -> cursor at start → Backspace → no effect
     *
     * {TITLE, NO DESCRIPTION WITH FEATURED} +
     * 1. Empty title → Enter → blockCreate command +
     * 2. Not empty title → cursor not at the end → Enter → split command +
     * 3. Not empty title → cursor at the end → Enter → blockCreate command +
     *
     * {TITLE, NO DESCRIPTION WITHOUT FEATURED} +
     * 1. Empty title → Enter → blockCreate command +
     * 2. Not empty title → cursor not at the end → Enter → split command +
     * 3. Not empty title → cursor at the end → Enter → blockCreate command +
     *
     * {TITLE, EMPTY_DESCRIPTION WITH FEATURED} +
     * 1. Empty title → Enter → set cursor to description +
     * 2. Not empty title → cursor not at the end → Enter → split command +
     * 3. Not empty title → cursor at the end → Enter → set cursor to description +
     *
     * {TITLE, EMPTY_DESCRIPTION WITHOUT FEATURED} +
     * 1. Empty title → Enter → set cursor to description +
     * 2. Not empty title → cursor not at the end → Enter → split command +
     * 3. Not empty title → cursor at the end → Enter → set cursor to description +
     *
     * {TITLE, NOT_EMPTY_DESCRIPTION WITH FEATURED} +
     * 1. Empty title → Enter → set cursor to end of description +
     * 2. Not empty title → cursor not at the end → Enter → split command +
     * 3. Not empty title → cursor at the end → Enter → set cursor to end of description +
     *
     * {TITLE, NOT_EMPTY_DESCRIPTION WITHOUT FEATURED} +
     * 1. Empty title → Enter → set cursor to end of description +
     * 2. Not empty title → cursor not at the end → Enter → split command +
     * 3. Not empty title → cursor at the end → Enter → set cursor to end of description +
     *
     * {EMPTY DESCRIPTION WITH FEATURED} +
     * 1. Empty description -> Enter -> split command +
     * 2. Not Empty description -> Enter in the middle -> split command +
     * 3. Not Empty description -> Enter at the end -> split command +
     *
     * {EMPTY DESCRIPTION WITHOUT FEATURED} +
     * 1. Empty description -> Enter -> split command +
     * 2. Not Empty description -> Enter in the middle -> split command +
     * 3. Not Empty description -> Enter at the end -> split command +
     *
     * {DESCRIPTION , BACKSPACE}
     * 1. Empty description -> Backspace -> no effect
     * 2. Not Empty description -> cursor at the start -> Backspace -> no effect
     *
     */

    //region SETUP

    private fun setupInteractions(
        doc: List<Block>,
        details: Block.Details = Block.Details(),
        relations: List<Relation> = listOf()
    ) {
        stubInterceptEvents()
        stubOpenDocument(
            document = doc,
            relations = relations,
            details = details
        )
        stubSplitBlock()
        stubMergeBlocks(root)
        stubUpdateText()
        stubCreateBlock(root)
    }

    companion object {

        fun createTitle(text: String) = Block(
            id = "title",
            content = Block.Content.Text(
                text = text,
                style = Block.Content.Text.Style.TITLE,
                marks = emptyList()
            ),
            children = emptyList(),
            fields = Block.Fields.empty()
        )

        fun createDescription(description: String) = Block(
            id = Relations.DESCRIPTION,
            content = Block.Content.Text(
                text = description,
                style = Block.Content.Text.Style.DESCRIPTION,
                marks = emptyList()
            ),
            children = emptyList(),
            fields = Block.Fields.empty()
        )

        fun createFeaturedBlock() = Block(
            id = Relations.FEATURED_RELATIONS,
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.FeaturedRelations
        )

        fun createHeader(
            title: String,
            description: String? = null,
            featured: String? = null
        ): Block {
            val children = mutableListOf<String>()
            children.add(title)
            if (description != null) children.add(description)
            if (featured != null) children.add(featured)
            return Block(
                id = "header",
                content = Block.Content.Layout(
                    type = Block.Content.Layout.Type.HEADER
                ),
                fields = Block.Fields.empty(),
                children = children
            )
        }

        fun createPage(root: Id, children: List<String>) = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart,
            children = children
        )

        fun createBlock() = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            ),
            children = emptyList()
        )

        fun createDetailsWithFeaturedRelations(root: Id, relation1: String, relation2: String) =
            Block.Details(
                mapOf(
                    root to Block.Fields(
                        mapOf(
                            "featuredRelations" to listOf(
                                relation1, relation2
                            )
                        )
                    )
                )
            )
    }

    //endregion

    //region TITLE BACKSPACE
    @Test
    fun `cursor at start then Backspace then no effect`() {
        val title = createTitle(text = "FooBar")
        val description = createDescription(description = "Some information")
        val header = createHeader(title = title.id, description = description.id)
        val page = createPage(root, listOf(header.id))
        val doc = listOf(page, header, title)
        setupInteractions(doc)
        val vm = buildViewModel()
        vm.onStart(root)

        val range = IntRange(start = 0, endInclusive = 0)

        vm.onSelectionChanged(
            id = title.id,
            selection = range
        )

        vm.onBlockFocusChanged(
            id = title.id,
            hasFocus = true
        )

        vm.onEmptyBlockBackspaceClicked(title.id)

        coroutineTestRule.advanceTime(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)

        verifyNoInteractions(updateText)
        verifyNoInteractions(splitBlock)
        verifyNoInteractions(mergeBlocks)
    }
    //endregion

    //region TITLE, NO DESCRIPTION WITH FEATURED
    @Test
    fun `Empty title with featured then Enter then blockCreate command`() {
        val title = createTitle(text = "")
        val featured = createFeaturedBlock()
        val header = createHeader(title = title.id, description = null, featured = featured.id)
        val block = createBlock()
        val page = createPage(root = root, children = listOf(header.id, block.id))
        val doc = listOf(page, header, title, block)
        setupInteractions(doc)
        val vm = buildViewModel()
        vm.onStart(root)

        val range = IntRange(start = 0, endInclusive = 0)

        vm.onSelectionChanged(
            id = title.id,
            selection = range
        )

        vm.onBlockFocusChanged(
            id = title.id,
            hasFocus = true
        )

        vm.onKeyPressedEvent(
            KeyPressedEvent.OnTitleBlockEnterKeyEvent(
                target = title.id,
                text = title.content<Block.Content.Text>().text,
                range = range
            )
        )

        coroutineTestRule.advanceTime(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)

        verifyBlocking(createBlock, times(1)) {
            execute(
                params = eq(
                    CreateBlock.Params(
                        context = root,
                        target = header.id,
                        position = Position.TOP,
                        prototype = Block.Prototype.Text(
                            style = Block.Content.Text.Style.P
                        )
                    )
                )
            )
        }
    }

    @Test
    fun `Not empty title with featured then cursor not at the end then Enter then split command`() {
        val title = createTitle(text = "FooBar")
        val featured = createFeaturedBlock()
        val header = createHeader(title = title.id, description = null, featured = featured.id)
        val block = createBlock()
        val page = createPage(root = root, children = listOf(header.id, block.id))
        val doc = listOf(page, header, title, block)
        setupInteractions(doc)
        val vm = buildViewModel()
        vm.onStart(root)

        val range = IntRange(start = 3, endInclusive = 3)

        vm.onSelectionChanged(
            id = title.id,
            selection = range
        )

        vm.onBlockFocusChanged(
            id = title.id,
            hasFocus = true
        )

        vm.onKeyPressedEvent(
            KeyPressedEvent.OnTitleBlockEnterKeyEvent(
                target = title.id,
                text = title.content<Block.Content.Text>().text,
                range = range
            )
        )

        coroutineTestRule.advanceTime(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)

        verifyBlocking(updateText, times(1)) {
            invoke(
                params = eq(
                    UpdateText.Params(
                        context = root,
                        text = title.content<Block.Content.Text>().text,
                        marks = emptyList(),
                        target = title.id
                    )
                )
            )
        }

        verifyBlocking(splitBlock, times(1)) {
            invoke(
                params = eq(
                    SplitBlock.Params(
                        context = root,
                        block = title,
                        range = range,
                        isToggled = null
                    )
                )
            )
        }
    }

    @Test
    fun `Not empty title with featured then cursor at the end then Enter then blockCreate command`() {
        val title = createTitle(text = "FooBar")
        val featured = createFeaturedBlock()
        val header = createHeader(title = title.id, description = null, featured = featured.id)
        val block = createBlock()
        val page = createPage(root = root, children = listOf(header.id, block.id))
        val doc = listOf(page, header, title, block)
        setupInteractions(doc)
        val vm = buildViewModel()
        vm.onStart(root)

        val range = IntRange(start = 7, endInclusive = 7)

        vm.onSelectionChanged(
            id = title.id,
            selection = range
        )

        vm.onBlockFocusChanged(
            id = title.id,
            hasFocus = true
        )

        vm.onKeyPressedEvent(
            KeyPressedEvent.OnTitleBlockEnterKeyEvent(
                target = title.id,
                text = title.content<Block.Content.Text>().text,
                range = range
            )
        )

        coroutineTestRule.advanceTime(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)

        verifyBlocking(updateText, times(1)) {
            invoke(
                params = eq(
                    UpdateText.Params(
                        context = root,
                        text = title.content<Block.Content.Text>().text,
                        marks = emptyList(),
                        target = title.id
                    )
                )
            )
        }

        verifyBlocking(splitBlock, times(1)) {
            invoke(
                params = eq(
                    SplitBlock.Params(
                        context = root,
                        block = title,
                        range = range,
                        isToggled = null
                    )
                )
            )
        }
    }
    //endregion

    //region TITLE, NO DESCRIPTION WITHOUT FEATURED
    @Test
    fun `Empty title without featured then Enter then blockCreate command`() {
        val title = createTitle(text = "")
        val header = createHeader(title = title.id, description = null, featured = null)
        val block = createBlock()
        val page = createPage(root = root, children = listOf(header.id, block.id))
        val doc = listOf(page, header, title, block)
        setupInteractions(doc)
        val vm = buildViewModel()
        vm.onStart(root)

        val range = IntRange(start = 0, endInclusive = 0)

        vm.onSelectionChanged(
            id = title.id,
            selection = range
        )

        vm.onBlockFocusChanged(
            id = title.id,
            hasFocus = true
        )

        vm.onKeyPressedEvent(
            KeyPressedEvent.OnTitleBlockEnterKeyEvent(
                target = title.id,
                text = title.content<Block.Content.Text>().text,
                range = range
            )
        )

        coroutineTestRule.advanceTime(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)

        verifyBlocking(createBlock, times(1)) {
            execute(
                params = eq(
                    CreateBlock.Params(
                        context = root,
                        target = header.id,
                        position = Position.TOP,
                        prototype = Block.Prototype.Text(
                            style = Block.Content.Text.Style.P
                        )
                    )
                )
            )
        }
    }

    @Test
    fun `Not empty title without featured then cursor not at the end then Enter then split command`() {
        val title = createTitle(text = "FooBar")
        val header = createHeader(title = title.id, description = null, featured = null)
        val block = createBlock()
        val page = createPage(root = root, children = listOf(header.id, block.id))
        val doc = listOf(page, header, title, block)
        setupInteractions(doc)
        val vm = buildViewModel()
        vm.onStart(root)

        val range = IntRange(start = 3, endInclusive = 3)

        vm.onSelectionChanged(
            id = title.id,
            selection = range
        )

        vm.onBlockFocusChanged(
            id = title.id,
            hasFocus = true
        )

        vm.onKeyPressedEvent(
            KeyPressedEvent.OnTitleBlockEnterKeyEvent(
                target = title.id,
                text = title.content<Block.Content.Text>().text,
                range = range
            )
        )

        coroutineTestRule.advanceTime(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)

        verifyBlocking(updateText, times(1)) {
            invoke(
                params = eq(
                    UpdateText.Params(
                        context = root,
                        text = title.content<Block.Content.Text>().text,
                        marks = emptyList(),
                        target = title.id
                    )
                )
            )
        }

        verifyBlocking(splitBlock, times(1)) {
            invoke(
                params = eq(
                    SplitBlock.Params(
                        context = root,
                        block = title,
                        range = range,
                        isToggled = null
                    )
                )
            )
        }
    }

    @Test
    fun `Not empty title without featured then cursor at the end then Enter then blockCreate command`() {
        val title = createTitle(text = "FooBar")
        val header = createHeader(title = title.id, description = null, featured = null)
        val block = createBlock()
        val page = createPage(root = root, children = listOf(header.id, block.id))
        val doc = listOf(page, header, title, block)
        setupInteractions(doc)
        val vm = buildViewModel()
        vm.onStart(root)

        val range = IntRange(start = 7, endInclusive = 7)

        vm.onSelectionChanged(
            id = title.id,
            selection = range
        )

        vm.onBlockFocusChanged(
            id = title.id,
            hasFocus = true
        )

        vm.onKeyPressedEvent(
            KeyPressedEvent.OnTitleBlockEnterKeyEvent(
                target = title.id,
                text = title.content<Block.Content.Text>().text,
                range = range
            )
        )

        coroutineTestRule.advanceTime(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)

        verifyBlocking(updateText, times(1)) {
            invoke(
                params = eq(
                    UpdateText.Params(
                        context = root,
                        text = title.content<Block.Content.Text>().text,
                        marks = emptyList(),
                        target = title.id
                    )
                )
            )
        }

        verifyBlocking(splitBlock, times(1)) {
            invoke(
                params = eq(
                    SplitBlock.Params(
                        context = root,
                        block = title,
                        range = range,
                        isToggled = null
                    )
                )
            )
        }
    }
    //endregion

    //region TITLE, EMPTY DESCRIPTION WITH FEATURED
    @Test
    fun `Empty title with empty desc and featured then Enter then set cursor to description`() {
        val title = createTitle(text = "")
        val featured = createFeaturedBlock()
        val description = createDescription(description = "")
        val header = createHeader(
            title = title.id,
            description = description.id,
            featured = featured.id
        )
        val block = createBlock()
        val page = createPage(root = root, children = listOf(header.id, block.id))
        val relation2 = Relation(
            key = MockDataFactory.randomString(),
            name = "Year",
            format = Relation.Format.NUMBER,
            source = Relation.Source.values().random()
        )
        val doc = listOf(page, header, title, description, featured, block)
        setupInteractions(
            doc = doc,
            details = createDetailsWithFeaturedRelations(root, description.id, relation2.key),
            relations = listOf()
        )
        val vm = buildViewModel()
        vm.onStart(root)

        val range = IntRange(start = 0, endInclusive = 0)

        vm.onSelectionChanged(
            id = title.id,
            selection = range
        )

        vm.onBlockFocusChanged(
            id = title.id,
            hasFocus = true
        )

        vm.onKeyPressedEvent(
            KeyPressedEvent.OnTitleBlockEnterKeyEvent(
                target = title.id,
                text = title.content<Block.Content.Text>().text,
                range = range
            )
        )

        coroutineTestRule.advanceTime(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)

        verifyNoInteractions(createBlock)
        verifyNoInteractions(splitBlock)
        verifyNoInteractions(mergeBlocks)
        verifyNoInteractions(updateText)

        val selection = orchestrator.stores.textSelection.current()
        val descLength = description.content.asText().text.length
        val expectedSelection = IntRange(descLength, descLength)
        assertEquals(expected = expectedSelection, actual = selection.selection)

        val focus = orchestrator.stores.focus.current()
        val expectedFocus = description.id
        assertEquals(expected = expectedFocus, actual = focus.requireTarget())
    }

    @Test
    fun `Not empty title with empty desc and featured then cursor not at the end then Enter then split command`() {
        val title = createTitle(text = "FooBar")
        val featured = createFeaturedBlock()
        val description = createDescription(description = "")
        val header =
            createHeader(title = title.id, description = description.id, featured = featured.id)
        val block = createBlock()
        val page = createPage(root = root, children = listOf(header.id, block.id))
        val doc = listOf(page, header, title, description, featured, block)
        setupInteractions(doc)
        val vm = buildViewModel()
        vm.onStart(root)

        val range = IntRange(start = 3, endInclusive = 3)

        vm.onSelectionChanged(
            id = title.id,
            selection = range
        )

        vm.onBlockFocusChanged(
            id = title.id,
            hasFocus = true
        )

        vm.onKeyPressedEvent(
            KeyPressedEvent.OnTitleBlockEnterKeyEvent(
                target = title.id,
                text = title.content<Block.Content.Text>().text,
                range = range
            )
        )

        coroutineTestRule.advanceTime(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)

        verifyBlocking(updateText, times(1)) {
            invoke(
                params = eq(
                    UpdateText.Params(
                        context = root,
                        text = title.content<Block.Content.Text>().text,
                        marks = emptyList(),
                        target = title.id
                    )
                )
            )
        }

        verifyBlocking(splitBlock, times(1)) {
            invoke(
                params = eq(
                    SplitBlock.Params(
                        context = root,
                        block = title,
                        range = range,
                        isToggled = null
                    )
                )
            )
        }
    }

    @Test
    fun `Not empty title with empty desc and featured then cursor at the end then Enter then set cursor to description`() {
        val title = createTitle(text = "FooBar")
        val featured = createFeaturedBlock()
        val description = createDescription(description = "")
        val header =
            createHeader(title = title.id, description = description.id, featured = featured.id)
        val block = createBlock()
        val page = createPage(root = root, children = listOf(header.id, block.id))
        val doc = listOf(page, header, title, description, featured, block)
        setupInteractions(doc)
        val vm = buildViewModel()
        vm.onStart(root)

        val range = IntRange(start = 6, endInclusive = 6)

        vm.onSelectionChanged(
            id = title.id,
            selection = range
        )

        vm.onBlockFocusChanged(
            id = title.id,
            hasFocus = true
        )

        vm.onKeyPressedEvent(
            KeyPressedEvent.OnTitleBlockEnterKeyEvent(
                target = title.id,
                text = title.content<Block.Content.Text>().text,
                range = range
            )
        )

        coroutineTestRule.advanceTime(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)

        verifyNoInteractions(createBlock)
        verifyNoInteractions(splitBlock)
        verifyNoInteractions(mergeBlocks)
        verifyNoInteractions(updateText)

        val selection = orchestrator.stores.textSelection.current()
        val descLength = description.content.asText().text.length
        val expectedSelection = IntRange(descLength, descLength)
        assertEquals(expected = expectedSelection, actual = selection.selection)

        val focus = orchestrator.stores.focus.current()
        val expectedFocus = description.id
        assertEquals(expected = expectedFocus, actual = focus.requireTarget())
    }
    //endregion

    //region TITLE, EMPTY DESCRIPTION WITHOUT FEATURED
    @Test
    fun `Empty title with empty desc and without featured then Enter then set cursor to description`() {
        val title = createTitle(text = "")
        val description = createDescription("")
        val header = createHeader(title = title.id, description = description.id, featured = null)
        val block = createBlock()
        val page = createPage(root = root, children = listOf(header.id, block.id))
        val doc = listOf(page, header, title, description, block)
        setupInteractions(doc)
        val vm = buildViewModel()
        vm.onStart(root)

        val range = IntRange(start = 0, endInclusive = 0)

        vm.onSelectionChanged(
            id = title.id,
            selection = range
        )

        vm.onBlockFocusChanged(
            id = title.id,
            hasFocus = true
        )

        vm.onKeyPressedEvent(
            KeyPressedEvent.OnTitleBlockEnterKeyEvent(
                target = title.id,
                text = title.content<Block.Content.Text>().text,
                range = range
            )
        )

        coroutineTestRule.advanceTime(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)

        verifyNoInteractions(createBlock)
        verifyNoInteractions(splitBlock)
        verifyNoInteractions(mergeBlocks)
        verifyNoInteractions(updateText)

        val selection = orchestrator.stores.textSelection.current()
        val descLength = description.content.asText().text.length
        val expectedSelection = IntRange(descLength, descLength)
        assertEquals(expected = expectedSelection, actual = selection.selection)

        val focus = orchestrator.stores.focus.current()
        val expectedFocus = description.id
        assertEquals(expected = expectedFocus, actual = focus.requireTarget())
    }

    @Test
    fun `Not empty title with empty desc and without featured then cursor not at the end then Enter then split command`() {
        val title = createTitle(text = "FooBar")
        val description = createDescription("")
        val header = createHeader(title = title.id, description = description.id, featured = null)
        val block = createBlock()
        val page = createPage(root = root, children = listOf(header.id, block.id))
        val doc = listOf(page, header, title, description, block)
        setupInteractions(doc)
        val vm = buildViewModel()
        vm.onStart(root)

        val range = IntRange(start = 3, endInclusive = 3)

        vm.onSelectionChanged(
            id = title.id,
            selection = range
        )

        vm.onBlockFocusChanged(
            id = title.id,
            hasFocus = true
        )

        vm.onKeyPressedEvent(
            KeyPressedEvent.OnTitleBlockEnterKeyEvent(
                target = title.id,
                text = title.content<Block.Content.Text>().text,
                range = range
            )
        )

        coroutineTestRule.advanceTime(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)

        verifyBlocking(updateText, times(1)) {
            invoke(
                params = eq(
                    UpdateText.Params(
                        context = root,
                        text = title.content<Block.Content.Text>().text,
                        marks = emptyList(),
                        target = title.id
                    )
                )
            )
        }

        verifyBlocking(splitBlock, times(1)) {
            invoke(
                params = eq(
                    SplitBlock.Params(
                        context = root,
                        block = title,
                        range = range,
                        isToggled = null
                    )
                )
            )
        }
    }

    @Test
    fun `Not empty title with empty desc and without featured then cursor at the end then Enter then set cursor to description`() {
        val title = createTitle(text = "FooBar")
        val description = createDescription("")
        val header = createHeader(title = title.id, description = description.id, featured = null)
        val block = createBlock()
        val page = createPage(root = root, children = listOf(header.id, block.id))
        val doc = listOf(page, header, title, description, block)
        setupInteractions(doc)
        val vm = buildViewModel()
        vm.onStart(root)

        val range = IntRange(start = 6, endInclusive = 6)

        vm.onSelectionChanged(
            id = title.id,
            selection = range
        )

        vm.onBlockFocusChanged(
            id = title.id,
            hasFocus = true
        )

        vm.onKeyPressedEvent(
            KeyPressedEvent.OnTitleBlockEnterKeyEvent(
                target = title.id,
                text = title.content<Block.Content.Text>().text,
                range = range
            )
        )

        coroutineTestRule.advanceTime(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)

        verifyNoInteractions(createBlock)
        verifyNoInteractions(splitBlock)
        verifyNoInteractions(mergeBlocks)
        verifyNoInteractions(updateText)

        val selection = orchestrator.stores.textSelection.current()
        val descLength = description.content.asText().text.length
        val expectedSelection = IntRange(descLength, descLength)
        assertEquals(expected = expectedSelection, actual = selection.selection)

        val focus = orchestrator.stores.focus.current()
        val expectedFocus = description.id
        assertEquals(expected = expectedFocus, actual = focus.requireTarget())
    }
    //endregion

    //region TITLE, NOT EMPTY DESCRIPTION WITH FEATURED
    @Test
    fun `Empty title with desc and featured then Enter then set cursor to end of description`() {
        val title = createTitle(text = "")
        val featured = createFeaturedBlock()
        val description = createDescription(description = MockDataFactory.randomString())
        val header = createHeader(
            title = title.id,
            description = description.id,
            featured = featured.id
        )
        val block = createBlock()
        val page = createPage(root = root, children = listOf(header.id, block.id))
        val relation2 = Relation(
            key = MockDataFactory.randomString(),
            name = "Year",
            format = Relation.Format.NUMBER,
            source = Relation.Source.values().random()
        )
        val doc = listOf(page, header, title, description, featured, block)
        setupInteractions(
            doc = doc,
            details = createDetailsWithFeaturedRelations(root, description.id, relation2.key),
            relations = listOf()
        )
        val vm = buildViewModel()
        vm.onStart(root)

        val range = IntRange(start = 0, endInclusive = 0)

        vm.onSelectionChanged(
            id = title.id,
            selection = range
        )

        vm.onBlockFocusChanged(
            id = title.id,
            hasFocus = true
        )

        vm.onKeyPressedEvent(
            KeyPressedEvent.OnTitleBlockEnterKeyEvent(
                target = title.id,
                text = title.content<Block.Content.Text>().text,
                range = range
            )
        )

        coroutineTestRule.advanceTime(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)

        verifyNoInteractions(createBlock)
        verifyNoInteractions(splitBlock)
        verifyNoInteractions(mergeBlocks)
        verifyNoInteractions(updateText)

        val selection = orchestrator.stores.textSelection.current()
        val descLength = description.content.asText().text.length
        val expectedSelection = IntRange(descLength, descLength)
        assertEquals(expected = expectedSelection, actual = selection.selection)

        val focus = orchestrator.stores.focus.current()
        val expectedFocus = description.id
        assertEquals(expected = expectedFocus, actual = focus.requireTarget())
    }

    @Test
    fun `Empty title with desc and featured then Enter then set cursor to end of description when description block is at the end of childrens`() {
        val title = createTitle(text = "")
        val featured = createFeaturedBlock()
        val description = createDescription(description = MockDataFactory.randomString())
        val header = createHeader(
            title = title.id,
            description = description.id,
            featured = featured.id
        )
        val block = createBlock()
        val page = createPage(root = root, children = listOf(header.id, block.id))
        val relation2 = Relation(
            key = MockDataFactory.randomString(),
            name = "Year",
            format = Relation.Format.NUMBER,
            source = Relation.Source.values().random()
        )
        val doc = listOf(page, header, title, featured, block, description)
        setupInteractions(
            doc = doc,
            details = createDetailsWithFeaturedRelations(root, description.id, relation2.key),
            relations = listOf()
        )
        val vm = buildViewModel()
        vm.onStart(root)

        val range = IntRange(start = 0, endInclusive = 0)

        vm.onSelectionChanged(
            id = title.id,
            selection = range
        )

        vm.onBlockFocusChanged(
            id = title.id,
            hasFocus = true
        )

        vm.onKeyPressedEvent(
            KeyPressedEvent.OnTitleBlockEnterKeyEvent(
                target = title.id,
                text = title.content<Block.Content.Text>().text,
                range = range
            )
        )

        coroutineTestRule.advanceTime(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)

        verifyNoInteractions(createBlock)
        verifyNoInteractions(splitBlock)
        verifyNoInteractions(mergeBlocks)
        verifyNoInteractions(updateText)

        val selection = orchestrator.stores.textSelection.current()
        val descLength = description.content.asText().text.length
        val expectedSelection = IntRange(descLength, descLength)
        assertEquals(expected = expectedSelection, actual = selection.selection)

        val focus = orchestrator.stores.focus.current()
        val expectedFocus = description.id
        assertEquals(expected = expectedFocus, actual = focus.requireTarget())
    }

    @Test
    fun `Not empty title with desc and featured then cursor not at the end then Enter then split command`() {
        val title = createTitle(text = "FooBar")
        val featured = createFeaturedBlock()
        val description = createDescription(description = MockDataFactory.randomString())
        val header =
            createHeader(title = title.id, description = description.id, featured = featured.id)
        val block = createBlock()
        val page = createPage(root = root, children = listOf(header.id, block.id))
        val doc = listOf(page, header, title, description, featured, block)
        setupInteractions(doc)
        val vm = buildViewModel()
        vm.onStart(root)

        val range = IntRange(start = 3, endInclusive = 3)

        vm.onSelectionChanged(
            id = title.id,
            selection = range
        )

        vm.onBlockFocusChanged(
            id = title.id,
            hasFocus = true
        )

        vm.onKeyPressedEvent(
            KeyPressedEvent.OnTitleBlockEnterKeyEvent(
                target = title.id,
                text = title.content<Block.Content.Text>().text,
                range = range
            )
        )

        coroutineTestRule.advanceTime(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)

        verifyBlocking(updateText, times(1)) {
            invoke(
                params = eq(
                    UpdateText.Params(
                        context = root,
                        text = title.content<Block.Content.Text>().text,
                        marks = emptyList(),
                        target = title.id
                    )
                )
            )
        }

        verifyBlocking(splitBlock, times(1)) {
            invoke(
                params = eq(
                    SplitBlock.Params(
                        context = root,
                        block = title,
                        range = range,
                        isToggled = null
                    )
                )
            )
        }
    }

    @Test
    fun `Not empty title with desc and featured then cursor at the end then Enter then set cursor to end of description`() {
        val title = createTitle(text = "FooBar")
        val featured = createFeaturedBlock()
        val description = createDescription(description = MockDataFactory.randomString())
        val header =
            createHeader(title = title.id, description = description.id, featured = featured.id)
        val block = createBlock()
        val page = createPage(root = root, children = listOf(header.id, block.id))
        val doc = listOf(page, header, title, description, featured, block)
        setupInteractions(doc)
        val vm = buildViewModel()
        vm.onStart(root)

        val range = IntRange(start = 6, endInclusive = 6)

        vm.onSelectionChanged(
            id = title.id,
            selection = range
        )

        vm.onBlockFocusChanged(
            id = title.id,
            hasFocus = true
        )

        vm.onKeyPressedEvent(
            KeyPressedEvent.OnTitleBlockEnterKeyEvent(
                target = title.id,
                text = title.content<Block.Content.Text>().text,
                range = range
            )
        )

        coroutineTestRule.advanceTime(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)

        verifyNoInteractions(createBlock)
        verifyNoInteractions(splitBlock)
        verifyNoInteractions(mergeBlocks)
        verifyNoInteractions(updateText)

        val selection = orchestrator.stores.textSelection.current()
        val descLength = description.content.asText().text.length
        val expectedSelection = IntRange(descLength, descLength)
        assertEquals(expected = expectedSelection, actual = selection.selection)

        val focus = orchestrator.stores.focus.current()
        val expectedFocus = description.id
        assertEquals(expected = expectedFocus, actual = focus.requireTarget())
    }
    //endregion

    //region TITLE, NOT EMPTY DESCRIPTION WITHOUT FEATURED
    @Test
    fun `Empty title with desc and without featured then Enter then set cursor to end of description`() {
        val title = createTitle(text = "")
        val description = createDescription(description = MockDataFactory.randomString())
        val header = createHeader(
            title = title.id,
            description = description.id,
            featured = null
        )
        val block = createBlock()
        val page = createPage(root = root, children = listOf(header.id, block.id))
        val relation2 = Relation(
            key = MockDataFactory.randomString(),
            name = "Year",
            format = Relation.Format.NUMBER,
            source = Relation.Source.values().random()
        )
        val doc = listOf(page, header, title, description, block)
        setupInteractions(
            doc = doc,
            details = createDetailsWithFeaturedRelations(root, description.id, relation2.key),
            relations = listOf()
        )
        val vm = buildViewModel()
        vm.onStart(root)

        val range = IntRange(start = 0, endInclusive = 0)

        vm.onSelectionChanged(
            id = title.id,
            selection = range
        )

        vm.onBlockFocusChanged(
            id = title.id,
            hasFocus = true
        )

        vm.onKeyPressedEvent(
            KeyPressedEvent.OnTitleBlockEnterKeyEvent(
                target = title.id,
                text = title.content<Block.Content.Text>().text,
                range = range
            )
        )

        coroutineTestRule.advanceTime(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)

        verifyNoInteractions(createBlock)
        verifyNoInteractions(splitBlock)
        verifyNoInteractions(mergeBlocks)
        verifyNoInteractions(updateText)

        val selection = orchestrator.stores.textSelection.current()
        val descLength = description.content.asText().text.length
        val expectedSelection = IntRange(descLength, descLength)
        assertEquals(expected = expectedSelection, actual = selection.selection)

        val focus = orchestrator.stores.focus.current()
        val expectedFocus = description.id
        assertEquals(
            expected = expectedFocus,
            actual = focus.requireTarget()
        )
    }

    @Test
    fun `Not empty title with desc and without featured then cursor not at the end then Enter then split command`() {
        val title = createTitle(text = "FooBar")
        val description = createDescription(description = MockDataFactory.randomString())
        val header =
            createHeader(title = title.id, description = description.id, featured = null)
        val block = createBlock()
        val page = createPage(root = root, children = listOf(header.id, block.id))
        val doc = listOf(page, header, title, description, block)
        setupInteractions(doc)
        val vm = buildViewModel()
        vm.onStart(root)

        val range = IntRange(start = 3, endInclusive = 3)

        vm.onSelectionChanged(
            id = title.id,
            selection = range
        )

        vm.onBlockFocusChanged(
            id = title.id,
            hasFocus = true
        )

        vm.onKeyPressedEvent(
            KeyPressedEvent.OnTitleBlockEnterKeyEvent(
                target = title.id,
                text = title.content<Block.Content.Text>().text,
                range = range
            )
        )

        coroutineTestRule.advanceTime(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)

        verifyBlocking(updateText, times(1)) {
            invoke(
                params = eq(
                    UpdateText.Params(
                        context = root,
                        text = title.content<Block.Content.Text>().text,
                        marks = emptyList(),
                        target = title.id
                    )
                )
            )
        }

        verifyBlocking(splitBlock, times(1)) {
            invoke(
                params = eq(
                    SplitBlock.Params(
                        context = root,
                        block = title,
                        range = range,
                        isToggled = null
                    )
                )
            )
        }
    }

    @Test
    fun `Not empty title with desc and without featured then cursor at the end then Enter then set cursor to end of description`() {
        val title = createTitle(text = "FooBar")
        val description = createDescription(description = MockDataFactory.randomString())
        val header =
            createHeader(title = title.id, description = description.id, featured = null)
        val block = createBlock()
        val page = createPage(root = root, children = listOf(header.id, block.id))
        val doc = listOf(page, header, title, description, block)
        setupInteractions(doc)
        val vm = buildViewModel()
        vm.onStart(root)

        val range = IntRange(start = 6, endInclusive = 6)

        vm.onSelectionChanged(
            id = title.id,
            selection = range
        )

        vm.onBlockFocusChanged(
            id = title.id,
            hasFocus = true
        )

        vm.onKeyPressedEvent(
            KeyPressedEvent.OnTitleBlockEnterKeyEvent(
                target = title.id,
                text = title.content<Block.Content.Text>().text,
                range = range
            )
        )

        coroutineTestRule.advanceTime(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)

        verifyNoInteractions(createBlock)
        verifyNoInteractions(splitBlock)
        verifyNoInteractions(mergeBlocks)
        verifyNoInteractions(updateText)

        val selection = orchestrator.stores.textSelection.current()
        val descLength = description.content.asText().text.length
        val expectedSelection = IntRange(descLength, descLength)
        assertEquals(expected = expectedSelection, actual = selection.selection)

        val focus = orchestrator.stores.focus.current()
        val expectedFocus = description.id
        assertEquals(expected = expectedFocus, actual = focus.requireTarget())
    }
    //endregion

    //region EMPTY DESCRIPTION WITH FEATURED
    @Test
    fun `Empty description then enter then split command`() {
        val title = createTitle(text = "")
        val featured = createFeaturedBlock()
        val description = createDescription(description = "")
        val header = createHeader(
            title = title.id,
            description = description.id,
            featured = featured.id
        )
        val block = createBlock()
        val page = createPage(root = root, children = listOf(header.id, block.id))
        val relation2 = Relation(
            key = MockDataFactory.randomString(),
            name = "Year",
            format = Relation.Format.NUMBER,
            source = Relation.Source.values().random()
        )
        val doc = listOf(page, header, title, description, featured, block)
        setupInteractions(
            doc = doc,
            details = createDetailsWithFeaturedRelations(root, description.id, relation2.key),
            relations = listOf()
        )
        val vm = buildViewModel()
        vm.onStart(root)

        val range = IntRange(start = 0, endInclusive = 0)

        vm.onSelectionChanged(
            id = description.id,
            selection = range
        )

        vm.onBlockFocusChanged(
            id = description.id,
            hasFocus = true
        )

        vm.onSplitObjectDescription(
            target = description.id,
            text = description.content<Block.Content.Text>().text,
            range = range
        )

        coroutineTestRule.advanceTime(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)

        verifyBlocking(updateText, times(1)) {
            invoke(
                params = eq(
                    UpdateText.Params(
                        context = root,
                        text = description.content<Block.Content.Text>().text,
                        marks = emptyList(),
                        target = description.id
                    )
                )
            )
        }

        verifyBlocking(splitBlock, times(1)) {
            invoke(
                params = eq(
                    SplitBlock.Params(
                        context = root,
                        block = description,
                        range = range,
                        isToggled = null
                    )
                )
            )
        }
    }

    @Test
    fun `Not Empty description then enter in the middle then split command`() {
        val title = createTitle(text = "")
        val featured = createFeaturedBlock()
        val description = createDescription(description = "9SIEdZU5")
        val header = createHeader(
            title = title.id,
            description = description.id,
            featured = featured.id
        )
        val block = createBlock()
        val page = createPage(root = root, children = listOf(header.id, block.id))
        val relation2 = Relation(
            key = MockDataFactory.randomString(),
            name = "Year",
            format = Relation.Format.NUMBER,
            source = Relation.Source.values().random()
        )
        val doc = listOf(page, header, title, description, featured, block)
        setupInteractions(
            doc = doc,
            details = createDetailsWithFeaturedRelations(root, description.id, relation2.key),
            relations = listOf()
        )
        val vm = buildViewModel()
        vm.onStart(root)

        val range = IntRange(start = 3, endInclusive = 3)

        vm.onSelectionChanged(
            id = description.id,
            selection = range
        )

        vm.onBlockFocusChanged(
            id = description.id,
            hasFocus = true
        )

        vm.onSplitObjectDescription(
            target = description.id,
            text = description.content<Block.Content.Text>().text,
            range = range
        )

        coroutineTestRule.advanceTime(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)

        verifyBlocking(updateText, times(1)) {
            invoke(
                params = eq(
                    UpdateText.Params(
                        context = root,
                        text = description.content<Block.Content.Text>().text,
                        marks = emptyList(),
                        target = description.id
                    )
                )
            )
        }

        verifyBlocking(splitBlock, times(1)) {
            invoke(
                params = eq(
                    SplitBlock.Params(
                        context = root,
                        block = description,
                        range = range,
                        isToggled = null
                    )
                )
            )
        }
    }

    @Test
    fun `Not Empty description then enter at the line end then split command`() {
        val title = createTitle(text = "")
        val featured = createFeaturedBlock()
        val description = createDescription(description = "9SIEdZU5")
        val header = createHeader(
            title = title.id,
            description = description.id,
            featured = featured.id
        )
        val block = createBlock()
        val page = createPage(root = root, children = listOf(header.id, block.id))
        val relation2 = Relation(
            key = MockDataFactory.randomString(),
            name = "Year",
            format = Relation.Format.NUMBER,
            source = Relation.Source.values().random()
        )
        val doc = listOf(page, header, title, description, featured, block)
        setupInteractions(
            doc = doc,
            details = createDetailsWithFeaturedRelations(root, description.id, relation2.key),
            relations = listOf()
        )
        val vm = buildViewModel()
        vm.onStart(root)

        val range = IntRange(start = 8, endInclusive = 8)

        vm.onSelectionChanged(
            id = description.id,
            selection = range
        )

        vm.onBlockFocusChanged(
            id = description.id,
            hasFocus = true
        )

        vm.onSplitObjectDescription(
            target = description.id,
            text = description.content<Block.Content.Text>().text,
            range = range
        )

        coroutineTestRule.advanceTime(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)

        verifyBlocking(updateText, times(1)) {
            invoke(
                params = eq(
                    UpdateText.Params(
                        context = root,
                        text = description.content<Block.Content.Text>().text,
                        marks = emptyList(),
                        target = description.id
                    )
                )
            )
        }

        verifyBlocking(splitBlock, times(1)) {
            invoke(
                params = eq(
                    SplitBlock.Params(
                        context = root,
                        block = description,
                        range = range,
                        isToggled = null
                    )
                )
            )
        }
    }

    //endregion

    //region EMPTY DESCRIPTION WITHOUT FEATURED
    @Test
    fun `Empty description without featured then enter then split command`() {
        val title = createTitle(text = "")
        val description = createDescription(description = "")
        val header = createHeader(
            title = title.id,
            description = description.id,
            featured = null
        )
        val block = createBlock()
        val page = createPage(root = root, children = listOf(header.id, block.id))
        val relation2 = Relation(
            key = MockDataFactory.randomString(),
            name = "Year",
            format = Relation.Format.NUMBER,
            source = Relation.Source.values().random()
        )
        val doc = listOf(page, header, title, description, block)
        setupInteractions(
            doc = doc,
            details = createDetailsWithFeaturedRelations(root, description.id, relation2.key),
            relations = listOf()
        )
        val vm = buildViewModel()
        vm.onStart(root)

        val range = IntRange(start = 0, endInclusive = 0)

        vm.onSelectionChanged(
            id = description.id,
            selection = range
        )

        vm.onBlockFocusChanged(
            id = description.id,
            hasFocus = true
        )

        vm.onSplitObjectDescription(
            target = description.id,
            text = description.content<Block.Content.Text>().text,
            range = range
        )

        coroutineTestRule.advanceTime(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)

        verifyBlocking(updateText, times(1)) {
            invoke(
                params = eq(
                    UpdateText.Params(
                        context = root,
                        text = description.content<Block.Content.Text>().text,
                        marks = emptyList(),
                        target = description.id
                    )
                )
            )
        }

        verifyBlocking(splitBlock, times(1)) {
            invoke(
                params = eq(
                    SplitBlock.Params(
                        context = root,
                        block = description,
                        range = range,
                        isToggled = null
                    )
                )
            )
        }
    }

    @Test
    fun `Not Empty description without featured then enter in the middle then split command`() {
        val title = createTitle(text = "")
        val description = createDescription(description = "9SIEdZU5")
        val header = createHeader(
            title = title.id,
            description = description.id,
            featured = null
        )
        val block = createBlock()
        val page = createPage(root = root, children = listOf(header.id, block.id))
        val relation2 = Relation(
            key = MockDataFactory.randomString(),
            name = "Year",
            format = Relation.Format.NUMBER,
            source = Relation.Source.values().random()
        )
        val doc = listOf(page, header, title, description, block)
        setupInteractions(
            doc = doc,
            details = createDetailsWithFeaturedRelations(root, description.id, relation2.key),
            relations = listOf()
        )
        val vm = buildViewModel()
        vm.onStart(root)

        val range = IntRange(start = 3, endInclusive = 3)

        vm.onSelectionChanged(
            id = description.id,
            selection = range
        )

        vm.onBlockFocusChanged(
            id = description.id,
            hasFocus = true
        )

        vm.onSplitObjectDescription(
            target = description.id,
            text = description.content<Block.Content.Text>().text,
            range = range
        )

        coroutineTestRule.advanceTime(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)

        verifyBlocking(updateText, times(1)) {
            invoke(
                params = eq(
                    UpdateText.Params(
                        context = root,
                        text = description.content<Block.Content.Text>().text,
                        marks = emptyList(),
                        target = description.id
                    )
                )
            )
        }

        verifyBlocking(splitBlock, times(1)) {
            invoke(
                params = eq(
                    SplitBlock.Params(
                        context = root,
                        block = description,
                        range = range,
                        isToggled = null
                    )
                )
            )
        }
    }

    @Test
    fun `Not Empty description without featured then enter at the line end then split command`() {
        val title = createTitle(text = "")
        val description = createDescription(description = "9SIEdZU5")
        val header = createHeader(
            title = title.id,
            description = description.id,
            featured = null
        )
        val block = createBlock()
        val page = createPage(root = root, children = listOf(header.id, block.id))
        val relation2 = Relation(
            key = MockDataFactory.randomString(),
            name = "Year",
            format = Relation.Format.NUMBER,
            source = Relation.Source.values().random()
        )
        val doc = listOf(page, header, title, description, block)
        setupInteractions(
            doc = doc,
            details = createDetailsWithFeaturedRelations(root, description.id, relation2.key),
            relations = listOf()
        )
        val vm = buildViewModel()
        vm.onStart(root)

        val range = IntRange(start = 8, endInclusive = 8)

        vm.onSelectionChanged(
            id = description.id,
            selection = range
        )

        vm.onBlockFocusChanged(
            id = description.id,
            hasFocus = true
        )

        vm.onSplitObjectDescription(
            target = description.id,
            text = description.content<Block.Content.Text>().text,
            range = range
        )

        coroutineTestRule.advanceTime(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)

        verifyBlocking(updateText, times(1)) {
            invoke(
                params = eq(
                    UpdateText.Params(
                        context = root,
                        text = description.content<Block.Content.Text>().text,
                        marks = emptyList(),
                        target = description.id
                    )
                )
            )
        }

        verifyBlocking(splitBlock, times(1)) {
            invoke(
                params = eq(
                    SplitBlock.Params(
                        context = root,
                        block = description,
                        range = range,
                        isToggled = null
                    )
                )
            )
        }
    }

    //endregion
}