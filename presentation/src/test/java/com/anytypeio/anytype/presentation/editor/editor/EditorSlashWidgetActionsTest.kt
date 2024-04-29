package com.anytypeio.anytype.presentation.editor.editor

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.domain.block.interactor.DuplicateBlock
import com.anytypeio.anytype.domain.block.interactor.UnlinkBlocks
import com.anytypeio.anytype.domain.block.interactor.UpdateText
import com.anytypeio.anytype.domain.clipboard.Copy
import com.anytypeio.anytype.domain.clipboard.Paste
import com.anytypeio.anytype.presentation.MockTypicalDocumentFactory
import com.anytypeio.anytype.presentation.editor.EditorViewModel.Companion.TEXT_CHANGES_DEBOUNCE_DURATION
import com.anytypeio.anytype.presentation.editor.editor.control.ControlPanelState
import com.anytypeio.anytype.presentation.editor.editor.slash.SlashEvent
import com.anytypeio.anytype.presentation.editor.editor.slash.SlashItem
import com.anytypeio.anytype.presentation.util.DefaultCoroutineTestRule
import com.anytypeio.anytype.test_utils.MockDataFactory
import com.jraska.livedata.test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.times
import org.mockito.kotlin.verifyBlocking
import org.mockito.kotlin.verifyNoMoreInteractions

class EditorSlashWidgetActionsTest : EditorPresentationTestSetup() {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    val coroutineTestRule = DefaultCoroutineTestRule()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        stubSpaceManager()
        stubGetNetworkMode()
        stubFileLimitEvents()
        stubUpdateText()
        stubDuplicateBlock("", emptyList())
        stubCopy()
        stubPaste()
        stubAnalyticSpaceHelperDelegate()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun after() {
        coroutineTestRule.advanceTime(TEXT_CHANGES_DEBOUNCE_DURATION)
    }

