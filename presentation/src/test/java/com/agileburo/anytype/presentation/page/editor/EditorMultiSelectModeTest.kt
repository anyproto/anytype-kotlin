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
        stubObserveEvents()
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
                        BlockView.Numbered(
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
                        BlockView.Numbered(
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
                        BlockView.Highlight(
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

    private fun clearPendingCoroutines() {
        coroutineTestRule.advanceTime(TEXT_CHANGES_DEBOUNCE_DURATION)
    }
}