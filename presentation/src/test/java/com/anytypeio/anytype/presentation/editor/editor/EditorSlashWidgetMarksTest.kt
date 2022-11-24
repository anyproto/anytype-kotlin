package com.anytypeio.anytype.presentation.editor.editor

import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.domain.block.interactor.UpdateBlocksMark
import com.anytypeio.anytype.domain.editor.Editor
import com.anytypeio.anytype.presentation.MockTypicalDocumentFactory
import com.anytypeio.anytype.presentation.editor.EditorViewModel.Companion.TEXT_CHANGES_DEBOUNCE_DURATION
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types
import com.anytypeio.anytype.presentation.editor.editor.slash.SlashEvent
import com.anytypeio.anytype.presentation.editor.editor.slash.SlashItem
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import com.anytypeio.anytype.test_utils.MockDataFactory
import net.lachlanmckee.timberjunit.TimberTestRule
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.times
import org.mockito.kotlin.verifyBlocking
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

class EditorSlashWidgetMarksTest : EditorPresentationTestSetup() {

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
        coroutineTestRule.advanceTime(TEXT_CHANGES_DEBOUNCE_DURATION)
    }


    /**
     * Testing clicks on Slash Style Markups
     * 1.1 Click on BOLD -> should hide slash widget
     * 1.2 Click on BOLD -> save in Store.Focus, targetId and selection
     * 1.3 Click on BOLD -> invoke updateBlocksMark useCase with proper mark
     * 2.1 Click on ITALIC -> should hide slash widget
     * 2.2 Click on ITALIC -> save in Store.Focus, targetId and selection
     * 2.3 Click on ITALIC -> invoke updateBlocksMark useCase with proper mark
     * 3.1 Click on STRIKETHROUGH -> should hide slash widget
     * 3.2 Click on STRIKETHROUGH -> save in Store.Focus, targetId and selection
     * 3.3 Click on STRIKETHROUGH -> invoke updateBlocksMark useCase with proper mark
     * 4.1 Click on CODE -> should hide slash widget
     * 4.2 Click on CODE -> save in Store.Focus, targetId and selection
     * 4.3 Click on CODE -> invoke updateBlocksMark useCase with proper mark
     * 5. Click on STYLE MARKUP -> save Focus.empty when selection is null
     */

    //region {1}
    @Test
    fun `should hide slash widget when clicked on BOLD `() {

        val doc = MockTypicalDocumentFactory.page(root)
        val block = MockTypicalDocumentFactory.a

        stubInterceptEvents()
        stubUpdateText()
        stubSearchObjects()
        stubUpdateBlocksMark()
        stubOpenDocument(document = doc)
        val vm = buildViewModel()

        vm.onStart(root)

        vm.apply {
            onSelectionChanged(
                id = block.id,
                selection = IntRange(0, 0)
            )
            onBlockFocusChanged(
                id = block.id,
                hasFocus = true
            )
            onSlashTextWatcherEvent(
                SlashEvent.Start(
                    cursorCoordinate = 100,
                    slashStart = 0
                )
            )
        }

        // TESTING

        vm.onSlashItemClicked(SlashItem.Style.Markup.Bold)

        val state = vm.controlPanelViewState.value

        assertNotNull(state)
        assertFalse(state.slashWidget.isVisible)
    }

    fun `should invoke update markup Bold command`() {

        val header = MockTypicalDocumentFactory.header
        val title = MockTypicalDocumentFactory.title

        val block = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = "FooBar",
                marks = listOf(),
                style = Block.Content.Text.Style.P
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(header.id, block.id)
        )

        val doc = listOf(page, header, title, block)

        stubInterceptEvents()
        stubUpdateBlocksMark()
        stubOpenDocument(document = doc)
        val vm = buildViewModel()

        vm.onStart(root)

        vm.apply {
            onSelectionChanged(
                id = block.id,
                selection = IntRange(3, 3)
            )
            onBlockFocusChanged(
                id = block.id,
                hasFocus = true
            )
            onSlashTextWatcherEvent(
                event = SlashEvent.Start(
                    cursorCoordinate = 820,
                    slashStart = 3
                )
            )
            onSlashTextWatcherEvent(
                event = SlashEvent.Filter(
                    filter = "/",
                    viewType = 0
                )
            )
            onTextBlockTextChanged(
                view = BlockView.Text.Paragraph(
                    id = block.id,
                    text = "Foo/Bar",
                    marks = listOf(),
                    isFocused = true,
                    mode = BlockView.Mode.EDIT,
                    isSelected = false,
                    cursor = null
                )
            )
            onSelectionChanged(
                id = block.id,
                selection = IntRange(4, 4)
            )
            onSlashTextWatcherEvent(
                event = SlashEvent.Filter(
                    filter = "/b",
                    viewType = 0
                )
            )
            onTextBlockTextChanged(
                view = BlockView.Text.Paragraph(
                    id = block.id,
                    text = "Foo/bBar",
                    marks = listOf(),
                    isFocused = true,
                    mode = BlockView.Mode.EDIT,
                    isSelected = false,
                    cursor = null
                )
            )
            onSelectionChanged(
                id = block.id,
                selection = IntRange(5, 5)
            )
            onSlashTextWatcherEvent(
                event = SlashEvent.Filter(
                    filter = "/bo",
                    viewType = 0
                )
            )
            onTextBlockTextChanged(
                view = BlockView.Text.Paragraph(
                    id = block.id,
                    text = "Foo/boBar",
                    marks = listOf(),
                    isFocused = true,
                    mode = BlockView.Mode.EDIT,
                    isSelected = false,
                    cursor = null
                )
            )
            onSelectionChanged(
                id = block.id,
                selection = IntRange(6, 6)
            )
        }

        //TESTING

        vm.onSlashItemClicked(
            item = SlashItem.Style.Markup.Bold
        )

        val params = UpdateBlocksMark.Params(
            context = root,
            targets = listOf(block.id),
            mark = Block.Content.Text.Mark(
                range = IntRange(0, block.content.asText().text.length),
                type = Block.Content.Text.Mark.Type.BOLD
            )
        )

        verifyBlocking(updateBlocksMark, times(1)) { invoke(params) }
    }

    @Test
    fun `should save selection and focus on slash start index when clicked on BOLD`() {
        val header = MockTypicalDocumentFactory.header
        val title = MockTypicalDocumentFactory.title

        val block = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = "Update block with BOLD",
                marks = listOf(),
                style = Block.Content.Text.Style.P
            )
        )

        val block2 = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.BULLET
            )
        )

        val block3 = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.NUMBERED
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(header.id, block.id, block2.id, block3.id)
        )

        val doc = listOf(page, header, title, block, block2, block3)

        stubInterceptEvents()
        stubUpdateBlocksMark()
        stubUpdateText()
        stubSearchObjects()
        stubOpenDocument(document = doc)
        val vm = buildViewModel()

        vm.onStart(root)

        val selection = IntRange(7, 7)

        vm.apply {
            onSelectionChanged(
                id = block.id,
                selection = selection
            )
            onBlockFocusChanged(
                id = block.id,
                hasFocus = true
            )
            onSlashTextWatcherEvent(
                SlashEvent.Start(
                    cursorCoordinate = 100,
                    slashStart = 7
                )
            )
            onSlashTextWatcherEvent(
                event = SlashEvent.Filter(
                    filter = "/",
                    viewType = 0
                )
            )
        }

        //TESTING

        vm.onSlashItemClicked(SlashItem.Style.Markup.Bold)

        val focus = orchestrator.stores.focus.current()
        val cursor = Editor.Cursor.Range(range = selection)

        assertEquals(block.id, focus.id)
        assertEquals(cursor, focus.cursor)
    }

    @Test
    fun `should invoke updateBlocksMark when clicked on BOLD`() {
        val header = MockTypicalDocumentFactory.header
        val title = MockTypicalDocumentFactory.title

        val block = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = listOf(),
                style = Block.Content.Text.Style.P
            )
        )

        val block2 = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = "Anytype is a next generation software",
                marks = listOf(
                    Block.Content.Text.Mark(
                        range = IntRange(3, 10),
                        type = Block.Content.Text.Mark.Type.ITALIC
                    )
                ),
                style = Block.Content.Text.Style.BULLET
            )
        )

        val block3 = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.NUMBERED
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(header.id, block.id, block2.id, block3.id)
        )

        val doc = listOf(page, header, title, block, block2, block3)

        stubInterceptEvents()
        stubUpdateBlocksMark()
        stubUpdateText()
        stubSearchObjects()
        stubOpenDocument(document = doc)
        val vm = buildViewModel()

        vm.onStart(root)

        val selection = IntRange(7, 7)

        vm.apply {
            onSelectionChanged(
                id = block2.id,
                selection = selection
            )
            onBlockFocusChanged(
                id = block2.id,
                hasFocus = true
            )
            onSlashTextWatcherEvent(
                SlashEvent.Start(
                    cursorCoordinate = 100,
                    slashStart = 7
                )
            )
            onSlashTextWatcherEvent(
                event = SlashEvent.Filter(
                    filter = "/",
                    viewType = Types.HOLDER_BULLET
                )
            )
            onTextBlockTextChanged(
                view = BlockView.Text.Paragraph(
                    id = block2.id,
                    text = "Anytype/ is a next generation software",
                    marks = listOf(),
                    isFocused = true,
                    mode = BlockView.Mode.EDIT,
                    isSelected = false,
                    cursor = null
                )
            )
        }

        coroutineTestRule.advanceTime(300)

        //TESTING

        vm.onSlashItemClicked(SlashItem.Style.Markup.Bold)

        val params = UpdateBlocksMark.Params(
            context = root,
            targets = listOf(block2.id),
            mark = Block.Content.Text.Mark(
                range = IntRange(0, block2.content.asText().text.length),
                type = Block.Content.Text.Mark.Type.BOLD
            )
        )

        verifyBlocking(updateBlocksMark, times(1)) { invoke(params) }
    }
    //endregion

    //region {2}
    @Test
    fun `should hide slash widget event when clicked on ITALIC `() {

        val doc = MockTypicalDocumentFactory.page(root)
        val block = MockTypicalDocumentFactory.a

        stubInterceptEvents()
        stubUpdateBlocksMark()
        stubUpdateText()
        stubSearchObjects()
        stubOpenDocument(document = doc)
        val vm = buildViewModel()

        vm.onStart(root)

        vm.apply {
            onSelectionChanged(
                id = block.id,
                selection = IntRange(0, 0)
            )
            onBlockFocusChanged(
                id = block.id,
                hasFocus = true
            )
            onSlashTextWatcherEvent(
                SlashEvent.Start(
                    cursorCoordinate = 100,
                    slashStart = 0
                )
            )
            onSlashTextWatcherEvent(
                event = SlashEvent.Filter(
                    filter = "/",
                    viewType = 0
                )
            )
        }

        // TESTING

        vm.onSlashItemClicked(SlashItem.Style.Markup.Italic)

        val state = vm.controlPanelViewState.value

        assertNotNull(state)
        assertFalse(state.slashWidget.isVisible)
    }

    @Test
    fun `should save selection and focus when clicked on ITALIC`() {
        val header = MockTypicalDocumentFactory.header
        val title = MockTypicalDocumentFactory.title

        val block = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = "Anytype is a next generation software",
                marks = listOf(),
                style = Block.Content.Text.Style.P
            )
        )

        val block2 = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.BULLET
            )
        )

        val block3 = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.NUMBERED
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(header.id, block.id, block2.id, block3.id)
        )

        val doc = listOf(page, header, title, block, block2, block3)

        stubInterceptEvents()
        stubUpdateBlocksMark()
        stubUpdateText()
        stubSearchObjects()
        stubOpenDocument(document = doc)
        val vm = buildViewModel()

        vm.onStart(root)

        val selection = IntRange(11, 11)

        vm.apply {
            onSelectionChanged(
                id = block.id,
                selection = selection
            )
            onBlockFocusChanged(
                id = block.id,
                hasFocus = true
            )
            onSlashTextWatcherEvent(
                SlashEvent.Start(
                    cursorCoordinate = 100,
                    slashStart = 11
                )
            )
            onSlashTextWatcherEvent(
                event = SlashEvent.Filter(
                    filter = "/",
                    viewType = 0
                )
            )
        }

        //TESTING

        vm.onSlashItemClicked(SlashItem.Style.Markup.Italic)

        val focus = orchestrator.stores.focus.current()
        val cursor = Editor.Cursor.Range(range = selection)

        assertEquals(block.id, focus.id)
        assertEquals(cursor, focus.cursor)
    }

    @Test
    fun `should invoke updateBlocksMark when clicked on ITALIC`() {
        val header = MockTypicalDocumentFactory.header
        val title = MockTypicalDocumentFactory.title

        val block = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = listOf(),
                style = Block.Content.Text.Style.P
            )
        )

        val block2 = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = "Anytype is a next generation software",
                marks = listOf(),
                style = Block.Content.Text.Style.BULLET
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(header.id, block.id, block2.id)
        )

        val doc = listOf(page, header, title, block, block2)

        stubInterceptEvents()
        stubUpdateBlocksMark()
        stubUpdateText()
        stubSearchObjects()
        stubOpenDocument(document = doc)
        val vm = buildViewModel()

        vm.onStart(root)

        val selection = IntRange(7, 7)

        vm.apply {
            onSelectionChanged(
                id = block2.id,
                selection = selection
            )
            onBlockFocusChanged(
                id = block2.id,
                hasFocus = true
            )
            onSlashTextWatcherEvent(
                SlashEvent.Start(
                    cursorCoordinate = 100,
                    slashStart = 7
                )
            )
            onSlashTextWatcherEvent(
                event = SlashEvent.Filter(
                    filter = "/",
                    viewType = 0
                )
            )
            onTextBlockTextChanged(
                view = BlockView.Text.Paragraph(
                    id = block2.id,
                    text = "Anytype/ is a next generation software",
                    marks = listOf(),
                    isFocused = true,
                    mode = BlockView.Mode.EDIT,
                    isSelected = false,
                    cursor = null
                )
            )
        }

        //TESTING

        vm.onSlashItemClicked(SlashItem.Style.Markup.Italic)

        val params = UpdateBlocksMark.Params(
            context = root,
            targets = listOf(block2.id),
            mark = Block.Content.Text.Mark(
                range = IntRange(0, block2.content.asText().text.length),
                type = Block.Content.Text.Mark.Type.ITALIC
            )
        )

        verifyBlocking(updateBlocksMark, times(1)) { invoke(params) }
    }
    //endregion

    //region {3}
    @Test
    fun `should hide slash widget event when clicked on STRIKETHROUGH `() {

        val doc = MockTypicalDocumentFactory.page(root)
        val block = MockTypicalDocumentFactory.a

        stubInterceptEvents()
        stubUpdateBlocksMark()
        stubUpdateText()
        stubSearchObjects()
        stubOpenDocument(document = doc)
        val vm = buildViewModel()

        vm.onStart(root)

        vm.apply {
            onSelectionChanged(
                id = block.id,
                selection = IntRange(0, 0)
            )
            onBlockFocusChanged(
                id = block.id,
                hasFocus = true
            )
            onSlashTextWatcherEvent(
                SlashEvent.Start(
                    cursorCoordinate = 100,
                    slashStart = 0
                )
            )
        }

        // TESTING

        vm.onSlashItemClicked(SlashItem.Style.Markup.Strikethrough)

        val state = vm.controlPanelViewState.value

        assertNotNull(state)
        assertFalse(state.slashWidget.isVisible)
    }

    @Test
    fun `should save selection and focus when clicked on STRIKETHROUGH`() {
        val header = MockTypicalDocumentFactory.header
        val title = MockTypicalDocumentFactory.title

        val block = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = "Anytype is a next generation software",
                marks = listOf(),
                style = Block.Content.Text.Style.P
            )
        )

        val block2 = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.BULLET
            )
        )

        val block3 = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.NUMBERED
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(header.id, block.id, block2.id, block3.id)
        )

        val doc = listOf(page, header, title, block, block2, block3)

        stubInterceptEvents()
        stubUpdateBlocksMark()
        stubUpdateText()
        stubSearchObjects()
        stubOpenDocument(document = doc)
        val vm = buildViewModel()

        vm.onStart(root)

        val selection = IntRange(5, 5)

        vm.apply {
            onSelectionChanged(
                id = block2.id,
                selection = selection
            )
            onBlockFocusChanged(
                id = block2.id,
                hasFocus = true
            )
            onSlashTextWatcherEvent(
                SlashEvent.Start(
                    cursorCoordinate = 100,
                    slashStart = 5
                )
            )
            onSlashTextWatcherEvent(
                event = SlashEvent.Filter(
                    filter = "/",
                    viewType = 0
                )
            )
        }

        //TESTING

        vm.onSlashItemClicked(SlashItem.Style.Markup.Strikethrough)

        val focus = orchestrator.stores.focus.current()
        val cursor = Editor.Cursor.Range(range = selection)

        assertEquals(block2.id, focus.id)
        assertEquals(cursor, focus.cursor)
    }

    @Test
    fun `should invoke updateBlocksMark when clicked on STRIKETHROUGH`() {
        val header = MockTypicalDocumentFactory.header
        val title = MockTypicalDocumentFactory.title

        val block = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = "Anytype is a next generation software",
                marks = listOf(),
                style = Block.Content.Text.Style.P
            )
        )

        val block2 = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = listOf(),
                style = Block.Content.Text.Style.BULLET
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(header.id, block.id, block2.id)
        )

        val doc = listOf(page, header, title, block, block2)

        stubInterceptEvents()
        stubUpdateBlocksMark()
        stubUpdateText()
        stubSearchObjects()
        stubOpenDocument(document = doc)
        val vm = buildViewModel()

        vm.onStart(root)

        val selection = IntRange(7, 7)

        vm.apply {
            onSelectionChanged(
                id = block.id,
                selection = selection
            )
            onBlockFocusChanged(
                id = block.id,
                hasFocus = true
            )
            onSlashTextWatcherEvent(
                SlashEvent.Start(
                    cursorCoordinate = 100,
                    slashStart = 7
                )
            )
            onSlashTextWatcherEvent(
                event = SlashEvent.Filter(
                    filter = "/",
                    viewType = 0
                )
            )
            onTextBlockTextChanged(
                view = BlockView.Text.Paragraph(
                    id = block.id,
                    text = "Anytype/ is a next generation software",
                    marks = listOf(),
                    isFocused = true,
                    mode = BlockView.Mode.EDIT,
                    isSelected = false,
                    cursor = null
                )
            )
        }

        //TESTING

        vm.onSlashItemClicked(SlashItem.Style.Markup.Strikethrough)

        val params = UpdateBlocksMark.Params(
            context = root,
            targets = listOf(block.id),
            mark = Block.Content.Text.Mark(
                range = IntRange(0, block.content.asText().text.length),
                type = Block.Content.Text.Mark.Type.STRIKETHROUGH
            )
        )

        verifyBlocking(updateBlocksMark, times(1)) { invoke(params) }
    }
    //endregion

    //region {4}
    @Test
    fun `should hide slash widget event when clicked on CODE `() {

        val doc = MockTypicalDocumentFactory.page(root)
        val block = MockTypicalDocumentFactory.a

        stubInterceptEvents()
        stubUpdateBlocksMark()
        stubUpdateText()
        stubSearchObjects()
        stubOpenDocument(document = doc)
        val vm = buildViewModel()

        vm.onStart(root)

        vm.apply {
            onSelectionChanged(
                id = block.id,
                selection = IntRange(0, 0)
            )
            onBlockFocusChanged(
                id = block.id,
                hasFocus = true
            )
            onSlashTextWatcherEvent(
                SlashEvent.Start(
                    cursorCoordinate = 100,
                    slashStart = 0
                )
            )
            onSlashTextWatcherEvent(
                event = SlashEvent.Filter(
                    filter = "/",
                    viewType = 0
                )
            )
        }

        // TESTING

        vm.onSlashItemClicked(SlashItem.Style.Markup.Code)

        val state = vm.controlPanelViewState.value

        assertNotNull(state)
        assertFalse(state.slashWidget.isVisible)
    }

    @Test
    fun `should save selection and focus when clicked on CODE`() {
        val header = MockTypicalDocumentFactory.header
        val title = MockTypicalDocumentFactory.title

        val block = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = "Anytype is a next generation software",
                marks = listOf(),
                style = Block.Content.Text.Style.P
            )
        )

        val block2 = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.BULLET
            )
        )

        val block3 = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.NUMBERED
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(header.id, block.id, block2.id, block3.id)
        )

        val doc = listOf(page, header, title, block, block2, block3)

        stubInterceptEvents()
        stubUpdateBlocksMark()
        stubUpdateText()
        stubSearchObjects()
        stubOpenDocument(document = doc)
        val vm = buildViewModel()

        vm.onStart(root)

        val selection = IntRange(15, 15)

        vm.apply {
            onSelectionChanged(
                id = block3.id,
                selection = selection
            )
            onBlockFocusChanged(
                id = block3.id,
                hasFocus = true
            )
            onSlashTextWatcherEvent(
                SlashEvent.Start(
                    cursorCoordinate = 100,
                    slashStart = 15
                )
            )
            onSlashTextWatcherEvent(
                event = SlashEvent.Filter(
                    filter = "/",
                    viewType = 0
                )
            )
        }

        //TESTING

        vm.onSlashItemClicked(SlashItem.Style.Markup.Code)

        val focus = orchestrator.stores.focus.current()
        val cursor = Editor.Cursor.Range(range = selection)

        assertEquals(block3.id, focus.id)
        assertEquals(cursor, focus.cursor)
    }

    @Test
    fun `should invoke updateBlocksMark when clicked on CODE`() {
        val header = MockTypicalDocumentFactory.header
        val title = MockTypicalDocumentFactory.title

        val block = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = "Anytype is a next generation software",
                marks = listOf(),
                style = Block.Content.Text.Style.P
            )
        )

        val block2 = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = listOf(),
                style = Block.Content.Text.Style.BULLET
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(header.id, block.id, block2.id)
        )

        val doc = listOf(page, header, title, block, block2)

        stubInterceptEvents()
        stubUpdateBlocksMark()
        stubUpdateText()
        stubSearchObjects()
        stubOpenDocument(document = doc)
        val vm = buildViewModel()

        vm.onStart(root)

        val selection = IntRange(7, 7)

        vm.apply {
            onSelectionChanged(
                id = block.id,
                selection = selection
            )
            onBlockFocusChanged(
                id = block.id,
                hasFocus = true
            )
            onSlashTextWatcherEvent(
                SlashEvent.Start(
                    cursorCoordinate = 100,
                    slashStart = 7
                )
            )
            onSlashTextWatcherEvent(
                event = SlashEvent.Filter(
                    filter = "/",
                    viewType = 0
                )
            )
            onTextBlockTextChanged(
                view = BlockView.Text.Paragraph(
                    id = block.id,
                    text = "Anytype/ is a next generation software",
                    marks = listOf(),
                    isFocused = true,
                    mode = BlockView.Mode.EDIT,
                    isSelected = false,
                    cursor = null
                )
            )
        }

        //TESTING

        vm.onSlashItemClicked(SlashItem.Style.Markup.Code)

        val params = UpdateBlocksMark.Params(
            context = root,
            targets = listOf(block.id),
            mark = Block.Content.Text.Mark(
                range = IntRange(0, block.content.asText().text.length),
                type = Block.Content.Text.Mark.Type.KEYBOARD
            )
        )

        verifyBlocking(updateBlocksMark, times(1)) { invoke(params) }
    }
    //endregion
}