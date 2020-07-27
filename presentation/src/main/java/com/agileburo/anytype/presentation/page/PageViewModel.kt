package com.agileburo.anytype.presentation.page

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.agileburo.anytype.core_ui.common.Alignment
import com.agileburo.anytype.core_ui.common.Markup
import com.agileburo.anytype.core_ui.extensions.updateSelection
import com.agileburo.anytype.core_ui.features.page.BlockDimensions
import com.agileburo.anytype.core_ui.features.page.BlockView
import com.agileburo.anytype.core_ui.features.page.ListenerType
import com.agileburo.anytype.core_ui.features.page.TurnIntoActionReceiver
import com.agileburo.anytype.core_ui.model.UiBlock
import com.agileburo.anytype.core_ui.state.ControlPanelState
import com.agileburo.anytype.core_ui.widgets.ActionItemType
import com.agileburo.anytype.core_utils.common.EventWrapper
import com.agileburo.anytype.core_utils.ext.*
import com.agileburo.anytype.core_utils.ui.ViewStateViewModel
import com.agileburo.anytype.domain.block.interactor.RemoveLinkMark
import com.agileburo.anytype.domain.block.interactor.UpdateLinkMarks
import com.agileburo.anytype.domain.block.model.Block
import com.agileburo.anytype.domain.block.model.Block.Content
import com.agileburo.anytype.domain.block.model.Block.Prototype
import com.agileburo.anytype.domain.block.model.Position
import com.agileburo.anytype.domain.common.Id
import com.agileburo.anytype.domain.editor.Editor
import com.agileburo.anytype.domain.event.interactor.InterceptEvents
import com.agileburo.anytype.domain.event.model.Event
import com.agileburo.anytype.domain.event.model.Payload
import com.agileburo.anytype.domain.ext.asMap
import com.agileburo.anytype.domain.ext.content
import com.agileburo.anytype.domain.ext.textStyle
import com.agileburo.anytype.domain.misc.UrlBuilder
import com.agileburo.anytype.domain.page.*
import com.agileburo.anytype.presentation.common.StateReducer
import com.agileburo.anytype.presentation.common.SupportCommand
import com.agileburo.anytype.presentation.mapper.style
import com.agileburo.anytype.presentation.navigation.AppNavigation
import com.agileburo.anytype.presentation.navigation.SupportNavigation
import com.agileburo.anytype.presentation.page.ControlPanelMachine.Interactor
import com.agileburo.anytype.presentation.page.editor.*
import com.agileburo.anytype.presentation.page.model.TextUpdate
import com.agileburo.anytype.presentation.page.render.BlockViewRenderer
import com.agileburo.anytype.presentation.page.render.DefaultBlockViewRenderer
import com.agileburo.anytype.presentation.page.selection.SelectionStateHolder
import com.agileburo.anytype.presentation.page.toggle.ToggleStateHolder
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
    private val orchestrator: Orchestrator
) : ViewStateViewModel<ViewState>(),
    SupportNavigation<EventWrapper<AppNavigation.Command>>,
    SupportCommand<Command>,
    BlockViewRenderer by renderer,
    ToggleStateHolder by renderer,
    SelectionStateHolder by orchestrator.memory.selections,
    TurnIntoActionReceiver,
    StateReducer<List<Block>, Event> by reducer {

    private var mode = EditorMode.EDITING

    private val controlPanelInteractor = Interactor(viewModelScope)
    val controlPanelViewState = MutableLiveData<ControlPanelState>()

    private val renderings = Proxy.Subject<List<Block>>()
    private val selections = Proxy.Subject<Pair<Id, IntRange>>()
    private val markups = Proxy.Subject<MarkupAction>()

    private val titleChannel = Channel<String>()
    private val titleChanges = titleChannel.consumeAsFlow()

    /**
     * Currently opened page id.
     */
    var context: String = ""

    /**
     * Current set of blocks on this page.
     */
    var blocks: List<Block> = emptyList()

    private val _focus: MutableLiveData<Id> = MutableLiveData()
    val focus: LiveData<Id> = _focus

    private val _error: MutableLiveData<String> = MutableLiveData()
    val error: LiveData<String> = _error

    /**
     * Open gallery and search media files for block with that id
     */
    private var mediaBlockId = ""

    override val navigation = MutableLiveData<EventWrapper<AppNavigation.Command>>()
    override val commands = MutableLiveData<EventWrapper<Command>>()

    init {
        startHandlingTextChanges()
        startProcessingFocusChanges()
        startProcessingTitleChanges()
        startProcessingControlPanelViewState()
        startObservingEvents()
        startObservingPayload()
        startObservingErrors()
        processRendering()
        processMarkupChanges()
        viewModelScope.launch { orchestrator.start() }
    }

    private fun startProcessingFocusChanges() {
        viewModelScope.launch {
            orchestrator.stores.focus.stream().collect { _focus.postValue(it.id) }
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

    private fun startObservingEvents() {
        viewModelScope.launch {
            interceptEvents
                .build()
                .filter { events -> events.any { it.context == context } }
                .map { events -> processEvents(events) }
                .collect { viewModelScope.launch { refresh() } }
        }
    }

    private fun startObservingErrors() {
        viewModelScope.launch {
            orchestrator.proxies.errors
                .stream()
                .collect {
                    _error.value = it.message ?: "Unknown error"
                }
        }
    }

    private suspend fun processEvents(events: List<Event>) {
        Timber.d("Blocks before handling events: $blocks")
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
        markups
            .stream()
            .withLatestFrom(
                selections
                    .stream()
                    .distinctUntilChanged()
                    .filter { (_, selection) -> selection.first != selection.last }
            ) { a, b -> Pair(a, b) }
            .onEach { (action, selection) ->
                if (action.type == Markup.Type.LINK) {
                    val block = blocks.first { it.id == selection.first }
                    val range = IntRange(
                        start = selection.second.first,
                        endInclusive = selection.second.last.dec()
                    )
                    stateData.value = ViewState.OpenLinkScreen(context, block, range)
                } else {
                    applyMarkup(selection, action)
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

        orchestrator
            .stores
            .views
            .stream()
            .onEach { dispatchToUI(it) }
            .launchIn(viewModelScope)

        // renderize, in order to send to UI

        renderings
            .stream()
            .filter { it.isNotEmpty() }
            .onEach {
                if (focus.value != null && focus.value != context) {
                    controlPanelInteractor.onEvent(
                        event = ControlPanelMachine.Event.OnRefresh(
                            target = blocks.find { it.id == focus.value }
                        )
                    )
                }
            }
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
            .onEach { orchestrator.stores.views.update(it) }
            .launchIn(viewModelScope)
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

    fun open(id: String) {

        context = id

        stateData.postValue(ViewState.Loading)

        viewModelScope.launch {
            openPage(OpenPage.Params(id)).proceed(
                success = { payload ->
                    onStartFocusing(payload)
                    orchestrator.proxies.payloads.send(payload)
                },
                failure = { Timber.e(it, "Error while opening page with id: $id") }
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

    fun onBackButtonPressed() {
        proceedWithExiting()
    }

    fun onBottomSheetHidden() {
        proceedWithExitingToDesktop()
    }

    private fun proceedWithExiting() {
        viewModelScope.launch {
            closePage(
                ClosePage.Params(context)
            ).proceed(
                success = { navigation.postValue(EventWrapper(AppNavigation.Command.Exit)) },
                failure = { Timber.e(it, "Error while closing the test page") }
            )
        }
    }

    private fun proceedWithExitingToDesktop() {
        closePage(viewModelScope, ClosePage.Params(context)) { result ->
            result.either(
                fnR = { navigation.postValue(EventWrapper(AppNavigation.Command.ExitToDesktop)) },
                fnL = { Timber.e(it, "Error while closing the test page") }
            )
        }
    }

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

    fun onParagraphTextChanged(
        id: String,
        text: String,
        marks: List<Content.Text.Mark>
    ) {
        val update = TextUpdate.Pattern(
            target = id,
            text = text,
            markup = marks
        )
        Timber.d("onParagraphTextChanged: $update")
        viewModelScope.launch {
            orchestrator.proxies.changes.send(update)
        }
    }

    fun onSelectionChanged(id: String, selection: IntRange) {
        viewModelScope.launch { selections.send(Pair(id, selection)) }
        controlPanelInteractor.onEvent(ControlPanelMachine.Event.OnSelectionChanged(selection))
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

    fun onSplitLineEnterClicked(
        target: String,
        index: Int,
        text: String,
        marks: List<Content.Text.Mark>
    ) {
        if (target == context) return

        var style: Content.Text.Style = Content.Text.Style.P

        blocks = blocks.map { block ->
            if (block.id == target) {
                val content = block.content<Content.Text>()
                style = content.style
                block.copy(
                    content = content.copy(
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
                    target = target,
                    index = index,
                    style = style
                )
            )
        }
    }

    fun onEndLineEnterTitleClicked() {
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
        val state = stateData.value
        if (state is ViewState.Success) {
            dispatch(Command.OpenActionBar(
                block = state.blocks.first { it.id == target },
                dimensions = dimensions
            ))
        } else {
            Timber.e("onBlockLongPressedClicked, state:$state should be ViewState.Success, to get proper BlockView")
        }
    }

    fun onMarkupActionClicked(markup: Markup.Type, selection: IntRange) {
        when (markup) {
            Markup.Type.BACKGROUND_COLOR -> {
                controlPanelInteractor.onEvent(
                    ControlPanelMachine.Event.OnMarkupContextMenuBackgroundColorClicked(
                        target = blocks.first { it.id == orchestrator.stores.focus.current().id },
                        selection = selection
                    )
                )
            }
            Markup.Type.TEXT_COLOR -> {
                controlPanelInteractor.onEvent(
                    ControlPanelMachine.Event.OnMarkupContextMenuTextColorClicked(
                        target = blocks.first { it.id == orchestrator.stores.focus.current().id },
                        selection = selection
                    )
                )
            }
            else -> {
                viewModelScope.launch {
                    markups.send(MarkupAction(type = markup))
                }
            }
        }
    }

    fun onMarkupTextColorAction(color: String) {
        controlPanelInteractor.onEvent(
            ControlPanelMachine.Event.OnMarkupTextColorSelected
        )

        viewModelScope.launch {
            markups.send(
                MarkupAction(
                    type = Markup.Type.TEXT_COLOR,
                    param = color
                )
            )
        }
    }

    fun onStyleBackgroundSlideClicked() {
        controlPanelInteractor.onEvent(ControlPanelMachine.Event.OnStyleBackgroundSlideClicked)
    }

    fun onStyleColorSlideClicked() {
        controlPanelInteractor.onEvent(ControlPanelMachine.Event.OnStyleColorSlideClicked)
    }

    fun onMarkupBackgroundColorAction(color: String) {
        controlPanelInteractor.onEvent(
            ControlPanelMachine.Event.OnMarkupBackgroundColorSelected
        )

        viewModelScope.launch {
            markups.send(
                MarkupAction(
                    type = Markup.Type.BACKGROUND_COLOR,
                    param = color
                )
            )
        }
    }

    fun onBlockAlignmentActionClicked(alignment: Alignment) {
        controlPanelInteractor.onEvent(
            ControlPanelMachine.Event.OnBlockAlignmentSelected
        )

        val state = stateData.value
        if (state is ViewState.Success) {
            val blockView = state.blocks.first { it.id == focus.value }
            if (blockView is BlockView.Alignable) {
                updateBlockAlignment(blockView, alignment)
            } else {
                Timber.e("Block $blockView is not alignable type")
            }
        } else {
            Timber.e("Error on block align update, ViewState:$state is not ViewState.Success")
        }
    }

    private fun updateBlockAlignment(
        blockView: BlockView,
        alignment: Alignment
    ) {
        viewModelScope.launch {
            orchestrator.proxies.intents.send(
                Intent.Text.Align(
                    context = context,
                    target = blockView.id,
                    alignment = when (alignment) {
                        Alignment.START -> Block.Align.AlignLeft
                        Alignment.CENTER -> Block.Align.AlignCenter
                        Alignment.END -> Block.Align.AlignRight
                    }
                )
            )
        }
    }

    fun onCloseBlockStyleToolbarClicked() {
        controlPanelInteractor.onEvent(ControlPanelMachine.Event.OnBlockStyleToolbarCloseButtonClicked)
    }

    fun onToolbarTextColorAction(color: String) {
        controlPanelInteractor.onEvent(ControlPanelMachine.Event.OnBlockTextColorSelected)
        viewModelScope.launch {
            orchestrator.proxies.intents.send(
                Intent.Text.UpdateColor(
                    context = context,
                    target = orchestrator.stores.focus.current().id,
                    color = color
                )
            )
        }
    }

    fun onBlockBackgroundColorAction(color: String) {
        controlPanelInteractor.onEvent(ControlPanelMachine.Event.OnBlockBackgroundColorSelected)
        viewModelScope.launch {
            orchestrator.proxies.intents.send(
                Intent.Text.UpdateBackgroundColor(
                    context = context,
                    targets = listOf(orchestrator.stores.focus.current().id),
                    color = color
                )
            )
        }
    }

    fun onBlockStyleLinkClicked() {
        val target = blocks.first { it.id == orchestrator.stores.focus.current().id }
        val range = IntRange(
            start = 0,
            endInclusive = target.content<Content.Text>().text.length.dec()
        )
        stateData.value = ViewState.OpenLinkScreen(context, target, range)
    }

    fun onBlockStyleMarkupActionClicked(action: Markup.Type) {

        controlPanelInteractor.onEvent(
            ControlPanelMachine.Event.OnBlockStyleSelected
        )

        val target = blocks.first { it.id == focus.value }
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

    fun onActionBarItemClicked(id: String, action: ActionItemType) {
        when (action) {
            ActionItemType.TurnInto -> {
                dispatch(Command.OpenTurnIntoPanel(target = id))
            }
            ActionItemType.Delete -> {
                proceedWithUnlinking(target = id)
                dispatch(Command.PopBackStack)
            }
            ActionItemType.Duplicate -> {
                duplicateBlock(target = id)
                dispatch(Command.PopBackStack)
            }
            ActionItemType.Rename -> {
                _error.value = "Rename not implemented"
            }
            ActionItemType.MoveTo -> {
                _error.value = "Move To not implemented"
            }
            ActionItemType.Color -> {
                controlPanelInteractor.onEvent(
                    ControlPanelMachine.Event.OnBlockActionToolbarTextColorClicked(
                        target = blocks.first { it.id == orchestrator.stores.focus.current().id }
                    )
                )
                dispatch(Command.PopBackStack)
            }
            ActionItemType.Background -> {
                controlPanelInteractor.onEvent(
                    ControlPanelMachine.Event.OnBlockActionToolbarBackgroundColorClicked(
                        target = blocks.first { it.id == orchestrator.stores.focus.current().id }
                    )
                )
                dispatch(Command.PopBackStack)
            }
            ActionItemType.Style -> {
                controlPanelInteractor.onEvent(
                    ControlPanelMachine.Event.OnBlockActionToolbarStyleClicked(
                        target = blocks.first { it.id == orchestrator.stores.focus.current().id }
                    )
                )
                dispatch(Command.PopBackStack)
            }
            ActionItemType.Download -> {
                _error.value = "Download not implemented"
            }
            ActionItemType.Replace -> {
                _error.value = "Replace not implemented"
            }
            ActionItemType.AddCaption -> {
                _error.value = "Add caption not implemented"
            }
            ActionItemType.Divider -> {
                _error.value = "not implemented"
            }
        }
    }

    private fun proceedWithUnlinking(target: String) {

        // TODO support nested blocks

        val parent = blocks.first { it.children.contains(target) }

        val index = parent.children.indexOf(target)

        val previous = index.dec().let { prev ->
            if (prev != -1) parent.children[prev] else context
        }

        val cursor = blocks.find { it.id == previous }?.let { block ->
            if (block.content is Content.Text) {
                block.content.asText().text.length
            } else {
                null
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

    fun onActionDuplicateClicked() {
        duplicateBlock(target = orchestrator.stores.focus.current().id)
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
        controlPanelInteractor.onEvent(ControlPanelMachine.Event.OnAddBlockToolbarOptionSelected)
        proceedWithCreatingNewTextBlock(
            id = orchestrator.stores.focus.current().id,
            style = style
        )
    }

    fun onAddVideoBlockClicked() {
        proceedWithCreatingEmptyFileBlock(
            id = orchestrator.stores.focus.current().id,
            type = Content.File.Type.VIDEO
        )
    }

    private fun onAddLocalVideoClicked(blockId: String) {
        mediaBlockId = blockId
        dispatch(Command.OpenGallery(mediaType = MIME_VIDEO_ALL))
    }

    private fun onAddLocalPictureClicked(blockId: String) {
        mediaBlockId = blockId
        dispatch(Command.OpenGallery(mediaType = MIME_IMAGE_ALL))
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

    fun onAddImageBlockClicked() {
        proceedWithCreatingEmptyFileBlock(
            id = orchestrator.stores.focus.current().id,
            type = Content.File.Type.IMAGE
        )
    }

    fun onAddFileBlockClicked() {
        proceedWithCreatingEmptyFileBlock(
            id = orchestrator.stores.focus.current().id,
            type = Content.File.Type.FILE
        )
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

    fun onCheckboxClicked(id: String) {
        val target = blocks.first { it.id == id }
        viewModelScope.launch {
            orchestrator.proxies.intents.send(
                Intent.Text.UpdateCheckbox(
                    context = context,
                    target = target.id,
                    isChecked = target.content<Content.Text>().toggleCheck()
                )
            )
        }
    }

    fun onAddBlockToolbarClicked() {
        dispatch(Command.OpenAddBlockPanel)
    }

    fun onEnterMultiSelectModeClicked() {
        controlPanelInteractor.onEvent(ControlPanelMachine.Event.OnEnterMultiSelectModeClicked)
        mode = EditorMode.MULTI_SELECT
        viewModelScope.launch {
            delay(150)
            refresh()
        }
    }

    fun onExitMultiSelectModeClicked() {
        controlPanelInteractor.onEvent(ControlPanelMachine.Event.OnExitMultiSelectModeClicked)
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
        controlPanelInteractor.onEvent(ControlPanelMachine.Event.OnEnterScrollAndMoveModeClicked)
    }

    fun onExitScrollAndMoveClicked() {
        mode = EditorMode.MULTI_SELECT
        controlPanelInteractor.onEvent(ControlPanelMachine.Event.OnExitScrollAndMoveModeClicked)
    }

    fun onMultiSelectModeDeleteClicked() {
        controlPanelInteractor.onEvent(ControlPanelMachine.Event.OnMultiSelectDeleteClicked)
        val selected = currentSelection().toList()
        clearSelections()
        viewModelScope.launch {
            orchestrator.proxies.intents.send(
                Intent.CRUD.Unlink(
                    context = context,
                    targets = selected,
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
        dispatch(Command.OpenMultiSelectTurnIntoPanel)
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
        }
        dispatch(Command.PopBackStack)
    }

    override fun onTurnIntoMultiSelectBlockClicked(block: UiBlock) {
        if (block.isText() || block.isCode()) {
            val targets = currentSelection().toList()
            clearSelections()
            proceedWithUpdatingTextStyle(
                style = block.style(),
                targets = targets
            )
        }
    }

    fun onTurnIntoStyleClicked(style: Content.Text.Style) {
        proceedWithUpdatingTextStyle(style, listOf(orchestrator.stores.focus.current().id))
    }

    fun onAddDividerBlockClicked() {
        controlPanelInteractor.onEvent(ControlPanelMachine.Event.OnAddBlockToolbarOptionSelected)
        viewModelScope.launch {
            orchestrator.proxies.intents.send(
                Intent.CRUD.Create(
                    context = context,
                    target = orchestrator.stores.focus.current().id,
                    position = Position.BOTTOM,
                    prototype = Prototype.Divider
                )
            )
        }
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
        proceedWithClearingFocus()
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
        renderings.send(blocks)
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

        val params = CreateDocument.Params(
            context = context,
            position = Position.BOTTOM,
            target = orchestrator.stores.focus.current().id,
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
                    target = context
                )
            ).proceed(
                failure = { Timber.e(it, "Error while archiving page") },
                success = { proceedWithExiting() }
            )
        }
    }

    fun onAddBookmarkBlockClicked() {
        controlPanelInteractor.onEvent(ControlPanelMachine.Event.OnAddBlockToolbarOptionSelected)
        viewModelScope.launch {
            orchestrator.proxies.intents.send(
                Intent.CRUD.Create(
                    context = context,
                    position = Position.BOTTOM,
                    target = orchestrator.stores.focus.current().id,
                    prototype = Prototype.Bookmark
                )
            )
        }
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

    private fun onBookmarkClicked(view: BlockView.Bookmark.View) =
        dispatch(command = Command.Browse(view.url))

    private fun onFailedBookmarkClicked(view: BlockView.Bookmark.Error) =
        dispatch(command = Command.Browse(view.url))

    fun onTitleTextInputClicked() {
        controlPanelInteractor.onEvent(ControlPanelMachine.Event.OnTextInputClicked)
    }

    fun onTextInputClicked(target: Id) {
        if (mode == EditorMode.MULTI_SELECT) {
            onBlockMultiSelectClicked(target)
        } else {
            controlPanelInteractor.onEvent(ControlPanelMachine.Event.OnTextInputClicked)
        }
    }

    private fun onBlockMultiSelectClicked(target: Id) {
        toggleSelection(target)
        onMultiSelectModeBlockClicked()
        (stateData.value as ViewState.Success).let { state ->
            val update = state.blocks.map { block ->
                if (block.id == target)
                    block.updateSelection(newSelection = isSelected(target))
                else
                    block
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
        val block = blocks.first { it.id == target }

        // TODO refact range check

        val position = when (ratio) {
            in 0.0..0.2 -> Position.TOP
            in 0.2..0.8 -> Position.INNER
            in 0.8..1.0 -> Position.BOTTOM
            else -> throw IllegalStateException("Unexpected ratio: $ratio")
        }

        val targetContext = if (block.content is Content.Link && position == Position.INNER) {
            block.content<Content.Link>().target
        } else {
            context
        }

        val selected = currentSelection().toList()

        clearSelections()

        controlPanelInteractor.onEvent(ControlPanelMachine.Event.OnApplyScrollAndMoveClicked)

        mode = EditorMode.MULTI_SELECT

        viewModelScope.launch {
            orchestrator.proxies.intents.send(
                Intent.Document.Move(
                    context = context,
                    target = target,
                    targetContext = targetContext,
                    blocks = selected,
                    position = position
                )
            )
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

    fun onClickListener(clicked: ListenerType) =
        when (clicked) {
            is ListenerType.Bookmark.View -> {
                when (mode) {
                    EditorMode.EDITING -> onBookmarkClicked(clicked.item)
                    EditorMode.MULTI_SELECT -> onBlockMultiSelectClicked(clicked.item.id)
                    EditorMode.SCROLL_AND_MOVE -> {
                    }
                }
            }
            is ListenerType.Bookmark.Placeholder -> {
                when (mode) {
                    EditorMode.EDITING -> onBookmarkPlaceholderClicked(clicked.target)
                    EditorMode.MULTI_SELECT -> onBlockMultiSelectClicked(clicked.target)
                    EditorMode.SCROLL_AND_MOVE -> {
                    }
                }
            }
            is ListenerType.Bookmark.Error -> {
                when (mode) {
                    EditorMode.EDITING -> onFailedBookmarkClicked(clicked.item)
                    EditorMode.MULTI_SELECT -> onBlockMultiSelectClicked(clicked.item.id)
                    EditorMode.SCROLL_AND_MOVE -> {
                    }
                }
            }
            is ListenerType.File.View -> {
                when (mode) {
                    EditorMode.EDITING -> onFileClicked(clicked.target)
                    EditorMode.MULTI_SELECT -> onBlockMultiSelectClicked(clicked.target)
                    EditorMode.SCROLL_AND_MOVE -> {
                    }
                }
            }
            is ListenerType.File.Placeholder -> {
                when (mode) {
                    EditorMode.EDITING -> onAddLocalFileClicked(clicked.target)
                    EditorMode.MULTI_SELECT -> onBlockMultiSelectClicked(clicked.target)
                    EditorMode.SCROLL_AND_MOVE -> {
                    }
                }
            }
            is ListenerType.File.Error -> {
                when (mode) {
                    EditorMode.EDITING -> onAddLocalFileClicked(clicked.target)
                    EditorMode.MULTI_SELECT -> onBlockMultiSelectClicked(clicked.target)
                    EditorMode.SCROLL_AND_MOVE -> {
                    }
                }
            }
            is ListenerType.File.Upload -> {
                when (mode) {
                    EditorMode.EDITING -> Unit
                    EditorMode.MULTI_SELECT -> onBlockMultiSelectClicked(clicked.target)
                    EditorMode.SCROLL_AND_MOVE -> {
                    }
                }
            }
            is ListenerType.Picture.View -> {
                when (mode) {
                    EditorMode.EDITING -> Unit
                    EditorMode.MULTI_SELECT -> onBlockMultiSelectClicked(clicked.target)
                    EditorMode.SCROLL_AND_MOVE -> {
                    }
                }
            }
            is ListenerType.Picture.Placeholder -> {
                when (mode) {
                    EditorMode.EDITING -> onAddLocalPictureClicked(clicked.target)
                    EditorMode.MULTI_SELECT -> onBlockMultiSelectClicked(clicked.target)
                    EditorMode.SCROLL_AND_MOVE -> {
                    }
                }
            }
            is ListenerType.Picture.Error -> {
                when (mode) {
                    EditorMode.EDITING -> Unit
                    EditorMode.MULTI_SELECT -> onBlockMultiSelectClicked(clicked.target)
                    EditorMode.SCROLL_AND_MOVE -> {
                    }
                }
            }
            is ListenerType.Picture.Upload -> {
                when (mode) {
                    EditorMode.EDITING -> Unit
                    EditorMode.MULTI_SELECT -> onBlockMultiSelectClicked(clicked.target)
                    EditorMode.SCROLL_AND_MOVE -> {
                    }
                }
            }
            is ListenerType.Video.View -> {
                when (mode) {
                    EditorMode.EDITING -> Unit
                    EditorMode.MULTI_SELECT -> onBlockMultiSelectClicked(clicked.target)
                    EditorMode.SCROLL_AND_MOVE -> {
                    }
                }
            }
            is ListenerType.Video.Placeholder -> {
                when (mode) {
                    EditorMode.EDITING -> onAddLocalVideoClicked(clicked.target)
                    EditorMode.MULTI_SELECT -> onBlockMultiSelectClicked(clicked.target)
                    EditorMode.SCROLL_AND_MOVE -> {
                    }
                }
            }
            is ListenerType.Video.Error -> {
                when (mode) {
                    EditorMode.EDITING -> Unit
                    EditorMode.MULTI_SELECT -> onBlockMultiSelectClicked(clicked.target)
                    EditorMode.SCROLL_AND_MOVE -> {
                    }
                }
            }
            is ListenerType.Video.Upload -> {
                when (mode) {
                    EditorMode.EDITING -> Unit
                    EditorMode.MULTI_SELECT -> onBlockMultiSelectClicked(clicked.target)
                    EditorMode.SCROLL_AND_MOVE -> {
                    }
                }
            }
            is ListenerType.LongClick -> {
                when (mode) {
                    EditorMode.EDITING -> onBlockLongPressedClicked(clicked.target, clicked.dimensions)
                    EditorMode.MULTI_SELECT -> Unit
                    EditorMode.SCROLL_AND_MOVE -> {
                    }
                }
            }
            is ListenerType.Page -> {
                when (mode) {
                    EditorMode.EDITING -> onPageClicked(clicked.target)
                    EditorMode.MULTI_SELECT -> onBlockMultiSelectClicked(clicked.target)
                    EditorMode.SCROLL_AND_MOVE -> Unit
                }
            }
            is ListenerType.Mention -> {
                when (mode) {
                    EditorMode.EDITING -> onMentionClicked(clicked.target)
                    EditorMode.MULTI_SELECT -> Unit
                    EditorMode.SCROLL_AND_MOVE -> Unit
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
        val block = blocks.first { it.id == id }
        val file = block.content<Content.File>()

        viewModelScope.launch {
            orchestrator.proxies.intents.send(
                Intent.Media.DownloadFile(
                    url = urlBuilder.file(file.hash),
                    name = file.name.orEmpty()
                )
            )
        }
    }

    fun onPageSearchClicked() {
        navigation.postValue(EventWrapper(AppNavigation.Command.OpenPageSearch))
    }

    private fun onMultiSelectModeBlockClicked() {
        controlPanelInteractor.onEvent(
            ControlPanelMachine.Event.OnMultiSelectModeBlockClick(
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
        const val EMPTY_FOCUS_ID = ""
        const val TEXT_CHANGES_DEBOUNCE_DURATION = 500L
        const val INITIAL_INDENT = 0
    }

    data class MarkupAction(
        val type: Markup.Type,
        val param: String? = null
    )

    override fun onCleared() {
        super.onCleared()

        orchestrator.stores.focus.cancel()
        orchestrator.stores.details.cancel()
        orchestrator.proxies.changes.cancel()
        orchestrator.proxies.saves.cancel()

        selections.cancel()
        markups.cancel()
        renderings.cancel()

        controlPanelInteractor.channel.cancel()
        titleChannel.cancel()
    }
}