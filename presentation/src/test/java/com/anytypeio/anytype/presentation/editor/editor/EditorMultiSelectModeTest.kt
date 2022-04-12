package com.anytypeio.anytype.presentation.editor.editor

import MockDataFactory
import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.ext.content
import com.anytypeio.anytype.domain.block.interactor.TurnIntoStyle
import com.anytypeio.anytype.domain.block.interactor.UnlinkBlocks
import com.anytypeio.anytype.domain.block.interactor.UpdateTextStyle
import com.anytypeio.anytype.presentation.MockBlockFactory
import com.anytypeio.anytype.presentation.editor.EditorViewModel
import com.anytypeio.anytype.presentation.editor.EditorViewModel.Companion.DELAY_REFRESH_DOCUMENT_TO_ENTER_MULTI_SELECT_MODE
import com.anytypeio.anytype.presentation.editor.EditorViewModel.Companion.TEXT_CHANGES_DEBOUNCE_DURATION
import com.anytypeio.anytype.presentation.editor.editor.actions.ActionItemType
import com.anytypeio.anytype.presentation.editor.editor.control.ControlPanelState
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.editor.model.UiBlock
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import com.anytypeio.anytype.presentation.util.TXT
import com.jraska.livedata.test
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.times
import org.mockito.kotlin.verifyBlocking
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals

@Config(sdk = [Build.VERSION_CODES.P])
@RunWith(RobolectricTestRunner::class)
class EditorMultiSelectModeTest : EditorPresentationTestSetup() {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

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

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun `should clear selection after turn-into in multi-select mode`() {

        // SETUP

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
            content = Block.Content.Smart(),
            children = listOf(header.id, a.id)
        )

        val document = listOf(page, header, title, a)

