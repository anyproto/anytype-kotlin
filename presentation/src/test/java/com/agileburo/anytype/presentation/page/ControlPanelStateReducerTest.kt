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
    fun `state should have only focus changed`() {

        val given = ControlPanelState(
            focus = ControlPanelState.Focus(
                id = MockDataFactory.randomUuid(),
                type = ControlPanelState.Focus.Type.P
            ),
            blockToolbar = ControlPanelState.Toolbar.Block(
                isVisible = true
            ),
            addBlockToolbar = ControlPanelState.Toolbar.AddBlock(
                isVisible = false
            ),
            turnIntoToolbar = ControlPanelState.Toolbar.TurnInto(
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
            blockToolbar = ControlPanelState.Toolbar.Block(
                isVisible = true
            ),
            addBlockToolbar = ControlPanelState.Toolbar.AddBlock(
                isVisible = false
            ),
            turnIntoToolbar = ControlPanelState.Toolbar.TurnInto(
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
    fun `should show add-block toolbar and hide turn-into toolbar on add-block-toolbar-toggle-clicked event`() {

        val given = ControlPanelState(
            focus = ControlPanelState.Focus(
                id = MockDataFactory.randomUuid(),
                type = ControlPanelState.Focus.Type.H1
            ),
            blockToolbar = ControlPanelState.Toolbar.Block(
                isVisible = true,
                selectedAction = ControlPanelState.Toolbar.Block.Action.TURN_INTO
            ),
            addBlockToolbar = ControlPanelState.Toolbar.AddBlock(
                isVisible = false
            ),
            turnIntoToolbar = ControlPanelState.Toolbar.TurnInto(
                isVisible = true
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

        val event = ControlPanelMachine.Event.OnAddBlockToolbarToggleClicked

        val actual = runBlocking {
            reducer.reduce(
                state = given,
                event = event
            )
        }

        val expected = given.copy(
            addBlockToolbar = ControlPanelState.Toolbar.AddBlock(
                isVisible = true
            ),
            turnIntoToolbar = ControlPanelState.Toolbar.TurnInto(
                isVisible = false
            ),
            blockToolbar = ControlPanelState.Toolbar.Block(
                isVisible = true,
                selectedAction = ControlPanelState.Toolbar.Block.Action.ADD
            )
        )

        assertEquals(
            expected = expected,
            actual = actual
        )
    }

    @Test
    fun `should hide add-block toolbar and show turn-into toolbar on turn-into-toolbar-toogle-clicked event`() {

        val given = ControlPanelState(
            focus = ControlPanelState.Focus(
                id = MockDataFactory.randomUuid(),
                type = ControlPanelState.Focus.Type.H1
            ),
            blockToolbar = ControlPanelState.Toolbar.Block(
                isVisible = true,
                selectedAction = ControlPanelState.Toolbar.Block.Action.ADD
            ),
            addBlockToolbar = ControlPanelState.Toolbar.AddBlock(
                isVisible = true
            ),
            turnIntoToolbar = ControlPanelState.Toolbar.TurnInto(
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

        val event = ControlPanelMachine.Event.OnTurnIntoToolbarToggleClicked

        val actual = runBlocking {
            reducer.reduce(
                state = given,
                event = event
            )
        }

        val expected = given.copy(
            addBlockToolbar = ControlPanelState.Toolbar.AddBlock(
                isVisible = false
            ),
            turnIntoToolbar = ControlPanelState.Toolbar.TurnInto(
                isVisible = true
            ),
            blockToolbar = ControlPanelState.Toolbar.Block(
                isVisible = true,
                selectedAction = ControlPanelState.Toolbar.Block.Action.TURN_INTO
            )
        )

        assertEquals(
            expected = expected,
            actual = actual
        )
    }

    @Test
    fun `should hide turn-into toolbar on focus-changed event`() {

        val given = ControlPanelState(
            focus = ControlPanelState.Focus(
                id = MockDataFactory.randomUuid(),
                type = ControlPanelState.Focus.Type.H1
            ),
            blockToolbar = ControlPanelState.Toolbar.Block(
                isVisible = true,
                selectedAction = ControlPanelState.Toolbar.Block.Action.TURN_INTO
            ),
            addBlockToolbar = ControlPanelState.Toolbar.AddBlock(
                isVisible = false
            ),
            turnIntoToolbar = ControlPanelState.Toolbar.TurnInto(
                isVisible = true
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
                type = ControlPanelState.Focus.Type.valueOf(event.style.name)
            ),
            turnIntoToolbar = ControlPanelState.Toolbar.TurnInto(
                isVisible = false
            ),
            blockToolbar = ControlPanelState.Toolbar.Block(
                isVisible = true,
                selectedAction = null
            )
        )

        assertEquals(
            expected = expected,
            actual = actual
        )
    }

    @Test
    fun `should hide add-block toolbar on focus-changed event`() {

        val given = ControlPanelState(
            focus = ControlPanelState.Focus(
                id = MockDataFactory.randomUuid(),
                type = ControlPanelState.Focus.Type.H1
            ),
            blockToolbar = ControlPanelState.Toolbar.Block(
                isVisible = true,
                selectedAction = ControlPanelState.Toolbar.Block.Action.ADD
            ),
            addBlockToolbar = ControlPanelState.Toolbar.AddBlock(
                isVisible = true
            ),
            turnIntoToolbar = ControlPanelState.Toolbar.TurnInto(
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
                type = ControlPanelState.Focus.Type.valueOf(event.style.name)
            ),
            addBlockToolbar = ControlPanelState.Toolbar.AddBlock(
                isVisible = false
            ),
            blockToolbar = ControlPanelState.Toolbar.Block(
                isVisible = true,
                selectedAction = null
            )
        )

        assertEquals(
            expected = expected,
            actual = actual
        )
    }

    @Test
    fun `should hide block-action toolbar on focus-changed event`() {

        val given = ControlPanelState(
            focus = ControlPanelState.Focus(
                id = MockDataFactory.randomUuid(),
                type = ControlPanelState.Focus.Type.H1
            ),
            blockToolbar = ControlPanelState.Toolbar.Block(
                isVisible = true,
                selectedAction = ControlPanelState.Toolbar.Block.Action.BLOCK_ACTION
            ),
            addBlockToolbar = ControlPanelState.Toolbar.AddBlock(
                isVisible = false
            ),
            turnIntoToolbar = ControlPanelState.Toolbar.TurnInto(
                isVisible = false
            ),
            colorToolbar = ControlPanelState.Toolbar.Color(
                isVisible = false
            ),
            actionToolbar = ControlPanelState.Toolbar.BlockAction(
                isVisible = true
            ),
            markupToolbar = ControlPanelState.Toolbar.Markup(
                isVisible = false
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
                type = ControlPanelState.Focus.Type.valueOf(event.style.name)
            ),
            blockToolbar = ControlPanelState.Toolbar.Block(
                isVisible = true,
                selectedAction = null
            ),
            actionToolbar = given.actionToolbar.copy(
                isVisible = false
            )
        )

        assertEquals(
            expected = expected,
            actual = actual
        )
    }

    @Test
    fun `should hide block-action toolbar and show turn-into toolbar on turn-into-toolbar-toogle-clicked-event`() {

        val given = ControlPanelState(
            focus = ControlPanelState.Focus(
                id = MockDataFactory.randomUuid(),
                type = ControlPanelState.Focus.Type.H1
            ),
            blockToolbar = ControlPanelState.Toolbar.Block(
                isVisible = true,
                selectedAction = ControlPanelState.Toolbar.Block.Action.BLOCK_ACTION
            ),
            addBlockToolbar = ControlPanelState.Toolbar.AddBlock(
                isVisible = false
            ),
            turnIntoToolbar = ControlPanelState.Toolbar.TurnInto(
                isVisible = false
            ),
            colorToolbar = ControlPanelState.Toolbar.Color(
                isVisible = false
            ),
            actionToolbar = ControlPanelState.Toolbar.BlockAction(
                isVisible = true
            ),
            markupToolbar = ControlPanelState.Toolbar.Markup(
                isVisible = false
            )
        )

        val event = ControlPanelMachine.Event.OnTurnIntoToolbarToggleClicked

        val actual = runBlocking {
            reducer.reduce(
                state = given,
                event = event
            )
        }

        val expected = given.copy(
            blockToolbar = ControlPanelState.Toolbar.Block(
                isVisible = true,
                selectedAction = ControlPanelState.Toolbar.Block.Action.TURN_INTO
            ),
            turnIntoToolbar = ControlPanelState.Toolbar.TurnInto(
                isVisible = true
            ),
            actionToolbar = given.actionToolbar.copy(
                isVisible = false
            )
        )

        assertEquals(
            expected = expected,
            actual = actual
        )
    }

    @Test
    fun `should hide turn-into toolbar, show block-action toolbar on action-toolbar-toogle-clicked event`() {

        val given = ControlPanelState(
            focus = ControlPanelState.Focus(
                id = MockDataFactory.randomUuid(),
                type = ControlPanelState.Focus.Type.H1
            ),
            blockToolbar = ControlPanelState.Toolbar.Block(
                isVisible = true,
                selectedAction = ControlPanelState.Toolbar.Block.Action.TURN_INTO
            ),
            addBlockToolbar = ControlPanelState.Toolbar.AddBlock(
                isVisible = false
            ),
            turnIntoToolbar = ControlPanelState.Toolbar.TurnInto(
                isVisible = true
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

        val event = ControlPanelMachine.Event.OnActionToolbarClicked

        val actual = runBlocking {
            reducer.reduce(
                state = given,
                event = event
            )
        }

        val expected = given.copy(
            blockToolbar = ControlPanelState.Toolbar.Block(
                isVisible = true,
                selectedAction = ControlPanelState.Toolbar.Block.Action.BLOCK_ACTION
            ),
            turnIntoToolbar = ControlPanelState.Toolbar.TurnInto(
                isVisible = false
            ),
            actionToolbar = given.actionToolbar.copy(
                isVisible = true
            )
        )

        assertEquals(
            expected = expected,
            actual = actual
        )
    }

    @Test
    fun `should show block-related color toolbar with appropriate selected action on color-toolbar-toggle-clicked event`() {

        val given = ControlPanelState(
            focus = ControlPanelState.Focus(
                id = MockDataFactory.randomUuid(),
                type = ControlPanelState.Focus.Type.P
            ),
            blockToolbar = ControlPanelState.Toolbar.Block(
                isVisible = true,
                selectedAction = null
            ),
            addBlockToolbar = ControlPanelState.Toolbar.AddBlock(
                isVisible = false
            ),
            turnIntoToolbar = ControlPanelState.Toolbar.TurnInto(
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

        val event = ControlPanelMachine.Event.OnColorToolbarToggleClicked

        val actual = runBlocking {
            reducer.reduce(
                state = given,
                event = event
            )
        }

        val expected = given.copy(
            blockToolbar = ControlPanelState.Toolbar.Block(
                isVisible = true,
                selectedAction = ControlPanelState.Toolbar.Block.Action.COLOR
            ),
            colorToolbar = given.colorToolbar.copy(
                isVisible = true
            )
        )

        assertEquals(
            expected = expected,
            actual = actual
        )
    }

    @Test
    fun `should hide block-related color toolbar on color-toolbar-toggle-clicked event`() {

        val given = ControlPanelState(
            focus = ControlPanelState.Focus(
                id = MockDataFactory.randomUuid(),
                type = ControlPanelState.Focus.Type.P
            ),
            blockToolbar = ControlPanelState.Toolbar.Block(
                isVisible = true,
                selectedAction = ControlPanelState.Toolbar.Block.Action.COLOR
            ),
            addBlockToolbar = ControlPanelState.Toolbar.AddBlock(
                isVisible = false
            ),
            turnIntoToolbar = ControlPanelState.Toolbar.TurnInto(
                isVisible = false
            ),
            colorToolbar = ControlPanelState.Toolbar.Color(
                isVisible = true
            ),
            actionToolbar = ControlPanelState.Toolbar.BlockAction(
                isVisible = false
            ),
            markupToolbar = ControlPanelState.Toolbar.Markup(
                isVisible = false
            )
        )

        val event = ControlPanelMachine.Event.OnColorToolbarToggleClicked

        val actual = runBlocking {
            reducer.reduce(
                state = given,
                event = event
            )
        }

        val expected = given.copy(
            blockToolbar = ControlPanelState.Toolbar.Block(
                isVisible = true,
                selectedAction = null
            ),
            colorToolbar = given.colorToolbar.copy(
                isVisible = false
            )
        )

        assertEquals(
            expected = expected,
            actual = actual
        )
    }

    @Test
    fun `should hide block-related color toolbar on focus-changed event`() {

        val given = ControlPanelState(
            focus = ControlPanelState.Focus(
                id = MockDataFactory.randomUuid(),
                type = ControlPanelState.Focus.Type.P
            ),
            blockToolbar = ControlPanelState.Toolbar.Block(
                isVisible = true,
                selectedAction = ControlPanelState.Toolbar.Block.Action.COLOR
            ),
            addBlockToolbar = ControlPanelState.Toolbar.AddBlock(
                isVisible = false
            ),
            turnIntoToolbar = ControlPanelState.Toolbar.TurnInto(
                isVisible = false
            ),
            colorToolbar = ControlPanelState.Toolbar.Color(
                isVisible = true
            ),
            actionToolbar = ControlPanelState.Toolbar.BlockAction(
                isVisible = false
            ),
            markupToolbar = ControlPanelState.Toolbar.Markup(
                isVisible = false
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
            blockToolbar = ControlPanelState.Toolbar.Block(
                isVisible = true,
                selectedAction = null
            ),
            colorToolbar = given.colorToolbar.copy(
                isVisible = false
            )
        )

        assertEquals(
            expected = expected,
            actual = actual
        )
    }

    @Test
    fun `should hide block toolbar and show markup toolbar on selection-changed-event`() {

        val given = ControlPanelState(
            focus = ControlPanelState.Focus(
                id = MockDataFactory.randomUuid(),
                type = ControlPanelState.Focus.Type.P
            ),
            blockToolbar = ControlPanelState.Toolbar.Block(
                isVisible = true,
                selectedAction = null
            ),
            addBlockToolbar = ControlPanelState.Toolbar.AddBlock(
                isVisible = false
            ),
            turnIntoToolbar = ControlPanelState.Toolbar.TurnInto(
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

        val event = ControlPanelMachine.Event.OnSelectionChanged(
            selection = 0..5
        )

        val actual = runBlocking {
            reducer.reduce(
                state = given,
                event = event
            )
        }

        val expected = given.copy(
            blockToolbar = ControlPanelState.Toolbar.Block(
                isVisible = false,
                selectedAction = null
            ),
            markupToolbar = given.markupToolbar.copy(
                isVisible = true,
                selectedAction = null
            )
        )

        assertEquals(
            expected = expected,
            actual = actual
        )
    }

    @Test
    fun `should hide color toolbar and deselect block-toolbar color toogle on block-text-color-selected event`() {

        val given = ControlPanelState(
            focus = ControlPanelState.Focus(
                id = MockDataFactory.randomUuid(),
                type = ControlPanelState.Focus.Type.P
            ),
            blockToolbar = ControlPanelState.Toolbar.Block(
                isVisible = true,
                selectedAction = ControlPanelState.Toolbar.Block.Action.COLOR
            ),
            addBlockToolbar = ControlPanelState.Toolbar.AddBlock(
                isVisible = false
            ),
            turnIntoToolbar = ControlPanelState.Toolbar.TurnInto(
                isVisible = false
            ),
            colorToolbar = ControlPanelState.Toolbar.Color(
                isVisible = true
            ),
            actionToolbar = ControlPanelState.Toolbar.BlockAction(
                isVisible = false
            ),
            markupToolbar = ControlPanelState.Toolbar.Markup(
                isVisible = false
            )
        )

        val event = ControlPanelMachine.Event.OnBlockTextColorSelected

        val actual = runBlocking {
            reducer.reduce(
                state = given,
                event = event
            )
        }

        val expected = given.copy(
            blockToolbar = given.blockToolbar.copy(
                isVisible = true,
                selectedAction = null
            ),
            colorToolbar = given.colorToolbar.copy(
                isVisible = false
            )
        )

        assertEquals(
            expected = expected,
            actual = actual
        )
    }

    @Test
    fun `should hide color toolbar on turn-into-toolbar-toogle-clicked event`() {

        val given = ControlPanelState(
            focus = ControlPanelState.Focus(
                id = MockDataFactory.randomUuid(),
                type = ControlPanelState.Focus.Type.P
            ),
            blockToolbar = ControlPanelState.Toolbar.Block(
                isVisible = true,
                selectedAction = ControlPanelState.Toolbar.Block.Action.COLOR
            ),
            addBlockToolbar = ControlPanelState.Toolbar.AddBlock(
                isVisible = false
            ),
            turnIntoToolbar = ControlPanelState.Toolbar.TurnInto(
                isVisible = false
            ),
            colorToolbar = ControlPanelState.Toolbar.Color(
                isVisible = true
            ),
            actionToolbar = ControlPanelState.Toolbar.BlockAction(
                isVisible = false
            ),
            markupToolbar = ControlPanelState.Toolbar.Markup(
                isVisible = false
            )
        )

        val event = ControlPanelMachine.Event.OnTurnIntoToolbarToggleClicked

        val actual = runBlocking {
            reducer.reduce(
                state = given,
                event = event
            )
        }

        val expected = given.copy(
            blockToolbar = given.blockToolbar.copy(
                isVisible = true,
                selectedAction = ControlPanelState.Toolbar.Block.Action.TURN_INTO
            ),
            colorToolbar = given.colorToolbar.copy(
                isVisible = false
            ),
            turnIntoToolbar = given.turnIntoToolbar.copy(
                isVisible = true
            )
        )

        assertEquals(
            expected = expected,
            actual = actual
        )
    }

    @Test
    fun `should hide color toolbar on add-block-toolbar-toogle-clicked event`() {

        val given = ControlPanelState(
            focus = ControlPanelState.Focus(
                id = MockDataFactory.randomUuid(),
                type = ControlPanelState.Focus.Type.P
            ),
            blockToolbar = ControlPanelState.Toolbar.Block(
                isVisible = true,
                selectedAction = ControlPanelState.Toolbar.Block.Action.COLOR
            ),
            addBlockToolbar = ControlPanelState.Toolbar.AddBlock(
                isVisible = false
            ),
            turnIntoToolbar = ControlPanelState.Toolbar.TurnInto(
                isVisible = false
            ),
            colorToolbar = ControlPanelState.Toolbar.Color(
                isVisible = true
            ),
            actionToolbar = ControlPanelState.Toolbar.BlockAction(
                isVisible = false
            ),
            markupToolbar = ControlPanelState.Toolbar.Markup(
                isVisible = false
            )
        )

        val event = ControlPanelMachine.Event.OnAddBlockToolbarToggleClicked

        val actual = runBlocking {
            reducer.reduce(
                state = given,
                event = event
            )
        }

        val expected = given.copy(
            blockToolbar = given.blockToolbar.copy(
                isVisible = true,
                selectedAction = ControlPanelState.Toolbar.Block.Action.ADD
            ),
            colorToolbar = given.colorToolbar.copy(
                isVisible = false
            ),
            addBlockToolbar = given.addBlockToolbar.copy(
                isVisible = true
            )
        )

        assertEquals(
            expected = expected,
            actual = actual
        )
    }

    @Test
    fun `should hide turn-into toolbar on block-color-toolbar-toggle-clicked event`() {

        val given = ControlPanelState(
            focus = ControlPanelState.Focus(
                id = MockDataFactory.randomUuid(),
                type = ControlPanelState.Focus.Type.P
            ),
            blockToolbar = ControlPanelState.Toolbar.Block(
                isVisible = true,
                selectedAction = ControlPanelState.Toolbar.Block.Action.TURN_INTO
            ),
            addBlockToolbar = ControlPanelState.Toolbar.AddBlock(
                isVisible = false
            ),
            turnIntoToolbar = ControlPanelState.Toolbar.TurnInto(
                isVisible = true
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

        val event = ControlPanelMachine.Event.OnColorToolbarToggleClicked

        val actual = runBlocking {
            reducer.reduce(
                state = given,
                event = event
            )
        }

        val expected = given.copy(
            blockToolbar = given.blockToolbar.copy(
                isVisible = true,
                selectedAction = ControlPanelState.Toolbar.Block.Action.COLOR
            ),
            colorToolbar = given.colorToolbar.copy(
                isVisible = true
            ),
            turnIntoToolbar = given.turnIntoToolbar.copy(
                isVisible = false
            )
        )

        assertEquals(
            expected = expected,
            actual = actual
        )
    }

    @Test
    fun `should hide add-block toolbar on block-color-toolbar-toggle-clicked event`() {

        val given = ControlPanelState(
            focus = ControlPanelState.Focus(
                id = MockDataFactory.randomUuid(),
                type = ControlPanelState.Focus.Type.P
            ),
            blockToolbar = ControlPanelState.Toolbar.Block(
                isVisible = true,
                selectedAction = ControlPanelState.Toolbar.Block.Action.ADD
            ),
            addBlockToolbar = ControlPanelState.Toolbar.AddBlock(
                isVisible = true
            ),
            turnIntoToolbar = ControlPanelState.Toolbar.TurnInto(
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

        val event = ControlPanelMachine.Event.OnColorToolbarToggleClicked

        val actual = runBlocking {
            reducer.reduce(
                state = given,
                event = event
            )
        }

        val expected = given.copy(
            blockToolbar = given.blockToolbar.copy(
                isVisible = true,
                selectedAction = ControlPanelState.Toolbar.Block.Action.COLOR
            ),
            colorToolbar = given.colorToolbar.copy(
                isVisible = true
            ),
            addBlockToolbar = given.addBlockToolbar.copy(
                isVisible = false
            )
        )

        assertEquals(
            expected = expected,
            actual = actual
        )
    }

    @Test
    fun `should hide block-action toolbar on block-color-toolbar-toggle-clicked event`() {

        val given = ControlPanelState(
            focus = ControlPanelState.Focus(
                id = MockDataFactory.randomUuid(),
                type = ControlPanelState.Focus.Type.P
            ),
            blockToolbar = ControlPanelState.Toolbar.Block(
                isVisible = true,
                selectedAction = ControlPanelState.Toolbar.Block.Action.BLOCK_ACTION
            ),
            addBlockToolbar = ControlPanelState.Toolbar.AddBlock(
                isVisible = false
            ),
            turnIntoToolbar = ControlPanelState.Toolbar.TurnInto(
                isVisible = false
            ),
            colorToolbar = ControlPanelState.Toolbar.Color(
                isVisible = false
            ),
            actionToolbar = ControlPanelState.Toolbar.BlockAction(
                isVisible = true
            ),
            markupToolbar = ControlPanelState.Toolbar.Markup(
                isVisible = false
            )
        )

        val event = ControlPanelMachine.Event.OnColorToolbarToggleClicked

        val actual = runBlocking {
            reducer.reduce(
                state = given,
                event = event
            )
        }

        val expected = given.copy(
            blockToolbar = given.blockToolbar.copy(
                isVisible = true,
                selectedAction = ControlPanelState.Toolbar.Block.Action.COLOR
            ),
            colorToolbar = given.colorToolbar.copy(
                isVisible = true
            ),
            actionToolbar = given.actionToolbar.copy(
                isVisible = false
            )
        )

        assertEquals(
            expected = expected,
            actual = actual
        )
    }
}