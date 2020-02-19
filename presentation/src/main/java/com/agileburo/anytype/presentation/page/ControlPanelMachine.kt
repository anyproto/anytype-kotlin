package com.agileburo.anytype.presentation.page

import com.agileburo.anytype.core_ui.state.ControlPanelState
import com.agileburo.anytype.core_ui.state.ControlPanelState.Toolbar
import com.agileburo.anytype.domain.block.model.Block
import com.agileburo.anytype.presentation.common.StateReducer
import com.agileburo.anytype.presentation.page.ControlPanelMachine.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * State machine for control panels consisting of [Interactor], [ControlPanelState], [Event] and [Reducer]
 * [Interactor] reduces [Event] to the immutable [ControlPanelState] by applying [Reducer] fuction.
 * This [ControlPanelState] then will be rendered.
 */
sealed class ControlPanelMachine {

    /**
     * @property scope coroutine scope (state machine runs inside this scope)
     */
    class Interactor(
        private val scope: CoroutineScope
    ) : ControlPanelMachine() {

        private val reducer: Reducer = Reducer()
        val channel: Channel<Event> = Channel()
        private val events: Flow<Event> = channel.consumeAsFlow()

        fun onEvent(event: Event) = scope.launch { channel.send(event) }

        /**
         * @return a stream of immutable states, as processed by [Reducer].
         */
        fun state(): Flow<ControlPanelState> =
            events.scan(ControlPanelState.init(), reducer.function)
    }

    /**
     * Represents events related to this state machine and to control panel logics.
     */
    sealed class Event {

        /**
         * Represents text selection changes events
         * @property selection text selection (end index and start index are inclusive)
         */
        data class OnSelectionChanged(
            val selection: IntRange
        ) : Event()

        /**
         * Represents an event when user selected a color option on [Toolbar.Markup] toolbar.
         */
        object OnMarkupToolbarColorClicked : Event()

        /**
         * Represents an event when user toggled [Toolbar.AddBlock] toolbar button on [Toolbar.Block].
         */
        object OnAddBlockToolbarToggleClicked : Event()

        /**
         * Represents an event when user toggled [Toolbar.TurnInto] toolbar button on [Toolbar.Block]
         */
        object OnTurnIntoToolbarToggleClicked : Event()

        /**
         * Represents an event when user selected any of the options on [Toolbar.AddBlock] toolbar.
         */
        object OnAddBlockToolbarOptionSelected : Event()

        /**
         * Represents an event when user selected any of the options on [Toolbar.TurnInto] toolbar.
         */
        object OnTurnIntoToolbarOptionSelected : Event()


        /**
         * Represents an event when user toggled [Toolbar.Color] toolbar button on [Toolbar.Block]
         */
        object OnColorToolbarToggleClicked : Event()

        /**
         * Represents an event when user selected a markup text color on [Toolbar.Color] toolbar.
         */
        object OnMarkupTextColorSelected : Event()

        /**
         * Represents an event when user selected a background color on [Toolbar.Color] toolbar.
         */
        object OnMarkupBackgroundColorSelected : Event()

        /**
         * Represents an event when user selected a block text color on [Toolbar.Color] toolbar.
         */
        object OnBlockTextColorSelected : Event()

        /**
         * Represents an event when user selected an action toolbar on [Toolbar.Block]
         */
        object OnActionToolbarClicked : Event()

        /**
         * Represents an event when user cleares the current focus by closing keyboard.
         */
        object OnClearFocusClicked : Event()

        /**
         * Represents an event when user clicked on text input widget.
         * This event is expected to trigger keyboard openining.
         */
        object OnTextInputClicked : Event()

        /**
         * Represents an event when focus changes.
         * @property id id of the focused block
         */
        data class OnFocusChanged(
            val id: String,
            val style: Block.Content.Text.Style
        ) : Event()
    }

