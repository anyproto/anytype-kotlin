package com.agileburo.anytype.presentation.page

import com.agileburo.anytype.core_ui.common.Alignment
import com.agileburo.anytype.core_ui.features.page.styling.StylingMode
import com.agileburo.anytype.core_ui.features.page.styling.StylingType
import com.agileburo.anytype.core_ui.state.ControlPanelState
import com.agileburo.anytype.core_ui.state.ControlPanelState.Toolbar
import com.agileburo.anytype.domain.block.model.Block
import com.agileburo.anytype.domain.block.model.Block.Content.Text.Mark
import com.agileburo.anytype.domain.ext.content
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


        object OnAddBlockToolbarOptionSelected : Event()

        /**
         * Represents an event when user selected a markup text color on [Toolbar.Styling] toolbar.
         */
        object OnMarkupTextColorSelected : Event()


        data class OnMarkupContextMenuTextColorClicked(val target: Block) : Event()

        object OnMarkupContextMenuBackgroundColorClicked : Event()

        /**
         * Represents an event when user selected a background color on [Toolbar.Styling] toolbar.
         */
        object OnMarkupBackgroundColorSelected : Event()

        /**
         * Represents an event when user selected a block text color on [Toolbar.Styling] toolbar.
         */
        object OnBlockTextColorSelected : Event()


        object OnBlockStyleSelected : Event()

        /**
         * Represents an event when user selected block background color on [Toolbar.Styling] toolbar.
         */
        object OnBlockBackgroundColorSelected : Event()

        /**
         * Represents an event when user selected alignment on [Toolbar.Styling] toolbar.
         */
        object OnBlockAlignmentSelected : Event()

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

        object OnBlockStyleToolbarCloseButtonClicked : Event()

        data class OnBlockActionToolbarTextColorClicked(val target: Block) : Event()
        data class OnBlockActionToolbarBackgroundColorClicked(val target: Block) : Event()
        data class OnBlockActionToolbarStyleClicked(val target: Block) : Event()

        object OnEnterMultiSelectModeClicked : Event()
        object OnExitMultiSelectModeClicked : Event()

        data class OnRefresh(val target: Block?) : Event()
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
            is Event.OnSelectionChanged -> {
                if (state.focus == null)
                    state.copy()
                else {
                    state.copy(
                        mainToolbar = state.mainToolbar.copy(
                            isVisible = (!state.multiSelect.isVisible && event.selection.first == event.selection.last)
                        )
                    )
                }
            }
            is Event.OnMarkupTextColorSelected -> state.copy(
                stylingToolbar = state.stylingToolbar.copy(
                    isVisible = false
                )
            )
            is Event.OnBlockTextColorSelected -> state.copy(
                stylingToolbar = state.stylingToolbar.copy(
                    type = StylingType.TEXT_COLOR
                )
            )
            is Event.OnBlockBackgroundColorSelected -> state.copy(
                stylingToolbar = state.stylingToolbar.copy(
                    type = StylingType.BACKGROUND
                )
            )
            is Event.OnBlockAlignmentSelected -> state.copy(
                stylingToolbar = state.stylingToolbar.copy(
                    type = StylingType.STYLE
                )
            )
            is Event.OnBlockStyleSelected -> state.copy(
                stylingToolbar = state.stylingToolbar.copy(
                    type = StylingType.STYLE
                )
            )
            is Event.OnAddBlockToolbarOptionSelected -> state.copy()
            is Event.OnMarkupBackgroundColorSelected -> state.copy(
                stylingToolbar = state.stylingToolbar.copy(
                    isVisible = false
                )
            )
            is Event.OnMarkupContextMenuTextColorClicked -> state.copy(
                stylingToolbar = state.stylingToolbar.copy(
                    isVisible = true,
                    mode = StylingMode.MARKUP,
                    type = StylingType.TEXT_COLOR
                )
            )
            is Event.OnMarkupContextMenuBackgroundColorClicked -> state.copy(
                stylingToolbar = state.stylingToolbar.copy(
                    isVisible = true,
                    mode = StylingMode.MARKUP,
                    type = StylingType.BACKGROUND
                )
            )
            is Event.OnClearFocusClicked -> ControlPanelState.init()
            is Event.OnTextInputClicked -> state.copy(
                stylingToolbar = state.stylingToolbar.copy(
                    isVisible = false
                )
            )
            is Event.OnBlockStyleToolbarCloseButtonClicked -> state.copy(
                stylingToolbar = state.stylingToolbar.copy(
                    isVisible = false
                ),
                mainToolbar = state.mainToolbar.copy(
                    isVisible = true
                )
            )
            is Event.OnBlockActionToolbarTextColorClicked -> state.copy(
                mainToolbar = state.mainToolbar.copy(
                    isVisible = false
                ),
                stylingToolbar = state.stylingToolbar.copy(
                    isVisible = true,
                    mode = StylingMode.BLOCK,
                    type = StylingType.TEXT_COLOR,
                    target = target(event.target)
                )
            )
            is Event.OnBlockActionToolbarBackgroundColorClicked -> state.copy(
                mainToolbar = state.mainToolbar.copy(
                    isVisible = false
                ),
                stylingToolbar = state.stylingToolbar.copy(
                    isVisible = true,
                    mode = StylingMode.BLOCK,
                    type = StylingType.BACKGROUND,
                    target = target(event.target)
                )
            )
            is Event.OnBlockActionToolbarStyleClicked -> state.copy(
                mainToolbar = state.mainToolbar.copy(
                    isVisible = false
                ),
                stylingToolbar = state.stylingToolbar.copy(
                    isVisible = true,
                    mode = StylingMode.BLOCK,
                    type = StylingType.STYLE,
                    target = target(event.target)
                )
            )
            is Event.OnRefresh -> {
                if (state.stylingToolbar.isVisible) {
                    state.copy(
                        stylingToolbar = state.stylingToolbar.copy(
                            target = event.target?.let { target(it) }
                        )
                    )
                } else {
                    state.copy()
                }
            }
            is Event.OnEnterMultiSelectModeClicked -> state.copy(
                mainToolbar = state.mainToolbar.copy(
                    isVisible = false
                ),
                multiSelect = state.multiSelect.copy(
                    isVisible = true
                )
            )
            is Event.OnExitMultiSelectModeClicked -> state.copy(
                focus = null,
                multiSelect = state.multiSelect.copy(
                    isVisible = false
                ),
                mainToolbar = state.mainToolbar.copy(
                    isVisible = false
                )
            )
            is Event.OnFocusChanged -> {
                when {
                    state.multiSelect.isVisible -> state.copy()
                    !state.mainToolbar.isVisible -> state.copy(
                        mainToolbar = state.mainToolbar.copy(
                            isVisible = true
                        ),
                        stylingToolbar = state.stylingToolbar.copy(
                            isVisible = false
                        ),
                        focus = ControlPanelState.Focus(
                            id = event.id,
                            type = ControlPanelState.Focus.Type.valueOf(
                                value = event.style.name
                            )
                        )
                    )
                    else -> {
                        state.copy(
                            focus = ControlPanelState.Focus(
                                id = event.id,
                                type = ControlPanelState.Focus.Type.valueOf(
                                    value = event.style.name
                                )
                            ),
                            stylingToolbar = state.stylingToolbar.copy(
                                isVisible = false
                            )
                        )
                    }
                }
            }
        }

        fun target(block: Block) : Toolbar.Styling.Target {
            val content = block.content<Block.Content.Text>()
            return Toolbar.Styling.Target(
                text = content.text,
                color = content.color,
                background = content.backgroundColor,
                alignment = content.align?.let { alignment ->
                    when(alignment) {
                        Block.Align.AlignLeft -> Alignment.START
                        Block.Align.AlignRight -> Alignment.END
                        Block.Align.AlignCenter -> Alignment.CENTER
                    }
                },
                isBold = content.marks.any { mark ->
                    mark.type == Mark.Type.BOLD && mark.range == 0..content.text.length
                },
                isItalic = content.marks.any { mark ->
                    mark.type == Mark.Type.ITALIC && mark.range == 0..content.text.length
                },
                isCode = content.marks.any { mark ->
                    mark.type == Mark.Type.KEYBOARD && mark.range == 0..content.text.length
                },
                isStrikethrough = content.marks.any { mark ->
                    mark.type == Mark.Type.STRIKETHROUGH && mark.range == 0..content.text.length
                }
            )
        }
    }
}