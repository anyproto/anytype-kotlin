package com.anytypeio.anytype.presentation.editor

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.TextStyle
import com.anytypeio.anytype.domain.config.Gateway
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.presentation.editor.editor.Markup
import com.anytypeio.anytype.presentation.editor.editor.control.ControlPanelState
import com.anytypeio.anytype.presentation.editor.editor.styling.StyleToolbarState
import com.anytypeio.anytype.presentation.editor.markup.MarkupStyleDescriptor
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import kotlin.test.assertEquals


class ControlPanelStateReducerTest {

    @get:Rule
    val rule = CoroutinesTestRule()

    @Mock
    lateinit var gateway: Gateway
    lateinit var urlBuilder: UrlBuilder

    private val reducer = ControlPanelMachine.Reducer()

    val paragraph = Block(
        id = MockDataFactory.randomUuid(),
        children = emptyList(),
        content = Block.Content.Text(
            text = MockDataFactory.randomString(),
            style = Block.Content.Text.Style.P,
            marks = emptyList()
        ),
        fields = Block.Fields.empty()
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        urlBuilder = UrlBuilder(gateway)
    }

    @Test
    fun `state should have visible block toolbar and focus from the event`() {

        val given = ControlPanelState.init()

        val event = ControlPanelMachine.Event.OnFocusChanged(
            id = MockDataFactory.randomUuid(),
            style = Block.Content.Text.Style.P
        )

        val actual = runBlocking {
            reducer.reduce(
                state = given,
                event = event
            )
        }

        val expected = given.copy(
            mainToolbar = ControlPanelState.Toolbar.Main(
                isVisible = true
            ),
            navigationToolbar = ControlPanelState.Toolbar.Navigation(
                isVisible = false
            )
        )

        assertEquals(
            expected = expected,
            actual = actual
        )
    }

    @Test
    fun `state should hide mentions when cursor before mentions start and widget is visible`() {

        val given = ControlPanelState(
            mentionToolbar = ControlPanelState.Toolbar.MentionToolbar(
                isVisible = true,
                cursorCoordinate = 333,
                mentionFilter = "start",
                mentionFrom = 10
            )
        )

        val event = ControlPanelMachine.Event.OnSelectionChanged(
            selection = IntRange(9, 9),
            target = paragraph
        )

        val actual = runBlocking {
            reducer.reduce(
                state = given,
                event = event
            )
        }

        val expected = given.copy(
            mentionToolbar = ControlPanelState.Toolbar.MentionToolbar(
                isVisible = false,
                cursorCoordinate = null,
                mentionFilter = null,
                mentionFrom = null
            ),
            mainToolbar = ControlPanelState.Toolbar.Main(
                isVisible = true
            ),
        )

        assertEquals(
            expected = expected,
            actual = actual
        )
    }

    @Test
    fun `state should not hide mentions when cursor after mention start`() {

        val given = ControlPanelState(
            mentionToolbar = ControlPanelState.Toolbar.MentionToolbar(
                isVisible = true,
                cursorCoordinate = 333,
                mentionFilter = "start",
                mentionFrom = 10
            )
        )

        val event = ControlPanelMachine.Event.OnSelectionChanged(
            selection = IntRange(11, 11),
            target = paragraph
        )

        val actual = runBlocking {
            reducer.reduce(
                state = given,
                event = event
            )
        }

        val expected = given.copy(
            mentionToolbar = ControlPanelState.Toolbar.MentionToolbar(
                isVisible = true,
                cursorCoordinate = 333,
                mentionFilter = "start",
                mentionFrom = 10
            )
        )

        assertEquals(
            expected = expected,
            actual = actual
        )
    }

    @Test
    fun `state should hide mentions after focus changed`() {

        val given = ControlPanelState(
            mentionToolbar = ControlPanelState.Toolbar.MentionToolbar(
                isVisible = true,
                cursorCoordinate = 333,
                mentionFilter = "start",
                mentionFrom = 10
            )
        )

        val event = ControlPanelMachine.Event.OnFocusChanged(
            id = MockDataFactory.randomUuid(),
            style = Block.Content.Text.Style.P
        )

        val actual = runBlocking {
            reducer.reduce(
                state = given,
                event = event
            )
        }

        val expected = given.copy(
            mentionToolbar = ControlPanelState.Toolbar.MentionToolbar(
                isVisible = false,
                cursorCoordinate = null,
                mentionFilter = null,
                mentionFrom = null
            ),
            mainToolbar = ControlPanelState.Toolbar.Main(isVisible = true)
        )

        assertEquals(
            expected = expected,
            actual = actual
        )
    }

