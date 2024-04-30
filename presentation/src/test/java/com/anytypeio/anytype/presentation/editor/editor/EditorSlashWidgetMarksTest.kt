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
import com.anytypeio.anytype.presentation.util.DefaultCoroutineTestRule
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import net.lachlanmckee.timberjunit.TimberTestRule
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.times
import org.mockito.kotlin.verifyBlocking

class EditorSlashWidgetMarksTest : EditorPresentationTestSetup() {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    val coroutineTestRule = DefaultCoroutineTestRule()

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
        stubSpaceManager()
        stubGetNetworkMode()
        stubFileLimitEvents()
        stubAnalyticSpaceHelperDelegate()
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
    fun `should hide slash widget when clicked on BOLD `() = runTest {

        val doc = MockTypicalDocumentFactory.page(root)
        val block = MockTypicalDocumentFactory.a

        stubInterceptEvents()
        stubUpdateText()
        stubSearchObjects()
        stubUpdateBlocksMark()
        stubOpenDocument(document = doc)
        val vm = buildViewModel()

        vm.onStart(id = root, space = defaultSpace)

        advanceUntilIdle()

        vm.apply {
            onSelectionChanged(
                id = block.id,
                selection = IntRange(0, 0)
            )
            advanceUntilIdle()
            onBlockFocusChanged(
                id = block.id,
                hasFocus = true
            )
            advanceUntilIdle()
            onSlashTextWatcherEvent(
                SlashEvent.Start(
                    cursorCoordinate = 100,
                    slashStart = 0
                )
            )
            advanceUntilIdle()
        }

        // TESTING

        vm.onSlashItemClicked(SlashItem.Style.Markup.Bold)

        advanceUntilIdle()

        val state = vm.controlPanelViewState.value

        assertNotNull(state)
        assertFalse(state.slashWidget.isVisible)
    }

    @Test
    fun `should save selection and focus on slash start index when clicked on BOLD`() = runTest {
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
            content = Block.Content.Smart,
            children = listOf(header.id, block.id, block2.id, block3.id)
        )

        val doc = listOf(page, header, title, block, block2, block3)

        stubInterceptEvents()
        stubUpdateBlocksMark()
        stubUpdateText()
        stubSearchObjects()
        stubOpenDocument(document = doc)
        val vm = buildViewModel()

        vm.onStart(id = root, space = defaultSpace)

        advanceUntilIdle()

        val selection = IntRange(7, 7)

        vm.apply {
            onSelectionChanged(
                id = block.id,
                selection = selection
            )
            advanceUntilIdle()
            onBlockFocusChanged(
                id = block.id,
                hasFocus = true
            )
            advanceUntilIdle()
            onSlashTextWatcherEvent(
                SlashEvent.Start(
                    cursorCoordinate = 100,
                    slashStart = 7
                )
            )
            advanceUntilIdle()
            onSlashTextWatcherEvent(
                event = SlashEvent.Filter(
                    filter = "/",
                    viewType = 0
                )
            )
            advanceUntilIdle()
        }

        //TESTING

        vm.onSlashItemClicked(SlashItem.Style.Markup.Bold)

        advanceUntilIdle()

        val focus = orchestrator.stores.focus.current()
        val cursor = Editor.Cursor.Range(range = selection)

