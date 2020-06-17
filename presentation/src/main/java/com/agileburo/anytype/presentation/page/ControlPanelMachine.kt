package com.agileburo.anytype.presentation.page

import com.agileburo.anytype.core_ui.common.Alignment
import com.agileburo.anytype.core_ui.common.Markup
import com.agileburo.anytype.core_ui.common.ThemeColor
import com.agileburo.anytype.core_ui.features.page.styling.StylingMode
import com.agileburo.anytype.core_ui.features.page.styling.StylingType
import com.agileburo.anytype.core_ui.state.ControlPanelState
import com.agileburo.anytype.core_ui.state.ControlPanelState.Toolbar
import com.agileburo.anytype.domain.block.model.Block
import com.agileburo.anytype.domain.ext.content
import com.agileburo.anytype.domain.ext.overlap
import com.agileburo.anytype.domain.misc.Overlap
import com.agileburo.anytype.presentation.common.StateReducer
import com.agileburo.anytype.presentation.mapper.marks
import com.agileburo.anytype.presentation.page.ControlPanelMachine.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.onEach
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
        fun state(): Flow<ControlPanelState> = events.scan(ControlPanelState.init(), reducer.function).onEach {
            Timber.d("Emitting: $it")
        }
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

        object OnStyleBackgroundSlideClicked: Event()
        object OnStyleColorSlideClicked: Event()

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

        private val excl = listOf(Overlap.LEFT, Overlap.RIGHT, Overlap.OUTER)
        private val incl = listOf(Overlap.EQUAL, Overlap.INNER, Overlap.LEFT, Overlap.RIGHT, Overlap.INNER_RIGHT, Overlap.INNER_LEFT)

        var selection: IntRange? = null

        override val function: suspend (ControlPanelState, Event) -> ControlPanelState
            get() = { state, event ->
                reduce(
                    state,
                    event
                ).also { Timber.d("Reducing event:\n$event") }
            }

        override suspend fun reduce(state: ControlPanelState, event: Event) = when (event) {
            is Event.OnSelectionChanged -> {
                when {
                    state.focus == null -> state.copy()
                    state.stylingToolbar.isVisible -> {
                        if (state.stylingToolbar.mode == StylingMode.MARKUP) {

                            val colorOverlaps = mutableListOf<Overlap>()
                            val backgroundOverlaps = mutableListOf<Overlap>()

                            val target = state.stylingToolbar.target
                            val props = state.stylingToolbar.props

                            var color: String? = null
                            var background: String? = null

                            target?.marks?.forEach { mark ->
                                if (mark.type == Markup.Type.TEXT_COLOR) {
                                    val range = mark.from..mark.to
                                    val overlap = event.selection.overlap(range)
                                    if (incl.contains(overlap))
                                        color = mark.param
                                    else
                                        colorOverlaps.add(overlap)
                                } else if (mark.type == Markup.Type.BACKGROUND_COLOR) {
                                    val range = mark.from..mark.to
                                    val overlap = range.overlap(event.selection)
                                    if (incl.contains(overlap))
                                        background = mark.param
                                    else
                                        backgroundOverlaps.add(overlap)
                                }
                            }

                            if (color == null) {
                                if (colorOverlaps.isEmpty() || colorOverlaps.none { value -> excl.contains(value) })
                                    color = target?.color ?: ThemeColor.DEFAULT.title
                            }

                            if (background == null) {
                                if (backgroundOverlaps.isEmpty() || backgroundOverlaps.none { value -> excl.contains(value) })
                                    background = target?.background ?: ThemeColor.DEFAULT.title
                            }

                            selection = event.selection

                            state.copy(
                                stylingToolbar = state.stylingToolbar.copy(
                                    props = props?.copy(
                                        color = color,
                                        background = background
                                    )
                                )
                            )
                        } else {
                            state.copy()
                        }
                    }
                    else -> {
                        state.copy(
                            mainToolbar = state.mainToolbar.copy(
                                isVisible = (!state.multiSelect.isVisible && event.selection.first == event.selection.last)
                            )
                        )
                    }
                }
            }
            is Event.OnStyleBackgroundSlideClicked -> state.copy(
                stylingToolbar = state.stylingToolbar.copy(
                    type = StylingType.BACKGROUND
                )
            )
            is Event.OnStyleColorSlideClicked -> state.copy(
                stylingToolbar = state.stylingToolbar.copy(
                    type = StylingType.TEXT_COLOR
                )
            )
            is Event.OnMarkupTextColorSelected -> state.copy(
                stylingToolbar = state.stylingToolbar.copy(
                    type = StylingType.TEXT_COLOR
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
                    type = StylingType.BACKGROUND
                )
            )
            is Event.OnMarkupContextMenuTextColorClicked -> {

                selection = event.selection

                val target = target(event.target)

                var color: String? = null
                var background: String? = null

                val colorOverlaps = mutableListOf<Overlap>()
                val backgroundOverlaps = mutableListOf<Overlap>()

                target.marks.forEach { mark ->
                    if (mark.type == Markup.Type.TEXT_COLOR) {
                        val range = mark.from..mark.to
                        val overlap = event.selection.overlap(range)
                        if (incl.contains(overlap))
                            color = mark.param
                        else
                            colorOverlaps.add(overlap)
                    } else if (mark.type == Markup.Type.BACKGROUND_COLOR) {
                        val range = mark.from..mark.to
                        val overlap = event.selection.overlap(range)
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
                    if (backgroundOverlaps.isEmpty() || backgroundOverlaps.none { value -> excl.contains(value) })
                        background = target.background ?: ThemeColor.DEFAULT.title
                }

                val props = Toolbar.Styling.Props(
                    isBold = target.isBold,
                    isItalic = target.isItalic,
                    isStrikethrough = target.isStrikethrough,
                    isCode = target.isCode,
                    color = color,
                    background = background,
                    alignment = target.alignment
                )

                state.copy(
                    mainToolbar = state.mainToolbar.copy(
                        isVisible = false
                    ),
                    stylingToolbar = state.stylingToolbar.copy(
                        isVisible = true,
                        mode = StylingMode.MARKUP,
                        type = StylingType.TEXT_COLOR,
                        target = target,
                        props = props
                    )
                )
            }
            is Event.OnMarkupContextMenuBackgroundColorClicked -> {

                selection = event.selection

                val target = target(event.target)

                var color: String? = null
                var background: String? = null

                target.marks.forEach { mark ->
                    if (mark.type == Markup.Type.TEXT_COLOR) {
                        val range = mark.from..mark.to
                        if (range.overlap(event.selection) == Overlap.EQUAL)
                            color = mark.param
                    } else if (mark.type == Markup.Type.BACKGROUND_COLOR) {
                        val range = mark.from..mark.to
                        if (range.overlap(event.selection) == Overlap.EQUAL)
                            background = mark.param
                    }
                }

                val props = Toolbar.Styling.Props(
                    isBold = target.isBold,
                    isItalic = target.isItalic,
                    isStrikethrough = target.isStrikethrough,
                    isCode = target.isCode,
                    color = color,
                    background = background,
                    alignment = target.alignment
                )

                state.copy(
                    mainToolbar = state.mainToolbar.copy(
                        isVisible = false
                    ),
                    stylingToolbar = state.stylingToolbar.copy(
                        isVisible = true,
                        mode = StylingMode.MARKUP,
                        type = StylingType.BACKGROUND,
                        target = target,
                        props = props
                    )
                )
            }
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
            is Event.OnBlockActionToolbarTextColorClicked -> {
                val target = target(event.target)
                state.copy(
                    mainToolbar = state.mainToolbar.copy(
                        isVisible = false
                    ),
                    stylingToolbar = state.stylingToolbar.copy(
                        isVisible = true,
                        mode = StylingMode.BLOCK,
                        type = StylingType.TEXT_COLOR,
                        target = target,
                        props = Toolbar.Styling.Props(
                            isBold = target.isBold,
                            isItalic = target.isItalic,
                            isStrikethrough = target.isStrikethrough,
                            isCode = target.isCode,
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
                        type = StylingType.BACKGROUND,
                        target = target,
                        props = Toolbar.Styling.Props(
                            isBold = target.isBold,
                            isItalic = target.isItalic,
                            isStrikethrough = target.isStrikethrough,
                            isCode = target.isCode,
                            color = target.color ?: ThemeColor.DEFAULT.title,
                            background = target.background ?: ThemeColor.DEFAULT.title,
                            alignment = target.alignment
                        )
                    )
                )
            }
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
                    if (state.stylingToolbar.mode == StylingMode.MARKUP) {
                        event.target?.let { block ->

                            val target = target(block)

                            var color: String? = null
                            var background: String? = null

                            selection?.let {
                                target.marks.forEach { mark ->
                                    if (mark.type == Markup.Type.TEXT_COLOR) {
                                        val range = mark.from..mark.to
                                        if (range.overlap(it) == Overlap.EQUAL)
                                            color = mark.param
                                    } else if (mark.type == Markup.Type.BACKGROUND_COLOR) {
                                        val range = mark.from..mark.to
                                        if (range.overlap(it) == Overlap.EQUAL)
                                            background = mark.param
                                    }
                                }

                                val props = Toolbar.Styling.Props(
                                    isBold = target.isBold,
                                    isItalic = target.isItalic,
                                    isStrikethrough = target.isStrikethrough,
                                    isCode = target.isCode,
                                    color = color,
                                    background = background,
                                    alignment = target.alignment
                                )

                                state.copy(
                                    stylingToolbar = state.stylingToolbar.copy(
                                        props = props,
                                        target = target
                                    )
                                )
                            } ?: state.copy()
                        } ?: state.copy()
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
                                        color = it.color,
                                        background = it.background,
                                        alignment = it.alignment
                                    )
                                }
                            )
                        )
                    }
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

        fun target(block: Block): Toolbar.Styling.Target {
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