        stubOpenDocument(document)
        stubInterceptEvents()
        stubTurnIntoStyle(
            params = TurnIntoStyle.Params(
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
                    styleTextToolbar = ControlPanelState.Toolbar.Styling(
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
                    ),
                    slashWidget = ControlPanelState.Toolbar.SlashWidget.reset()
                )
            )
        }

        // Checking editor entered multi-select mode

        coroutineTestRule.advanceTime(DELAY_REFRESH_DOCUMENT_TO_ENTER_MULTI_SELECT_MODE)

        vm.state.test().apply {
            assertValue(
                ViewState.Success(
                    blocks = listOf(
                        BlockView.Title.Basic(
                            id = title.id,
                            isFocused = false,
                            text = title.content<TXT>().text,
                            mode = BlockView.Mode.READ
                        ),
                        BlockView.Text.Numbered(
                            id = a.id,
                            isSelected = false,
                            isFocused = false,
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
                        BlockView.Title.Basic(
                            id = title.id,
                            isFocused = false,
                            text = title.content<TXT>().text,
                            mode = BlockView.Mode.READ
                        ),
                        BlockView.Text.Numbered(
                            id = a.id,
                            isSelected = true,
                            isFocused = false,
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
                    styleTextToolbar = ControlPanelState.Toolbar.Styling(
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
                    ),
                    slashWidget = ControlPanelState.Toolbar.SlashWidget.reset()
                )
            )
        }

        // Checking view state state after turn-into

        vm.state.test().apply {
            assertValue(
                ViewState.Success(
                    blocks = listOf(
                        BlockView.Title.Basic(
                            id = title.id,
                            isFocused = false,
                            text = title.content<TXT>().text,
                            mode = BlockView.Mode.READ
                        ),
                        BlockView.Text.Highlight(
                            id = a.id,
                            isSelected = false,
                            isFocused = false,
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
            content = Block.Content.Smart(),
            children = listOf(header.id, a.id)
        )

        val document = listOf(page, header, title, a)

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
                    styleTextToolbar = ControlPanelState.Toolbar.Styling(
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
                    ),
                    slashWidget = ControlPanelState.Toolbar.SlashWidget.reset()
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
                    styleTextToolbar = ControlPanelState.Toolbar.Styling.reset(),
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
                    ),
                    slashWidget = ControlPanelState.Toolbar.SlashWidget.reset()
                )
            )
        }

        clearPendingCoroutines()
    }

    @Test
    fun `should select all children when selecting parent and unselect children when unselecting parent and exit multi-select mode`() {

        // SETUP

        val ttl = Block(
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
            children = listOf(ttl.id)
        )

        val grandchild1 = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            )
        )

        val grandchild2 = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            )
        )

        val child1 = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = listOf(grandchild1.id),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            )
        )

        val child2 = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = listOf(grandchild2.id),
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
            content = Block.Content.Smart(),
            children = listOf(header.id, parent.id)
        )

        val document = listOf(page, header, ttl, parent, child1, child2, grandchild1, grandchild2)

        stubOpenDocument(document)
        stubInterceptEvents()
        stubInterceptThreadStatus()

        val vm = buildViewModel()

        vm.onStart(root)

        // TESTING

        // Try entering multi-select mode

        vm.apply {
            onClickListener(
                clicked = ListenerType.LongClick(
                    target = parent.id,
                    dimensions = BlockDimensions(0, 0, 0, 0, 0, 0)
                )
            )
            onEnterMultiSelectModeClicked()
        }

        // Checking editor entered multi-select mode

        coroutineTestRule.advanceTime(DELAY_REFRESH_DOCUMENT_TO_ENTER_MULTI_SELECT_MODE)

        val title = BlockView.Title.Basic(
            id = ttl.id,
            isFocused = false,
            text = ttl.content<TXT>().text,
            mode = BlockView.Mode.READ
        )

        val parentView = BlockView.Text.Paragraph(
            id = parent.id,
            isSelected = true,
            isFocused = false,
            marks = emptyList(),
            backgroundColor = null,
            indent = 0,
            text = parent.content<Block.Content.Text>().text,
            mode = BlockView.Mode.READ
        )

        val child1View = BlockView.Text.Paragraph(
            id = child1.id,
            isSelected = true,
            isFocused = false,
            marks = emptyList(),
            backgroundColor = null,
            indent = 1,
            text = child1.content<Block.Content.Text>().text,
            mode = BlockView.Mode.READ
        )

        val child2View = BlockView.Text.Paragraph(
            id = child2.id,
            isSelected = true,
            isFocused = false,
            marks = emptyList(),
            backgroundColor = null,
            indent = 1,
            text = child2.content<Block.Content.Text>().text,
            mode = BlockView.Mode.READ
        )

        val grandchild1View = BlockView.Text.Paragraph(
            id = grandchild1.id,
            isSelected = true,
            isFocused = false,
            marks = emptyList(),
            backgroundColor = null,
            indent = 2,
            text = grandchild1.content<Block.Content.Text>().text,
            mode = BlockView.Mode.READ
        )

        val grandchild2View = BlockView.Text.Paragraph(
            id = grandchild2.id,
            isSelected = true,
            isFocused = false,
            marks = emptyList(),
            backgroundColor = null,
            indent = 2,
            text = grandchild2.content<Block.Content.Text>().text,
            mode = BlockView.Mode.READ
        )

        vm.state.test().apply {
            assertValue(
                ViewState.Success(
                    blocks = listOf(
                        title,
                        parentView,
                        child1View,
                        grandchild1View,
                        child2View,
                        grandchild2View
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
                        parentView,
                        child1View,
                        grandchild1View,
                        child2View,
                        grandchild2View
                    )
                )
            )
        }

        // Perform click, to unselect parent

        vm.onTextInputClicked(
            target = parent.id
        )

        coroutineTestRule.advanceTime(EditorViewModel.DELAY_REFRESH_DOCUMENT_ON_EXIT_MULTI_SELECT_MODE)

        // Checking whether parent and its children are not selected

        vm.state.test().apply {
            assertValue(
                ViewState.Success(
                    blocks = listOf(
                        title.copy(mode = BlockView.Mode.EDIT),
                        parentView.copy(isSelected = false, mode = BlockView.Mode.EDIT),
                        child1View.copy(isSelected = false, mode = BlockView.Mode.EDIT),
                        grandchild1View.copy(isSelected = false, mode = BlockView.Mode.EDIT),
                        child2View.copy(isSelected = false, mode = BlockView.Mode.EDIT),
                        grandchild2View.copy(isSelected = false, mode = BlockView.Mode.EDIT)
                    )
                )
            )
        }

        clearPendingCoroutines()
    }

    @Test
    fun `if parent is selected, it is not possible to unselect its child`() {

        // SETUP

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
            content = Block.Content.Smart(),
            children = listOf(header.id, parent.id)
        )

        val document = listOf(page, header, title, parent, child1, child2)

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

        val titleView = BlockView.Title.Basic(
            id = title.id,
            isFocused = false,
            text = title.content<TXT>().text,
            mode = BlockView.Mode.READ
        )

        val parentView = BlockView.Text.Paragraph(
            id = parent.id,
            isSelected = false,
            isFocused = false,
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
                        titleView,
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
                        titleView,
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
                        titleView,
                        parentView.copy(isSelected = true),
                        child1View.copy(isSelected = true),
                        child2View.copy(isSelected = true)
                    )
                )
            )
        }

        clearPendingCoroutines()
    }

    @Test
    fun `should request only parent's deletion, when all its descendants are selected`() {

        // SETUP

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

        val grandchild1 = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            )
        )

        val grandchild2 = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            )
        )

        val child1 = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = listOf(grandchild1.id),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            )
        )

        val child2 = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = listOf(grandchild2.id),
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
            content = Block.Content.Smart(),
            children = listOf(header.id, parent.id)
        )

        val document = listOf(page, header, title, parent, child1, child2, grandchild1, grandchild2)

        stubOpenDocument(document)
        stubInterceptEvents()

        val unlinkParams = UnlinkBlocks.Params(
            context = root,
            targets = listOf(parent.id)
        )

        stubUnlinkBlocks(params = unlinkParams)

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

        // Perform click, to select parent

        vm.onTextInputClicked(
            target = parent.id
        )

        vm.onMultiSelectModeDeleteClicked()

        verifyBlocking(unlinkBlocks, times(1)) { invoke(unlinkParams) }

        clearPendingCoroutines()
    }

    @Test
    fun `should exit to editor mode on system back button event`() {

        // SETUP

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

        val focus = listOf(a.id, title.id).random()

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(header.id, a.id)
        )

        val document = listOf(page, header, title, a)

        stubOpenDocument(document)
        stubInterceptEvents()

        val vm = buildViewModel()

        vm.onStart(root)

        // TESTING

        vm.apply {
            onBlockFocusChanged(id = focus, hasFocus = true)
            onEnterMultiSelectModeClicked()
            onSystemBackPressed(false)
        }

        coroutineTestRule.advanceTime(DELAY_REFRESH_DOCUMENT_TO_ENTER_MULTI_SELECT_MODE)

        vm.controlPanelViewState.test().assertValue { state ->
            !state.multiSelect.isVisible
                    && !state.multiSelect.isQuickScrollAndMoveMode
                    && !state.multiSelect.isScrollAndMoveEnabled
                    && state.multiSelect.count == 0
        }

        coroutineTestRule.advanceTime(EditorViewModel.DELAY_REFRESH_DOCUMENT_ON_EXIT_MULTI_SELECT_MODE + 100)

        vm.state.test().assertValue(
            ViewState.Success(
                listOf(
                    BlockView.Title.Basic(
                        id = title.id,
                        text = title.content<TXT>().text,
                        mode = BlockView.Mode.EDIT,
                    ),
                    BlockView.Text.Paragraph(
                        id = a.id,
                        text = a.content<TXT>().text,
                        mode = BlockView.Mode.EDIT,
                        isSelected = false
                    )
                )
            )
        )

        clearPendingCoroutines()
    }

    @Test
    fun `should exit multi-select mode when selecting one block and then unselecting it`() {

        // SETUP

        val title = MockBlockFactory.title()
        val header = MockBlockFactory.header(children = listOf(title.id))
        val a = MockBlockFactory.paragraph()
        val b = MockBlockFactory.paragraph()
        val c = MockBlockFactory.paragraph()

        val smart = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(header.id, a.id, b.id, c.id)
        )

        val document = listOf(smart, header, title, a, b, c)

        stubOpenDocument(document = document)
        stubInterceptEvents()
        stubInterceptThreadStatus()

        val vm = buildViewModel()

        // TESTING

        vm.onStart(root)

        // Simulating long tap on "b" block, in order to enter multi-select mode.

        vm.onClickListener(
            ListenerType.LongClick(
                target = b.id,
                dimensions = BlockDimensions()
            )
        )

        // "a" block must now be selected.

        vm.controlPanelViewState.test().assertValue(
            ControlPanelState(
                mainToolbar = ControlPanelState.Toolbar.Main(),
                navigationToolbar = ControlPanelState.Toolbar.Navigation(isVisible = false),
                mentionToolbar = ControlPanelState.Toolbar.MentionToolbar.reset(),
                slashWidget = ControlPanelState.Toolbar.SlashWidget.reset(),
                styleTextToolbar = ControlPanelState.Toolbar.Styling.reset(),
                multiSelect = ControlPanelState.Toolbar.MultiSelect(
                    isVisible = true,
                    count = 1
                )
            )
        )

        // Tapping on "a" block, in order to unselect it and exit multi-select mode

        vm.onTextInputClicked(target = b.id)

        vm.controlPanelViewState.test().assertValue(
            ControlPanelState(
                mainToolbar = ControlPanelState.Toolbar.Main(),
                navigationToolbar = ControlPanelState.Toolbar.Navigation(isVisible = true),
                mentionToolbar = ControlPanelState.Toolbar.MentionToolbar.reset(),
                slashWidget = ControlPanelState.Toolbar.SlashWidget.reset(),
                styleTextToolbar = ControlPanelState.Toolbar.Styling.reset(),
                multiSelect = ControlPanelState.Toolbar.MultiSelect(
                    isVisible = false,
                    count = 0
                )
            )
        )

        clearPendingCoroutines()
    }

    @Test
    fun `should process a long click in multi-select mode the same way as a simple click`() {
        // SETUP

        val title = MockBlockFactory.title()
        val header = MockBlockFactory.header(children = listOf(title.id))
        val a = MockBlockFactory.paragraph()
        val b = MockBlockFactory.paragraph()
        val c = MockBlockFactory.paragraph()

        val smart = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(header.id, a.id, b.id, c.id)
        )

        val document = listOf(smart, header, title, a, b, c)

        stubOpenDocument(document = document)
        stubInterceptEvents()
        stubInterceptThreadStatus()

        val vm = buildViewModel()

        // TESTING

        vm.onStart(root)

        // Selecting blocks "a", "b" and "c"

        vm.onClickListener(
            ListenerType.LongClick(
                target = b.id,
                dimensions = BlockDimensions()
            )
        )

        vm.onClickListener(
            ListenerType.LongClick(
                target = c.id,
                dimensions = BlockDimensions()
            )
        )

        vm.onClickListener(
            ListenerType.LongClick(
                target = a.id,
                dimensions = BlockDimensions()
            )
        )

        // Checking that all blocks are selected by now

        vm.controlPanelViewState.test().assertValue(
            ControlPanelState(
                mainToolbar = ControlPanelState.Toolbar.Main(),
                navigationToolbar = ControlPanelState.Toolbar.Navigation(isVisible = false),
                mentionToolbar = ControlPanelState.Toolbar.MentionToolbar.reset(),
                slashWidget = ControlPanelState.Toolbar.SlashWidget.reset(),
                styleTextToolbar = ControlPanelState.Toolbar.Styling.reset(),
                multiSelect = ControlPanelState.Toolbar.MultiSelect(
                    isVisible = true,
                    count = 3
                )
            )
        )

        vm.state.test().assertValue(
            ViewState.Success(
                listOf(
                    BlockView.Title.Basic(
                        id = title.id,
                        text = title.content<TXT>().text,
                        mode = BlockView.Mode.READ,
                    ),
                    BlockView.Text.Paragraph(
                        id = a.id,
                        text = a.content<TXT>().text,
                        mode = BlockView.Mode.READ,
                        isSelected = true
                    ),
                    BlockView.Text.Paragraph(
                        id = b.id,
                        text = b.content<TXT>().text,
                        mode = BlockView.Mode.READ,
                        isSelected = true
                    ),
                    BlockView.Text.Paragraph(
                        id = c.id,
                        text = c.content<TXT>().text,
                        mode = BlockView.Mode.READ,
                        isSelected = true
                    )
                )
            )
        )

        clearPendingCoroutines()
    }

    @Test
    fun `should start main style toolbar when all blocks are texted`() {
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

        val b = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            )
        )

        val c = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.H2
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(a.id, b.id, c.id)
        )

        val document = listOf(page, a, b, c)

        stubOpenDocument(document)
        stubInterceptEvents()

        val vm = buildViewModel()

        vm.onStart(root)

        vm.apply {
            onClickListener(ListenerType.LongClick(target = a.id))
            onTextInputClicked(target = b.id)
            onTextInputClicked(target = c.id)
            onMultiSelectAction(ActionItemType.Style)
        }

        val expectedState = ControlPanelState(
            styleTextToolbar = ControlPanelState.Toolbar.Styling(
                isVisible = true,
                mode = null,
                style = null
            ),
            multiSelect = ControlPanelState.Toolbar.MultiSelect(
                isVisible = true,
                isScrollAndMoveEnabled = false,
                count = 3
            )
        )

        vm.controlPanelViewState.test().apply {
            assertEquals(expected = expectedState, actual = this.value())
        }

        clearPendingCoroutines()
    }

    @Test
    fun `should start background style toolbar with null color when all blocks are not texted`() {
        val targetA = MockDataFactory.randomUuid()
        val backgroundA = "red"
        val a = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Link(
                target = targetA,
                type = Block.Content.Link.Type.PAGE,
                fields = Block.Fields.empty()
            ),
            backgroundColor = backgroundA
        )

        val targetB = MockDataFactory.randomUuid()
        val backgroundB = "teal"
        val b = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Link(
                target = targetB,
                type = Block.Content.Link.Type.PAGE,
                fields = Block.Fields.empty()
            ),
            backgroundColor = backgroundB
        )

        val backgroundC = null
        val c = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.File(
                state = Block.Content.File.State.EMPTY
            ),
            backgroundColor = backgroundC
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(a.id, b.id, c.id)
        )

        val document = listOf(page, a, b, c)

        stubOpenDocument(document)
        stubInterceptEvents()

        val vm = buildViewModel()

        vm.onStart(root)

        vm.apply {
            onClickListener(ListenerType.LongClick(target = a.id))
            onTextInputClicked(target = b.id)
            onTextInputClicked(target = c.id)
            onMultiSelectAction(ActionItemType.Style)
        }

        val expectedState = ControlPanelState(
            multiSelect = ControlPanelState.Toolbar.MultiSelect(
                isVisible = true,
                isScrollAndMoveEnabled = false,
                count = 3
            ),
            styleBackgroundToolbar = ControlPanelState.Toolbar.Styling.Background(
                isVisible = true,
                selectedBackground = null
            )
        )

        vm.controlPanelViewState.test().apply {
            assertEquals(expected = expectedState, actual = this.value())
        }

        clearPendingCoroutines()
    }

    @Test
    fun `should start background style toolbar with red color when all blocks are not texted`() {
        val targetA = MockDataFactory.randomUuid()
        val backgroundA = "red"
        val a = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Link(
                target = targetA,
                type = Block.Content.Link.Type.PAGE,
                fields = Block.Fields.empty()
            ),
            backgroundColor = backgroundA
        )

        val targetB = MockDataFactory.randomUuid()
        val b = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Link(
                target = targetB,
                type = Block.Content.Link.Type.PAGE,
                fields = Block.Fields.empty()
            ),
            backgroundColor = backgroundA
        )

        val c = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.File(
                state = Block.Content.File.State.EMPTY
            ),
            backgroundColor = backgroundA
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(a.id, b.id, c.id)
        )

        val document = listOf(page, a, b, c)

        stubOpenDocument(document)
        stubInterceptEvents()

        val vm = buildViewModel()

        vm.onStart(root)

        vm.apply {
            onClickListener(ListenerType.LongClick(target = a.id))
            onTextInputClicked(target = b.id)
            onTextInputClicked(target = c.id)
            onMultiSelectAction(ActionItemType.Style)
        }

        val expectedState = ControlPanelState(
            multiSelect = ControlPanelState.Toolbar.MultiSelect(
                isVisible = true,
                isScrollAndMoveEnabled = false,
                count = 3
            ),
            styleBackgroundToolbar = ControlPanelState.Toolbar.Styling.Background(
                isVisible = true,
                selectedBackground = backgroundA
            )
        )

        vm.controlPanelViewState.test().apply {
            assertEquals(expected = expectedState, actual = this.value())
        }

        clearPendingCoroutines()
    }

    @Test
    fun `should start background style toolbar with red color when blocks are mixed`() {
        val targetA = MockDataFactory.randomUuid()
        val backgroundA = "red"
        val a = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Link(
                target = targetA,
                type = Block.Content.Link.Type.PAGE,
                fields = Block.Fields.empty()
            ),
            backgroundColor = backgroundA
        )

        val targetB = MockDataFactory.randomUuid()
        val b = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Link(
                target = targetB,
                type = Block.Content.Link.Type.PAGE,
                fields = Block.Fields.empty()
            ),
            backgroundColor = backgroundA
        )

        val c = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Link(
                target = targetA,
                type = Block.Content.Link.Type.PAGE,
                fields = Block.Fields.empty()
            ),
            backgroundColor = backgroundA
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(a.id, b.id, c.id)
        )

        val document = listOf(page, a, b, c)

        stubOpenDocument(document)
        stubInterceptEvents()

        val vm = buildViewModel()

        vm.onStart(root)

        vm.apply {
            onClickListener(ListenerType.LongClick(target = a.id))
            onTextInputClicked(target = b.id)
            onTextInputClicked(target = c.id)
            onMultiSelectAction(ActionItemType.Style)
        }

        val expectedState = ControlPanelState(
            multiSelect = ControlPanelState.Toolbar.MultiSelect(
                isVisible = true,
                isScrollAndMoveEnabled = false,
                count = 3
            ),
            styleBackgroundToolbar = ControlPanelState.Toolbar.Styling.Background(
                isVisible = true,
                selectedBackground = backgroundA
            )
        )

        vm.controlPanelViewState.test().apply {
            assertEquals(expected = expectedState, actual = this.value())
        }

        clearPendingCoroutines()
    }

    @Test
    fun `should start background style toolbar with default color when all blocks has nullable backgrounds`() {
        val targetA = MockDataFactory.randomUuid()
        val a = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Link(
                target = targetA,
                type = Block.Content.Link.Type.PAGE,
                fields = Block.Fields.empty()
            ),
            backgroundColor = null
        )

        val b = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            ),
            backgroundColor = null
        )

        val c = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Link(
                target = targetA,
                type = Block.Content.Link.Type.PAGE,
                fields = Block.Fields.empty()
            ),
            backgroundColor = null
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(a.id, b.id, c.id)
        )

        val document = listOf(page, a, b, c)

        stubOpenDocument(document)
        stubInterceptEvents()

        val vm = buildViewModel()

        vm.onStart(root)

        vm.apply {
            onClickListener(ListenerType.LongClick(target = a.id))
            onTextInputClicked(target = b.id)
            onTextInputClicked(target = c.id)
            onMultiSelectAction(ActionItemType.Style)
        }

        val expectedState = ControlPanelState(
            multiSelect = ControlPanelState.Toolbar.MultiSelect(
                isVisible = true,
                isScrollAndMoveEnabled = false,
                count = 3
            ),
            styleBackgroundToolbar = ControlPanelState.Toolbar.Styling.Background(
                isVisible = true,
                selectedBackground = ThemeColor.DEFAULT.title
            )
        )

        vm.controlPanelViewState.test().apply {
            assertEquals(expected = expectedState, actual = this.value())
        }

        clearPendingCoroutines()
    }

    private fun clearPendingCoroutines() {
        coroutineTestRule.advanceTime(TEXT_CHANGES_DEBOUNCE_DURATION)
    }
}