package com.agileburo.anytype.presentation.page

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.agileburo.anytype.core_ui.common.Alignment
import com.agileburo.anytype.core_ui.common.Markup
import com.agileburo.anytype.core_ui.extensions.updateSelection
import com.agileburo.anytype.core_ui.features.page.*
import com.agileburo.anytype.core_ui.features.page.scrollandmove.ScrollAndMoveTargetDescriptor.Companion.END_RANGE
import com.agileburo.anytype.core_ui.features.page.scrollandmove.ScrollAndMoveTargetDescriptor.Companion.INNER_RANGE
import com.agileburo.anytype.core_ui.features.page.scrollandmove.ScrollAndMoveTargetDescriptor.Companion.START_RANGE
import com.agileburo.anytype.core_ui.features.page.styling.StylingEvent
import com.agileburo.anytype.core_ui.features.page.styling.StylingMode
import com.agileburo.anytype.core_ui.model.UiBlock
import com.agileburo.anytype.core_ui.state.ControlPanelState
import com.agileburo.anytype.core_ui.widgets.ActionItemType
import com.agileburo.anytype.core_ui.widgets.toolbar.adapter.Mention
import com.agileburo.anytype.core_utils.common.EventWrapper
import com.agileburo.anytype.core_utils.ext.*
import com.agileburo.anytype.core_utils.ui.ViewStateViewModel
import com.agileburo.anytype.domain.base.Result
import com.agileburo.anytype.domain.block.interactor.RemoveLinkMark
import com.agileburo.anytype.domain.block.interactor.UpdateLinkMarks
import com.agileburo.anytype.domain.block.model.Block
import com.agileburo.anytype.domain.block.model.Block.Content
import com.agileburo.anytype.domain.block.model.Block.Prototype
import com.agileburo.anytype.domain.block.model.Position
import com.agileburo.anytype.domain.common.Document
import com.agileburo.anytype.domain.common.Id
import com.agileburo.anytype.domain.editor.Editor
import com.agileburo.anytype.domain.error.Error
import com.agileburo.anytype.domain.event.interactor.InterceptEvents
import com.agileburo.anytype.domain.event.model.Event
import com.agileburo.anytype.domain.event.model.Payload
import com.agileburo.anytype.domain.ext.*
import com.agileburo.anytype.domain.misc.UrlBuilder
import com.agileburo.anytype.domain.page.*
import com.agileburo.anytype.domain.page.navigation.GetListPages
import com.agileburo.anytype.presentation.common.StateReducer
import com.agileburo.anytype.presentation.common.SupportCommand
import com.agileburo.anytype.presentation.mapper.mark
import com.agileburo.anytype.presentation.mapper.style
import com.agileburo.anytype.presentation.mapper.toMentionView
import com.agileburo.anytype.presentation.navigation.AppNavigation
import com.agileburo.anytype.presentation.navigation.SupportNavigation
import com.agileburo.anytype.presentation.page.ControlPanelMachine.Interactor
import com.agileburo.anytype.presentation.page.editor.*
import com.agileburo.anytype.presentation.page.model.TextUpdate
import com.agileburo.anytype.presentation.page.render.BlockViewRenderer
import com.agileburo.anytype.presentation.page.render.DefaultBlockViewRenderer
import com.agileburo.anytype.presentation.page.selection.SelectionStateHolder
import com.agileburo.anytype.presentation.page.toggle.ToggleStateHolder
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber

