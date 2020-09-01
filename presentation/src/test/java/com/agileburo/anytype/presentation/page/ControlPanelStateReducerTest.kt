package com.agileburo.anytype.presentation.page

import MockDataFactory
import com.agileburo.anytype.core_ui.common.Alignment
import com.agileburo.anytype.core_ui.common.Markup
import com.agileburo.anytype.core_ui.features.page.styling.StylingMode
import com.agileburo.anytype.core_ui.features.page.styling.StylingType
import com.agileburo.anytype.core_ui.model.StyleConfig
import com.agileburo.anytype.core_ui.state.ControlPanelState
import com.agileburo.anytype.domain.block.model.Block
import com.agileburo.anytype.presentation.util.CoroutinesTestRule
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals


class ControlPanelStateReducerTest {

    @get:Rule
    val rule = CoroutinesTestRule()

    private val reducer = ControlPanelMachine.Reducer()

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
            focus = ControlPanelState.Focus(
                id = event.id,
                type = ControlPanelState.Focus.Type.P
            ),
            mainToolbar = ControlPanelState.Toolbar.Main(
                isVisible = true
            )
        )

        assertEquals(
            expected = expected,
            actual = actual
        )
    }

    @Test
    fun `state should hide mentions when cursor before mentions start and widget is visible`() {

        val id = MockDataFactory.randomUuid()

        val given = ControlPanelState(
            focus = ControlPanelState.Focus(
                id = id,
                type = ControlPanelState.Focus.Type.P
            ),
            mainToolbar = ControlPanelState.Toolbar.Main(
                isVisible = true
            ),
            stylingToolbar = ControlPanelState.Toolbar.Styling(
                isVisible = false,
                mode = null
            ),
            multiSelect = ControlPanelState.Toolbar.MultiSelect(
                isVisible = false
            ),
            mentionToolbar = ControlPanelState.Toolbar.MentionToolbar(
                isVisible = true,
                cursorCoordinate = 333,
                mentionFilter = "start",
                mentionFrom = 10
            )
        )

        val event = ControlPanelMachine.Event.OnSelectionChanged(
            selection = IntRange(9, 9),
            target = id
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
            )
        )

        assertEquals(
            expected = expected,
            actual = actual
        )
    }

    @Test
    fun `state should not hide mentions when cursor after mention start`() {

        val id = MockDataFactory.randomUuid()

        val given = ControlPanelState(
            focus = ControlPanelState.Focus(
                id = id,
                type = ControlPanelState.Focus.Type.P
            ),
            mainToolbar = ControlPanelState.Toolbar.Main(
                isVisible = true
            ),
            stylingToolbar = ControlPanelState.Toolbar.Styling(
                isVisible = false,
                mode = null
            ),
            multiSelect = ControlPanelState.Toolbar.MultiSelect(
                isVisible = false
            ),
            mentionToolbar = ControlPanelState.Toolbar.MentionToolbar(
                isVisible = true,
                cursorCoordinate = 333,
                mentionFilter = "start",
                mentionFrom = 10
            )
        )

        val event = ControlPanelMachine.Event.OnSelectionChanged(
            selection = IntRange(11, 11),
            target = id
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

        val id = MockDataFactory.randomUuid()

        val given = ControlPanelState(
            focus = ControlPanelState.Focus(
                id = id,
                type = ControlPanelState.Focus.Type.P
            ),
            mainToolbar = ControlPanelState.Toolbar.Main(
                isVisible = true
            ),
            stylingToolbar = ControlPanelState.Toolbar.Styling(
                isVisible = false,
                mode = null
            ),
            multiSelect = ControlPanelState.Toolbar.MultiSelect(
                isVisible = false
            ),
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
            focus = ControlPanelState.Focus(
                id = event.id,
                type = ControlPanelState.Focus.Type.P
            ),
            mentionToolbar = ControlPanelState.Toolbar.MentionToolbar(
                isVisible = false,
                cursorCoordinate = null,
                mentionFilter = null,
                mentionFrom = null
            )
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
            focus = ControlPanelState.Focus(
                id = id,
                type = ControlPanelState.Focus.Type.P
            ),
            mainToolbar = ControlPanelState.Toolbar.Main(
                isVisible = true
            ),
            stylingToolbar = ControlPanelState.Toolbar.Styling(
                isVisible = false,
                mode = null
            ),
            multiSelect = ControlPanelState.Toolbar.MultiSelect(
                isVisible = false
            ),
            mentionToolbar = ControlPanelState.Toolbar.MentionToolbar(
                isVisible = true,
                cursorCoordinate = 333,
                mentionFilter = "start",
                mentionFrom = 10
            )
        )

        val event = ControlPanelMachine.Event.OnSelectionChanged(
            selection = IntRange(8, 8),
            target = id
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
            focus = ControlPanelState.Focus(
                id = MockDataFactory.randomUuid(),
                type = ControlPanelState.Focus.Type.P
            ),
            mainToolbar = ControlPanelState.Toolbar.Main(
                isVisible = true
            ),
            stylingToolbar = ControlPanelState.Toolbar.Styling(
                isVisible = false,
                mode = null
            ),
            multiSelect = ControlPanelState.Toolbar.MultiSelect(
                isVisible = false
            ),
            mentionToolbar = ControlPanelState.Toolbar.MentionToolbar(
                isVisible = false,
                cursorCoordinate = null,
                mentionFilter = null,
                mentionFrom = null
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
            focus = ControlPanelState.Focus(
                id = event.id,
                type = ControlPanelState.Focus.Type.P
            )
        )

        assertEquals(
            expected = expected,
            actual = actual
        )
    }

    @Test
    fun `should return to initial state when focus is cleared`() {

        val given = ControlPanelState(
            focus = ControlPanelState.Focus(
                id = MockDataFactory.randomUuid(),
                type = ControlPanelState.Focus.Type.P
            ),
            mainToolbar = ControlPanelState.Toolbar.Main(
                isVisible = true
            ),
            stylingToolbar = ControlPanelState.Toolbar.Styling(
                isVisible = false,
                mode = null
            ),
            multiSelect = ControlPanelState.Toolbar.MultiSelect(
                isVisible = false
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
    fun `should not show toolbar after focus is cleared and selections continue to change`() {

        val id = MockDataFactory.randomUuid()

        val given = ControlPanelState(
            focus = ControlPanelState.Focus(
                id = id,
                type = ControlPanelState.Focus.Type.P
            ),
            mainToolbar = ControlPanelState.Toolbar.Main(
                isVisible = true
            ),
            stylingToolbar = ControlPanelState.Toolbar.Styling(
                isVisible = false,
                mode = null
            ),
            multiSelect = ControlPanelState.Toolbar.MultiSelect(
                isVisible = false
            ),
            mentionToolbar = ControlPanelState.Toolbar.MentionToolbar(
                isVisible = false,
                cursorCoordinate = null,
                mentionFilter = null,
                mentionFrom = null
            )
        )

        val cleared = runBlocking {
            reducer.reduce(
                state = given,
                event = ControlPanelMachine.Event.OnClearFocusClicked
            )
        }

        val result = runBlocking {
            reducer.reduce(
                state = cleared,
                event = ControlPanelMachine.Event.OnSelectionChanged(
                    selection = 0..0,
                    target = id
                )
            )
        }

        val expected = ControlPanelState.init()

        assertEquals(
            expected = expected,
            actual = result
        )
    }

    @Test
    fun `should be zero selected blocks when enter selected state`() {

        val id = MockDataFactory.randomUuid()

        val nonSelected = ControlPanelState(
            focus = ControlPanelState.Focus(
                id = id,
                type = ControlPanelState.Focus.Type.P
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
            focus = ControlPanelState.Focus(
                id = id,
                type = ControlPanelState.Focus.Type.P
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
                count = 0
            ),
            mentionToolbar = ControlPanelState.Toolbar.MentionToolbar(
                isVisible = false,
                cursorCoordinate = null,
                mentionFilter = null,
                mentionFrom = null
            )
        )

        val result = runBlocking {
            reducer.reduce(
                state = nonSelected,
                event = ControlPanelMachine.Event.MultiSelect.OnEnter
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
            focus = ControlPanelState.Focus(
                id = id,
                type = ControlPanelState.Focus.Type.P
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
                count = 0
            ),
            mentionToolbar = ControlPanelState.Toolbar.MentionToolbar(
                isVisible = false,
                cursorCoordinate = null,
                mentionFilter = null,
                mentionFrom = null
            )
        )

        val expected = ControlPanelState(
            focus = ControlPanelState.Focus(
                id = id,
                type = ControlPanelState.Focus.Type.P
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
                count = 3
            ),
            mentionToolbar = ControlPanelState.Toolbar.MentionToolbar(
                isVisible = false,
                cursorCoordinate = null,
                mentionFilter = null,
                mentionFrom = null
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
    fun `should update style toolbar state with italic true after selection changed`() {

        val id = MockDataFactory.randomUuid()
        val selectionFirst = IntRange(0, 2)
        val selectionSecond = IntRange(4, 6)

        val given = ControlPanelState(
            focus = ControlPanelState.Focus(
                id = id,
                type = ControlPanelState.Focus.Type.P
            ),
            mainToolbar = ControlPanelState.Toolbar.Main(
                isVisible = false
            ),
            stylingToolbar = ControlPanelState.Toolbar.Styling(
                isVisible = true,
                target = ControlPanelState.Toolbar.Styling.Target(
                    text = "Foo Bar",
                    color = "yellow",
                    background = "red",
                    alignment = Alignment.START,
                    marks = listOf(
                        Markup.Mark(0, 3, Markup.Type.BOLD),
                        Markup.Mark(4, 7, Markup.Type.ITALIC)
                    )
                ),
                config = StyleConfig(
                    visibleTypes = listOf(
                        StylingType.STYLE,
                        StylingType.TEXT_COLOR,
                        StylingType.BACKGROUND
                    ),
                    enabledAlignment = listOf(),
                    enabledMarkup = listOf(
                        Markup.Type.BOLD,
                        Markup.Type.ITALIC,
                        Markup.Type.STRIKETHROUGH,
                        Markup.Type.KEYBOARD,
                        Markup.Type.LINK
                    )
                ),
                props = ControlPanelState.Toolbar.Styling.Props(
                    isBold = true,
                    isItalic = false,
                    isStrikethrough = false,
                    isCode = false,
                    isLinked = false,
                    alignment = Alignment.START,
                    color = "yellow",
                    background = "red"
                ),
                mode = StylingMode.MARKUP
            ),
            multiSelect = ControlPanelState.Toolbar.MultiSelect(
                isVisible = false
            ),
            mentionToolbar = ControlPanelState.Toolbar.MentionToolbar(
                isVisible = false,
                cursorCoordinate = null,
                mentionFilter = null,
                mentionFrom = null
            )
        )

        val result = runBlocking {
            reducer.mSelection = selectionFirst
            reducer.reduce(
                state = given,
                event = ControlPanelMachine.Event.OnSelectionChanged(
                    selection = selectionSecond,
                    target = id
                )
            )
        }

        val expected = ControlPanelState(
            focus = ControlPanelState.Focus(
                id = id,
                type = ControlPanelState.Focus.Type.P
            ),
            mainToolbar = ControlPanelState.Toolbar.Main(
                isVisible = false
            ),
            stylingToolbar = ControlPanelState.Toolbar.Styling(
                isVisible = true,
                target = ControlPanelState.Toolbar.Styling.Target(
                    text = "Foo Bar",
                    color = "yellow",
                    background = "red",
                    alignment = Alignment.START,
                    marks = listOf(
                        Markup.Mark(0, 3, Markup.Type.BOLD),
                        Markup.Mark(4, 7, Markup.Type.ITALIC)
                    )
                ),
                config = StyleConfig(
                    visibleTypes = listOf(
                        StylingType.STYLE,
                        StylingType.TEXT_COLOR,
                        StylingType.BACKGROUND
                    ),
                    enabledAlignment = listOf(),
                    enabledMarkup = listOf(
                        Markup.Type.BOLD,
                        Markup.Type.ITALIC,
                        Markup.Type.STRIKETHROUGH,
                        Markup.Type.KEYBOARD,
                        Markup.Type.LINK
                    )
                ),
                props = ControlPanelState.Toolbar.Styling.Props(
                    isBold = false,
                    isItalic = true,
                    isStrikethrough = false,
                    isCode = false,
                    isLinked = false,
                    alignment = Alignment.START,
                    color = "yellow",
                    background = "red"
                ),
                mode = StylingMode.MARKUP
            ),
            multiSelect = ControlPanelState.Toolbar.MultiSelect(
                isVisible = false
            ),
            mentionToolbar = ControlPanelState.Toolbar.MentionToolbar(
                isVisible = false,
                cursorCoordinate = null,
                mentionFilter = null,
                mentionFrom = null
            )
        )
        assertEquals(
            expected = expected,
            actual = result
        )
    }

    @Test
    fun `should update style toolbar state without markup after selection changed`() {

        val id = MockDataFactory.randomUuid()
        val selectionFirst = IntRange(0, 2)
        val selectionSecond = IntRange(4, 6)

        val given = ControlPanelState(
            focus = ControlPanelState.Focus(
                id = id,
                type = ControlPanelState.Focus.Type.P
            ),
            mainToolbar = ControlPanelState.Toolbar.Main(
                isVisible = false
            ),
            stylingToolbar = ControlPanelState.Toolbar.Styling(
                isVisible = true,
                target = ControlPanelState.Toolbar.Styling.Target(
                    text = "Foo Bar",
                    color = "yellow",
                    background = "red",
                    alignment = Alignment.START,
                    marks = listOf(
                        Markup.Mark(0, 3, Markup.Type.BOLD)
                    )
                ),
                config = StyleConfig(
                    visibleTypes = listOf(
                        StylingType.STYLE,
                        StylingType.TEXT_COLOR,
                        StylingType.BACKGROUND
                    ),
                    enabledAlignment = listOf(),
                    enabledMarkup = listOf(
                        Markup.Type.BOLD,
                        Markup.Type.ITALIC,
                        Markup.Type.STRIKETHROUGH,
                        Markup.Type.KEYBOARD,
                        Markup.Type.LINK
                    )
                ),
                props = ControlPanelState.Toolbar.Styling.Props(
                    isBold = true,
                    isItalic = false,
                    isStrikethrough = false,
                    isCode = false,
                    isLinked = false,
                    alignment = Alignment.START,
                    color = "yellow",
                    background = "red"
                ),
                mode = StylingMode.MARKUP
            ),
            multiSelect = ControlPanelState.Toolbar.MultiSelect(
                isVisible = false
            ),
            mentionToolbar = ControlPanelState.Toolbar.MentionToolbar(
                isVisible = false,
                cursorCoordinate = null,
                mentionFilter = null,
                mentionFrom = null
            )
        )

        val result = runBlocking {
            reducer.mSelection = selectionFirst
            reducer.reduce(
                state = given,
                event = ControlPanelMachine.Event.OnSelectionChanged(
                    selection = selectionSecond,
                    target = id
                )
            )
        }

        val expected = ControlPanelState(
            focus = ControlPanelState.Focus(
                id = id,
                type = ControlPanelState.Focus.Type.P
            ),
            mainToolbar = ControlPanelState.Toolbar.Main(
                isVisible = false
            ),
            stylingToolbar = ControlPanelState.Toolbar.Styling(
                isVisible = true,
                target = ControlPanelState.Toolbar.Styling.Target(
                    text = "Foo Bar",
                    color = "yellow",
                    background = "red",
                    alignment = Alignment.START,
                    marks = listOf(
                        Markup.Mark(0, 3, Markup.Type.BOLD)
                    )
                ),
                config = StyleConfig(
                    visibleTypes = listOf(
                        StylingType.STYLE,
                        StylingType.TEXT_COLOR,
                        StylingType.BACKGROUND
                    ),
                    enabledAlignment = listOf(),
                    enabledMarkup = listOf(
                        Markup.Type.BOLD,
                        Markup.Type.ITALIC,
                        Markup.Type.STRIKETHROUGH,
                        Markup.Type.KEYBOARD,
                        Markup.Type.LINK
                    )
                ),
                props = ControlPanelState.Toolbar.Styling.Props(
                    isBold = false,
                    isItalic = false,
                    isStrikethrough = false,
                    isCode = false,
                    isLinked = false,
                    alignment = Alignment.START,
                    color = "yellow",
                    background = "red"
                ),
                mode = StylingMode.MARKUP
            ),
            multiSelect = ControlPanelState.Toolbar.MultiSelect(
                isVisible = false
            ),
            mentionToolbar = ControlPanelState.Toolbar.MentionToolbar(
                isVisible = false,
                cursorCoordinate = null,
                mentionFilter = null,
                mentionFrom = null
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
                backgroundColor = "red",
                align = Block.Align.AlignLeft
            ),
            fields = Block.Fields.empty()
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
            focus = ControlPanelState.Focus(
                id = id,
                type = ControlPanelState.Focus.Type.P
            ),
            mainToolbar = ControlPanelState.Toolbar.Main(
                isVisible = true
            ),
            stylingToolbar = ControlPanelState.Toolbar.Styling.reset(),
            multiSelect = ControlPanelState.Toolbar.MultiSelect(
                isVisible = false
            ),
            mentionToolbar = ControlPanelState.Toolbar.MentionToolbar.reset()
        )

        runBlocking {
            reducer.reduce(
                state = given,
                event = ControlPanelMachine.Event.OnSelectionChanged(
                    selection = IntRange(0, 3),
                    target = id
                )
            )
        }

        val result = runBlocking {
            reducer.reduce(
                state = given,
                event = ControlPanelMachine.Event.OnBlockActionToolbarStyleClicked(
                    target = block
                )
            )
        }

        val expected = ControlPanelState(
            focus = ControlPanelState.Focus(
                id = id,
                type = ControlPanelState.Focus.Type.P
            ),
            mainToolbar = ControlPanelState.Toolbar.Main(
                isVisible = false
            ),
            stylingToolbar = ControlPanelState.Toolbar.Styling(
                isVisible = true,
                target = ControlPanelState.Toolbar.Styling.Target(
                    text = "Foo Bar",
                    color = "yellow",
                    background = "red",
                    alignment = Alignment.START,
                    marks = listOf(
                        Markup.Mark(0, 3, Markup.Type.BOLD)
                    )
                ),
                config = StyleConfig(
                    visibleTypes = listOf(
                        StylingType.STYLE,
                        StylingType.TEXT_COLOR,
                        StylingType.BACKGROUND
                    ),
                    enabledAlignment = listOf(),
                    enabledMarkup = listOf(
                        Markup.Type.BOLD,
                        Markup.Type.ITALIC,
                        Markup.Type.STRIKETHROUGH,
                        Markup.Type.KEYBOARD,
                        Markup.Type.LINK
                    )
                ),
                props = ControlPanelState.Toolbar.Styling.Props(
                    isBold = true,
                    isItalic = false,
                    isStrikethrough = false,
                    isCode = false,
                    isLinked = false,
                    alignment = Alignment.START,
                    color = "yellow",
                    background = "red"
                ),
                mode = StylingMode.MARKUP
            ),
            multiSelect = ControlPanelState.Toolbar.MultiSelect(
                isVisible = false
            ),
            mentionToolbar = ControlPanelState.Toolbar.MentionToolbar.reset()
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
                style = Block.Content.Text.Style.P,
                marks = listOf(
                    Block.Content.Text.Mark(
                        range = 0..3,
                        type = Block.Content.Text.Mark.Type.BOLD
                    )
                ),
                color = "yellow",
                backgroundColor = "red",
                align = Block.Align.AlignCenter
            ),
            fields = Block.Fields.empty()
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

        runBlocking {
            reducer.reduce(
                state = ControlPanelState.init(),
                event = ControlPanelMachine.Event.OnSelectionChanged(
                    selection = IntRange(1, 1),
                    target = id
                )
            )
        }

        val given = ControlPanelState(
            focus = ControlPanelState.Focus(
                id = id,
                type = ControlPanelState.Focus.Type.P
            ),
            mainToolbar = ControlPanelState.Toolbar.Main(
                isVisible = true
            ),
            stylingToolbar = ControlPanelState.Toolbar.Styling.reset(),
            multiSelect = ControlPanelState.Toolbar.MultiSelect(
                isVisible = false
            ),
            mentionToolbar = ControlPanelState.Toolbar.MentionToolbar.reset()
        )

        val result = runBlocking {
            reducer.reduce(
                state = given,
                event = ControlPanelMachine.Event.OnBlockActionToolbarStyleClicked(
                    target = block
                )
            )
        }

        val expected = ControlPanelState(
            focus = ControlPanelState.Focus(
                id = id,
                type = ControlPanelState.Focus.Type.P
            ),
            mainToolbar = ControlPanelState.Toolbar.Main(
                isVisible = false
            ),
            stylingToolbar = ControlPanelState.Toolbar.Styling(
                isVisible = true,
                target = ControlPanelState.Toolbar.Styling.Target(
                    text = "Foo Bar",
                    color = "yellow",
                    background = "red",
                    alignment = Alignment.CENTER,
                    marks = listOf(
                        Markup.Mark(0, 3, Markup.Type.BOLD)
                    )
                ),
                config = StyleConfig(
                    visibleTypes = listOf(
                        StylingType.STYLE,
                        StylingType.TEXT_COLOR,
                        StylingType.BACKGROUND
                    ),
                    enabledAlignment = listOf(
                        Alignment.START,
                        Alignment.CENTER,
                        Alignment.END
                    ),
                    enabledMarkup = listOf(
                        Markup.Type.BOLD,
                        Markup.Type.ITALIC,
                        Markup.Type.STRIKETHROUGH,
                        Markup.Type.KEYBOARD,
                        Markup.Type.LINK
                    )
                ),
                props = ControlPanelState.Toolbar.Styling.Props(
                    isBold = false,
                    isItalic = false,
                    isStrikethrough = false,
                    isCode = false,
                    isLinked = false,
                    alignment = Alignment.CENTER,
                    color = "yellow",
                    background = "red"
                ),
                mode = StylingMode.BLOCK
            ),
            multiSelect = ControlPanelState.Toolbar.MultiSelect(
                isVisible = false
            ),
            mentionToolbar = ControlPanelState.Toolbar.MentionToolbar.reset()
        )
        assertEquals(
            expected = expected,
            actual = result
        )
    }

    @Test
    fun `should close style toolbar when selection is zero`() {

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
                backgroundColor = "red",
                align = Block.Align.AlignLeft
            ),
            fields = Block.Fields.empty()
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

        runBlocking {
            reducer.reduce(
                state = ControlPanelState.init(),
                event = ControlPanelMachine.Event.OnSelectionChanged(
                    selection = IntRange(0, 3),
                    target = id
                )
            )
        }

        val given = ControlPanelState(
            focus = ControlPanelState.Focus(
                id = id,
                type = ControlPanelState.Focus.Type.P
            ),
            mainToolbar = ControlPanelState.Toolbar.Main(
                isVisible = true
            ),
            stylingToolbar = ControlPanelState.Toolbar.Styling.reset(),
            multiSelect = ControlPanelState.Toolbar.MultiSelect(
                isVisible = false
            ),
            mentionToolbar = ControlPanelState.Toolbar.MentionToolbar.reset()
        )

        runBlocking {
            reducer.reduce(
                state = given,
                event = ControlPanelMachine.Event.OnEditorContextMenuStyleClicked(
                    selection = IntRange(0, 3),
                    target = block
                )
            )
        }

        val result = runBlocking {
            reducer.reduce(
                state = given,
                event = ControlPanelMachine.Event.OnSelectionChanged(
                    selection = IntRange(0, 7),
                    target = id
                )
            )
        }

        val expected = ControlPanelState(
            focus = ControlPanelState.Focus(
                id = id,
                type = ControlPanelState.Focus.Type.P
            ),
            mainToolbar = ControlPanelState.Toolbar.Main(
                isVisible = true
            ),
            stylingToolbar = ControlPanelState.Toolbar.Styling.reset(),
            multiSelect = ControlPanelState.Toolbar.MultiSelect(
                isVisible = false
            ),
            mentionToolbar = ControlPanelState.Toolbar.MentionToolbar.reset()
        )
        assertEquals(
            expected = expected,
            actual = result
        )
    }

    @Test
    fun `should update style toolbar when selection is changed and not zero or empty`() {

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
                backgroundColor = "red",
                align = Block.Align.AlignLeft
            ),
            fields = Block.Fields.empty()
        )

        //Focus block
        runBlocking {
            reducer.reduce(
                state = ControlPanelState.init(),
                event = ControlPanelMachine.Event.OnFocusChanged(
                    id = id,
                    style = Block.Content.Text.Style.P
                )
            )
        }

        //Select Foo
        runBlocking {
            reducer.reduce(
                state = ControlPanelState.init(),
                event = ControlPanelMachine.Event.OnSelectionChanged(
                    selection = IntRange(0, 3),
                    target = id
                )
            )
        }

        val given = ControlPanelState(
            focus = ControlPanelState.Focus(
                id = id,
                type = ControlPanelState.Focus.Type.P
            ),
            mainToolbar = ControlPanelState.Toolbar.Main(
                isVisible = true
            ),
            stylingToolbar = ControlPanelState.Toolbar.Styling.reset(),
            multiSelect = ControlPanelState.Toolbar.MultiSelect(
                isVisible = false
            ),
            mentionToolbar = ControlPanelState.Toolbar.MentionToolbar.reset()
        )


        runBlocking {
            reducer.reduce(
                state = given,
                event = ControlPanelMachine.Event.OnSelectionChanged(
                    selection = IntRange(0, 3),
                    target = id
                )
            )
        }

        runBlocking {
            reducer.reduce(
                state = given,
                event = ControlPanelMachine.Event.OnEditorContextMenuStyleClicked(
                    selection = IntRange(0, 3),
                    target = block
                )
            )
        }

        val result = runBlocking {
            reducer.reduce(
                state = ControlPanelState(
                    focus = ControlPanelState.Focus(
                        id = id,
                        type = ControlPanelState.Focus.Type.P
                    ),
                    mainToolbar = ControlPanelState.Toolbar.Main(
                        isVisible = false
                    ),
                    stylingToolbar = ControlPanelState.Toolbar.Styling(
                        isVisible = true,
                        target = ControlPanelState.Toolbar.Styling.Target(
                            text = "Foo Bar",
                            color = "yellow",
                            background = "red",
                            alignment = Alignment.CENTER,
                            marks = listOf(
                                Markup.Mark(0, 3, Markup.Type.BOLD)
                            )
                        ),
                        config = StyleConfig(
                            visibleTypes = listOf(
                                StylingType.STYLE,
                                StylingType.TEXT_COLOR,
                                StylingType.BACKGROUND
                            ),
                            enabledAlignment = listOf(
                                Alignment.START,
                                Alignment.CENTER,
                                Alignment.END
                            ),
                            enabledMarkup = listOf(
                                Markup.Type.BOLD,
                                Markup.Type.ITALIC,
                                Markup.Type.STRIKETHROUGH,
                                Markup.Type.KEYBOARD,
                                Markup.Type.LINK
                            )
                        ),
                        props = ControlPanelState.Toolbar.Styling.Props(
                            isBold = true,
                            isItalic = false,
                            isStrikethrough = false,
                            isCode = false,
                            isLinked = false,
                            alignment = Alignment.CENTER,
                            color = "yellow",
                            background = "red"
                        ),
                        mode = StylingMode.MARKUP
                    ),
                    multiSelect = ControlPanelState.Toolbar.MultiSelect(
                        isVisible = false
                    ),
                    mentionToolbar = ControlPanelState.Toolbar.MentionToolbar.reset()
                ),
                event = ControlPanelMachine.Event.OnSelectionChanged(
                    selection = IntRange(0, 7),
                    target = id
                )
            )
        }

        val expected = ControlPanelState(
            focus = ControlPanelState.Focus(
                id = id,
                type = ControlPanelState.Focus.Type.P
            ),
            mainToolbar = ControlPanelState.Toolbar.Main(
                isVisible = false
            ),
            stylingToolbar = ControlPanelState.Toolbar.Styling(
                isVisible = true,
                target = ControlPanelState.Toolbar.Styling.Target(
                    text = "Foo Bar",
                    color = "yellow",
                    background = "red",
                    alignment = Alignment.CENTER,
                    marks = listOf(
                        Markup.Mark(0, 3, Markup.Type.BOLD)
                    )
                ),
                config = StyleConfig(
                    visibleTypes = listOf(
                        StylingType.STYLE,
                        StylingType.TEXT_COLOR,
                        StylingType.BACKGROUND
                    ),
                    enabledAlignment = listOf(
                        Alignment.START,
                        Alignment.CENTER,
                        Alignment.END
                    ),
                    enabledMarkup = listOf(
                        Markup.Type.BOLD,
                        Markup.Type.ITALIC,
                        Markup.Type.STRIKETHROUGH,
                        Markup.Type.KEYBOARD,
                        Markup.Type.LINK
                    )
                ),
                props = ControlPanelState.Toolbar.Styling.Props(
                    isBold = false,
                    isItalic = false,
                    isStrikethrough = false,
                    isCode = false,
                    isLinked = false,
                    alignment = Alignment.CENTER,
                    color = "yellow",
                    background = "red"
                ),
                mode = StylingMode.MARKUP
            ),
            multiSelect = ControlPanelState.Toolbar.MultiSelect(
                isVisible = false
            ),
            mentionToolbar = ControlPanelState.Toolbar.MentionToolbar.reset()
        )
        assertEquals(
            expected = expected,
            actual = result
        )
    }

    @Test
    fun `should reset style toolbar only when selection range is 0 on block that in focus`() {

        val id = MockDataFactory.randomUuid()

        val block = Block(
            id = id,
            children = emptyList(),
            content = Block.Content.Text(
                text = "Foo Bar",
                style = Block.Content.Text.Style.P,
                marks = listOf(),
                color = "yellow",
                backgroundColor = "red",
                align = Block.Align.AlignLeft
            ),
            fields = Block.Fields.empty()
        )

        //Focus block
        val afterFocusStateResult = runBlocking {
            reducer.reduce(
                state = ControlPanelState.init(),
                event = ControlPanelMachine.Event.OnFocusChanged(
                    id = id,
                    style = Block.Content.Text.Style.P
                )
            )
        }

        val afterFocusStateExpected = ControlPanelState(
            focus = ControlPanelState.Focus(
                id = id,
                type = ControlPanelState.Focus.Type.P
            ),
            mainToolbar = ControlPanelState.Toolbar.Main(
                isVisible = true
            ),
            stylingToolbar = ControlPanelState.Toolbar.Styling.reset(),
            multiSelect = ControlPanelState.Toolbar.MultiSelect(
                isVisible = false
            ),
            mentionToolbar = ControlPanelState.Toolbar.MentionToolbar.reset()
        )

        assertEquals(
            expected = afterFocusStateExpected,
            actual = afterFocusStateResult
        )

        //Select last position
        val afterSelectionStateResult = runBlocking {
            reducer.reduce(
                state = afterFocusStateResult,
                event = ControlPanelMachine.Event.OnSelectionChanged(
                    target = id,
                    selection = IntRange(6, 6)
                )
            )
        }

        val afterSelectionStateExpected = ControlPanelState(
            focus = ControlPanelState.Focus(
                id = id,
                type = ControlPanelState.Focus.Type.P
            ),
            mainToolbar = ControlPanelState.Toolbar.Main(
                isVisible = true
            ),
            stylingToolbar = ControlPanelState.Toolbar.Styling.reset(),
            multiSelect = ControlPanelState.Toolbar.MultiSelect(
                isVisible = false
            ),
            mentionToolbar = ControlPanelState.Toolbar.MentionToolbar.reset()
        )

        assertEquals(
            expected = afterSelectionStateExpected,
            actual = afterSelectionStateResult
        )

        //Click on [T] style button
        val afterShowStyleToolbarStateResult = runBlocking {
            reducer.reduce(
                state = afterSelectionStateExpected,
                event = ControlPanelMachine.Event.OnBlockActionToolbarStyleClicked(
                    target = block
                )
            )
        }

        val afterShowStyleToolbarStateExpected = ControlPanelState(
            focus = ControlPanelState.Focus(
                id = id,
                type = ControlPanelState.Focus.Type.P
            ),
            mainToolbar = ControlPanelState.Toolbar.Main(
                isVisible = false
            ),
            stylingToolbar = ControlPanelState.Toolbar.Styling(
                isVisible = true,
                target = ControlPanelState.Toolbar.Styling.Target(
                    text = "Foo Bar",
                    color = "yellow",
                    background = "red",
                    alignment = Alignment.START,
                    marks = listOf()
                ),
                config = StyleConfig(
                    visibleTypes = listOf(
                        StylingType.STYLE,
                        StylingType.TEXT_COLOR,
                        StylingType.BACKGROUND
                    ),
                    enabledAlignment = listOf(
                        Alignment.START,
                        Alignment.CENTER,
                        Alignment.END
                    ),
                    enabledMarkup = listOf(
                        Markup.Type.BOLD,
                        Markup.Type.ITALIC,
                        Markup.Type.STRIKETHROUGH,
                        Markup.Type.KEYBOARD,
                        Markup.Type.LINK
                    )
                ),
                props = ControlPanelState.Toolbar.Styling.Props(
                    isBold = false,
                    isItalic = false,
                    isStrikethrough = false,
                    isCode = false,
                    isLinked = false,
                    alignment = Alignment.START,
                    color = "yellow",
                    background = "red"
                ),
                mode = StylingMode.BLOCK
            ),
            multiSelect = ControlPanelState.Toolbar.MultiSelect(
                isVisible = false
            ),
            mentionToolbar = ControlPanelState.Toolbar.MentionToolbar.reset()
        )

        assertEquals(
            expected = afterShowStyleToolbarStateExpected,
            actual = afterShowStyleToolbarStateResult
        )

        //Selection on other block
        val result = runBlocking {
            reducer.reduce(
                state = afterShowStyleToolbarStateResult,
                event = ControlPanelMachine.Event.OnSelectionChanged(
                    target = MockDataFactory.randomUuid(),
                    selection = IntRange(0, 0)
                )
            )
        }

        val expected = ControlPanelState(
            focus = ControlPanelState.Focus(
                id = id,
                type = ControlPanelState.Focus.Type.P
            ),
            mainToolbar = ControlPanelState.Toolbar.Main(
                isVisible = false
            ),
            stylingToolbar = ControlPanelState.Toolbar.Styling(
                isVisible = true,
                target = ControlPanelState.Toolbar.Styling.Target(
                    text = "Foo Bar",
                    color = "yellow",
                    background = "red",
                    alignment = Alignment.START,
                    marks = listOf()
                ),
                config = StyleConfig(
                    visibleTypes = listOf(
                        StylingType.STYLE,
                        StylingType.TEXT_COLOR,
                        StylingType.BACKGROUND
                    ),
                    enabledAlignment = listOf(
                        Alignment.START,
                        Alignment.CENTER,
                        Alignment.END
                    ),
                    enabledMarkup = listOf(
                        Markup.Type.BOLD,
                        Markup.Type.ITALIC,
                        Markup.Type.STRIKETHROUGH,
                        Markup.Type.KEYBOARD,
                        Markup.Type.LINK
                    )
                ),
                props = ControlPanelState.Toolbar.Styling.Props(
                    isBold = false,
                    isItalic = false,
                    isStrikethrough = false,
                    isCode = false,
                    isLinked = false,
                    alignment = Alignment.START,
                    color = "yellow",
                    background = "red"
                ),
                mode = StylingMode.BLOCK
            ),
            multiSelect = ControlPanelState.Toolbar.MultiSelect(
                isVisible = false
            ),
            mentionToolbar = ControlPanelState.Toolbar.MentionToolbar.reset()
        )

        assertEquals(
            expected = expected,
            actual = result
        )
    }
}