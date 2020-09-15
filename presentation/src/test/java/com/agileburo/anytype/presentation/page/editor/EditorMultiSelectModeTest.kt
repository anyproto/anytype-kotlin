package com.agileburo.anytype.presentation.page.editor

import MockDataFactory
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.agileburo.anytype.core_ui.features.page.BlockView
import com.agileburo.anytype.core_ui.model.UiBlock
import com.agileburo.anytype.core_ui.state.ControlPanelState
import com.agileburo.anytype.domain.block.interactor.UpdateTextStyle
import com.agileburo.anytype.domain.block.model.Block
import com.agileburo.anytype.domain.event.model.Event
import com.agileburo.anytype.domain.ext.content
import com.agileburo.anytype.presentation.page.PageViewModel.Companion.DELAY_REFRESH_DOCUMENT_TO_ENTER_MULTI_SELECT_MODE
import com.agileburo.anytype.presentation.page.PageViewModel.Companion.TEXT_CHANGES_DEBOUNCE_DURATION
import com.agileburo.anytype.presentation.util.CoroutinesTestRule
import com.jraska.livedata.test
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations

class EditorMultiSelectModeTest : EditorPresentationTestSetup() {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun `should clear selection after turn-into in multi-select mode`() {

        // SETUP

        val a = Block(
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
            content = Block.Content.Smart(
                type = Block.Content.Smart.Type.PAGE
            ),
            children = listOf(a.id)
        )

        val document = listOf(page, a)

        stubOpenDocument(document)
        stubInterceptEvents()
        stubUpdateTextStyle(
            params = UpdateTextStyle.Params(
                style = Block.Content.Text.Style.QUOTE,
                context = root,
                targets = listOf(a.id)
            ),
            events = listOf(
                Event.Command.GranularChange(
                    context = root,
                    id = a.id,
                    style = Block.Content.Text.Style.QUOTE
                )
            )
        )

        val vm = buildViewModel()

        vm.onStart(root)

        // TESTING

        // Try entering multi-select mode

        vm.apply {
            onBlockFocusChanged(id = a.id, hasFocus = true)
            onEnterMultiSelectModeClicked()
        }

        // Checking control panel entered multi-select mode

        vm.controlPanelViewState.test().apply {
            assertValue(
                ControlPanelState(
                    navigationToolbar = ControlPanelState.Toolbar.Navigation(
                        isVisible = false
                    ),
                    mainToolbar = ControlPanelState.Toolbar.Main(
                        isVisible = false
                    ),
                    stylingToolbar = ControlPanelState.Toolbar.Styling(
                        isVisible = false,
                        mode = null
                    ),
                    multiSelect = ControlPanelState.Toolbar.MultiSelect(
                        isVisible = true,
                        isScrollAndMoveEnabled = false,
                        count = 0
                    ),
                    mentionToolbar = ControlPanelState.Toolbar.MentionToolbar(
                        isVisible = false,
                        cursorCoordinate = null,
                        mentionFilter = null,
                        mentionFrom = null
                    )
                )
            )
        }

        // Checking editor entered multi-select mode

        coroutineTestRule.advanceTime(DELAY_REFRESH_DOCUMENT_TO_ENTER_MULTI_SELECT_MODE)

        vm.state.test().apply {
            assertValue(
                ViewState.Success(
                    blocks = listOf(
                        BlockView.Title.Document(
                            id = root,
                            isFocused = false,
                            text = null,
                            mode = BlockView.Mode.READ
                        ),
                        BlockView.Text.Numbered(
                            id = a.id,
                            isSelected = false,
                            isFocused = true,
                            marks = emptyList(),
                            backgroundColor = null,
                            indent = 0,
                            number = 1,
                            text = a.content<Block.Content.Text>().text,
                            mode = BlockView.Mode.READ
                        )
                    )
                )
            )
        }

        // Perform click, to select block A

        vm.onTextInputClicked(
            target = a.id
        )

