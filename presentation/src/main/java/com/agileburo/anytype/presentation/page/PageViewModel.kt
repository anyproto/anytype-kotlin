package com.agileburo.anytype.presentation.page

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.agileburo.anytype.core_ui.common.Markup
import com.agileburo.anytype.core_ui.features.page.BlockView
import com.agileburo.anytype.core_ui.state.ControlPanelState
import com.agileburo.anytype.core_ui.state.ControlPanelState.Toolbar
import com.agileburo.anytype.core_utils.common.EventWrapper
import com.agileburo.anytype.core_utils.ext.withLatestFrom
import com.agileburo.anytype.core_utils.ui.ViewStateViewModel
import com.agileburo.anytype.domain.block.interactor.*
import com.agileburo.anytype.domain.block.model.Block
import com.agileburo.anytype.domain.block.model.Block.Prototype
import com.agileburo.anytype.domain.block.model.Position
import com.agileburo.anytype.domain.common.Id
import com.agileburo.anytype.domain.event.interactor.InterceptEvents
import com.agileburo.anytype.domain.event.model.Event
import com.agileburo.anytype.domain.ext.addMark
import com.agileburo.anytype.domain.ext.asMap
import com.agileburo.anytype.domain.ext.asRender
import com.agileburo.anytype.domain.page.ClosePage
import com.agileburo.anytype.domain.page.OpenPage
import com.agileburo.anytype.presentation.common.StateReducer
import com.agileburo.anytype.presentation.mapper.toView
import com.agileburo.anytype.presentation.navigation.AppNavigation
import com.agileburo.anytype.presentation.navigation.SupportNavigation
import com.agileburo.anytype.presentation.page.PageViewModel.ControlPanelMachine.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber

class PageViewModel(
    private val openPage: OpenPage,
    private val closePage: ClosePage,
    private val updateBlock: UpdateBlock,
    private val createBlock: CreateBlock,
    private val interceptEvents: InterceptEvents,
    private val updateCheckbox: UpdateCheckbox,
    private val unlinkBlocks: UnlinkBlocks,
    private val duplicateBlock: DuplicateBlock
) : ViewStateViewModel<PageViewModel.ViewState>(),
    SupportNavigation<EventWrapper<AppNavigation.Command>> {

    private val controlPanelInteractor = Interactor(viewModelScope)
    val controlPanelViewState = MutableLiveData<ControlPanelState>()

    private val renderingChannel = Channel<List<Block>>()
    private val renderings = renderingChannel.consumeAsFlow()

    private val focusChannel = ConflatedBroadcastChannel(EMPTY_FOCUS_ID)
    private val focusChanges = focusChannel.asFlow()

    private val textChannel = Channel<Triple<Id, String, List<Block.Content.Text.Mark>>>()
    private val textChanges = textChannel.consumeAsFlow()

    private val selectionChannel = Channel<Pair<Id, IntRange>>()
    private val selectionsChanges = selectionChannel.consumeAsFlow()

    private val markupActionChannel = Channel<MarkupAction>()
    private val markupActions = markupActionChannel.consumeAsFlow()

    /**
     * Currently opened page id.
     */
    private var pageId: String = ""

    /**
     * Current set of blocks on this page.
     */
    var blocks: List<Block> = emptyList()
        private set

    private val _focus: MutableLiveData<Id> = MutableLiveData()
    val focus: LiveData<Id> = _focus

    override val navigation = MutableLiveData<EventWrapper<AppNavigation.Command>>()

    init {
        startHandlingTextChanges()
        startProcessingFocusChanges()
        startProcessingControlPanelViewState()
        startObservingEvents()
        processRendering()
        processMarkupChanges()
    }

    private fun startProcessingFocusChanges() {
        viewModelScope.launch {
            focusChanges.collect { _focus.postValue(it) }
        }
    }

    private fun startObservingEvents() {
        viewModelScope.launch {
            interceptEvents.build().collect { event -> handleEvents(event) }
        }
    }

    private fun startProcessingControlPanelViewState() {
        viewModelScope.launch {
            controlPanelInteractor.state().distinctUntilChanged()
                .collect { controlPanelViewState.postValue(it) }
        }
    }

    private fun handleEvents(events: List<Event>) {
        Timber.d("Intercepted events: $events")
        events.forEach { event ->
            Timber.d("Handling event: $event")
            when (event) {
                is Event.Command.ShowBlock -> {
                    blocks = event.blocks
                }
                is Event.Command.AddBlock -> {
                    blocks = blocks + event.blocks
                    viewModelScope.launch { focusChannel.send(event.blocks.last().id) }
                }
                is Event.Command.UpdateStructure -> {
                    blocks = blocks.map { block ->
                        if (block.id == event.id)
                            block.copy(children = event.children)
                        else
                            block
                    }
                }
                is Event.Command.DeleteBlock -> {
                    blocks = blocks.filter { it.id != event.target }
                }
            }
        }

        viewModelScope.launch { renderingChannel.send(blocks) }
    }

    private fun processMarkupChanges() {
        markupActions
            .withLatestFrom(
                selectionsChanges
                    .distinctUntilChanged()
                    .filter { (_, selection) -> selection.first != selection.last }
            ) { a, b -> Pair(a, b) }
            .onEach { (action, selection) ->
                applyMarkup(selection, action)
            }
            .launchIn(viewModelScope)
    }

    private suspend fun applyMarkup(
        selection: Pair<String, IntRange>,
        action: MarkupAction
    ) {
        val targetBlock = blocks.first { it.id == selection.first }
        val targetContent = targetBlock.content as Block.Content.Text

        val mark = Block.Content.Text.Mark(
            range = selection.second,
            type = when (action.type) {
                Markup.Type.BOLD -> Block.Content.Text.Mark.Type.BOLD
                Markup.Type.ITALIC -> Block.Content.Text.Mark.Type.ITALIC
                Markup.Type.STRIKETHROUGH -> Block.Content.Text.Mark.Type.STRIKETHROUGH
                Markup.Type.TEXT_COLOR -> Block.Content.Text.Mark.Type.TEXT_COLOR
            },
            param = action.param
        )

        val marks = targetContent.marks.addMark(mark)

        val newContent = targetContent.copy(
            marks = marks
        )

        val newBlock = targetBlock.copy(content = newContent)

        val update = blocks.map { block ->
            if (block.id != selection.first)
                block
            else
                newBlock
        }

        blocks = update

        renderingChannel.send(blocks)

        proceedWithUpdatingBlock(
            params = UpdateBlock.Params(
                contextId = pageId,
                blockId = newBlock.id,
                text = newContent.text,
                marks = newContent.marks
            )
        )
    }

    private fun processRendering() {
        viewModelScope.launch {
            renderings
                .onEach { Timber.d("New rendering: $it") }
                .withLatestFrom(focusChanges) { models, focus ->
                    models.asMap().asRender(pageId).mapNotNull { block ->
                        when {
                            block.content is Block.Content.Text -> {
                                block.toView(focused = block.id == focus)
                            }
                            block.content is Block.Content.Image -> {
                                block.toView()
                            }
                            else -> null
                        }
                    }
                }
                .collect { dispatchToUI(it) }
        }
    }

    private fun dispatchToUI(views: List<BlockView>) {
        stateData.postValue(ViewState.Success(views))
    }

    private fun startHandlingTextChanges() {
        textChanges
            .debounce(TEXT_CHANGES_DEBOUNCE_DURATION)
            .map { (id, text, marks) ->
                val update = blocks.map { block ->
                    if (block.id == id) {
                        block.copy(
                            content = block.content.asText().copy(
                                text = text,
                                marks = marks
                            )
                        )
                    } else
                        block
                }

                blocks = update

                UpdateBlock.Params(
                    contextId = pageId,
                    blockId = id,
                    text = text,
                    marks = marks
                )
            }
            .onEach { params -> proceedWithUpdatingBlock(params) }
            .launchIn(viewModelScope)
    }

    private fun proceedWithUpdatingBlock(params: UpdateBlock.Params) {
        Timber.d("Starting updating block with params: $params")
        updateBlock.invoke(viewModelScope, params) { result ->
            result.either(
                fnL = { Timber.e(it, "Error while updating text: $params") },
                fnR = { Timber.d("Text has been updated") }
            )
        }
    }

    fun open(id: String) {
        pageId = id

        stateData.postValue(ViewState.Loading)

        openPage.invoke(viewModelScope, OpenPage.Params(id)) { result ->
            result.either(
                fnR = { Timber.d("Page has been opened") },
                fnL = { Timber.e(it, "Error while openining page with id: $id") }
            )
        }
    }

    fun onSystemBackPressed() {
        closePage.invoke(viewModelScope, ClosePage.Params(pageId)) { result ->
            result.either(
                fnR = { navigation.postValue(EventWrapper(AppNavigation.Command.Exit)) },
                fnL = { Timber.e(it, "Error while closing the test page") }
            )
        }
    }

    fun onTextChanged(id: String, text: String, marks: List<Block.Content.Text.Mark>) {
        Timber.d("onTextChanged: $id")
        Timber.d("With the following: $text")
        viewModelScope.launch { textChannel.send(Triple(id, text, marks)) }
    }

    fun onSelectionChanged(id: String, selection: IntRange) {
        viewModelScope.launch { selectionChannel.send(Pair(id, selection)) }
        controlPanelInteractor.onEvent(ControlPanelMachine.Event.OnSelectionChanged(selection))
    }

    fun onBlockFocusChanged(id: String, hasFocus: Boolean) {
        Timber.d("Focus changed ($hasFocus): $id")
        if (hasFocus) {
            viewModelScope.launch { focusChannel.send(id) }
            controlPanelInteractor.onEvent(ControlPanelMachine.Event.OnFocusChanged(id))
        }
    }

    fun onEmptyBlockBackspaceClicked(id: String) {
        Timber.d("onEmptyBlockBackspaceClicked: $id")
        blocks.find { it.id == id }?.let { target ->
            if (!target.content.asText().isTitle())
                proceedWithUnlinking(target = id)
        }
    }

    fun onSplitLineEnterClicked(id: String) {
        // TODO
    }

    fun onEndLineEnterClicked(id: String) {
        Timber.d("On endline enter clicked")
        createBlock.invoke(
            scope = viewModelScope,
            params = CreateBlock.Params(
                contextId = pageId,
                targetId = id,
                position = Position.BOTTOM,
                prototype = Prototype.Text(style = Block.Content.Text.Style.P)
            )
        ) { result ->
            result.either(
                fnL = { Timber.e(it, "Error while creating a block") },
                fnR = { Timber.d("Request to create a block has been dispatched") }
            )
        }
    }

    fun onMarkupActionClicked(markup: Markup.Type) {
        viewModelScope.launch {
            markupActionChannel.send(MarkupAction(type = markup))
        }
    }

    fun onMarkupTextColorAction(color: String) {
        controlPanelInteractor.onEvent(ControlPanelMachine.Event.OnTextColorSelected)
        viewModelScope.launch {
            markupActionChannel.send(
                MarkupAction(
                    type = Markup.Type.TEXT_COLOR,
                    param = color
                )
            )
        }
    }

    fun onMarkupToolbarColorClicked() {
        controlPanelInteractor.onEvent(ControlPanelMachine.Event.OnMarkupToolbarColorClicked)
    }

    fun onActionDeleteClicked() {
        viewModelScope.launch {
            focusChanges
                .take(1)
                .collect { focus -> proceedWithUnlinking(focus) }
        }
    }

    private fun proceedWithUnlinking(target: String) {
        unlinkBlocks.invoke(
            scope = viewModelScope,
            params = UnlinkBlocks.Params(
                context = pageId,
                targets = listOf(target)
            )
        ) { result ->
            result.either(
                fnL = { Timber.e(it, "Error while unlinking block with id: $target") },
                fnR = { Timber.d("Succesfully unlinked block with id: $target") }
            )
        }
    }

    fun onActionDuplicateClicked() {
        viewModelScope.launch {
            focusChanges
                .take(1)
                .collect { focus ->
                    duplicateBlock.invoke(
                        scope = this,
                        params = DuplicateBlock.Params(
                            context = pageId,
                            original = focus
                        )
                    ) { result ->
                        result.either(
                            fnL = { Timber.e(it, "Error while duplicating block with id: $focus") },
                            fnR = { Timber.d("Succesfully duplicated block with id: $focus") }
                        )
                    }
                }
        }
    }

    fun onAddTextBlockClicked(style: Block.Content.Text.Style) {
        controlPanelInteractor.onEvent(ControlPanelMachine.Event.OnOptionSelected)
        createBlock.invoke(
            viewModelScope, CreateBlock.Params(
                contextId = pageId,
                targetId = "",
                position = Position.INNER,
                prototype = Prototype.Text(style = style)
            )
        ) { result ->
            result.either(
                fnL = { Timber.e(it, "Error while creating a block") },
                fnR = { Timber.d("Request to create a block has been dispatched") }
            )
        }
    }

    fun onCheckboxClicked(id: String) {
        val target = blocks.first { it.id == id }

        val params = UpdateCheckbox.Params(
            context = pageId,
            target = target.id,
            isChecked = target.content.asText().toggleCheck()
        )

        updateCheckbox.invoke(viewModelScope, params) { result ->
            result.either(
                fnL = { Timber.e(it, "Error while updating checkbox: $id") },
                fnR = { Timber.d("Checkbox block has been succesfully updated") }
            )
        }
    }

    fun onAddBlockToolbarClicked() {
        controlPanelInteractor.onEvent(ControlPanelMachine.Event.OnAddBlockToolbarClicked)
    }

    fun onActionToolbarClicked() {
        controlPanelInteractor.onEvent(ControlPanelMachine.Event.OnActionToolbarClicked)
    }

    fun onOutsideClicked() {
        blocks.first { it.id == pageId }.let { page ->
            if (page.children.isNotEmpty()) {
                val lastBlock = blocks.first { it.id == page.children.last() }
                if (lastBlock.content is Block.Content.Text && lastBlock.content.asText().isTitle())
                    addNewBlockAtTheEnd()
            }
        }
    }

    fun onHideKeyboardClicked() {
        viewModelScope.launch {
            focusChannel.send(EMPTY_FOCUS_ID)
            renderingChannel.send(blocks)
        }
        controlPanelInteractor.onEvent(ControlPanelMachine.Event.OnClearFocusClicked)
    }

    private fun addNewBlockAtTheEnd() {
        createBlock.invoke(
            scope = viewModelScope,
            params = CreateBlock.Params(
                contextId = pageId,
                targetId = "",
                position = Position.INNER,
                prototype = Prototype.Text(style = Block.Content.Text.Style.P)
            )
        ) { result ->
            result.either(
                fnL = { Timber.e(it, "Error while creating a block") },
                fnR = { Timber.d("Request to create a block has been dispatched") }
            )
        }
    }

    sealed class ViewState {
        object Loading : ViewState()
        data class Success(val blocks: List<BlockView>) : ViewState()
        data class Error(val message: String) : ViewState()
    }

    companion object {
        const val EMPTY_FOCUS_ID = ""
        const val TEXT_CHANGES_DEBOUNCE_DURATION = 500L
    }

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
            private val channel: Channel<Event> = Channel()
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
             * Represents an event when user selected a add-block-option on [Toolbar.AddBlock] toolbar.
             */
            object OnAddBlockToolbarClicked : Event()

            /**
             * Represents an event when user selected any of the options on [Toolbar.AddBlock] toolbar.
             */
            object OnOptionSelected : Event()

            /**
             * Represents an event when user selected a text color on [Toolbar.Color] toolbar.
             */
            object OnTextColorSelected : Event()

            /**
             * Represents an event when user selected an action toolbar on [Toolbar.Block]
             */
            object OnActionToolbarClicked : Event()

            /**
             * Represents an event when user cleares the current focus by closing keyboard.
             */
            object OnClearFocusClicked : Event()

            /**
             * Represents an event when focus changes.
             * @property id id of the focused block
             */
            data class OnFocusChanged(
                val id: String
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
                        selectedAction = null
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
                is Event.OnTextColorSelected -> state.copy(
                    colorToolbar = state.colorToolbar.copy(
                        isVisible = false
                    ),
                    markupToolbar = state.markupToolbar.copy(
                        selectedAction = null
                    )
                )
                is Event.OnAddBlockToolbarClicked -> state.copy(
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
                    )
                )
                is Event.OnOptionSelected -> state.copy(
                    addBlockToolbar = state.addBlockToolbar.copy(
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
                is Event.OnFocusChanged -> {
                    if (state.isNotVisible())
                        state.copy(
                            blockToolbar = state.blockToolbar.copy(
                                isVisible = true
                            ),
                            focus = ControlPanelState.Focus(event.id)
                        )
                    else {
                        state.copy(
                            focus = ControlPanelState.Focus(event.id)
                        )
                    }
                }
            }
        }
    }

    data class MarkupAction(
        val type: Markup.Type,
        val param: Any? = null
    )
}

