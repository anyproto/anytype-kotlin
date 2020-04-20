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
}