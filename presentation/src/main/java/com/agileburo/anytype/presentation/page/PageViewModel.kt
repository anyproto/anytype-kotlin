package com.agileburo.anytype.presentation.page

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.agileburo.anytype.core_ui.common.Markup
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
import com.agileburo.anytype.domain.block.interactor.UploadUrl
import com.agileburo.anytype.domain.block.model.Block
import com.agileburo.anytype.domain.block.model.Block.Content
import com.agileburo.anytype.domain.block.model.Block.Prototype
import com.agileburo.anytype.domain.block.model.Position
import com.agileburo.anytype.domain.common.Id
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

const val EMPTY_PATH = ""

class PageViewModel(
    private val openPage: OpenPage,
    private val closePage: ClosePage,
    private val createPage: CreatePage,
    private val createDocument: CreateDocument,
    private val archiveDocument: ArchiveDocument,
    private val interceptEvents: InterceptEvents,
    private val updateLinkMarks: UpdateLinkMarks,
    private val removeLinkMark: RemoveLinkMark,
    private val uploadUrl: UploadUrl,
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
            orchestrator.stores.focus.stream().collect { _focus.postValue(it) }
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
                    stateData.value = ViewState.Error(it.message ?: "Unknown error")
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
        viewModelScope.launch {
            renderings
                .stream()
                .filter { it.isNotEmpty() }
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
                .collect { dispatchToUI(it) }
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
            .onEach { update -> orchestrator.textInteractor.consume(update, context) }
            .launchIn(viewModelScope)

        orchestrator
            .proxies
            .saves
            .stream()
            .debounce(TEXT_CHANGES_DEBOUNCE_DURATION)
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

    fun onEmptyBlockBackspaceClicked(id: String) {
        Timber.d("onEmptyBlockBackspaceClicked: $id")
        proceedWithUnlinking(target = id)
    }

    fun onNonEmptyBlockBackspaceClicked(id: String) {
        val page = blocks.first { it.id == context }
        val index = page.children.indexOf(id)
        if (index > 0) {
            val previous = page.children[index.dec()]
            proceedWithMergingBlocks(
                previous = previous,
                id = id
            )
        } else {
            Timber.d("Skipping merge on non-empty-block-backspace-pressed event")
        }
    }

    private fun proceedWithMergingBlocks(id: String, previous: String) {
        viewModelScope.launch {
            orchestrator.proxies.intents.send(
                Intent.Text.Merge(
                    context = context,
                    previous = previous,
                    pair = Pair(previous, id)
                )
            )
        }
    }

    fun onSplitLineEnterClicked(
        target: String,
        index: Int
    ) {
        viewModelScope.launch {
            orchestrator.proxies.intents.send(
                Intent.Text.Split(
                    context = context,
                    target = target,
                    index = index
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
        viewModelScope.launch { orchestrator.stores.focus.update(id) }
    }

    fun onBlockLongPressedClicked(target: String) {
        val state = stateData.value
        if (state is ViewState.Success) {
            dispatch(Command.OpenActionBar(block = state.blocks.first { it.id == target }))
        }
    }

    fun onMarkupActionClicked(markup: Markup.Type) {
        when (markup) {
            Markup.Type.BACKGROUND_COLOR -> {
                controlPanelInteractor.onEvent(
                    ControlPanelMachine.Event.OnMarkupContextMenuBackgroundColorClicked
                )
            }
            Markup.Type.TEXT_COLOR -> {
                controlPanelInteractor.onEvent(
                    ControlPanelMachine.Event.OnMarkupContextMenuTextColorClicked
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
        viewModelScope.launch {
            markups.send(
                MarkupAction(
                    type = Markup.Type.TEXT_COLOR,
                    param = color
                )
            )
        }
    }

    fun onMarkupBackgroundColorAction(color: String) {
        viewModelScope.launch {
            markups.send(
                MarkupAction(
                    type = Markup.Type.BACKGROUND_COLOR,
                    param = color
                )
            )
        }
    }

    fun onBlockAlignmentActionClicked(alignment: BlockView.Alignment) {
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
        alignment: BlockView.Alignment
    ) {
        viewModelScope.launch {
            orchestrator.proxies.intents.send(
                Intent.Text.Align(
                    context = context,
                    target = blockView.id,
                    alignment = when (alignment) {
                        BlockView.Alignment.START -> Block.Align.AlignLeft
                        BlockView.Alignment.CENTER -> Block.Align.AlignCenter
                        BlockView.Alignment.END -> Block.Align.AlignRight
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
                    target = orchestrator.stores.focus.current(),
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
                    targets = listOf(orchestrator.stores.focus.current()),
                    color = color
                )
            )
        }
    }

    fun onBlockStyleMarkupActionClicked(action: Markup.Type) {
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

    fun onActionDeleteClicked() {
        proceedWithUnlinking(orchestrator.stores.focus.current())
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
                stateData.value = ViewState.Error("Rename not implemented")
            }
            ActionItemType.MoveTo -> {
                stateData.value = ViewState.Error("Move To not implemented")
            }
            ActionItemType.Color -> {
                controlPanelInteractor.onEvent(
                    ControlPanelMachine.Event.OnBlockActionToolbarTextColorClicked
                )
                dispatch(Command.PopBackStack)
            }
            ActionItemType.Background -> {
                controlPanelInteractor.onEvent(
                    ControlPanelMachine.Event.OnBlockActionToolbarBackgroundColorClicked
                )
                dispatch(Command.PopBackStack)
            }
            ActionItemType.Style -> {
                controlPanelInteractor.onEvent(
                    ControlPanelMachine.Event.OnBlockActionToolbarStyleClicked
                )
                dispatch(Command.PopBackStack)
            }
            ActionItemType.Download -> {
                stateData.value = ViewState.Error("Download not implemented")
            }
            ActionItemType.Replace -> {
                stateData.value = ViewState.Error("Replace not implemented")
            }
            ActionItemType.AddCaption -> {
                stateData.value = ViewState.Error("Add caption not implemented")
            }
            ActionItemType.Divider -> {
                stateData.value = ViewState.Error("not implemented")
            }
        }
    }

    private fun proceedWithUnlinking(target: String) {

        // TODO support nested blocks

        val parent = blocks.first { it.children.contains(target) }

        val index = parent.children.indexOf(target)

        val previous = index.dec().let { prev ->
            if (prev != -1) parent.children[prev] else null
        }

        val next = index.inc().let { nxt ->
            if (nxt <= parent.children.lastIndex) parent.children[nxt] else null
        }

        viewModelScope.launch {
            orchestrator.proxies.intents.send(
                Intent.CRUD.Unlink(
                    context = context,
                    targets = listOf(target),
                    previous = previous,
                    next = next
                )
            )
        }
    }

    fun onActionDuplicateClicked() {
        duplicateBlock(target = orchestrator.stores.focus.current())
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
            id = orchestrator.stores.focus.current(),
            style = style
        )
    }

    fun onAddVideoBlockClicked() {
        proceedWithCreatingEmptyFileBlock(
            id = orchestrator.stores.focus.current(),
            type = Content.File.Type.VIDEO
        )
    }

    fun onAddLocalVideoClicked(blockId: String) {
        mediaBlockId = blockId
        dispatch(Command.OpenGallery(mediaType = MIME_VIDEO_ALL))
    }

    fun onAddLocalPictureClicked(blockId: String) {
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

    fun onAddLocalFileClicked(blockId: String) {
        mediaBlockId = blockId
        dispatch(Command.OpenGallery(mediaType = MIME_FILE_ALL))
    }

    fun onAddImageBlockClicked() {
        proceedWithCreatingEmptyFileBlock(
            id = orchestrator.stores.focus.current(),
            type = Content.File.Type.IMAGE
        )
    }

    fun onAddFileBlockClicked() {
        proceedWithCreatingEmptyFileBlock(
            id = orchestrator.stores.focus.current(),
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
            orchestrator.stores.focus.update(EMPTY_FOCUS_ID)
            refresh()
        }
    }

    fun onMultiSelectModeDeleteClicked() {
        viewModelScope.launch {
            orchestrator.proxies.intents.send(
                Intent.CRUD.Unlink(
                    context = context,
                    targets = currentSelection().toList(),
                    next = null,
                    previous = null,
                    effects = listOf(SideEffect.ClearMultiSelectSelection)
                )
            )
        }
    }

    fun onMultiSelectModeSelectAllClicked() {
        (stateData.value as ViewState.Success).let { state ->
            val update = state.blocks.map { block ->
                select(block.id)
                when (block) {
                    is BlockView.Paragraph -> block.copy(isSelected = true)
                    is BlockView.HeaderOne -> block.copy(isSelected = true)
                    is BlockView.HeaderTwo -> block.copy(isSelected = true)
                    is BlockView.HeaderThree -> block.copy(isSelected = true)
                    is BlockView.Highlight -> block.copy(isSelected = true)
                    is BlockView.Checkbox -> block.copy(isSelected = true)
                    is BlockView.Bulleted -> block.copy(isSelected = true)
                    is BlockView.Numbered -> block.copy(isSelected = true)
                    is BlockView.Toggle -> block.copy(isSelected = true)
                    is BlockView.Bookmark.View -> block.copy(isSelected = true)
                    is BlockView.Bookmark.Placeholder -> block.copy(isSelected = true)
                    is BlockView.Bookmark.Error -> block.copy(isSelected = true)
                    else -> block
                }
            }
            stateData.postValue(ViewState.Success(update))
        }
    }

    fun onMultiSelectTurnIntoButtonClicked() {
        dispatch(Command.OpenMultiSelectTurnIntoPanel)
    }

    override fun onTurnIntoBlockClicked(target: String, block: UiBlock) {
        if (block.isText()) {
            proceedWithUpdatingTextStyle(
                style = block.style(),
                targets = listOf(target)
            )
        }
        dispatch(Command.PopBackStack)
    }

    override fun onTurnIntoMultiSelectBlockClicked(block: UiBlock) {
        if (block.isText()) {
            val targets = currentSelection().toList()
            clearSelections()
            proceedWithUpdatingTextStyle(
                style = block.style(),
                targets = targets
            )
        }
    }

    fun onTurnIntoStyleClicked(style: Content.Text.Style) {
        proceedWithUpdatingTextStyle(style, listOf(orchestrator.stores.focus.current()))
    }

    fun onAddDividerBlockClicked() {
        controlPanelInteractor.onEvent(ControlPanelMachine.Event.OnAddBlockToolbarOptionSelected)
        viewModelScope.launch {
            orchestrator.proxies.intents.send(
                Intent.CRUD.Create(
                    context = context,
                    target = orchestrator.stores.focus.current(),
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
            orchestrator.stores.focus.update(EMPTY_FOCUS_ID)
            refresh()
        }
        controlPanelInteractor.onEvent(ControlPanelMachine.Event.OnClearFocusClicked)
    }

    private suspend fun refresh() {
        Timber.d("Refreshing: $blocks")
        renderings.send(blocks)
    }

    fun onPageClicked(id: String) {
        if (mode == EditorMode.MULTI_SELECT) {
            toggleSelection(id)
            (stateData.value as ViewState.Success).let { state ->
                val update = state.blocks.map { block ->
                    if (block.id == id && block is BlockView.Page)
                        block.copy(isSelected = isSelected(id))
                    else
                        block
                }
                stateData.postValue(ViewState.Success(update))
            }
        } else {
            proceedWithOpeningPage(
                target = blocks.first { it.id == id }.content<Content.Link>().target
            )
        }
    }

    fun onAddNewPageClicked() {
        controlPanelInteractor.onEvent(ControlPanelMachine.Event.OnAddBlockToolbarOptionSelected)

        val params = CreateDocument.Params(
            context = context,
            position = Position.BOTTOM,
            target = orchestrator.stores.focus.current(),
            prototype = Prototype.Page(style = Content.Page.Style.EMPTY)
        )

        createDocument(scope = viewModelScope, params = params) { result ->
            result.either(
                fnL = { Timber.e(it, "Error while creating new page with params: $params") },
                fnR = { (_, target) -> proceedWithOpeningPage(target) }
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
                    target = orchestrator.stores.focus.current(),
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

    fun onBookmarkPlaceholderClicked(target: String) {
        dispatch(
            command = Command.OpenBookmarkSetter(
                context = context,
                target = target
            )
        )
    }

    fun onBookmarkClicked(view: BlockView.Bookmark.View) {
        if (mode == EditorMode.MULTI_SELECT) {
            toggleSelection(view.id)
            (stateData.value as ViewState.Success).let { state ->
                val update = state.blocks.map { block ->
                    if (block.id == view.id && block is BlockView.Bookmark.View)
                        block.copy(isSelected = isSelected(view.id))
                    else
                        block
                }
                stateData.postValue(ViewState.Success(update))
            }
        } else {
            dispatch(
                command = Command.Browse(
                    url = view.url
                )
            )
        }
    }

    fun onFailedBookmarkClicked(view: BlockView.Bookmark.Error) {
        dispatch(
            command = Command.Browse(
                url = view.url
            )
        )
    }

    fun onTitleTextInputClicked() {
        controlPanelInteractor.onEvent(ControlPanelMachine.Event.OnTextInputClicked)
    }

    fun onTextInputClicked(target: Id) {
        if (mode == EditorMode.MULTI_SELECT) {
            toggleSelection(target)
            (stateData.value as ViewState.Success).let { state ->
                val update = state.blocks.map { block ->
                    if (block.id == target)
                        when (block) {
                            is BlockView.Paragraph -> block.copy(isSelected = isSelected(target))
                            is BlockView.HeaderOne -> block.copy(isSelected = isSelected(target))
                            is BlockView.HeaderTwo -> block.copy(isSelected = isSelected(target))
                            is BlockView.HeaderThree -> block.copy(isSelected = isSelected(target))
                            is BlockView.Highlight -> block.copy(isSelected = isSelected(target))
                            is BlockView.Checkbox -> block.copy(isSelected = isSelected(target))
                            is BlockView.Bulleted -> block.copy(isSelected = isSelected(target))
                            is BlockView.Numbered -> block.copy(isSelected = isSelected(target))
                            is BlockView.Toggle -> block.copy(isSelected = isSelected(target))
                            is BlockView.Bookmark.View -> block.copy(isSelected = isSelected(target))
                            is BlockView.Bookmark.Placeholder -> block.copy(isSelected = isSelected(target))
                            is BlockView.Bookmark.Error -> block.copy(isSelected = isSelected(target))
                            else -> block
                        }
                    else
                        block
                }
                stateData.postValue(ViewState.Success(update))
            }
        } else {
            controlPanelInteractor.onEvent(ControlPanelMachine.Event.OnTextInputClicked)
        }
    }

    fun onClickListener(clicked: ListenerType) =
        when (clicked) {
            is ListenerType.Bookmark.View -> {
                when (mode) {
                    EditorMode.EDITING -> onBookmarkClicked(clicked.item)
                    EditorMode.MULTI_SELECT -> onTextInputClicked(clicked.item.id)
                }
            }
            is ListenerType.Bookmark.ViewLong -> {
                onBlockLongPressedClicked(clicked.target)
            }
            is ListenerType.Bookmark.Placeholder -> {
                when (mode) {
                    EditorMode.EDITING -> onBookmarkPlaceholderClicked(clicked.target)
                    EditorMode.MULTI_SELECT -> onTextInputClicked(clicked.target)
                }
            }
            is ListenerType.Bookmark.PlaceholderLong -> {
                onBlockLongPressedClicked(clicked.target)
            }
            is ListenerType.Bookmark.Error -> {
                when (mode) {
                    EditorMode.EDITING -> onFailedBookmarkClicked(clicked.item)
                    EditorMode.MULTI_SELECT -> onTextInputClicked(clicked.item.id)
                }
            }
            is ListenerType.Bookmark.ErrorLong -> {
                onBlockLongPressedClicked(clicked.target)
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

    fun onAddVideoUrlClicked(blockId: String, url: String) {
        viewModelScope.launch {
            uploadUrl(
                params = UploadUrl.Params(
                    contextId = context,
                    blockId = blockId,
                    url = url,
                    filePath = EMPTY_PATH
                )
            ).proceed(
                failure = { Timber.e(it, "Error while upload new url for video block") },
                success = { Timber.d("Upload Url Success") }
            )
        }
    }

    fun onAddVideoFileClicked(filePath: String?) {
        if (filePath == null) {
            Timber.d("Error while getting filePath")
            return
        }
        uploadUrl(
            scope = viewModelScope,
            params = UploadUrl.Params(
                contextId = context,
                blockId = mediaBlockId,
                url = "",
                filePath = filePath
            )
        ) { result ->
            result.either(
                fnL = { Timber.e(it, "Error while upload new file path for video block") },
                fnR = { Timber.d("Upload File Path Success") }
            )
        }
    }

    fun onChooseVideoFileFromMedia() {
        try {
            val targetBlock = blocks.first { it.id == mediaBlockId }
            val targetContent = targetBlock.content as Content.File
            val newContent = targetContent.copy(state = Content.File.State.UPLOADING)
            val newBlock = targetBlock.copy(content = newContent)
            rerenderingBlocks(newBlock)
        } catch (e: Exception) {
            Timber.e(e, "Error while update block:$mediaBlockId state to Uploading")
            stateData.value = ViewState.Error("Can't load video for this block")
        }
    }

    fun onPageIconClicked() {
        dispatch(Command.OpenPagePicker(context))
    }

    fun onFileClicked(id: String) {
        if (mode == EditorMode.MULTI_SELECT) {
            toggleSelection(id)
            (stateData.value as ViewState.Success).let { state ->
                val update = state.blocks.map { block ->
                    if (block.id == id && block is BlockView.File.View)
                        block.copy(isSelected = isSelected(id))
                    else
                        block
                }
                stateData.postValue(ViewState.Success(update))
            }
        } else {
            dispatch(Command.RequestDownloadPermission(id))
        }
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

    fun onMediaBlockMenuClicked(id: String) {
        updateFocus(id)
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