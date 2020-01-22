package com.agileburo.anytype.presentation.page

import MockDataFactory
import com.agileburo.anytype.core_ui.state.ControlPanelState
import com.agileburo.anytype.presentation.page.PageViewModel.ControlPanelMachine
import com.agileburo.anytype.presentation.util.CoroutinesTestRule
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals


class ControlPanelStateReducerTest {

    @get:Rule
    val rule = CoroutinesTestRule()

    private val reducer = ControlPanelMachine.Reducer()

    @Test
    fun `state should have visibile block toolbar and focus from the event`() = runBlockingTest {

        val given = ControlPanelState.init()

        val event = ControlPanelMachine.Event.OnFocusChanged(
            id = MockDataFactory.randomUuid()
        )

        val actual = reducer.reduce(
            state = given,
            event = event
        )

        val expected = given.copy(
            focus = ControlPanelState.Focus(
                id = event.id
            ),
            blockToolbar = ControlPanelState.Toolbar.Block(
                isVisible = true
            )
        )

        assertEquals(
            expected = expected,
            actual = actual
        )
    }

    @Test
    fun `state should have only focus changed`() = runBlockingTest {

        val given = ControlPanelState(
            focus = ControlPanelState.Focus(id = MockDataFactory.randomUuid()),
            blockToolbar = ControlPanelState.Toolbar.Block(
                isVisible = true
            ),
            addBlockToolbar = ControlPanelState.Toolbar.AddBlock(
                isVisible = false
            ),
            colorToolbar = ControlPanelState.Toolbar.Color(
                isVisible = false
            ),
            actionToolbar = ControlPanelState.Toolbar.BlockAction(
                isVisible = false
            ),
            markupToolbar = ControlPanelState.Toolbar.Markup(
                isVisible = false
            )
        )

        val event = ControlPanelMachine.Event.OnFocusChanged(
            id = MockDataFactory.randomUuid()
        )

        val actual = reducer.reduce(
            state = given,
            event = event
        )

        val expected = given.copy(
            focus = ControlPanelState.Focus(event.id)
        )

        assertEquals(
            expected = expected,
            actual = actual
        )
    }

    @Test
    fun `should return to initial state when focus is cleared`() = runBlockingTest {

        val given = ControlPanelState(
            focus = ControlPanelState.Focus(id = MockDataFactory.randomUuid()),
            blockToolbar = ControlPanelState.Toolbar.Block(
                isVisible = true
            ),
            addBlockToolbar = ControlPanelState.Toolbar.AddBlock(
                isVisible = false
            ),
            colorToolbar = ControlPanelState.Toolbar.Color(
                isVisible = false
            ),
            actionToolbar = ControlPanelState.Toolbar.BlockAction(
                isVisible = false
            ),
            markupToolbar = ControlPanelState.Toolbar.Markup(
                isVisible = false
            )
        )

        val event = ControlPanelMachine.Event.OnClearFocusClicked

        val actual = reducer.reduce(
            state = given,
            event = event
        )

        val expected = ControlPanelState.init()

        assertEquals(
            expected = expected,
            actual = actual
        )
    }
}