    @Test
    fun `state should hide mentions after selection chaged`() {

        val id = MockDataFactory.randomUuid()

        val given = ControlPanelState(
            mentionToolbar = ControlPanelState.Toolbar.MentionToolbar(
                isVisible = true,
                cursorCoordinate = 333,
                mentionFilter = "start",
                mentionFrom = 10
            )
        )

        val event = ControlPanelMachine.Event.OnSelectionChanged(
            selection = IntRange(8, 8),
            target = Block(
                id = id,
                children = emptyList(),
                content = Block.Content.Text(
                    text = MockDataFactory.randomString(),
                    style = Block.Content.Text.Style.P,
                    marks = emptyList()
                ),
                fields = Block.Fields.empty()
            )
        )

        val actual = runBlocking {
            reducer.reduce(
                state = given,
                event = event
            )
        }

        val expected = given.copy(
            mentionToolbar = ControlPanelState.Toolbar.MentionToolbar(
                isVisible = false,
                cursorCoordinate = null,
                mentionFilter = null,
                mentionFrom = null
            ),
            mainToolbar = ControlPanelState.Toolbar.Main(
                isVisible = true,
            )
        )

        assertEquals(
            expected = expected,
            actual = actual
        )
    }

    @Test
    fun `state should have only focus changed`() {

        val given = ControlPanelState(
            mainToolbar = ControlPanelState.Toolbar.Main(
                isVisible = true
            )
        )

        val event = ControlPanelMachine.Event.OnFocusChanged(
            id = MockDataFactory.randomUuid(),
            style = Block.Content.Text.Style.P
        )

        val actual = runBlocking {
            reducer.reduce(
                state = given,
                event = event
            )
        }

        val expected = given.copy()

        assertEquals(
            expected = expected,
            actual = actual
        )
    }

    @Test
    fun `should return to initial state when focus is cleared`() {

        val given = ControlPanelState(
            mainToolbar = ControlPanelState.Toolbar.Main(
                isVisible = true
            ),
            mentionToolbar = ControlPanelState.Toolbar.MentionToolbar(
                isVisible = false,
                cursorCoordinate = null,
                mentionFilter = null,
                mentionFrom = null
            )
        )

        val event = ControlPanelMachine.Event.OnClearFocusClicked

        val actual = runBlocking {
            reducer.reduce(
                state = given,
                event = event
            )
        }

        val expected = ControlPanelState.init()

        assertEquals(
            expected = expected,
            actual = actual
        )
    }

    @Test
    fun `should be zero selected blocks when enter selected state`() {

        val id = MockDataFactory.randomUuid()

        val nonSelected = ControlPanelState(
            multiSelect = ControlPanelState.Toolbar.MultiSelect(
                isVisible = false,
                count = 2
            ),
            mentionToolbar = ControlPanelState.Toolbar.MentionToolbar(
                isVisible = false,
                cursorCoordinate = null,
                mentionFilter = null,
                mentionFrom = null
            )
        )

        val selected = ControlPanelState(
            multiSelect = ControlPanelState.Toolbar.MultiSelect(
                isVisible = true,
                count = 0
            )
        )

        val result = runBlocking {
            reducer.reduce(
                state = nonSelected,
                event = ControlPanelMachine.Event.MultiSelect.OnEnter()
            )
        }

        assertEquals(
            expected = selected,
            actual = result
        )
    }

    @Test
    fun `should be three selected blocks when click on block`() {

        val id = MockDataFactory.randomUuid()

        val selectedZero = ControlPanelState(
            multiSelect = ControlPanelState.Toolbar.MultiSelect(
                isVisible = true,
                count = 0
            )
        )

        val expected = ControlPanelState(
            multiSelect = ControlPanelState.Toolbar.MultiSelect(
                isVisible = true,
                count = 3
            )
        )

        val result = runBlocking {
            reducer.reduce(
                state = selectedZero,
                event = ControlPanelMachine.Event.MultiSelect.OnBlockClick(count = 3)
            )
        }

        assertEquals(
            expected = expected,
            actual = result
        )
    }

