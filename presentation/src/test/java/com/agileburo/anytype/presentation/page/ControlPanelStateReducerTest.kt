package com.agileburo.anytype.presentation.page

import MockDataFactory
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
                mode = null,
                type = null
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
            selection = IntRange(9,9)
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
                mode = null,
                type = null
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
            selection = IntRange(11,11)
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
            focus = ControlPanelState.Focus(
                id = MockDataFactory.randomUuid(),
                type = ControlPanelState.Focus.Type.P
            ),
            mainToolbar = ControlPanelState.Toolbar.Main(
                isVisible = true
            ),
            stylingToolbar = ControlPanelState.Toolbar.Styling(
                isVisible = false,
                mode = null,
                type = null
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
                mode = null,
                type = null
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
            selection = IntRange(8, 8)
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
                mode = null,
                type = null
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
                mode = null,
                type = null
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
                mode = null,
                type = null
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
                    selection = 0..0
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
                mode = null,
                type = null
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
                mode = null,
                type = null
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
                event = ControlPanelMachine.Event.OnEnterMultiSelectModeClicked
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
                mode = null,
                type = null
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
                mode = null,
                type = null
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
                event = ControlPanelMachine.Event.OnMultiSelectModeBlockClick(count = 3)
            )
        }

        assertEquals(
            expected = expected,
            actual = result
        )
    }
}