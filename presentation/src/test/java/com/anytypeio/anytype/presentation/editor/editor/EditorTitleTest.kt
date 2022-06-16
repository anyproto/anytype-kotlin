package com.anytypeio.anytype.presentation.editor.editor

import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.ext.content
import com.anytypeio.anytype.domain.block.interactor.SplitBlock
import com.anytypeio.anytype.domain.block.interactor.UpdateText
import com.anytypeio.anytype.domain.event.interactor.InterceptEvents
import com.anytypeio.anytype.presentation.editor.EditorViewModel
import com.anytypeio.anytype.presentation.editor.editor.control.ControlPanelState
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import com.anytypeio.anytype.test_utils.MockDataFactory
import com.jraska.livedata.test
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import net.lachlanmckee.timberjunit.TimberTestRule
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.eq
import org.mockito.kotlin.times
import org.mockito.kotlin.verifyBlocking
import org.mockito.kotlin.verifyZeroInteractions
import kotlin.test.assertEquals

class EditorTitleTest : EditorPresentationTestSetup() {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

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
    }

    @After
    fun after() {
        coroutineTestRule.advanceTime(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)
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
            content = Block.Content.Smart(),
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
                expected = EditorViewModel.CANNOT_OPEN_ACTION_MENU_FOR_TITLE_ERROR,
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
            content = Block.Content.Smart(),
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
                    styleTextToolbar = ControlPanelState.Toolbar.Styling.reset(),
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
                expected = EditorViewModel.CANNOT_OPEN_STYLE_PANEL_FOR_TITLE_ERROR,
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
                content = Block.Content.Smart(),
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

        vm.onTitleBlockTextChanged(title.id, update)

        verifyZeroInteractions(updateText)

        coroutineTestRule.advanceTime(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)

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

    @Test
    fun `should send split intent when split happens in title`() {
        // SETUP

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(header.id)
        )

        val block = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = MockDataFactory.randomUuid(),
                marks = emptyList(),
                style = Block.Content.Text.Style.values().random()
            )
        )

        val document = listOf(page, header, title, block)

        stubOpenDocument(document = document)
        stubUpdateText()
        stubCreateBlock(root)
        stubInterceptEvents(InterceptEvents.Params(context = root))

        val vm = buildViewModel()

        vm.onStart(root)
        
        // TESTING

        vm.onBlockFocusChanged(
            id = title.id,
            hasFocus = true
        )

        val text = MockDataFactory.randomString()

        vm.onEnterKeyClicked(
            target = title.id,
            text = text,
            marks = emptyList(),
            range = 2..2
        )

        verifyBlocking(updateText, times(1)) {
            invoke(
                params = eq(
                    UpdateText.Params(
                        context = root,
                        text = text,
                        marks = emptyList(),
                        target = title.id
                    )
                )
            )
        }

        coroutineTestRule.advanceTime(EditorViewModel.TEXT_CHANGES_DEBOUNCE_DURATION)

        verifyBlocking(splitBlock, times(1)) {
            invoke(
                params = eq(
                    SplitBlock.Params(
                        context = root,
                        block = title,
                        range = 2..2,
                        isToggled = null
                    )
                )
            )
        }
    }

    @Test
    fun `should update emoji in title`()  {
        // SETUP

        val delay = 500L
        val emoji = "\uD83D\uDE0D"

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

        val block = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = MockDataFactory.randomUuid(),
                marks = emptyList(),
                style = Block.Content.Text.Style.BULLET
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(header.id, block.id)
        )

        val document = listOf(page, header, title, block)

        stubOpenDocument(document = document)
        stubUpdateText()
        val events = flow<List<Event>> {
            delay(delay)
            emit(
                listOf(Event.Command.Details.Amend(
                    context = root,
                    target = root,
                    details = mapOf(Relations.ICON_EMOJI to emoji)
                ))
            )
        }
        stubInterceptEvents(flow = events)

        val vm = buildViewModel()

        vm.onStart(root)

        // TESTING

        val before = ViewState.Success(
            blocks = listOf(
                BlockView.Title.Basic(
                    id = title.id,
                    text = title.content<Block.Content.Text>().text,
                    isFocused = false,
                    emoji = null
                ),
                BlockView.Text.Bulleted(
                    id = block.id,
                    text = block.content<Block.Content.Text>().text
                )
            )
        )

        // Checking that title has no emoji
        assertEquals(expected = before, actual = vm.state.value)

        // Moving time forward to receive granular change event
        coroutineTestRule.advanceTime(delay)

        val after = ViewState.Success(
            blocks = listOf(
                BlockView.Title.Basic(
                    id = title.id,
                    text = title.content<Block.Content.Text>().text,
                    isFocused = false,
                    emoji = emoji
                ),
                BlockView.Text.Bulleted(
                    id = block.id,
                    text = block.content<Block.Content.Text>().text
                )
            )
        )

        // Checking that title has emoji
        assertEquals(expected = after, actual = vm.state.value)
    }

    @Test
    fun `should update emoji and then update text in title`()  {
        // SETUP

        val delay = 500L
        val emoji = "\uD83D\uDE0D"

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

        val block = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields.empty(),
            children = emptyList(),
            content = Block.Content.Text(
                text = MockDataFactory.randomUuid(),
                marks = emptyList(),
                style = Block.Content.Text.Style.BULLET
            )
        )

        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(header.id, block.id)
        )

        val document = listOf(page, header, title, block)

        stubOpenDocument(document = document)
        stubUpdateText()
        val events = flow<List<Event>> {
            delay(delay)
            emit(
                listOf(Event.Command.Details.Amend(
                    context = root,
                    target = root,
                    details = mapOf(Relations.ICON_EMOJI to emoji)
                ))
            )
        }
        stubInterceptEvents(flow = events)

        val vm = buildViewModel()

        vm.onStart(root)

        // TESTING

        // Moving time forward to receive granular change event
        coroutineTestRule.advanceTime(delay)

        val newText = MockDataFactory.randomString()
        //Update title text
        vm.onTitleBlockTextChanged(id = title.id, text = newText)

        val after = ViewState.Success(
            blocks = listOf(
                BlockView.Title.Basic(
                    id = title.id,
                    text = newText,
                    isFocused = false,
                    emoji = emoji
                ),
                BlockView.Text.Bulleted(
                    id = block.id,
                    text = block.content<Block.Content.Text>().text
                )
            )
        )

        // Checking that title has new text and emoji
        assertEquals(expected = after, actual = vm.state.value)
    }
 }