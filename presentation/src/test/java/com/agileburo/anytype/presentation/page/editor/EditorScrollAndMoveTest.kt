package com.agileburo.anytype.presentation.page.editor

import MockDataFactory
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.agileburo.anytype.core_ui.state.ControlPanelState
import com.agileburo.anytype.domain.block.interactor.Move
import com.agileburo.anytype.domain.block.model.Block
import com.agileburo.anytype.domain.block.model.Position
import com.agileburo.anytype.domain.ext.content
import com.agileburo.anytype.presentation.page.PageViewModel
import com.agileburo.anytype.presentation.util.CoroutinesTestRule
import com.jraska.livedata.test
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verifyBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations


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
        stubObserveEvents()

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
                    focus = ControlPanelState.Focus(
                        id = a.id,
                        type = ControlPanelState.Focus.Type.NUMBERED
                    ),
                    mainToolbar = ControlPanelState.Toolbar.Main(
                        isVisible = false
                    ),
                    stylingToolbar = ControlPanelState.Toolbar.Styling(
                        isVisible = false,
                        mode = null,
                        type = null
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
        stubObserveEvents()

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
                    focus = ControlPanelState.Focus(
                        id = a.id,
                        type = ControlPanelState.Focus.Type.NUMBERED
                    ),
                    mainToolbar = ControlPanelState.Toolbar.Main(
                        isVisible = false
                    ),
                    stylingToolbar = ControlPanelState.Toolbar.Styling(
                        isVisible = false,
                        mode = null,
                        type = null
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
        stubObserveEvents()
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
                    focus = ControlPanelState.Focus(
                        id = a.id,
                        type = ControlPanelState.Focus.Type.NUMBERED
                    ),
                    mainToolbar = ControlPanelState.Toolbar.Main(
                        isVisible = false
                    ),
                    stylingToolbar = ControlPanelState.Toolbar.Styling(
                        isVisible = false,
                        mode = null,
                        type = null
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
        stubObserveEvents()
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
        stubObserveEvents()
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
        stubObserveEvents()
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

    private fun clearPendingCoroutines() {
        coroutineTestRule.advanceTime(PageViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)
    }

}