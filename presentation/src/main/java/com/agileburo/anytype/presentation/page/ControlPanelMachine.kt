package com.agileburo.anytype.presentation.page

import com.agileburo.anytype.core_ui.common.Alignment
import com.agileburo.anytype.core_ui.common.Markup
import com.agileburo.anytype.core_ui.common.ThemeColor
import com.agileburo.anytype.core_ui.features.page.styling.StylingMode
import com.agileburo.anytype.core_ui.state.ControlPanelState
import com.agileburo.anytype.core_ui.state.ControlPanelState.Companion.init
import com.agileburo.anytype.core_ui.state.ControlPanelState.Toolbar
import com.agileburo.anytype.core_ui.widgets.toolbar.adapter.Mention
import com.agileburo.anytype.domain.block.model.Block
import com.agileburo.anytype.domain.ext.content
import com.agileburo.anytype.domain.ext.overlap
import com.agileburo.anytype.domain.misc.Overlap
import com.agileburo.anytype.presentation.common.StateReducer
import com.agileburo.anytype.presentation.extension.isInRange
import com.agileburo.anytype.presentation.mapper.marks
import com.agileburo.anytype.presentation.page.ControlPanelMachine.*
import com.agileburo.anytype.presentation.page.editor.getStyleConfig
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
        private val scope: CoroutineScope
    ) : ControlPanelMachine() {

        private val reducer: Reducer = Reducer()
        val channel: Channel<Event> = Channel()
        private val events: Flow<Event> = channel.consumeAsFlow()

        fun onEvent(event: Event) =
            scope.launch { channel.send(event) }.also { Timber.d("Event: $event") }

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

        data class OnMarkupContextMenuTextColorClicked(
            val selection: IntRange,
            val target: Block
        ) : Event()

        data class OnMarkupContextMenuBackgroundColorClicked(
            val selection: IntRange,
            val target: Block
        ) : Event()

        /**
         * Represents an event when user selected a block text color on [Toolbar.Styling] toolbar.
         */
        object OnBlockTextColorSelected : Event()


        data class OnEditorContextMenuStyleClicked(
            val selection: IntRange,
            val target: Block
        ) : Event()


        object OnBlockStyleSelected : Event()

        /**
         * Represents an event when user selected block background color on [Toolbar.Styling] toolbar.
         */
        object OnBlockBackgroundColorSelected : Event()


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

        data class OnBlockActionToolbarTextColorClicked(val target: Block) : Event()
        data class OnBlockActionToolbarBackgroundColorClicked(val target: Block) : Event()
        data class OnBlockActionToolbarStyleClicked(val target: Block) : Event()

        /**
         * Styling-toolbar-related events
         */

        sealed class StylingToolbar : Event() {

            object OnBackgroundSlideSelected : StylingToolbar()
            object OnColorSlideSelected : StylingToolbar()

            /**
             * Represents an event when user selected alignment on [Toolbar.Styling] toolbar.
             */
            object OnAlignmentSelected : StylingToolbar()

            object OnClose : StylingToolbar()
        }

        /**
         * Multi-select-related events
         */
        sealed class MultiSelect : Event() {
            object OnEnter : MultiSelect()
            object OnExit : MultiSelect()
            object OnDelete : MultiSelect()
            object OnTurnInto : MultiSelect()
            data class OnBlockClick(val count: Int) : MultiSelect()
        }

        /**
         * Scroll-and-move-related events.
         */
        sealed class SAM : Event() {
            object OnApply : SAM()
            object OnExit : SAM()
            object OnEnter : SAM()
        }

        /**
         * Mention-related events.
         */
        sealed class Mentions : Event() {
            data class OnStart(val cursorCoordinate: Int, val mentionFrom: Int) : Mentions()
            data class OnQuery(val text: String) : Mentions()
            data class OnResult(val mentions: List<Mention>) : Mentions()
            object OnMentionClicked : Mentions()
            object OnStop : Mentions()
        }

        data class OnRefresh(val target: Block?) : Event()
    }

    /**
     * Concrete reducer implementation that holds all the logic related to control panels.
     */
    class Reducer : StateReducer<ControlPanelState, Event> {

        private val excl = listOf(Overlap.LEFT, Overlap.RIGHT, Overlap.OUTER)
        private val incl = listOf(
            Overlap.EQUAL,
            Overlap.INNER,
            Overlap.LEFT,
            Overlap.RIGHT,
            Overlap.INNER_RIGHT,
            Overlap.INNER_LEFT
        )

        var selection: IntRange? = null

        override val function: suspend (ControlPanelState, Event) -> ControlPanelState
            get() = { state, event -> reduce(state, event) }

        override suspend fun reduce(state: ControlPanelState, event: Event) = when (event) {
            is Event.OnSelectionChanged -> {
                selection = event.selection
                when {
                    state.focus == null -> state.copy()
                    state.stylingToolbar.isVisible -> {
                        handleSelectionChangeForStylingToolbar(event, state)
                    }
                    state.mentionToolbar.isVisible -> state.copy(
                        mentionToolbar = handleSelectionChangeEventForMentionState(
                            state = state.mentionToolbar,
                            start = event.selection.first
                        )
                    )
                    else -> {
                        state.copy(
                            mainToolbar = state.mainToolbar.copy(
                                isVisible = true
                            )
                        )
                    }
                }
            }
            is Event.StylingToolbar -> {
                handleStylingToolbarEvent(event, state)
            }
            is Event.OnMarkupTextColorSelected -> state.copy()
            is Event.OnBlockTextColorSelected -> state.copy()
            is Event.OnBlockBackgroundColorSelected -> state.copy()
            is Event.OnBlockStyleSelected -> state.copy()
            is Event.OnAddBlockToolbarOptionSelected -> state.copy()
            is Event.OnMarkupBackgroundColorSelected -> state.copy()
            is Event.OnMarkupContextMenuTextColorClicked -> {
                selection = event.selection
                val target = target(event.target)
                val props = getMarkupLevelStylingProps(target, event.selection)
                state.copy(
                    mainToolbar = state.mainToolbar.copy(
                        isVisible = false
                    ),
                    stylingToolbar = state.stylingToolbar.copy(
                        isVisible = true,
                        mode = StylingMode.MARKUP,
                        target = target,
                        props = props
                    )
                )
            }
            is Event.OnMarkupContextMenuBackgroundColorClicked -> {
                selection = event.selection
                val target = target(event.target)
                val props = getMarkupLevelStylingProps(target, event.selection)
                state.copy(
                    mainToolbar = state.mainToolbar.copy(
                        isVisible = false
                    ),
                    stylingToolbar = state.stylingToolbar.copy(
                        isVisible = true,
                        mode = StylingMode.MARKUP,
                        target = target,
                        props = props
                    )
                )
            }
            is Event.OnEditorContextMenuStyleClicked -> {
                selection = event.selection
                val config = event.target.getStyleConfig(
                    focus = true,
                    selection = selection
                )
                val target = target(event.target)
                val props = getMarkupLevelStylingProps(target, event.selection)
                state.copy(
                    mainToolbar = state.mainToolbar.copy(
                        isVisible = false
                    ),
                    stylingToolbar = state.stylingToolbar.copy(
                        isVisible = true,
                        config = config,
                        mode = StylingMode.MARKUP,
                        target = target,
                        props = props
                    )
                )
            }
            is Event.OnClearFocusClicked -> init()
            is Event.OnTextInputClicked -> {
                if (state.stylingToolbar.isVisible) {
                    state.copy(
                        stylingToolbar = Toolbar.Styling.reset(),
                        mainToolbar = state.mainToolbar.copy(isVisible = true)
                    )
                } else {
                    state.copy()
                }
            }
            is Event.OnBlockActionToolbarTextColorClicked -> {
                val target = target(event.target)
                state.copy(
                    mainToolbar = state.mainToolbar.copy(
                        isVisible = false
                    ),
                    stylingToolbar = state.stylingToolbar.copy(
                        isVisible = true,
                        mode = StylingMode.BLOCK,
                        target = target,
                        props = Toolbar.Styling.Props(
                            isBold = target.isBold,
                            isItalic = target.isItalic,
                            isStrikethrough = target.isStrikethrough,
                            isCode = target.isCode,
                            isLinked = target.isLinked,
                            color = target.color ?: ThemeColor.DEFAULT.title,
                            background = target.background ?: ThemeColor.DEFAULT.title,
                            alignment = target.alignment
                        )
                    )
                )
            }
            is Event.OnBlockActionToolbarBackgroundColorClicked -> {
                val target = target(event.target)
                state.copy(
                    mainToolbar = state.mainToolbar.copy(
                        isVisible = false
                    ),
                    stylingToolbar = state.stylingToolbar.copy(
                        isVisible = true,
                        mode = StylingMode.BLOCK,
                        target = target,
                        props = Toolbar.Styling.Props(
                            isBold = target.isBold,
                            isItalic = target.isItalic,
                            isStrikethrough = target.isStrikethrough,
                            isCode = target.isCode,
                            isLinked = target.isLinked,
                            color = target.color ?: ThemeColor.DEFAULT.title,
                            background = target.background ?: ThemeColor.DEFAULT.title,
                            alignment = target.alignment
                        )
                    )
                )
            }
            is Event.OnBlockActionToolbarStyleClicked -> {
                val target = target(event.target)
                state.copy(
                    mainToolbar = state.mainToolbar.copy(
                        isVisible = false
                    ),
                    stylingToolbar = state.stylingToolbar.copy(
                        isVisible = true,
                        mode = getModeForSelection(selection),
                        target = target(event.target),
                        config = event.target.getStyleConfig(true, selection),
                        props = getPropsForSelection(target, selection)
                    )
                )
            }
            is Event.OnRefresh -> {
                if (state.stylingToolbar.isVisible) {
                    handleRefreshForMarkupLevelStyling(state, event)
                } else {
                    state.copy()
                }
            }
            is Event.MultiSelect -> {
                handleMultiSelectEvent(event, state)
            }
            is Event.SAM -> {
                handleScrollAndMoveEvent(event, state)
            }
            is Event.Mentions -> {
                handleMentionEvent(event, state)
            }
            is Event.OnFocusChanged -> {
                when {
                    state.multiSelect.isVisible -> state.copy(
                        mentionToolbar = Toolbar.MentionToolbar.reset()
                    )
                    !state.mainToolbar.isVisible -> state.copy(
                        mainToolbar = state.mainToolbar.copy(
                            isVisible = true
                        ),
                        stylingToolbar = Toolbar.Styling.reset(),
                        focus = ControlPanelState.Focus(
                            id = event.id,
                            type = ControlPanelState.Focus.Type.valueOf(
                                value = event.style.name
                            )
                        ),
                        mentionToolbar = Toolbar.MentionToolbar.reset()
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
                            ),
                            mentionToolbar = Toolbar.MentionToolbar.reset()
                        )
                    }
                }
            }
        }

        private fun handleRefreshForMarkupLevelStyling(
            state: ControlPanelState,
            event: Event.OnRefresh
        ): ControlPanelState {
            return if (state.stylingToolbar.mode == StylingMode.MARKUP) {
                if (event.target != null) {
                    val target = target(event.target)
                    selection?.let {
                        val props = getMarkupLevelStylingProps(target, it)
                        state.copy(
                            stylingToolbar = state.stylingToolbar.copy(
                                props = props,
                                target = target
                            )
                        )
                    } ?: state.copy()
                } else {
                    state.copy()
                }
            } else {
                val target = event.target?.let { target(it) }
                state.copy(
                    stylingToolbar = state.stylingToolbar.copy(
                        target = target,
                        props = target?.let {
                            Toolbar.Styling.Props(
                                isBold = it.isBold,
                                isItalic = it.isItalic,
                                isStrikethrough = it.isStrikethrough,
                                isCode = it.isCode,
                                isLinked = it.isLinked,
                                color = it.color,
                                background = it.background,
                                alignment = it.alignment
                            )
                        }
                    )
                )
            }
        }

        private fun getModeForSelection(selection: IntRange?): StylingMode {
            return if (selection != null && selection.first != selection.last) StylingMode.MARKUP
            else StylingMode.BLOCK
        }

        private fun getPropsForSelection(target: Toolbar.Styling.Target, selection: IntRange?)
                : Toolbar.Styling.Props {
            return if (selection != null && selection.first != selection.last) {
                getMarkupLevelStylingProps(target, selection)
            } else {
                Toolbar.Styling.Props(
                    isBold = target.isBold,
                    isItalic = target.isItalic,
                    isStrikethrough = target.isStrikethrough,
                    isCode = target.isCode,
                    isLinked = target.isLinked,
                    color = target.color,
                    background = target.background,
                    alignment = target.alignment
                )
            }
        }

        private fun getMarkupLevelStylingProps(
            target: Toolbar.Styling.Target,
            selection: IntRange
        ): Toolbar.Styling.Props {

            var color: String? = null
            var background: String? = null

            val colorOverlaps = mutableListOf<Overlap>()
            val backgroundOverlaps = mutableListOf<Overlap>()

            target.marks.forEach { mark ->
                if (mark.type == Markup.Type.TEXT_COLOR) {
                    val range = mark.from..mark.to
                    val overlap = selection.overlap(range)
                    if (incl.contains(overlap))
                        color = mark.param
                    else
                        colorOverlaps.add(overlap)
                } else if (mark.type == Markup.Type.BACKGROUND_COLOR) {
                    val range = mark.from..mark.to
                    val overlap = selection.overlap(range)
                    if (incl.contains(overlap))
                        background = mark.param
                    else
                        backgroundOverlaps.add(overlap)
                }
            }

            if (color == null) {
                if (colorOverlaps.isEmpty() || colorOverlaps.none { value -> excl.contains(value) })
                    color = target.color ?: ThemeColor.DEFAULT.title
            }

            if (background == null) {
                if (backgroundOverlaps.isEmpty() || backgroundOverlaps.none { value ->
                        excl.contains(
                            value
                        )
                    })
                    background = target.background ?: ThemeColor.DEFAULT.title
            }

            return Toolbar.Styling.Props(
                isBold = Markup.Type.BOLD.isInRange(target.marks, selection),
                isItalic = Markup.Type.ITALIC.isInRange(target.marks, selection),
                isStrikethrough = Markup.Type.STRIKETHROUGH.isInRange(target.marks, selection),
                isCode = Markup.Type.KEYBOARD.isInRange(target.marks, selection),
                isLinked = Markup.Type.LINK.isInRange(target.marks, selection),
                color = color,
                background = background,
                alignment = target.alignment
            )
        }

        private fun handleSelectionChangeForStylingToolbar(
            event: Event.OnSelectionChanged,
            state: ControlPanelState
        ): ControlPanelState {
            Timber.d("handleSelectionChangeForStylingToolbar")
            return if (event.selection.first != event.selection.last) {
                if (state.stylingToolbar.mode == StylingMode.MARKUP) {
                    val target = state.stylingToolbar.target
                    selection = event.selection
                    if (target != null) {
                        state.copy(
                            stylingToolbar = state.stylingToolbar.copy(
                                props = getMarkupLevelStylingProps(
                                    target = target,
                                    selection = event.selection
                                )
                            )
                        )
                    } else {
                        state.copy()
                    }
                } else {
                    state.copy()
                }
            } else {
                state.copy()
            }
        }

        private fun handleStylingToolbarEvent(
            event: Event.StylingToolbar,
            state: ControlPanelState
        ): ControlPanelState = when (event) {
            is Event.StylingToolbar.OnBackgroundSlideSelected -> state.copy()
            is Event.StylingToolbar.OnColorSlideSelected -> state.copy()
            is Event.StylingToolbar.OnAlignmentSelected -> state.copy()
            is Event.StylingToolbar.OnClose -> state.copy(
                stylingToolbar = Toolbar.Styling.reset(),
                mainToolbar = state.mainToolbar.copy(
                    isVisible = true
                )
            )
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
                )
            )
            is Event.Mentions.OnStop -> state.copy(
                mentionToolbar = state.mentionToolbar.copy(
                    isVisible = false,
                    cursorCoordinate = null,
                    updateList = true,
                    mentionFrom = null,
                    mentionFilter = null,
                    mentions = emptyList()
                )
            )
            is Event.Mentions.OnResult -> state.copy(
                mentionToolbar = state.mentionToolbar.copy(
                    mentions = event.mentions,
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
                    count = NO_BLOCK_SELECTED
                )
            )
            is Event.MultiSelect.OnExit -> state.copy(
                focus = null,
                multiSelect = state.multiSelect.copy(
                    isVisible = false,
                    isScrollAndMoveEnabled = false,
                    count = NO_BLOCK_SELECTED
                ),
                mainToolbar = state.mainToolbar.copy(
                    isVisible = false
                )
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

        private fun handleScrollAndMoveEvent(
            event: Event.SAM,
            state: ControlPanelState
        ): ControlPanelState = when (event) {
            is Event.SAM.OnExit -> state.copy(
                multiSelect = state.multiSelect.copy(
                    isScrollAndMoveEnabled = false
                )
            )
            is Event.SAM.OnEnter -> state.copy(
                multiSelect = state.multiSelect.copy(
                    isScrollAndMoveEnabled = true
                )
            )
            is Event.SAM.OnApply -> state.copy(
                multiSelect = state.multiSelect.copy(
                    count = NO_BLOCK_SELECTED,
                    isScrollAndMoveEnabled = false
                )
            )
        }

        private fun handleSelectionChangeEventForMentionState(
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

        private fun target(block: Block): Toolbar.Styling.Target {
            val content = block.content<Block.Content.Text>()
            return Toolbar.Styling.Target(
                text = content.text,
                color = content.color,
                background = content.backgroundColor,
                alignment = content.align?.let { alignment ->
                    when (alignment) {
                        Block.Align.AlignLeft -> Alignment.START
                        Block.Align.AlignRight -> Alignment.END
                        Block.Align.AlignCenter -> Alignment.CENTER
                    }
                },
                marks = content.marks()
            )
        }
    }
}