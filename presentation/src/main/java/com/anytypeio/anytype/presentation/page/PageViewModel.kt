package com.anytypeio.anytype.presentation.page

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.analytics.base.EventsDictionary.OBJECT_CREATE
import com.anytypeio.anytype.analytics.base.EventsDictionary.PAGE_CREATE
import com.anytypeio.anytype.analytics.base.EventsDictionary.PAGE_MENTION_CREATE
import com.anytypeio.anytype.analytics.base.EventsDictionary.POPUP_ACTION_MENU
import com.anytypeio.anytype.analytics.base.EventsDictionary.POPUP_ADD_BLOCK
import com.anytypeio.anytype.analytics.base.EventsDictionary.POPUP_BOOKMARK
import com.anytypeio.anytype.analytics.base.EventsDictionary.POPUP_DOCUMENT_ICON_MENU
import com.anytypeio.anytype.analytics.base.EventsDictionary.POPUP_DOCUMENT_MENU
import com.anytypeio.anytype.analytics.base.EventsDictionary.POPUP_MARKUP_LINK
import com.anytypeio.anytype.analytics.base.EventsDictionary.POPUP_MENTION_MENU
import com.anytypeio.anytype.analytics.base.EventsDictionary.POPUP_MULTI_SELECT_MENU
import com.anytypeio.anytype.analytics.base.EventsDictionary.POPUP_PROFILE_ICON_MENU
import com.anytypeio.anytype.analytics.base.EventsDictionary.POPUP_PROFILE_MENU
import com.anytypeio.anytype.analytics.base.EventsDictionary.POPUP_STYLE
import com.anytypeio.anytype.analytics.base.EventsDictionary.POPUP_TURN_INTO
import com.anytypeio.anytype.analytics.base.EventsDictionary.PROP_STYLE
import com.anytypeio.anytype.analytics.base.sendEvent
import com.anytypeio.anytype.analytics.event.EventAnalytics
import com.anytypeio.anytype.analytics.props.Props
import com.anytypeio.anytype.core_models.*
import com.anytypeio.anytype.core_models.Block.Content
import com.anytypeio.anytype.core_models.Block.Prototype
import com.anytypeio.anytype.core_models.ext.*
import com.anytypeio.anytype.core_models.restrictions.ObjectRestriction
import com.anytypeio.anytype.core_utils.common.EventWrapper
import com.anytypeio.anytype.core_utils.ext.*
import com.anytypeio.anytype.core_utils.ui.ViewStateViewModel
import com.anytypeio.anytype.domain.`object`.UpdateDetail
import com.anytypeio.anytype.domain.base.Result
import com.anytypeio.anytype.domain.block.interactor.RemoveLinkMark
import com.anytypeio.anytype.domain.block.interactor.UpdateLinkMarks
import com.anytypeio.anytype.domain.block.interactor.UpdateText
import com.anytypeio.anytype.domain.cover.RemoveDocCover
import com.anytypeio.anytype.domain.cover.SetDocCoverImage
import com.anytypeio.anytype.domain.dataview.interactor.GetCompatibleObjectTypes
import com.anytypeio.anytype.domain.editor.Editor
import com.anytypeio.anytype.domain.error.Error
import com.anytypeio.anytype.domain.event.interactor.InterceptEvents
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.page.*
import com.anytypeio.anytype.domain.page.navigation.GetListPages
import com.anytypeio.anytype.domain.status.InterceptThreadStatus
import com.anytypeio.anytype.domain.status.SyncStatus
import com.anytypeio.anytype.presentation.BuildConfig
import com.anytypeio.anytype.presentation.common.StateReducer
import com.anytypeio.anytype.presentation.common.SupportCommand
import com.anytypeio.anytype.presentation.mapper.mark
import com.anytypeio.anytype.presentation.mapper.style
import com.anytypeio.anytype.presentation.mapper.toMentionView
import com.anytypeio.anytype.presentation.navigation.AppNavigation
import com.anytypeio.anytype.presentation.navigation.SupportNavigation
import com.anytypeio.anytype.presentation.page.ControlPanelMachine.Interactor
import com.anytypeio.anytype.presentation.page.Editor.Restore
import com.anytypeio.anytype.presentation.page.TurnIntoConstants.excludeCategoriesForDivider
import com.anytypeio.anytype.presentation.page.TurnIntoConstants.excludeTypesForDotsDivider
import com.anytypeio.anytype.presentation.page.TurnIntoConstants.excludeTypesForLineDivider
import com.anytypeio.anytype.presentation.page.TurnIntoConstants.excludeTypesForText
import com.anytypeio.anytype.presentation.page.TurnIntoConstants.excludedCategoriesForText
import com.anytypeio.anytype.presentation.page.editor.*
import com.anytypeio.anytype.presentation.page.editor.Command
import com.anytypeio.anytype.presentation.page.editor.actions.ActionItemType
import com.anytypeio.anytype.presentation.page.editor.control.ControlPanelState
import com.anytypeio.anytype.presentation.page.editor.ext.*
import com.anytypeio.anytype.presentation.page.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.page.editor.mention.Mention
import com.anytypeio.anytype.presentation.page.editor.mention.MentionEvent
import com.anytypeio.anytype.presentation.page.editor.mention.getMentionName
import com.anytypeio.anytype.presentation.page.editor.model.Alignment
import com.anytypeio.anytype.presentation.page.editor.model.BlockView
import com.anytypeio.anytype.presentation.page.editor.model.Focusable
import com.anytypeio.anytype.presentation.page.editor.model.UiBlock
import com.anytypeio.anytype.presentation.page.editor.sam.ScrollAndMoveTargetDescriptor.Companion.END_RANGE
import com.anytypeio.anytype.presentation.page.editor.sam.ScrollAndMoveTargetDescriptor.Companion.INNER_RANGE
import com.anytypeio.anytype.presentation.page.editor.sam.ScrollAndMoveTargetDescriptor.Companion.START_RANGE
import com.anytypeio.anytype.presentation.page.editor.search.SearchInDocEvent
import com.anytypeio.anytype.presentation.page.editor.slash.*
import com.anytypeio.anytype.presentation.page.editor.slash.SlashExtensions.SLASH_CHAR
import com.anytypeio.anytype.presentation.page.editor.slash.SlashExtensions.SLASH_EMPTY_SEARCH_MAX
import com.anytypeio.anytype.presentation.page.editor.slash.SlashExtensions.getSlashWidgetAlignmentItems
import com.anytypeio.anytype.presentation.page.editor.slash.SlashExtensions.getSlashWidgetStyleItems
import com.anytypeio.anytype.presentation.page.editor.styling.StylingEvent
import com.anytypeio.anytype.presentation.page.editor.styling.StylingMode
import com.anytypeio.anytype.presentation.page.model.TextUpdate
import com.anytypeio.anytype.presentation.page.render.BlockViewRenderer
import com.anytypeio.anytype.presentation.page.render.DefaultBlockViewRenderer
import com.anytypeio.anytype.presentation.page.search.search
import com.anytypeio.anytype.presentation.page.selection.SelectionStateHolder
import com.anytypeio.anytype.presentation.page.toggle.ToggleStateHolder
import com.anytypeio.anytype.presentation.relations.DocumentRelationView
import com.anytypeio.anytype.presentation.relations.RelationListViewModel
import com.anytypeio.anytype.presentation.relations.views
import com.anytypeio.anytype.presentation.util.Dispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*
import java.util.regex.Pattern
import com.anytypeio.anytype.presentation.page.Editor.Mode as EditorMode