        // Checking whether block A is selected

        vm.state.test().apply {
            assertValue(
                ViewState.Success(
                    blocks = listOf(
                        BlockView.Title.Document(
                            id = root,
                            isFocused = false,
                            text = null,
                            mode = BlockView.Mode.READ
                        ),
                        BlockView.Text.Numbered(
                            id = a.id,
                            isSelected = true,
                            isFocused = true,
                            marks = emptyList(),
                            backgroundColor = null,
                            indent = 0,
                            number = 1,
                            text = a.content<Block.Content.Text>().text,
                            mode = BlockView.Mode.READ
                        )
                    )
                )
            )
        }

        // Turning block A into a highlight block.

        vm.onTurnIntoMultiSelectBlockClicked(
            UiBlock.HIGHLIGHTED
        )

        // Checking control panel state after turn-into

        vm.controlPanelViewState.test().apply {
            assertValue(
                ControlPanelState(
                    navigationToolbar = ControlPanelState.Toolbar.Navigation(
                        isVisible = false
                    ),
                    mainToolbar = ControlPanelState.Toolbar.Main(
                        isVisible = false
                    ),
                    stylingToolbar = ControlPanelState.Toolbar.Styling(
                        isVisible = false,
                        mode = null
                    ),
                    multiSelect = ControlPanelState.Toolbar.MultiSelect(
                        isVisible = true,
                        isScrollAndMoveEnabled = false,
                        count = 0
                    ),
                    mentionToolbar = ControlPanelState.Toolbar.MentionToolbar(
                        isVisible = false,
                        cursorCoordinate = null,
                        mentionFilter = null,
                        mentionFrom = null
                    )
                )
            )
        }

        // Checking view state state after turn-into

        vm.state.test().apply {
            assertValue(
                ViewState.Success(
                    blocks = listOf(
                        BlockView.Title.Document(
                            id = root,
                            isFocused = false,
                            text = null,
                            mode = BlockView.Mode.READ
                        ),
                        BlockView.Text.Highlight(
                            id = a.id,
                            isSelected = false,
                            isFocused = true,
                            marks = emptyList(),
                            backgroundColor = null,
                            color = null,
                            indent = 0,
                            text = a.content<Block.Content.Text>().text,
                            mode = BlockView.Mode.READ
                        )
                    )
                )
            )
        }

        clearPendingCoroutines()
    }

    @Test
    fun `should show main toolbar when block view holder returning to edit mode gains focus after turn-into in multi-select-mode`() {

        // SETUP

        val a = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(
                type = Block.Content.Smart.Type.PAGE
            ),
            children = listOf(a.id)
        )

        val document = listOf(page, a)

        stubOpenDocument(document)
        stubInterceptEvents()
        stubUpdateTextStyle(
            params = UpdateTextStyle.Params(
                style = Block.Content.Text.Style.QUOTE,
                context = root,
                targets = listOf(a.id)
            ),
            events = listOf(
                Event.Command.GranularChange(
                    context = root,
                    id = a.id,
                    style = Block.Content.Text.Style.QUOTE
                )
            )
        )

        val vm = buildViewModel()

        // TESTING

        vm.onStart(root)

        // Try entering multi-select mode

        vm.apply {
            onBlockFocusChanged(id = a.id, hasFocus = true)
            onEnterMultiSelectModeClicked()
        }

        // Checking control panel entered multi-select mode

        vm.controlPanelViewState.test().apply {
            assertValue(
                ControlPanelState(
                    navigationToolbar = ControlPanelState.Toolbar.Navigation(
                        isVisible = false
                    ),
                    mainToolbar = ControlPanelState.Toolbar.Main(
                        isVisible = false
                    ),
                    stylingToolbar = ControlPanelState.Toolbar.Styling(
                        isVisible = false,
                        mode = null
                    ),
                    multiSelect = ControlPanelState.Toolbar.MultiSelect(
                        isVisible = true,
                        isScrollAndMoveEnabled = false,
                        count = 0
                    ),
                    mentionToolbar = ControlPanelState.Toolbar.MentionToolbar(
                        isVisible = false,
                        cursorCoordinate = null,
                        mentionFilter = null,
                        mentionFrom = null
                    )
                )
            )
        }