    /**
     * Concrete reducer implementation that holds all the logic related to control panels.
     */
    class Reducer : StateReducer<ControlPanelState, Event> {

        override val function: suspend (ControlPanelState, Event) -> ControlPanelState
            get() = { state, event ->
                reduce(
                    state,
                    event
                ).also { Timber.d("Reducing event:\n$event") }
            }

        override suspend fun reduce(state: ControlPanelState, event: Event) = when (event) {
            is Event.OnSelectionChanged -> state.copy(
                markupToolbar = state.markupToolbar.copy(
                    isVisible = event.selection.first != event.selection.last,
                    selectedAction = if (event.selection.first != event.selection.last)
                        state.markupToolbar.selectedAction
                    else
                        null
                ),
                blockToolbar = state.blockToolbar.copy(
                    selectedAction = null,
                    isVisible = (event.selection.first == event.selection.last)
                ),
                actionToolbar = state.actionToolbar.copy(
                    isVisible = false
                ),
                addBlockToolbar = state.addBlockToolbar.copy(
                    isVisible = false
                ),
                colorToolbar = if (event.selection.first != event.selection.last)
                    state.colorToolbar.copy()
                else
                    state.colorToolbar.copy(isVisible = false)
            )
            is Event.OnMarkupToolbarColorClicked -> state.copy(
                colorToolbar = state.colorToolbar.copy(
                    isVisible = !state.colorToolbar.isVisible
                ),
                markupToolbar = state.markupToolbar.copy(
                    selectedAction = if (!state.colorToolbar.isVisible)
                        Toolbar.Markup.Action.COLOR
                    else
                        null
                )
            )
            is Event.OnMarkupTextColorSelected -> state.copy(
                colorToolbar = state.colorToolbar.copy(
                    isVisible = false
                ),
                markupToolbar = state.markupToolbar.copy(
                    selectedAction = null
                )
            )
            is Event.OnBlockTextColorSelected -> state.copy(
                colorToolbar = state.colorToolbar.copy(
                    isVisible = false
                ),
                blockToolbar = state.blockToolbar.copy(
                    selectedAction = null
                )
            )
            is Event.OnAddBlockToolbarToggleClicked -> state.copy(
                addBlockToolbar = state.addBlockToolbar.copy(
                    isVisible = !state.addBlockToolbar.isVisible
                ),
                blockToolbar = state.blockToolbar.copy(
                    selectedAction = if (!state.addBlockToolbar.isVisible)
                        Toolbar.Block.Action.ADD
                    else
                        null
                ),
                actionToolbar = state.actionToolbar.copy(
                    isVisible = false
                ),
                turnIntoToolbar = state.turnIntoToolbar.copy(
                    isVisible = false
                ),
                colorToolbar = state.colorToolbar.copy(
                    isVisible = false
                )
            )
            is Event.OnTurnIntoToolbarToggleClicked -> state.copy(
                turnIntoToolbar = state.turnIntoToolbar.copy(
                    isVisible = !state.turnIntoToolbar.isVisible
                ),
                blockToolbar = state.blockToolbar.copy(
                    selectedAction = if (!state.turnIntoToolbar.isVisible)
                        Toolbar.Block.Action.TURN_INTO
                    else
                        null
                ),
                addBlockToolbar = state.addBlockToolbar.copy(
                    isVisible = false
                ),
                actionToolbar = state.actionToolbar.copy(
                    isVisible = false
                ),
                colorToolbar = state.colorToolbar.copy(
                    isVisible = false
                )
            )
            is Event.OnColorToolbarToggleClicked -> state.copy(
                colorToolbar = state.colorToolbar.copy(
                    isVisible = !state.colorToolbar.isVisible
                ),
                blockToolbar = state.blockToolbar.copy(
                    selectedAction = if (!state.colorToolbar.isVisible)
                        Toolbar.Block.Action.COLOR
                    else
                        null
                ),
                turnIntoToolbar = state.turnIntoToolbar.copy(
                    isVisible = false
                ),
                addBlockToolbar = state.addBlockToolbar.copy(
                    isVisible = false
                ),
                actionToolbar = state.actionToolbar.copy(
                    isVisible = false
                )
            )
            is Event.OnAddBlockToolbarOptionSelected -> state.copy(
                addBlockToolbar = state.addBlockToolbar.copy(
                    isVisible = false
                ),
                blockToolbar = state.blockToolbar.copy(
                    selectedAction = null
                )
            )
            is Event.OnMarkupBackgroundColorSelected -> state.copy(
                colorToolbar = state.colorToolbar.copy(
                    isVisible = false
                ),
                markupToolbar = state.markupToolbar.copy(
                    selectedAction = null
                )
            )
            is Event.OnTurnIntoToolbarOptionSelected -> state.copy(
                turnIntoToolbar = state.turnIntoToolbar.copy(
                    isVisible = false
                ),
                blockToolbar = state.blockToolbar.copy(
                    selectedAction = null
                )
            )
            is Event.OnActionToolbarClicked -> state.copy(
                colorToolbar = state.colorToolbar.copy(
                    isVisible = false
                ),
                addBlockToolbar = state.addBlockToolbar.copy(
                    isVisible = false
                ),
                turnIntoToolbar = state.turnIntoToolbar.copy(
                    isVisible = false
                ),
                actionToolbar = state.actionToolbar.copy(
                    isVisible = !state.actionToolbar.isVisible
                ),
                blockToolbar = state.blockToolbar.copy(
                    selectedAction = if (state.actionToolbar.isVisible)
                        null
                    else Toolbar.Block.Action.BLOCK_ACTION
                )
            )
            is Event.OnClearFocusClicked -> ControlPanelState.init()
            is Event.OnTextInputClicked -> state.copy(
                blockToolbar = state.blockToolbar.copy(
                    selectedAction = null
                ),
                markupToolbar = state.markupToolbar.copy(
                    selectedAction = null
                ),
                addBlockToolbar = state.addBlockToolbar.copy(
                    isVisible = false
                ),
                turnIntoToolbar = state.turnIntoToolbar.copy(
                    isVisible = false
                ),
                actionToolbar = state.actionToolbar.copy(
                    isVisible = false
                ),
                colorToolbar = state.colorToolbar.copy(
                    isVisible = false
                )
            )
            is Event.OnFocusChanged -> {
                if (state.isNotVisible())
                    state.copy(
                        blockToolbar = state.blockToolbar.copy(
                            isVisible = true,
                            selectedAction = null
                        ),
                        turnIntoToolbar = state.turnIntoToolbar.copy(
                            isVisible = false
                        ),
                        addBlockToolbar = state.addBlockToolbar.copy(
                            isVisible = false
                        ),
                        actionToolbar = state.actionToolbar.copy(
                            isVisible = false
                        ),
                        colorToolbar = state.colorToolbar.copy(
                            isVisible = false
                        ),
                        focus = ControlPanelState.Focus(
                            id = event.id,
                            type = ControlPanelState.Focus.Type.valueOf(
                                value = event.style.name
                            )
                        )
                    )
                else {
                    state.copy(
                        blockToolbar = state.blockToolbar.copy(
                            selectedAction = null
                        ),
                        focus = ControlPanelState.Focus(
                            id = event.id,
                            type = ControlPanelState.Focus.Type.valueOf(
                                value = event.style.name
                            )
                        ),
                        addBlockToolbar = state.addBlockToolbar.copy(
                            isVisible = false
                        ),
                        actionToolbar = state.actionToolbar.copy(
                            isVisible = false
                        ),
                        turnIntoToolbar = state.turnIntoToolbar.copy(
                            isVisible = false
                        ),
                        colorToolbar = state.colorToolbar.copy(
                            isVisible = false
                        )
                    )
                }
            }
        }
    }
}