class PageViewModel(
    private val openPage: OpenPage,
    private val closePage: CloseBlock,
    private val createPage: CreatePage,
    private val createDocument: CreateDocument,
    private val createObject: CreateObject,
    private val createNewDocument: CreateNewDocument,
    private val archiveDocument: ArchiveDocument,
    private val interceptEvents: InterceptEvents,
    private val interceptThreadStatus: InterceptThreadStatus,
    private val updateLinkMarks: UpdateLinkMarks,
    private val removeLinkMark: RemoveLinkMark,
    private val reducer: StateReducer<List<Block>, Event>,
    private val setDocCoverImage: SetDocCoverImage,
    private val removeDocCover: RemoveDocCover,
    private val urlBuilder: UrlBuilder,
    private val renderer: DefaultBlockViewRenderer,
    private val orchestrator: Orchestrator,
    private val getListPages: GetListPages,
    private val analytics: Analytics,
    private val dispatcher: Dispatcher<Payload>,
    private val detailModificationManager: DetailModificationManager,
    private val updateDetail: UpdateDetail,
    private val getCompatibleObjectTypes: GetCompatibleObjectTypes
) : ViewStateViewModel<ViewState>(),
    SupportNavigation<EventWrapper<AppNavigation.Command>>,
    SupportCommand<Command>,
    BlockViewRenderer by renderer,
    ToggleStateHolder by renderer,
    SelectionStateHolder by orchestrator.memory.selections,
    StateReducer<List<Block>, Event> by reducer {

    val syncStatus = MutableStateFlow<SyncStatus?>(null)

    val isUndoEnabled = MutableStateFlow(false)
    val isRedoEnabled = MutableStateFlow(false)

    val searchResultScrollPosition = MutableStateFlow(NO_SEARCH_RESULT_POSITION)

    private val session = MutableStateFlow(Session.IDLE)

    private val views: List<BlockView> get() = orchestrator.stores.views.current()

    val pending: Queue<Restore> = LinkedList()
    val restore: Queue<Restore> = LinkedList()

    private val jobs = mutableListOf<Job>()

    private var mode: EditorMode = EditorMode.Edit

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

    private val _toasts: Channel<String> = Channel()
    val toasts: Flow<String> get() = _toasts.consumeAsFlow()

    /**
     * Open gallery and search media files for block with that id
     */
    private var mediaBlockId = ""

    /**
     * Current position of last mentionFilter or -1 if none
     */
    private var mentionFrom = -1

    /**
     * Currently pending text update. If null, it is not present or already dispatched.
     */
    private var pendingTextUpdate: TextUpdate? = null

    override val navigation = MutableLiveData<EventWrapper<AppNavigation.Command>>()
    override val commands = MutableLiveData<EventWrapper<Command>>()

    init {
        startHandlingTextChanges()
        startProcessingFocusChanges()
        startProcessingTitleChanges()
        startProcessingControlPanelViewState()
        startProcessingInternalDetailModifications()
        startObservingPayload()
        startObservingErrors()
        processRendering()
        processMarkupChanges()
        viewModelScope.launch { orchestrator.start() }
    }

    private fun startProcessingInternalDetailModifications() {
        detailModificationManager.modifications.onEach { refresh() }.launchIn(viewModelScope)
    }

    private fun startProcessingFocusChanges() {
        viewModelScope.launch {
            orchestrator.stores.focus.stream().collect { focus ->
                if (focus.isEmpty) {
                    orchestrator.stores.textSelection.update(Editor.TextSelection.empty())
                } else {
                    if (!focus.isPending) {
                        try {
                            controlPanelInteractor.onEvent(
                                ControlPanelMachine.Event.OnFocusChanged(
                                    id = focus.id,
                                    style = if (focus.id == context)
                                        Content.Text.Style.TITLE
                                    else
                                        blocks.first { it.id == focus.id }.textStyle()
                                )
                            )
                        } catch (e: NoSuchElementException) {
                            Timber.e(e, "Could not found focused block. Doc size: ${blocks.size}")
                        }
                    }
                }
                _focus.postValue(focus.id)
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
                .filter { it.events.isNotEmpty() }
                .map { payload -> processEvents(payload.events) }
                .collect { viewModelScope.launch { refresh() } }
        }
    }

    private fun startObservingErrors() {
        viewModelScope.launch {
            orchestrator.proxies.errors
                .stream()
                .collect { _toasts.offer(it.message ?: "Unknown error") }
        }
        viewModelScope.launch {
            orchestrator.proxies.toasts
                .stream()
                .collect { _toasts.send(it) }
        }
    }

    private suspend fun processEvents(events: List<Event>) {
        Timber.d("Blocks before handling events: $blocks")
        Timber.d("Events: $events")
        events.forEach { event ->
            if (event is Event.Command.ShowBlock) {
                orchestrator.stores.details.update(event.details)
                orchestrator.stores.relations.update(event.relations)
                orchestrator.stores.objectTypes.update(event.objectTypes)
                orchestrator.stores.objectRestrictions.update(event.objectRestrictions)
            }
            if (event is Event.Command.Details) {
                orchestrator.stores.details.apply { update(current().process(event)) }
            }
            if (event is Event.Command.ObjectRelations) {
                orchestrator.stores.relations.apply { update(current().process(event)) }
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
                    fnR = { marks ->
                        val sortedMarks = marks.sortByType()
                        val newContent = targetContent.copy(marks = sortedMarks)
                        val newBlock = targetBlock.copy(content = newContent)
                        rerenderingBlocks(newBlock)
                        proceedWithUpdatingText(
                            intent = Intent.Text.UpdateText(
                                context = context,
                                text = newBlock.content.asText().text,
                                target = targetBlock.id,
                                marks = sortedMarks
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
            .onEach { document -> refreshStyleToolbar(document) }
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
                    details = details,
                    relations = orchestrator.stores.relations.current(),
                    restrictions = orchestrator.stores.objectRestrictions.current()
                )
            }
            .catch { error ->
                Timber.e(error, "Get error in renderizePipeline")
                emit(emptyList())
            }
            .onEach { views ->
                orchestrator.stores.views.update(views)
                renderCommand.send(Unit)
            }
            .launchIn(viewModelScope)
    }

    private fun refreshStyleToolbar(document: Document) {
        controlPanelViewState.value?.let { state ->
            if (state.stylingToolbar.isVisible || state.styleColorToolbar.isVisible || state.styleExtraToolbar.isVisible) {
                state.stylingToolbar.target?.id?.let { targetId ->
                    controlPanelInteractor.onEvent(
                        event = ControlPanelMachine.Event.OnRefresh.StyleToolbar(
                            target = document.find { it.id == targetId },
                            selection = orchestrator.stores.textSelection.current().selection
                        )
                    )
                }
            }
            if (state.markupMainToolbar.isVisible) {
                controlPanelInteractor.onEvent(
                    event = ControlPanelMachine.Event.OnRefresh.Markup(
                        target = document.find { block -> block.id == orchestrator.stores.focus.current().id },
                        selection = orchestrator.stores.textSelection.current().selection
                    )
                )
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
            .onEach { pendingTextUpdate = it }
            .debounce(TEXT_CHANGES_DEBOUNCE_DURATION)
            .onEach { if (it == pendingTextUpdate) pendingTextUpdate = null }
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
        Timber.d("onStart, id:[$id]")

        context = id

        stateData.postValue(ViewState.Loading)

        jobs += viewModelScope.launch {
            interceptEvents
                .build(InterceptEvents.Params(context))
                .map { events -> processEvents(events) }
                .collect { refresh() }
        }

        jobs += viewModelScope.launch {
            interceptThreadStatus
                .build(InterceptThreadStatus.Params(context))
                .collect { syncStatus.value = it }
        }

        jobs += viewModelScope.launch {
            dispatcher
                .flow()
                .filter { it.context == context }
                .collect { orchestrator.proxies.payloads.send(it) }
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
                                dispatch(Command.AlertDialog)
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
        val event = payload.events.find { it is Event.Command.ShowBlock }
        if (event is Event.Command.ShowBlock) {
            val root = event.blocks.find { it.id == context }
            when {
                root == null -> Timber.e("Could not find the root block on initial focusing")
                root.children.size == 1 -> {
                    val first = event.blocks.first { it.id == root.children.first() }
                    val content = first.content
                    if (content is Content.Layout && content.type == Content.Layout.Type.HEADER) {
                        try {
                            val title = event.blocks.title()
                            if (title.content<Content.Text>().text.isEmpty()) {
                                val focus = Editor.Focus(id = title.id, cursor = Editor.Cursor.End)
                                viewModelScope.launch { orchestrator.stores.focus.update(focus) }
                            } else {
                                Timber.d("Skipping initial focusing. Title is not empty.")
                            }
                        } catch (e: Throwable) {
                            Timber.e(e, "Error while initial focusing")
                        }
                    }
                }
                else -> Timber.d("Skipping initial focusing, document is not empty.")
            }
        }
    }

    fun onAddLinkPressed(blockId: String, link: String, range: IntRange) {
        Timber.d("onAddLinkPressed, blockId:[$blockId] link:[$link] range:[$range]")
        applyLinkMarkup(blockId, link, range)
    }

    fun onSystemBackPressed(editorHasChildrenScreens: Boolean) {
        Timber.d("onSystemBackPressed, editorHasChildrenScreens:[$editorHasChildrenScreens]")
        if (editorHasChildrenScreens) {
            dispatch(Command.PopBackStack)
        } else {
            val state = controlPanelViewState.value
            checkNotNull(state) { "Control panel state is null" }
            when {
                state.stylingToolbar.isVisible -> {
                    onCloseBlockStyleToolbarClicked()
                }
                state.styleColorToolbar.isVisible -> {
                    onCloseBlockStyleColorToolbarClicked()
                }
                state.styleExtraToolbar.isVisible -> {
                    onCloseBlockStyleExtraToolbarClicked()
                }
                state.multiSelect.isVisible -> {
                    onExitMultiSelectModeClicked()
                }
                else -> {
                    proceedWithExitingBack()
                }
            }
        }
    }

    fun onDismissBlockActionMenu(editorHasChildrenScreens: Boolean) {
        Timber.d("onDismissBlockActionMenu, editorHasChildrenScreens:[$editorHasChildrenScreens]")
        onExitActionMode()
        onSystemBackPressed(editorHasChildrenScreens)
    }

    fun onBackButtonPressed() {
        Timber.d("onBackButtonPressed, ")
        proceedWithExitingBack()
    }

    fun onBottomSheetHidden() {
        Timber.d("onBottomSheetHidden, ")
        proceedWithExitingToDashboard()
    }

    private fun proceedWithExitingBack() {
        val update = pendingTextUpdate
        if (update != null) {
            viewModelScope.launch {
                orchestrator.proxies.saves.send(null)
                orchestrator.proxies.changes.send(null)
            }
            viewModelScope.launch {
                orchestrator.updateText(
                    UpdateText.Params(
                        context = context,
                        text = update.text,
                        marks = update.markup,
                        target = update.target
                    )
                ).proceed(
                    failure = { exitBack() },
                    success = { exitBack() }
                )
            }
        } else {
            exitBack()
        }
    }

    private fun exitBack() {
        when (session.value) {
            Session.ERROR -> navigate(EventWrapper(AppNavigation.Command.Exit))
            Session.IDLE -> navigate(EventWrapper(AppNavigation.Command.Exit))
            Session.OPEN -> {
                viewModelScope.launch {
                    closePage(
                        CloseBlock.Params(context)
                    ).proceed(
                        success = { navigation.postValue(EventWrapper(AppNavigation.Command.Exit)) },
                        failure = { Timber.e(it, "Error while closing document: $context") }
                    )
                }
            }
        }
    }

    private fun proceedWithExitingToDashboard() {
        val update = pendingTextUpdate
        if (update != null) {
            viewModelScope.launch {
                orchestrator.proxies.saves.send(null)
                orchestrator.proxies.changes.send(null)
            }
            viewModelScope.launch {
                orchestrator.updateText(
                    UpdateText.Params(
                        context = context,
                        text = update.text,
                        marks = update.markup,
                        target = update.target
                    )
                ).proceed(
                    failure = { exitDashboard() },
                    success = { exitDashboard() }
                )
            }
        } else {
            exitDashboard()
        }
    }

    private fun exitDashboard() {
        viewModelScope.launch {
            closePage(CloseBlock.Params(context)).proceed(
                success = { navigateToDesktop() },
                failure = { Timber.e(it, "Error while closing this page: $context") }
            )
        }
    }

    fun navigateToDesktop() {
        Timber.d("navigateToDesktop, ")
        viewModelScope.sendEvent(
            analytics = analytics,
            eventName = EventsDictionary.SCREEN_DASHBOARD
        )
        navigation.postValue(EventWrapper(AppNavigation.Command.ExitToDesktop))
    }

    @Deprecated("replace by onTextBlockTextChanged")
    fun onTextChanged(
        id: String,
        text: String,
        marks: List<Content.Text.Mark>
    ) {
        Timber.d("onTextChanged, id:[$id], text:[$text], marks:[$marks]")
        val update = TextUpdate.Default(target = id, text = text, markup = marks)
        viewModelScope.launch { orchestrator.proxies.changes.send(update) }
    }

    fun onTitleBlockTextChanged(view: BlockView.Title) {
        Timber.d("onTitleBlockTextChanged, view:[$view]")
        val new = views.map { if (it.id == view.id) view else it }
        val update = TextUpdate.Default(
            target = view.id,
            text = view.text ?: EMPTY_TEXT,
            markup = emptyList()
        )
        viewModelScope.launch { orchestrator.stores.views.update(new) }
        viewModelScope.launch { orchestrator.proxies.changes.send(update) }
    }

    fun onDescriptionBlockTextChanged(view: BlockView.Description) {

        Timber.d("onDescriptionBlockTextChanged, view:[$view]")

        val new = views.map { if (it.id == view.id) view else it }
        val update = TextUpdate.Default(
            target = view.id,
            text = view.description ?: EMPTY_TEXT,
            markup = emptyList()
        )
        viewModelScope.launch { orchestrator.stores.views.update(new) }
        viewModelScope.launch { orchestrator.proxies.changes.send(update) }
    }

    fun onTextBlockTextChanged(view: BlockView.Text) {

        Timber.d("onTextBlockTextChanged, view:[$view]")

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
        if (mode != EditorMode.Edit) return
        Timber.d("onSelectionChanged, id:[$id] selection:[$selection]")
        viewModelScope.launch {
            orchestrator.stores.textSelection.update(Editor.TextSelection(id, selection))
        }
        blocks.find { it.id == id }?.let { target ->
            controlPanelInteractor.onEvent(
                ControlPanelMachine.Event.OnSelectionChanged(
                    target = target,
                    selection = selection
                )
            )
        }
    }

    fun onBlockFocusChanged(id: String, hasFocus: Boolean) {
        Timber.d("onBlockFocusChanged, id:[$id] hasFocus:[$hasFocus]")
        if (hasFocus) {
            viewModelScope.launch {
                orchestrator.stores.focus.update(
                    Editor.Focus.id(id = id, isPending = false)
                )
            }
//            controlPanelInteractor.onEvent(
//                ControlPanelMachine.Event.OnFocusChanged(
//                    id = id,
//                    style = if (id == context)
//                        Content.Text.Style.TITLE
//                    else
//                        blocks.first { it.id == id }.textStyle()
//                )
//            )
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
        Timber.d("onEnterKeyClicked, target:[$target] text:[$text] marks:[$marks] range:[$range]")
        val focus = orchestrator.stores.focus.current()
        if (!focus.isEmpty && focus.id == target) {
            proceedWithEnterEvent(focus.id, range, text, marks)
        } else {
            Timber.e("No blocks in focus, emit SplitLineEnter event")
        }
    }

    private fun proceedWithEnterEvent(
        target: Id,
        range: IntRange,
        text: String,
        marks: List<Content.Text.Mark>
    ) {
        val block = blocks.first { it.id == target }
        val content = block.content.asText()
        if (content.style == Content.Text.Style.TITLE) {
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
        Timber.d("onEndLineEnterClicked, id:[$id] text:[$text] marks:[$marks]")

        val target = blocks.first { it.id == id }

        val content = target.content<Content.Text>().copy(
            text = text,
            marks = marks
        )

        blocks = blocks.replace(
            replacement = { old -> old.copy(content = content) }
        ) { block -> block.id == id }

        if (content.isList() || content.isToggle()) {
            handleEndlineEnterPressedEventForListItem(content, id)
        } else {
            proceedWithCreatingNewTextBlock(
                id = id,
                style = Content.Text.Style.P
            )
        }
    }

    fun onDocumentMenuClicked() {
        Timber.d("onDocumentMenuClicked, ")
        proceedWithOpeningObjectMenu()
    }

    private fun proceedWithOpeningObjectMenu() {
        blocks.find { it.id == context }?.let { root ->
            val content = root.content
            check(content is Content.Smart)
            when (content.type) {
                SmartBlockType.PROFILE_PAGE -> {
                    val details = orchestrator.stores.details.current().details
                    val restrictions = orchestrator.stores.objectRestrictions.current()
                    dispatch(
                        command = Command.OpenProfileMenu(
                            status = syncStatus.value ?: SyncStatus.UNKNOWN,
                            title = try {
                                blocks.title().content<Content.Text>().text
                            } catch (e: Throwable) {
                                null
                            },
                            emoji = details[context]?.iconEmoji?.let { name ->
                                if (name.isNotEmpty())
                                    name
                                else
                                    null
                            },
                            image = details[context]?.iconImage?.let { name ->
                                if (name.isNotEmpty())
                                    urlBuilder.image(name)
                                else
                                    null
                            },
                            isDeleteAllowed = restrictions.none { it == ObjectRestriction.DELETE },
                            isLayoutAllowed = restrictions.none { it == ObjectRestriction.LAYOUT_CHANGE },
                            isDetailsAllowed = restrictions.none { it == ObjectRestriction.DETAILS },
                            isRelationsAllowed = restrictions.none { it == ObjectRestriction.RELATIONS },
                            isDownloadAllowed = false
                        )
                    )
                    viewModelScope.sendEvent(
                        analytics = analytics,
                        eventName = POPUP_PROFILE_MENU
                    )
                }
                SmartBlockType.PAGE -> {
                    val details = orchestrator.stores.details.current().details
                    val restrictions = orchestrator.stores.objectRestrictions.current()
                    controlPanelInteractor.onEvent(ControlPanelMachine.Event.OnDocumentMenuClicked)
                    dispatch(
                        command = Command.OpenDocumentMenu(
                            status = syncStatus.value ?: SyncStatus.UNKNOWN,
                            title = try {
                                blocks.title().content<Content.Text>().text
                            } catch (e: Throwable) {
                                null
                            },
                            emoji = details[context]?.iconEmoji?.let { name ->
                                if (name.isNotEmpty())
                                    name
                                else
                                    null
                            },
                            image = details[context]?.iconImage?.let { name ->
                                if (name.isNotEmpty())
                                    urlBuilder.image(name)
                                else
                                    null
                            },
                            isDeleteAllowed = restrictions.none { it == ObjectRestriction.DELETE },
                            isLayoutAllowed = restrictions.none { it == ObjectRestriction.LAYOUT_CHANGE },
                            isDetailsAllowed = restrictions.none { it == ObjectRestriction.DETAILS },
                            isRelationsAllowed = restrictions.none { it == ObjectRestriction.RELATIONS },
                            isArchived = details[context]?.isArchived ?: false,
                            isDownloadAllowed = false
                        )
                    )
                    viewModelScope.sendEvent(
                        analytics = analytics,
                        eventName = POPUP_DOCUMENT_MENU
                    )
                }
                SmartBlockType.FILE -> {
                    val details = orchestrator.stores.details.current().details
                    val restrictions = orchestrator.stores.objectRestrictions.current()
                    controlPanelInteractor.onEvent(ControlPanelMachine.Event.OnDocumentMenuClicked)
                    dispatch(
                        command = Command.OpenDocumentMenu(
                            status = syncStatus.value ?: SyncStatus.UNKNOWN,
                            title = try {
                                blocks.title().content<Content.Text>().text
                            } catch (e: Throwable) {
                                null
                            },
                            emoji = details[context]?.iconEmoji?.let { name ->
                                if (name.isNotEmpty())
                                    name
                                else
                                    null
                            },
                            image = details[context]?.iconImage?.let { name ->
                                if (name.isNotEmpty())
                                    urlBuilder.image(name)
                                else
                                    null
                            },
                            isDeleteAllowed = restrictions.none { it == ObjectRestriction.DELETE },
                            isLayoutAllowed = restrictions.none { it == ObjectRestriction.LAYOUT_CHANGE },
                            isDetailsAllowed = restrictions.none { it == ObjectRestriction.DETAILS },
                            isRelationsAllowed = restrictions.none { it == ObjectRestriction.RELATIONS },
                            isArchived = details[context]?.isArchived ?: false,
                            isDownloadAllowed = true
                        )
                    )
                    viewModelScope.sendEvent(
                        analytics = analytics,
                        eventName = POPUP_DOCUMENT_MENU
                    )
                }
                else -> {
                    Timber.e("Trying to open menu for unexpected smart content: ${content.type}")
                }
            }
        }
    }

    fun onEmptyBlockBackspaceClicked(id: String) {
        Timber.d("onEmptyBlockBackspaceClicked, id:[$id]")
        proceedWithUnlinking(target = id)
    }

    fun onNonEmptyBlockBackspaceClicked(
        id: String,
        text: String,
        marks: List<Content.Text.Mark>
    ) {
        Timber.d("onNonEmptyBlockBackspaceClicked, id:[$id] text:[$text] marks:[$marks]")

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

        val index = views.indexOfFirst { it.id == id }

        if (index > 0) {
            val previous = views[index.dec()]
            if (previous is BlockView.Text) {
                proceedWithMergingBlocks(
                    previous = previous.id,
                    target = id
                )
            } else {
                if (previous is BlockView.Title) _toasts.offer("Merging with title currently not supported")
                Timber.d("Skipping merge because previous block is not a text block")
            }
        } else {
            Timber.d("Skipping merge on non-empty-block-backspace-pressed event")
        }
    }

    private fun handleEndlineEnterPressedEventForListItem(
        content: Content.Text,
        id: String
    ) {
        if (content.text.isNotEmpty()) {
            proceedWithSplitEvent(
                target = id,
                range = content.text.length..content.text.length,
                marks = content.marks,
                text = content.text
            )
        } else {
            proceedWithUpdateTextStyle(
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

    private fun onBlockLongPressedClicked(target: String, dimensions: BlockDimensions) {
        val views = orchestrator.stores.views.current()
        val view = views.find { it.id == target }

        val restrictions = orchestrator.stores.objectRestrictions.current()
        if (restrictions.isNotEmpty()) {
            when (view) {
                is BlockView.Code, is BlockView.Text,
                is BlockView.Media, is BlockView.MediaPlaceholder,
                is BlockView.Upload -> {
                    if (restrictions.contains(ObjectRestriction.BLOCKS)) {
                        _toasts.offer(NOT_ALLOWED_FOR_OBJECT)
                        return
                    }
                }
                is BlockView.Relation, is BlockView.FeaturedRelation -> {
                    if (restrictions.contains(ObjectRestriction.RELATIONS)) {
                        _toasts.offer(NOT_ALLOWED_FOR_OBJECT)
                        return
                    }
                }
                is BlockView.Title -> {
                    if (restrictions.contains(ObjectRestriction.DETAILS)) {
                        _toasts.offer(NOT_ALLOWED_FOR_OBJECT)
                        return
                    }
                }
            }
        }

        if (view != null) {
            onEnterActionMode()
            dispatch(
                Command.OpenActionBar(
                    block = view,
                    dimensions = dimensions
                )
            )
            viewModelScope.sendEvent(
                analytics = analytics,
                eventName = POPUP_ACTION_MENU
            )
        } else {
            Timber.e("Could not open action menu on long click. Target view was missing.")
        }
    }

    fun onEditorContextMenuStyleClicked(selection: IntRange) {

        Timber.d("onEditorContextMenuStyleClicked, selection:[$selection]")

        val target = blocks.first { it.id == orchestrator.stores.focus.current().id }
        controlPanelInteractor.onEvent(
            ControlPanelMachine.Event.OnEditorContextMenuStyleClicked(
                target = target,
                selection = selection
            )
        )
        viewModelScope.sendEvent(
            analytics = analytics,
            eventName = POPUP_STYLE
        )
    }

    fun onStylingToolbarEvent(event: StylingEvent) {
        Timber.d("onStylingToolbarEvent, event:[$event]")
        val state = controlPanelViewState.value!!
        when (event) {
            is StylingEvent.Coloring.Text -> {
                val currentMode = mode
                if (currentMode is EditorMode.Styling.Multi) {
                    onToolbarTextColorAction(currentMode.targets.toList(), event.color.title)
                } else {
                    proceedWithStylingEvent(state, Markup.Type.TEXT_COLOR, event.color.title)
                }
            }
            is StylingEvent.Coloring.Background -> {
                val currentMode = mode
                if (currentMode is EditorMode.Styling.Multi) {
                    onBlockBackgroundColorAction(currentMode.targets.toList(), event.color.title)
                } else {
                    proceedWithStylingEvent(state, Markup.Type.BACKGROUND_COLOR, event.color.title)
                }
            }
            is StylingEvent.Markup.Bold -> {
                val currentMode = mode
                if (currentMode is EditorMode.Styling.Multi) {
                    _toasts.trySend(ERROR_UNSUPPORTED_BEHAVIOR)
                } else {
                    proceedWithStylingEvent(state, Markup.Type.BOLD, null)
                }
            }
            is StylingEvent.Markup.Italic -> {
                val currentMode = mode
                if (currentMode is EditorMode.Styling.Multi) {
                    _toasts.trySend(ERROR_UNSUPPORTED_BEHAVIOR)
                } else {
                    proceedWithStylingEvent(state, Markup.Type.ITALIC, null)
                }
            }
            is StylingEvent.Markup.StrikeThrough -> {
                val currentMode = mode
                if (currentMode is EditorMode.Styling.Multi) {
                    _toasts.trySend(ERROR_UNSUPPORTED_BEHAVIOR)
                } else {
                    proceedWithStylingEvent(state, Markup.Type.STRIKETHROUGH, null)
                }
            }
            is StylingEvent.Markup.Code -> {
                val currentMode = mode
                if (currentMode is EditorMode.Styling.Multi) {
                    _toasts.trySend(ERROR_UNSUPPORTED_BEHAVIOR)
                } else {
                    proceedWithStylingEvent(state, Markup.Type.KEYBOARD, null)
                }
            }
            is StylingEvent.Markup.Link -> {
                val currentMode = mode
                if (currentMode is EditorMode.Styling.Multi) {
                    _toasts.trySend(ERROR_UNSUPPORTED_BEHAVIOR)
                } else {
                    proceedWithStylingEvent(state, Markup.Type.LINK, null)
                }
            }
            is StylingEvent.Alignment.Left -> {
                val currentMode = mode
                if (currentMode is EditorMode.Styling.Multi) {
                    proceedWithAlignmentUpdate(
                        targets = currentMode.targets.toList(),
                        alignment = Block.Align.AlignLeft
                    )
                } else {
                    onBlockAlignmentActionClicked(Alignment.START)
                }
            }
            is StylingEvent.Alignment.Center -> {
                val currentMode = mode
                if (currentMode is EditorMode.Styling.Multi) {
                    proceedWithAlignmentUpdate(
                        targets = currentMode.targets.toList(),
                        alignment = Block.Align.AlignCenter
                    )
                } else {
                    onBlockAlignmentActionClicked(Alignment.CENTER)
                }
            }
            is StylingEvent.Alignment.Right -> {
                val currentMode = mode
                if (currentMode is EditorMode.Styling.Multi) {
                    proceedWithAlignmentUpdate(
                        targets = currentMode.targets.toList(),
                        alignment = Block.Align.AlignRight
                    )
                } else {
                    onBlockAlignmentActionClicked(Alignment.END)
                }
            }
            else -> Timber.d("Ignoring styling toolbar event: $event")
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
                    Markup.Type.TEXT_COLOR -> onToolbarTextColorAction(listOf(id), param)
                    Markup.Type.BACKGROUND_COLOR -> onBlockBackgroundColorAction(listOf(id), param)
                    Markup.Type.LINK -> onBlockStyleLinkClicked(id)
                    Markup.Type.KEYBOARD -> onBlockStyleMarkupActionClicked(id, type)
                    Markup.Type.MENTION -> Unit
                }
            } ?: run { Timber.e("Target id was missing for markup styling event: $type") }
        }
    }

    fun onStyleToolbarMarkupAction(type: Markup.Type, param: String? = null) {
        Timber.d("onStyleToolbarMarkupAction, type:[$type] param:[$param]")
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
            proceedWithAlignmentUpdate(
                targets = listOf(id),
                alignment = when (alignment) {
                    Alignment.START -> Block.Align.AlignLeft
                    Alignment.CENTER -> Block.Align.AlignCenter
                    Alignment.END -> Block.Align.AlignRight
                }
            )
        }
    }

    private fun proceedWithAlignmentUpdate(targets: List<Id>, alignment: Block.Align) {
        viewModelScope.launch {
            orchestrator.proxies.intents.send(
                Intent.Text.Align(
                    context = context,
                    targets = targets,
                    alignment = alignment
                )
            )
        }
    }

    fun onToolbarTextColorAction(targets: List<Id>, color: String?) {
        Timber.d("onToolbarTextColorAction, ids:[$targets] color:[$color]")
        check(color != null)
        controlPanelInteractor.onEvent(ControlPanelMachine.Event.OnBlockTextColorSelected)
        viewModelScope.launch {
            orchestrator.proxies.intents.send(
                Intent.Text.UpdateColor(
                    context = context,
                    targets = targets,
                    color = color
                )
            )
        }
    }

    private fun onBlockBackgroundColorAction(targets: List<Id>, color: String?) {
        check(color != null)
        controlPanelInteractor.onEvent(ControlPanelMachine.Event.OnBlockBackgroundColorSelected)
        viewModelScope.launch {
            orchestrator.proxies.intents.send(
                Intent.Text.UpdateBackgroundColor(
                    context = context,
                    targets = targets,
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
        viewModelScope.sendEvent(
            analytics = analytics,
            eventName = POPUP_MARKUP_LINK
        )
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

    fun onSetRelationKeyClicked(blockId: Id, key: Id) {
        Timber.d("onSetRelationKeyClicked, blockId:[$blockId] key:[$key]")
        viewModelScope.launch {
            orchestrator.proxies.intents.send(
                Intent.Document.SetRelationKey(
                    context = context,
                    blockId = blockId,
                    key = key
                )
            )
        }
    }

    fun onActionMenuItemClicked(id: String, action: ActionItemType) {
        Timber.d("onActionMenuItemClicked, id:[$id] action:[$action]")
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
                val excludedCategories = mutableListOf<String>()
                val target = blocks.first { it.id == id }
                when (val content = target.content) {
                    is Content.Text -> {
                        excludedCategories.addAll(excludedCategoriesForText())
                        excludedTypes.addAll(excludeTypesForText())
                    }
                    is Content.Divider -> {
                        excludedCategories.addAll(excludeCategoriesForDivider())
                        when (content.style) {
                            Content.Divider.Style.LINE -> excludedTypes.addAll(
                                excludeTypesForLineDivider()
                            )
                            Content.Divider.Style.DOTS -> excludedTypes.addAll(
                                excludeTypesForDotsDivider()
                            )
                        }
                    }
                }
                onExitActionMode()
                dispatch(
                    Command.OpenTurnIntoPanel(
                        target = id,
                        excludedCategories = excludedCategories,
                        excludedTypes = excludedTypes
                    )
                )
                viewModelScope.sendEvent(
                    analytics = analytics,
                    eventName = POPUP_TURN_INTO
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
                _toasts.trySend("Rename not implemented")
            }
            ActionItemType.MoveTo -> {
                onExitActionMode()
                dispatch(Command.PopBackStack)
                proceedWithMoveTo(id)
            }
            ActionItemType.Style -> {
                viewModelScope.launch { proceedWithOpeningStyleToolbarFromActionMenu(id) }
            }
            ActionItemType.Download -> {
                viewModelScope.launch {
                    onExitActionMode()
                    dispatch(Command.PopBackStack)
                    delay(300)
                    dispatch(Command.RequestDownloadPermission(id))
                }
            }
            ActionItemType.SAM -> {
                mode = EditorMode.SAM
                viewModelScope.launch { orchestrator.stores.focus.update(Editor.Focus.empty()) }
                viewModelScope.launch { refresh() }
                proceedWithSAMQuickStartSelection(id)
                controlPanelInteractor.onEvent(ControlPanelMachine.Event.SAM.OnQuickStart(1))
                dispatch(Command.PopBackStack)
            }
            ActionItemType.Replace -> {
                _toasts.offer("Replace not implemented")
            }
            ActionItemType.AddCaption -> {
                _toasts.offer("Add caption not implemented")
            }
            ActionItemType.Divider -> {
                _toasts.offer("not implemented")
            }
            ActionItemType.TurnIntoPage -> {
                proceedWithTurningIntoDocument(targets = listOf(id))
                onExitActionMode()
                dispatch(Command.PopBackStack)
            }
            else -> Timber.d("Action ignored: $action")
        }
    }

    private fun proceedWithOpeningStyleToolbarFromActionMenu(id: String) {
        val target = id

        val lastKnownSelection = orchestrator.stores.textSelection.current().takeIf { value ->
            value.id == target
        }

        val lastKnownCursor = lastKnownSelection?.selection

        val isFocused = lastKnownSelection?.isNotEmpty ?: false

        mode = EditorMode.Styling.Single(
            target = target,
            cursor = lastKnownCursor?.first
        )

        viewModelScope.launch {
            orchestrator.stores.focus.update(Editor.Focus.empty())
            orchestrator.stores.views.update(views.singleStylingMode(target))
            renderCommand.send(Unit)
        }

        controlPanelInteractor.onEvent(
            ControlPanelMachine.Event.OnBlockActionToolbarStyleClicked(
                target = blocks.first { it.id == target },
                focused = isFocused,
                selection = lastKnownCursor
            )
        )

        dispatch(Command.PopBackStack)

        viewModelScope.sendEvent(
            analytics = analytics,
            eventName = POPUP_STYLE
        )
    }

    private fun proceedWithMoveTo(id: String) {
        viewModelScope.sendEvent(
            analytics = analytics,
            eventName = EventsDictionary.SCREEN_MOVE_TO
        )

        val excluded = mutableListOf<Id>()

        val target = blocks.find { it.id == id }

        if (target != null) {
            (target.content as? Content.Link)?.let { content ->
                excluded.add(content.target)
            }
        }

        navigate(
            EventWrapper(
                AppNavigation.Command.OpenMoveToScreen(
                    context = context,
                    targets = listOf(id),
                    excluded = excluded
                )
            )
        )
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
        Timber.d("onActionUndoClicked, ")
        viewModelScope.launch {
            orchestrator.proxies.intents.send(
                Intent.Document.Undo(
                    context = context,
                    onUndoExhausted = { _toasts.offer("Nothing to undo.") }
                )
            )
        }
    }

    fun onActionRedoClicked() {
        Timber.d("onActionRedoClicked, ")
        viewModelScope.launch {
            orchestrator.proxies.intents.send(
                Intent.Document.Redo(
                    context = context,
                    onRedoExhausted = { _toasts.offer("Nothing to redo.") }
                )
            )
        }
    }

    fun onEnterSearchModeClicked() {
        Timber.d("onEnterSearchModeClicked, ")
        mode = EditorMode.Search
        viewModelScope.launch { orchestrator.stores.views.update(views.toReadMode()) }
        viewModelScope.launch { renderCommand.send(Unit) }
        viewModelScope.launch { controlPanelInteractor.onEvent(ControlPanelMachine.Event.SearchToolbar.OnEnterSearchMode) }
    }

    fun onDocRelationsClicked() {
        Timber.d("onDocRelationsClicked, ")
        dispatch(Command.OpenObjectRelationScreen.List(ctx = context, target = null))
    }

    fun onSearchToolbarEvent(event: SearchInDocEvent) {
        Timber.d("onSearchToolbarEvent, event:[$event]")
        if (mode !is EditorMode.Search) return
        when (event) {
            is SearchInDocEvent.Query -> {
                val query = event.query.trim()
                val update = if (query.isEmpty()) {
                    views.clearSearchHighlights()
                } else {
                    val flags = Pattern.MULTILINE or Pattern.CASE_INSENSITIVE
                    val escaped = Pattern.quote(query)
                    val pattern = Pattern.compile(escaped, flags)
                    views.highlight { pairs ->
                        pairs.map { (key, txt) ->
                            BlockView.Searchable.Field(
                                key = key,
                                highlights = txt.search(pattern)
                            )
                        }
                    }
                }
                viewModelScope.launch { orchestrator.stores.views.update(update) }
                viewModelScope.launch { renderCommand.send(Unit) }
            }
            is SearchInDocEvent.Next -> {
                val update = views.nextSearchTarget()
                viewModelScope.launch { orchestrator.stores.views.update(update) }
                viewModelScope.launch { renderCommand.send(Unit) }
                val target = update.find { view ->
                    view is BlockView.Searchable && view.searchFields.any { it.isTargeted }
                }
                val pos = update.indexOf(target)
                searchResultScrollPosition.value = pos
            }
            is SearchInDocEvent.Previous -> {
                val update = views.previousSearchTarget()
                viewModelScope.launch { orchestrator.stores.views.update(update) }
                viewModelScope.launch { renderCommand.send(Unit) }
                val target = update.find { view ->
                    view is BlockView.Searchable && view.searchFields.any { it.isTargeted }
                }
                val pos = update.indexOf(target)
                searchResultScrollPosition.value = pos
            }
            is SearchInDocEvent.Cancel -> {
                mode = EditorMode.Edit
                val update = views.clearSearchHighlights().toEditMode()
                viewModelScope.launch { orchestrator.stores.views.update(update) }
                viewModelScope.launch { renderCommand.send(Unit) }
                controlPanelInteractor.onEvent(ControlPanelMachine.Event.SearchToolbar.OnExitSearchMode)
                dispatch(Command.ClearSearchInput)
            }
            is SearchInDocEvent.Search -> {
                val update = views.nextSearchTarget()
                viewModelScope.launch { orchestrator.stores.views.update(update) }
                viewModelScope.launch { renderCommand.send(Unit) }
                val target = update.find { view ->
                    view is BlockView.Searchable && view.searchFields.any { it.isTargeted }
                }
                val pos = update.indexOf(target)
                searchResultScrollPosition.value = pos
            }
        }
    }

    fun onAddTextBlockClicked(style: Content.Text.Style) {

        Timber.d("onAddTextBlockClicked, style:[$style]")

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
            proceedWithCreatingNewTextBlock(
                id = target.id,
                style = style,
                position = Position.BOTTOM
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

        Timber.d("onAddLinkToObjectClicked, ")

        val focused = blocks.first { it.id == orchestrator.stores.focus.current().id }

        val content = focused.content

        val replace = content is Content.Text && content.text.isEmpty()

        var position: Position = Position.BOTTOM

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

        viewModelScope.sendEvent(
            analytics = analytics,
            eventName = EventsDictionary.SCREEN_LINK_TO
        )

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

    fun onAddRelationBlockClicked() {

        Timber.d("onAddRelationBlockClicked, ")

        val focused = blocks.first { it.id == orchestrator.stores.focus.current().id }
        val content = focused.content
        val replace = content is Content.Text && content.text.isEmpty()

        viewModelScope.launch {
            if (replace) {
                orchestrator.proxies.intents.send(
                    Intent.CRUD.Replace(
                        context = context,
                        target = focused.id,
                        prototype = Prototype.Relation(key = "")
                    )
                )
            } else {
                orchestrator.proxies.intents.send(
                    Intent.CRUD.Create(
                        context = context,
                        target = focused.id,
                        position = Position.BOTTOM,
                        prototype = Prototype.Relation(key = "")
                    )
                )
            }
        }
    }

    fun onTogglePlaceholderClicked(target: Id) {
        Timber.d("onTogglePlaceholderClicked, target:[$target]")
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
        Timber.d("onToggleClicked, target:[$target]")
        onToggleChanged(target)
        viewModelScope.launch { refresh() }
    }

    private fun onAddLocalFileClicked(blockId: String) {
        mediaBlockId = blockId
        dispatch(Command.OpenGallery(mediaType = MIME_FILE_ALL))
    }

    fun onAddFileBlockClicked(type: Content.File.Type) {
        Timber.d("onAddFileBlockClicked, type:[$type]")
        val focused = blocks.first { it.id == orchestrator.stores.focus.current().id }
        val content = focused.content
        if (content is Content.Text && content.text.isEmpty()) {
            proceedWithReplacingByEmptyFileBlock(
                id = focused.id,
                type = type
            )
        } else {
            proceedWithCreatingEmptyFileBlock(
                id = focused.id,
                type = type,
                position = Position.BOTTOM
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

        Timber.d("onCheckboxClicked, view:[$view]")

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

    fun onTitleCheckboxClicked(view: BlockView.Title.Todo) {

        Timber.d("onTitleCheckboxClicked, view:[$view]")

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
        Timber.d("onBlockToolbarStyleClicked, ")
        val focus = orchestrator.stores.focus.current()
        val target = focus.id
        if (target.isNotEmpty()) {
            when (views.find { it.id == target }) {
                is BlockView.Title -> _toasts.offer(CANNOT_OPEN_STYLE_PANEL_FOR_TITLE_ERROR)
                is BlockView.Code -> {
                    val selection = orchestrator.stores.textSelection.current().selection
                    if (selection != null && selection.first != selection.last) {
                        _toasts.offer(CANNOT_OPEN_STYLE_PANEL_FOR_CODE_BLOCK_ERROR)
                    } else {
                        proceedWithStyleToolbarEvent()
                    }
                }
                else -> {
                    proceedWithStyleToolbarEvent()
                }
            }
        } else {
            Timber.e("Unknown focus for style toolbar: $focus")
        }
    }

    private fun proceedWithStyleToolbarEvent() {
        val target = orchestrator.stores.focus.current().id
        mode = EditorMode.Styling.Single(
            target = target,
            cursor = orchestrator.stores.textSelection.current().selection?.first
        )
        viewModelScope.launch {
            orchestrator.stores.focus.update(Editor.Focus.empty())
            orchestrator.stores.views.update(views.singleStylingMode(target))
            renderCommand.send(Unit)
        }

        val textSelection = orchestrator.stores.textSelection.current()
        controlPanelInteractor.onEvent(
            ControlPanelMachine.Event.OnBlockActionToolbarStyleClicked(
                target = blocks.first { it.id == target },
                focused = textSelection.isNotEmpty,
                selection = textSelection.selection
            )
        )
        viewModelScope.sendEvent(
            analytics = analytics,
            eventName = EventsDictionary.BTN_STYLE_MENU
        )
        viewModelScope.sendEvent(
            analytics = analytics,
            eventName = POPUP_STYLE
        )
    }

    private fun proceedWithMultiStyleToolbarEvent() {
        mode = EditorMode.Styling.Multi(currentSelection())
        controlPanelInteractor.onEvent(ControlPanelMachine.Event.OnMultiSelectStyleClicked)
//        viewModelScope.sendEvent(
//            analytics = analytics,
//            eventName = EventsDictionary.BTN_STYLE_MENU
//        )
//        viewModelScope.sendEvent(
//            analytics = analytics,
//            eventName = POPUP_STYLE
//        )
    }

    fun onCloseBlockStyleToolbarClicked() {
        Timber.d("onCloseBlockStyleToolbarClicked, ")
        if (mode is EditorMode.Styling.Single) {
            val target = (mode as EditorMode.Styling.Single).target
            val cursor = (mode as EditorMode.Styling.Single).cursor
            mode = EditorMode.Edit
            viewModelScope.launch {
                orchestrator.stores.focus.update(
                    Editor.Focus(
                        id = target,
                        cursor = cursor?.let { c -> Editor.Cursor.Range(c..c) }
                    )
                )
                orchestrator.stores.textSelection.update(
                    Editor.TextSelection(target, cursor?.let { it..it })
                )
                val focused = !orchestrator.stores.focus.current().isEmpty
                controlPanelInteractor.onEvent(
                    ControlPanelMachine.Event.StylingToolbar.OnClose(
                        focused
                    )
                )
                orchestrator.stores.views.update(
                    views.exitSingleStylingMode(
                        target = target,
                        cursor = cursor
                    )
                )
                renderCommand.send(Unit)
            }
        } else if (mode is EditorMode.Styling.Multi) {
            exitMultiStylingMode()
        }
    }

    private fun exitMultiStylingMode() {
        mode = EditorMode.Select
        controlPanelInteractor.onEvent(ControlPanelMachine.Event.StylingToolbar.OnCloseMulti)
    }

    /**
     * Closing style-toolbar and its dependent toolbars (color, extra). Back to edit mode.
     */
    private fun onExitBlockStyleToolbarClicked() {
        if (mode is EditorMode.Styling.Single) {
            val target = (mode as EditorMode.Styling.Single).target
            val cursor = (mode as EditorMode.Styling.Single).cursor
            mode = EditorMode.Edit
            viewModelScope.launch {
                orchestrator.stores.focus.update(
                    Editor.Focus(
                        id = target,
                        cursor = cursor?.let { c -> Editor.Cursor.Range(c..c) }
                    )
                )
                orchestrator.stores.textSelection.update(
                    Editor.TextSelection(target, cursor?.let { it..it })
                )
                controlPanelInteractor.onEvent(ControlPanelMachine.Event.StylingToolbar.OnExit)
                orchestrator.stores.views.update(
                    views.exitSingleStylingMode(
                        target = target,
                        cursor = cursor
                    )
                )
                renderCommand.send(Unit)
            }
        } else if (mode is EditorMode.Styling.Multi) {
            exitMultiStylingMode()
        }
    }

    fun onCloseBlockStyleExtraToolbarClicked() {
        Timber.d("onCloseBlockStyleExtraToolbarClicked, ")
        controlPanelInteractor.onEvent(
            ControlPanelMachine.Event.StylingToolbar.OnExtraClosed
        )
    }

    fun onCloseBlockStyleColorToolbarClicked() {
        Timber.d("onCloseBlockStyleColorToolbarClicked, ")
        controlPanelInteractor.onEvent(
            ControlPanelMachine.Event.StylingToolbar.OnColorClosed
        )
    }

    fun onBlockToolbarBlockActionsClicked() {
        Timber.d("onBlockToolbarBlockActionsClicked, ")
        val target = orchestrator.stores.focus.current().id
        val view = views.first { it.id == target }
        if (view is BlockView.Title) {
            _toasts.offer(CANNOT_OPEN_ACTION_MENU_FOR_TITLE_ERROR)
        } else {
            dispatch(Command.Measure(target = target))
            viewModelScope.sendEvent(
                analytics = analytics,
                eventName = EventsDictionary.BTN_BLOCK_ACTIONS
            )
        }
    }

    fun onMeasure(target: Id, dimensions: BlockDimensions) {
        Timber.d("onMeasure, target:[$target] dimensions:[$dimensions]")
        proceedWithClearingFocus()
        onBlockLongPressedClicked(target, dimensions)
    }

    fun onAddBlockToolbarClicked() {
        Timber.d("onAddBlockToolbarClicked, ")

        dispatch(Command.OpenAddBlockPanel(ctx = context))
        viewModelScope.sendEvent(
            analytics = analytics,
            eventName = EventsDictionary.BTN_ADD_BLOCK_MENU
        )
        viewModelScope.sendEvent(
            analytics = analytics,
            eventName = POPUP_ADD_BLOCK
        )
    }

    fun onEnterMultiSelectModeClicked() {
        Timber.d("onEnterMultiSelectModeClicked, ")
        controlPanelInteractor.onEvent(ControlPanelMachine.Event.MultiSelect.OnEnter)
        mode = EditorMode.Select
        viewModelScope.launch { orchestrator.stores.focus.update(Editor.Focus.empty()) }
        viewModelScope.launch {
            delay(DELAY_REFRESH_DOCUMENT_TO_ENTER_MULTI_SELECT_MODE)
            refresh()
        }
        viewModelScope.sendEvent(
            analytics = analytics,
            eventName = POPUP_MULTI_SELECT_MENU
        )
        viewModelScope.sendEvent(
            analytics = analytics,
            eventName = EventsDictionary.BTN_ENTER_MS
        )
    }

    fun onExitMultiSelectModeClicked() {
        Timber.d("onExitMultiSelectModeClicked, ")
        controlPanelInteractor.onEvent(ControlPanelMachine.Event.MultiSelect.OnExit)
        mode = EditorMode.Edit
        clearSelections()
        viewModelScope.launch {
            delay(DELAY_REFRESH_DOCUMENT_ON_EXIT_MULTI_SELECT_MODE)
            orchestrator.stores.focus.update(Editor.Focus.empty())
            refresh()
        }
        viewModelScope.sendEvent(
            analytics = analytics,
            eventName = EventsDictionary.BTN_MS_DONE
        )
    }

    fun onEnterScrollAndMoveClicked() {
        Timber.d("onEnterScrollAndMoveClicked, ")
        mode = EditorMode.SAM
        controlPanelInteractor.onEvent(ControlPanelMachine.Event.SAM.OnEnter)
        viewModelScope.sendEvent(
            analytics = analytics,
            eventName = EventsDictionary.BTN_SCROLL_MOVE
        )
    }

    fun onExitScrollAndMoveClicked() {
        Timber.d("onExitScrollAndMoveClicked, ")
        viewModelScope.sendEvent(
            analytics = analytics,
            eventName = EventsDictionary.BTN_SCROLL_MOVE_CANCEL
        )
        if (controlPanelViewState.value?.multiSelect?.isQuickScrollAndMoveMode == true) {
            mode = EditorMode.Edit
            controlPanelInteractor.onEvent(ControlPanelMachine.Event.SAM.OnExit)
            viewModelScope.launch { refresh() }
        } else {
            mode = EditorMode.Select
            controlPanelInteractor.onEvent(ControlPanelMachine.Event.SAM.OnExit)
        }
    }

    fun onApplyScrollAndMoveClicked() {
        Timber.d("onApplyScrollAndMoveClicked, ")
        viewModelScope.sendEvent(
            analytics = analytics,
            eventName = EventsDictionary.BTN_SCROLL_MOVE_MOVE
        )
    }

    private fun onEnterActionMode() {
        mode = EditorMode.Action
        controlPanelInteractor.onEvent(ControlPanelMachine.Event.ReadMode.OnEnter)
        viewModelScope.launch {
            refresh()
        }
    }

    private fun onExitActionMode() {
        mode = EditorMode.Edit
        controlPanelInteractor.onEvent(ControlPanelMachine.Event.ReadMode.OnExit)
        viewModelScope.launch { refresh() }
    }

    fun onMultiSelectModeDeleteClicked() {
        Timber.d("onMultiSelectModeDeleteClicked, ")
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

        viewModelScope.sendEvent(
            analytics = analytics,
            eventName = EventsDictionary.BTN_MS_DELETE
        )
    }

    fun onMultiSelectCopyClicked() {
        Timber.d("onMultiSelectCopyClicked, ")
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

        viewModelScope.sendEvent(
            analytics = analytics,
            eventName = EventsDictionary.BTN_MS_COPY
        )
    }

    fun onMultiSelectModeSelectAllClicked() {
        Timber.d("onMultiSelectModeSelectAllClicked, ")
        (stateData.value as ViewState.Success).let { state ->
            if (currentSelection().isEmpty()) {
                onSelectAllClicked(state)
                viewModelScope.sendEvent(
                    analytics = analytics,
                    eventName = EventsDictionary.BTN_MS_SELECT_ALL
                )
            } else {
                onUnselectAllClicked(state)
                viewModelScope.sendEvent(
                    analytics = analytics,
                    eventName = EventsDictionary.BTN_MS_UNSELECT_ALL
                )
            }
        }
    }

    private fun onSelectAllClicked(state: ViewState.Success) =
        state.blocks.map { block ->
            if (block is BlockView.Selectable) {
                select(block.id)
            }
            block.updateSelection(newSelection = true)
        }.let {
            onMultiSelectModeBlockClicked()
            stateData.postValue(ViewState.Success(it))
        }

    private fun onUnselectAllClicked(state: ViewState.Success) =
        state.blocks.map { block ->
            unselect(block.id)
            block.updateSelection(newSelection = false)
        }.let {
            onMultiSelectModeBlockClicked()
            stateData.postValue(ViewState.Success(it))
        }

    fun onMultiSelectStyleButtonClicked() {
        proceedWithMultiStyleToolbarEvent()
    }

    fun onMultiSelectTurnIntoButtonClicked() {
        Timber.d("onMultiSelectTurnIntoButtonClicked, ")

        val targets = currentSelection()

        val blocks = blocks.filter { targets.contains(it.id) }

        val hasTextBlocks = blocks.any { it.content is Content.Text }

        when {
            hasTextBlocks -> {
                proceedUpdateBlockStyle(
                    targets = currentSelection().toList(),
                    uiBlock = UiBlock.PAGE,
                    action = {
                        clearSelections()
                        controlPanelInteractor.onEvent(ControlPanelMachine.Event.MultiSelect.OnTurnInto)
                    },
                    errorAction = { _toasts.offer("Cannot convert selected blocks to PAGE") }
                )
            }
            else -> {
                _toasts.offer("Cannot turn selected blocks into page")
            }
        }

        viewModelScope.sendEvent(
            analytics = analytics,
            eventName = EventsDictionary.BTN_MS_TURN_INTO
        )
    }

    fun onOpenPageNavigationButtonClicked() {
        Timber.d("onOpenPageNavigationButtonClicked, ")
        viewModelScope.sendEvent(
            analytics = analytics,
            eventName = EventsDictionary.SCREEN_NAVIGATION
        )
        navigation.postValue(
            EventWrapper(
                AppNavigation.Command.OpenPageNavigationScreen(
                    target = context
                )
            )
        )
    }

    // ----------------- Turn Into -----------------------------------------

    fun onTurnIntoMultiSelectBlockClicked(uiBlock: UiBlock) {
        Timber.d("onTurnIntoMultiSelectBlockClicked, uiBlock:[$uiBlock]")
        proceedUpdateBlockStyle(
            targets = currentSelection().toList(),
            uiBlock = uiBlock,
            action = {
                clearSelections()
                controlPanelInteractor.onEvent(ControlPanelMachine.Event.MultiSelect.OnTurnInto)
            },
            errorAction = { _toasts.offer("Cannot convert selected blocks to $uiBlock") }
        )
    }

    fun onTurnIntoBlockClicked(target: String, uiBlock: UiBlock) {
        Timber.d("onTurnIntoBlockClicked, taget:[$target] uiBlock:[$uiBlock]")
        proceedUpdateBlockStyle(
            targets = listOf(target),
            uiBlock = uiBlock,
            errorAction = { _toasts.offer("Cannot convert block to $uiBlock") }
        )
        dispatch(Command.PopBackStack)
    }

    fun onUpdateTextBlockStyle(uiBlock: UiBlock) {
        Timber.d("onUpdateSingleTextBlockStyle, uiBlock:[$uiBlock]")
        (mode as? EditorMode.Styling.Single)?.let { eMode ->
            proceedUpdateBlockStyle(
                targets = listOf(eMode.target),
                uiBlock = uiBlock,
                errorAction = { _toasts.offer("Cannot convert block to $uiBlock") }
            )
        }
        (mode as? EditorMode.Styling.Multi)?.let {
            proceedUpdateBlockStyle(
                targets = currentSelection().toList(),
                uiBlock = uiBlock,
                errorAction = { _toasts.offer("Cannot convert block to $uiBlock") }
            )
        }
    }

    fun onBlockStyleToolbarOtherClicked() {
        Timber.d("onBlockStyleToolbarOtherClicked, ")
        controlPanelInteractor.onEvent(ControlPanelMachine.Event.StylingToolbar.OnExtraClicked)
    }

    fun onBlockStyleToolbarColorClicked() {
        Timber.d("onBlockStyleToolbarColorClicked, ")
        controlPanelInteractor.onEvent(ControlPanelMachine.Event.StylingToolbar.OnColorClicked)
    }

    private fun proceedUpdateBlockStyle(
        targets: List<String>,
        uiBlock: UiBlock,
        action: (() -> Unit)? = null,
        errorAction: (() -> Unit)? = null
    ) {
        when (uiBlock) {
            UiBlock.TEXT, UiBlock.HEADER_ONE,
            UiBlock.HEADER_TWO, UiBlock.HEADER_THREE,
            UiBlock.HIGHLIGHTED, UiBlock.CHECKBOX,
            UiBlock.BULLETED, UiBlock.NUMBERED,
            UiBlock.TOGGLE, UiBlock.CODE -> {
                action?.invoke()
                proceedWithTurnIntoStyle(targets, uiBlock.style())
            }
            UiBlock.PAGE -> {
                action?.invoke()
                proceedWithTurningIntoDocument(targets)
            }
            UiBlock.LINE_DIVIDER -> {
                action?.invoke()
                proceedUpdateDividerStyle(targets, Content.Divider.Style.LINE)
            }
            UiBlock.THREE_DOTS -> {
                action?.invoke()
                proceedUpdateDividerStyle(targets, Content.Divider.Style.DOTS)
            }
            UiBlock.LINK_TO_OBJECT -> errorAction?.invoke()
            UiBlock.FILE -> errorAction?.invoke()
            UiBlock.IMAGE -> errorAction?.invoke()
            UiBlock.VIDEO -> errorAction?.invoke()
            UiBlock.BOOKMARK -> errorAction?.invoke()
            else -> Timber.e("Unexpected style update")
        }
    }

    private fun proceedWithTurnIntoStyle(
        targets: List<String>,
        style: Content.Text.Style
    ) {
        viewModelScope.launch {
            orchestrator.proxies.intents.send(
                Intent.Text.TurnInto(
                    context = context,
                    targets = targets,
                    style = style
                )
            )
        }
    }

    private fun proceedWithUpdateTextStyle(
        targets: List<String>,
        style: Content.Text.Style
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

    private fun proceedUpdateDividerStyle(targets: List<String>, style: Content.Divider.Style) {
        viewModelScope.launch {
            orchestrator.proxies.intents.send(
                Intent.Divider.UpdateStyle(
                    context = context,
                    targets = targets,
                    style = style
                )
            )
        }
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

    private fun addDividerBlock(style: Content.Divider.Style) {

        val focused = blocks.first { it.id == orchestrator.stores.focus.current().id }
        val content = focused.content
        val prototype = when (style) {
            Content.Divider.Style.LINE -> Prototype.DividerLine
            Content.Divider.Style.DOTS -> Prototype.DividerDots
        }

        if (content is Content.Text && content.text.isEmpty()) {
            viewModelScope.launch {
                orchestrator.proxies.intents.send(
                    Intent.CRUD.Replace(
                        context = context,
                        target = focused.id,
                        prototype = prototype
                    )
                )
            }
        } else {

            val position: Position

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
                        prototype = prototype
                    )
                )
            }
        }

    }

    fun onAddDividerBlockClicked(style: Content.Divider.Style) {
        Timber.d("onAddDividerBlockClicked, style:[$style]")
        addDividerBlock(style)
        controlPanelInteractor.onEvent(ControlPanelMachine.Event.OnAddBlockToolbarOptionSelected)
    }

    fun onOutsideClicked() {
        Timber.d("onOutsideClicked, ")
        if (mode is EditorMode.Styling) {
            onExitBlockStyleToolbarClicked()
            return
        }

        val restrictions = orchestrator.stores.objectRestrictions.current()
        if (restrictions.contains(ObjectRestriction.BLOCKS)) {
            Timber.d("Object contains restriction BLOCKS, can't create blocks")
            return
        }

        val root = blocks.find { it.id == context } ?: return

        if (root.children.isEmpty()) {
            addNewBlockAtTheEnd()
        } else {
            val last = blocks.first { it.id == root.children.last() }
            when (val content = last.content) {
                is Content.Text -> {
                    when {
                        content.style == Content.Text.Style.TITLE -> addNewBlockAtTheEnd()
                        content.text.isNotEmpty() -> addNewBlockAtTheEnd()
                        content.text.isEmpty() -> {
                            val stores = orchestrator.stores
                            if (stores.focus.current().isEmpty) {
                                val focus = Editor.Focus(id = last.id, cursor = null)
                                viewModelScope.launch { orchestrator.stores.focus.update(focus) }
                                viewModelScope.launch { refresh() }
                            } else {
                                Timber.d("Outside click is ignored because focus is not empty")
                            }
                        }
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
                is Content.Layout -> {
                    addNewBlockAtTheEnd()
                }
                is Content.RelationBlock -> {
                    addNewBlockAtTheEnd()
                }
                else -> {
                    Timber.d("Outside-click has been ignored.")
                }
            }
        }
    }

    //Todo this method need refactoring
    fun onHideKeyboardClicked() {
        Timber.d("onHideKeyboardClicked, ")
        controlPanelInteractor.onEvent(ControlPanelMachine.Event.OnClearFocusClicked)
        viewModelScope.launch { orchestrator.stores.focus.update(Editor.Focus.empty()) }
        views.onEach { if (it is Focusable) it.isFocused = false }
        viewModelScope.launch { renderCommand.send(Unit) }
        viewModelScope.sendEvent(
            analytics = analytics,
            eventName = EventsDictionary.BTN_HIDE_KEYBOARD
        )
    }

    private fun proceedWithClearingFocus() {
        viewModelScope.launch {
            orchestrator.stores.focus.update(Editor.Focus.empty())
            refresh()
        }
        controlPanelInteractor.onEvent(ControlPanelMachine.Event.OnClearFocusClicked)
    }

    private suspend fun refresh() {
        if (BuildConfig.DEBUG) {
            Timber.d("----------Refreshing Blocks---------------------\n$blocks")
            Timber.d("----------Finished Refreshing Blocks------------")
        }
        renderizePipeline.send(blocks)
    }

    private fun onPageClicked(target: String) {
        val view = views.find { it.id == target }
        if (view is BlockView.Loadable && !view.isLoading) {
            proceedWithOpeningPage(
                target = blocks.first { it.id == target }.content<Content.Link>().target
            )
        } else {
            _toasts.offer("Still syncing...")
        }
    }

    private fun onMentionClicked(target: String) {
        proceedWithClearingFocus()
        proceedWithOpeningPage(target = target)
    }

    fun onAddNewObjectClicked(type: String, layout: ObjectType.Layout) {
        controlPanelInteractor.onEvent(ControlPanelMachine.Event.OnAddBlockToolbarOptionSelected)

        val position: Position

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

        val params = CreateObject.Params(
            context = context,
            position = position,
            target = target,
            type = type,
            layout = layout
        )

        val startTime = System.currentTimeMillis()

        viewModelScope.launch {
            createObject(
                params = params
            ).proceed(
                failure = { Timber.e(it, "Error while creating new object with params: $params") },
                success = { result ->
                    val middleTime = System.currentTimeMillis()
                    analytics.registerEvent(
                        EventAnalytics.Anytype(
                            name = OBJECT_CREATE,
                            props = Props(mapOf(PROP_STYLE to Content.Page.Style.EMPTY)),
                            duration = EventAnalytics.Duration(
                                start = startTime,
                                middleware = middleTime,
                                render = middleTime
                            )
                        )
                    )
                    orchestrator.proxies.payloads.send(result.payload)
                    proceedWithOpeningPage(result.target)
                }
            )
        }
    }

    fun onAddNewPageClicked() {
        Timber.d("onAddNewPageClicked, ")
        controlPanelInteractor.onEvent(ControlPanelMachine.Event.OnAddBlockToolbarOptionSelected)

        val position: Position

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
            target = target
        )

        val startTime = System.currentTimeMillis()

        viewModelScope.launch {
            createDocument(
                params = params
            ).proceed(
                failure = { Timber.e(it, "Error while creating new page with params: $params") },
                success = { result ->
                    val middleTime = System.currentTimeMillis()
                    analytics.registerEvent(
                        EventAnalytics.Anytype(
                            name = PAGE_CREATE,
                            props = Props(mapOf(PROP_STYLE to Content.Page.Style.EMPTY)),
                            duration = EventAnalytics.Duration(
                                start = startTime,
                                middleware = middleTime,
                                render = middleTime
                            )
                        )
                    )
                    orchestrator.proxies.payloads.send(result.payload)
                    proceedWithOpeningPage(result.target)
                }
            )
        }
    }

    fun onArchiveThisObjectClicked() {
        proceedWithChangingIsArchivedStatus(isArchived = true)
    }

    fun onRestoreThisObjectFromArchive() {
        proceedWithChangingIsArchivedStatus(isArchived = false)
    }

    private fun proceedWithChangingIsArchivedStatus(isArchived: Boolean) {
        Timber.d("onArchiveThisPageClicked, ")
        dispatch(command = Command.CloseKeyboard)
        viewModelScope.launch {
            archiveDocument(
                ArchiveDocument.Params(
                    context = context,
                    targets = listOf(context),
                    isArchived = isArchived
                )
            ).proceed(
                failure = { Timber.e(it, "Error while archiving page") },
                success = { proceedWithExitingBack() }
            )
        }
    }

    fun onAddCoverClicked() {
        Timber.d("onAddCoverClicked, ")
        dispatch(Command.OpenCoverGallery(context))
    }

    fun onLayoutClicked() {
        Timber.d("onLayoutClicked, ")
        dispatch(Command.OpenObjectLayout(context))
    }

    fun onDownloadClicked() {
        Timber.d("onDownloadClicked, ")
        val block = blocks.firstOrNull { it.content is Content.File }
        if (block != null) {
            dispatch(Command.RequestDownloadPermission(block.id))
        } else {
            Timber.e("onDownloadClicked, file not found in object")
        }
    }

    fun onLayoutDialogDismissed() {
        Timber.d("onLayoutDialogDismissed, ")
        proceedWithOpeningObjectMenu()
    }

    fun onDocCoverImagePicked(path: String) {
        Timber.d("onDocCoverImagePicked, path:[$path]")
        viewModelScope.launch {
            setDocCoverImage(
                SetDocCoverImage.Params.FromPath(
                    context = context,
                    path = path
                )
            ).proceed(
                failure = { Timber.e(it, "Error while setting doc cover image") },
                success = { orchestrator.proxies.payloads.send(it) }
            )
        }
    }

    fun onDocCoverImageSelected(hash: String) {
        Timber.d("onDocCoverImageSelected, hash:[$hash]")
        viewModelScope.launch {
            setDocCoverImage(
                SetDocCoverImage.Params.FromHash(
                    context = context,
                    hash = hash
                )
            ).proceed(
                failure = { Timber.e(it, "Error while setting doc cover image") },
                success = {
                    orchestrator.proxies.payloads.send(it)
                    detailModificationManager.setDocCoverImage(
                        target = context,
                        hash = hash
                    )
                }
            )
        }
    }

    fun onRemoveCover() {
        Timber.d("onRemoveCover, ")
        viewModelScope.launch {
            removeDocCover(
                RemoveDocCover.Params(
                    ctx = context
                )
            ).proceed(
                failure = { Timber.e(it, "Error while removing doc cover") },
                success = {
                    orchestrator.proxies.payloads.send(it)
                    detailModificationManager.removeDocCover(context)
                }
            )
        }
    }

    fun onAddBookmarkBlockClicked() {
        Timber.d("onAddBookmarkBlockClicked, ")

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

            val position: Position

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
        Timber.d("onAddBookmarkUrl, target:[$target] url:[$url]")
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

    private fun onBookmarkPlaceholderClicked(target: String) {
        dispatch(
            command = Command.OpenBookmarkSetter(
                context = context,
                target = target
            )
        )
        viewModelScope.sendEvent(
            analytics = analytics,
            eventName = POPUP_BOOKMARK
        )
    }

    private fun onBookmarkClicked(view: BlockView.Media.Bookmark) =
        dispatch(command = Command.Browse(view.url))

    private fun onFailedBookmarkClicked(view: BlockView.Error.Bookmark) =
        dispatch(command = Command.Browse(view.url))

    fun onTitleTextInputClicked() {
        Timber.d("onTitleTextInputClicked, ")
        if (mode is EditorMode.Styling) {
            onExitBlockStyleToolbarClicked()
            return
        }
        controlPanelInteractor.onEvent(ControlPanelMachine.Event.OnTextInputClicked)
    }

    fun onTextInputClicked(target: Id) {
        Timber.d("onTextInputClicked, target:[$target]")
        when (mode) {
            is EditorMode.Select -> {
                onBlockMultiSelectClicked(target)
            }
            is EditorMode.Styling -> {
                onExitBlockStyleToolbarClicked()
            }
            else -> {
                controlPanelInteractor.onEvent(ControlPanelMachine.Event.OnTextInputClicked)
            }
        }
    }

    private fun onBlockMultiSelectClicked(target: Id) {
        proceedWithTogglingSelection(target)
    }

    private fun proceedWithTogglingSelection(target: Id) {
        (stateData.value as? ViewState.Success)?.let { state ->

            var allow = true

            val parent = blocks.find { it.children.contains(target) }

            if (parent != null && parent.id != context) {
                if (isSelected(parent.id)) allow = false
            }

            if (!allow) return

            toggleSelection(target)

            val descendants = blocks.asMap().descendants(parent = target)

            if (isSelected(target)) {
                descendants.forEach { child -> select(child) }
            } else {
                descendants.forEach { child -> unselect(child) }
            }

            onMultiSelectModeBlockClicked()

            val update = state.blocks.map { view ->
                if (view.id == target || descendants.contains(view.id))
                    view.updateSelection(newSelection = isSelected(target))
                else
                    view
            }

            stateData.postValue(ViewState.Success(update))
        }
    }

    private fun proceedWithSAMQuickStartSelection(target: Id) {
        (stateData.value as? ViewState.Success)?.let { state ->

            var allow = true

            val parent = blocks.find { it.children.contains(target) }

            if (parent != null && parent.id != context) {
                if (isSelected(parent.id)) allow = false
            }

            if (!allow) return

            toggleSelection(target)

            val descendants = blocks.asMap().descendants(parent = target)

            if (isSelected(target)) {
                descendants.forEach { child -> select(child) }
            } else {
                descendants.forEach { child -> unselect(child) }
            }

            val update = state.blocks.map { view ->
                if (view.id == target || descendants.contains(view.id))
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
        Timber.d("onPaste, range:[$range]")
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

        Timber.d("onApplyScrollAndMove, target:[$target] ratio:[$ratio]")

        val ordering = views.mapIndexed { index, view -> view.id to index }.toMap()

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
            if (position == Position.INNER) {
                _toasts.offer(CANNOT_BE_DROPPED_INSIDE_ITSELF_ERROR)
            } else if (selected.size == 1) {
                _toasts.offer(CANNOT_MOVE_BLOCK_ON_SAME_POSITION)
            }
            return
        }

        if (selected.contains(parent)) {
            _toasts.offer(CANNOT_MOVE_PARENT_INTO_CHILD)
            return
        }

        if (position == Position.INNER) {

            if (!targetBlock.supportNesting()) {
                _toasts.offer(CANNOT_BE_PARENT_ERROR)
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

            mode = if (controlPanelViewState.value?.multiSelect?.isQuickScrollAndMoveMode == true) {
                EditorMode.Edit
            } else {
                EditorMode.Select
            }

            controlPanelInteractor.onEvent(ControlPanelMachine.Event.SAM.OnApply)

            viewModelScope.launch {
                orchestrator.proxies.intents.send(
                    Intent.Document.Move(
                        context = context,
                        target = moveTarget,
                        targetContext = targetContext,
                        blocks = (selected - exclude).sortedBy { id -> ordering[id] },
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

            mode = if (controlPanelViewState.value?.multiSelect?.isQuickScrollAndMoveMode == true) {
                EditorMode.Edit
            } else {
                EditorMode.Select
            }

            controlPanelInteractor.onEvent(ControlPanelMachine.Event.SAM.OnApply)

            viewModelScope.launch {
                orchestrator.proxies.intents.send(
                    Intent.Document.Move(
                        context = context,
                        target = moveTarget,
                        targetContext = targetContext,
                        blocks = (selected - exclude).sortedBy { id -> ordering[id] },
                        position = position
                    )
                )
            }
        }
    }

    fun onCopy(
        range: IntRange?
    ) {
        Timber.d("onCopy, ")
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

    fun onClickListener(clicked: ListenerType) {
        Timber.d("onClickListener, clicked:[$clicked]")
        if (mode is EditorMode.Styling) {
            onExitBlockStyleToolbarClicked()
            return
        }
        when (clicked) {
            is ListenerType.Bookmark.View -> {
                when (mode) {
                    EditorMode.Edit -> onBookmarkClicked(clicked.item)
                    EditorMode.Select -> onBlockMultiSelectClicked(clicked.item.id)
                    else -> Unit
                }
            }
            is ListenerType.Bookmark.Placeholder -> {
                when (mode) {
                    EditorMode.Edit -> onBookmarkPlaceholderClicked(clicked.target)
                    EditorMode.Select -> onBlockMultiSelectClicked(clicked.target)
                    else -> Unit
                }
            }
            is ListenerType.Bookmark.Error -> {
                when (mode) {
                    EditorMode.Edit -> onFailedBookmarkClicked(clicked.item)
                    EditorMode.Select -> onBlockMultiSelectClicked(clicked.item.id)
                    else -> Unit
                }
            }
            is ListenerType.File.View -> {
                when (mode) {
                    EditorMode.Edit -> onFileClicked(clicked.target)
                    EditorMode.Select -> onBlockMultiSelectClicked(clicked.target)
                    else -> Unit
                }
            }
            is ListenerType.File.Placeholder -> {
                when (mode) {
                    EditorMode.Edit -> onAddLocalFileClicked(clicked.target)
                    EditorMode.Select -> onBlockMultiSelectClicked(clicked.target)
                    else -> Unit
                }
            }
            is ListenerType.File.Error -> {
                when (mode) {
                    EditorMode.Edit -> onAddLocalFileClicked(clicked.target)
                    EditorMode.Select -> onBlockMultiSelectClicked(clicked.target)
                    else -> Unit
                }
            }
            is ListenerType.File.Upload -> {
                when (mode) {
                    EditorMode.Edit -> Unit
                    EditorMode.Select -> onBlockMultiSelectClicked(clicked.target)
                    else -> Unit
                }
            }
            is ListenerType.Picture.View -> {
                when (mode) {
                    EditorMode.Edit -> {
                        val target = blocks.find { it.id == clicked.target }
                        if (target != null) {
                            val content = target.content
                            check(content is Content.File)
                            dispatch(
                                Command.OpenFullScreenImage(
                                    target = clicked.target,
                                    url = urlBuilder.original(content.hash)
                                )
                            )
                        } else {
                            Timber.e("Could not find target for picture")
                        }
                    }
                    EditorMode.Select -> onBlockMultiSelectClicked(clicked.target)
                    else -> Unit
                }
            }
            is ListenerType.Picture.Placeholder -> {
                when (mode) {
                    EditorMode.Edit -> onAddLocalPictureClicked(clicked.target)
                    EditorMode.Select -> onBlockMultiSelectClicked(clicked.target)
                    else -> Unit
                }
            }
            is ListenerType.Picture.Error -> {
                when (mode) {
                    EditorMode.Edit -> Unit
                    EditorMode.Select -> onBlockMultiSelectClicked(clicked.target)
                    else -> Unit
                }
            }
            is ListenerType.Picture.Upload -> {
                when (mode) {
                    EditorMode.Edit -> Unit
                    EditorMode.Select -> onBlockMultiSelectClicked(clicked.target)
                    else -> Unit
                }
            }
            is ListenerType.Video.View -> {
                when (mode) {
                    EditorMode.Edit -> Unit
                    EditorMode.Select -> onBlockMultiSelectClicked(clicked.target)
                    else -> Unit
                }
            }
            is ListenerType.Video.Placeholder -> {
                when (mode) {
                    EditorMode.Edit -> onAddLocalVideoClicked(clicked.target)
                    EditorMode.Select -> onBlockMultiSelectClicked(clicked.target)
                    else -> Unit
                }
            }
            is ListenerType.Video.Error -> {
                when (mode) {
                    EditorMode.Edit -> Unit
                    EditorMode.Select -> onBlockMultiSelectClicked(clicked.target)
                    else -> Unit
                }
            }
            is ListenerType.Video.Upload -> {
                when (mode) {
                    EditorMode.Edit -> Unit
                    EditorMode.Select -> onBlockMultiSelectClicked(clicked.target)
                    else -> Unit
                }
            }
            is ListenerType.LongClick -> {
                when (mode) {
                    EditorMode.Edit -> onBlockLongPressedClicked(clicked.target, clicked.dimensions)
                    EditorMode.Select -> Unit
                    else -> Unit
                }
            }
            is ListenerType.Page -> {
                when (mode) {
                    EditorMode.Edit -> onPageClicked(clicked.target)
                    EditorMode.Select -> onBlockMultiSelectClicked(clicked.target)
                    else -> Unit
                }
            }
            is ListenerType.Mention -> {
                when (mode) {
                    EditorMode.Edit -> onMentionClicked(clicked.target)
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
                    EditorMode.Select -> onBlockMultiSelectClicked(clicked.target)
                    else -> Unit
                }
            }
            is ListenerType.Code.SelectLanguage -> {
                when (mode) {
                    EditorMode.Edit -> dispatch(Command.Dialog.SelectLanguage(clicked.target))
                    else -> Unit
                }
            }
            is ListenerType.Relation.Placeholder -> {
                when (mode) {
                    EditorMode.Edit -> dispatch(
                        Command.OpenObjectRelationScreen.Add(
                            ctx = context,
                            target = clicked.target
                        )
                    )
                    else -> onBlockMultiSelectClicked(clicked.target)
                }
            }
            is ListenerType.Relation.Related -> {
                val restrictions = orchestrator.stores.objectRestrictions.current()
                if (restrictions.contains(ObjectRestriction.RELATIONS)) {
                    _toasts.offer(NOT_ALLOWED_FOR_RELATION)
                    Timber.d("No interaction allowed with this relation")
                    return
                }
                when (mode) {
                    EditorMode.Edit -> {
                        val relationId = (clicked.value as BlockView.Relation.Related).view.relationId
                        val relation = orchestrator.stores.relations.current().first { it.key == relationId }
                        if (relation.isReadOnly) {
                            _toasts.offer(NOT_ALLOWED_FOR_RELATION)
                            Timber.d("No interaction allowed with this relation")
                            return
                        }
                        when (relation.format) {
                            Relation.Format.SHORT_TEXT,
                            Relation.Format.LONG_TEXT,
                            Relation.Format.URL,
                            Relation.Format.PHONE,
                            Relation.Format.NUMBER,
                            Relation.Format.EMAIL -> {
                                dispatch(
                                    Command.OpenObjectRelationScreen.Value.Text(
                                        ctx = context,
                                        target = context,
                                        relation = relationId
                                    )
                                )
                            }
                            Relation.Format.CHECKBOX -> {
                                proceedWithTogglingBlockRelationCheckbox(clicked.value, relationId)
                            }
                            Relation.Format.DATE -> {
                                dispatch(
                                    Command.OpenObjectRelationScreen.Value.Date(
                                        ctx = context,
                                        target = context,
                                        relation = relationId
                                    )
                                )
                            }
                            else -> {
                                dispatch(
                                    Command.OpenObjectRelationScreen.Value.Default(
                                        ctx = context,
                                        target = context,
                                        relation = relationId
                                    )
                                )
                            }
                        }
                    }
                    else -> onBlockMultiSelectClicked(clicked.value.id)
                }
            }
            is ListenerType.Relation.ObjectType -> {
                val restrictions = orchestrator.stores.objectRestrictions.current()
                if (restrictions.contains(ObjectRestriction.TYPE_CHANGE)) {
                    _toasts.offer(NOT_ALLOWED_FOR_OBJECT)
                    Timber.d("No interaction allowed with this object type")
                    return
                }
                val block = blocks.firstOrNull { it.id == context }
                val smartBlockType = if (block?.content is Content.Smart) {
                    block.content<Content.Smart>().type
                } else {
                    SmartBlockType.PAGE
                }
                dispatch(
                    Command.OpenChangeObjectTypeScreen(
                        ctx = context,
                        smartBlockType = smartBlockType
                    )
                )
            }
        }
    }

    private fun proceedWithTogglingBlockRelationCheckbox(
        value: BlockView.Relation.Related,
        relation: Id
    ) {
        viewModelScope.launch {
            val view = value.view as DocumentRelationView.Checkbox
            updateDetail(
                UpdateDetail.Params(
                    ctx = context,
                    key = relation,
                    value = !view.isChecked
                )
            ).process(
                success = { dispatcher.send(it) },
                failure = { Timber.e(it, "Error while updating relation values") }
            )
        }
    }

    fun onPlusButtonPressed() {
        Timber.d("onPlusButtonPressed, ")
        val startTime = System.currentTimeMillis()
        createPage(
            scope = viewModelScope,
            params = CreatePage.Params.insideDashboard()
        ) { result ->
            result.either(
                fnL = { Timber.e(it, "Error while creating a new page on home dashboard") },
                fnR = { id ->
                    val middle = System.currentTimeMillis()
                    viewModelScope.sendEvent(
                        analytics = analytics,
                        startTime = startTime,
                        middleTime = middle,
                        renderTime = middle,
                        eventName = PAGE_CREATE,
                        props = Props(mapOf(PROP_STYLE to Content.Page.Style.EMPTY))
                    )
                    proceedWithOpeningPage(id)
                }
            )
        }
    }

    fun onProceedWithFilePath(filePath: String?) {
        Timber.d("onProceedWithFilePath, filePath:[$filePath]")
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
        Timber.d("onPageIconClicked, ")
        val restrictions = orchestrator.stores.objectRestrictions.current()
        val isDetailsAllowed = restrictions.none { it == ObjectRestriction.DETAILS }
        if (isDetailsAllowed) {
            controlPanelInteractor.onEvent(ControlPanelMachine.Event.OnDocumentIconClicked)
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
        } else {
            _toasts.offer(NOT_ALLOWED_FOR_OBJECT)
        }
        viewModelScope.sendEvent(
            analytics = analytics,
            eventName = POPUP_DOCUMENT_ICON_MENU
        )
    }

    fun onProfileIconClicked() {
        Timber.d("onProfileIconClicked, ")
        controlPanelInteractor.onEvent(ControlPanelMachine.Event.OnDocumentIconClicked)
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
        viewModelScope.sendEvent(
            analytics = analytics,
            eventName = POPUP_PROFILE_ICON_MENU
        )
    }

    private fun onFileClicked(id: String) {
        dispatch(Command.RequestDownloadPermission(id))
    }

    fun startDownloadingFile(id: String) {

        Timber.d("startDownloadingFile, id:[$id]")

        _toasts.offer("Downloading file in background...")

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

        Timber.d("onPageSearchClicked, ")

        viewModelScope.sendEvent(
            analytics = analytics,
            eventName = EventsDictionary.SCREEN_SEARCH
        )
        navigation.postValue(EventWrapper(AppNavigation.Command.OpenPageSearch))
    }

    fun onMentionEvent(mentionEvent: MentionEvent) {
        Timber.d("onMentionEvent, mentionEvent:[$mentionEvent]")
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
                viewModelScope.sendEvent(
                    analytics = analytics,
                    eventName = POPUP_MENTION_MENU
                )
            }
            MentionEvent.MentionSuggestStop -> {
                mentionFrom = -1
                controlPanelInteractor.onEvent(
                    ControlPanelMachine.Event.Mentions.OnStop
                )
            }
        }
    }

    fun onAddMentionNewPageClicked(name: String) {
        Timber.d("onAddMentionNewPageClicked, name:[$name]")

        val params = CreateNewDocument.Params(
            name = name.removePrefix(Mention.MENTION_PREFIX)
        )

        val startTime = System.currentTimeMillis()

        viewModelScope.launch {
            createNewDocument(
                params = params
            ).proceed(
                failure = {
                    Timber.e(it, "Error while creating new page with params: $params")
                },
                success = { result ->
                    val middleTime = System.currentTimeMillis()
                    onCreateMentionInText(
                        mention = Mention(
                            id = result.id,
                            title = result.name.getMentionName(MENTION_TITLE_EMPTY),
                            emoji = result.emoji,
                            image = null
                        ),
                        mentionTrigger = name
                    )
                    analytics.registerEvent(
                        EventAnalytics.Anytype(
                            name = PAGE_MENTION_CREATE,
                            props = Props(mapOf(PROP_STYLE to Content.Page.Style.EMPTY)),
                            duration = EventAnalytics.Duration(
                                start = startTime,
                                middleware = middleTime,
                                render = middleTime
                            )
                        )
                    )
                }
            )
        }
    }

    fun onMentionSuggestClick(mention: Mention, mentionTrigger: String) {
        Timber.d("onMentionSuggestClick, mention:[$mention] mentionTrigger:[$mentionTrigger]")
        onCreateMentionInText(mention, mentionTrigger)
    }

    fun onCreateMentionInText(mention: Mention, mentionTrigger: String) {
        Timber.d("onCreateMentionInText, mention:[$mention], mentionTrigger:[$mentionFrom]")

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
        viewModelScope.sendEvent(
            analytics = analytics,
            eventName = EventsDictionary.SCREEN_DOCUMENT
        )
        navigate(EventWrapper(AppNavigation.Command.OpenPage(target)))
    }

    /**
     * Return true, when mention menu is closed, and we need absorb back button click
     */
    fun onBackPressedCallback(): Boolean {
        Timber.d("onBackPressedCallback, ")
        return controlPanelViewState.value?.let { state ->
            val isVisible = state.mentionToolbar.isVisible
            val isSlashWidgetVisible = state.slashWidget.isVisible
            if (isVisible) {
                onMentionEvent(MentionEvent.MentionSuggestStop)
                return true
            }
            if (isSlashWidgetVisible) {
                controlPanelInteractor.onEvent(ControlPanelMachine.Event.Slash.OnStop)
                return true
            }
            return false
        } ?: run { false }
    }

    fun onSelectProgrammingLanguageClicked(target: Id, key: String) {
        Timber.d("onSelectProgrammingLanguageClicked, target:[$target] key:[$key]")
        viewModelScope.launch {
            orchestrator.proxies.intents.send(
                Intent.CRUD.UpdateFields(
                    context = context,
                    fields = listOf(
                        Pair(
                            target,
                            Block.Fields(
                                mapOf("lang" to key)
                            )
                        )
                    )
                )
            )
        }
    }

    fun onRelationTextValueChanged(
        ctx: Id,
        value: Any?,
        relationId: Id
    ) {
        Timber.d("onRelationTextValueChanged, ctx:[$ctx] value:[$value] relationId:[$relationId]")
        viewModelScope.launch {
            updateDetail(
                UpdateDetail.Params(
                    ctx = ctx,
                    key = relationId,
                    value = value
                )
            ).process(
                success = { dispatcher.send(it) },
                failure = { Timber.e(it, "Error while updating relation values") }
            )
        }
    }

    fun onObjectTypeChanged(id: Id) {
        Timber.d("onObjectTypeChanged, typeId:[$id]")
        viewModelScope.launch {
            orchestrator.proxies.intents.send(
                Intent.Document.SetObjectType(
                    context = context,
                    typeId = id
                )
            )
        }
    }

    companion object {
        const val NO_SEARCH_RESULT_POSITION = -1
        const val EMPTY_TEXT = ""
        const val EMPTY_CONTEXT = ""
        const val EMPTY_FOCUS_ID = ""
        const val TEXT_CHANGES_DEBOUNCE_DURATION = 500L
        const val DELAY_REFRESH_DOCUMENT_TO_ENTER_MULTI_SELECT_MODE = 150L
        const val DELAY_REFRESH_DOCUMENT_ON_EXIT_MULTI_SELECT_MODE = 300L
        const val INITIAL_INDENT = 0
        const val CANNOT_MOVE_BLOCK_ON_SAME_POSITION = "Selected block is already on the position"
        const val CANNOT_BE_DROPPED_INSIDE_ITSELF_ERROR = "A block cannot be moved inside itself."
        const val CANNOT_BE_PARENT_ERROR = "This block does not support nesting."
        const val CANNOT_MOVE_PARENT_INTO_CHILD =
            "Cannot move parent into child. Please, check selected blocks."
        const val MENTION_TITLE_EMPTY = "Untitled"

        const val CANNOT_OPEN_ACTION_MENU_FOR_TITLE_ERROR =
            "Opening action menu for title currently not supported"
        const val CANNOT_OPEN_STYLE_PANEL_FOR_TITLE_ERROR =
            "Opening style panel for title currently not supported"
        const val CANNOT_OPEN_STYLE_PANEL_FOR_CODE_BLOCK_ERROR =
            "Opening style panel for code block currently not supported"
        const val FLAVOUR_EXPERIMENTAL = "experimental"

        const val ERROR_UNSUPPORTED_BEHAVIOR = "Currently unsupported behavior."
        const val NOT_ALLOWED_FOR_OBJECT = "Not allowed for this object"
        const val NOT_ALLOWED_FOR_RELATION = "Not allowed for this relation"
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

        Timber.d("onCleared, ")
    }

    fun onStop() {
        Timber.d("onStop, ")
        jobs.apply {
            forEach { it.cancel() }
            clear()
        }
    }

    enum class Session { IDLE, OPEN, ERROR }

    //region SLASH WIDGET
    fun onStartSlashWidgetClicked() {
        dispatch(Command.AddSlashWidgetTriggerToFocusedBlock)
    }

    fun onSlashItemClicked(item: SlashItem) {
        Timber.d("onSlashItemClicked, item:[$item]")
        val target = orchestrator.stores.focus.current()
        if (!target.isEmpty) {
            proceedWithSlashItem(item, target.id)
        } else {
            Timber.e("Slash Widget Error, target is empty")
        }
    }

    fun onSlashTextWatcherEvent(event: SlashEvent) {
        Timber.d("onSlashTextWatcherEvent, event:[$event]")
        when (event) {
            is SlashEvent.Start -> {
                slashStartIndex = event.slashStart
                filterSearchEmptyCount = 0
                val panelEvent = ControlPanelMachine.Event.Slash.OnStart(
                    cursorCoordinate = event.cursorCoordinate,
                    slashFrom = event.slashStart
                )
                controlPanelInteractor.onEvent(panelEvent)
            }
            is SlashEvent.Filter -> {
                slashFilter = event.filter.toString()
                slashViewType = event.viewType
                if (event.filter.isEmpty() || event.filter.first() != SLASH_CHAR) {
                    val widgetState = SlashWidgetState.UpdateItems.empty()
                    val panelEvent = ControlPanelMachine.Event.Slash.OnFilterChange(
                        widgetState = widgetState
                    )
                    controlPanelInteractor.onEvent(panelEvent)
                    return
                }
                if (event.filter.length == 1) {
                    val mainItems = if (BuildConfig.FLAVOR == FLAVOUR_EXPERIMENTAL) {
                        SlashExtensions.getExperimentalSlashWidgetMainItems()
                    } else {
                        SlashExtensions.getStableSlashWidgetMainItems()
                    }
                    val widgetState = SlashWidgetState.UpdateItems.empty()
                        .copy(mainItems = mainItems)
                    val panelEvent = ControlPanelMachine.Event.Slash.OnFilterChange(
                        widgetState = widgetState
                    )
                    controlPanelInteractor.onEvent(panelEvent)
                    return
                }

                if (BuildConfig.FLAVOR == FLAVOUR_EXPERIMENTAL) {
                    getObjectTypes { objectTypes ->
                        getRelations { relations ->
                            val widgetState = SlashExtensions.getUpdatedSlashWidgetState(
                                text = event.filter,
                                objectTypes = objectTypes.toView(),
                                relations = relations,
                                viewType = slashViewType
                            )
                            incFilterSearchEmptyCount(widgetState)
                            val panelEvent = if (filterSearchEmptyCount == SLASH_EMPTY_SEARCH_MAX) {
                                filterSearchEmptyCount = 0
                                slashStartIndex = 0
                                slashFilter = ""
                                slashViewType = 0
                                ControlPanelMachine.Event.Slash.OnStop
                            } else {
                                ControlPanelMachine.Event.Slash.OnFilterChange(widgetState)
                            }
                            controlPanelInteractor.onEvent(panelEvent)
                        }
                    }
                } else {
                    getObjectTypes { objectTypes ->
                        val filter = objectTypes.filter { it.url == ObjectType.PAGE_URL }
                        val widgetState = SlashExtensions.getUpdatedSlashWidgetState(
                            text = event.filter,
                            objectTypes = filter.toView(),
                            relations = emptyList(),
                            viewType = slashViewType
                        )
                        incFilterSearchEmptyCount(widgetState)
                        val panelEvent = if (filterSearchEmptyCount == SLASH_EMPTY_SEARCH_MAX) {
                            filterSearchEmptyCount = 0
                            slashStartIndex = 0
                            slashFilter = ""
                            slashViewType = 0
                            ControlPanelMachine.Event.Slash.OnStop
                        } else {
                            ControlPanelMachine.Event.Slash.OnFilterChange(widgetState)
                        }
                        controlPanelInteractor.onEvent(panelEvent)
                    }
                }

            }
            SlashEvent.Stop -> {
                slashStartIndex = 0
                slashFilter = ""
                slashViewType = 0
                filterSearchEmptyCount = 0
                val panelEvent = ControlPanelMachine.Event.Slash.OnStop
                controlPanelInteractor.onEvent(panelEvent)
            }
        }
    }

    private fun proceedWithSlashItem(item: SlashItem, targetId: Id) {
        when (item) {
            is SlashItem.Main.Style -> {
                val items =
                    listOf(SlashItem.Subheader.StyleWithBack) + getSlashWidgetStyleItems(
                        slashViewType
                    )
                onSlashWidgetStateChanged(
                    SlashWidgetState.UpdateItems.empty().copy(
                        styleItems = items
                    )
                )
            }
            is SlashItem.Main.Media -> {
                val items =
                    listOf(SlashItem.Subheader.MediaWithBack) + SlashExtensions.getSlashWidgetMediaItems()
                onSlashWidgetStateChanged(
                    SlashWidgetState.UpdateItems.empty().copy(
                        mediaItems = items
                    )
                )
            }
            is SlashItem.Main.Relations -> {
                getRelations { proceedWithRelations(it) }
            }
            is SlashItem.Main.Objects -> {
                getObjectTypes { proceedWithObjectTypes(it) }
            }
            is SlashItem.Main.Other -> {
                val items =
                    listOf(SlashItem.Subheader.OtherWithBack) + SlashExtensions.getSlashWidgetOtherItems()
                onSlashWidgetStateChanged(
                    SlashWidgetState.UpdateItems.empty().copy(
                        otherItems = items
                    )
                )
            }
            is SlashItem.Main.Actions -> {
                val items =
                    listOf(SlashItem.Subheader.ActionsWithBack) + SlashExtensions.getSlashWidgetActionItems()
                onSlashWidgetStateChanged(
                    SlashWidgetState.UpdateItems.empty().copy(
                        actionsItems = items
                    )
                )
            }
            is SlashItem.Main.Alignment -> {
                val items =
                    listOf(SlashItem.Subheader.AlignmentWithBack) + getSlashWidgetAlignmentItems(
                        slashViewType
                    )
                onSlashWidgetStateChanged(
                    SlashWidgetState.UpdateItems.empty().copy(
                        alignmentItems = items
                    )
                )
            }
            is SlashItem.Main.Color -> {
                val block = blocks.first { it.id == targetId }
                val blockColor = block.content.asText().color
                val color = blockColor ?: ThemeColor.DEFAULT.title
                val items =
                    listOf(SlashItem.Subheader.ColorWithBack) + SlashExtensions.getSlashWidgetColorItems(
                        code = color
                    )
                onSlashWidgetStateChanged(
                    SlashWidgetState.UpdateItems.empty().copy(
                        colorItems = items
                    )
                )
            }
            is SlashItem.Main.Background -> {
                val block = blocks.first { it.id == targetId }
                val blockBackground = block.content.asText().backgroundColor
                val background = blockBackground ?: ThemeColor.DEFAULT.title
                val items = listOf(SlashItem.Subheader.BackgroundWithBack) +
                        SlashExtensions.getSlashWidgetBackgroundItems(
                            code = background
                        )
                onSlashWidgetStateChanged(
                    SlashWidgetState.UpdateItems.empty().copy(
                        backgroundItems = items
                    )
                )
            }
            is SlashItem.Style.Type -> {
                cutSlashFilter(targetId = targetId)
                controlPanelInteractor.onEvent(ControlPanelMachine.Event.Slash.OnStop)
                onSlashStyleTypeItemClicked(item, targetId)
            }
            is SlashItem.Style.Markup -> {
                cutSlashFilter(targetId = targetId)
                controlPanelInteractor.onEvent(ControlPanelMachine.Event.Slash.OnStop)
                viewModelScope.launch {
                    val view = views.find { it.id == targetId }
                    if (view is BlockView.Text) {
                        orchestrator.proxies.intents.send(
                            Intent.Text.UpdateMark(
                                context = context,
                                targets = listOf(targetId),
                                mark = Content.Text.Mark(
                                    range = IntRange(0, view.text.length),
                                    type = item.convertToMarkType()
                                )
                            )
                        )
                    }
                }
            }
            is SlashItem.Media -> {
                cutSlashFilter(targetId = targetId)
                controlPanelInteractor.onEvent(ControlPanelMachine.Event.Slash.OnStop)
                onSlashMediaItemClicked(item = item)
            }
            is SlashItem.ObjectType -> {
                cutSlashFilter(targetId = targetId)
                controlPanelInteractor.onEvent(ControlPanelMachine.Event.Slash.OnStop)
                onAddNewObjectClicked(
                    type = item.url,
                    layout = item.layout
                )
            }
            is SlashItem.Relation -> {
                val isBlockEmpty = cutSlashFilter(targetId = targetId)
                onSlashRelationItemClicked(item, targetId, isBlockEmpty)
            }
            is SlashItem.Other.Line -> {
                cutSlashFilter(targetId = targetId)
                controlPanelInteractor.onEvent(ControlPanelMachine.Event.Slash.OnStop)
                onHideKeyboardClicked()
                addDividerBlock(style = Content.Divider.Style.LINE)
            }
            is SlashItem.Other.Dots -> {
                cutSlashFilter(targetId = targetId)
                controlPanelInteractor.onEvent(ControlPanelMachine.Event.Slash.OnStop)
                onHideKeyboardClicked()
                addDividerBlock(style = Content.Divider.Style.DOTS)
            }
            is SlashItem.Actions -> {
                cutSlashFilter(targetId = targetId)
                controlPanelInteractor.onEvent(ControlPanelMachine.Event.Slash.OnStop)
                onSlashActionItemClicked(item, targetId)
            }
            is SlashItem.Alignment -> {
                cutSlashFilter(targetId = targetId)
                controlPanelInteractor.onEvent(ControlPanelMachine.Event.Slash.OnStop)
                onSlashAlignmentItemClicked(item, targetId)
            }
            is SlashItem.Color -> {
                cutSlashFilter(targetId = targetId)
                controlPanelInteractor.onEvent(ControlPanelMachine.Event.Slash.OnStop)
                onSlashItemColorClicked(item, targetId)
            }
            SlashItem.Back -> {
                onSlashBackClicked()
            }
            is SlashItem.Subheader -> {
                Timber.d("Click on Slash Subheader, do nothing")
            }
        }
    }

    private fun cutSlashFilter(targetId: Id): Boolean {

        //saving cursor on slash start index
        setPendingCursorToPosition(targetId = targetId, position = slashStartIndex)

        // cut text from List<BlockView> and rerender views
        val newBlockView = cutSlashFilterFromViews(targetId)

        // cut text from List<Block> and send TextUpdate Intent
        if (newBlockView != null) {
            cutSlashFilterFromBlocksAndSendUpdate(
                targetId = targetId,
                text = newBlockView.text,
                marks = newBlockView.marks.map { it.mark() }
            )
        } else {
            Timber.e("cutSlashFilter error, BlockView is null on targetId:$targetId")
        }

        return newBlockView?.text?.isEmpty() ?: false
    }

    private fun cutSlashFilterFromViews(targetId: Id): BlockView.Text? {
        val blockView = views.firstOrNull { it.id == targetId }
        if (blockView is BlockView.Text) {
            val new = blockView.cutPartOfText(
                from = slashStartIndex,
                partLength = slashFilter.length
            )
            val update = views.update(new)
            viewModelScope.launch {
                orchestrator.stores.views.update(update)
                renderCommand.send(Unit)
            }
            return new
        }
        return null
    }

    private fun cutSlashFilterFromBlocksAndSendUpdate(
        targetId: Id,
        text: String,
        marks: List<Content.Text.Mark>
    ) {
        blocks = blocks.updateTextContent(
            target = targetId,
            text = text,
            marks = marks
        )

        //send new text to Middleware
        viewModelScope.launch {
            orchestrator.proxies.saves.send(null)
            orchestrator.proxies.changes.send(null)
        }

        val intent = Intent.Text.UpdateText(
            context = context,
            target = targetId,
            text = text,
            marks = marks
        )

        proceedWithUpdatingText(intent)
    }

    private fun setPendingCursorToPosition(targetId: Id, position: Int) {
        val cursor = Editor.Cursor.Range(
            range = IntRange(position, position)
        )
        val focus = Editor.Focus(
            id = targetId,
            cursor = cursor
        )
        viewModelScope.launch {
            orchestrator.stores.focus.update(focus)
        }
    }

    private fun getObjectTypes(action: (List<ObjectType>) -> Unit) {
        viewModelScope.launch {
            getCompatibleObjectTypes.invoke(
                GetCompatibleObjectTypes.Params(
                    smartBlockType = blocks.first { it.id == context }.content<Content.Smart>().type
                )
            ).proceed(
                failure = {
                    Timber.e(it, "Error while getting object types")
                },
                success = { objectTypes ->
                    action.invoke(objectTypes)
                }
            )
        }
    }

    private fun getRelations(action: (List<RelationListViewModel.Model.Item>) -> Unit) {
        val relations = orchestrator.stores.relations.current()
        val details = orchestrator.stores.details.current()
        val detail = details.details[context]
        val values = detail?.map ?: emptyMap()
        val update = relations.views(
            details = details,
            values = values,
            urlBuilder = urlBuilder
        )
            .map { RelationListViewModel.Model.Item(it) }
        action.invoke(update)
    }

    private fun proceedWithObjectTypes(objectTypes: List<ObjectType>) {
        if (BuildConfig.FLAVOR == FLAVOUR_EXPERIMENTAL) {
            onSlashWidgetStateChanged(
                SlashWidgetState.UpdateItems.empty().copy(
                    objectItems = SlashExtensions.getSlashWidgetObjectTypeItems(objectTypes = objectTypes)
                )
            )
        } else {
            val filter = objectTypes.filter { it.url == ObjectType.PAGE_URL }
            onSlashWidgetStateChanged(
                SlashWidgetState.UpdateItems.empty().copy(
                    objectItems = SlashExtensions.getSlashWidgetObjectTypeItems(objectTypes = filter)
                )
            )
        }
    }

    private fun proceedWithRelations(relations: List<RelationListViewModel.Model>) {
        onSlashWidgetStateChanged(
            SlashWidgetState.UpdateItems.empty().copy(
                relationItems = SlashExtensions.getSlashWidgetRelationItems(relations)
            )
        )
    }

    private fun onSlashItemColorClicked(item: SlashItem.Color, targetId: Id) {

        val intent = when (item) {
            is SlashItem.Color.Background -> {
                Intent.Text.UpdateBackgroundColor(
                    context = context,
                    targets = listOf(targetId),
                    color = item.code
                )
            }
            is SlashItem.Color.Text -> {
                Intent.Text.UpdateColor(
                    context = context,
                    targets = listOf(targetId),
                    color = item.code
                )
            }
        }
        viewModelScope.launch {
            orchestrator.proxies.intents.send(intent)
        }
    }

    private fun onSlashMediaItemClicked(item: SlashItem.Media) {
        when (item) {
            SlashItem.Media.Bookmark -> {
                onHideKeyboardClicked()
                onAddBookmarkBlockClicked()
            }
            SlashItem.Media.Code -> {
                onHideKeyboardClicked()
                onAddTextBlockClicked(style = Content.Text.Style.CODE_SNIPPET)
            }
            SlashItem.Media.File -> {
                onHideKeyboardClicked()
                onAddFileBlockClicked(Content.File.Type.FILE)
            }
            SlashItem.Media.Picture -> {
                onHideKeyboardClicked()
                onAddFileBlockClicked(Content.File.Type.IMAGE)
            }
            SlashItem.Media.Video -> {
                onHideKeyboardClicked()
                onAddFileBlockClicked(Content.File.Type.VIDEO)
            }
        }
    }

    private fun onSlashStyleTypeItemClicked(item: SlashItem.Style.Type, targetId: Id) {
        when (item) {
            is SlashItem.Style.Type.Callout -> {
                _toasts.offer("Callout not implemented")
            }
            else -> {
                val uiBlock = item.convertToUiBlock()
                onTurnIntoBlockClicked(
                    target = targetId,
                    uiBlock = uiBlock
                )
            }
        }
    }

    private fun onSlashActionItemClicked(item: SlashItem.Actions, targetId: Id) {
        when (item) {
            SlashItem.Actions.CleanStyle -> {
                viewModelScope.launch {
                    _toasts.offer("CleanStyle not implemented")
                }
            }
            SlashItem.Actions.Copy -> {
                val block = blocks.first { it.id == targetId }
                val intent = Intent.Clipboard.Copy(
                    context = context,
                    range = null,
                    blocks = listOf(block)
                )
                viewModelScope.launch {
                    orchestrator.proxies.intents.send(intent)
                }
            }
            SlashItem.Actions.Paste -> {
                viewModelScope.launch {
                    orchestrator.proxies.intents.send(
                        Intent.Clipboard.Paste(
                            context = context,
                            focus = targetId,
                            range = IntRange(slashStartIndex, slashStartIndex),
                            selected = emptyList()
                        )
                    )
                }
            }
            SlashItem.Actions.Delete -> {
                proceedWithUnlinking(targetId)
            }
            SlashItem.Actions.Duplicate -> {
                duplicateBlock(targetId)
            }
            SlashItem.Actions.Move -> {
                viewModelScope.launch {
                    blocks.forEach { unselect(it.id) }
                    mode = EditorMode.SAM
                    selectWithDescendants(targetId)
                    val updated = views.enterSAM(currentSelection())
                    orchestrator.stores.views.update(updated)
                    renderCommand.send(Unit)
                    controlPanelInteractor.onEvent(
                        ControlPanelMachine.Event.SAM.OnQuickStart(
                            currentSelection().size
                        )
                    )
                }
            }
            SlashItem.Actions.MoveTo -> {
                onHideKeyboardClicked()
                proceedWithMoveTo(targetId)
            }
            SlashItem.Actions.LinkTo -> {
                onAddLinkToObjectClicked()
            }
        }
    }

    private fun selectWithDescendants(targetId: Id) {
        select(targetId)
        val descendants = blocks.asMap().descendants(parent = targetId)
        descendants.forEach { child -> select(child) }
    }

    private fun onSlashAlignmentItemClicked(item: SlashItem.Alignment, targetId: Id) {
        val alignment = when (item) {
            SlashItem.Alignment.Center -> Block.Align.AlignCenter
            SlashItem.Alignment.Left -> Block.Align.AlignLeft
            SlashItem.Alignment.Right -> Block.Align.AlignRight
        }
        proceedWithAlignmentUpdate(
            targets = listOf(targetId),
            alignment = alignment
        )
    }

    private fun onSlashWidgetStateChanged(widgetState: SlashWidgetState) {
        val panelEvent = ControlPanelMachine.Event.Slash.OnFilterChange(
            widgetState = widgetState
        )
        controlPanelInteractor.onEvent(panelEvent)
    }

    private fun onSlashBackClicked() {
        val items = if (BuildConfig.FLAVOR == FLAVOUR_EXPERIMENTAL) {
            SlashExtensions.getExperimentalSlashWidgetMainItems()
        } else {
            SlashExtensions.getStableSlashWidgetMainItems()
        }
        val widgetState = SlashWidgetState.UpdateItems.empty().copy(
            mainItems = items
        )
        val panelEvent = ControlPanelMachine.Event.Slash.OnFilterChange(
            widgetState = widgetState
        )
        controlPanelInteractor.onEvent(panelEvent)
    }

    private var filterSearchEmptyCount = 0
    private var slashStartIndex = 0
    private var slashFilter = ""
    private var slashViewType = 0

    private fun incFilterSearchEmptyCount(widgetState: SlashWidgetState.UpdateItems) {
        if (SlashExtensions.isSlashWidgetEmpty(widgetState)) {
            filterSearchEmptyCount += 1
        }
    }

    private fun onSlashRelationItemClicked(
        item: SlashItem.Relation, targetId: Id, isBlockEmpty: Boolean
    ) {
        controlPanelInteractor.onEvent(ControlPanelMachine.Event.Slash.OnStopAndClearFocus)
        val intent = if (isBlockEmpty) {
            Intent.CRUD.Replace(
                context = context,
                target = targetId,
                prototype = Prototype.Relation(key = item.relation.view.relationId)
            )
        } else {
            Intent.CRUD.Create(
                context = context,
                target = targetId,
                position = Position.BOTTOM,
                prototype = Prototype.Relation(key = item.relation.view.relationId)
            )
        }
        viewModelScope.launch {
            orchestrator.proxies.intents.send(intent)
        }
    }
    //endregion

    //region MARKUP TOOLBAR

    fun onMarkupUrlClicked() {

        Timber.d("onMarkupUrlClicked, ")

        val target = orchestrator.stores.focus.current().id
        val selection = orchestrator.stores.textSelection.current().selection!!

        pending.add(
            Restore.Selection(
                target = target,
                range = selection
            )
        )

        val update = views.map { view ->
            if (view.id == target) {
                view.setGhostEditorSelection(selection)
            } else {
                view
            }
        }

        viewModelScope.launch { orchestrator.stores.views.update(update) }
        viewModelScope.launch { renderCommand.send(Unit) }
        controlPanelInteractor.onEvent(
            ControlPanelMachine.Event.MarkupToolbar.OnMarkupToolbarUrlClicked
        )
    }

    fun onSetLink(url: String) {
        Timber.d("onSetLink, url:[$url]")
        val range = orchestrator.stores.textSelection.current().selection
        if (range != null) {
            val target = orchestrator.stores.focus.current().id
            restore.add(pending.poll())
            if (url.isNotEmpty())
                applyLinkMarkup(
                    blockId = target,
                    link = url,
                    range = range.first..range.last.dec()
                )
            else
                onUnlinkPressed(
                    blockId = target,
                    range = range.first..range.last.dec()
                )
            controlPanelInteractor.onEvent(
                event = ControlPanelMachine.Event.MarkupToolbar.OnMarkupUrlSet
            )
        }
    }

    fun onBlockerClicked() {
        Timber.d("onBlockerClicked, ")
        val target = orchestrator.stores.focus.current().id
        val update = views.map { view ->
            if (view.id == target) {
                view.setGhostEditorSelection(null).apply {
                    if (this is Focusable) {
                        isFocused = true
                    }
                }
            } else {
                view
            }
        }
        restore.add(pending.poll())
        viewModelScope.launch { orchestrator.stores.views.update(update) }
        viewModelScope.launch { renderCommand.send(Unit) }
        controlPanelInteractor.onEvent(
            event = ControlPanelMachine.Event.MarkupToolbar.OnBlockerClicked
        )
    }

    fun onUnlinkPressed(blockId: String, range: IntRange) {
        Timber.d("onUnlinkPressed, blockId:[$blockId] range:[$range]")

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

    fun onMarkupColorToggleClicked() {
        Timber.d("onMarkupColorToggleClicked, ")
        controlPanelInteractor.onEvent(
            ControlPanelMachine.Event.MarkupToolbar.OnMarkupColorToggleClicked
        )
    }

    fun onMarkupHighlightToggleClicked() {
        Timber.d("onMarkupHighlightToggleClicked, ")
        controlPanelInteractor.onEvent(
            ControlPanelMachine.Event.MarkupToolbar.OnMarkupHighlightToggleClicked
        )
    }

    //endregion
}