        assertEquals(block.id, focus.requireTarget())
        assertEquals(cursor, focus.cursor)
    }

    @Test
    fun `should invoke updateBlocksMark when clicked on BOLD`() = runTest {
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
            content = Block.Content.Smart,
            children = listOf(header.id, block.id, block2.id, block3.id)
        )

        val doc = listOf(page, header, title, block, block2, block3)

        stubInterceptEvents()
        stubUpdateBlocksMark()
        stubUpdateText()
        stubSearchObjects()
        stubOpenDocument(document = doc)
        val vm = buildViewModel()

        vm.onStart(id = root, space = defaultSpace)

        advanceUntilIdle()

        val selection = IntRange(7, 7)

        vm.apply {
            onSelectionChanged(
                id = block2.id,
                selection = selection
            )
            advanceUntilIdle()
            onBlockFocusChanged(
                id = block2.id,
                hasFocus = true
            )
            advanceUntilIdle()
            onSlashTextWatcherEvent(
                SlashEvent.Start(
                    cursorCoordinate = 100,
                    slashStart = 7
                )
            )
            advanceUntilIdle()
            onSlashTextWatcherEvent(
                event = SlashEvent.Filter(
                    filter = "/",
                    viewType = Types.HOLDER_BULLET
                )
            )
            advanceUntilIdle()
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
            advanceUntilIdle()
        }

        coroutineTestRule.advanceTime(300)

        //TESTING

        vm.onSlashItemClicked(SlashItem.Style.Markup.Bold)

        advanceUntilIdle()

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
    fun `should hide slash widget event when clicked on ITALIC `() = runTest {

        val doc = MockTypicalDocumentFactory.page(root)
        val block = MockTypicalDocumentFactory.a

        stubInterceptEvents()
        stubUpdateBlocksMark()
        stubUpdateText()
        stubSearchObjects()
        stubOpenDocument(document = doc)
        val vm = buildViewModel()

        vm.onStart(id = root, space = defaultSpace)

        advanceUntilIdle()

        vm.apply {
            onSelectionChanged(
                id = block.id,
                selection = IntRange(0, 0)
            )
            advanceUntilIdle()
            onBlockFocusChanged(
                id = block.id,
                hasFocus = true
            )
            advanceUntilIdle()
            onSlashTextWatcherEvent(
                SlashEvent.Start(
                    cursorCoordinate = 100,
                    slashStart = 0
                )
            )
            advanceUntilIdle()
            onSlashTextWatcherEvent(
                event = SlashEvent.Filter(
                    filter = "/",
                    viewType = 0
                )
            )
            advanceUntilIdle()
        }

        // TESTING

        vm.onSlashItemClicked(SlashItem.Style.Markup.Italic)

        advanceUntilIdle()

        val state = vm.controlPanelViewState.value

        assertNotNull(state)
        assertFalse(state.slashWidget.isVisible)
    }

    @Test
    fun `should save selection and focus when clicked on ITALIC`() = runTest {
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
            content = Block.Content.Smart,
            children = listOf(header.id, block.id, block2.id, block3.id)
        )

        val doc = listOf(page, header, title, block, block2, block3)

        stubInterceptEvents()
        stubUpdateBlocksMark()
        stubUpdateText()
        stubSearchObjects()
        stubOpenDocument(document = doc)
        val vm = buildViewModel()

        vm.onStart(id = root, space = defaultSpace)

        advanceUntilIdle()

        val selection = IntRange(11, 11)

        vm.apply {
            onSelectionChanged(
                id = block.id,
                selection = selection
            )
            advanceUntilIdle()
            onBlockFocusChanged(
                id = block.id,
                hasFocus = true
            )
            advanceUntilIdle()
            onSlashTextWatcherEvent(
                SlashEvent.Start(
                    cursorCoordinate = 100,
                    slashStart = 11
                )
            )
            advanceUntilIdle()
            onSlashTextWatcherEvent(
                event = SlashEvent.Filter(
                    filter = "/",
                    viewType = 0
                )
            )
            advanceUntilIdle()
        }

        //TESTING

        vm.onSlashItemClicked(SlashItem.Style.Markup.Italic)

        advanceUntilIdle()

        val focus = orchestrator.stores.focus.current()
        val cursor = Editor.Cursor.Range(range = selection)

        assertEquals(block.id, focus.requireTarget())
        assertEquals(cursor, focus.cursor)
    }

    @Test
    fun `should invoke updateBlocksMark when clicked on ITALIC`() = runTest {
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
            content = Block.Content.Smart,
            children = listOf(header.id, block.id, block2.id)
        )

        val doc = listOf(page, header, title, block, block2)

        stubInterceptEvents()
        stubUpdateBlocksMark()
        stubUpdateText()
        stubSearchObjects()
        stubOpenDocument(document = doc)
        val vm = buildViewModel()

        vm.onStart(id = root, space = defaultSpace)

        advanceUntilIdle()

        val selection = IntRange(7, 7)

        vm.apply {
            onSelectionChanged(
                id = block2.id,
                selection = selection
            )
            advanceUntilIdle()
            onBlockFocusChanged(
                id = block2.id,
                hasFocus = true
            )
            advanceUntilIdle()
            onSlashTextWatcherEvent(
                SlashEvent.Start(
                    cursorCoordinate = 100,
                    slashStart = 7
                )
            )
            advanceUntilIdle()
            onSlashTextWatcherEvent(
                event = SlashEvent.Filter(
                    filter = "/",
                    viewType = 0
                )
            )
            advanceUntilIdle()
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
            advanceUntilIdle()
        }

        //TESTING

        vm.onSlashItemClicked(SlashItem.Style.Markup.Italic)

        advanceUntilIdle()

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
    fun `should hide slash widget event when clicked on STRIKETHROUGH `() = runTest {

        val doc = MockTypicalDocumentFactory.page(root)
        val block = MockTypicalDocumentFactory.a

        stubInterceptEvents()
        stubUpdateBlocksMark()
        stubUpdateText()
        stubSearchObjects()
        stubOpenDocument(document = doc)
        val vm = buildViewModel()

        vm.onStart(id = root, space = defaultSpace)

        advanceUntilIdle()

        vm.apply {
            onSelectionChanged(
                id = block.id,
                selection = IntRange(0, 0)
            )
            advanceUntilIdle()
            onBlockFocusChanged(
                id = block.id,
                hasFocus = true
            )
            advanceUntilIdle()
            onSlashTextWatcherEvent(
                SlashEvent.Start(
                    cursorCoordinate = 100,
                    slashStart = 0
                )
            )
            advanceUntilIdle()
        }

        // TESTING

        vm.onSlashItemClicked(SlashItem.Style.Markup.Strikethrough)

        advanceUntilIdle()

        val state = vm.controlPanelViewState.value

        assertNotNull(state)
        assertFalse(state.slashWidget.isVisible)
    }

    @Test
    fun `should save selection and focus when clicked on STRIKETHROUGH`() = runTest {
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
            content = Block.Content.Smart,
            children = listOf(header.id, block.id, block2.id, block3.id)
        )

        val doc = listOf(page, header, title, block, block2, block3)

        stubInterceptEvents()
        stubUpdateBlocksMark()
        stubUpdateText()
        stubSearchObjects()
        stubOpenDocument(document = doc)
        val vm = buildViewModel()

        vm.onStart(id = root, space = defaultSpace)

        advanceUntilIdle()

        val selection = IntRange(5, 5)

        vm.apply {
            onSelectionChanged(
                id = block2.id,
                selection = selection
            )
            advanceUntilIdle()
            onBlockFocusChanged(
                id = block2.id,
                hasFocus = true
            )
            advanceUntilIdle()
            onSlashTextWatcherEvent(
                SlashEvent.Start(
                    cursorCoordinate = 100,
                    slashStart = 5
                )
            )
            advanceUntilIdle()
            onSlashTextWatcherEvent(
                event = SlashEvent.Filter(
                    filter = "/",
                    viewType = 0
                )
            )
            advanceUntilIdle()
        }

        //TESTING

        vm.onSlashItemClicked(SlashItem.Style.Markup.Strikethrough)

        advanceUntilIdle()

        val focus = orchestrator.stores.focus.current()
        val cursor = Editor.Cursor.Range(range = selection)

        assertEquals(block2.id, focus.requireTarget())
        assertEquals(cursor, focus.cursor)
    }

    @Test
    fun `should invoke updateBlocksMark when clicked on STRIKETHROUGH`() = runTest {
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
            content = Block.Content.Smart,
            children = listOf(header.id, block.id, block2.id)
        )

        val doc = listOf(page, header, title, block, block2)

        stubInterceptEvents()
        stubUpdateBlocksMark()
        stubUpdateText()
        stubSearchObjects()
        stubOpenDocument(document = doc)
        val vm = buildViewModel()

        vm.onStart(id = root, space = defaultSpace)

        advanceUntilIdle()

        val selection = IntRange(7, 7)

        vm.apply {
            onSelectionChanged(
                id = block.id,
                selection = selection
            )
            advanceUntilIdle()
            onBlockFocusChanged(
                id = block.id,
                hasFocus = true
            )
            advanceUntilIdle()
            onSlashTextWatcherEvent(
                SlashEvent.Start(
                    cursorCoordinate = 100,
                    slashStart = 7
                )
            )
            advanceUntilIdle()
            onSlashTextWatcherEvent(
                event = SlashEvent.Filter(
                    filter = "/",
                    viewType = 0
                )
            )
            advanceUntilIdle()
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
            advanceUntilIdle()
        }

        //TESTING

        vm.onSlashItemClicked(SlashItem.Style.Markup.Strikethrough)

        advanceUntilIdle()

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
    fun `should hide slash widget event when clicked on CODE `() = runTest {

        val doc = MockTypicalDocumentFactory.page(root)
        val block = MockTypicalDocumentFactory.a

        stubInterceptEvents()
        stubUpdateBlocksMark()
        stubUpdateText()
        stubSearchObjects()
        stubOpenDocument(document = doc)
        val vm = buildViewModel()

        vm.onStart(id = root, space = defaultSpace)

        advanceUntilIdle()

        vm.apply {
            onSelectionChanged(
                id = block.id,
                selection = IntRange(0, 0)
            )
            advanceUntilIdle()
            onBlockFocusChanged(
                id = block.id,
                hasFocus = true
            )
            advanceUntilIdle()
            onSlashTextWatcherEvent(
                SlashEvent.Start(
                    cursorCoordinate = 100,
                    slashStart = 0
                )
            )
            advanceUntilIdle()
            onSlashTextWatcherEvent(
                event = SlashEvent.Filter(
                    filter = "/",
                    viewType = 0
                )
            )
            advanceUntilIdle()
        }

        // TESTING

        vm.onSlashItemClicked(SlashItem.Style.Markup.Code)

        advanceUntilIdle()

        val state = vm.controlPanelViewState.value

        assertNotNull(state)
        assertFalse(state.slashWidget.isVisible)
    }

    @Test
    fun `should save selection and focus when clicked on CODE`() = runTest {
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
            content = Block.Content.Smart,
            children = listOf(header.id, block.id, block2.id, block3.id)
        )

        val doc = listOf(page, header, title, block, block2, block3)

        stubInterceptEvents()
        stubUpdateBlocksMark()
        stubUpdateText()
        stubSearchObjects()
        stubOpenDocument(document = doc)
        val vm = buildViewModel()

        vm.onStart(id = root, space = defaultSpace)

        advanceUntilIdle()

        val selection = IntRange(15, 15)

        vm.apply {
            onSelectionChanged(
                id = block3.id,
                selection = selection
            )
            advanceUntilIdle()
            onBlockFocusChanged(
                id = block3.id,
                hasFocus = true
            )
            advanceUntilIdle()
            onSlashTextWatcherEvent(
                SlashEvent.Start(
                    cursorCoordinate = 100,
                    slashStart = 15
                )
            )
            advanceUntilIdle()
            onSlashTextWatcherEvent(
                event = SlashEvent.Filter(
                    filter = "/",
                    viewType = 0
                )
            )
            advanceUntilIdle()
        }

        //TESTING

        vm.onSlashItemClicked(SlashItem.Style.Markup.Code)

        advanceUntilIdle()

        val focus = orchestrator.stores.focus.current()
        val cursor = Editor.Cursor.Range(range = selection)

        assertEquals(block3.id, focus.requireTarget())
        assertEquals(cursor, focus.cursor)
    }

    @Test
    fun `should invoke updateBlocksMark when clicked on CODE`() = runTest {
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
            content = Block.Content.Smart,
            children = listOf(header.id, block.id, block2.id)
        )

        val doc = listOf(page, header, title, block, block2)

        stubInterceptEvents()
        stubUpdateBlocksMark()
        stubUpdateText()
        stubSearchObjects()
        stubOpenDocument(document = doc)
        val vm = buildViewModel()

        vm.onStart(id = root, space = defaultSpace)

        advanceUntilIdle()

        val selection = IntRange(7, 7)

        vm.apply {
            onSelectionChanged(
                id = block.id,
                selection = selection
            )
            advanceUntilIdle()
            onBlockFocusChanged(
                id = block.id,
                hasFocus = true
            )
            advanceUntilIdle()
            onSlashTextWatcherEvent(
                SlashEvent.Start(
                    cursorCoordinate = 100,
                    slashStart = 7
                )
            )
            advanceUntilIdle()
            onSlashTextWatcherEvent(
                event = SlashEvent.Filter(
                    filter = "/",
                    viewType = 0
                )
            )
            advanceUntilIdle()
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
            advanceUntilIdle()
        }

        //TESTING

        vm.onSlashItemClicked(SlashItem.Style.Markup.Code)

        advanceUntilIdle()

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