    //region {Action DELETE}
    @Test
    fun `should not hide slash widget when action delete happened`() = runTest {

        val doc = MockTypicalDocumentFactory.page(root)
        val block = MockTypicalDocumentFactory.a

        stubInterceptEvents()
        stubUpdateText()
        stubSearchObjects()
        stubOpenDocument(document = doc)
        stubUnlinkBlocks()

        val vm = buildViewModel()

        vm.onStart(id = root, space = defaultSpace)

        advanceUntilIdle()

        vm.apply {
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

        vm.onSlashItemClicked(SlashItem.Actions.Delete)

        advanceUntilIdle()

        val state = vm.controlPanelViewState.value

        assertNotNull(state)
        assertFalse(state.slashWidget.isVisible)
    }

    @Test
    fun `should send unlinkBlocks UseCase when action Delete happened`() = runTest {

        val doc = MockTypicalDocumentFactory.page(root)
        val block = MockTypicalDocumentFactory.a

        stubInterceptEvents()
        stubUpdateText()
        stubSearchObjects()
        stubOpenDocument(document = doc)
        stubUnlinkBlocks()

        val vm = buildViewModel()

        vm.onStart(id = root, space = defaultSpace)

        advanceUntilIdle()

        vm.apply {
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

        vm.onSlashItemClicked(SlashItem.Actions.Delete)

        advanceUntilIdle()

        val params = UnlinkBlocks.Params(
            context = root,
            targets = listOf(block.id)
        )

        stubUnlinkBlocks(params = params)

        verifyBlocking(unlinkBlocks, times(1)) { invoke(params) }
    }

    @Test
    fun `should not triggered unlinkBlocks UseCase when no blocks in focus`() = runTest {

        val doc = MockTypicalDocumentFactory.page(root)
        val block = MockTypicalDocumentFactory.a

        stubInterceptEvents()
        stubOpenDocument(document = doc)
        val vm = buildViewModel()

        vm.onStart(id = root, space = defaultSpace)

        advanceUntilIdle()

        vm.apply {
            onSlashTextWatcherEvent(
                SlashEvent.Start(
                    cursorCoordinate = 100,
                    slashStart = 0
                )
            )
            advanceUntilIdle()
        }

        // TESTING

        vm.onSlashItemClicked(SlashItem.Actions.Delete)

        advanceUntilIdle()

        val params = UnlinkBlocks.Params(
            context = root,
            targets = listOf(block.id)
        )
        verifyBlocking(unlinkBlocks, times(0)) { invoke(params) }
    }
    //endregion

    //region {Action DUPLICATE}
    @Test
    fun `should hide slash widget after action Duplicate`() = runTest {
        val doc = MockTypicalDocumentFactory.page(root)
        val block = MockTypicalDocumentFactory.a

        stubInterceptEvents()
        stubUpdateText()
        stubSearchObjects()
        stubOpenDocument(document = doc)
        val vm = buildViewModel()

        vm.onStart(id = root, space = defaultSpace)

        advanceUntilIdle()

        vm.apply {
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

        vm.onSlashItemClicked(SlashItem.Actions.Duplicate)

        advanceUntilIdle()

        val stateAfter = vm.controlPanelViewState.value

        assertNotNull(stateAfter)
        assertFalse(stateAfter.slashWidget.isVisible)
        assertFalse(stateAfter.navigationToolbar.isVisible)
        assertFalse(stateAfter.mainToolbar.isVisible)

        val params = DuplicateBlock.Params(
            context = root,
            target = block.id,
            blocks = listOf(block.id)
        )
        verifyBlocking(duplicateBlock, times(1)) { invoke(params) }
    }

    @Test
    fun `should invoke duplicateBlock UseCase after action Duplicate`() = runTest {
        val doc = MockTypicalDocumentFactory.page(root)
        val block = MockTypicalDocumentFactory.a

        stubInterceptEvents()
        stubUpdateText()
        stubSearchObjects()
        stubOpenDocument(document = doc)
        val vm = buildViewModel()

        vm.onStart(id = root, space = defaultSpace)

        advanceUntilIdle()

        vm.apply {
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

        vm.onSlashItemClicked(SlashItem.Actions.Duplicate)

        advanceUntilIdle()

        val params = DuplicateBlock.Params(
            context = root,
            target = block.id,
            blocks = listOf(block.id)
        )
        verifyBlocking(duplicateBlock, times(1)) { invoke(params) }
    }
    //endregion

    //region {Action COPY}
    @Test
    fun `should hide slash widget after action Copy`() = runTest {
        val doc = MockTypicalDocumentFactory.page(root)
        val block = MockTypicalDocumentFactory.a

        stubInterceptEvents()
        stubOpenDocument(document = doc)

        val vm = buildViewModel()

        vm.onStart(id = root, space = defaultSpace)

        advanceUntilIdle()

        vm.apply {
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

        vm.onSlashItemClicked(SlashItem.Actions.Copy)

        advanceUntilIdle()

        val state = vm.controlPanelViewState.value

        assertNotNull(state)
        assertFalse(state.slashWidget.isVisible)
    }

    @Test
    fun `should send Copy UseCase with null range after action Copy`() = runTest {
        val doc = MockTypicalDocumentFactory.page(root)
        val block = MockTypicalDocumentFactory.a

        stubInterceptEvents()
        stubUpdateText()
        stubSearchObjects()
        stubOpenDocument(document = doc)
        stubCopy()

        val vm = buildViewModel()

        vm.onStart(id = root, space = defaultSpace)

        advanceUntilIdle()

        vm.apply {
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

        vm.onSlashItemClicked(SlashItem.Actions.Copy)

        advanceUntilIdle()

        val params = Copy.Params(
            context = root,
            range = null,
            blocks = listOf(block)
        )

        verifyBlocking(copy, times(1)) { invoke(params) }
    }
    //endregion

    //region {Action PASTE}
    @Test
    fun `should hide slash widget after action Paste`() = runTest {
        val doc = MockTypicalDocumentFactory.page(root)
        val block = MockTypicalDocumentFactory.a

        stubInterceptEvents()
        stubOpenDocument(document = doc)
        val vm = buildViewModel()

        vm.onStart(id = root, space = defaultSpace)

        advanceUntilIdle()

        vm.apply {
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

        vm.onSlashItemClicked(SlashItem.Actions.Paste)

        advanceUntilIdle()

        val state = vm.controlPanelViewState.value

        assertNotNull(state)
        assertFalse(state.slashWidget.isVisible)
    }

    @Test
    fun `should send Paste UseCase with selection range after action Paste`() = runTest {
        val doc = MockTypicalDocumentFactory.page(root)
        val block = MockTypicalDocumentFactory.a

        stubInterceptEvents()
        stubUpdateText()
        stubSearchObjects()
        stubOpenDocument(document = doc)
        val vm = buildViewModel()

        vm.onStart(id = root, space = defaultSpace)

        advanceUntilIdle()

        vm.apply {
            onBlockFocusChanged(
                id = block.id,
                hasFocus = true
            )
            advanceUntilIdle()
            onSelectionChanged(
                id = block.id,
                selection = IntRange(3, 3)
            )
            advanceUntilIdle()
            onSlashTextWatcherEvent(
                SlashEvent.Start(
                    cursorCoordinate = 100,
                    slashStart = 3
                )
            )
            advanceUntilIdle()
        }

        // TESTING

        vm.onSlashItemClicked(SlashItem.Actions.Paste)
        advanceUntilIdle()


        val focus = vm.focus.value
        assertNotNull(focus)

        val params = Paste.Params(
            context = root,
            range = IntRange(3, 3),
            focus = focus,
            selected = listOf()
        )

        verifyBlocking(paste, times(1)) { invoke(params) }
    }
    //endregion

    //region {Action MOVE}
    @Test
    fun `should hide slash widget after action Move`() = runTest {
        val doc = MockTypicalDocumentFactory.page(root)
        val block = MockTypicalDocumentFactory.a

        stubInterceptEvents()
        stubOpenDocument(document = doc)
        val vm = buildViewModel()

        vm.onStart(id = root, space = defaultSpace)

        advanceUntilIdle()

        vm.apply {
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

        vm.onSlashItemClicked(SlashItem.Actions.Move)

        advanceUntilIdle()

        val state = vm.controlPanelViewState.value

        assertNotNull(state)
        assertFalse(state.slashWidget.isVisible)
    }

    @Test
    fun `should enter mode SAM after action Move`() = runTest {
        val doc = MockTypicalDocumentFactory.page(root)
        val block = MockTypicalDocumentFactory.a

        stubInterceptEvents()
        stubOpenDocument(document = doc)
        val vm = buildViewModel()

        vm.onStart(id = root, space = defaultSpace)

        advanceUntilIdle()

        vm.apply {
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

        vm.onSlashItemClicked(SlashItem.Actions.Move)

        advanceUntilIdle()

        val state = vm.controlPanelViewState.value

        assertNotNull(state)

        val expected = ControlPanelState.Toolbar.MultiSelect(
            isVisible = true,
            isScrollAndMoveEnabled = true,
            isQuickScrollAndMoveMode = true,
            count = 1
        )

        assertEquals(expected, state.multiSelect)
    }
    //endregion

    //region {Action MOVE TO}
    @Test
    fun `should hide slash widget and navigate to move to screen after move to action`() = runTest {
        val doc = MockTypicalDocumentFactory.page(root)
        val block = MockTypicalDocumentFactory.a

        stubInterceptEvents()
        stubOpenDocument(document = doc)
        val vm = buildViewModel()

        vm.onStart(id = root, space = defaultSpace)

        advanceUntilIdle()

        vm.apply {
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

        vm.onSlashItemClicked(SlashItem.Actions.MoveTo)

        advanceUntilIdle()

        val state = vm.controlPanelViewState.value

        assertNotNull(state)
        assertFalse(state.slashWidget.isVisible)

        vm.commands.test()
            .assertHasValue()
            .assertValue { result ->
                val command = result.peekContent()
                command is Command.OpenMoveToScreen
                        && command.blocks.first() == block.id
                        && command.ctx == root
            }
    }
    //endregion

    //region {Action CLEAN STYLE}
    @Test
    fun `should hide slash widget after action Clean Style`() = runTest {
        val doc = MockTypicalDocumentFactory.page(root)
        val block = MockTypicalDocumentFactory.a

        stubInterceptEvents()
        stubOpenDocument(document = doc)
        val vm = buildViewModel()

        vm.onStart(id = root, space = defaultSpace)

        advanceUntilIdle()

        vm.apply {
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

        vm.onSlashItemClicked(SlashItem.Actions.CleanStyle)

        advanceUntilIdle()

        val state = vm.controlPanelViewState.value

        assertNotNull(state)
        assertFalse(state.slashWidget.isVisible)
    }

    @Test
    fun `should not send UpdateText UseCase after action Clean Style`() = runTest {

        val header = MockTypicalDocumentFactory.header
        val title = MockTypicalDocumentFactory.title

        val block = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = listOf(
                    Block.Content.Text.Mark(
                        range = IntRange(0, 5),
                        type = Block.Content.Text.Mark.Type.BOLD
                    ),
                    Block.Content.Text.Mark(
                        range = IntRange(3, 10),
                        type = Block.Content.Text.Mark.Type.ITALIC
                    )
                ),
                style = Block.Content.Text.Style.NUMBERED
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart,
            children = listOf(header.id, block.id)
        )

        val doc = listOf(
            page,
            header,
            title,
            block
        )

        stubInterceptEvents()
        stubOpenDocument(document = doc)
        stubUpdateText()
        stubSearchObjects()
        val vm = buildViewModel()

        vm.onStart(id = root, space = defaultSpace)

        advanceUntilIdle()

        vm.apply {
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

        vm.onSlashItemClicked(SlashItem.Actions.CleanStyle)

        advanceUntilIdle()

        val params = UpdateText.Params(
            context = root,
            target = block.id,
            text = block.content.asText().text,
            marks = block.content.asText().marks
        )

        verifyBlocking(updateText, times(1)) { invoke(params) }
        verifyNoMoreInteractions(updateText)
    }
    //endregion
}