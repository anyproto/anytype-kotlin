package com.anytypeio.anytype.presentation.editor

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Block.Content.Text.Style
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.TextBlock
import com.anytypeio.anytype.core_utils.tools.FeatureToggles
import com.anytypeio.anytype.core_utils.tools.toPrettyString
import com.anytypeio.anytype.presentation.common.StateReducer
import com.anytypeio.anytype.presentation.editor.ControlPanelMachine.Event
import com.anytypeio.anytype.presentation.editor.ControlPanelMachine.Interactor
import com.anytypeio.anytype.presentation.editor.ControlPanelMachine.Reducer
import com.anytypeio.anytype.presentation.editor.editor.control.ControlPanelState
import com.anytypeio.anytype.presentation.editor.editor.control.ControlPanelState.Companion.init
import com.anytypeio.anytype.presentation.editor.editor.control.ControlPanelState.Toolbar
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.editor.slash.SlashWidgetState
import com.anytypeio.anytype.presentation.editor.editor.styling.StyleToolbarState
import com.anytypeio.anytype.presentation.editor.editor.styling.getSupportedMarkupTypes
import com.anytypeio.anytype.presentation.editor.editor.table.SimpleTableWidgetItem
import com.anytypeio.anytype.presentation.extension.style
import com.anytypeio.anytype.presentation.navigation.DefaultObjectView
import com.anytypeio.anytype.presentation.objects.ObjectTypeView
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

    companion object {
        const val NO_BLOCK_SELECTED = 0
    }

    /**
     * @property scope coroutine scope (state machine runs inside this scope)
     */
    class Interactor(
        private val scope: CoroutineScope,
        private val reducer: Reducer
    ) : ControlPanelMachine() {

        val channel: Channel<Event> = Channel()
        private val events: Flow<Event> = channel.consumeAsFlow()

        fun onEvent(event: Event) =
            scope.launch { channel.send(event) }
        //.also { Timber.d("Event: $event") }

        /**
         * @return a stream of immutable states, as processed by [Reducer].
         */
        fun state(): Flow<ControlPanelState> = events.scan(init(), reducer.function)
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
            val target: Block,
            val selection: IntRange
        ) : Event()


        object OnAddBlockToolbarOptionSelected : Event()

        /**
         * Represents an event when user selected a markup text color on [Toolbar.Styling] toolbar.
         */
        object OnMarkupTextColorSelected : Event()

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
            val type: Toolbar.Main.TargetBlockType
        ) : Event()

        sealed class SearchToolbar : Event() {
            object OnEnterSearchMode : SearchToolbar()
            object OnExitSearchMode : SearchToolbar()
        }

        sealed class MarkupToolbar : Event() {
            object OnMarkupColorToggleClicked : MarkupToolbar()
            object OnMarkupHighlightToggleClicked : MarkupToolbar()
            object OnMarkupToolbarUrlClicked : MarkupToolbar()
        }

        /**
         * Styling-toolbar-related events
         */

        sealed class StylingToolbar : Event() {
            data class OnUpdateTextToolbar(val state: StyleToolbarState.Text) : StylingToolbar()
            data class OnUpdateBackgroundToolbar(
                val state: StyleToolbarState.Background
            ) : StylingToolbar()

            data class OnClose(val focused: Boolean) : StylingToolbar()
            object OnCloseMulti : StylingToolbar()
            object OnExit : StylingToolbar()
            object OnBackgroundClosed : StylingToolbar()
        }

        /**
         * Texted Block color & background toolbar related events
         */
        sealed class ColorBackgroundToolbar : Event() {

            data class Show(
                val state: StyleToolbarState.ColorBackground,
                val navigateFromStylingTextToolbar: Boolean,
                val navigatedFromCellsMenu: Boolean
            ) : ColorBackgroundToolbar()

            data class Update(
                val state: StyleToolbarState.ColorBackground
            ) : ColorBackgroundToolbar()

            data class Hide(val focused: Boolean) : ColorBackgroundToolbar()
        }

        /**
         * Texted Block other toolbar related events
         */
        sealed class OtherToolbar : Event() {

            data class Show(
                val state: StyleToolbarState.Other,
                val navigateFromStylingTextToolbar: Boolean,
                val navigatedFromCellsMenu: Boolean
            ) : OtherToolbar()

            data class Update(
                val state: StyleToolbarState.Other
            ) : OtherToolbar()

            object Hide : OtherToolbar()
        }

        /**
         * Multi-select-related events
         */
        sealed class MultiSelect : Event() {
            data class OnEnter(val count: Int = 0) : MultiSelect()
            object OnExit : MultiSelect()
            object OnDelete : MultiSelect()
            object OnTurnInto : MultiSelect()
            data class OnBlockClick(val count: Int) : MultiSelect()
        }

        /**
         * Read mode events
         */
        sealed class ReadMode : Event() {
            object OnEnter : ReadMode()
            object OnExit : ReadMode()
        }

        /**
         * Scroll-and-move-related events.
         */
        sealed class SAM : Event() {
            object OnApply : SAM()
            object OnExit : SAM()
            object OnEnter : SAM()
            data class OnQuickStart(val countOnStart: Int) : SAM()
        }

        /**
         * Mention-related events.
         */
        sealed class Mentions : Event() {
            data class OnStart(val cursorCoordinate: Int, val mentionFrom: Int) : Mentions()
            data class OnQuery(val text: String) : Mentions()
            data class OnResult(val mentions: List<DefaultObjectView>, val text: String) :
                Mentions()

            object OnMentionClicked : Mentions()
            object OnStop : Mentions()
        }

        sealed class Slash : Event() {
            data class OnStart(
                val cursorCoordinate: Int,
                val slashFrom: Int
            ) : Slash()

            data class OnFilterChange(
                val widgetState: SlashWidgetState
            ) : Slash()

            object OnStop : Slash()
            object OnStopAndClearFocus : Slash()
        }

        sealed class OnRefresh : Event() {
            data class Markup(val target: Block?, val selection: IntRange?) : OnRefresh()
        }

        object OnDocumentMenuClicked : Event()
        object OnDocumentIconClicked : Event()

        sealed class ObjectTypesWidgetEvent : Event() {
            data class Show(val data: List<ObjectTypeView>) : ObjectTypesWidgetEvent()
            object Hide : ObjectTypesWidgetEvent()
        }

        sealed class SimpleTableWidget : Event() {
            data class ShowCellTab(
                val tableId: Id,
                val cellItems: List<SimpleTableWidgetItem> = emptyList(),
                val cellSize: Int
            ) : SimpleTableWidget()

            data class ShowColumnTab(
                val tableId: Id,
                val columnItems: List<SimpleTableWidgetItem> = emptyList(),
                val columnsSize: Int
            ) : SimpleTableWidget()

            data class ShowRowTab(
                val tableId: Id,
                val rowItems: List<SimpleTableWidgetItem> = emptyList(),
                val rowsSize: Int
            ) : SimpleTableWidget()

            data class Hide(val tableId: Id) : SimpleTableWidget()
        }
    }

    /**
     * Concrete reducer implementation that holds all the logic related to control panels.
     */
    class Reducer(private val featureToggles: FeatureToggles) :
        StateReducer<ControlPanelState, Event> {

        override val function: suspend (ControlPanelState, Event) -> ControlPanelState
            get() = { state, event ->
                logEvent(event)
                logState(text = "BEFORE", state = state)
                val afterState = reduce(state, event)
                logState(text = "AFTER", state = afterState)
                afterState
            }

        override suspend fun reduce(state: ControlPanelState, event: Event) = when (event) {
            is Event.OnSelectionChanged -> {
                val content = event.target.content
                when {
                    content is TextBlock && content.style == Style.TITLE -> {
                        // ignore event for Title
                        state
                    }
                    else -> {
                        when {
                            state.mainToolbar.isVisible -> {
                                if (event.selection.isEmpty() || event.selection.first == event.selection.last) {
                                    state.copy(
                                        markupMainToolbar = Toolbar.MarkupMainToolbar.reset()
                                    )
                                } else {
                                    state.copy(
                                        mainToolbar = Toolbar.Main.reset(),
                                        markupMainToolbar = state.markupMainToolbar.copy(
                                            isVisible = true,
                                            style = event.target.style(event.selection),
                                            supportedTypes = event.target.content.let { content ->
                                                if (content is TextBlock) {
                                                    content.getSupportedMarkupTypes()
                                                } else {
                                                    emptyList()
                                                }
                                            }
                                        ),
                                        objectTypesToolbar = state.objectTypesToolbar.copy(
                                            isVisible = false
                                        )
                                    )
                                }
                            }
                            state.markupMainToolbar.isVisible -> {
                                if (event.selection.isEmpty() || event.selection.first == event.selection.last) {
                                    state.copy(
                                        mainToolbar = state.mainToolbar.copy(
                                            isVisible = true
                                        ),
                                        markupMainToolbar = Toolbar.MarkupMainToolbar.reset(),
                                        markupColorToolbar = state.markupColorToolbar.copy(
                                            isVisible = false
                                        ),
                                        objectTypesToolbar = state.objectTypesToolbar.copy(
                                            isVisible = false
                                        )
                                    )
                                } else {
                                    state.copy(
                                        markupMainToolbar = state.markupMainToolbar.copy(
                                            style = event.target.style(event.selection)
                                        ),
                                        objectTypesToolbar = state.objectTypesToolbar.copy(
                                            isVisible = false
                                        )
                                    )
                                }
                            }
                            state.mentionToolbar.isVisible -> {
                                val newMentionToolbarState =
                                    handleOnSelectionChangedForMentionState(
                                        state = state.mentionToolbar,
                                        start = event.selection.first
                                    )
                                state.copy(
                                    mentionToolbar = newMentionToolbarState,
                                    mainToolbar = state.mainToolbar.copy(
                                        isVisible = !newMentionToolbarState.isVisible
                                    )
                                )
                            }
                            else -> {
                                state.copy(
                                    mainToolbar = state.mainToolbar.copy(
                                        isVisible = true,
                                    ),
                                    navigationToolbar = state.navigationToolbar.copy(
                                        isVisible = false
                                    )
                                )
                            }
                        }
                    }
                }
            }
            is Event.StylingToolbar -> {
                handleStylingToolbarEvent(event, state)
            }
            is Event.ColorBackgroundToolbar -> {
                handleColorBackgroundToolbarEvents(event, state)
            }
            is Event.OtherToolbar -> {
                handleOtherToolbarEvents(event, state)
            }
            is Event.MarkupToolbar.OnMarkupColorToggleClicked -> {
                val isVisible = if (state.markupColorToolbar.isVisible) {
                    state.markupMainToolbar.isBackgroundColorSelected
                } else {
                    true
                }
                state.copy(
                    markupColorToolbar = state.markupColorToolbar.copy(
                        isVisible = isVisible
                    ),
                    markupMainToolbar = state.markupMainToolbar.copy(
                        isBackgroundColorSelected = false,
                        isTextColorSelected = isVisible
                    )
                )
            }
            is Event.MarkupToolbar.OnMarkupHighlightToggleClicked -> {
                val isVisible = if (state.markupColorToolbar.isVisible) {
                    state.markupMainToolbar.isTextColorSelected
                } else {
                    true
                }
                state.copy(
                    markupColorToolbar = state.markupColorToolbar.copy(
                        isVisible = isVisible
                    ),
                    markupMainToolbar = state.markupMainToolbar.copy(
                        isTextColorSelected = false,
                        isBackgroundColorSelected = isVisible
                    )
                )
            }
            is Event.MarkupToolbar.OnMarkupToolbarUrlClicked -> {
                state.copy(
                    markupColorToolbar = state.markupColorToolbar.copy(
                        isVisible = false
                    ),
                    markupMainToolbar = state.markupMainToolbar.copy()
                )
            }
            is Event.OnMarkupTextColorSelected -> state.copy()
            is Event.OnBlockTextColorSelected -> state.copy()
            is Event.OnBlockStyleSelected -> state.copy()
            is Event.OnAddBlockToolbarOptionSelected -> state.copy()
            is Event.OnMarkupBackgroundColorSelected -> state.copy()
            is Event.OnClearFocusClicked -> init()
            is Event.OnTextInputClicked -> {
                if (state.styleTextToolbar.isVisible) {
                    state.copy(
                        styleTextToolbar = Toolbar.Styling.reset(),
                        mainToolbar = state.mainToolbar.copy(isVisible = true),
                        navigationToolbar = state.navigationToolbar.copy(
                            isVisible = false
                        ),
                        slashWidget = Toolbar.SlashWidget.reset(),
                        objectTypesToolbar = Toolbar.ObjectTypes.reset(),
                        styleBackgroundToolbar = Toolbar.Styling.Background.reset(),
                        simpleTableWidget = Toolbar.SimpleTableWidget.reset()
                    )
                } else {
                    state.copy(
                        slashWidget = Toolbar.SlashWidget.reset()
                    )
                }
            }
            is Event.OnRefresh.Markup -> {
                if (event.target != null && event.selection != null) {
                    state.copy(
                        markupMainToolbar = state.markupMainToolbar.copy(
                            style = event.target.style(event.selection)
                        )
                    )
                } else {
                    state.copy()
                }
            }
            is Event.MultiSelect -> {
                handleMultiSelectEvent(event, state)
            }
            is Event.SearchToolbar.OnEnterSearchMode -> state.copy(
                searchToolbar = Toolbar.SearchToolbar(isVisible = true),
                mainToolbar = Toolbar.Main.reset(),
                multiSelect = Toolbar.MultiSelect(
                    isVisible = false,
                    isScrollAndMoveEnabled = false,
                    count = 0
                ),
                styleTextToolbar = Toolbar.Styling.reset(),
                navigationToolbar = Toolbar.Navigation(isVisible = false),
                objectTypesToolbar = Toolbar.ObjectTypes.reset(),
                styleBackgroundToolbar = Toolbar.Styling.Background.reset()
            )
            is Event.SearchToolbar.OnExitSearchMode -> state.copy(
                searchToolbar = state.searchToolbar.copy(isVisible = false),
                navigationToolbar = state.navigationToolbar.copy(isVisible = true)
            )
            is Event.SAM -> {
                handleScrollAndMoveEvent(event, state)
            }
            is Event.ReadMode -> {
                handleReadModeEvent(event, state)
            }
            is Event.Mentions -> {
                handleMentionEvent(event, state)
            }
            is Event.Slash -> {
                handleSlashEvent(event, state)
            }
            is Event.OnFocusChanged -> {
                when {
                    state.multiSelect.isVisible -> state.copy(
                        mentionToolbar = Toolbar.MentionToolbar.reset(),
                        slashWidget = Toolbar.SlashWidget.reset()
                    )
                    !state.mainToolbar.isVisible -> state.copy(
                        mainToolbar = state.mainToolbar.copy(
                            isVisible = true,
                            targetBlockType = when (event.type) {
                                Toolbar.Main.TargetBlockType.Any -> Toolbar.Main.TargetBlockType.Any
                                Toolbar.Main.TargetBlockType.Cell -> Toolbar.Main.TargetBlockType.Cell
                                Toolbar.Main.TargetBlockType.Title -> Toolbar.Main.TargetBlockType.Title
                            }
                        ),
                        styleTextToolbar = Toolbar.Styling.reset(),
                        mentionToolbar = Toolbar.MentionToolbar.reset(),
                        slashWidget = Toolbar.SlashWidget.reset(),
                        markupMainToolbar = Toolbar.MarkupMainToolbar.reset(),
                        navigationToolbar = Toolbar.Navigation.reset(),
                        styleBackgroundToolbar = Toolbar.Styling.Background.reset(),
                        simpleTableWidget = Toolbar.SimpleTableWidget.reset()
                    )
                    else -> {
                        state.copy(
                            mainToolbar = state.mainToolbar.copy(
                                targetBlockType = when (event.type) {
                                    Toolbar.Main.TargetBlockType.Any -> Toolbar.Main.TargetBlockType.Any
                                    Toolbar.Main.TargetBlockType.Cell -> Toolbar.Main.TargetBlockType.Cell
                                    Toolbar.Main.TargetBlockType.Title -> Toolbar.Main.TargetBlockType.Title
                                }
                            ),
                            styleTextToolbar = Toolbar.Styling.reset(),
                            mentionToolbar = Toolbar.MentionToolbar.reset(),
                            slashWidget = Toolbar.SlashWidget.reset(),
                            objectTypesToolbar = Toolbar.ObjectTypes.reset(),
                            styleBackgroundToolbar = Toolbar.Styling.Background.reset(),
                            simpleTableWidget = Toolbar.SimpleTableWidget.reset()
                        )
                    }
                }
            }
            Event.OnDocumentMenuClicked -> {
                state.copy(
                    slashWidget = Toolbar.SlashWidget.reset(),
                    objectTypesToolbar = Toolbar.ObjectTypes.reset()
                )
            }
            Event.OnDocumentIconClicked -> {
                state.copy(
                    slashWidget = Toolbar.SlashWidget.reset(),
                    objectTypesToolbar = Toolbar.ObjectTypes.reset()
                )
            }
            is Event.ObjectTypesWidgetEvent.Show -> {
                state.copy(
                    objectTypesToolbar = state.objectTypesToolbar.copy(
                        isVisible = true,
                        data = event.data
                    )
                )
            }
            Event.ObjectTypesWidgetEvent.Hide -> {
                state.copy(
                    objectTypesToolbar = state.objectTypesToolbar.copy(
                        isVisible = false,
                        data = listOf()
                    )
                )
            }
            is Event.SimpleTableWidget -> {
                handleSimpleTableEvent(event, state)
            }
        }

        private fun handleStylingToolbarEvent(
            event: Event.StylingToolbar,
            state: ControlPanelState
        ): ControlPanelState = when (event) {
            is Event.StylingToolbar.OnUpdateTextToolbar -> {
                state.copy(
                    mainToolbar = Toolbar.Main.reset(),
                    styleTextToolbar = state.styleTextToolbar.copy(
                        isVisible = true,
                        state = event.state
                    ),
                    navigationToolbar = Toolbar.Navigation.reset(),
                    objectTypesToolbar = Toolbar.ObjectTypes.reset(),
                    styleBackgroundToolbar = Toolbar.Styling.Background.reset()
                )
            }
            is Event.StylingToolbar.OnClose -> {
                if (event.focused) {
                    state.copy(
                        mainToolbar = state.mainToolbar.copy(
                            isVisible = true
                        ),
                        navigationToolbar = state.navigationToolbar.copy(
                            isVisible = false
                        ),
                        styleTextToolbar = Toolbar.Styling.reset(),
                        styleBackgroundToolbar = Toolbar.Styling.Background.reset()
                    )
                } else {
                    init()
                }
            }
            is Event.StylingToolbar.OnCloseMulti -> {
                state.copy(
                    styleTextToolbar = Toolbar.Styling.reset(),
                    styleColorBackgroundToolbar = Toolbar.Styling.ColorBackground.reset(),
                    styleExtraToolbar = Toolbar.Styling.Extra.reset(),
                    styleBackgroundToolbar = Toolbar.Styling.Background.reset()
                )
            }
            is Event.StylingToolbar.OnExit -> {
                state.copy(
                    mainToolbar = state.mainToolbar.copy(
                        isVisible = true
                    ),
                    navigationToolbar = state.navigationToolbar.copy(
                        isVisible = false
                    ),
                    styleTextToolbar = Toolbar.Styling.reset(),
                    styleColorBackgroundToolbar = Toolbar.Styling.ColorBackground.reset(),
                    styleExtraToolbar = Toolbar.Styling.Extra.reset(),
                    styleBackgroundToolbar = Toolbar.Styling.Background.reset()
                )
            }
            is Event.StylingToolbar.OnBackgroundClosed -> {
                state.copy(
                    styleBackgroundToolbar = Toolbar.Styling.Background.reset()
                )
            }
            is Event.StylingToolbar.OnUpdateBackgroundToolbar -> {
                state.copy(
                    mainToolbar = state.mainToolbar.copy(
                        isVisible = false
                    ),
                    styleTextToolbar = Toolbar.Styling.reset(),
                    navigationToolbar = state.navigationToolbar.copy(
                        isVisible = false
                    ),
                    objectTypesToolbar = Toolbar.ObjectTypes.reset(),
                    styleColorBackgroundToolbar = Toolbar.Styling.ColorBackground.reset(),
                    styleBackgroundToolbar = Toolbar.Styling.Background(
                        isVisible = true,
                        state = event.state
                    )
                )
            }
        }

        private fun handleColorBackgroundToolbarEvents(
            event: Event.ColorBackgroundToolbar,
            state: ControlPanelState
        ): ControlPanelState {
            return when (event) {
                is Event.ColorBackgroundToolbar.Show -> {
                    state.copy(
                        styleColorBackgroundToolbar = Toolbar.Styling.ColorBackground(
                            isVisible = true,
                            state = event.state,
                            navigatedFromStylingTextToolbar = event.navigateFromStylingTextToolbar,
                            navigatedFromCellsMenu = event.navigatedFromCellsMenu
                        ),
                        simpleTableWidget = state.simpleTableWidget.copy(
                            isVisible = false
                        ),
                        mainToolbar = Toolbar.Main.reset(),
                        navigationToolbar = Toolbar.Navigation.reset(),
                        styleTextToolbar = Toolbar.Styling.reset(),
                        styleBackgroundToolbar = Toolbar.Styling.Background.reset(),
                        styleExtraToolbar = Toolbar.Styling.Extra.reset()
                    )
                }
                is Event.ColorBackgroundToolbar.Update -> {
                    state.copy(
                        mainToolbar = Toolbar.Main.reset(),
                        styleColorBackgroundToolbar = state.styleColorBackgroundToolbar.copy(
                            state = event.state
                        ),
                        simpleTableWidget = state.simpleTableWidget.copy(
                            isVisible = false
                        ),
                        navigationToolbar = Toolbar.Navigation.reset(),
                        styleTextToolbar = Toolbar.Styling.reset(),
                        styleBackgroundToolbar = Toolbar.Styling.Background.reset(),
                        styleExtraToolbar = Toolbar.Styling.Extra.reset()
                    )
                }
                is Event.ColorBackgroundToolbar.Hide -> {
                    var mainToolbar = Toolbar.Main.reset()
                    var navigationToolbar = Toolbar.Navigation.reset()
                    var styleTextToolbar = state.styleTextToolbar
                    var simpleTableWidget = state.simpleTableWidget
                    when {
                        state.styleColorBackgroundToolbar.navigatedFromStylingTextToolbar -> {
                            styleTextToolbar = styleTextToolbar.copy(
                                isVisible = true
                            )
                        }
                        state.styleColorBackgroundToolbar.navigatedFromCellsMenu -> {
                            simpleTableWidget = simpleTableWidget.copy(
                                isVisible = true
                            )
                        }
                        !state.styleColorBackgroundToolbar.navigatedFromStylingTextToolbar &&
                                !state.styleColorBackgroundToolbar.navigatedFromCellsMenu -> {
                            if (event.focused) {
                                mainToolbar = mainToolbar.copy(isVisible = true)
                            } else {
                                navigationToolbar = navigationToolbar.copy(isVisible = true)
                            }
                        }
                    }
                    state.copy(
                        styleTextToolbar = styleTextToolbar,
                        simpleTableWidget = simpleTableWidget,
                        mainToolbar = mainToolbar,
                        navigationToolbar = navigationToolbar,
                        styleColorBackgroundToolbar = Toolbar.Styling.ColorBackground.reset(),
                        styleBackgroundToolbar = Toolbar.Styling.Background.reset(),
                    )
                }
            }
        }

        private fun handleOtherToolbarEvents(
            event: Event.OtherToolbar,
            state: ControlPanelState
        ): ControlPanelState {
            return when (event) {
                is Event.OtherToolbar.Show -> {
                    state.copy(
                        styleTextToolbar = Toolbar.Styling.reset(),
                        styleExtraToolbar = Toolbar.Styling.Extra(
                            isVisible = true,
                            state = event.state,
                            navigatedFromStylingTextToolbar = event.navigateFromStylingTextToolbar,
                            navigatedFromCellsMenu = event.navigatedFromCellsMenu
                        ),
                        simpleTableWidget = state.simpleTableWidget.copy(
                            isVisible = false
                        ),
                        styleBackgroundToolbar = Toolbar.Styling.Background.reset()
                    )
                }
                is Event.OtherToolbar.Update -> {
                    state.copy(
                        styleTextToolbar = Toolbar.Styling.reset(),
                        styleExtraToolbar = state.styleExtraToolbar.copy(
                            state = event.state
                        ),
                        simpleTableWidget = state.simpleTableWidget.copy(
                            isVisible = false
                        ),
                        styleBackgroundToolbar = Toolbar.Styling.Background.reset()
                    )
                }
                Event.OtherToolbar.Hide -> {
                    state.copy(
                        styleExtraToolbar = Toolbar.Styling.Extra.reset(),
                        simpleTableWidget = state.simpleTableWidget.copy(
                            isVisible = state.styleExtraToolbar.navigatedFromCellsMenu
                        ),
                        styleTextToolbar = state.styleTextToolbar.copy(
                            isVisible = state.styleColorBackgroundToolbar.navigatedFromStylingTextToolbar
                        ),
                        styleBackgroundToolbar = Toolbar.Styling.Background.reset()
                    )
                }
            }
        }

        private fun handleMentionEvent(
            event: Event.Mentions,
            state: ControlPanelState
        ): ControlPanelState = when (event) {
            is Event.Mentions.OnStart -> state.copy(
                mentionToolbar = state.mentionToolbar.copy(
                    isVisible = true,
                    cursorCoordinate = event.cursorCoordinate,
                    mentionFilter = "",
                    updateList = false,
                    mentionFrom = event.mentionFrom
                ),
                mainToolbar = Toolbar.Main(isVisible = false)
            )
            is Event.Mentions.OnStop -> state.copy(
                mentionToolbar = state.mentionToolbar.copy(
                    isVisible = false,
                    cursorCoordinate = null,
                    updateList = false,
                    mentionFrom = null,
                    mentionFilter = null,
                    mentions = emptyList()
                ),
                mainToolbar = Toolbar.Main(isVisible = true)
            )
            is Event.Mentions.OnResult -> state.copy(
                mentionToolbar = state.mentionToolbar.copy(
                    mentions = event.mentions,
                    mentionFilter = event.text,
                    updateList = true
                )
            )
            is Event.Mentions.OnMentionClicked -> state.copy(
                mentionToolbar = state.mentionToolbar.copy(
                    isVisible = false,
                    cursorCoordinate = null,
                    mentionFrom = null,
                    updateList = true,
                    mentionFilter = null,
                    mentions = emptyList()
                )
            )
            is Event.Mentions.OnQuery -> state.copy(
                mentionToolbar = state.mentionToolbar.copy(
                    mentionFilter = event.text,
                    updateList = false
                )
            )
        }

        private fun handleSlashEvent(
            event: Event.Slash,
            state: ControlPanelState
        ): ControlPanelState = when (event) {
            is Event.Slash.OnStart -> {
                state.copy(
                    slashWidget = state.slashWidget.copy(
                        isVisible = true,
                        from = event.slashFrom,
                        filter = "",
                        widgetState = null,
                        cursorCoordinate = event.cursorCoordinate,
                        updateList = false,
                        items = emptyList()
                    ),
                    mainToolbar = state.mainToolbar.copy(
                        isVisible = false
                    ),
                    navigationToolbar = state.navigationToolbar.copy(
                        isVisible = false
                    )
                )
            }
            Event.Slash.OnStop -> {
                state.copy(slashWidget = Toolbar.SlashWidget.reset())
            }
            is Event.Slash.OnStopAndClearFocus -> {
                state.copy(
                    slashWidget = Toolbar.SlashWidget.reset(),
                    mainToolbar = state.mainToolbar.copy(isVisible = false),
                    navigationToolbar = state.navigationToolbar.copy(isVisible = true)
                )
            }
            is Event.Slash.OnFilterChange -> {
                state.copy(
                    slashWidget = state.slashWidget.copy(
                        widgetState = event.widgetState
                    )
                )
            }
        }

        private fun handleMultiSelectEvent(
            event: Event.MultiSelect,
            state: ControlPanelState
        ): ControlPanelState = when (event) {
            is Event.MultiSelect.OnEnter -> state.copy(
                mainToolbar = state.mainToolbar.copy(
                    isVisible = false
                ),
                multiSelect = state.multiSelect.copy(
                    isVisible = true,
                    count = event.count
                ),
                navigationToolbar = state.navigationToolbar.copy(
                    isVisible = false
                ),
                slashWidget = Toolbar.SlashWidget.reset(),
                mentionToolbar = Toolbar.MentionToolbar.reset(),
                simpleTableWidget = Toolbar.SimpleTableWidget.reset()
            )
            is Event.MultiSelect.OnExit -> state.copy(
                multiSelect = state.multiSelect.copy(
                    isVisible = false,
                    isScrollAndMoveEnabled = false,
                    isQuickScrollAndMoveMode = false,
                    count = NO_BLOCK_SELECTED
                ),
                mainToolbar = state.mainToolbar.copy(
                    isVisible = false
                ),
                navigationToolbar = state.navigationToolbar.copy(
                    isVisible = true
                ),
                styleColorBackgroundToolbar = Toolbar.Styling.ColorBackground.reset(),
                styleExtraToolbar = Toolbar.Styling.Extra.reset(),
                styleTextToolbar = Toolbar.Styling.reset(),
                styleBackgroundToolbar = Toolbar.Styling.Background.reset()
            )
            is Event.MultiSelect.OnDelete -> state.copy(
                multiSelect = state.multiSelect.copy(
                    count = NO_BLOCK_SELECTED
                )
            )
            is Event.MultiSelect.OnTurnInto -> state.copy(
                multiSelect = state.multiSelect.copy(
                    count = NO_BLOCK_SELECTED
                )
            )
            is Event.MultiSelect.OnBlockClick -> state.copy(
                multiSelect = state.multiSelect.copy(
                    count = event.count
                )
            )
        }

        private fun handleReadModeEvent(
            event: Event.ReadMode,
            state: ControlPanelState
        ): ControlPanelState = when (event) {
            Event.ReadMode.OnEnter -> {
                state.copy(
                    mainToolbar = state.mainToolbar.copy(
                        isVisible = false
                    ),
                    multiSelect = state.multiSelect.copy(
                        isVisible = false
                    ),
                    styleTextToolbar = Toolbar.Styling.reset(),
                    mentionToolbar = Toolbar.MentionToolbar.reset(),
                    slashWidget = Toolbar.SlashWidget.reset(),
                    styleBackgroundToolbar = Toolbar.Styling.Background.reset(),
                    simpleTableWidget = Toolbar.SimpleTableWidget.reset()
                )
            }
            Event.ReadMode.OnExit -> state.copy()
        }

        private fun handleScrollAndMoveEvent(
            event: Event.SAM,
            state: ControlPanelState
        ): ControlPanelState = when (event) {
            is Event.SAM.OnExit -> {
                if (state.multiSelect.isQuickScrollAndMoveMode) {
                    state.copy(
                        multiSelect = state.multiSelect.copy(
                            isVisible = false,
                            isScrollAndMoveEnabled = false,
                            isQuickScrollAndMoveMode = false,
                            count = NO_BLOCK_SELECTED
                        ),
                        mainToolbar = state.mainToolbar.copy(
                            isVisible = false
                        ),
                        navigationToolbar = state.navigationToolbar.copy(
                            isVisible = true
                        )
                    )
                } else {
                    state.copy(
                        multiSelect = state.multiSelect.copy(
                            isScrollAndMoveEnabled = false,
                            isQuickScrollAndMoveMode = false
                        )
                    )
                }
            }
            is Event.SAM.OnEnter -> state.copy(
                multiSelect = state.multiSelect.copy(
                    isScrollAndMoveEnabled = true
                )
            )
            is Event.SAM.OnQuickStart -> state.copy(
                multiSelect = state.multiSelect.copy(
                    isVisible = true,
                    isScrollAndMoveEnabled = true,
                    isQuickScrollAndMoveMode = true,
                    count = event.countOnStart
                ),
                mainToolbar = state.mainToolbar.copy(
                    isVisible = false
                ),
                navigationToolbar = state.navigationToolbar.copy(
                    isVisible = false
                ),
                slashWidget = Toolbar.SlashWidget.reset()
            )
            is Event.SAM.OnApply -> {
                state.copy(
                    multiSelect = state.multiSelect.copy(
                        isVisible = false,
                        isScrollAndMoveEnabled = false,
                        count = NO_BLOCK_SELECTED
                    ),
                    mainToolbar = state.mainToolbar.copy(
                        isVisible = false
                    ),
                    navigationToolbar = state.navigationToolbar.copy(
                        isVisible = true
                    )
                )
            }
        }

        private fun handleOnSelectionChangedForMentionState(
            state: Toolbar.MentionToolbar,
            start: Int
        ): Toolbar.MentionToolbar {
            val from = state.mentionFrom
            return if (state.isVisible && from != null && start < from) {
                state.copy(
                    isVisible = false,
                    cursorCoordinate = null,
                    mentionFrom = null,
                    updateList = false,
                    mentionFilter = null,
                    mentions = emptyList()
                )
            } else {
                state.copy()
            }
        }

        private fun handleSimpleTableEvent(
            event: Event.SimpleTableWidget,
            state: ControlPanelState
        ): ControlPanelState = when (event) {
            is Event.SimpleTableWidget.Hide -> {
                init().copy(
                    navigationToolbar = state.navigationToolbar.copy(
                        isVisible = true
                    )
                )
            }
            is Event.SimpleTableWidget.ShowCellTab -> {
                init().copy(
                    simpleTableWidget = Toolbar.SimpleTableWidget(
                        isVisible = true,
                        tableId = event.tableId,
                        cellItems = event.cellItems,
                        selectedCount = event.cellSize,
                        tab = BlockView.Table.Tab.CELL
                    )
                )
            }
            is Event.SimpleTableWidget.ShowColumnTab -> {
                init().copy(
                    simpleTableWidget = Toolbar.SimpleTableWidget(
                        isVisible = true,
                        tableId = event.tableId,
                        columnItems = event.columnItems,
                        selectedCount = event.columnsSize,
                        tab = BlockView.Table.Tab.COLUMN
                    )
                )
            }
            is Event.SimpleTableWidget.ShowRowTab -> {
                init().copy(
                    simpleTableWidget = Toolbar.SimpleTableWidget(
                        isVisible = true,
                        tableId = event.tableId,
                        rowItems = event.rowItems,
                        selectedCount = event.rowsSize,
                        tab = BlockView.Table.Tab.ROW
                    )
                )
            }
        }

        private fun logState(text: String, state: ControlPanelState) {
            if (featureToggles.isLogEditorControlPanelMachine) {
                Timber.i(
                    "REDUCER, $text STATE:${state.toPrettyString()}"
                )
            }
        }

        private fun logEvent(event: Event) {
            if (featureToggles.isLogEditorControlPanelMachine) {
                Timber.i(
                    "REDUCER, EVENT:${
                        event::class.qualifiedName?.substringAfter("Event.")
                    }:\n${event.toPrettyString()}"
                )
            }
        }
    }
}