    @Test
    fun `should update style toolbar state with italic true after selection changed`() =
        runBlocking {

            val block = Block(
                id = MockDataFactory.randomUuid(),
                children = emptyList(),
                content = Block.Content.Text(
                    text = "Foo Bar",
                    style = Block.Content.Text.Style.P,
                    marks = listOf(
                        Block.Content.Text.Mark(
                            range = IntRange(0, 2),
                            type = Block.Content.Text.Mark.Type.BOLD
                        ),
                        Block.Content.Text.Mark(
                            range = IntRange(4, 6),
                            type = Block.Content.Text.Mark.Type.ITALIC
                        )
                    )
                ),
                fields = Block.Fields.empty()
            )

            val selectionFirst = IntRange(0, 2)
            val selectionSecond = IntRange(4, 6)

            val given = ControlPanelState(
                styleTextToolbar = ControlPanelState.Toolbar.Styling.reset(),
                navigationToolbar = ControlPanelState.Toolbar.Navigation.reset(),
                mainToolbar = ControlPanelState.Toolbar.Main.reset(),
                multiSelect = ControlPanelState.Toolbar.MultiSelect.reset(),
                mentionToolbar = ControlPanelState.Toolbar.MentionToolbar.reset(),
                slashWidget = ControlPanelState.Toolbar.SlashWidget.reset(),
                markupMainToolbar = ControlPanelState.Toolbar.MarkupMainToolbar(
                    isVisible = true,
                    style = MarkupStyleDescriptor.Default(
                        isBold = true,
                        isItalic = false,
                        isCode = false,
                        isLinked = false,
                        isStrikethrough = false,
                        markupTextColor = "yellow",
                        markupUrl = null,
                        markupHighlightColor = "red",
                        range = IntRange(0, 2),
                        blockBackroundColor = null,
                        blockTextColor = null
                    ),
                    supportedTypes = listOf(
                        Markup.Type.BOLD,
                        Markup.Type.ITALIC,
                        Markup.Type.STRIKETHROUGH,
                        Markup.Type.KEYBOARD,
                        Markup.Type.LINK
                    )
                )
            )

            val result = runBlocking {
                reducer.reduce(
                    state = given,
                    event = ControlPanelMachine.Event.OnSelectionChanged(
                        selection = selectionSecond,
                        target = block
                    )
                )
            }

            val expected = ControlPanelState(
                navigationToolbar = ControlPanelState.Toolbar.Navigation(
                    isVisible = false
                ),
                mainToolbar = ControlPanelState.Toolbar.Main(
                    isVisible = false
                ),
                styleTextToolbar = ControlPanelState.Toolbar.Styling.reset(),
                multiSelect = ControlPanelState.Toolbar.MultiSelect(
                    isVisible = false
                ),
                mentionToolbar = ControlPanelState.Toolbar.MentionToolbar(
                    isVisible = false,
                    cursorCoordinate = null,
                    mentionFilter = null,
                    mentionFrom = null
                ),
                slashWidget = ControlPanelState.Toolbar.SlashWidget(
                    isVisible = false
                ),
                markupMainToolbar = ControlPanelState.Toolbar.MarkupMainToolbar(
                    isVisible = true,
                    style = MarkupStyleDescriptor.Default(
                        isBold = false,
                        isItalic = true,
                        isCode = false,
                        isLinked = false,
                        isStrikethrough = false,
                        markupTextColor = null,
                        markupUrl = null,
                        markupHighlightColor = null,
                        range = IntRange(4, 6),
                        blockBackroundColor = null,
                        blockTextColor = null
                    ),
                    supportedTypes = listOf(
                        Markup.Type.BOLD,
                        Markup.Type.ITALIC,
                        Markup.Type.STRIKETHROUGH,
                        Markup.Type.KEYBOARD,
                        Markup.Type.LINK
                    )
                )
            )
            assertEquals(
                expected = expected,
                actual = result
            )
        }

