package com.anytypeio.anytype.presentation.page.editor

import MockDataFactory
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.domain.block.interactor.Move
import com.anytypeio.anytype.domain.block.model.Block
import com.anytypeio.anytype.domain.block.model.Position
import com.anytypeio.anytype.domain.ext.content
import com.anytypeio.anytype.presentation.page.PageViewModel
import com.anytypeio.anytype.presentation.page.editor.control.ControlPanelState
import com.anytypeio.anytype.presentation.page.editor.model.BlockView
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import com.anytypeio.anytype.presentation.util.TXT
import com.jraska.livedata.test
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verifyBlocking
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations
import kotlin.test.assertEquals


class EditorScrollAndMoveTest : EditorPresentationTestSetup() {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun `should enter scroll-and-move mode after selecting one-block`() {

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
                style = Block.Content.Text.Style.P
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(
                type = Block.Content.Smart.Type.PAGE
            ),
            children = listOf(a.id, b.id, c.id)
        )

        val document = listOf(page, a, b, c)

        stubOpenDocument(document)
        stubInterceptEvents()

        val vm = buildViewModel()

        vm.onStart(root)

        vm.apply {
            onBlockFocusChanged(
                id = a.id,
                hasFocus = true
            )
            onEnterMultiSelectModeClicked()
            onTextInputClicked(a.id)
            onEnterScrollAndMoveClicked()
        }

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
                        isScrollAndMoveEnabled = true,
                        count = 1
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
    fun `should exit scroll-and-move mode on cancel-button-pressed event`() {

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
                style = Block.Content.Text.Style.P
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(
                type = Block.Content.Smart.Type.PAGE
            ),
            children = listOf(a.id, b.id, c.id)
        )

        val document = listOf(page, a, b, c)

        stubOpenDocument(document)
        stubInterceptEvents()

        val vm = buildViewModel()

        vm.onStart(root)