        coroutineTestRule.advanceTime(DELAY_REFRESH_DOCUMENT_TO_ENTER_MULTI_SELECT_MODE)

        // Perform click, to select block A

        vm.onTextInputClicked(
            target = a.id
        )

        // Turning block A into a highlight block.

        vm.onTurnIntoMultiSelectBlockClicked(
            UiBlock.HIGHLIGHTED
        )

        vm.onExitMultiSelectModeClicked()

        vm.onTextInputClicked(
            target = a.id
        )

        vm.onBlockFocusChanged(
            id = a.id,
            hasFocus = true
        )

        vm.controlPanelViewState.test().apply {
            assertValue(
                ControlPanelState(
                    navigationToolbar = ControlPanelState.Toolbar.Navigation(
                        isVisible = false
                    ),
                    mainToolbar = ControlPanelState.Toolbar.Main(
                        isVisible = true
                    ),
                    stylingToolbar = ControlPanelState.Toolbar.Styling.reset(),
                    multiSelect = ControlPanelState.Toolbar.MultiSelect(
                        isVisible = false,
                        isScrollAndMoveEnabled = false,
                        count = 0
                    ),
                    mentionToolbar = ControlPanelState.Toolbar.MentionToolbar(
                        isVisible = false,
                        cursorCoordinate = null,
                        mentionFilter = null,
                        mentionFrom = null
                    )
                )
            )
        }