    @Test
    fun `should show style toolbar in markup mode after main toolbar style clicked`() {

        val id = MockDataFactory.randomUuid()

        val block = Block(
            id = id,
            children = emptyList(),
            content = Block.Content.Text(
                text = "Foo Bar",
                style = Block.Content.Text.Style.P,
                marks = listOf(
                    Block.Content.Text.Mark(
                        range = 0..3,
                        type = Block.Content.Text.Mark.Type.BOLD
                    )
                ),
                color = "yellow",
                align = Block.Align.AlignLeft
            ),
            fields = Block.Fields.empty(),
            backgroundColor = "red"
        )

        runBlocking {
            reducer.reduce(
                state = ControlPanelState.init(),
                event = ControlPanelMachine.Event.OnFocusChanged(
                    id = id,
                    style = Block.Content.Text.Style.P
                )
            )
        }

        val given = ControlPanelState(
            mainToolbar = ControlPanelState.Toolbar.Main(
                isVisible = true
            )
        )

        runBlocking {
            reducer.reduce(
                state = given,
                event = ControlPanelMachine.Event.OnSelectionChanged(
                    selection = IntRange(0, 3),
                    target = paragraph
                )
            )
        }

        val result = runBlocking {
            reducer.reduce(
                state = given,
                event = ControlPanelMachine.Event.StylingToolbar.OnUpdateTextToolbar(
                    state = StyleToolbarState.Text(textStyle = Block.Content.Text.Style.P)
                )
            )
        }

        val expected = ControlPanelState(
            navigationToolbar = ControlPanelState.Toolbar.Navigation(
                isVisible = false
            ),
            mainToolbar = ControlPanelState.Toolbar.Main(
                isVisible = false
            ),
            styleTextToolbar = ControlPanelState.Toolbar.Styling(
                state = StyleToolbarState.Text(Block.Content.Text.Style.P),
                isVisible = true
            ),
            multiSelect = ControlPanelState.Toolbar.MultiSelect(
                isVisible = false
            ),
            mentionToolbar = ControlPanelState.Toolbar.MentionToolbar.reset(),
            slashWidget = ControlPanelState.Toolbar.SlashWidget.reset()
        )
        assertEquals(
            expected = expected,
            actual = result
        )
    }

    @Test
    fun `should show style toolbar in block mode after main toolbar style clicked`() {

        val id = MockDataFactory.randomUuid()

        val block = Block(
            id = id,
            children = emptyList(),
            content = Block.Content.Text(
                text = "Foo Bar",
                style = Block.Content.Text.Style.BULLET,
                marks = listOf(
                    Block.Content.Text.Mark(
                        range = 0..3,
                        type = Block.Content.Text.Mark.Type.BOLD
                    )
                ),
                color = "yellow",
                align = Block.Align.AlignCenter
            ),
            fields = Block.Fields.empty(),
            backgroundColor = "red"
        )

        runBlocking {
            reducer.reduce(
                state = ControlPanelState.init(),
                event = ControlPanelMachine.Event.OnFocusChanged(
                    id = id,
                    style = Block.Content.Text.Style.BULLET
                )
            )
        }

        runBlocking {
            reducer.reduce(
                state = ControlPanelState.init(),
                event = ControlPanelMachine.Event.OnSelectionChanged(
                    selection = IntRange(1, 1),
                    target = paragraph
                )
            )
        }

        val given = ControlPanelState(
            mainToolbar = ControlPanelState.Toolbar.Main(
                isVisible = true
            )
        )

        val result = runBlocking {
            reducer.reduce(
                state = given,
                event = ControlPanelMachine.Event.StylingToolbar.OnUpdateTextToolbar(
                    state = StyleToolbarState.Text(textStyle = Block.Content.Text.Style.BULLET)
                )
            )
        }

        val expected = ControlPanelState(
            styleTextToolbar = ControlPanelState.Toolbar.Styling(
                state = StyleToolbarState.Text(textStyle = TextStyle.BULLET),
                isVisible = true
            )
        )
        assertEquals(
            expected = expected,
            actual = result
        )
    }