        vm.apply {
            onBlockFocusChanged(
                id = a.id,
                hasFocus = true
            )
            onEnterMultiSelectModeClicked()
            onTextInputClicked(a.id)
            onEnterScrollAndMoveClicked()
            onExitScrollAndMoveClicked()
        }

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
                        count = 1
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
    fun `should exit to editor mode on system back button event`() {

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

        val vm = buildViewModel()

        vm.onStart(root)

        vm.apply {
            onBlockFocusChanged(
                id = a.id,
                hasFocus = true
            )
            onEnterMultiSelectModeClicked()
            onTextInputClicked(a.id)
            onEnterScrollAndMoveClicked()
            onSystemBackPressed(false)
        }

        coroutineTestRule.advanceTime(PageViewModel.DELAY_REFRESH_DOCUMENT_ON_EXIT_MULTI_SELECT_MODE + 100)

        vm.controlPanelViewState.test().apply {
            assertValue(
                ControlPanelState(
                    navigationToolbar = ControlPanelState.Toolbar.Navigation(
                        isVisible = true
                    ),
                    mainToolbar = ControlPanelState.Toolbar.Main(
                        isVisible = false
                    ),
                    stylingToolbar = ControlPanelState.Toolbar.Styling(
                        isVisible = false,
                        mode = null
                    ),
                    multiSelect = ControlPanelState.Toolbar.MultiSelect(
                        isVisible = false,
                        isScrollAndMoveEnabled = false,
                        isQuickScrollAndMoveMode = false,
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

        coroutineTestRule.advanceTime(PageViewModel.DELAY_REFRESH_DOCUMENT_ON_EXIT_MULTI_SELECT_MODE + 100)

        vm.state.test().assertValue(
            ViewState.Success(
                listOf(
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
    fun `should exit scroll-and-move mode on apply-scroll-and-move event`() {

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
                style = Block.Content.Text.Style.P
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(
                type = Block.Content.Smart.Type.PAGE
            ),
            children = listOf(a.id, b.id, c.id)
        )

        val document = listOf(page, a, b, c)

        stubOpenDocument(document)
        stubInterceptEvents()
        stubMove()

        val vm = buildViewModel()

        vm.onStart(root)

        vm.apply {
            onBlockFocusChanged(
                id = a.id,
                hasFocus = true
            )
            onEnterMultiSelectModeClicked()
            onTextInputClicked(a.id)
            onEnterScrollAndMoveClicked()
            onApplyScrollAndMove(
                target = b.id,
                ratio = 0.9f
            )
        }

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

        clearPendingCoroutines()
    }

    @Test
    fun `should move the selected block below target`() {

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

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(
                type = Block.Content.Smart.Type.PAGE
            ),
            children = listOf(a.id, b.id)
        )

        val document = listOf(page, a, b)

        stubOpenDocument(document)
        stubInterceptEvents()
        stubMove()

        val vm = buildViewModel()

        vm.onStart(root)

        vm.apply {
            onBlockFocusChanged(
                id = a.id,
                hasFocus = true
            )
            onEnterMultiSelectModeClicked()
            onTextInputClicked(a.id)
            onEnterScrollAndMoveClicked()
            onApplyScrollAndMove(
                target = b.id,
                ratio = 0.9f
            )
        }

        verifyBlocking(move, times(1)) {
            invoke(
                params = Move.Params(
                    context = root,
                    targetContext = root,
                    targetId = b.id,
                    blockIds = listOf(a.id),
                    position = Position.BOTTOM
                )
            )
        }

        clearPendingCoroutines()
    }

    @Test
    fun `should move the selected block above target`() {

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

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(
                type = Block.Content.Smart.Type.PAGE
            ),
            children = listOf(a.id, b.id)
        )

        val document = listOf(page, a, b)

        stubOpenDocument(document)
        stubInterceptEvents()
        stubMove()

        val vm = buildViewModel()

        vm.onStart(root)

        vm.apply {
            onBlockFocusChanged(
                id = a.id,
                hasFocus = true
            )
            onEnterMultiSelectModeClicked()
            onTextInputClicked(a.id)
            onEnterScrollAndMoveClicked()
            onApplyScrollAndMove(
                target = b.id,
                ratio = 0.1f
            )
        }

        verifyBlocking(move, times(1)) {
            invoke(
                params = Move.Params(
                    context = root,
                    targetContext = root,
                    targetId = b.id,
                    blockIds = listOf(a.id),
                    position = Position.TOP
                )
            )
        }

        clearPendingCoroutines()
    }

    @Test
    fun `should move the selected block inside page`() {

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
            content = Block.Content.Link(
                target = MockDataFactory.randomUuid(),
                fields = Block.Fields.empty(),
                type = Block.Content.Link.Type.PAGE
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(
                type = Block.Content.Smart.Type.PAGE
            ),
            children = listOf(a.id, b.id)
        )

        val document = listOf(page, a, b)

        stubOpenDocument(document)
        stubInterceptEvents()
        stubMove()

        val vm = buildViewModel()

        vm.onStart(root)

        vm.apply {
            onBlockFocusChanged(
                id = a.id,
                hasFocus = true
            )
            onEnterMultiSelectModeClicked()
            onTextInputClicked(a.id)
            onEnterScrollAndMoveClicked()
            onApplyScrollAndMove(
                target = b.id,
                ratio = 0.5f
            )
        }

        verifyBlocking(move, times(1)) {
            invoke(
                params = Move.Params(
                    context = root,
                    targetContext = b.content<Block.Content.Link>().target,
                    targetId = b.id,
                    blockIds = listOf(a.id),
                    position = Position.INNER
                )
            )
        }

        clearPendingCoroutines()
    }

    @Test
    fun `should not move parent paragraph into child paragraph`() {

        // SETUP

        val child = Block(
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
            children = listOf(child.id),
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

        val document = listOf(page, parent, child)

        stubOpenDocument(document)
        stubInterceptEvents()
        stubMove()

        val vm = buildViewModel()

        vm.onStart(root)

        // TESTING

        val toasts = mutableListOf<String>()

        runBlockingTest {

            val subscription = launch { vm.toasts.collect { toasts.add(it) } }

            vm.apply {
                onBlockFocusChanged(
                    id = parent.id,
                    hasFocus = true
                )
                onEnterMultiSelectModeClicked()
                onTextInputClicked(parent.id)
                onEnterScrollAndMoveClicked()
                onApplyScrollAndMove(
                    target = child.id,
                    ratio = 0.5f
                )
            }

            verifyZeroInteractions(move)

            assertEquals(
                expected = 1,
                actual = toasts.size
            )

            assertEquals(
                expected = PageViewModel.CANNOT_BE_DROPPED_INSIDE_ITSELF_ERROR,
                actual = toasts.first()
            )

            clearPendingCoroutines()

            subscription.cancel()
        }
    }

    @Test
    fun `should not move because one of selected blocks is parent for target`() {

        // SETUP

        val child = Block(
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
            children = listOf(child.id),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            )
        )

        val block = Block(
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
            children = listOf(parent.id, block.id)
        )

        val document = listOf(page, parent, child, block)

        stubOpenDocument(document)
        stubInterceptEvents()
        stubMove()

        val vm = buildViewModel()

        vm.onStart(root)

        val toasts = mutableListOf<String>()

        // TESTING

        runBlockingTest {

            val subscription = launch { vm.toasts.collect { toasts.add(it) } }

            vm.apply {
                onBlockFocusChanged(
                    id = parent.id,
                    hasFocus = true
                )
                onEnterMultiSelectModeClicked()
                onTextInputClicked(parent.id)
                onTextInputClicked(block.id)
                onEnterScrollAndMoveClicked()
                onApplyScrollAndMove(
                    target = child.id,
                    ratio = 0.5f
                )
            }

            verifyZeroInteractions(move)

            assertEquals(
                expected = 1,
                actual = toasts.size
            )

            assertEquals(
                expected = PageViewModel.CANNOT_BE_DROPPED_INSIDE_ITSELF_ERROR,
                actual = toasts.first()
            )

            clearPendingCoroutines()

            subscription.cancel()
        }
    }

    @Test
    fun `should move blocks following document order, not selection order`() {

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
                style = Block.Content.Text.Style.P
            )
        )

        val d = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            )
        )

        val e = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.P
            )
        )

        val f = Block(
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
            children = listOf(a.id, b.id, c.id, d.id, e.id, f.id)
        )

        val document = listOf(page, a, b, c, d, e, f)

        stubOpenDocument(document)
        stubInterceptEvents()
        stubMove()

        val vm = buildViewModel()

        // TESTING

        vm.onStart(root)

        vm.apply {

            onBlockFocusChanged(
                id = a.id,
                hasFocus = true
            )

            onEnterMultiSelectModeClicked()

            onTextInputClicked(d.id)
            onTextInputClicked(c.id)
            onTextInputClicked(f.id)
            onTextInputClicked(b.id)

            onEnterScrollAndMoveClicked()

            onApplyScrollAndMove(
                target = a.id,
                ratio = 0.1f
            )
        }

        // Verifying order of selected blocks used in request

        verifyBlocking(move, times(1)) {
            invoke(
                params = Move.Params(
                    context = root,
                    targetContext = root,
                    targetId = a.id,
                    blockIds = listOf(b.id, c.id, d.id, f.id),
                    position = Position.TOP
                )
            )
        }

        clearPendingCoroutines()
    }

    private fun clearPendingCoroutines() {
        coroutineTestRule.advanceTime(PageViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)
    }
}