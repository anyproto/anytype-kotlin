package com.anytypeio.anytype.presentation.editor.editor

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.ext.content
import com.anytypeio.anytype.presentation.editor.EditorViewModel
import com.anytypeio.anytype.presentation.editor.editor.control.ControlPanelState
import com.anytypeio.anytype.presentation.editor.editor.control.ControlPanelState.Toolbar
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import com.anytypeio.anytype.presentation.util.TXT
import com.anytypeio.anytype.test_utils.MockDataFactory
import com.jraska.livedata.test
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations
import kotlin.test.assertEquals

class EditorQuickStartingScrollAndMoveTest : EditorPresentationTestSetup() {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

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

    @Test
    fun `should be in selected mode when exiting from quick SAM`() {

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

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(header.id, a.id, b.id)
        )

        val document = listOf(page, header, title, a, b)

        stubOpenDocument(document)
        stubInterceptEvents()

        val vm = buildViewModel()

        // TESTING

        vm.onStart(root)

        val controlPanelTestObserver = vm.controlPanelViewState.test()
        val viewStateTestObserver = vm.state.test()

        vm.apply {
            onBlockFocusChanged(id = b.id, hasFocus = true)
            onBlockToolbarBlockActionsClicked()
            onBlockFocusChanged(id = b.id, hasFocus = false)
            onEnterScrollAndMoveClicked()
            onExitScrollAndMoveClicked()
        }

        // VERIFYING

        val actual = controlPanelTestObserver.value()

        val expected =
            ControlPanelState(
                navigationToolbar = Toolbar.Navigation.reset(),
                mainToolbar = Toolbar.Main.reset(),
                mentionToolbar = Toolbar.MentionToolbar.reset(),
                multiSelect = Toolbar.MultiSelect(
                    isVisible = true,
                    count = 1,
                    isScrollAndMoveEnabled = false,
                    isQuickScrollAndMoveMode = false,
                ),
                styleTextToolbar = Toolbar.Styling.reset(),
                slashWidget = Toolbar.SlashWidget.reset()
            )

        assertEquals(expected, actual)

        val actualViewState = viewStateTestObserver.value()

        val expectedViewState = ViewState.Success(
            listOf(
                BlockView.Title.Basic(
                    id = title.id,
                    text = title.content<TXT>().text,
                    mode = BlockView.Mode.READ,
                ),
                BlockView.Text.Numbered(
                    id = a.id,
                    text = a.content<TXT>().text,
                    number = 1,
                    mode = BlockView.Mode.READ,
                    isSelected = false
                ),
                BlockView.Text.Paragraph(
                    id = b.id,
                    text = b.content<TXT>().text,
                    mode = BlockView.Mode.READ,
                    isSelected = true
                )
            )
        )

        assertEquals(expectedViewState, actualViewState)
    }

    @Test
    fun `should exit on system back to editing-mode when exiting from quick SAM`() {

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

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(header.id, a.id, b.id)
        )

        val document = listOf(page, header, title, a, b)

        stubOpenDocument(document)
        stubInterceptEvents()

        val vm = buildViewModel()

        // TESTING

        vm.onStart(root)

        val controlPanelTestObserver = vm.controlPanelViewState.test()
        val viewStateTestObserver = vm.state.test()

        vm.apply {
            onBlockFocusChanged(id = b.id, hasFocus = true)
            onBlockToolbarBlockActionsClicked()
            onBlockFocusChanged(id = b.id, hasFocus = false)
            onEnterScrollAndMoveClicked()
            onSystemBackPressed(false)
        }

        // VERIFYING

        controlPanelTestObserver.assertValue(
            ControlPanelState(
                navigationToolbar = Toolbar.Navigation(isVisible = true),
                mainToolbar = Toolbar.Main.reset(),
                mentionToolbar = Toolbar.MentionToolbar.reset(),
                multiSelect = Toolbar.MultiSelect.reset(),
                styleTextToolbar = Toolbar.Styling.reset(),
                slashWidget = Toolbar.SlashWidget.reset()
            )
        )

        coroutineTestRule.advanceTime(EditorViewModel.DELAY_REFRESH_DOCUMENT_ON_EXIT_MULTI_SELECT_MODE + 100)

        viewStateTestObserver.assertValue(
            ViewState.Success(
                listOf(
                    BlockView.Title.Basic(
                        id = title.id,
                        text = title.content<TXT>().text,
                        mode = BlockView.Mode.EDIT,
                    ),
                    BlockView.Text.Numbered(
                        id = a.id,
                        text = a.content<TXT>().text,
                        number = 1,
                        mode = BlockView.Mode.EDIT,
                        isSelected = false
                    ),
                    BlockView.Text.Paragraph(
                        id = b.id,
                        text = b.content<TXT>().text,
                        mode = BlockView.Mode.EDIT,
                        isSelected = false
                    )
                )
            )
        )
    }
}