    @Test
    fun `should update markup toolbar when selection is changed`() =
        runBlocking {

            val id = MockDataFactory.randomUuid()

            Block(
                id = id,
                children = emptyList(),
                content = Block.Content.Text(
                    text = "Foo Bar",
                    style = Block.Content.Text.Style.P,
                    marks = listOf(
                        Block.Content.Text.Mark(
                            range = 0..3,
                            type = Block.Content.Text.Mark.Type.BOLD
                        )
                    ),
                    color = "yellow",
                    align = Block.Align.AlignLeft
                ),
                fields = Block.Fields.empty(),
                backgroundColor = "red"
            )

            //Focus block
            val stateAfterFocus = reducer.reduce(
                state = ControlPanelState.init(),
                event = ControlPanelMachine.Event.OnFocusChanged(
                    id = id,
                    style = Block.Content.Text.Style.P
                )
            )

            assertEquals(
                ControlPanelState.init().copy(
                    navigationToolbar = ControlPanelState.Toolbar.Navigation.reset(),
                    mainToolbar = ControlPanelState.Toolbar.Main(true)
                ), stateAfterFocus
            )

            val stateSelectedFirst = reducer.reduce(
                state = stateAfterFocus,
                event = ControlPanelMachine.Event.OnSelectionChanged(
                    selection = IntRange(3, 3),
                    target = paragraph
                )
            )

            //Select Foo
            val stateSelectedSecond = reducer.reduce(
                state = stateSelectedFirst,
                event = ControlPanelMachine.Event.OnSelectionChanged(
                    selection = IntRange(0, 3),
                    target = paragraph
                )
            )

            val result = reducer.reduce(
                state = stateSelectedSecond,
                event = ControlPanelMachine.Event.OnSelectionChanged(
                    selection = IntRange(0, 7),
                    target = paragraph
                )
            )

            val expected = ControlPanelState(
                markupMainToolbar = ControlPanelState.Toolbar.MarkupMainToolbar(
                    isVisible = true,
                    style = MarkupStyleDescriptor.Default(
                        isBold = false,
                        isItalic = false,
                        isCode = false,
                        isLinked = false,
                        isStrikethrough = false,
                        markupTextColor = null,
                        markupUrl = null,
                        markupHighlightColor = null,
                        range = IntRange(0, 7),
                        blockBackroundColor = null,
                        blockTextColor = null
                    ),
                    supportedTypes = listOf(
                        Markup.Type.BOLD,
                        Markup.Type.ITALIC,
                        Markup.Type.STRIKETHROUGH,
                        Markup.Type.KEYBOARD,
                        Markup.Type.LINK
                    )
                )
            )
            assertEquals(
                expected = expected,
                actual = result
            )
        }

    @Test
    fun `state should have visible navigation toolbar on init`() {

        val given = ControlPanelState.init()

        val expected = given.copy(
            mainToolbar = ControlPanelState.Toolbar.Main(
                isVisible = false
            ),
            navigationToolbar = ControlPanelState.Toolbar.Navigation(
                isVisible = true
            )
        )

        assertEquals(
            expected = expected,
            actual = given
        )
    }

    @Test
    fun `state should have visible navigation toolbar on clear focus`() {

        val stateOnInit = ControlPanelState.init()

        val eventFocus = ControlPanelMachine.Event.OnFocusChanged(
            id = MockDataFactory.randomUuid(),
            style = Block.Content.Text.Style.P
        )

        val stateAfterFocus = runBlocking {
            reducer.reduce(
                state = stateOnInit,
                event = eventFocus
            )
        }

        val eventClearFocus = ControlPanelMachine.Event.OnClearFocusClicked

        val stateAfterClearFocus = runBlocking {
            reducer.reduce(
                state = stateAfterFocus,
                event = eventClearFocus
            )
        }

        val stateAfterClearFocusExpected = ControlPanelState.init().copy(
            navigationToolbar = ControlPanelState.Toolbar.Navigation(
                isVisible = true
            )
        )

        assertEquals(
            expected = stateAfterClearFocusExpected,
            actual = stateAfterClearFocus
        )
    }

    @Test
    fun `state should have visible navigation toolbar on exit multi select `() {

        val stateOnInit = ControlPanelState.init()

        val eventFocus = ControlPanelMachine.Event.OnFocusChanged(
            id = MockDataFactory.randomUuid(),
            style = Block.Content.Text.Style.P
        )

        val stateAfterFocus = runBlocking {
            reducer.reduce(
                state = stateOnInit,
                event = eventFocus
            )
        }

        val stateAfterEnterMultiSelect = runBlocking {
            reducer.reduce(
                state = stateAfterFocus,
                event = ControlPanelMachine.Event.MultiSelect.OnEnter()
            )
        }

        val stateAfterExitMultiSelect = runBlocking {
            reducer.reduce(
                state = stateAfterEnterMultiSelect,
                event = ControlPanelMachine.Event.MultiSelect.OnExit
            )
        }

        val stateAfterExitMultiSelectExpected = ControlPanelState.init().copy(
            navigationToolbar = ControlPanelState.Toolbar.Navigation(
                isVisible = true
            )
        )

        assertEquals(
            expected = stateAfterExitMultiSelectExpected,
            actual = stateAfterExitMultiSelect
        )
    }
}