class PageViewModel(
    private val openPage: OpenPage,
    private val closePage: ClosePage,
    private val createPage: CreatePage,
    private val createDocument: CreateDocument,
    private val archiveDocument: ArchiveDocument,
    private val interceptEvents: InterceptEvents,
    private val updateLinkMarks: UpdateLinkMarks,
    private val removeLinkMark: RemoveLinkMark,
    private val reducer: StateReducer<List<Block>, Event>,
    private val urlBuilder: UrlBuilder,
    private val renderer: DefaultBlockViewRenderer,
    private val orchestrator: Orchestrator,
    private val getListPages: GetListPages
) : ViewStateViewModel<ViewState>(),
    SupportNavigation<EventWrapper<AppNavigation.Command>>,
    SupportCommand<Command>,
    BlockViewRenderer by renderer,
    ToggleStateHolder by renderer,
    SelectionStateHolder by orchestrator.memory.selections,
    TurnIntoActionReceiver,
    StateReducer<List<Block>, Event> by reducer {

    private val session = MutableStateFlow(Session.IDLE)

    private val views: List<BlockView> get() = orchestrator.stores.views.current()

    private var eventSubscription: Job? = null

    private var mode = EditorMode.EDITING

    private val controlPanelInteractor = Interactor(viewModelScope)
    val controlPanelViewState = MutableLiveData<ControlPanelState>()

    /**
     * Sends renderized document to UI
     */
    private val renderCommand = Proxy.Subject<Unit>()

    /**
     * Renderizes document, create views from it, dispatches them to [renderCommand]
     */
    private val renderizePipeline = Proxy.Subject<Document>()

    private val markupActionPipeline = Proxy.Subject<MarkupAction>()

    private val titleChannel = Channel<String>()
    private val titleChanges = titleChannel.consumeAsFlow()

    /**
     * Currently opened document id.
     */
    var context: String = EMPTY_CONTEXT

    /**
     * Current document
     */
    var blocks: Document = emptyList()

    private val _focus: MutableLiveData<Id> = MutableLiveData()
    val focus: LiveData<Id> = _focus

    private val _error: MutableLiveData<ErrorViewState> = MutableLiveData()
    val error: LiveData<ErrorViewState> = _error

    /**
     * Open gallery and search media files for block with that id
     */
    private var mediaBlockId = ""

    /**
     * Current position of last mentionFilter or -1 if none
     */
    private var mentionFrom = -1

    override val navigation = MutableLiveData<EventWrapper<AppNavigation.Command>>()
    override val commands = MutableLiveData<EventWrapper<Command>>()

    init {
        startHandlingTextChanges()
        startProcessingFocusChanges()
        startProcessingTitleChanges()
        startProcessingControlPanelViewState()
        startObservingPayload()
        startObservingErrors()
        processRendering()
        processMarkupChanges()
        viewModelScope.launch { orchestrator.start() }
    }

    private fun startProcessingFocusChanges() {
        viewModelScope.launch {
            orchestrator.stores.focus.stream().collect {
                if (it.isEmpty) {
                    orchestrator.stores.textSelection.update(Editor.TextSelection.empty())
                }
                _focus.postValue(it.id)
            }
        }
    }

    private fun startProcessingTitleChanges() {
        titleChanges
            .debounce(TEXT_CHANGES_DEBOUNCE_DURATION)
            .onEach { update -> proceedWithUpdatingDocumentTitle(update) }
            .launchIn(viewModelScope)
    }

    private fun proceedWithUpdatingDocumentTitle(update: String) {

        viewModelScope.launch {
            orchestrator.proxies.intents.send(
                Intent.Document.UpdateTitle(
                    context = context,
                    title = update
                )
            )
        }
    }

    private fun startObservingPayload() {
        viewModelScope.launch {
            orchestrator
                .proxies
                .payloads
                .stream()
                .map { payload -> processEvents(payload.events) }
                .collect { viewModelScope.launch { refresh() } }
        }
    }

    private fun startObservingErrors() {
        viewModelScope.launch {
            orchestrator.proxies.errors
                .stream()
                .collect {
                    _error.value = ErrorViewState.Toast(it.message ?: "Unknown error")
                }
        }
    }

    private suspend fun processEvents(events: List<Event>) {
        Timber.d("Blocks before handling events: $blocks")
        Timber.d("Events: $events")
        events.forEach { event ->
            if (event is Event.Command.ShowBlock) {
                orchestrator.stores.details.update(event.details)
            }
            if (event is Event.Command.UpdateDetails) {
                orchestrator.stores.details.add(event.target, event.details)
            }
            blocks = reduce(blocks, event)
        }
        Timber.d("Blocks after handling events: $blocks")
    }

    private fun startProcessingControlPanelViewState() {
        viewModelScope.launch {
            controlPanelInteractor
                .state()
                .distinctUntilChanged()
                .collect { controlPanelViewState.postValue(it) }
        }
    }

    private fun processMarkupChanges() {
        markupActionPipeline
            .stream()
            .withLatestFrom(
                orchestrator.stores.textSelection
                    .stream()
                    .distinctUntilChanged()
            )
            { a, b -> Pair(a, b) }
            .onEach { (action, textSelection) ->
                val range = textSelection.selection
                if (textSelection.isNotEmpty && range != null && range.first != range.last) {
                    if (action.type == Markup.Type.LINK) {
                        val block = blocks.first { it.id == textSelection.id }
                        stateData.value = ViewState.OpenLinkScreen(
                            pageId = context,
                            block = block,
                            range = IntRange(
                                start = range.first,
                                endInclusive = range.last.dec()
                            )
                        )
                    } else {
                        applyMarkup(
                            selection = Pair(textSelection.id, range),
                            action = action
                        )
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun applyLinkMarkup(
        blockId: String, link: String, range: IntRange
    ) {
        val targetBlock = blocks.first { it.id == blockId }
        val targetContent = targetBlock.content as Content.Text
        val linkMark = Content.Text.Mark(
            type = Content.Text.Mark.Type.LINK,
            range = IntRange(start = range.first, endInclusive = range.last.inc()),
            param = link
        )
        val marks = targetContent.marks

        updateLinkMarks(
            scope = viewModelScope,
            params = UpdateLinkMarks.Params(
                marks = marks,
                newMark = linkMark
            ),
            onResult = { result ->
                result.either(
                    fnL = { throwable ->
                        Timber.e("Error update marks:${throwable.message}")
                    },
                    fnR = {
                        val newContent = targetContent.copy(marks = it)
                        val newBlock = targetBlock.copy(content = newContent)
                        rerenderingBlocks(newBlock)
                        proceedWithUpdatingText(
                            intent = Intent.Text.UpdateText(
                                context = context,
                                text = newBlock.content.asText().text,
                                target = targetBlock.id,
                                marks = it
                            )
                        )
                    }
                )
            }
        )
    }

    private suspend fun applyMarkup(
        selection: Pair<String, IntRange>,
        action: MarkupAction
    ) {
        val target = blocks.first { block -> block.id == selection.first }

        val new = target.markup(
            type = action.type,
            param = action.param,
            range = selection.second
        )

        blocks = blocks.map { block ->
            if (block.id != target.id)
                block
            else
                new
        }

        refresh()

        proceedWithUpdatingText(
            intent = Intent.Text.UpdateText(
                context = context,
                target = new.id,
                text = new.content<Content.Text>().text,
                marks = new.content<Content.Text>().marks
            )
        )
    }

    private fun rerenderingBlocks(block: Block) =
        viewModelScope.launch {
            blocks = blocks.map {
                if (it.id != block.id)
                    it
                else
                    block
            }
            refresh()
        }

    private fun processRendering() {

        // stream to UI

        renderCommand
            .stream()
            .switchToLatestFrom(orchestrator.stores.views.stream())
            .onEach { dispatchToUI(it) }
            .launchIn(viewModelScope)

        // renderize, in order to send to UI

        renderizePipeline
            .stream()
            .filter { it.isNotEmpty() }
            .onEach(this::refreshStyleToolbar)
            .withLatestFrom(
                orchestrator.stores.focus.stream(),
                orchestrator.stores.details.stream()
            ) { models, focus, details ->
                models.asMap().render(
                    mode = mode,
                    indent = INITIAL_INDENT,
                    anchor = context,
                    focus = focus,
                    root = models.first { it.id == context },
                    details = details
                )
            }
            .onEach { views ->
                orchestrator.stores.views.update(views)
                renderCommand.send(Unit)
            }
            .launchIn(viewModelScope)
    }

    private fun refreshStyleToolbar(document: Document) {
        controlPanelViewState.value?.let { state ->
            if (state.stylingToolbar.isVisible) {
                state.stylingToolbar.target?.id?.let { targetId ->
                    controlPanelInteractor.onEvent(
                        event = ControlPanelMachine.Event.OnRefresh.StyleToolbar(
                            target = document.find { it.id == targetId },
                            selection = orchestrator.stores.textSelection.current().selection
                        )
                    )
                }
            }
        }
    }

    private fun dispatchToUI(views: List<BlockView>) {
        stateData.postValue(
            ViewState.Success(
                blocks = views
            )
        )
    }

    private fun startHandlingTextChanges() {
        orchestrator
            .proxies
            .changes
            .stream()
            .filterNotNull()
            .onEach { update ->
                orchestrator.textInteractor.consume(update, context)
            }
            .launchIn(viewModelScope)

        orchestrator
            .proxies
            .saves
            .stream()
            .debounce(TEXT_CHANGES_DEBOUNCE_DURATION)
            .filterNotNull()
            .onEach { update ->
                blocks = blocks.map { block ->
                    if (block.id == update.target) {
                        block.updateText(update)
                    } else
                        block
                }
            }
            .map { update ->
                Intent.Text.UpdateText(
                    context = context,
                    target = update.target,
                    text = update.text,
                    marks = update.markup.filter { it.range.first != it.range.last }
                )
            }
            .onEach { params -> proceedWithUpdatingText(params) }
            .launchIn(viewModelScope)
    }

    private fun proceedWithUpdatingText(intent: Intent.Text.UpdateText) {
        viewModelScope.launch {
            orchestrator.proxies.intents.send(intent)
        }
    }

    fun onStart(id: Id) {
        Timber.d("onStart")

        context = id

        stateData.postValue(ViewState.Loading)

        eventSubscription = viewModelScope.launch {
            interceptEvents
                .build(InterceptEvents.Params(context))
                .map { events -> processEvents(events) }
                .collect { refresh() }
        }

        viewModelScope.launch {
            openPage(OpenPage.Params(id)).proceed(
                success = { result ->
                    when (result) {
                        is Result.Success -> {
                            session.value = Session.OPEN
                            onStartFocusing(result.data)
                            orchestrator.proxies.payloads.send(result.data)
                        }
                        is Result.Failure -> {
                            session.value = Session.ERROR
                            if (result.error is Error.BackwardCompatibility)
                                _error.value = ErrorViewState.AlertDialog
                        }
                    }
                },
                failure = {
                    session.value = Session.ERROR
                    Timber.e(it, "Error while opening page with id: $id")
                }
            )
        }
    }

    private fun onStartFocusing(payload: Payload) {
        payload.events.find { it is Event.Command.ShowBlock }?.let { event ->
            (event as Event.Command.ShowBlock).blocks.first { it.id == context }
                .let { page ->
                    if (page.children.isEmpty()) {
                        updateFocus(page.id)
                        controlPanelInteractor.onEvent(
                            ControlPanelMachine.Event.OnFocusChanged(
                                id = page.id, style = Content.Text.Style.TITLE
                            )
                        )
                    }
                }
        }
    }

    fun onAddLinkPressed(blockId: String, link: String, range: IntRange) {
        applyLinkMarkup(blockId, link, range)
    }

    fun onUnlinkPressed(blockId: String, range: IntRange) {

        val target = blocks.first { it.id == blockId }
        val content = target.content<Content.Text>()
        val marks = content.marks

        viewModelScope.launch {
            removeLinkMark(
                params = RemoveLinkMark.Params(
                    range = range,
                    marks = marks
                )
            ).proceed(
                failure = { Timber.e("Error update marks:${it.message}") },
                success = {
                    val newContent = content.copy(marks = it)
                    val newBlock = target.copy(content = newContent)
                    rerenderingBlocks(newBlock)
                    proceedWithUpdatingText(
                        intent = Intent.Text.UpdateText(
                            context = context,
                            text = newBlock.content.asText().text,
                            target = target.id,
                            marks = it
                        )
                    )
                }
            )
        }
    }

    fun onSystemBackPressed(editorHasChildrenScreens: Boolean) {
        if (editorHasChildrenScreens) {
            dispatch(Command.PopBackStack)
        } else {
            proceedWithExiting()
        }
    }

    fun onDismissBlockActionMenu(editorHasChildrenScreens: Boolean) {
        onExitActionMode()
        onSystemBackPressed(editorHasChildrenScreens)
    }

    fun onBackButtonPressed() {
        proceedWithExiting()
    }

    fun onBottomSheetHidden() {
        proceedWithExitingToDesktop()
    }

    private fun proceedWithExiting() {
        when (session.value) {
            Session.ERROR -> navigate(EventWrapper(AppNavigation.Command.Exit))
            Session.IDLE -> navigate(EventWrapper(AppNavigation.Command.Exit))
            Session.OPEN -> {
                viewModelScope.launch {
                    closePage(
                        ClosePage.Params(context)
                    ).proceed(
                        success = { navigation.postValue(EventWrapper(AppNavigation.Command.Exit)) },
                        failure = { Timber.e(it, "Error while closing document: $context") }
                    )
                }
            }
        }
    }

    private fun proceedWithExitingToDesktop() {
        closePage(viewModelScope, ClosePage.Params(context)) { result ->
            result.either(
                fnR = { navigateToDesktop() },
                fnL = { Timber.e(it, "Error while closing the test page") }
            )
        }
    }

    fun navigateToDesktop() {
        navigation.postValue(EventWrapper(AppNavigation.Command.ExitToDesktop))
    }

    @Deprecated("replace by onTextBlockTextChanged")
    fun onTextChanged(
        id: String,
        text: String,
        marks: List<Content.Text.Mark>
    ) {
        val update = TextUpdate.Default(target = id, text = text, markup = marks)
        viewModelScope.launch { orchestrator.proxies.changes.send(update) }
    }

    fun onTitleTextChanged(text: String) {
        viewModelScope.launch { titleChannel.send(text) }
    }

    fun onTextBlockTextChanged(
        view: BlockView.Text
    ) {

        Timber.d("Text block's text changed: $view")

        val update = if (view is BlockView.Text.Paragraph) TextUpdate.Pattern(
            target = view.id,
            text = view.text,
            markup = view.marks.map { it.mark() }
        ) else TextUpdate.Default(
            target = view.id,
            text = view.text,
            markup = view.marks.map { it.mark() }
        )

        val store = orchestrator.stores.views
        val old = store.current()
        val new = old.map { if (it.id == view.id) view else it }

        viewModelScope.launch { store.update(new) }
        viewModelScope.launch { orchestrator.proxies.changes.send(update) }
    }

    fun onSelectionChanged(id: String, selection: IntRange) {
        viewModelScope.launch {
            orchestrator.stores.textSelection.update(Editor.TextSelection(id, selection))
        }
        controlPanelInteractor.onEvent(ControlPanelMachine.Event.OnSelectionChanged(id, selection))
    }

    fun onBlockFocusChanged(id: String, hasFocus: Boolean) {
        Timber.d("Focus changed ($hasFocus): $id")
        if (hasFocus) {
            updateFocus(id)
            controlPanelInteractor.onEvent(
                ControlPanelMachine.Event.OnFocusChanged(
                    id = id,
                    style = if (id == context)
                        Content.Text.Style.TITLE
                    else
                        blocks.first { it.id == id }.textStyle()
                )
            )
        }
    }

    private fun proceedWithMergingBlocks(
        target: String,
        previous: String
    ) {
        viewModelScope.launch {
            orchestrator.proxies.intents.send(
                Intent.Text.Merge(
                    context = context,
                    previous = previous,
                    pair = Pair(previous, target),
                    previousLength = blocks.find { it.id == previous }?.let { block ->
                        if (block.content is Content.Text) {
                            block.content.asText().text.length
                        } else {
                            null
                        }
                    }
                )
            )
        }
    }

    fun onEnterKeyClicked(
        target: String,
        text: String,
        marks: List<Content.Text.Mark>,
        range: IntRange
    ) {
        val focus = orchestrator.stores.focus.current()
        if (!focus.isEmpty && focus.id == target) {
            proceedWithEnterEvent(focus.id, range, text, marks)
        } else {
            Timber.e("No blocks in focus, emit SplitLineEnter event")
        }
    }

    private fun proceedWithEnterEvent(target: Id,
                                      range: IntRange,
                                      text: String,
                                      marks: List<Content.Text.Mark>) {
        if (context == target) {
            onEndLineEnterTitleClicked()
        } else {
            if (text.isEndLineClick(range)) {
                onEndLineEnterClicked(target, text, marks)
            } else {
                proceedWithSplitEvent(target, range, text, marks)
            }
        }
    }

    private fun proceedWithSplitEvent(
        target: Id,
        range: IntRange,
        text: String,
        marks: List<Content.Text.Mark>
    ) {

        val block = blocks.first { it.id == target }
        val content = block.content<Content.Text>()

        blocks = blocks.updateTextContent(target, text, marks)

        viewModelScope.launch {
            orchestrator.proxies.saves.send(null)
            orchestrator.proxies.changes.send(null)
        }

        viewModelScope.launch {
            orchestrator.proxies.intents.send(
                Intent.Text.UpdateText(
                    context = context,
                    target = target,
                    marks = marks,
                    text = text
                )
            )
        }

        viewModelScope.launch {
            orchestrator.proxies.intents.send(
                Intent.Text.Split(
                    context = context,
                    block = block,
                    range = range,
                    isToggled = if (content.isToggle()) renderer.isToggled(target) else null
                )
            )
        }
    }

    private fun onEndLineEnterTitleClicked() {
        val page = blocks.first { it.id == context }
        val next = page.children.getOrElse(0) { "" }
        proceedWithCreatingNewTextBlock(
            id = next,
            style = Content.Text.Style.P,
            position = Position.TOP
        )
    }

    fun onEndLineEnterClicked(
        id: String,
        text: String,
        marks: List<Content.Text.Mark>
    ) {
        Timber.d("On endline enter clicked")

        val target = blocks.first { it.id == id }

        val content = target.content<Content.Text>().copy(
            text = text,
            marks = marks
        )

        blocks = blocks.replace(
            replacement = { old -> old.copy(content = content) }
        ) { block -> block.id == id }

        if (content.isList()) {
            handleEndlineEnterPressedEventForListItem(content, id)
        } else {
            proceedWithCreatingNewTextBlock(
                id = id,
                style = Content.Text.Style.P
            )
        }
    }

    fun onEmptyBlockBackspaceClicked(id: String) {
        Timber.d("onEmptyBlockBackspaceClicked: $id")
        proceedWithUnlinking(target = id)
    }

    fun onNonEmptyBlockBackspaceClicked(
        id: String,
        text: String,
        marks: List<Content.Text.Mark>
    ) {
        blocks = blocks.map { block ->
            if (block.id == id) {
                block.copy(
                    content = block.content<Content.Text>().copy(
                        text = text,
                        marks = marks
                    )
                )
            } else {
                block
            }
        }

        viewModelScope.launch {
            orchestrator.proxies.saves.send(null)
            orchestrator.proxies.changes.send(null)
        }

        viewModelScope.launch {
            orchestrator.proxies.intents.send(
                Intent.Text.UpdateText(
                    context = context,
                    target = id,
                    marks = marks,
                    text = text
                )
            )
        }

        // TODO should take into account that previous block could be a Block.Content.Layout!

        val page = blocks.first { it.id == context }

        val index = page.children.indexOf(id)

        if (index > 0) {
            val previous = page.children[index.dec()]
            proceedWithMergingBlocks(
                previous = previous,
                target = id
            )
        } else {
            Timber.d("Skipping merge on non-empty-block-backspace-pressed event")
        }
    }

    private fun handleEndlineEnterPressedEventForListItem(
        content: Content.Text,
        id: String
    ) {
        if (content.text.isNotEmpty()) {
            proceedWithCreatingNewTextBlock(id, content.style)
        } else {
            proceedWithUpdatingTextStyle(
                style = Content.Text.Style.P,
                targets = listOf(id)
            )
        }
    }

    private fun proceedWithCreatingNewTextBlock(
        id: String,
        style: Content.Text.Style,
        position: Position = Position.BOTTOM
    ) {
        viewModelScope.launch {
            orchestrator.proxies.intents.send(
                Intent.CRUD.Create(
                    context = context,
                    target = id,
                    position = position,
                    prototype = Prototype.Text(style = style)
                )
            )
        }
    }

    private fun updateFocus(id: Id) {
        Timber.d("Updating focus: $id")
        viewModelScope.launch { orchestrator.stores.focus.update(Editor.Focus.id(id)) }
    }

    private fun onBlockLongPressedClicked(target: String, dimensions: BlockDimensions) {
        val views = orchestrator.stores.views.current()
        onEnterActionMode()
        dispatch(
            Command.OpenActionBar(
                block = views.first { it.id == target },
                dimensions = dimensions
            )
        )
    }

    fun onEditorContextMenuStyleClicked(selection: IntRange) {
        val target = blocks.first { it.id == orchestrator.stores.focus.current().id }
        controlPanelInteractor.onEvent(
            ControlPanelMachine.Event.OnEditorContextMenuStyleClicked(
                target = target,
                selection = selection
            )
        )
    }

    fun onStylingToolbarEvent(event: StylingEvent) {
        val state = controlPanelViewState.value!!
        when (event) {
            is StylingEvent.Coloring.Text -> {
                proceedWithStylingEvent(state, Markup.Type.TEXT_COLOR, event.color.title)
            }
            is StylingEvent.Coloring.Background -> {
                proceedWithStylingEvent(state, Markup.Type.BACKGROUND_COLOR, event.color.title)
            }
            is StylingEvent.Markup.Bold -> {
                proceedWithStylingEvent(state, Markup.Type.BOLD, null)
            }
            is StylingEvent.Markup.Italic -> {
                proceedWithStylingEvent(state, Markup.Type.ITALIC, null)
            }
            is StylingEvent.Markup.StrikeThrough -> {
                proceedWithStylingEvent(state, Markup.Type.STRIKETHROUGH, null)
            }
            is StylingEvent.Markup.Code -> {
                proceedWithStylingEvent(state, Markup.Type.KEYBOARD, null)
            }
            is StylingEvent.Markup.Link -> {
                proceedWithStylingEvent(state, Markup.Type.LINK, null)
            }
            is StylingEvent.Alignment.Left -> {
                onBlockAlignmentActionClicked(Alignment.START)
            }
            is StylingEvent.Alignment.Center -> {
                onBlockAlignmentActionClicked(Alignment.CENTER)
            }
            is StylingEvent.Alignment.Right -> {
                onBlockAlignmentActionClicked(Alignment.END)
            }
        }
    }

    private fun proceedWithStylingEvent(
        state: ControlPanelState,
        type: Markup.Type,
        param: String?
    ) {
        if (state.stylingToolbar.mode == StylingMode.MARKUP) {
            onStyleToolbarMarkupAction(type, param)
        } else {
            state.stylingToolbar.target?.id?.let { id ->
                when (type) {
                    Markup.Type.ITALIC -> onBlockStyleMarkupActionClicked(id, type)
                    Markup.Type.BOLD -> onBlockStyleMarkupActionClicked(id, type)
                    Markup.Type.STRIKETHROUGH -> onBlockStyleMarkupActionClicked(id, type)
                    Markup.Type.TEXT_COLOR -> onToolbarTextColorAction(id, param)
                    Markup.Type.BACKGROUND_COLOR -> onBlockBackgroundColorAction(id, param)
                    Markup.Type.LINK -> onBlockStyleLinkClicked(id)
                    Markup.Type.KEYBOARD -> onBlockStyleMarkupActionClicked(id, type)
                    Markup.Type.MENTION -> Unit
                }
            } ?: run { throw IllegalStateException("Target id should be non null") }
        }
    }

    private fun onStyleToolbarMarkupAction(type: Markup.Type, param: String? = null) {
        viewModelScope.launch {
            markupActionPipeline.send(
                MarkupAction(
                    type = type,
                    param = param
                )
            )
        }
    }

    private fun onBlockAlignmentActionClicked(alignment: Alignment) {
        controlPanelViewState.value?.stylingToolbar?.target?.id?.let { id ->
            viewModelScope.launch {
                orchestrator.proxies.intents.send(
                    Intent.Text.Align(
                        context = context,
                        target = id,
                        alignment = when (alignment) {
                            Alignment.START -> Block.Align.AlignLeft
                            Alignment.CENTER -> Block.Align.AlignCenter
                            Alignment.END -> Block.Align.AlignRight
                        }
                    )
                )
            }
        }
    }

    fun onCloseBlockStyleToolbarClicked() {
        val focused = !orchestrator.stores.focus.current().isEmpty
        controlPanelInteractor.onEvent(ControlPanelMachine.Event.StylingToolbar.OnClose(focused))
    }

    fun onToolbarTextColorAction(id: String, color: String?) {
        check(color != null)
        controlPanelInteractor.onEvent(ControlPanelMachine.Event.OnBlockTextColorSelected)
        viewModelScope.launch {
            orchestrator.proxies.intents.send(
                Intent.Text.UpdateColor(
                    context = context,
                    target = id,
                    color = color
                )
            )
        }
    }

    private fun onBlockBackgroundColorAction(id: String, color: String?) {
        check(color != null)
        controlPanelInteractor.onEvent(ControlPanelMachine.Event.OnBlockBackgroundColorSelected)
        viewModelScope.launch {
            orchestrator.proxies.intents.send(
                Intent.Text.UpdateBackgroundColor(
                    context = context,
                    targets = listOf(id),
                    color = color
                )
            )
        }
    }

    private fun onBlockStyleLinkClicked(id: String) {
        val target = blocks.first { it.id == id }
        val range = IntRange(
            start = 0,
            endInclusive = target.content<Content.Text>().text.length.dec()
        )
        stateData.value = ViewState.OpenLinkScreen(context, target, range)
    }

    private fun onBlockStyleMarkupActionClicked(id: String, action: Markup.Type) {

        controlPanelInteractor.onEvent(
            ControlPanelMachine.Event.OnBlockStyleSelected
        )

        val target = blocks.first { it.id == id }
        val content = target.content as Content.Text

        if (content.text.isNotEmpty()) {

            val new = target.markup(
                type = action,
                range = 0..content.text.length,
                param = null
            )

            blocks = blocks.map { block ->
                if (block.id != target.id)
                    block
                else
                    new
            }

            viewModelScope.launch { refresh() }

            viewModelScope.launch {
                proceedWithUpdatingText(
                    intent = Intent.Text.UpdateText(
                        context = context,
                        target = new.id,
                        text = new.content<Content.Text>().text,
                        marks = new.content<Content.Text>().marks
                    )
                )
            }
        }
    }

    fun onActionMenuItemClicked(id: String, action: ActionItemType) {
        when (action) {
            ActionItemType.AddBelow -> {
                onExitActionMode()
                dispatch(Command.PopBackStack)
                proceedWithCreatingNewTextBlock(
                    id = id,
                    style = Content.Text.Style.P
                )
            }
            ActionItemType.TurnInto -> {
                val excludedTypes = mutableListOf<String>()
                val target = blocks.first { it.id == id }
                if (target.content is Content.Text) {
                    excludedTypes.apply {
                        add(UiBlock.FILE.name)
                        add(UiBlock.IMAGE.name)
                        add(UiBlock.VIDEO.name)
                        add(UiBlock.BOOKMARK.name)
                        add(UiBlock.LINE_DIVIDER.name)
                        add(UiBlock.THREE_DOTS.name)
                        add(UiBlock.LINK_TO_OBJECT.name)
                    }
                }
                onExitActionMode()
                dispatch(
                    Command.OpenTurnIntoPanel(
                        target = id,
                        excludedCategories = emptyList(),
                        excludedTypes = excludedTypes
                    )
                )
            }
            ActionItemType.Delete -> {
                proceedWithUnlinking(target = id)
                onExitActionMode()
                dispatch(Command.PopBackStack)
            }
            ActionItemType.Duplicate -> {
                duplicateBlock(target = id)
                onExitActionMode()
                dispatch(Command.PopBackStack)
            }
            ActionItemType.Rename -> {
                _error.value = ErrorViewState.Toast("Rename not implemented")
            }
            ActionItemType.MoveTo -> {
                onExitActionMode()
                dispatch(Command.PopBackStack)
                navigate(
                    EventWrapper(
                        AppNavigation.Command.OpenMoveToScreen(
                            context = context,
                            targets = listOf(id)
                        )
                    )
                )
            }
            ActionItemType.Style -> {
                val textSelection = orchestrator.stores.textSelection.current()
                controlPanelInteractor.onEvent(
                    ControlPanelMachine.Event.OnBlockActionToolbarStyleClicked(
                        target = blocks.first { it.id == id },
                        focused = textSelection.isNotEmpty,
                        selection = textSelection.selection
                    )
                )
                onExitActionMode()
                dispatch(Command.PopBackStack)
            }
            ActionItemType.Download -> {
                viewModelScope.launch {
                    onExitActionMode()
                    dispatch(Command.PopBackStack)
                    delay(300)
                    dispatch(Command.RequestDownloadPermission(id))
                }
            }
            ActionItemType.Replace -> {
                _error.value = ErrorViewState.Toast("Replace not implemented")
            }
            ActionItemType.AddCaption -> {
                _error.value = ErrorViewState.Toast("Add caption not implemented")
            }
            ActionItemType.Divider -> {
                _error.value = ErrorViewState.Toast("not implemented")
            }
        }
    }

    private fun proceedWithUnlinking(target: String) {

        val position = views.indexOfFirst { it.id == target }

        var previous: Id? = null
        var cursor: Int? = null

        if (position <= 0) return

        for (i in position.dec() downTo 0) {
            when (val view = views[i]) {
                is BlockView.Text -> {
                    previous = view.id
                    cursor = view.text.length
                    break
                }
                is BlockView.Code -> {
                    previous = view.id
                    cursor = view.text.length
                    break
                }
                is BlockView.Title -> {
                    previous = view.id
                    cursor = view.text?.length ?: 0
                    break
                }
            }
        }

        viewModelScope.launch {
            orchestrator.proxies.intents.send(
                Intent.CRUD.Unlink(
                    context = context,
                    targets = listOf(target),
                    previous = previous,
                    next = null,
                    cursor = cursor
                )
            )
        }
    }

    private fun duplicateBlock(target: String) {
        viewModelScope.launch {
            orchestrator.proxies.intents.send(
                Intent.CRUD.Duplicate(
                    context = context,
                    target = target
                )
            )
        }
    }

    fun onActionUndoClicked() {
        viewModelScope.launch {
            orchestrator.proxies.intents.send(
                Intent.Document.Undo(
                    context = context
                )
            )
        }
    }

    fun onActionRedoClicked() {
        viewModelScope.launch {
            orchestrator.proxies.intents.send(
                Intent.Document.Redo(
                    context = context
                )
            )
        }
    }

    fun onAddTextBlockClicked(style: Content.Text.Style) {

        val target = blocks.first { it.id == orchestrator.stores.focus.current().id }

        val content = target.content

        if (content is Content.Text && content.text.isEmpty()) {
            viewModelScope.launch {
                orchestrator.proxies.intents.send(
                    Intent.CRUD.Replace(
                        context = context,
                        target = target.id,
                        prototype = Prototype.Text(style = style)
                    )
                )
            }
        } else {

            var id = target.id

            val position: Position

            if (target.id == context) {
                if (target.children.isEmpty())
                    position = Position.INNER
                else {
                    position = Position.TOP
                    id = target.children.first()
                }
            } else {
                position = Position.BOTTOM
            }

            proceedWithCreatingNewTextBlock(
                id = id,
                style = style,
                position = position
            )
        }

        controlPanelInteractor.onEvent(ControlPanelMachine.Event.OnAddBlockToolbarOptionSelected)
    }

    private fun onAddLocalVideoClicked(blockId: String) {
        mediaBlockId = blockId
        dispatch(Command.OpenGallery(mediaType = MIME_VIDEO_ALL))
    }

    private fun onAddLocalPictureClicked(blockId: String) {
        mediaBlockId = blockId
        dispatch(Command.OpenGallery(mediaType = MIME_IMAGE_ALL))
    }

    fun onAddLinkToObjectClicked() {

        val focused = blocks.first { it.id == orchestrator.stores.focus.current().id }

        val content = focused.content

        val replace = content is Content.Text && content.text.isEmpty()

        var position : Position = Position.BOTTOM

        var target: Id = focused.id

        if (!replace && focused.id == context) {
            if (focused.children.isEmpty()) {
                position = Position.INNER
            } else {
                position = Position.TOP
                target = focused.children.first()
            }
        }

        proceedWithClearingFocus()

        navigate(
            EventWrapper(
                AppNavigation.Command.OpenLinkToScreen(
                    target = target,
                    context = context,
                    replace = replace,
                    position = position
                )
            )
        )
    }

    fun onTogglePlaceholderClicked(target: Id) {
        viewModelScope.launch {
            orchestrator.proxies.intents.send(
                Intent.CRUD.Create(
                    context = context,
                    target = target,
                    prototype = Prototype.Text(
                        style = Content.Text.Style.P
                    ),
                    position = Position.INNER
                )
            )
        }
    }

    fun onToggleClicked(target: Id) {
        onToggleChanged(target)
        viewModelScope.launch { refresh() }
    }

    private fun onAddLocalFileClicked(blockId: String) {
        mediaBlockId = blockId
        dispatch(Command.OpenGallery(mediaType = MIME_FILE_ALL))
    }

    fun onAddFileBlockClicked(type: Content.File.Type) {

        val focused = blocks.first { it.id == orchestrator.stores.focus.current().id }

        val content = focused.content

        if (content is Content.Text && content.text.isEmpty()) {
            proceedWithReplacingByEmptyFileBlock(
                id = focused.id,
                type = type
            )
        } else {

            val position : Position

            var target: Id = focused.id

            if (focused.id == context) {
                if (focused.children.isEmpty()) {
                    position = Position.INNER
                } else {
                    position = Position.TOP
                    target = focused.children.first()
                }
            } else {
                position = Position.BOTTOM
            }

            proceedWithCreatingEmptyFileBlock(
                id = target,
                type = type,
                position = position
            )
        }
    }

    private fun proceedWithCreatingEmptyFileBlock(
        id: String,
        type: Content.File.Type,
        state: Content.File.State = Content.File.State.EMPTY,
        position: Position = Position.BOTTOM
    ) {
        viewModelScope.launch {
            orchestrator.proxies.intents.send(
                Intent.CRUD.Create(
                    context = context,
                    target = id,
                    position = position,
                    prototype = Prototype.File(type = type, state = state)
                )
            )
        }
    }

    private fun proceedWithReplacingByEmptyFileBlock(
        id: String,
        type: Content.File.Type,
        state: Content.File.State = Content.File.State.EMPTY
    ) {
        viewModelScope.launch {
            orchestrator.proxies.intents.send(
                Intent.CRUD.Replace(
                    context = context,
                    target = id,
                    prototype = Prototype.File(type = type, state = state)
                )
            )
        }
    }

    fun onCheckboxClicked(view: BlockView.Text.Checkbox) {

        blocks = blocks.map { block ->
            if (block.id == view.id) {
                block.copy(
                    content = block.content<Content.Text>().copy(
                        isChecked = view.isChecked
                    )
                )
            } else {
                block
            }
        }

        val store = orchestrator.stores.views

        viewModelScope.launch {
            store.update(
                views.map { v ->
                    if (v.id == view.id)
                        view.copy()
                    else
                        v
                }
            )
        }

        viewModelScope.launch {
            orchestrator.proxies.intents.send(
                Intent.Text.UpdateCheckbox(
                    context = context,
                    target = view.id,
                    isChecked = view.isChecked
                )
            )
        }
    }

    fun onBlockToolbarStyleClicked() {
        if (orchestrator.stores.focus.current().id == context) {
            _error.value = ErrorViewState.Toast("Changing style for title currently not supported")
        } else {
            val textSelection = orchestrator.stores.textSelection.current()
            controlPanelInteractor.onEvent(
                ControlPanelMachine.Event.OnBlockActionToolbarStyleClicked(
                    target = blocks.first { it.id == orchestrator.stores.focus.current().id },
                    focused = textSelection.isNotEmpty,
                    selection = textSelection.selection
                )
            )
        }
    }

    fun onBlockToolbarBlockActionsClicked() {
        if (orchestrator.stores.focus.current().id == context) {
            _error.value = ErrorViewState.Toast("Not implemented for title")
        } else {
            dispatch(
                Command.Measure(
                    target = orchestrator.stores.focus.current().id
                )
            )
        }
    }

    fun onMeasure(target: Id, dimensions: BlockDimensions) {
        proceedWithClearingFocus()
        onBlockLongPressedClicked(target, dimensions)
    }

    fun onAddBlockToolbarClicked() {
        dispatch(Command.OpenAddBlockPanel)
    }

    fun onEnterMultiSelectModeClicked() {
        controlPanelInteractor.onEvent(ControlPanelMachine.Event.MultiSelect.OnEnter)
        mode = EditorMode.MULTI_SELECT
        viewModelScope.launch {
            delay(DELAY_REFRESH_DOCUMENT_TO_ENTER_MULTI_SELECT_MODE)
            refresh()
        }
    }

    fun onExitMultiSelectModeClicked() {
        controlPanelInteractor.onEvent(ControlPanelMachine.Event.MultiSelect.OnExit)
        mode = EditorMode.EDITING
        clearSelections()
        viewModelScope.launch {
            delay(300)
            orchestrator.stores.focus.update(Editor.Focus.empty())
            refresh()
        }
    }

    fun onEnterScrollAndMoveClicked() {
        mode = EditorMode.SCROLL_AND_MOVE
        controlPanelInteractor.onEvent(ControlPanelMachine.Event.SAM.OnEnter)
    }

    fun onExitScrollAndMoveClicked() {
        mode = EditorMode.MULTI_SELECT
        controlPanelInteractor.onEvent(ControlPanelMachine.Event.SAM.OnExit)
    }

    private fun onEnterActionMode() {
        mode = EditorMode.ACTION_MODE
        controlPanelInteractor.onEvent(ControlPanelMachine.Event.ReadMode.OnEnter)
        viewModelScope.launch {
            refresh()
        }
    }

    private fun onExitActionMode() {
        mode = EditorMode.EDITING
        controlPanelInteractor.onEvent(ControlPanelMachine.Event.ReadMode.OnExit)
        viewModelScope.launch { refresh() }
    }

    fun onMultiSelectModeDeleteClicked() {
        controlPanelInteractor.onEvent(ControlPanelMachine.Event.MultiSelect.OnDelete)

        val exclude = mutableSetOf<String>()

        val selected = currentSelection().toList()

        blocks.filter { selected.contains(it.id) }.forEach { block ->
            block.children.forEach { if (selected.contains(it)) exclude.add(it) }
        }

        clearSelections()

        viewModelScope.launch {
            orchestrator.proxies.intents.send(
                Intent.CRUD.Unlink(
                    context = context,
                    targets = selected - exclude,
                    next = null,
                    previous = null,
                    effects = listOf(SideEffect.ClearMultiSelectSelection)
                )
            )
        }
    }

    fun onMultiSelectCopyClicked() {
        viewModelScope.launch {
            orchestrator.proxies.intents.send(
                Intent.Clipboard.Copy(
                    context = context,
                    blocks = blocks.filter { block ->
                        currentSelection().contains(block.id)
                    },
                    range = null
                )
            )
        }
    }

    fun onMultiSelectModeSelectAllClicked() =
        (stateData.value as ViewState.Success).let { state ->
            if (currentSelection().isEmpty()) {
                onSelectAllClicked(state)
            } else {
                onUnselectAllClicked(state)
            }
        }

    private fun onSelectAllClicked(state: ViewState.Success) =
        state.blocks.map { block ->
            if (block.id != context) select(block.id)
            block.updateSelection(newSelection = true)
        }.let {
            onMultiSelectModeBlockClicked()
            stateData.postValue(ViewState.Success(it))
        }

    private fun onUnselectAllClicked(state: ViewState.Success) =
        state.blocks.map { block ->
            if (block.id != context) unselect(block.id)
            block.updateSelection(newSelection = false)
        }.let {
            onMultiSelectModeBlockClicked()
            stateData.postValue(ViewState.Success(it))
        }

    fun onMultiSelectTurnIntoButtonClicked() {

        val excludedCategories = mutableListOf<String>()
        val excludedTypes = mutableListOf<String>()

        val targets = currentSelection()

        val blocks = blocks.filter { targets.contains(it.id) }

        val hasTextBlocks = blocks.any { it.content is Content.Text }
        val hasDividerBlocks = blocks.any { it.content is Content.Divider }

        when {
            hasTextBlocks -> {
                excludedTypes.apply {
                    add(UiBlock.FILE.name)
                    add(UiBlock.IMAGE.name)
                    add(UiBlock.VIDEO.name)
                    add(UiBlock.BOOKMARK.name)
                    add(UiBlock.LINE_DIVIDER.name)
                    add(UiBlock.THREE_DOTS.name)
                    add(UiBlock.LINK_TO_OBJECT.name)
                }
                dispatch(Command.OpenMultiSelectTurnIntoPanel(excludedCategories, excludedTypes))
            }
            hasDividerBlocks -> {
                excludedCategories.apply {
                    add(UiBlock.Category.TEXT.name)
                    add(UiBlock.Category.LIST.name)
                    add(UiBlock.Category.OBJECT.name)
                }
                excludedTypes.add(UiBlock.CODE.name)
                dispatch(Command.OpenMultiSelectTurnIntoPanel(excludedCategories, excludedTypes))
            }
            else -> {
                _error.value = ErrorViewState.Toast("Cannot turn selected blocks into other blocks")
            }
        }
    }

    fun onOpenPageNavigationButtonClicked() {
        navigation.postValue(
            EventWrapper(
                AppNavigation.Command.OpenPageNavigationScreen(
                    target = context
                )
            )
        )
    }

    override fun onTurnIntoBlockClicked(target: String, block: UiBlock) {
        if (block.isText() || block.isCode()) {
            proceedWithUpdatingTextStyle(
                style = block.style(),
                targets = listOf(target)
            )
        } else if (block == UiBlock.PAGE) {
            proceedWithTurningIntoDocument(listOf(target))
        }
        dispatch(Command.PopBackStack)
    }

    private fun proceedWithTurningIntoDocument(targets: List<String>) {
        viewModelScope.launch {
            orchestrator.proxies.intents.send(
                Intent.Document.TurnIntoDocument(
                    context = context,
                    targets = targets
                )
            )
        }
    }

    override fun onTurnIntoMultiSelectBlockClicked(block: UiBlock) {
        if (block.isText() || block.isCode()) {
            val targets = currentSelection().toList()
            clearSelections()
            controlPanelInteractor.onEvent(ControlPanelMachine.Event.MultiSelect.OnTurnInto)
            proceedWithUpdatingTextStyle(
                style = block.style(),
                targets = targets
            )
        } else if (block == UiBlock.PAGE) {
            val targets = currentSelection().toList()
            clearSelections()
            controlPanelInteractor.onEvent(ControlPanelMachine.Event.MultiSelect.OnTurnInto)
            proceedWithTurningIntoDocument(targets)
        } else {
            _error.value = ErrorViewState.Toast("Cannot convert selected blocks to $block")
        }
    }

    fun onTurnIntoStyleClicked(style: Content.Text.Style) {
        proceedWithUpdatingTextStyle(style, listOf(orchestrator.stores.focus.current().id))
    }

    fun onAddDividerBlockClicked() {

        val focused = blocks.first { it.id == orchestrator.stores.focus.current().id }
        val content = focused.content

        if (content is Content.Text && content.text.isEmpty()) {
            viewModelScope.launch {
                orchestrator.proxies.intents.send(
                    Intent.CRUD.Replace(
                        context = context,
                        target = focused.id,
                        prototype = Prototype.Divider
                    )
                )
            }
        } else {

            val position : Position

            var target: Id = focused.id

            if (focused.id == context) {
                if (focused.children.isEmpty()) {
                    position = Position.INNER
                } else {
                    position = Position.TOP
                    target = focused.children.first()
                }
            } else {
                position = Position.BOTTOM
            }

            viewModelScope.launch {
                orchestrator.proxies.intents.send(
                    Intent.CRUD.Create(
                        context = context,
                        target = target,
                        position = position,
                        prototype = Prototype.Divider
                    )
                )
            }
        }

        controlPanelInteractor.onEvent(ControlPanelMachine.Event.OnAddBlockToolbarOptionSelected)
    }

    private fun proceedWithUpdatingTextStyle(
        style: Content.Text.Style,
        targets: List<String>
    ) {
        viewModelScope.launch {
            orchestrator.proxies.intents.send(
                Intent.Text.UpdateStyle(
                    context = context,
                    targets = targets,
                    style = style
                )
            )
        }
    }

    fun onOutsideClicked() {
        blocks.first { it.id == context }.let { page ->
            if (page.children.isNotEmpty()) {
                val last = blocks.first { it.id == page.children.last() }
                when (val content = last.content) {
                    is Content.Text -> {
                        when {
                            content.style == Content.Text.Style.TITLE -> addNewBlockAtTheEnd()
                            content.text.isNotEmpty() -> addNewBlockAtTheEnd()
                            else -> Timber.d("Outside-click has been ignored.")
                        }
                    }
                    is Content.Link -> {
                        addNewBlockAtTheEnd()
                    }
                    is Content.Bookmark -> {
                        addNewBlockAtTheEnd()
                    }
                    is Content.File -> {
                        addNewBlockAtTheEnd()
                    }
                    is Content.Divider -> {
                        addNewBlockAtTheEnd()
                    }
                    else -> {
                        Timber.d("Outside-click has been ignored.")
                    }
                }
            } else {
                addNewBlockAtTheEnd()
            }
        }
    }

    fun onHideKeyboardClicked() {
        controlPanelInteractor.onEvent(ControlPanelMachine.Event.OnClearFocusClicked)
        viewModelScope.launch { orchestrator.stores.focus.update(Editor.Focus.empty()) }
        viewModelScope.launch { renderCommand.send(Unit) }
    }

    private fun proceedWithClearingFocus() {
        viewModelScope.launch {
            orchestrator.stores.focus.update(Editor.Focus.empty())
            refresh()
        }
        controlPanelInteractor.onEvent(ControlPanelMachine.Event.OnClearFocusClicked)
    }

    private suspend fun refresh() {
        Timber.d("Refreshing: $blocks")
        renderizePipeline.send(blocks)
    }

    private fun onPageClicked(target: String) =
        proceedWithOpeningPage(
            target = blocks.first { it.id == target }.content<Content.Link>().target
        )

    private fun onMentionClicked(target: String) {
        proceedWithClearingFocus()
        proceedWithOpeningPage(target = target)
    }

    fun onAddNewPageClicked() {
        controlPanelInteractor.onEvent(ControlPanelMachine.Event.OnAddBlockToolbarOptionSelected)

        val position : Position

        val focused = blocks.first { it.id == orchestrator.stores.focus.current().id }

        var target = focused.id

        if (focused.id == context) {
            if (focused.children.isEmpty())
                position = Position.INNER
            else {
                position = Position.TOP
                target = focused.children.first()
            }
        } else {
            position = Position.BOTTOM
        }

        val params = CreateDocument.Params(
            context = context,
            position = position,
            target = target,
            prototype = Prototype.Page(style = Content.Page.Style.EMPTY)
        )

        viewModelScope.launch {
            createDocument(
                params = params
            ).proceed(
                failure = { Timber.e(it, "Error while creating new page with params: $params") },
                success = { result ->
                    orchestrator.proxies.payloads.send(result.payload)
                    proceedWithOpeningPage(result.target)
                }
            )
        }
    }

    fun onArchiveThisPageClicked() {
        dispatch(command = Command.CloseKeyboard)
        viewModelScope.launch {
            archiveDocument(
                ArchiveDocument.Params(
                    context = context,
                    targets = listOf<String>(context),
                    isArchived = true
                )
            ).proceed(
                failure = { Timber.e(it, "Error while archiving page") },
                success = { proceedWithExiting() }
            )
        }
    }

    fun onAddBookmarkBlockClicked() {

        val focused = blocks.first { it.id == orchestrator.stores.focus.current().id }

        val content = focused.content

        if (content is Content.Text && content.text.isEmpty()) {
            viewModelScope.launch {
                orchestrator.proxies.intents.send(
                    Intent.CRUD.Replace(
                        context = context,
                        target = focused.id,
                        prototype = Prototype.Bookmark
                    )
                )
            }
        } else {

            val position : Position

            var target: Id = focused.id

            if (focused.id == context) {
                if (focused.children.isEmpty()) {
                    position = Position.INNER
                } else {
                    position = Position.TOP
                    target = focused.children.first()
                }
            } else {
                position = Position.BOTTOM
            }

            viewModelScope.launch {
                orchestrator.proxies.intents.send(
                    Intent.CRUD.Create(
                        context = context,
                        position = position,
                        target = target,
                        prototype = Prototype.Bookmark
                    )
                )
            }
        }
        controlPanelInteractor.onEvent(ControlPanelMachine.Event.OnAddBlockToolbarOptionSelected)
    }

    fun onAddBookmarkUrl(target: String, url: String) {
        viewModelScope.launch {
            orchestrator.proxies.intents.send(
                Intent.Bookmark.SetupBookmark(
                    context = context,
                    target = target,
                    url = url
                )
            )
        }
    }

    private fun onBookmarkPlaceholderClicked(target: String) =
        dispatch(
            command = Command.OpenBookmarkSetter(
                context = context,
                target = target
            )
        )

    private fun onBookmarkClicked(view: BlockView.Media.Bookmark) =
        dispatch(command = Command.Browse(view.url))

    private fun onFailedBookmarkClicked(view: BlockView.Error.Bookmark) =
        dispatch(command = Command.Browse(view.url))

    fun onTitleTextInputClicked() {
        controlPanelInteractor.onEvent(ControlPanelMachine.Event.OnTextInputClicked)
    }

    fun onTextInputClicked(target: Id) {
        Timber.d("onTextInputClicked: $target")
        if (mode == EditorMode.MULTI_SELECT) {
            onBlockMultiSelectClicked(target)
        } else {
            controlPanelInteractor.onEvent(ControlPanelMachine.Event.OnTextInputClicked)
        }
    }

    private fun onBlockMultiSelectClicked(target: Id) {
        (stateData.value as? ViewState.Success)?.let { state ->

            var allow = true

            val block = blocks.first { it.id == target }

            val parent = blocks.find { it.children.contains(target) }

            if (parent != null && parent.id != context) {
                if (isSelected(parent.id)) allow = false
            }

            if (!allow) return

            toggleSelection(target)

            if (isSelected(target)) {
                block.children.forEach { child -> select(child) }
            } else {
                block.children.forEach { child -> unselect(child) }
            }

            onMultiSelectModeBlockClicked()

            val update = state.blocks.map { view ->
                if (view.id == target || block.children.contains(view.id))
                    view.updateSelection(newSelection = isSelected(target))
                else
                    view
            }

            stateData.postValue(ViewState.Success(update))
        }
    }

    fun onPaste(
        range: IntRange
    ) {
        viewModelScope.launch {
            orchestrator.proxies.intents.send(
                Intent.Clipboard.Paste(
                    context = context,
                    focus = orchestrator.stores.focus.current().id,
                    range = range,
                    selected = emptyList()
                )
            )
        }
    }

    fun onApplyScrollAndMove(
        target: Id,
        ratio: Float
    ) {

        val exclude = mutableSetOf<String>()

        var moveTarget = target

        var position = when (ratio) {
            in START_RANGE -> Position.TOP
            in END_RANGE -> Position.BOTTOM
            in INNER_RANGE -> Position.INNER
            else -> {
                if (ratio > 1) Position.BOTTOM
                else throw IllegalStateException("Unexpected ratio: $ratio")
            }
        }

        val targetBlock = blocks.first { it.id == target }

        val parent = blocks.find { it.children.contains(target) }?.id

        val selected = currentSelection().toList()

        if (selected.contains(target)) {
            _error.value = ErrorViewState.Toast(CANNOT_BE_DROPPED_INSIDE_ITSELF_ERROR)
            return
        }

        if (selected.contains(parent)) {
            _error.value = ErrorViewState.Toast(CANNOT_MOVE_PARENT_INTO_CHILD)
            return
        }

        if (position == Position.INNER) {

            if (!targetBlock.supportNesting()) {
                _error.value = ErrorViewState.Toast(CANNOT_BE_PARENT_ERROR)
                return
            }

            val targetContext = if (targetBlock.content is Content.Link) {
                targetBlock.content<Content.Link>().target
            } else {
                context
            }

            blocks.filter { selected.contains(it.id) }.forEach { block ->
                block.children.forEach { if (selected.contains(it)) exclude.add(it) }
            }

            clearSelections()

            controlPanelInteractor.onEvent(ControlPanelMachine.Event.SAM.OnApply)

            mode = EditorMode.MULTI_SELECT

            viewModelScope.launch {
                orchestrator.proxies.intents.send(
                    Intent.Document.Move(
                        context = context,
                        target = moveTarget,
                        targetContext = targetContext,
                        blocks = selected - exclude,
                        position = position
                    )
                )
            }
        } else {

            val targetContext = context

            if (target == context) {
                position = Position.TOP
                moveTarget = targetBlock.children.first()
            }

            blocks.filter { selected.contains(it.id) }.forEach { block ->
                block.children.forEach { if (selected.contains(it)) exclude.add(it) }
            }

            clearSelections()

            controlPanelInteractor.onEvent(ControlPanelMachine.Event.SAM.OnApply)

            mode = EditorMode.MULTI_SELECT

            viewModelScope.launch {
                orchestrator.proxies.intents.send(
                    Intent.Document.Move(
                        context = context,
                        target = moveTarget,
                        targetContext = targetContext,
                        blocks = selected - exclude,
                        position = position
                    )
                )
            }
        }
    }

    fun onCopy(
        range: IntRange
    ) {
        viewModelScope.launch {
            orchestrator.proxies.intents.send(
                Intent.Clipboard.Copy(
                    context = context,
                    range = range,
                    blocks = listOf(blocks.first { it.id == focus.value })
                )
            )
        }
    }

    fun onClickListener(clicked: ListenerType) = when (clicked) {
        is ListenerType.Bookmark.View -> {
            when (mode) {
                EditorMode.EDITING -> onBookmarkClicked(clicked.item)
                EditorMode.MULTI_SELECT -> onBlockMultiSelectClicked(clicked.item.id)
                else -> Unit
            }
        }
        is ListenerType.Bookmark.Placeholder -> {
            when (mode) {
                EditorMode.EDITING -> onBookmarkPlaceholderClicked(clicked.target)
                EditorMode.MULTI_SELECT -> onBlockMultiSelectClicked(clicked.target)
                else -> Unit
            }
        }
        is ListenerType.Bookmark.Error -> {
            when (mode) {
                EditorMode.EDITING -> onFailedBookmarkClicked(clicked.item)
                EditorMode.MULTI_SELECT -> onBlockMultiSelectClicked(clicked.item.id)
                else -> Unit
            }
        }
        is ListenerType.File.View -> {
            when (mode) {
                EditorMode.EDITING -> onFileClicked(clicked.target)
                EditorMode.MULTI_SELECT -> onBlockMultiSelectClicked(clicked.target)
                else -> Unit
            }
        }
        is ListenerType.File.Placeholder -> {
            when (mode) {
                EditorMode.EDITING -> onAddLocalFileClicked(clicked.target)
                EditorMode.MULTI_SELECT -> onBlockMultiSelectClicked(clicked.target)
                else -> Unit
            }
        }
        is ListenerType.File.Error -> {
            when (mode) {
                EditorMode.EDITING -> onAddLocalFileClicked(clicked.target)
                EditorMode.MULTI_SELECT -> onBlockMultiSelectClicked(clicked.target)
                else -> Unit
            }
        }
        is ListenerType.File.Upload -> {
            when (mode) {
                EditorMode.EDITING -> Unit
                EditorMode.MULTI_SELECT -> onBlockMultiSelectClicked(clicked.target)
                else -> Unit
            }
        }
        is ListenerType.Picture.View -> {
            when (mode) {
                EditorMode.EDITING -> Unit
                EditorMode.MULTI_SELECT -> onBlockMultiSelectClicked(clicked.target)
                else -> Unit
            }
        }
        is ListenerType.Picture.Placeholder -> {
            when (mode) {
                EditorMode.EDITING -> onAddLocalPictureClicked(clicked.target)
                EditorMode.MULTI_SELECT -> onBlockMultiSelectClicked(clicked.target)
                else -> Unit
            }
        }
        is ListenerType.Picture.Error -> {
            when (mode) {
                EditorMode.EDITING -> Unit
                EditorMode.MULTI_SELECT -> onBlockMultiSelectClicked(clicked.target)
                else -> Unit
            }
        }
        is ListenerType.Picture.Upload -> {
            when (mode) {
                EditorMode.EDITING -> Unit
                EditorMode.MULTI_SELECT -> onBlockMultiSelectClicked(clicked.target)
                else -> Unit
            }
        }
        is ListenerType.Video.View -> {
            when (mode) {
                EditorMode.EDITING -> Unit
                EditorMode.MULTI_SELECT -> onBlockMultiSelectClicked(clicked.target)
                else -> Unit
            }
        }
        is ListenerType.Video.Placeholder -> {
            when (mode) {
                EditorMode.EDITING -> onAddLocalVideoClicked(clicked.target)
                EditorMode.MULTI_SELECT -> onBlockMultiSelectClicked(clicked.target)
                else -> Unit
            }
        }
        is ListenerType.Video.Error -> {
            when (mode) {
                EditorMode.EDITING -> Unit
                EditorMode.MULTI_SELECT -> onBlockMultiSelectClicked(clicked.target)
                else -> Unit
            }
        }
        is ListenerType.Video.Upload -> {
            when (mode) {
                EditorMode.EDITING -> Unit
                EditorMode.MULTI_SELECT -> onBlockMultiSelectClicked(clicked.target)
                else -> Unit
            }
        }
        is ListenerType.LongClick -> {
            when (mode) {
                EditorMode.EDITING -> onBlockLongPressedClicked(clicked.target, clicked.dimensions)
                EditorMode.MULTI_SELECT -> Unit
                else -> Unit
            }
        }
        is ListenerType.Page -> {
            when (mode) {
                EditorMode.EDITING -> onPageClicked(clicked.target)
                EditorMode.MULTI_SELECT -> onBlockMultiSelectClicked(clicked.target)
                else -> Unit
            }
        }
        is ListenerType.Mention -> {
            when (mode) {
                EditorMode.EDITING -> onMentionClicked(clicked.target)
                else -> Unit
            }
        }
        is ListenerType.EditableBlock -> {
            //Todo block view refactoring
        }
        ListenerType.TitleBlock -> {
            //Todo block view refactoring
        }
        is ListenerType.DividerClick -> {
            when (mode) {
                EditorMode.MULTI_SELECT -> onBlockMultiSelectClicked(clicked.target)
                else -> Unit
            }
        }
    }

    fun onPlusButtonPressed() {
        createPage(
            scope = viewModelScope,
            params = CreatePage.Params.insideDashboard()
        ) { result ->
            result.either(
                fnL = { Timber.e(it, "Error while creating a new page on home dashboard") },
                fnR = { id -> proceedWithOpeningPage(id) }
            )
        }
    }

    fun onProceedWithFilePath(filePath: String?) {
        if (filePath == null) {
            Timber.d("Error while getting filePath")
            return
        }
        viewModelScope.launch {
            orchestrator.proxies.intents.send(
                Intent.Media.Upload(
                    context = context,
                    target = mediaBlockId,
                    filePath = filePath,
                    url = ""
                )
            )
        }
    }

    fun onPageIconClicked() {
        val details = orchestrator.stores.details.current()
        dispatch(
            Command.OpenDocumentIconActionMenu(
                target = context,
                emoji = details.details[context]?.iconEmoji?.let { name ->
                    if (name.isNotEmpty())
                        name
                    else
                        null
                },
                image = details.details[context]?.iconImage?.let { name ->
                    if (name.isNotEmpty())
                        urlBuilder.image(name)
                    else
                        null
                }
            )
        )
    }

    fun onProfileIconClicked() {
        val details = orchestrator.stores.details.current()
        dispatch(
            Command.OpenProfileIconActionMenu(
                target = context,
                image = details.details[context]?.iconImage?.let { name ->
                    if (name.isNotEmpty() && name.isNotBlank())
                        urlBuilder.image(name)
                    else
                        null
                },
                name = details.details[context]?.name
            )
        )
    }

    private fun onFileClicked(id: String) {
        dispatch(Command.RequestDownloadPermission(id))
    }

    fun startDownloadingFile(id: String) {

        _error.value = ErrorViewState.Toast("Downloading file in background...")

        val block = blocks.first { it.id == id }
        val file = block.content<Content.File>()

        viewModelScope.launch {
            orchestrator.proxies.intents.send(
                Intent.Media.DownloadFile(
                    url = when (file.type) {
                        Content.File.Type.IMAGE -> urlBuilder.image(file.hash)
                        else -> urlBuilder.file(file.hash)
                    },
                    name = file.name.orEmpty()
                )
            )
        }
    }

    fun onPageSearchClicked() {
        navigation.postValue(EventWrapper(AppNavigation.Command.OpenPageSearch))
    }

    fun onMentionEvent(mentionEvent: MentionEvent) {
        when (mentionEvent) {
            is MentionEvent.MentionSuggestText -> {
                controlPanelInteractor.onEvent(
                    ControlPanelMachine.Event.Mentions.OnQuery(
                        text = mentionEvent.text.toString()
                    )
                )
            }
            is MentionEvent.MentionSuggestStart -> {
                mentionFrom = mentionEvent.mentionStart
                controlPanelInteractor.onEvent(
                    ControlPanelMachine.Event.Mentions.OnStart(
                        cursorCoordinate = mentionEvent.cursorCoordinate,
                        mentionFrom = mentionEvent.mentionStart
                    )
                )
                viewModelScope.launch {
                    getListPages.invoke(Unit).proceed(
                        failure = { it.timber() },
                        success = { response ->
                            controlPanelInteractor.onEvent(
                                ControlPanelMachine.Event.Mentions.OnResult(
                                    mentions = response.listPages.map { it.toMentionView(urlBuilder) }
                                )
                            )
                        }
                    )
                }
            }
            MentionEvent.MentionSuggestStop -> {
                mentionFrom = -1
                controlPanelInteractor.onEvent(
                    ControlPanelMachine.Event.Mentions.OnStop
                )
            }
        }
    }

    fun onAddMentionNewPageClicked() {
        onAddNewPageClicked()
    }

    fun onMentionSuggestClick(mention: Mention, mentionTrigger: String) {
        Timber.d("onAddMentionClicked, suggest:$mention, from:$mentionFrom")

        controlPanelInteractor.onEvent(ControlPanelMachine.Event.Mentions.OnMentionClicked)

        val target = blocks.first { it.id == focus.value }

        val new = target.addMention(
            mentionText = mention.title,
            mentionId = mention.id,
            from = mentionFrom,
            mentionTrigger = mentionTrigger
        )

        blocks = blocks.map { block ->
            if (block.id != target.id)
                block
            else
                new
        }

        viewModelScope.launch {
            val position = mentionFrom + mention.title.length + 1
            orchestrator.stores.focus.update(
                t = Editor.Focus(
                    id = new.id,
                    cursor = Editor.Cursor.Range(IntRange(position, position))
                )
            )
            refresh()
        }

        viewModelScope.launch {
            proceedWithUpdatingText(
                intent = Intent.Text.UpdateText(
                    context = context,
                    target = new.id,
                    text = new.content<Content.Text>().text,
                    marks = new.content<Content.Text>().marks
                )
            )
        }
    }

    private fun onMultiSelectModeBlockClicked() {
        controlPanelInteractor.onEvent(
            ControlPanelMachine.Event.MultiSelect.OnBlockClick(
                count = currentSelection().size
            )
        )
    }

    private fun addNewBlockAtTheEnd() {
        proceedWithCreatingNewTextBlock(
            id = "",
            position = Position.INNER,
            style = Content.Text.Style.P
        )
    }

    private fun proceedWithOpeningPage(target: Id) {
        navigate(EventWrapper(AppNavigation.Command.OpenPage(target)))
    }

    companion object {
        const val EMPTY_CONTEXT = ""
        const val EMPTY_FOCUS_ID = ""
        const val TEXT_CHANGES_DEBOUNCE_DURATION = 500L
        const val DELAY_REFRESH_DOCUMENT_TO_ENTER_MULTI_SELECT_MODE = 150L
        const val INITIAL_INDENT = 0
        const val CANNOT_BE_DROPPED_INSIDE_ITSELF_ERROR = "A block cannot be moved inside itself."
        const val CANNOT_BE_PARENT_ERROR = "This block does not support nesting."
        const val CANNOT_MOVE_PARENT_INTO_CHILD =
            "Cannot move parent into child. Please, check selected blocks."
    }

    data class MarkupAction(
        val type: Markup.Type,
        val param: String? = null
    )

    override fun onCleared() {
        super.onCleared()

        orchestrator.stores.focus.cancel()
        orchestrator.stores.details.cancel()
        orchestrator.stores.textSelection.cancel()
        orchestrator.proxies.changes.cancel()
        orchestrator.proxies.saves.cancel()

        markupActionPipeline.cancel()
        renderizePipeline.cancel()

        controlPanelInteractor.channel.cancel()
        titleChannel.cancel()

        Timber.d("onCleared")
    }

    fun onStop() {
        Timber.d("onStop")
        eventSubscription?.cancel()
    }

    enum class Session { IDLE, OPEN, ERROR }
}