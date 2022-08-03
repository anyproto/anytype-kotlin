package com.anytypeio.anytype.presentation.editor.editor

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.StubTitle
import com.anytypeio.anytype.core_models.ThemeColor
import com.anytypeio.anytype.core_models.StubHeader
import com.anytypeio.anytype.core_models.StubLayoutColumns
import com.anytypeio.anytype.core_models.StubLayoutRows
import com.anytypeio.anytype.core_models.StubParagraph
import com.anytypeio.anytype.core_models.StubTable
import com.anytypeio.anytype.core_models.StubTableCells
import com.anytypeio.anytype.core_models.StubTableColumn
import com.anytypeio.anytype.core_models.StubTableColumns
import com.anytypeio.anytype.core_models.StubTableRow
import com.anytypeio.anytype.core_models.StubTableRows
import com.anytypeio.anytype.core_models.StubTitle
import com.anytypeio.anytype.core_models.ext.content
import com.anytypeio.anytype.domain.block.interactor.UnlinkBlocks
import com.anytypeio.anytype.domain.clipboard.Copy
import com.anytypeio.anytype.presentation.BuildConfig
import com.anytypeio.anytype.presentation.MockBlockContentFactory.StubLinkContent
import com.anytypeio.anytype.presentation.MockBlockFactory
import com.anytypeio.anytype.presentation.MockBlockFactory.link
import com.anytypeio.anytype.presentation.editor.EditorViewModel
import com.anytypeio.anytype.presentation.editor.EditorViewModel.Companion.DELAY_REFRESH_DOCUMENT_TO_ENTER_MULTI_SELECT_MODE
import com.anytypeio.anytype.presentation.editor.EditorViewModel.Companion.TEXT_CHANGES_DEBOUNCE_DURATION
import com.anytypeio.anytype.presentation.editor.editor.actions.ActionItemType
import com.anytypeio.anytype.presentation.editor.editor.control.ControlPanelState
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.editor.styling.StyleToolbarState
import com.anytypeio.anytype.presentation.editor.render.parseThemeBackgroundColor
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import com.anytypeio.anytype.presentation.util.TXT
import com.anytypeio.anytype.test_utils.MockDataFactory
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

    val title = StubTitle()

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
            indent = 0,
            text = parent.content<Block.Content.Text>().text,
            mode = BlockView.Mode.READ,
            decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                listOf(
                    BlockView.Decoration(
                        background = parent.parseThemeBackgroundColor()
                    )
                )
            } else {
                emptyList()
            }
        )

        val child1View = BlockView.Text.Paragraph(
            id = child1.id,
            isSelected = true,
            isFocused = false,
            marks = emptyList(),
            indent = 1,
            text = child1.content<Block.Content.Text>().text,
            mode = BlockView.Mode.READ,
            decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                listOf(
                    BlockView.Decoration(
                        background = parent.parseThemeBackgroundColor()
                    ),
                    BlockView.Decoration(
                        background = child1.parseThemeBackgroundColor()
                    )
                )
            } else {
                emptyList()
            }
        )

        val child2View = BlockView.Text.Paragraph(
            id = child2.id,
            isSelected = true,
            isFocused = false,
            marks = emptyList(),
            indent = 1,
            text = child2.content<Block.Content.Text>().text,
            mode = BlockView.Mode.READ,
            decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                listOf(
                    BlockView.Decoration(
                        background = parent.parseThemeBackgroundColor()
                    ),
                    BlockView.Decoration(
                        background = child2.parseThemeBackgroundColor()
                    )
                )
            } else {
                emptyList()
            }
        )

        val grandchild1View = BlockView.Text.Paragraph(
            id = grandchild1.id,
            isSelected = true,
            isFocused = false,
            marks = emptyList(),
            indent = 2,
            text = grandchild1.content<Block.Content.Text>().text,
            mode = BlockView.Mode.READ,
            decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                listOf(
                    BlockView.Decoration(
                        background = parent.parseThemeBackgroundColor()
                    ),
                    BlockView.Decoration(
                        background = child1.parseThemeBackgroundColor()
                    ),
                    BlockView.Decoration(
                        background = grandchild1.parseThemeBackgroundColor()
                    )
                )
            } else {
                emptyList()
            }
        )

        val grandchild2View = BlockView.Text.Paragraph(
            id = grandchild2.id,
            isSelected = true,
            isFocused = false,
            marks = emptyList(),
            indent = 2,
            text = grandchild2.content<Block.Content.Text>().text,
            mode = BlockView.Mode.READ,
            decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                listOf(
                    BlockView.Decoration(
                        background = parent.parseThemeBackgroundColor()
                    ),
                    BlockView.Decoration(
                        background = child1.parseThemeBackgroundColor()
                    ),
                    BlockView.Decoration(
                        background = grandchild2.parseThemeBackgroundColor()
                    )
                )
            } else {
                emptyList()
            }
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
            indent = 0,
            text = parent.content<Block.Content.Text>().text,
            mode = BlockView.Mode.READ,
            decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                listOf(
                    BlockView.Decoration(
                        background = parent.parseThemeBackgroundColor()
                    )
                )
            } else {
                emptyList()
            }
        )

        val child1View = BlockView.Text.Paragraph(
            id = child1.id,
            isSelected = false,
            isFocused = false,
            marks = emptyList(),
            indent = 1,
            text = child1.content<Block.Content.Text>().text,
            mode = BlockView.Mode.READ,
            decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                listOf(
                    BlockView.Decoration(
                        background = parent.parseThemeBackgroundColor()
                    ),
                    BlockView.Decoration(
                        background = child1.parseThemeBackgroundColor()
                    )
                )
            } else {
                emptyList()
            }
        )

        val child2View = BlockView.Text.Paragraph(
            id = child2.id,
            isSelected = false,
            isFocused = false,
            marks = emptyList(),
            indent = 1,
            text = child2.content<Block.Content.Text>().text,
            mode = BlockView.Mode.READ,
            decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                listOf(
                    BlockView.Decoration(
                        background = parent.parseThemeBackgroundColor()
                    ),
                    BlockView.Decoration(
                        background = child2.parseThemeBackgroundColor()
                    )
                )
            } else {
                emptyList()
            }
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
                        isSelected = false,
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
                mainToolbar = ControlPanelState.Toolbar.Main.reset(),
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
                mainToolbar = ControlPanelState.Toolbar.Main.reset(),
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
                mainToolbar = ControlPanelState.Toolbar.Main.reset(),
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
                        isSelected = true,
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
                        id = b.id,
                        text = b.content<TXT>().text,
                        mode = BlockView.Mode.READ,
                        isSelected = true,
                        decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                            listOf(
                                BlockView.Decoration(
                                    background = b.parseThemeBackgroundColor()
                                )
                            )
                        } else {
                            emptyList()
                        }
                    ),
                    BlockView.Text.Paragraph(
                        id = c.id,
                        text = c.content<TXT>().text,
                        mode = BlockView.Mode.READ,
                        isSelected = true,
                        decorations = if (BuildConfig.NESTED_DECORATION_ENABLED) {
                            listOf(
                                BlockView.Decoration(
                                    background = c.parseThemeBackgroundColor()
                                )
                            )
                        } else {
                            emptyList()
                        }
                    )
                )
            )
        )

        clearPendingCoroutines()
    }

    @Test
    fun `should start main style toolbar when all blocks are texted with default background`() {
        val a = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.NUMBERED
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
            backgroundColor = ThemeColor.DEFAULT.code
        )

        val c = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.H2
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
            styleTextToolbar = ControlPanelState.Toolbar.Styling(
                isVisible = true,
                state = StyleToolbarState.Text(null)
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
        val a = link(
            content = StubLinkContent(
                target = targetA,
            ),
            backgroundColor = backgroundA
        )

        val targetB = MockDataFactory.randomUuid()
        val backgroundB = "teal"
        val b = link(
            content = StubLinkContent(
                target = targetB,
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
                state = StyleToolbarState.Background(background = null)
            )
        )

        vm.controlPanelViewState.test().apply {
            assertEquals(expected = expectedState, actual = this.value())
        }

        clearPendingCoroutines()
    }

    @Test
    fun `should start background style toolbar with red color when all blocks are not texted`() {
        val backgroundA = "red"
        val a = link(backgroundColor = backgroundA)
        val b = link(backgroundColor = backgroundA)
        val c = link(backgroundColor = backgroundA)
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
                state = StyleToolbarState.Background(background = backgroundA)
            )
        )

        vm.controlPanelViewState.test().apply {
            assertEquals(expected = expectedState, actual = this.value())
        }

        clearPendingCoroutines()
    }

    @Test
    fun `should start background style toolbar with red color when blocks are mixed`() {
        val backgroundA = "red"
        val a = link(backgroundColor = backgroundA)
        val b = link(backgroundColor = backgroundA)
        val c = link(backgroundColor = backgroundA)

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
                state = StyleToolbarState.Background(background = backgroundA)
            )
        )

        vm.controlPanelViewState.test().apply {
            assertEquals(expected = expectedState, actual = this.value())
        }

        clearPendingCoroutines()
    }

    @Test
    fun `should start background style toolbar with default color when all blocks has nullable backgrounds`() {
        val a = link()
        val b = link()
        val c = link()

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
                state = StyleToolbarState.Background(background = ThemeColor.DEFAULT.code)
            )
        )

        vm.controlPanelViewState.test().apply {
            assertEquals(expected = expectedState, actual = this.value())
        }

        clearPendingCoroutines()
    }

    @Test
    fun `should send selected blocks on copy click`() {

        // SETUP

        val block1 = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
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
                style = Block.Content.Text.Style.P
            )
        )

        val block3 = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = listOf(),
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
            children = listOf(header.id, block1.id, block2.id, block3.id)
        )

        val document = listOf(page, header, title, block1, block2, block3)

        stubOpenDocument(document)
        stubInterceptEvents()

        val vm = buildViewModel()

        vm.onStart(root)

        // TESTING

        vm.apply {
            onEnterMultiSelectModeClicked()
            onTextInputClicked(block1.id)
            onTextInputClicked(block2.id)
            onTextInputClicked(block3.id)
            onMultiSelectAction(ActionItemType.Copy)
        }

        val params = Copy.Params(
            context = root,
            range = null,
            blocks = listOf(block1, block2, block3)
        )

        verifyBlocking(copy, times(1)) { invoke(params) }

        clearPendingCoroutines()
    }

    @Test
    fun `should not select table block cells on table block long click event`() {

        val columns = StubTableColumns(size = 3)
        val rows = StubTableRows(size = 2)
        val cells = StubTableCells(columns = columns, rows = rows)
        val columnLayout = StubLayoutColumns(children = columns.map { it.id })
        val rowLayout = StubLayoutRows(children = rows.map { it.id })
        val table = StubTable(children = listOf(columnLayout.id, rowLayout.id))

        val title = StubTitle()
        val header = StubHeader(children = listOf(title.id))

        val page = Block(
            id = root,
            children = listOf(header.id) + listOf(table.id),
            fields = Block.Fields.empty(),
            content = Block.Content.Smart()
        )

        val document =
            listOf(page, header, title, table, columnLayout, rowLayout) + columns + rows + cells

        stubOpenDocument(document)
        stubInterceptEvents()

        val vm = buildViewModel()

        vm.onStart(root)

        vm.onClickListener(ListenerType.LongClick(target = table.id))

        val selectedId = vm.currentSelection().first()

        assertEquals(1, vm.currentSelection().size)
        assertEquals(table.id, selectedId)
    }

    private fun clearPendingCoroutines() {
        coroutineTestRule.advanceTime(TEXT_CHANGES_DEBOUNCE_DURATION)
    }
}