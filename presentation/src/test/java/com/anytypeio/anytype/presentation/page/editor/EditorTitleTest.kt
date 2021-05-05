package com.anytypeio.anytype.presentation.page.editor

import MockDataFactory
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.domain.block.interactor.UpdateText
import com.anytypeio.anytype.domain.event.interactor.InterceptEvents
import com.anytypeio.anytype.presentation.page.PageViewModel
import com.anytypeio.anytype.presentation.page.editor.control.ControlPanelState
import com.anytypeio.anytype.presentation.page.editor.model.BlockView
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import com.jraska.livedata.test
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verifyBlocking
import org.mockito.kotlin.verifyZeroInteractions
import kotlin.test.assertEquals

class EditorTitleTest : EditorPresentationTestSetup() {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
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
    fun `should not open action menu for title block`() {

        // SETUP

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(
                type = Block.Content.Smart.Type.PAGE
            ),
            children = listOf(header.id)
        )

        val document = listOf(page, header, title)

        stubOpenDocument(document = document)
        stubInterceptEvents(InterceptEvents.Params(context = root))

        val vm = buildViewModel()

        // TESTING

        val toasts = mutableListOf<String>()

        runBlockingTest {

            val toastSubscription = launch { vm.toasts.collect { toasts.add(it) } }
            val commandTestObserver = vm.commands.test()

            vm.apply {
                onStart(root)
                onBlockFocusChanged(title.id, true)
                onBlockToolbarBlockActionsClicked()
            }

            commandTestObserver.assertNoValue().assertHistorySize(0)

            assertEquals(
                expected = 1,
                actual = toasts.size
            )
            assertEquals(
                expected = PageViewModel.CANNOT_OPEN_ACTION_MENU_FOR_TITLE_ERROR,
                actual = toasts.first()
            )

            toastSubscription.cancel()
        }
    }

    @Test
    fun `should not open style panel for title block`() {

        // SETUP

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(
                type = Block.Content.Smart.Type.PAGE
            ),
            children = listOf(header.id)
        )

        val document = listOf(page, header, title)

        stubOpenDocument(document = document)
        stubInterceptEvents(InterceptEvents.Params(context = root))

        val vm = buildViewModel()

        // TESTING

        val toasts = mutableListOf<String>()

        runBlockingTest {

            val toastSubscription = launch { vm.toasts.collect { toasts.add(it) } }

            val commandTestObserver = vm.commands.test()
            val controlPanelObserver = vm.controlPanelViewState.test()

            vm.apply {
                onStart(root)
                onBlockFocusChanged(title.id, true)
                onBlockToolbarStyleClicked()
            }

            commandTestObserver.assertNoValue().assertHistorySize(0)
            controlPanelObserver.assertValue(
                ControlPanelState(
                    mainToolbar = ControlPanelState.Toolbar.Main(
                        isVisible = true
                    ),
                    stylingToolbar = ControlPanelState.Toolbar.Styling.reset(),
                    multiSelect = ControlPanelState.Toolbar.MultiSelect(
                        isVisible = false,
                        isScrollAndMoveEnabled = false
                    ),
                    mentionToolbar = ControlPanelState.Toolbar.MentionToolbar.reset(),
                    navigationToolbar = ControlPanelState.Toolbar.Navigation(isVisible = false),
                    slashWidget = ControlPanelState.Toolbar.SlashWidget.reset()
                )
            )

            assertEquals(
                expected = 1,
                actual = toasts.size
            )
            assertEquals(
                expected = PageViewModel.CANNOT_OPEN_STYLE_PANEL_FOR_TITLE_ERROR,
                actual = toasts.first()
            )

            toastSubscription.cancel()
        }
    }

    @Test
    fun `should start updating title on title-text-changed event with delay`() {

        // SETUP

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields.empty(),
                content = Block.Content.Smart(Block.Content.Smart.Type.PAGE),
                children = listOf(header.id)
            ),
            header,
            title
        )

        stubInterceptEvents()
        stubOpenDocument(page)
        stubUpdateText()

        val vm = buildViewModel()

        vm.onStart(root)

        // TESTING

        val update = MockDataFactory.randomString()

        vm.onTitleBlockTextChanged(
            BlockView.Title.Basic(
                id = title.id,
                text = update
            )
        )

        verifyZeroInteractions(updateTitle)
        verifyZeroInteractions(updateText)

        coroutineTestRule.advanceTime(PageViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)

        verifyZeroInteractions(updateTitle)

        verifyBlocking(updateText) {
            invoke(
                UpdateText.Params(
                    context = root,
                    text = update,
                    target = title.id,
                    marks = emptyList()
                )
            )
        }
    }
}