        clearPendingCoroutines()
    }

    @Test
    fun `should select all children when selecting parent and unselect children when unselecting parent`() {

        // SETUP

        val child1 = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            )
        )

        val child2 = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            )
        )

        val parent = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = listOf(child1.id, child2.id),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(
                type = Block.Content.Smart.Type.PAGE
            ),
            children = listOf(parent.id)
        )

        val document = listOf(page, parent, child1, child2)

        stubOpenDocument(document)
        stubInterceptEvents()

        val vm = buildViewModel()

        vm.onStart(root)

        // TESTING

        // Try entering multi-select mode

        vm.apply {
            onBlockFocusChanged(id = parent.id, hasFocus = true)
            onEnterMultiSelectModeClicked()
        }

        // Checking editor entered multi-select mode

        coroutineTestRule.advanceTime(DELAY_REFRESH_DOCUMENT_TO_ENTER_MULTI_SELECT_MODE)

        val title = BlockView.Title.Document(
            id = root,
            isFocused = false,
            text = null,
            mode = BlockView.Mode.READ
        )

        val parentView = BlockView.Text.Paragraph(
            id = parent.id,
            isSelected = false,
            isFocused = true,
            marks = emptyList(),
            backgroundColor = null,
            indent = 0,
            text = parent.content<Block.Content.Text>().text,
            mode = BlockView.Mode.READ
        )

        val child1View = BlockView.Text.Paragraph(
            id = child1.id,
            isSelected = false,
            isFocused = false,
            marks = emptyList(),
            backgroundColor = null,
            indent = 1,
            text = child1.content<Block.Content.Text>().text,
            mode = BlockView.Mode.READ
        )

        val child2View = BlockView.Text.Paragraph(
            id = child2.id,
            isSelected = false,
            isFocused = false,
            marks = emptyList(),
            backgroundColor = null,
            indent = 1,
            text = child2.content<Block.Content.Text>().text,
            mode = BlockView.Mode.READ
        )

        vm.state.test().apply {
            assertValue(
                ViewState.Success(
                    blocks = listOf(
                        title,
                        parentView,
                        child1View,
                        child2View
                    )
                )
            )
        }

        // Perform click, to select parent

        vm.onTextInputClicked(
            target = parent.id
        )

        // Checking whether parent and its children are selected

        vm.state.test().apply {
            assertValue(
                ViewState.Success(
                    blocks = listOf(
                        title,
                        parentView.copy(isSelected = true),
                        child1View.copy(isSelected = true),
                        child2View.copy(isSelected = true)
                    )
                )
            )
        }

        // Perform click, to unselect parent

        vm.onTextInputClicked(
            target = parent.id
        )

        // Checking whether parent and its children are not selected

        vm.state.test().apply {
            assertValue(
                ViewState.Success(
                    blocks = listOf(
                        title,
                        parentView.copy(isSelected = false),
                        child1View.copy(isSelected = false),
                        child2View.copy(isSelected = false)
                    )
                )
            )
        }

        clearPendingCoroutines()
    }

    @Test
    fun `if parent is selected, it is not possible to unselect its child`() {

        // SETUP

        val child1 = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            )
        )

        val child2 = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            )
        )

        val parent = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = listOf(child1.id, child2.id),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(
                type = Block.Content.Smart.Type.PAGE
            ),
            children = listOf(parent.id)
        )

        val document = listOf(page, parent, child1, child2)

        stubOpenDocument(document)
        stubInterceptEvents()

        val vm = buildViewModel()

        vm.onStart(root)

        // TESTING

        // Try entering multi-select mode

        vm.apply {
            onBlockFocusChanged(id = parent.id, hasFocus = true)
            onEnterMultiSelectModeClicked()
        }

        // Checking editor entered multi-select mode

        coroutineTestRule.advanceTime(DELAY_REFRESH_DOCUMENT_TO_ENTER_MULTI_SELECT_MODE)

        val title = BlockView.Title.Document(
            id = root,
            isFocused = false,
            text = null,
            mode = BlockView.Mode.READ
        )

        val parentView = BlockView.Text.Paragraph(
            id = parent.id,
            isSelected = false,
            isFocused = true,
            marks = emptyList(),
            backgroundColor = null,
            indent = 0,
            text = parent.content<Block.Content.Text>().text,
            mode = BlockView.Mode.READ
        )

        val child1View = BlockView.Text.Paragraph(
            id = child1.id,
            isSelected = false,
            isFocused = false,
            marks = emptyList(),
            backgroundColor = null,
            indent = 1,
            text = child1.content<Block.Content.Text>().text,
            mode = BlockView.Mode.READ
        )

        val child2View = BlockView.Text.Paragraph(
            id = child2.id,
            isSelected = false,
            isFocused = false,
            marks = emptyList(),
            backgroundColor = null,
            indent = 1,
            text = child2.content<Block.Content.Text>().text,
            mode = BlockView.Mode.READ
        )

        vm.state.test().apply {
            assertValue(
                ViewState.Success(
                    blocks = listOf(
                        title,
                        parentView,
                        child1View,
                        child2View
                    )
                )
            )
        }

        // Perform click, to select parent

        vm.onTextInputClicked(
            target = parent.id
        )

        // Checking whether parent and its children are selected

        vm.state.test().apply {
            assertValue(
                ViewState.Success(
                    blocks = listOf(
                        title,
                        parentView.copy(isSelected = true),
                        child1View.copy(isSelected = true),
                        child2View.copy(isSelected = true)
                    )
                )
            )
        }

        // Perform click, to unselect parent

        vm.onTextInputClicked(
            target = child1.id
        )

        // Checking whether parent and its children are not selected

        vm.state.test().apply {
            assertValue(
                ViewState.Success(
                    blocks = listOf(
                        title,
                        parentView.copy(isSelected = true),
                        child1View.copy(isSelected = true),
                        child2View.copy(isSelected = true)
                    )
                )
            )
        }

        clearPendingCoroutines()
    }

    private fun clearPendingCoroutines() {
        coroutineTestRule.advanceTime(TEXT_CHANGES_DEBOUNCE_DURATION)
    }
}