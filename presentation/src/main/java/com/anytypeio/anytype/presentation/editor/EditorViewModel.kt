package com.anytypeio.anytype.presentation.editor

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.analytics.base.EventsDictionary.searchScreenShow
import com.anytypeio.anytype.analytics.base.EventsPropertiesKey
import com.anytypeio.anytype.analytics.base.sendEvent
import com.anytypeio.anytype.analytics.props.Props
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Block.Content
import com.anytypeio.anytype.core_models.Block.Prototype
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.Document
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.FileLimitsEvent
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.InternalFlags
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Position
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.RelationLink
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.SyncStatus
import com.anytypeio.anytype.core_models.TextBlock
import com.anytypeio.anytype.core_models.ThemeColor
import com.anytypeio.anytype.core_models.Url
import com.anytypeio.anytype.core_models.ext.addMention
import com.anytypeio.anytype.core_models.ext.asMap
import com.anytypeio.anytype.core_models.ext.content
import com.anytypeio.anytype.core_models.ext.descendants
import com.anytypeio.anytype.core_models.ext.isAllTextAndNoneCodeBlocks
import com.anytypeio.anytype.core_models.ext.isAllTextBlocks
import com.anytypeio.anytype.core_models.ext.parents
import com.anytypeio.anytype.core_models.ext.process
import com.anytypeio.anytype.core_models.ext.sortByType
import com.anytypeio.anytype.core_models.ext.supportNesting
import com.anytypeio.anytype.core_models.ext.title
import com.anytypeio.anytype.core_models.ext.updateTextContent
import com.anytypeio.anytype.core_models.restrictions.ObjectRestriction
import com.anytypeio.anytype.core_utils.common.EventWrapper
import com.anytypeio.anytype.core_utils.ext.Mimetype
import com.anytypeio.anytype.core_utils.ext.isEndLineClick
import com.anytypeio.anytype.core_utils.ext.replace
import com.anytypeio.anytype.core_utils.ext.switchToLatestFrom
import com.anytypeio.anytype.core_utils.ext.withLatestFrom
import com.anytypeio.anytype.core_utils.tools.FeatureToggles
import com.anytypeio.anytype.core_utils.tools.toPrettyString
import com.anytypeio.anytype.core_utils.ui.ViewStateViewModel
import com.anytypeio.anytype.domain.base.Result
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.block.interactor.RemoveLinkMark
import com.anytypeio.anytype.domain.block.interactor.UpdateLinkMarks
import com.anytypeio.anytype.domain.block.interactor.sets.CreateObjectSet
import com.anytypeio.anytype.domain.block.interactor.sets.GetObjectTypes
import com.anytypeio.anytype.domain.clipboard.Paste.Companion.DEFAULT_RANGE
import com.anytypeio.anytype.domain.cover.SetDocCoverImage
import com.anytypeio.anytype.domain.editor.Editor
import com.anytypeio.anytype.domain.error.Error
import com.anytypeio.anytype.domain.event.interactor.InterceptEvents
import com.anytypeio.anytype.domain.icon.SetDocumentImageIcon
import com.anytypeio.anytype.domain.icon.SetImageIcon
import com.anytypeio.anytype.domain.launch.GetDefaultPageType
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.`object`.ConvertObjectToCollection
import com.anytypeio.anytype.domain.`object`.ConvertObjectToSet
import com.anytypeio.anytype.domain.`object`.SetObjectInternalFlags
import com.anytypeio.anytype.domain.`object`.UpdateDetail
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.page.CloseBlock
import com.anytypeio.anytype.domain.page.CreateBlockLinkWithObject
import com.anytypeio.anytype.domain.page.CreateObject
import com.anytypeio.anytype.domain.page.CreateObjectAsMentionOrLink
import com.anytypeio.anytype.domain.page.OpenPage
import com.anytypeio.anytype.domain.relations.AddRelationToObject
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.domain.sets.FindObjectSetForType
import com.anytypeio.anytype.domain.status.InterceptThreadStatus
import com.anytypeio.anytype.domain.unsplash.DownloadUnsplashImage
import com.anytypeio.anytype.domain.workspace.InterceptFileLimitEvents
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.domain.workspace.WorkspaceManager
import com.anytypeio.anytype.presentation.BuildConfig
import com.anytypeio.anytype.presentation.common.Action
import com.anytypeio.anytype.presentation.common.Delegator
import com.anytypeio.anytype.presentation.common.StateReducer
import com.anytypeio.anytype.presentation.common.SupportCommand
import com.anytypeio.anytype.presentation.editor.ControlPanelMachine.Event.ObjectTypesWidgetEvent
import com.anytypeio.anytype.presentation.editor.ControlPanelMachine.Interactor
import com.anytypeio.anytype.presentation.editor.Editor.Restore
import com.anytypeio.anytype.presentation.editor.editor.Command
import com.anytypeio.anytype.presentation.editor.editor.Intent
import com.anytypeio.anytype.presentation.editor.editor.Intent.Media
import com.anytypeio.anytype.presentation.editor.editor.KeyPressedEvent
import com.anytypeio.anytype.presentation.editor.editor.Markup
import com.anytypeio.anytype.presentation.editor.editor.Orchestrator
import com.anytypeio.anytype.presentation.editor.editor.Proxy
import com.anytypeio.anytype.presentation.editor.editor.SideEffect
import com.anytypeio.anytype.presentation.editor.editor.ViewState
import com.anytypeio.anytype.presentation.editor.editor.actions.ActionItemType
import com.anytypeio.anytype.presentation.editor.editor.control.ControlPanelState
import com.anytypeio.anytype.presentation.editor.editor.ext.clearSearchHighlights
import com.anytypeio.anytype.presentation.editor.editor.ext.cutPartOfText
import com.anytypeio.anytype.presentation.editor.editor.ext.enterSAM
import com.anytypeio.anytype.presentation.editor.editor.ext.fillTableOfContents
import com.anytypeio.anytype.presentation.editor.editor.ext.findSearchResultPosition
import com.anytypeio.anytype.presentation.editor.editor.ext.findTableCellView
import com.anytypeio.anytype.presentation.editor.editor.ext.getOnFocusChangedEvent
import com.anytypeio.anytype.presentation.editor.editor.ext.highlight
import com.anytypeio.anytype.presentation.editor.editor.ext.isStyleClearable
import com.anytypeio.anytype.presentation.editor.editor.ext.nextSearchTarget
import com.anytypeio.anytype.presentation.editor.editor.ext.previousSearchTarget
import com.anytypeio.anytype.presentation.editor.editor.ext.singleStylingMode
import com.anytypeio.anytype.presentation.editor.editor.ext.toEditMode
import com.anytypeio.anytype.presentation.editor.editor.ext.toReadMode
import com.anytypeio.anytype.presentation.editor.editor.ext.update
import com.anytypeio.anytype.presentation.editor.editor.ext.updateCursorAndEditMode
import com.anytypeio.anytype.presentation.editor.editor.ext.updateSelection
import com.anytypeio.anytype.presentation.editor.editor.ext.updateTableOfContentsViews
import com.anytypeio.anytype.presentation.editor.editor.items
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.markup
import com.anytypeio.anytype.presentation.editor.editor.mention.MentionConst.MENTION_PREFIX
import com.anytypeio.anytype.presentation.editor.editor.mention.MentionConst.MENTION_TITLE_EMPTY
import com.anytypeio.anytype.presentation.editor.editor.mention.MentionEvent
import com.anytypeio.anytype.presentation.editor.editor.mention.getMentionName
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.editor.model.Focusable
import com.anytypeio.anytype.presentation.editor.editor.model.UiBlock
import com.anytypeio.anytype.presentation.editor.editor.sam.ScrollAndMoveTargetDescriptor.Companion.END_RANGE
import com.anytypeio.anytype.presentation.editor.editor.sam.ScrollAndMoveTargetDescriptor.Companion.INNER_RANGE
import com.anytypeio.anytype.presentation.editor.editor.sam.ScrollAndMoveTargetDescriptor.Companion.START_RANGE
import com.anytypeio.anytype.presentation.editor.editor.search.SearchInDocEvent
import com.anytypeio.anytype.presentation.editor.editor.slash.SlashEvent
import com.anytypeio.anytype.presentation.editor.editor.slash.SlashExtensions
import com.anytypeio.anytype.presentation.editor.editor.slash.SlashExtensions.SLASH_CHAR
import com.anytypeio.anytype.presentation.editor.editor.slash.SlashExtensions.SLASH_EMPTY_SEARCH_MAX
import com.anytypeio.anytype.presentation.editor.editor.slash.SlashExtensions.getSlashWidgetAlignmentItems
import com.anytypeio.anytype.presentation.editor.editor.slash.SlashExtensions.getSlashWidgetStyleItems
import com.anytypeio.anytype.presentation.editor.editor.slash.SlashItem
import com.anytypeio.anytype.presentation.editor.editor.slash.SlashRelationView
import com.anytypeio.anytype.presentation.editor.editor.slash.SlashWidgetState
import com.anytypeio.anytype.presentation.editor.editor.slash.convertToMarkType
import com.anytypeio.anytype.presentation.editor.editor.slash.convertToUiBlock
import com.anytypeio.anytype.presentation.editor.editor.slash.toSlashItemView
import com.anytypeio.anytype.presentation.editor.editor.styling.StylingEvent
import com.anytypeio.anytype.presentation.editor.editor.styling.getIds
import com.anytypeio.anytype.presentation.editor.editor.styling.getStyleBackgroundToolbarState
import com.anytypeio.anytype.presentation.editor.editor.styling.getStyleColorBackgroundToolbarState
import com.anytypeio.anytype.presentation.editor.editor.styling.getStyleOtherStateForTableCells
import com.anytypeio.anytype.presentation.editor.editor.styling.getStyleOtherToolbarState
import com.anytypeio.anytype.presentation.editor.editor.styling.getStyleTextToolbarState
import com.anytypeio.anytype.presentation.editor.editor.table.EditorTableDelegate
import com.anytypeio.anytype.presentation.editor.editor.table.EditorTableEvent
import com.anytypeio.anytype.presentation.editor.editor.table.SimpleTableWidgetItem
import com.anytypeio.anytype.presentation.editor.editor.toCoreModel
import com.anytypeio.anytype.presentation.editor.editor.updateText
import com.anytypeio.anytype.presentation.editor.model.EditorFooter
import com.anytypeio.anytype.presentation.editor.model.TextUpdate
import com.anytypeio.anytype.presentation.editor.picker.PickerListener
import com.anytypeio.anytype.presentation.editor.render.BlockViewRenderer
import com.anytypeio.anytype.presentation.editor.render.DefaultBlockViewRenderer
import com.anytypeio.anytype.presentation.editor.search.search
import com.anytypeio.anytype.presentation.editor.selection.SelectionStateHolder
import com.anytypeio.anytype.presentation.editor.selection.getAllSelectedColumns
import com.anytypeio.anytype.presentation.editor.selection.getAllSelectedRows
import com.anytypeio.anytype.presentation.editor.selection.getIdsInColumn
import com.anytypeio.anytype.presentation.editor.selection.getIdsInRow
import com.anytypeio.anytype.presentation.editor.selection.getSimpleTableWidgetCellItems
import com.anytypeio.anytype.presentation.editor.selection.getSimpleTableWidgetColumnItems
import com.anytypeio.anytype.presentation.editor.selection.getSimpleTableWidgetRowItems
import com.anytypeio.anytype.presentation.editor.selection.getTableColumnsById
import com.anytypeio.anytype.presentation.editor.selection.getTableRowsById
import com.anytypeio.anytype.presentation.editor.selection.toggleTableMode
import com.anytypeio.anytype.presentation.editor.selection.updateTableBlockSelection
import com.anytypeio.anytype.presentation.editor.selection.updateTableBlockTab
import com.anytypeio.anytype.presentation.editor.template.EditorTemplateDelegate
import com.anytypeio.anytype.presentation.editor.template.SelectTemplateEvent
import com.anytypeio.anytype.presentation.editor.template.SelectTemplateState
import com.anytypeio.anytype.presentation.editor.template.SelectTemplateViewState
import com.anytypeio.anytype.presentation.editor.toggle.ToggleStateHolder
import com.anytypeio.anytype.presentation.extension.getProperObjectName
import com.anytypeio.anytype.presentation.extension.sendAnalyticsBlockActionEvent
import com.anytypeio.anytype.presentation.extension.sendAnalyticsBlockAlignEvent
import com.anytypeio.anytype.presentation.extension.sendAnalyticsBlockBackgroundEvent
import com.anytypeio.anytype.presentation.extension.sendAnalyticsBlockMoveToEvent
import com.anytypeio.anytype.presentation.extension.sendAnalyticsBlockReorder
import com.anytypeio.anytype.presentation.extension.sendAnalyticsBookmarkOpen
import com.anytypeio.anytype.presentation.extension.sendAnalyticsGoBackEvent
import com.anytypeio.anytype.presentation.extension.sendAnalyticsMentionMenuEvent
import com.anytypeio.anytype.presentation.extension.sendAnalyticsObjectCreateEvent
import com.anytypeio.anytype.presentation.extension.sendAnalyticsObjectShowEvent
import com.anytypeio.anytype.presentation.extension.sendAnalyticsObjectTypeChangeEvent
import com.anytypeio.anytype.presentation.extension.sendAnalyticsOpenAsObject
import com.anytypeio.anytype.presentation.extension.sendAnalyticsRelationValueEvent
import com.anytypeio.anytype.presentation.extension.sendAnalyticsSearchResultEvent
import com.anytypeio.anytype.presentation.extension.sendAnalyticsSearchWordsEvent
import com.anytypeio.anytype.presentation.extension.sendAnalyticsSelectionMenuEvent
import com.anytypeio.anytype.presentation.extension.sendAnalyticsSetDescriptionEvent
import com.anytypeio.anytype.presentation.extension.sendAnalyticsSetTitleEvent
import com.anytypeio.anytype.presentation.extension.sendAnalyticsSlashMenuEvent
import com.anytypeio.anytype.presentation.extension.sendAnalyticsStyleMenuEvent
import com.anytypeio.anytype.presentation.extension.sendAnalyticsUpdateTextMarkupEvent
import com.anytypeio.anytype.presentation.extension.sendHideKeyboardEvent
import com.anytypeio.anytype.presentation.mapper.mark
import com.anytypeio.anytype.presentation.mapper.style
import com.anytypeio.anytype.presentation.navigation.AppNavigation
import com.anytypeio.anytype.presentation.navigation.DefaultObjectView
import com.anytypeio.anytype.presentation.navigation.SupportNavigation
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.objects.ObjectTypeView
import com.anytypeio.anytype.presentation.objects.SupportedLayouts
import com.anytypeio.anytype.presentation.objects.getObjectTypeViewsForSBPage
import com.anytypeio.anytype.presentation.objects.getProperType
import com.anytypeio.anytype.presentation.objects.toView
import com.anytypeio.anytype.presentation.relations.ObjectRelationView
import com.anytypeio.anytype.presentation.relations.getNotIncludedRecommendedRelations
import com.anytypeio.anytype.presentation.relations.getObjectRelations
import com.anytypeio.anytype.presentation.relations.views
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import com.anytypeio.anytype.presentation.search.ObjectSearchViewModel
import com.anytypeio.anytype.presentation.util.CopyFileStatus
import com.anytypeio.anytype.presentation.util.CopyFileToCacheDirectory
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.presentation.util.OnCopyFileToCacheAction
import java.util.*
import java.util.regex.Pattern
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import com.anytypeio.anytype.presentation.editor.Editor.Mode as EditorMode

class EditorViewModel(
    private val openPage: OpenPage,
    private val closePage: CloseBlock,
    private val createBlockLinkWithObject: CreateBlockLinkWithObject,
    private val createObjectAsMentionOrLink: CreateObjectAsMentionOrLink,
    private val interceptEvents: InterceptEvents,
    private val interceptThreadStatus: InterceptThreadStatus,
    private val updateLinkMarks: UpdateLinkMarks,
    private val removeLinkMark: RemoveLinkMark,
    private val reducer: StateReducer<List<Block>, Event>,
    private val urlBuilder: UrlBuilder,
    private val renderer: DefaultBlockViewRenderer,
    private val orchestrator: Orchestrator,
    private val analytics: Analytics,
    private val dispatcher: Dispatcher<Payload>,
    private val delegator: Delegator<Action>,
    private val updateDetail: UpdateDetail,
    private val searchObjects: SearchObjects,
    private val getDefaultPageType: GetDefaultPageType,
    private val findObjectSetForType: FindObjectSetForType,
    private val createObjectSet: CreateObjectSet,
    private val copyFileToCache: CopyFileToCacheDirectory,
    private val downloadUnsplashImage: DownloadUnsplashImage,
    private val setDocCoverImage: SetDocCoverImage,
    private val setDocImageIcon: SetDocumentImageIcon,
    private val templateDelegate: EditorTemplateDelegate,
    private val createObject: CreateObject,
    private val objectToSet: ConvertObjectToSet,
    private val objectToCollection: ConvertObjectToCollection,
    private val storeOfRelations: StoreOfRelations,
    private val storeOfObjectTypes: StoreOfObjectTypes,
    private val featureToggles: FeatureToggles,
    private val tableDelegate: EditorTableDelegate,
    private val workspaceManager: WorkspaceManager,
    private val spaceManager: SpaceManager,
    private val getObjectTypes: GetObjectTypes,
    private val interceptFileLimitEvents: InterceptFileLimitEvents,
    private val addRelationToObject: AddRelationToObject,
    private val setObjectInternalFlags: SetObjectInternalFlags
) : ViewStateViewModel<ViewState>(),
    PickerListener,
    SupportNavigation<EventWrapper<AppNavigation.Command>>,
    SupportCommand<Command>,
    BlockViewRenderer by renderer,
    ToggleStateHolder by renderer,
    SelectionStateHolder by orchestrator.memory.selections,
    EditorTemplateDelegate by templateDelegate,
    EditorTableDelegate by tableDelegate,
    StateReducer<List<Block>, Event> by reducer {

    val actions = MutableStateFlow(ActionItemType.defaultSorting)

    val isSyncStatusVisible = MutableStateFlow(true)
    val syncStatus = MutableStateFlow<SyncStatus?>(null)

    val isUndoEnabled = MutableStateFlow(false)
    val isRedoEnabled = MutableStateFlow(false)
    val isUndoRedoToolbarIsVisible = MutableStateFlow(false)

    val selectTemplateViewState = templateDelegateState.map { state ->
        when (state) {
            is SelectTemplateState.Available -> {
                SelectTemplateViewState.Active(
                    count = state.templates.size,
                    typeName = state.typeName
                )
            }
            else -> SelectTemplateViewState.Idle
        }
    }

    val searchResultScrollPosition = MutableStateFlow(NO_SEARCH_RESULT_POSITION)

    private val session = MutableStateFlow(Session.IDLE)

    val views: List<BlockView> get() = orchestrator.stores.views.current()

    val restore: Queue<Restore> = LinkedList()

    private val jobs = mutableListOf<Job>()

    var mode: EditorMode = EditorMode.Edit

    val footers = MutableStateFlow<EditorFooter>(EditorFooter.None)

    private val controlPanelInteractor = Interactor(
        viewModelScope,
        reducer = ControlPanelMachine.Reducer(featureToggles)
    )
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

    /**
     * Currently opened document id.
     */
    var context: String = EMPTY_CONTEXT

    /**
     * Current document
     */
    val blocks: Document get() = orchestrator.stores.document.get()

    private val _focus: MutableLiveData<Id> = MutableLiveData()
    val focus: LiveData<Id> = _focus

    private val _toasts = MutableSharedFlow<String>()
    val toasts: SharedFlow<String> = _toasts

    val snacks = MutableSharedFlow<Snack>(replay = 0)

    /**
     * Open gallery and search media files for block with that id
     */
    var currentMediaUploadDescription: Media.Upload.Description? = null
        private set

    private var analyticsContext: String? = null
    private var analyticsOriginalId: String? = null

    override val navigation = MutableLiveData<EventWrapper<AppNavigation.Command>>()
    override val commands = MutableLiveData<EventWrapper<Command>>()

    init {
        startHandlingTextChanges()
        startProcessingFocusChanges()
        startProcessingControlPanelViewState()
        startObservingPayload()
        startObservingErrors()
        processRendering()
        processMarkupChanges()
        viewModelScope.launch { orchestrator.start() }

        viewModelScope.launch {
            delegator.receive().collect { action ->
                when (action) {
                    is Action.SetUnsplashImage -> {
                        proceedWithSettingUnsplashImage(action)
                    }
                    is Action.Duplicate -> proceedWithOpeningObject(action.id)
                    Action.SearchOnPage -> onEnterSearchModeClicked()
                    Action.UndoRedo -> onUndoRedoActionClicked()
                    is Action.OpenObject -> proceedWithOpeningObject(action.id)
                    is Action.OpenCollection -> proceedWithOpeningDataViewObject(action.id)
                }
            }
        }

        viewModelScope.launch {
            templateDelegateState.collect { state ->
                Timber.v("Template delegate state: $state")
                when (state) {
                    is SelectTemplateState.Accepted -> {
                        commands.postValue(EventWrapper(Command.CloseKeyboard))
                        navigate(
                            EventWrapper(
                                AppNavigation.Command.OpenTemplates(
                                    type = state.type,
                                    ctx = context
                                )
                            )
                        )
                    }
                    is SelectTemplateState.Available -> {}
                    SelectTemplateState.Idle -> {}
                }
            }
        }
    }

    override fun onPickedDocImageFromDevice(ctx: Id, path: String) {
        viewModelScope.launch {
            setDocImageIcon(
                SetImageIcon.Params(
                    target = ctx,
                    path = path
                )
            ).process(
                failure = {
                    sendToast("Can't update object icon image")
                    Timber.e(it, "Error while setting image icon")
                },
                success = { (payload, _) ->
                    dispatcher.send(payload)
                }
            )
        }
    }

    private suspend fun proceedWithSettingUnsplashImage(
        action: Action.SetUnsplashImage
    ) {
        downloadUnsplashImage(
            DownloadUnsplashImage.Params(
                picture = action.img
            )
        ).process(
            failure = {
                Timber.e(it, "Error while download unsplash image")
            },
            success = { hash ->
                setDocCoverImage(
                    SetDocCoverImage.Params.FromHash(
                        context = context,
                        hash = hash
                    )
                ).process(
                    failure = {
                        Timber.e(it, "Error while setting unsplash image")
                    },
                    success = { payload -> dispatcher.send(payload) }
                )
            }
        )
    }

    private fun startProcessingFocusChanges() {
        viewModelScope.launch {
            orchestrator.stores.focus.stream().collect { focus ->
                if (focus.isEmpty) {
                    orchestrator.stores.textSelection.update(Editor.TextSelection.empty())
                } else {
                    if (!focus.isPending) {
                        val focused = focus.targetOrNull()
                        val event = if (focused != null)
                            views.getOnFocusChangedEvent(blockId = focused)
                        else
                            null
                        if (event != null) {
                            controlPanelInteractor.onEvent(event)
                        } else {
                            Timber.w("Couldn't found focused block by id:[${focus.targetOrNull()}]")
                        }
                    }
                }
                _focus.postValue(focus.targetOrNull().orEmpty())
            }
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
                .collect { flags ->
                    if (flags.contains(Flags.FLAG_REFRESH)) {
                        Timber.d("Starting refresh due to internal payload update")
                        refresh()
                    } else {
                        Timber.d("----------Refresh skipped----------")
                    }
                }
        }
    }

    private fun startObservingErrors() {
        viewModelScope.launch {
            orchestrator.proxies.errors
                .stream()
                .collect { sendToast(it.message ?: "Unknown error") }
        }
        viewModelScope.launch {
            orchestrator.proxies.toasts
                .stream()
                .collect { sendToast(it) }
        }
    }

    private suspend fun processEvents(events: List<Event>): List<Flag> {
        if (featureToggles.isLogEditorViewModelEvents) {
            Timber.d("Blocks before handling events: ${blocks.toPrettyString()}")
            Timber.d("Events: ${events.toPrettyString()}")
        }
        events.forEach { event ->
            when (event) {
                is Event.Command.ShowObject -> {
                    orchestrator.stores.details.update(event.details)
                    orchestrator.stores.relationLinks.update(event.relationLinks)
                    orchestrator.stores.objectRestrictions.update(event.objectRestrictions)

                    proceedWithShowingObjectTypesWidget()
                }
                is Event.Command.Details -> {
                    orchestrator.stores.details.apply { update(current().process(event)) }
                }
                is Event.Command.ObjectRelationLinks -> {
                    orchestrator.stores.relationLinks.apply { update(current().process(event)) }
                }
                else -> {
                    // do nothing
                }
            }
            orchestrator.stores.document.update(reduce(blocks, event))
        }
        if (featureToggles.isLogEditorViewModelEvents) {
            Timber.d("Blocks after handling events: ${blocks.toPrettyString()}")
        }
        return events.flags(context)
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
                    applyMarkup(
                        selection = Pair(textSelection.id, range),
                        action = action
                    )
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

        val update = blocks.map { block ->
            if (block.id != target.id)
                block
            else
                new
        }
        orchestrator.stores.document.update(update)

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
            val update = blocks.map {
                if (it.id != block.id)
                    it
                else
                    block
            }
            orchestrator.stores.document.update(update)
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
                val root = models.first { it.id == context }
                if (mode == EditorMode.Locked) {
                    if (root.fields.isLocked != true) {
                        mode = EditorMode.Edit
                        sendToast("Your object is unlocked")
                    }
                } else {
                    if (root.fields.isLocked == true) {
                        mode = EditorMode.Locked
                        sendToast("Your object is locked")
                    }
                }
                footers.value = getFooterState(root, details)
                val flags = mutableListOf<BlockViewRenderer.RenderFlag>()
                Timber.d("Rendering starting...")
                val doc = models.asMap().render(
                    mode = mode,
                    root = root,
                    focus = focus,
                    anchor = context,
                    indent = INITIAL_INDENT,
                    details = details,
                    relationLinks = orchestrator.stores.relationLinks.current(),
                    restrictions = orchestrator.stores.objectRestrictions.current(),
                    selection = currentSelection()
                ) { onRenderFlagFound -> flags.add(onRenderFlagFound) }
                if (flags.isNotEmpty()) {
                    doc.fillTableOfContents()
                } else {
                    doc
                }
            }
            .catch { error ->
                Timber.e(error, "Get error in renderizePipeline")
                emit(emptyList())
            }
            .onEach { views ->
                orchestrator.stores.views.update(views)
                renderCommand.send(Unit)
            }
            .onEach { refreshTableToolbar() }
            .launchIn(viewModelScope)
    }

    private fun refreshTableToolbar() {
        val tableMode = mode
        if (tableMode is EditorMode.Table) {
            controlPanelViewState.value?.let { state ->
                if (state.simpleTableWidget.isVisible) {
                    val tableBlock = views.find { it.id == tableMode.tableId }
                    if (tableBlock is BlockView.Table) {
                        when (tableMode.tab) {
                            BlockView.Table.Tab.CELL -> {}
                            BlockView.Table.Tab.COLUMN -> {
                                val selectedColumns = mutableSetOf<BlockView.Table.Column>()
                                tableBlock.cells.forEach {
                                    if (currentSelection().contains(it.getId())) {
                                        selectedColumns.add(it.column)
                                    }
                                }
                                val event =
                                    ControlPanelMachine.Event.SimpleTableWidget.ShowColumnTab(
                                        columnItems = getSimpleTableWidgetColumnItems(
                                            selectedColumns = selectedColumns,
                                            columnsSize = tableBlock.columns.size
                                        ),
                                        tableId = tableBlock.id,
                                        columnsSize = selectedColumns.size
                                    )
                                controlPanelInteractor.onEvent(event)
                            }
                            BlockView.Table.Tab.ROW -> {
                                val selectedRows = tableBlock.getAllSelectedRows(
                                    selectedCellsIds = currentSelection()
                                )
                                val rowItems = getSimpleTableWidgetRowItems(
                                    selectedRows = selectedRows.selectedRows,
                                    rowsSize = tableBlock.rows.size
                                )
                                val event = ControlPanelMachine.Event.SimpleTableWidget.ShowRowTab(
                                    rowItems = rowItems,
                                    tableId = tableBlock.id,
                                    rowsSize = selectedRows.selectedRows.size
                                )
                                controlPanelInteractor.onEvent(event)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun refreshStyleToolbar(document: Document) {
        controlPanelViewState.value?.let { state ->
            if (state.styleTextToolbar.isVisible) {
                val ids = mode.getIds()
                if (ids.isNullOrEmpty()) return
                onSendRefreshStyleTextToolbarEvent(ids)
            }
            if (state.styleBackgroundToolbar.isVisible) {
                val ids = mode.getIds()
                if (ids.isNullOrEmpty()) return
                onSendRefreshStyleBackgroundToolbarEvent(ids)
            }
            if (state.markupMainToolbar.isVisible) {
                controlPanelInteractor.onEvent(
                    event = ControlPanelMachine.Event.OnRefresh.Markup(
                        target = document.find { block -> block.id == orchestrator.stores.focus.current().targetOrNull() },
                        selection = orchestrator.stores.textSelection.current().selection
                    )
                )
            }
            if (state.styleColorBackgroundToolbar.isVisible) {
                val ids = mode.getIds()
                if (ids.isNullOrEmpty()) return
                onSendUpdateStyleColorBackgroundToolbarEvent(ids)
            }
            if (state.styleExtraToolbar.isVisible) {
                val ids = mode.getIds()
                if (ids.isNullOrEmpty()) return
                onSendUpdateStyleOtherToolbarEvent(ids)
            }
        }
    }

    private fun onSendRefreshStyleTextToolbarEvent(ids: List<Id>) {
        val selected = blocks.filter { ids.contains(it.id) }
        val isAllSelectedText = selected.isAllTextBlocks()
        if (isAllSelectedText) {
            val state = selected.map { it.content.asText() }.getStyleTextToolbarState()
            controlPanelInteractor.onEvent(
                ControlPanelMachine.Event.StylingToolbar.OnUpdateTextToolbar(state)
            )
        }
    }

    private fun onSendRefreshStyleBackgroundToolbarEvent(ids: List<Id>) {
        val selected = blocks.filter { ids.contains(it.id) }
        val state = selected.getStyleBackgroundToolbarState()
        controlPanelInteractor.onEvent(
            ControlPanelMachine.Event.StylingToolbar.OnUpdateBackgroundToolbar(state)
        )
    }

    private fun onShowColorBackgroundToolbarEvent(
        ids: List<Id>,
        navigatedFromCellsMenu: Boolean,
        navigateFromStylingTextToolbar: Boolean
    ) {
        val selected = blocks.filter { ids.contains(it.id) }
        val state = selected.getStyleColorBackgroundToolbarState()
        controlPanelInteractor.onEvent(
            ControlPanelMachine.Event.ColorBackgroundToolbar.Show(
                state = state,
                navigatedFromCellsMenu = navigatedFromCellsMenu,
                navigateFromStylingTextToolbar = navigateFromStylingTextToolbar
            )
        )
    }

    private fun onSendUpdateStyleColorBackgroundToolbarEvent(ids: List<Id>) {
        val selected = blocks.filter { ids.contains(it.id) }
        val state = selected.getStyleColorBackgroundToolbarState()
        controlPanelInteractor.onEvent(
            ControlPanelMachine.Event.ColorBackgroundToolbar.Update(state)
        )
    }

    private fun onShowStyleOtherToolbarEvent(
        ids: List<Id>,
        navigatedFromCellsMenu: Boolean,
        navigateFromStylingTextToolbar: Boolean
    ) {
        val selected = blocks.filter { ids.contains(it.id) }
        val state = selected.map { it.content.asText() }.getStyleOtherToolbarState()
        controlPanelInteractor.onEvent(
            ControlPanelMachine.Event.OtherToolbar.Show(
                state = state,
                navigatedFromCellsMenu = navigatedFromCellsMenu,
                navigateFromStylingTextToolbar = navigateFromStylingTextToolbar
            )
        )
    }

    private fun onSendUpdateStyleOtherToolbarEvent(ids: List<Id>) {
        val selected = blocks.filter { ids.contains(it.id) }
        val state = when (mode) {
            is EditorMode.Table ->
                selected.map { it.content.asText() }.getStyleOtherStateForTableCells()
            else -> selected.map { it.content.asText() }.getStyleOtherToolbarState()
        }
        controlPanelInteractor.onEvent(
            ControlPanelMachine.Event.OtherToolbar.Update(state)
        )
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
            .onEach { update -> orchestrator.textInteractor.consume(update, context) }
            .launchIn(viewModelScope)

        orchestrator
            .proxies
            .saves
            .stream()
            .filterNotNull()
            .onEach { update ->
                val updated = blocks.map { block ->
                    if (block.id == update.target) {
                        block.updateText(update)
                    } else
                        block
                }
                orchestrator.stores.document.update(updated)
            }
            .map { update ->
                Intent.Text.UpdateText(
                    context = context,
                    target = update.target,
                    text = update.text,
                    marks = update.markup.filter { it.range.first != it.range.last }
                )
            }
            .onEach { params ->
                proceedWithUpdatingText(params)
            }
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
                .collect { flags ->
                    if (flags.contains(Flags.FLAG_REFRESH)) {
                        Timber.d("Starting refresh due external payload update")
                        refresh()
                    } else
                        Timber.d("----------Refresh skipped----------")
                }
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

        observeFileLimitsEvents()

        val startTime = System.currentTimeMillis()
        viewModelScope.launch {
            openPage.async(id).fold(
                onSuccess = { result ->
                    when (result) {
                        is Result.Success -> {
                            session.value = Session.OPEN
                            onStartFocusing(result.data)
                            orchestrator.proxies.payloads.send(result.data)
                            result.data.events.forEach { event ->
                                if (event is Event.Command.ShowObject) {
                                    if (event.details.details[context]?.type?.contains(ObjectTypeIds.FILE) == true) {
                                        isSyncStatusVisible.value = false
                                    }
                                    val block = event.blocks.firstOrNull { it.id == context }
                                    analytics.setContext(block?.fields?.analyticsContext)
                                    analytics.setOriginalId(block?.fields?.analyticsOriginalId)
                                    analyticsContext = block?.fields?.analyticsContext
                                    if (analyticsContext != null) {
                                        analyticsOriginalId = block?.fields?.analyticsOriginalId
                                    }
                                    sendAnalyticsObjectShowEvent(
                                        analytics = analytics,
                                        startTime = startTime
                                    )
                                }
                            }
                        }
                        is Result.Failure -> {
                            session.value = Session.ERROR
                            when (result.error) {
                                Error.BackwardCompatibility -> dispatch(Command.AlertDialog)
                                Error.NotFoundObject -> {
                                    stateData.postValue(ViewState.NotExist)
                                }
                            }
                        }
                    }
                },
                onFailure = {
                    session.value = Session.ERROR
                    Timber.e(it, "Error while opening page with id: $id")
                }
            )
        }
    }

    //TODO need refactoring, logic must depend on Object Layouts
    private fun onStartFocusing(payload: Payload) {
        val event = payload.events.find { it is Event.Command.ShowObject }
        if (event is Event.Command.ShowObject) {
            val root = event.blocks.find { it.id == context }
            when {
                root == null -> Timber.e("Could not find the root block on initial focusing")
                root.fields.isLocked == true -> {
                    mode = EditorMode.Locked
                }
                root.children.size == 1 -> {
                    val first = event.blocks.first { it.id == root.children.first() }
                    val content = first.content
                    if (content is Content.Layout && content.type == Content.Layout.Type.HEADER) {
                        try {
                            val title = event.blocks.title()
                            if (title != null && title.content<Content.Text>().text.isEmpty()) {
                                val focus = Editor.Focus(
                                    target = Editor.Focus.Target.Block(title.id),
                                    cursor = Editor.Cursor.End
                                )
                                viewModelScope.launch { orchestrator.stores.focus.update(focus) }
                            } else {
                                Timber.d("Skipping initial focusing. Title is not empty or is null")
                            }
                        } catch (e: Throwable) {
                            Timber.e(e, "Error while initial focusing")
                        }
                    }
                }
                root.children.size == 2 -> {
                    val layout = event.details.details[root.id]?.layout
                    if (layout == ObjectType.Layout.NOTE.code.toDouble()) {
                        val block = event.blocks.firstOrNull { it.content is Content.Text }
                        if (block != null && block.content<Content.Text>().text.isEmpty()) {
                            val focus = Editor.Focus(
                                target = Editor.Focus.Target.Block(block.id),
                                cursor = Editor.Cursor.End
                            )
                            viewModelScope.launch { orchestrator.stores.focus.update(focus) }
                        }
                    }
                }
                else -> Timber.d("Skipping initial focusing, document is not empty.")
            }
        }
    }

    fun onAddWebLinkToBlock(blockId: Id, link: Id) {
        Timber.d("onAddWebUrlLinkToBlock, blockId:[$blockId] link:[$link]")
        onUpdateBlockListMarkup(ids = listOf(blockId), type = Markup.Type.LINK, param = link)
    }

    fun onAddObjectLinkToBlock(blockId: Id, objectId: Id) {
        Timber.d("onAddObjectIdLinkToBlock, blockId:[$blockId] objectId:[$objectId]")
        onUpdateBlockListMarkup(ids = listOf(blockId), type = Markup.Type.OBJECT, param = objectId)
    }

    fun onSystemBackPressed(editorHasChildrenScreens: Boolean) {
        Timber.d("onSystemBackPressed, editorHasChildrenScreens:[$editorHasChildrenScreens]")
        if (editorHasChildrenScreens) {
            dispatch(Command.PopBackStack)
        } else {
            val state = controlPanelViewState.value
            checkNotNull(state) { "Control panel state is null" }
            when {
                state.styleTextToolbar.isVisible -> {
                    onCloseBlockStyleToolbarClicked()
                }
                state.styleColorBackgroundToolbar.isVisible -> {
                    onCloseBlockStyleColorToolbarClicked()
                }
                state.styleExtraToolbar.isVisible -> {
                    onCloseBlockStyleExtraToolbarClicked()
                }
                state.multiSelect.isVisible -> {
                    onExitMultiSelectModeClicked()
                }
                state.styleBackgroundToolbar.isVisible -> {
                    onCloseBlockStyleBackgroundToolbarClicked()
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
        viewModelScope.sendAnalyticsGoBackEvent(analytics, analyticsContext)
        proceedWithExitingBack()
    }

    fun onHomeButtonClicked() {
        Timber.d("onHomeButtonClicked, ")
        if (stateData.value == ViewState.NotExist) {
            navigateToDesktop()
            return
        }
        proceedWithExitingToDashboard()
    }

    fun proceedWithExitingBack() {
        exitBack()
    }

    fun onMovedToBin() {
        navigate(EventWrapper(AppNavigation.Command.Exit))
    }

    private fun exitBack() {
        when (session.value) {
            Session.ERROR -> navigate(EventWrapper(AppNavigation.Command.Exit))
            Session.IDLE -> navigate(EventWrapper(AppNavigation.Command.Exit))
            Session.OPEN -> {
                viewModelScope.launch {
                    closePage.async(context).fold(
                        onSuccess = { navigate(EventWrapper(AppNavigation.Command.Exit)) },
                        onFailure = {
                            Timber.e(it, "Error while closing document: $context")
                            navigate(EventWrapper(AppNavigation.Command.Exit))
                        }
                    )
                }
            }
        }
    }

    private fun proceedWithExitingToDashboard() {
        exitDashboard()
    }

    private fun exitDashboard() {
        viewModelScope.launch {
            closePage.async(context).fold(
                onSuccess = { navigateToDesktop() },
                onFailure = {
                    Timber.e(it, "Error while closing this page: $context")
                    navigateToDesktop()
                }
            )
        }
    }

    fun navigateToDesktop() {
        Timber.d("navigateToDesktop, ")
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

    fun onTitleBlockTextChanged(id: Id, text: String) {
        Timber.d("onTitleBlockTextChanged, id:[$id], text:[$text]")
        val new = views.map {
            if (it.id == id && it is BlockView.Title) {
                it.text = text
                it
            } else {
                it
            }
        }
        val update = TextUpdate.Default(
            target = id,
            text = text,
            markup = emptyList()
        )
        viewModelScope.launch { orchestrator.stores.views.update(new) }
        viewModelScope.launch { orchestrator.proxies.changes.send(update) }
        sendHideObjectTypeWidgetEvent()
    }

    fun onDescriptionBlockTextChanged(view: BlockView.Description) {

        Timber.d("onDescriptionBlockTextChanged, view:[$view]")

        val new = views.map { if (it.id == view.id) view else it }
        val update = TextUpdate.Default(
            target = view.id,
            text = view.text,
            markup = emptyList()
        )
        viewModelScope.launch { orchestrator.stores.views.update(new) }
        viewModelScope.launch { orchestrator.proxies.changes.send(update) }
        sendHideObjectTypeWidgetEvent()
    }

    fun onTextBlockTextChanged(view: BlockView.Text) {
        Timber.d("onTextBlockTextChanged, view:[$view]")

        val update = TextUpdate.Pattern(
            target = view.id,
            text = view.text,
            markup = view.marks.map { it.mark() }
        )

        val store = orchestrator.stores.views
        val old = store.current()
        val new = old.map { if (it.id == view.id) view else it }

        viewModelScope.launch {
            if (view is BlockView.Text.Header && new.any { it is BlockView.TableOfContents }) {
                store.update(new.updateTableOfContentsViews(view))
                renderCommand.send(Unit)
            } else {
                store.update(new)
            }
        }

        viewModelScope.launch { orchestrator.proxies.changes.send(update) }
        sendHideObjectTypeWidgetEvent()
    }

    fun onSelectionChanged(id: String, selection: IntRange) {
        if (mode != EditorMode.Edit) return
        Timber.d("onSelectionChanged, id:[$id] selection:[$selection]")
        viewModelScope.launch {
            orchestrator.stores.textSelection.update(Editor.TextSelection(id, selection))
        }
        blocks.find { it.id == id }?.let { target ->
            val content = target.content
            val targetBlockType =
                if (content is TextBlock && content.style == Content.Text.Style.TITLE) {
                    ControlPanelState.Toolbar.Main.TargetBlockType.Title
                } else {
                    ControlPanelState.Toolbar.Main.TargetBlockType.Any
                }
            controlPanelInteractor.onEvent(
                ControlPanelMachine.Event.OnSelectionChanged(
                    target = target,
                    selection = selection,
                    targetBlockType = targetBlockType
                )
            )
        }
    }

    fun onCellSelectionChanged(id: Id, selection: IntRange) {
        if (mode != EditorMode.Edit) return
        Timber.d("onCellSelectionChanged, id:[$id] selection:[$selection]")
        viewModelScope.launch {
            orchestrator.stores.textSelection.update(Editor.TextSelection(id, selection))
        }
        blocks.find { it.id == id }?.let { target ->
            controlPanelInteractor.onEvent(
                ControlPanelMachine.Event.OnSelectionChanged(
                    target = target,
                    selection = selection,
                    targetBlockType = ControlPanelState.Toolbar.Main.TargetBlockType.Cell
                )
            )
        }
    }

    fun onBlockFocusChanged(id: String, hasFocus: Boolean) {
        Timber.d("onBlockFocusChanged, id:[$id] hasFocus:[$hasFocus]")
        if (hasFocus) {
            isUndoRedoToolbarIsVisible.value = false
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
        if (!focus.isEmpty && focus.isTarget(target)) {
            proceedWithEnterEvent(focus.requireTarget(), range, text, marks)
        } else {
            Timber.e("No blocks in focus, emit SplitLineEnter event")
        }
    }

    fun onSplitObjectDescription(
        target: Id,
        text: String,
        range: IntRange
    ) {
        Timber.d("onSplitObjectDescription, target:[$target] text:[$text] range:[$range]")
        proceedWithSplitEvent(
            target = target,
            text = text,
            range = range,
            marks = emptyList()
        )
        viewModelScope.sendAnalyticsSetDescriptionEvent(analytics, analyticsContext)
    }

    private fun proceedWithEnterEvent(
        target: Id,
        range: IntRange,
        text: String,
        marks: List<Content.Text.Mark>
    ) {
        if (text.isEndLineClick(range)) {
            onEndLineEnterClicked(target, text, marks)
        } else {
            proceedWithSplitEvent(target, range, text, marks)
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

        val update = blocks.updateTextContent(target, text, marks)
        orchestrator.stores.document.update(update)

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
                    isToggled = if (content.isToggle()) renderer.isToggled(target) else null,
                    style = content.style
                )
            )
        }
    }

    fun onEndLineEnterClicked(
        id: String,
        text: String,
        marks: List<Content.Text.Mark>
    ) {
        Timber.d("onEndLineEnterClicked, id:[$id] text:[$text] marks:[$marks]")

        sendHideObjectTypeWidgetEvent()

        val target = blocks.first { it.id == id }

        val content = target.content<Content.Text>().copy(
            text = text,
            marks = marks
        )

        val update = blocks.replace(
            replacement = { old -> old.copy(content = content) }
        ) { block -> block.id == id }

        orchestrator.stores.document.update(update)

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
        viewModelScope.launch {
            orchestrator.stores.focus.update(Editor.Focus.empty())
            views.onEach { if (it is Focusable) it.isFocused = false }
            renderCommand.send(Unit)
            proceedWithOpeningObjectMenu()
        }
    }

    private fun proceedWithOpeningObjectMenu() {
        if (context.isEmpty()) {
            sendToast("Your object is not initialized. Please, try again later.")
            return
        }
        controlPanelInteractor.onEvent(ControlPanelMachine.Event.OnDocumentMenuClicked)
        val details = orchestrator.stores.details.current().details
        val wrapper = ObjectWrapper.Basic(details[context]?.map.orEmpty())
        dispatch(
            command = Command.OpenDocumentMenu(
                isArchived = details[context]?.isArchived ?: false,
                isFavorite = details[context]?.isFavorite ?: false,
                isLocked = mode == EditorMode.Locked,
                fromName = wrapper.getProperObjectName().orEmpty(),
                isTemplate = isObjectTemplate()
            )
        )
    }

    fun onEmptyBlockBackspaceClicked(id: String) {
        Timber.d("onEmptyBlockBackspaceClicked, id:[$id]")
        val position = views.indexOfFirst { it.id == id }
        if (position > 0) {
            val current = views[position]
            if (current is BlockView.Text && current.isStyleClearable()) {
                viewModelScope.launch {
                    orchestrator.proxies.intents.send(
                        Intent.Text.UpdateStyle(
                            context = context,
                            targets = listOf(id),
                            style = Content.Text.Style.P
                        )
                    )
                }
            } else {
                val previous = views[position.dec()]
                if (previous !is BlockView.Text
                    && previous !is BlockView.Title
                    && previous !is BlockView.Description
                    && previous !is BlockView.FeaturedRelation
                ) {
                    viewModelScope.launch {
                        orchestrator.proxies.intents.send(
                            Intent.CRUD.Unlink(
                                context = context,
                                targets = listOf(previous.id),
                                previous = null,
                                next = null,
                                cursor = null
                            )
                        )
                    }
                } else {
                    proceedWithUnlinking(target = id)
                }
            }
        }
    }

    fun onNonEmptyBlockBackspaceClicked(
        id: String,
        text: String,
        marks: List<Content.Text.Mark>
    ) {
        Timber.d("onNonEmptyBlockBackspaceClicked, id:[$id] text:[$text] marks:[$marks]")

        val update = blocks.map { block ->
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

        orchestrator.stores.document.update(update)

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
            val previousBlockId = index.dec()
            when (val previous = views[previousBlockId]) {
                is BlockView.Text -> {
                    proceedWithMergingBlocks(
                        previous = previous.id,
                        target = id
                    )
                }
                is BlockView.FeaturedRelation -> {
                    val upperThanPreviousBlock = views.getOrNull(previousBlockId.dec())
                    if (upperThanPreviousBlock is Focusable) {
                        proceedWithMergingBlocks(
                            previous = upperThanPreviousBlock.id,
                            target = id
                        )
                    }
                }
                is BlockView.Description,
                is BlockView.Title -> {
                    proceedWithMergingBlocks(
                        previous = previous.id,
                        target = id
                    )
                }
                else -> {
                    viewModelScope.launch {
                        orchestrator.proxies.intents.send(
                            Intent.CRUD.Unlink(
                                context = context,
                                targets = listOf(previous.id),
                                previous = null,
                                next = null,
                                cursor = null
                            )
                        )
                    }
                }
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

    private fun proceedWithEnteringActionMode(target: Id, scrollTarget: Boolean = true) {
        val views = orchestrator.stores.views.current()
        val view = views.find { it.id == target }

        val restrictions = orchestrator.stores.objectRestrictions.current()
        if (restrictions.isNotEmpty()) {
            when (view) {
                is BlockView.Code, is BlockView.Text,
                is BlockView.Media, is BlockView.MediaPlaceholder,
                is BlockView.Upload -> {
                    if (restrictions.contains(ObjectRestriction.BLOCKS)) {
                        sendToast(NOT_ALLOWED_FOR_OBJECT)
                        return
                    }
                }
                is BlockView.Relation, is BlockView.FeaturedRelation -> {
                    if (restrictions.contains(ObjectRestriction.RELATIONS)) {
                        sendToast(NOT_ALLOWED_FOR_OBJECT)
                        return
                    }
                }
                is BlockView.Title -> {
                    if (restrictions.contains(ObjectRestriction.DETAILS)) {
                        sendToast(NOT_ALLOWED_FOR_OBJECT)
                        return
                    }
                }
                else -> {}
            }
        }

        toggleSelection(target)

        if (view !is BlockView.Table && view !is BlockView.TableOfContents) {
            val descendants = blocks.asMap().descendants(parent = target)
            if (isSelected(target)) {
                descendants.forEach { child -> select(child) }
            } else {
                descendants.forEach { child -> unselect(child) }
            }
        }

        mode = EditorMode.Select

        viewModelScope.launch {
            orchestrator.stores.focus.update(Editor.Focus.empty())
            orchestrator.stores.views.update(
                views.enterSAM(targets = currentSelection())
            )
            renderCommand.send(Unit)
            controlPanelInteractor.onEvent(
                ControlPanelMachine.Event.MultiSelect.OnEnter(
                    currentSelection().size
                )
            )
            if (isSelected(target) && scrollTarget) {
                dispatch(Command.ScrollToActionMenu(target = target))
            }
        }

        proceedWithUpdatingActionsForCurrentSelection()
    }

    private fun proceedWithUpdatingActionsForCurrentSelection() {
        val isMultiMode = currentSelection().size > 1

        val targetActions = mutableListOf<ActionItemType>().apply {
            addAll(ActionItemType.defaultSorting)
        }
        val excludedActions = mutableSetOf<ActionItemType>()

        if (isMultiMode) {
            excludedActions.add(ActionItemType.AddBelow)
            excludedActions.add(ActionItemType.Divider)
            excludedActions.add(ActionItemType.DividerExtended)
            excludedActions.add(ActionItemType.OpenObject)
        }

        var needSortByDownloads = false

        blocks.forEach { block ->
            if (currentSelection().contains(block.id)) {
                when (val content = block.content) {
                    is Content.Bookmark -> {
                        excludedActions.add(ActionItemType.Download)
                        if (!isMultiMode) {
                            if (content.targetObjectId != null) {
                                val details = orchestrator.stores.details.current().details
                                val obj = ObjectWrapper.Basic(
                                    details[content.targetObjectId]?.map ?: emptyMap()
                                )
                                val isReady = content.state == Content.Bookmark.State.DONE
                                val isActive = obj.isArchived != true && obj.isDeleted != true
                                val idx = targetActions.indexOf(ActionItemType.OpenObject)
                                if (idx == NO_POSITION && isReady && isActive) {
                                    targetActions.add(
                                        OPEN_OBJECT_POSITION,
                                        ActionItemType.OpenObject
                                    )
                                }
                            }
                        }
                    }
                    is Content.Divider -> {
                        excludedActions.add(ActionItemType.Download)
                    }
                    is Content.File -> {
                        needSortByDownloads = true
                        if (content.state == Content.File.State.DONE) {
                            targetActions.addIfNotExists(ActionItemType.Download)
                        } else {
                            excludedActions.add(ActionItemType.Download)
                        }
                    }
                    is Content.Link -> {
                        targetActions.clear()
                        if (!isMultiMode) {
                            targetActions.addAll(ActionItemType.objectSorting)
                        } else {
                            targetActions.addAll(ActionItemType.objectSortingMultiline)
                        }
                        if (!BuildConfig.ENABLE_LINK_APPERANCE_MENU) {
                            excludedActions.add(ActionItemType.Preview)
                        }
                    }
                    is Content.RelationBlock -> {
                        excludedActions.add(ActionItemType.Download)
                    }
                    is Content.Latex -> {
                        excludedActions.add(ActionItemType.Download)
                    }
                    is Content.Text -> {
                        excludedActions.add(ActionItemType.Download)
                    }
                    is Content.Table -> {
                        excludedActions.add(ActionItemType.Paste)
                        excludedActions.add(ActionItemType.Copy)
                        excludedActions.add(ActionItemType.Style)
                    }
                    is Content.TableOfContents -> {
                        excludedActions.add(ActionItemType.Paste)
                        excludedActions.add(ActionItemType.Copy)
                        excludedActions.add(ActionItemType.Style)
                    }
                    else -> {
                        // do nothing
                    }
                }
            }
        }

        targetActions.removeAll(excludedActions)

        actions.value = if (needSortByDownloads) {
            targetActions.sortedBy { it !is ActionItemType.Download }
        } else {
            targetActions
        }
    }

    private fun MutableList<ActionItemType>.addIfNotExists(
        item: ActionItemType,
        position: Int = NO_POSITION
    ) {
        if (contains(item)) {
            return
        }

        if (position == NO_POSITION) {
            add(item)
        } else {
            add(position, item)
        }
    }

    fun onStylingToolbarEvent(event: StylingEvent) {
        Timber.d("onStylingToolbarEvent, event:[$event]")
        val ids: List<Id>? = mode.getIds()
        if (ids.isNullOrEmpty()) return
        when (event) {
            is StylingEvent.Coloring.Text -> {
                onToolbarTextColorAction(ids, event.color.code)
            }
            is StylingEvent.Coloring.Background -> {
                onBlockBackgroundColorAction(ids, event.color.code)
            }
            is StylingEvent.Markup.Bold -> {
                onUpdateBlockListMarkup(ids, Markup.Type.BOLD)
            }
            is StylingEvent.Markup.Italic -> {
                onUpdateBlockListMarkup(ids, Markup.Type.ITALIC)
            }
            is StylingEvent.Markup.StrikeThrough -> {
                onUpdateBlockListMarkup(ids, Markup.Type.STRIKETHROUGH)
            }
            is StylingEvent.Markup.Code -> {
                onUpdateBlockListMarkup(ids, Markup.Type.KEYBOARD)
            }
            is StylingEvent.Markup.Underline -> {
                onUpdateBlockListMarkup(ids, Markup.Type.UNDERLINE)
            }
            is StylingEvent.Markup.Link -> {
                if (ids.size == 1) {
                    onBlockStyleLinkClicked(ids[0])
                } else {
                    sendToast(ERROR_UNSUPPORTED_BEHAVIOR)
                }
            }
            is StylingEvent.Alignment.Left -> {
                proceedWithAlignmentUpdate(ids, Block.Align.AlignLeft)
            }
            is StylingEvent.Alignment.Center -> {
                proceedWithAlignmentUpdate(ids, Block.Align.AlignCenter)
            }
            is StylingEvent.Alignment.Right -> {
                proceedWithAlignmentUpdate(ids, Block.Align.AlignRight)
            }
            else -> Timber.d("Ignoring styling toolbar event: $event")
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
        viewModelScope.sendAnalyticsUpdateTextMarkupEvent(
            analytics = analytics,
            type = type
        )
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
            sendAnalyticsBlockAlignEvent(
                analytics = analytics,
                context = analyticsContext,
                count = targets.size,
                align = alignment
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
        viewModelScope.sendAnalyticsUpdateTextMarkupEvent(
            analytics = analytics,
            type = Content.Text.Mark.Type.TEXT_COLOR,
            context = analyticsContext
        )
    }

    private fun onBlockBackgroundColorAction(ids: List<Id>, color: String) {
        viewModelScope.launch {
            orchestrator.proxies.intents.send(
                Intent.Text.UpdateBackgroundColor(
                    context = context,
                    targets = ids,
                    color = color
                )
            )
        }
        viewModelScope.sendAnalyticsBlockBackgroundEvent(
            analytics = analytics,
            count = ids.size,
            color = color,
            context = analyticsContext
        )
    }

    private fun onBlockStyleLinkClicked(id: String) {
        val target = blocks.first { it.id == id }
        val range = IntRange(
            start = 0,
            endInclusive = target.content<Content.Text>().text.length
        )
        dispatch(
            Command.OpenLinkToObjectOrWebScreen(
                ctx = context,
                target = target.id,
                range = range,
                isWholeBlockMarkup = true
            )
        )
    }

    private fun onUpdateBlockListMarkup(ids: List<Id>, type: Markup.Type, param: String? = null) {
        viewModelScope.launch {
            orchestrator.proxies.intents.send(
                Intent.Text.UpdateMark(
                    context = context,
                    targets = ids,
                    mark = Content.Text.Mark(
                        range = IntRange(0, Int.MAX_VALUE),
                        type = type.toCoreModel(),
                        param = param
                    )
                )
            )
            sendAnalyticsUpdateTextMarkupEvent(
                analytics = analytics,
                type = type,
                context = analyticsContext
            )
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
                else -> {}
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

    private fun duplicateBlock(
        blocks: List<Id>,
        target: Id
    ) {
        viewModelScope.launch {
            orchestrator.proxies.intents.send(
                Intent.CRUD.Duplicate(
                    context = context,
                    target = target,
                    blocks = blocks
                )
            )
        }
    }

    fun onActionUndoClicked() {
        Timber.d("onActionUndoClicked, ")
        jobs += viewModelScope.launch {
            orchestrator.proxies.intents.send(
                Intent.Document.Undo(
                    context = context,
                    onUndoExhausted = { sendSnack(Snack.UndoRedo("Nothing to undo.")) }
                )
            )
        }
    }

    fun onActionRedoClicked() {
        Timber.d("onActionRedoClicked, ")
        jobs += viewModelScope.launch {
            orchestrator.proxies.intents.send(
                Intent.Document.Redo(
                    context = context,
                    onRedoExhausted = { sendSnack(Snack.UndoRedo("Nothing to redo.")) }
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

    fun onSetTextBlockValue() {
        viewModelScope.launch { refresh() }
    }

    fun onDocRelationsClicked() {
        Timber.d("onDocRelationsClicked, ")
        dispatch(
            Command.OpenObjectRelationScreen.RelationList(
                ctx = context,
                target = null,
                isLocked = mode == EditorMode.Locked
            )
        )
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
                viewModelScope.sendAnalyticsSearchWordsEvent(
                    analytics = analytics,
                    length = query.length,
                    context = analyticsContext
                )
            }
            is SearchInDocEvent.Next -> {
                val update = views.nextSearchTarget()
                viewModelScope.launch { orchestrator.stores.views.update(update) }
                viewModelScope.launch { renderCommand.send(Unit) }
                searchResultScrollPosition.value = update.findSearchResultPosition()
            }
            is SearchInDocEvent.Previous -> {
                val update = views.previousSearchTarget()
                viewModelScope.launch { orchestrator.stores.views.update(update) }
                viewModelScope.launch { renderCommand.send(Unit) }
                searchResultScrollPosition.value = update.findSearchResultPosition()
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
                searchResultScrollPosition.value = update.findSearchResultPosition()
            }
            else -> {}
        }
    }

    fun onAddTextBlockClicked(style: Content.Text.Style) {

        Timber.d("onAddTextBlockClicked, style:[$style]")

        val focused = orchestrator.stores.focus.current().targetOrNull()

        val target = if (focused != null)
            blocks.find { it.id == focused }
        else
            null

        val content = target?.content ?: return

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
        currentMediaUploadDescription = Media.Upload.Description(blockId, Mimetype.MIME_VIDEO_ALL)
        dispatch(Command.OpenGallery(mimeType = Mimetype.MIME_VIDEO_ALL))
    }

    private fun onAddLocalPictureClicked(blockId: String) {
        currentMediaUploadDescription = Media.Upload.Description(blockId, Mimetype.MIME_IMAGE_ALL)
        dispatch(Command.OpenGallery(mimeType = Mimetype.MIME_IMAGE_ALL))
    }

    fun onTogglePlaceholderClicked(target: Id) {
        Timber.d("onTogglePlaceholderClicked, target:[$target]")
        if (mode == EditorMode.Edit) {
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
    }

    fun onToggleClicked(target: Id) {
        Timber.d("onToggleClicked, target:[$target]")
        if (mode is EditorMode.Edit || mode is EditorMode.Locked) {
            onToggleChanged(target)
            viewModelScope.launch { refresh() }
        }
    }

    private fun onAddLocalFileClicked(blockId: String) {
        currentMediaUploadDescription = Media.Upload.Description(blockId, Mimetype.MIME_FILE_ALL)
        dispatch(Command.OpenGallery(mimeType = Mimetype.MIME_FILE_ALL))
    }

    fun onAddFileBlockClicked(type: Content.File.Type) {
        Timber.d("onAddFileBlockClicked, type:[$type]")
        val focused = orchestrator.stores.focus.current().targetOrNull()
        val target = if (focused != null)
            blocks.find { it.id == focused }
        else
            null
        if (target != null) {
            val content = target.content
            if (content is Content.Text && content.text.isEmpty()) {
                proceedWithReplacingByEmptyFileBlock(
                    id = target.id,
                    type = type
                )
            } else {
                proceedWithCreatingEmptyFileBlock(
                    id = target.id,
                    type = type,
                    position = Position.BOTTOM
                )
            }
        } else {
            Timber.e("Missing focus while onAddFileBlockClicked")
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

        val update = blocks.map { block ->
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

        orchestrator.stores.document.update(update)

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

        val update = blocks.map { block ->
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

        orchestrator.stores.document.update(update)

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
        val focus = orchestrator.stores.focus.current().targetOrNull()
        val targetId = focus.orEmpty()
        if (targetId.isNotEmpty()) {
            when (val targetView = views.singleOrNull { it.id == targetId }) {
                is BlockView.Description -> sendToast(CANNOT_OPEN_STYLE_PANEL_FOR_DESCRIPTION)
                is BlockView.Code -> {
                    val selection = orchestrator.stores.textSelection.current().selection
                    if (selection != null && selection.first != selection.last) {
                        sendToast(CANNOT_OPEN_STYLE_PANEL_FOR_CODE_BLOCK_ERROR)
                    } else {
                        proceedWithStyleToolbarEvent(targetView)
                    }
                }
                is BlockView -> proceedWithStyleToolbarEvent(targetView)
                else -> {
                    Timber.w("Failed to handle toolbar style click. Can't find targetView by id $targetId")
                }
            }
        } else {
            Timber.w("Failed to handle toolbar style click. Unknown focus for style toolbar: $focus")
        }
        viewModelScope.sendAnalyticsStyleMenuEvent(analytics)
    }

    private fun proceedWithStyleToolbarEvent(target: BlockView) {
        val targetId = target.id
        val targetBlock = blocks.find { it.id == targetId }
        if (targetBlock != null) {
            when (val content = targetBlock.content) {
                is Content.Text -> {
                    mode = EditorMode.Styling.Single(
                        target = targetId,
                        cursor = orchestrator.stores.textSelection.current().selection?.first
                    )
                    viewModelScope.launch {
                        orchestrator.stores.focus.update(Editor.Focus.empty())
                        orchestrator.stores.views.update(views.singleStylingMode(targetId))
                        renderCommand.send(Unit)
                    }
                    when {
                        target is BlockView.Title -> onShowColorBackgroundToolbarEvent(
                            ids = listOf(targetId),
                            navigatedFromCellsMenu = false,
                            navigateFromStylingTextToolbar = false
                        )
                        content.style == Content.Text.Style.CODE_SNIPPET -> {
                            val state = targetBlock.getStyleBackgroundToolbarState()
                            controlPanelInteractor.onEvent(
                                ControlPanelMachine.Event.StylingToolbar.OnUpdateBackgroundToolbar(
                                    state
                                )
                            )
                        }
                        else -> {
                            val styleState = content.getStyleTextToolbarState()
                            controlPanelInteractor.onEvent(
                                ControlPanelMachine.Event.StylingToolbar.OnUpdateTextToolbar(
                                    styleState
                                )
                            )
                        }
                    }
                }
                else -> {
                    Timber.w("Failed to open style menu. Block content must be Text but was ${content.javaClass}")
                    sendToast("Failed to open style menu. Block content mustbe Text")
                }
            }
        } else {
            Timber.w("Failed to open style menu. Can't find target block: $target")
            sendToast("Failed to open style menu. Can't find target block")
        }
    }

    private fun proceedWithMultiStyleToolbarEvent() {
        val selected = blocks.filter { currentSelection().contains(it.id) }
        val isAllTextAndNoneCodeBlocks = selected.isAllTextAndNoneCodeBlocks()
        mode = EditorMode.Styling.Multi(currentSelection())
        if (isAllTextAndNoneCodeBlocks) {
            val styleState = selected.map { it.content.asText() }.getStyleTextToolbarState()
            controlPanelInteractor.onEvent(
                ControlPanelMachine.Event.StylingToolbar.OnUpdateTextToolbar(styleState)
            )
        } else {
            val styleState = selected.getStyleBackgroundToolbarState()
            controlPanelInteractor.onEvent(
                ControlPanelMachine.Event.StylingToolbar.OnUpdateBackgroundToolbar(styleState)
            )
        }
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
                        target = Editor.Focus.Target.Block(target),
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
                    views.updateCursorAndEditMode(
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
                        target = Editor.Focus.Target.Block(target),
                        cursor = cursor?.let { c -> Editor.Cursor.Range(c..c) }
                    )
                )
                orchestrator.stores.textSelection.update(
                    Editor.TextSelection(target, cursor?.let { it..it })
                )
                controlPanelInteractor.onEvent(ControlPanelMachine.Event.StylingToolbar.OnExit)
                orchestrator.stores.views.update(
                    views.updateCursorAndEditMode(
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
            ControlPanelMachine.Event.OtherToolbar.Hide
        )
    }

    fun onCloseBlockStyleColorToolbarClicked() {
        Timber.d("onCloseBlockStyleColorToolbarClicked, ")
        val focused = !orchestrator.stores.focus.current().isEmpty
        controlPanelInteractor.onEvent(
            ControlPanelMachine.Event.ColorBackgroundToolbar.Hide(focused = focused)
        )
    }

    fun onCloseBlockStyleBackgroundToolbarClicked() {
        Timber.d("onCloseBlockStyleColorToolbarClicked, ")
        onCloseBlockStyleToolbarClicked()
    }

    fun onBlockToolbarBlockActionsClicked() {
        Timber.d("onBlockToolbarBlockActionsClicked, ")
        val target = orchestrator.stores.focus.current().targetOrNull()
        val view = if (target != null) views.find { it.id == target } else null
        if (view == null) {
            val cell = if (target != null) views.findTableCellView(target) else null
            if (cell != null) {
                proceedWithEnterTableMode(cell)
                viewModelScope.sendAnalyticsSelectionMenuEvent(analytics)
            }
        } else {
            when (view) {
                is BlockView.Title -> {
                    sendToast(CANNOT_OPEN_ACTION_MENU_FOR_TITLE_ERROR)
                }
                is BlockView.Description -> {
                    sendToast(CANNOT_OPEN_ACTION_MENU_FOR_DESCRIPTION)
                }
                else -> {
                    proceedWithEnteringActionMode(target = view.id, scrollTarget = false)
                }
            }
            viewModelScope.sendAnalyticsSelectionMenuEvent(analytics)
        }
    }

    fun onEnterScrollAndMoveClicked() {
        Timber.d("onEnterScrollAndMoveClicked, ")
        mode = EditorMode.SAM
        controlPanelInteractor.onEvent(ControlPanelMachine.Event.SAM.OnEnter)
    }

    fun onExitScrollAndMoveClicked() {
        Timber.d("onExitScrollAndMoveClicked, ")
        if (controlPanelViewState.value?.multiSelect?.isQuickScrollAndMoveMode == true) {
            clearSelections()
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
    }

    private fun onExitActionMode() {
        mode = EditorMode.Edit
        controlPanelInteractor.onEvent(ControlPanelMachine.Event.ReadMode.OnExit)
        viewModelScope.launch { refresh() }
    }

    // ----------------- Turn Into -----------------------------------------

    private fun onTurnIntoBlockClicked(target: String, uiBlock: UiBlock) {
        Timber.d("onTurnIntoBlockClicked, taget:[$target] uiBlock:[$uiBlock]")
        proceedUpdateBlockStyle(
            targets = listOf(target),
            uiBlock = uiBlock,
            errorAction = { sendToast("Cannot convert block to $uiBlock") }
        )
    }

    fun onUpdateTextBlockStyle(uiBlock: UiBlock) {
        Timber.d("onUpdateSingleTextBlockStyle, uiBlock:[$uiBlock]")
        val ids = mode.getIds()
        if (ids.isNullOrEmpty()) return
        proceedUpdateBlockStyle(
            targets = ids,
            uiBlock = uiBlock,
            errorAction = { sendToast("Cannot convert block to $uiBlock") }
        )
    }

    fun onBlockStyleToolbarOtherClicked() {
        Timber.d("onBlockStyleToolbarOtherClicked, ")
        val ids = mode.getIds()
        if (ids.isNullOrEmpty()) return
        onShowStyleOtherToolbarEvent(
            ids = ids,
            navigatedFromCellsMenu = false,
            navigateFromStylingTextToolbar = true
        )
    }

    fun onBlockStyleToolbarColorClicked() {
        Timber.d("onBlockStyleToolbarColorClicked, ")
        val ids = mode.getIds()
        if (ids.isNullOrEmpty()) return
        onShowColorBackgroundToolbarEvent(
            ids = ids,
            navigatedFromCellsMenu = false,
            navigateFromStylingTextToolbar = true
        )
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
            UiBlock.TOGGLE, UiBlock.CODE,
            UiBlock.CALLOUT -> {
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
            UiBlock.LINK_TO_OBJECT,
            UiBlock.FILE,
            UiBlock.IMAGE,
            UiBlock.VIDEO,
            UiBlock.BOOKMARK,
            UiBlock.RELATION -> errorAction?.invoke()
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
                    style = style,
                    analyticsContext = analyticsContext
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
        val focus = orchestrator.stores.focus.current().targetOrNull() ?: return
        val focused = blocks.find { it.id == focus } ?: return

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

    private fun addTableOfContentsBlock() {
        val focus = orchestrator.stores.focus.current().targetOrNull() ?: return
        val focused = blocks.find { it.id == focus } ?: return

        val content = focused.content
        val prototype = Prototype.TableOfContents

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

    private fun addSimpleTableBlock(item: SlashItem.Other.Table) {
        val focus = orchestrator.stores.focus.current().targetOrNull() ?: return
        val focused = blocks.find { it.id == focus } ?: return

        val content = focused.content

        if (content is Content.Text && content.text.isEmpty()) {
            viewModelScope.launch {
                orchestrator.proxies.intents.send(
                    Intent.Table.CreateTable(
                        ctx = context,
                        target = focused.id,
                        position = Position.REPLACE,
                        rows = item.rowCount,
                        columns = item.columnCount
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
                    Intent.Table.CreateTable(
                        ctx = context,
                        target = target,
                        position = position
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

        sendHideObjectTypeWidgetEvent()

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
                        content.style == Content.Text.Style.CODE_SNIPPET -> addNewBlockAtTheEnd()
                        content.text.isNotEmpty() -> addNewBlockAtTheEnd()
                        content.text.isEmpty() -> {
                            val stores = orchestrator.stores
                            if (stores.focus.current().isEmpty) {
                                val focus = Editor.Focus(
                                    target = Editor.Focus.Target.Block(last.id),
                                    cursor = null
                                )
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
                is Content.Table -> {
                    addNewBlockAtTheEnd()
                }
                is Content.TableOfContents -> {
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
        viewModelScope.launch { analytics.sendHideKeyboardEvent() }
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
            Timber.d("----------Blocks dispatched to render pipeline----------")
        }
        renderizePipeline.send(blocks)
    }

    private fun onPageClicked(blockLinkId: Id) {
        if (isObjectTemplate()) return
        val block = blocks.firstOrNull { it.id == blockLinkId }
        when (val content = block?.content) {
            is Content.Link -> {
                proceedWithOpeningObjectByLayout(target = content.target)
            }
            is Content.Bookmark -> {
                val target = content.targetObjectId
                val details = orchestrator.stores.details.current().details[target]
                if (target != null) {
                    val obj = ObjectWrapper.Bookmark(details?.map ?: mapOf())
                    if (obj.isArchived != true && obj.isDeleted != true) {
                        proceedWithOpeningObjectByLayout(target = target)
                    } else {
                        val source = obj.source
                        if (!source.isNullOrBlank()) {
                            commands.postValue(
                                EventWrapper(
                                    Command.Browse(source)
                                )
                            )
                        } else {
                            sendToast("Source is missing for this object")
                        }
                    }
                } else {
                    sendToast("Couldn't find the target of the link")
                }
            }
            is Content.DataView -> {
                proceedWithOpeningDataViewBlock(dv = content)
            }
            else -> {
                sendToast("Couldn't find the target of the link")
            }
        }
    }

    private fun proceedWithOpeningDataViewBlock(dv: Content.DataView) {
        if (dv.targetObjectId.isNotEmpty()) {
            proceedWithOpeningDataViewObject(dv.targetObjectId)
            viewModelScope.sendAnalyticsOpenAsObject(
                analytics = analytics,
                type = EventsDictionary.Type.dataView
            )
        } else {
            val toastMessage = if (dv.isCollection) {
                "This inline collection doesn't have a source"
            } else {
                "This inline set doesn't have a source"
            }
            sendToast(toastMessage)
        }
    }

    private fun proceedWithOpeningObjectByLayout(target: String) {
        proceedWithClearingFocus()
        val details = orchestrator.stores.details.current()
        val wrapper = ObjectWrapper.Basic(map = details.details[target]?.map ?: emptyMap())
        when (wrapper.layout) {
            ObjectType.Layout.BASIC,
            ObjectType.Layout.PROFILE,
            ObjectType.Layout.NOTE,
            ObjectType.Layout.TODO,
            ObjectType.Layout.FILE,
            ObjectType.Layout.BOOKMARK -> {
                proceedWithOpeningObject(target = target)
            }
            ObjectType.Layout.SET, ObjectType.Layout.COLLECTION -> {
                proceedWithOpeningDataViewObject(target = target)
            }
            else -> {
                sendToast("Cannot open object with layout: ${wrapper.layout}")
            }
        }
    }

    private fun onAddNewObjectClicked(type: String) {
        controlPanelInteractor.onEvent(ControlPanelMachine.Event.OnAddBlockToolbarOptionSelected)

        val position: Position

        val focused = blocks.first { it.id == orchestrator.stores.focus.current().targetOrNull() }

        var target = focused.id

        if (focused.id == context) {
            if (focused.children.isEmpty())
                position = Position.INNER
            else {
                position = Position.TOP
                target = focused.children.first()
            }
        } else {
            val content = focused.content
            position = if (content is Content.Text) {
                if (content.text.isEmpty() || content.text == "/") {
                    Position.REPLACE
                } else {
                    Position.BOTTOM
                }
            } else {
                Position.REPLACE
            }
        }

        val params = CreateBlockLinkWithObject.Params(
            context = context,
            position = position,
            target = target,
            type = type
        )

        val startTime = System.currentTimeMillis()

        viewModelScope.launch {
            createBlockLinkWithObject.async(
                params = params
            ).fold(
                onFailure = {
                    Timber.e(it, "Error while creating new object with params: $params")
                },
                onSuccess = { result ->
                    orchestrator.proxies.payloads.send(result.payload)
                    sendAnalyticsObjectCreateEvent(
                        analytics = analytics,
                        type = type,
                        storeOfObjectTypes = storeOfObjectTypes,
                        route = EventsDictionary.Routes.objPowerTool,
                        startTime = startTime
                    )
                    proceedWithOpeningObject(result.objectId)
                }
            )
        }
    }


    fun onAddNewDocumentClicked() {

        Timber.d("onAddNewDocumentClicked, ")

        val startTime = System.currentTimeMillis()
        jobs += viewModelScope.launch {
            createObject.async(CreateObject.Param(type = null))
                .fold(
                    onSuccess = { result ->
                        sendAnalyticsObjectCreateEvent(
                            analytics = analytics,
                            type = result.type,
                            storeOfObjectTypes = storeOfObjectTypes,
                            route = EventsDictionary.Routes.navigation,
                            view = EventsDictionary.View.viewNavbar,
                            startTime = startTime
                        )
                        proceedWithOpeningObject(result.objectId)
                    },
                    onFailure = { e -> Timber.e(e, "Error while creating a new page") }
                )
        }
    }

    fun onAddCoverClicked() {
        Timber.d("onAddCoverClicked, ")
        if (mode != EditorMode.Locked) {
            dispatch(Command.OpenCoverGallery(context))
        } else {
            sendToast("Cannot change cover: your object is locked.")
        }
    }

    fun onLayoutClicked() {
        Timber.d("onLayoutClicked, ")
        dispatch(Command.OpenObjectLayout(context))
    }

    fun onLayoutDialogDismissed() {
        Timber.d("onLayoutDialogDismissed, ")
        proceedWithOpeningObjectMenu()
    }

    fun onAddBookmarkBlockClicked() {
        Timber.d("onAddBookmarkBlockClicked, ")

        val focus = orchestrator.stores.focus.current().targetOrNull() ?: return
        val focused = blocks.find { it.id == focus } ?: return

        val content = focused.content

        if (content is Content.Text && content.text.isEmpty()) {
            viewModelScope.launch {
                orchestrator.proxies.intents.send(
                    Intent.CRUD.Replace(
                        context = context,
                        target = focused.id,
                        prototype = Prototype.Bookmark.New
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
                        prototype = Prototype.Bookmark.New
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
        proceedWithBookmarkSetter(target)
    }

    private fun proceedWithBookmarkSetter(target: String, currentValue: String? = null) {
        dispatch(
            command = Command.OpenBookmarkSetter(
                context = context,
                target = target,
                url = currentValue
            )
        )
    }

    private fun onBookmarkClicked(view: BlockView.Media.Bookmark) =
        dispatch(command = Command.Browse(view.url))

    private fun onFailedBookmarkClicked(view: BlockView.Error.Bookmark) {
        if (view.url.isBlank()) {
            proceedWithBookmarkSetter(target = view.id)
        } else {
            proceedWithBookmarkSetter(target = view.id, view.url)
        }
    }

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
        proceedWithUpdatingActionsForCurrentSelection()
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

            if (currentSelection().isNotEmpty()) {
                onMultiSelectModeBlockClicked()
                val update = state.blocks.map { view ->
                    if (view.id == target || descendants.contains(view.id))
                        view.updateSelection(newSelection = isSelected(target))
                    else
                        view
                }
                stateData.postValue(ViewState.Success(update))
                if (isSelected(target)) {
                    dispatch(Command.ScrollToActionMenu(target = target))
                }
            } else {
                proceedWithExitingMultiSelectMode()
            }
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
                    focus = orchestrator.stores.focus.current().targetOrNull().orEmpty(),
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
                sendToast(CANNOT_BE_DROPPED_INSIDE_ITSELF_ERROR)
            } else if (selected.size == 1) {
                sendToast(CANNOT_MOVE_BLOCK_ON_SAME_POSITION)
            }
            return
        }

        if (selected.contains(parent)) {
            sendToast(CANNOT_MOVE_PARENT_INTO_CHILD)
            return
        }

        if (position == Position.INNER) {

            if (!targetBlock.supportNesting()) {
                sendToast(CANNOT_BE_PARENT_ERROR)
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

            mode = EditorMode.Edit

            controlPanelInteractor.onEvent(ControlPanelMachine.Event.SAM.OnApply)

            viewModelScope.launch {
                val blocks = (selected - exclude).sortedBy { id -> ordering[id] }
                orchestrator.proxies.intents.send(
                    Intent.Document.Move(
                        context = context,
                        target = moveTarget,
                        targetContext = targetContext,
                        blocks = blocks,
                        position = position
                    )
                )
                sendAnalyticsBlockReorder(
                    analytics = analytics,
                    count = blocks.size,
                    context = analyticsContext
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

            mode = EditorMode.Edit

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

    fun onCopy(range: IntRange?) {
        Timber.d("onCopy, ")
        viewModelScope.launch {
            val target = blocks.find { it.id == focus.value }
            if (target != null) {
                orchestrator.proxies.intents.send(
                    Intent.Clipboard.Copy(
                        context = context,
                        range = range,
                        blocks = listOf(target)
                    )
                )
            } else {
                Timber.e("Error while copying: target not found").also {
                    sendToast("Something went wrong. Please try again.")
                }
            }
        }
    }

    fun onBookmarkPasted(url: Url) {
        Timber.d("onBookmarkPasted $url")
        val focus = orchestrator.stores.focus.current()
        if (!focus.isEmpty) {
            viewModelScope.launch {
                orchestrator.proxies.intents.send(
                    Intent.Bookmark.CreateBookmark(
                        context = context,
                        target = focus.requireTarget(),
                        position = Position.TOP,
                        url = url
                    )
                )
            }
        }
    }

    fun onClickListener(clicked: ListenerType) {
        Timber.d("onClickListener, clicked:[$clicked]")
        if (mode is EditorMode.Styling) {
            onExitBlockStyleToolbarClicked()
            return
        }
        isUndoRedoToolbarIsVisible.value = false
        when (clicked) {
            is ListenerType.Bookmark.View -> {
                when (mode) {
                    EditorMode.Edit -> {
                        onBookmarkClicked(clicked.item)
                        viewModelScope.sendAnalyticsBookmarkOpen(analytics)
                    }
                    EditorMode.Locked -> onBookmarkClicked(clicked.item)
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
                    EditorMode.Locked -> onFileClicked(clicked.target)
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
                    EditorMode.Edit, EditorMode.Locked -> {
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
                    EditorMode.Edit -> onAddLocalPictureClicked(clicked.target)
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
                    EditorMode.Edit -> onAddLocalVideoClicked(clicked.target)
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
            is ListenerType.ProfileImageIcon -> {
                controlPanelInteractor.onEvent(ControlPanelMachine.Event.OnDocumentIconClicked)
                dispatch(Command.OpenDocumentImagePicker(Mimetype.MIME_IMAGE_ALL))
            }
            is ListenerType.LongClick -> {
                when (mode) {
                    EditorMode.Edit -> proceedWithEnteringActionMode(clicked.target)
                    EditorMode.Select -> onBlockMultiSelectClicked(target = clicked.target)
                    else -> Unit
                }
            }
            is ListenerType.CellLongClick -> {
                when (mode) {
                    EditorMode.Edit -> proceedWithEnteringActionMode(clicked.tableId)
                    EditorMode.Select -> onBlockMultiSelectClicked(target = clicked.tableId)
                    else -> Unit
                }
            }
            is ListenerType.LinkToObject -> {
                when (mode) {
                    EditorMode.Edit -> onPageClicked(blockLinkId = clicked.target)
                    EditorMode.Locked -> onPageClicked(blockLinkId = clicked.target)
                    EditorMode.Select -> onBlockMultiSelectClicked(clicked.target)
                    else -> Unit
                }
            }
            is ListenerType.LinkToObjectArchived -> {
                when (mode) {
                    EditorMode.Select -> onBlockMultiSelectClicked(clicked.target)
                    else -> Unit
                }
            }
            is ListenerType.LinkToObjectDeleted -> {
                when (mode) {
                    EditorMode.Edit -> Unit
                    EditorMode.Select -> onBlockMultiSelectClicked(clicked.target)
                    else -> Unit
                }
            }
            is ListenerType.LinkToObjectLoading -> {
                when (mode) {
                    EditorMode.Edit -> Unit
                    EditorMode.Select -> onBlockMultiSelectClicked(clicked.target)
                    else -> Unit
                }
            }
            is ListenerType.LinkToObjectCheckboxUpdate -> {
                when (mode) {
                    EditorMode.Edit -> {
                        val content = blocks.firstOrNull { it.id == clicked.target }?.content
                        if (content is Content.Link) {
                            proceedWithSetObjectDetails(
                                ctx = content.target,
                                key = Relations.DONE,
                                value = !clicked.isChecked
                            )
                        }
                    }
                    else -> Unit
                }
            }
            is ListenerType.Mention -> {
                when (mode) {
                    EditorMode.Edit, EditorMode.Locked -> {
                        viewModelScope.launch {
                            orchestrator.stores.focus.update(Editor.Focus.empty())
                        }
                        onMentionClicked(clicked.target)
                    }
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
            is ListenerType.Latex -> {
                when (mode) {
//                    EditorMode.Edit -> proceedWithEnteringActionMode(clicked.id)
                    EditorMode.Select -> onBlockMultiSelectClicked(clicked.id)
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
                        Command.OpenObjectRelationScreen.RelationAdd(
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
                    sendToast(NOT_ALLOWED_FOR_RELATION)
                    Timber.d("No interaction allowed with this relation")
                    return
                }
                when (mode) {
                    EditorMode.Edit, EditorMode.Locked -> {
                        when (clicked.value) {
                            is BlockView.Relation.Placeholder -> {
                                Timber.d("Clicked in BlockView.Relation.Placeholder")
                            }
                            is BlockView.Relation.Related -> {
                                val relationView = clicked.value.view
                                proceedWithRelationBlockClicked(
                                    relationView = relationView
                                )
                            }
                            is BlockView.Relation.Deleted -> {
                                Timber.d("Clicked in BlockView.Relation.Deleted")
                            }
                        }
                    }
                    else -> onBlockMultiSelectClicked(clicked.value.id)
                }
            }
            is ListenerType.Relation.Featured -> {
                val restrictions = orchestrator.stores.objectRestrictions.current()
                if (restrictions.contains(ObjectRestriction.RELATIONS)) {
                    sendToast(NOT_ALLOWED_FOR_RELATION)
                    return
                }
                when (mode) {
                    EditorMode.Edit, EditorMode.Locked -> {
                        viewModelScope.launch {
                            val relationId = clicked.relation.id
                            val relation = storeOfRelations.getById(relationId)
                            if (relation != null) {
                                if (relation.isReadOnly == true || relation.isReadonlyValue) {
                                    sendToast(NOT_ALLOWED_FOR_RELATION)
                                    return@launch
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
                                                relationKey = relationId,
                                                isLocked = mode == EditorMode.Locked
                                            )
                                        )
                                    }
                                    Relation.Format.CHECKBOX -> {
                                        val view = clicked.relation
                                        if (view is ObjectRelationView.Checkbox) {
                                            proceedWithSetObjectDetails(
                                                ctx = context,
                                                key = relationId,
                                                value = !view.isChecked
                                            )
                                        }
                                    }
                                    Relation.Format.DATE -> {
                                        dispatch(
                                            Command.OpenObjectRelationScreen.Value.Date(
                                                ctx = context,
                                                target = context,
                                                relationKey = relationId
                                            )
                                        )
                                    }
                                    else -> {
                                        dispatch(
                                            Command.OpenObjectRelationScreen.Value.Default(
                                                ctx = context,
                                                target = context,
                                                relationKey = relationId,
                                                targetObjectTypes = relation.relationFormatObjectTypes,
                                                isLocked = mode == EditorMode.Locked
                                            )
                                        )
                                    }
                                }
                                proceedWithRelationBlockClicked(
                                    relationView = clicked.relation
                                )
                            }
                        }
                    }
                    else -> {
                        // Do nothing
                    }
                }
            }
            is ListenerType.TableOfContentsItem -> {
                when (mode) {
                    EditorMode.Select -> onBlockMultiSelectClicked(clicked.target)
                    EditorMode.Edit, EditorMode.Locked -> {
                        val block = views.find { it.id == clicked.item }
                        val pos = views.indexOf(block)
                        if (pos != NO_SCROLL_POSITION) {
                            commands.value = EventWrapper(Command.ScrollToPosition(pos))
                        }
                    }
                    else -> Unit
                }
            }
            is ListenerType.TableOfContents -> {
                when (mode) {
                    EditorMode.Select -> onBlockMultiSelectClicked(clicked.target)
                    else -> Unit
                }
            }
            is ListenerType.Callout.Icon -> {
                dispatch(Command.OpenTextBlockIconPicker(clicked.blockId))
            }
            is ListenerType.TableEmptyCell -> {
                when (val m = mode) {
                    EditorMode.Edit -> {
                        onTableRowEmptyCellClicked(
                            cellId = clicked.cell.getId(),
                            rowId = clicked.cell.row.id.value
                        )
                    }
                    EditorMode.Select -> {
                        onBlockMultiSelectClicked(
                            target = clicked.cell.tableId
                        )
                    }
                    is EditorMode.Table -> {
                        val modeTableId = m.tableId
                        val cellTableId = clicked.cell.tableId
                        if (cellTableId == modeTableId) {
                            onTableRowEmptyCellClicked(
                                cellId = clicked.cell.getId(),
                                rowId = clicked.cell.row.id.value
                            )
                            proceedWithClickingOnCellInTableMode(
                                cell = clicked.cell,
                                modeTable = m
                            )
                        } else {
                            Timber.e("Cell is from the different table, amend click")
                        }
                    }
                    else -> Unit
                }
            }
            is ListenerType.TableTextCell -> {
                when (val m = mode) {
                    EditorMode.Select -> {
                        onBlockMultiSelectClicked(target = clicked.cell.tableId)
                    }
                    is EditorMode.Table -> {
                        val modeTableId = m.tableId
                        val cellTableId = clicked.cell.tableId
                        if (cellTableId == modeTableId) {
                            proceedWithClickingOnCellInTableMode(
                                cell = clicked.cell,
                                modeTable = m
                            )
                        } else {
                            Timber.e("Cell is from the different table, amend click")
                        }
                    }
                    else -> Unit
                }
            }
            is ListenerType.DataViewClick -> {
                when (mode) {
                    EditorMode.Edit -> onPageClicked(blockLinkId = clicked.target)
                    EditorMode.Locked -> onPageClicked(blockLinkId = clicked.target)
                    EditorMode.Select -> onBlockMultiSelectClicked(clicked.target)
                    else -> Unit
                }
            }
            is ListenerType.Relation.ObjectType -> {
                if (isObjectTemplate()) return
                viewModelScope.launch {
                    val params = FindObjectSetForType.Params(
                        type = clicked.typeId,
                        filters = ObjectSearchConstants.setsByObjectTypeFilters(
                            types = listOf(clicked.typeId),
                            space = workspaceManager.getCurrentWorkspace()
                        )
                    )
                    findObjectSetForType(params).process(
                        failure = {
                            Timber.e(it, "Error while search for a set for type ${clicked.typeId}")
                        },
                        success = { response ->
                            val command = when (response) {
                                is FindObjectSetForType.Response.NotFound ->
                                    Command.OpenObjectTypeMenu(clicked.items())
                                is FindObjectSetForType.Response.Success ->
                                    Command.OpenObjectTypeMenu(clicked.items(set = response.obj.id))
                            }
                            commands.postValue(EventWrapper(command))
                        }
                    )
                }
            }
            is ListenerType.Relation.ObjectTypeDeleted -> {
                commands.postValue(EventWrapper(Command.OpenObjectTypeMenu(clicked.items())))
            }
            else -> {}
        }
    }

    fun onChangeObjectTypeClicked() {
        if (mode != EditorMode.Locked) {
            val restrictions = orchestrator.stores.objectRestrictions.current()
            if (restrictions.contains(ObjectRestriction.TYPE_CHANGE)) {
                sendToast(NOT_ALLOWED_FOR_OBJECT)
                Timber.d("No interaction allowed with this object type")
                return
            }
            proceedWithOpeningSelectingObjectTypeScreen()
        } else {
            sendToast("Your object is locked. To change its type, simply unlock it.")
        }
    }

    private fun proceedWithTogglingBlockRelationCheckbox(
        view: ObjectRelationView.Checkbox,
        relation: Id
    ) {
        proceedWithSetObjectDetails(
            ctx = context,
            key = relation,
            value = !view.isChecked
        )
    }

    override fun onProceedWithFilePath(filePath: String?) {
        Timber.d("onProceedWithFilePath, filePath:[$filePath]")
        if (filePath == null) {
            Timber.w("Error while getting filePath")
            return
        }
        viewModelScope.launch {
            val uploadDescription = currentMediaUploadDescription
            if (uploadDescription != null) {
                orchestrator.proxies.intents.send(
                    Media.Upload(
                        context = context,
                        description = uploadDescription,
                        filePath = filePath,
                        url = "",
                    )
                )
            } else {
                Timber.w("Failed to upload file $filePath. uploadDescription==null")
            }
        }
    }

    fun onRestoreSavedState(uploadMediaDescription: Media.Upload.Description?) {
        currentMediaUploadDescription = uploadMediaDescription
    }

    fun onPageIconClicked() {
        Timber.d("onPageIconClicked, ")
        if (mode == EditorMode.Locked) {
            sendToast("Unlock your object to change its icon")
            return
        }
        val restrictions = orchestrator.stores.objectRestrictions.current()
        val isDetailsAllowed = restrictions.none { it == ObjectRestriction.DETAILS }
        if (isDetailsAllowed) {
            controlPanelInteractor.onEvent(ControlPanelMachine.Event.OnDocumentIconClicked)
            dispatch(Command.OpenDocumentEmojiIconPicker)
        } else {
            sendToast(NOT_ALLOWED_FOR_OBJECT)
        }
    }

    private fun onFileClicked(id: String) {
        val file = blocks.find { it.id == id }
        if (file != null && file.content is Content.File) {
            val cnt = (file.content as Content.File)
            dispatch(
                Command.OpenFileByDefaultApp(
                    id = id,
                    mime = cnt.mime.orEmpty(),
                    uri = urlBuilder.file(cnt.hash)
                )
            )
        }
    }

    fun startSharingFile(id: String, onDownloaded: (Uri) -> Unit = {}) {

        Timber.d("startDownloadingFile, id:[$id]")

        sendToast("Preparing file to share...")

        val block = blocks.firstOrNull { it.id == id }
        val content = block?.content

        if (content is Content.File && content.state == Content.File.State.DONE) {
            viewModelScope.launch {
                orchestrator.proxies.intents.send(
                    Media.ShareFile(
                        hash = content.hash.orEmpty(),
                        name = content.name.orEmpty(),
                        type = content.type,
                        onDownloaded = onDownloaded
                    )
                )
            }
        } else {
            Timber.e("Block is not File or with wrong state, can't proceed with share!")
        }
    }

    fun startDownloadingFile(id: String) {

        Timber.d("startDownloadingFile, id:[$id]")

        sendToast("Downloading file in background...")

        val block = blocks.firstOrNull { it.id == id }
        val content = block?.content

        if (content is Content.File && content.state == Content.File.State.DONE) {
            viewModelScope.launch {
                orchestrator.proxies.intents.send(
                    Media.DownloadFile(
                        url = when (content.type) {
                            Content.File.Type.IMAGE -> urlBuilder.image(content.hash)
                            else -> urlBuilder.file(content.hash)
                        },
                        name = content.name.orEmpty(),
                        type = content.type
                    )
                )
            }
        } else {
            Timber.e("Block is not File or with wrong state, can't proceed with download")
        }
    }

    private fun startDownloadingFiles(ids: List<String>) {
        Timber.d("startDownloadingFiles, ids:[$ids]")
        ids.forEach { id -> startDownloadingFile(id) }
    }

    fun onPageSearchClicked() {

        Timber.d("onPageSearchClicked, ")

        viewModelScope.sendEvent(
            analytics = analytics,
            eventName = searchScreenShow,
            props = Props(mapOf(EventsPropertiesKey.context to analyticsContext))
        )
        navigation.postValue(EventWrapper(AppNavigation.Command.OpenPageSearch))
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

    fun proceedWithOpeningObject(target: Id) {
        viewModelScope.launch {
            closePage.async(context).fold(
                onFailure = {
                    Timber.e(it, "Error while closing object")
                    navigate(EventWrapper(AppNavigation.Command.OpenObject(target)))
                },
                onSuccess = {
                    navigate(EventWrapper(AppNavigation.Command.OpenObject(target)))
                }
            )
        }
    }

    fun proceedWithOpeningDataViewObject(target: Id, isPopUpToDashboard: Boolean = false) {
        viewModelScope.launch {
            closePage.async(context).fold(
                onFailure = {
                    Timber.e(it, "Error while closing object")
                    navigate(
                        EventWrapper(
                            AppNavigation.Command.OpenSetOrCollection(
                                target,
                                isPopUpToDashboard
                            )
                        )
                    )
                },
                onSuccess = {
                    navigate(
                        EventWrapper(
                            AppNavigation.Command.OpenSetOrCollection(
                                target,
                                isPopUpToDashboard
                            )
                        )
                    )
                }
            )
        }
    }

    private fun proceedWithSetObjectDetails(ctx: Id, key: String, value: Any?) {
        viewModelScope.launch {
            updateDetail(
                UpdateDetail.Params(
                    target = ctx,
                    key = key,
                    value = value
                )
            ).process(
                success = {
                    dispatcher.send(it)
                    sendAnalyticsRelationValueEvent(
                        analytics = analytics,
                        context = analyticsContext
                    )
                },
                failure = {
                    Timber.e(it, "Error while set object details")
                }
            )
        }
    }

    private fun sendToast(msg: String) {
        jobs += viewModelScope.launch {
            _toasts.emit(msg)
        }
    }

    private fun sendSnack(snack: Snack) {
        jobs += viewModelScope.launch {
            snacks.emit(snack)
        }
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
            if (!orchestrator.stores.focus.current().isEmpty) {
                onHideKeyboardClicked()
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
        relationKey: Key
    ) {
        Timber.d("onRelationTextValueChanged, ctx:[$ctx] value:[$value] relationId:[$relationKey]")
        proceedWithSetObjectDetails(
            ctx = ctx,
            key = relationKey,
            value = value
        )
    }

    fun onObjectTypeChanged(
        type: Id,
        key: Key,
        applyTemplate: Boolean
    ) {
        Timber.d("onObjectTypeChanged, type:[$type], applyTemplate:[$applyTemplate]")
        proceedWithObjectTypeChange(
            type = type,
            key = key,
            applyTemplate = applyTemplate
        )
    }

    private fun proceedWithObjectTypeChange(
        type: Id,
        key: Key,
        applyTemplate: Boolean
    ) {
        viewModelScope.launch {
            when (type) {
                ObjectTypeIds.SET -> {
                    proceedWithConvertingToSet()
                }
                ObjectTypeIds.COLLECTION -> {
                    proceedWithConvertingToCollection()
                }
                else -> {
                    proceedWithUpdateObjectType(type = type, key = key)
                    sendAnalyticsObjectTypeChangeEvent(
                        analytics = analytics,
                        objType = storeOfObjectTypes.get(type)
                    )
                    sendHideObjectTypeWidgetEvent()
                    if (applyTemplate) {
                        proceedWithCheckingInternalFlagShouldSelectTemplate(objTypeId = type)
                    }
                }
            }
        }
    }

    private suspend fun proceedWithConvertingToSet() {
        val params = ConvertObjectToSet.Params(
            ctx = context,
            sources = emptyList()
        )
        objectToSet.async(params).fold(
            onFailure = { error -> Timber.e(error, "Error convert object to set") },
            onSuccess = {
                proceedWithOpeningDataViewObject(target = context, isPopUpToDashboard = true)
            }
        )
    }

    private suspend fun proceedWithConvertingToCollection() {
        val params = ConvertObjectToCollection.Params(ctx = context)
        objectToCollection.async(params).fold(
            onFailure = { error -> Timber.e(error, "Error convert object to collection") },
            onSuccess = {
                proceedWithOpeningDataViewObject(target = context, isPopUpToDashboard = true)
            }
        )
    }

    private suspend fun proceedWithUpdateObjectType(type: Id, key: Key) {
        val focus = orchestrator.stores.focus.current()
        val effects = buildList<SideEffect> {
            if (focus.targetOrNull() != null) {
                add(SideEffect.ResetFocusToFirstTextBlock)
            }
        }
        orchestrator.proxies.intents.send(
            Intent.Document.SetObjectType(
                context = context,
                typeId = type,
                key = key,
                effects = effects
            )
        )
    }

    companion object {
        const val NO_SEARCH_RESULT_POSITION = -1
        const val NO_SCROLL_POSITION = -1
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
        const val CANNOT_MOVE_PARENT_INTO_CHILD = "Cannot move parent into child."

        const val CANNOT_OPEN_ACTION_MENU_FOR_TITLE_ERROR =
            "Opening action menu for title currently not supported"
        const val CANNOT_OPEN_ACTION_MENU_FOR_DESCRIPTION =
            "Cannot open action menu for description"
        const val CANNOT_OPEN_STYLE_PANEL_FOR_DESCRIPTION =
            "Description block is text primitive and therefore no styling can be applied."
        const val CANNOT_OPEN_STYLE_PANEL_FOR_CODE_BLOCK_ERROR =
            "Opening style panel for code block currently not supported"

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

        Timber.d("onCleared, ")
    }

    fun onStop() {
        Timber.d("onStop, ")
        jobs.apply {
            forEach { it.cancel() }
            clear()
        }
        if (copyFileToCache.isActive()) {
            copyFileToCache.cancel()
        }
    }

    enum class Session { IDLE, OPEN, ERROR }

    //region SLASH WIDGET
    fun onStartSlashWidgetClicked() {
        dispatch(Command.AddSlashWidgetTriggerToFocusedBlock)
        viewModelScope.sendAnalyticsSlashMenuEvent(analytics)
    }

    fun onSlashItemClicked(item: SlashItem) {
        Timber.v("onSlashItemClicked, item:[$item]")
        val target = orchestrator.stores.focus.current()
        if (!target.isEmpty) {
            proceedWithSlashItem(item, target.requireTarget())
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
                    val mainItems = SlashExtensions.getSlashWidgetMainItems()
                    val widgetState = SlashWidgetState.UpdateItems.empty()
                        .copy(mainItems = mainItems)
                    val panelEvent = ControlPanelMachine.Event.Slash.OnFilterChange(
                        widgetState = widgetState
                    )
                    controlPanelInteractor.onEvent(panelEvent)
                    return
                }
                proceedWithGettingObjectTypes { objectTypes ->
                    getRelations { relations ->
                        val widgetState = SlashExtensions.getUpdatedSlashWidgetState(
                            text = event.filter,
                            objectTypes = objectTypes.toSlashItemView(),
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

    fun proceedWithAddingRelationToTarget(target: Id, relationKey: Key) {
        Timber.d("proceedWithAddingRelationToTarget, target:[$target], relation:[$relationKey]")
        val newBlockView = cutSlashFilterFromViews(target)

        // cut text from List<Block> and send TextUpdate Intent
        if (newBlockView != null) {
            cutSlashFilterFromBlocksAndSendUpdate(
                targetId = target,
                text = newBlockView.text,
                marks = newBlockView.marks.map { it.mark() }
            )
            onSlashRelationItemClicked(
                relationKey = relationKey,
                targetId = target,
                isBlockEmpty = newBlockView.text.isEmpty()
            )
        } else {
            Timber.e("cutSlashFilter error, BlockView is null on targetId:$target")
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
                proceedWithGettingObjectTypes {
                    proceedWithObjectTypes(it)
                }
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
                val color = if (blockColor != null) {
                    ThemeColor.valueOf(blockColor.toUpperCase())
                } else ThemeColor.DEFAULT
                val items =
                    listOf(SlashItem.Subheader.ColorWithBack) + SlashExtensions.getSlashWidgetColorItems(
                        color = color
                    )
                onSlashWidgetStateChanged(
                    SlashWidgetState.UpdateItems.empty().copy(
                        colorItems = items
                    )
                )
            }
            is SlashItem.Main.Background -> {
                val block = blocks.first { it.id == targetId }
                val blockBackground = block.backgroundColor
                val background = if (blockBackground == null) {
                    ThemeColor.DEFAULT
                } else {
                    ThemeColor.valueOf(blockBackground.toUpperCase())
                }
                val items = listOf(SlashItem.Subheader.BackgroundWithBack) +
                        SlashExtensions.getSlashWidgetBackgroundItems(
                            color = background
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
                        val type = item.convertToMarkType()
                        orchestrator.proxies.intents.send(
                            Intent.Text.UpdateMark(
                                context = context,
                                targets = listOf(targetId),
                                mark = Content.Text.Mark(
                                    range = IntRange(0, view.text.length),
                                    type = type
                                )
                            )
                        )
                        sendAnalyticsUpdateTextMarkupEvent(
                            analytics = analytics,
                            type = type,
                            context = analyticsContext
                        )
                    }
                }
            }
            is SlashItem.Media -> {
                // TODO join cutting and block creation operations and merge its payload changes
                // TODO unify focus handling
                cutSlashFilter(
                    targetId = targetId,
                    setPendingCursor = false,
                    clearFocusFromView = true
                )
                controlPanelInteractor.onEvent(ControlPanelMachine.Event.Slash.OnStopAndClearFocus)
                onSlashMediaItemClicked(item = item)
            }
            is SlashItem.ObjectType -> {
                cutSlashFilter(targetId = targetId)
                controlPanelInteractor.onEvent(ControlPanelMachine.Event.Slash.OnStop)
                onAddNewObjectClicked(
                    type = item.objectTypeView.id
                )
            }
            is SlashItem.Relation -> {
                val isBlockEmpty = cutSlashFilter(targetId = targetId)
                val relationKey = item.relation.view.key
                if (relationKey != null) {
                    onSlashRelationItemClicked(
                        relationKey = relationKey,
                        targetId = targetId,
                        isBlockEmpty = isBlockEmpty
                    )
                } else {
                    Timber.e("On create relation block error, relation key is null")
                }

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
            is SlashItem.Other.TOC -> {
                cutSlashFilter(targetId = targetId)
                controlPanelInteractor.onEvent(ControlPanelMachine.Event.Slash.OnStop)
                onHideKeyboardClicked()
                addTableOfContentsBlock()
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
            SlashItem.RelationNew -> {
                dispatch(
                    Command.OpenAddRelationScreen(ctx = context, target = targetId)
                )
            }
            is SlashItem.Other.Table -> {
                cutSlashFilter(targetId = targetId)
                controlPanelInteractor.onEvent(ControlPanelMachine.Event.Slash.OnStop)
                onHideKeyboardClicked()
                addSimpleTableBlock(item)
            }
        }
    }

    private fun cutSlashFilter(
        targetId: Id,
        setPendingCursor: Boolean = true,
        clearFocusFromView: Boolean = false
    ): Boolean {

        //saving cursor on slash start index
        if (setPendingCursor) {
            setPendingCursorToPosition(targetId = targetId, position = slashStartIndex)
        }

        // cut text from List<BlockView> and re-render views
        val newBlockView = cutSlashFilterFromViews(
            targetId = targetId,
            clearFocusFromView = clearFocusFromView
        )

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

    private fun cutSlashFilterFromViews(
        targetId: Id,
        clearFocusFromView: Boolean = false
    ): BlockView.Text? {
        Timber.d("cutSlashFilterFromViews, targetId:[$targetId], slashStartIndex:[$slashStartIndex], slashFilter:[$slashFilter]")
        val blockView = views.firstOrNull { it.id == targetId }
        if (blockView is BlockView.Text) {
            val new = blockView.cutPartOfText(
                from = slashStartIndex,
                partLength = slashFilter.length
            )
            if (clearFocusFromView) {
                new.isFocused = false
            }
            val update = views.update(new)
            viewModelScope.launch {
                orchestrator.stores.views.update(update)
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
        val update = blocks.updateTextContent(
            target = targetId,
            text = text,
            marks = marks
        )

        orchestrator.stores.document.update(update)

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
            target = Editor.Focus.Target.Block(targetId),
            cursor = cursor
        )
        viewModelScope.launch {
            orchestrator.stores.focus.update(focus)
        }
    }

    private fun proceedWithGettingObjectTypes(
        action: (List<ObjectTypeView>) -> Unit
    ) {
        viewModelScope.launch {
            val params = GetObjectTypes.Params(
                sorts = emptyList(),
                filters = buildList {
                    addAll(
                        ObjectSearchConstants.filterObjectTypeLibrary(
                            space = spaceManager.get()
                        )
                    )
                    add(
                        DVFilter(
                            relation = Relations.RECOMMENDED_LAYOUT,
                            condition = DVFilterCondition.IN,
                            value = SupportedLayouts.editorLayouts.map {
                                it.code.toDouble()
                            }
                        )
                    )
                },
                keys = ObjectSearchConstants.defaultKeysObjectType
            )
            getObjectTypes.async(params).fold(
                onFailure = { Timber.e(it, "Error while getting library object types") },
                onSuccess = { types ->
                    val views = types.getObjectTypeViewsForSBPage(
                        isWithCollection = false,
                        isWithBookmark = false,
                        selectedTypes = emptyList(),
                        excludeTypes = emptyList()
                    )
                    action.invoke(views)
                }
            )
        }
    }

    private fun getRelations(action: (List<SlashRelationView.Item>) -> Unit) {
        val details = orchestrator.stores.details.current()
        val objectDetails = details.details[context]?.map ?: emptyMap()
        val objectWrapper = ObjectWrapper.Basic(objectDetails)
        val objectType = objectWrapper.getProperType()
        val objectTypeWrapper = ObjectWrapper.Type(details.details[objectType]?.map ?: emptyMap())
        val relationLinks = orchestrator.stores.relationLinks.current()

        viewModelScope.launch {
            val objectRelationViews = getObjectRelationsView(
                ctx = context,
                objectDetails = objectDetails,
                relationLinks = relationLinks,
                details = details,
                objectWrapper = objectWrapper
            )

            val recommendedRelationViews = getRecommendedRelations(
                ctx = context,
                objectDetails = objectDetails,
                relationLinks = relationLinks,
                objectTypeWrapper = objectTypeWrapper,
                details = details
            )
            val update =
                (objectRelationViews + recommendedRelationViews).map { SlashRelationView.Item(it) }

            action.invoke(update)
        }
    }

    private suspend fun getObjectRelationsView(
        ctx: Id,
        objectDetails: Map<Key, Any?>,
        relationLinks: List<RelationLink>,
        details: Block.Details,
        objectWrapper: ObjectWrapper.Basic
    ): List<ObjectRelationView> {
        return getObjectRelations(
            systemRelations = listOf(),
            relationLinks = relationLinks,
            storeOfRelations = storeOfRelations
        ).views(
            context = ctx,
            details = details,
            values = objectDetails,
            urlBuilder = urlBuilder,
            featured = objectWrapper.featuredRelations
        )
    }

    private suspend fun getRecommendedRelations(
        ctx: Id,
        objectDetails: Map<Key, Any?>,
        relationLinks: List<RelationLink>,
        objectTypeWrapper: ObjectWrapper.Type,
        details: Block.Details
    ): List<ObjectRelationView> {
        return getNotIncludedRecommendedRelations(
            relationLinks = relationLinks,
            recommendedRelations = objectTypeWrapper.recommendedRelations,
            storeOfRelations = storeOfRelations
        ).views(
            context = ctx,
            details = details,
            values = objectDetails,
            urlBuilder = urlBuilder
        )
    }

    private fun proceedWithObjectTypes(objectTypes: List<ObjectTypeView>) {
        onSlashWidgetStateChanged(
            SlashWidgetState.UpdateItems.empty().copy(
                objectItems = SlashExtensions.getSlashWidgetObjectTypeItems(objectTypes = objectTypes)
            )
        )
    }

    private fun proceedWithRelations(relations: List<SlashRelationView>) {
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
                    color = item.themeColor.code
                )
            }
            is SlashItem.Color.Text -> {
                Intent.Text.UpdateColor(
                    context = context,
                    targets = listOf(targetId),
                    color = item.themeColor.code
                )
            }
        }
        viewModelScope.launch {
            orchestrator.proxies.intents.send(intent)
            when (item) {
                is SlashItem.Color.Background -> {
                    sendAnalyticsBlockBackgroundEvent(
                        analytics = analytics,
                        color = item.themeColor.code,
                        context = analyticsContext
                    )
                }
                is SlashItem.Color.Text -> {
                    sendAnalyticsUpdateTextMarkupEvent(
                        analytics = analytics,
                        type = Content.Text.Mark.Type.TEXT_COLOR,
                        context = analyticsContext
                    )
                }
            }
        }
    }

    private fun onSlashMediaItemClicked(item: SlashItem.Media) {
        when (item) {
            SlashItem.Media.Bookmark -> {
                onAddBookmarkBlockClicked()
            }
            SlashItem.Media.Code -> {
                onAddTextBlockClicked(style = Content.Text.Style.CODE_SNIPPET)
            }
            SlashItem.Media.File -> {
                onAddFileBlockClicked(Content.File.Type.FILE)
            }
            SlashItem.Media.Picture -> {
                onAddFileBlockClicked(Content.File.Type.IMAGE)
            }
            SlashItem.Media.Video -> {
                onAddFileBlockClicked(Content.File.Type.VIDEO)
            }
        }
    }

    private fun onSlashStyleTypeItemClicked(item: SlashItem.Style.Type, targetId: Id) {
        val uiBlock = item.convertToUiBlock()
        onTurnIntoBlockClicked(
            target = targetId,
            uiBlock = uiBlock
        )
    }

    private fun onSlashActionItemClicked(item: SlashItem.Actions, targetId: Id) {
        when (item) {
            SlashItem.Actions.CleanStyle -> {
                viewModelScope.launch {
                    sendToast("CleanStyle not implemented")
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
                duplicateBlock(
                    blocks = listOf(targetId),
                    target = targetId
                )
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
                proceedWithMoveToButtonClicked(
                    blocks = listOf(targetId),
                    restorePosition = slashStartIndex,
                    restoreBlock = targetId
                )
            }
            SlashItem.Actions.LinkTo -> {
                onHideKeyboardClicked()
                proceedWithLinkToButtonClicked(block = targetId, position = slashStartIndex)
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
        val items = SlashExtensions.getSlashWidgetMainItems()
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
        relationKey: Key, targetId: Id, isBlockEmpty: Boolean
    ) {
        controlPanelInteractor.onEvent(ControlPanelMachine.Event.Slash.OnStopAndClearFocus)
        val intent = if (isBlockEmpty) {
            Intent.CRUD.Replace(
                context = context,
                target = targetId,
                prototype = Prototype.Relation(key = relationKey)
            )
        } else {
            Intent.CRUD.Create(
                context = context,
                target = targetId,
                position = Position.BOTTOM,
                prototype = Prototype.Relation(key = relationKey)
            )
        }
        viewModelScope.launch {
            orchestrator.proxies.intents.send(intent)
        }
    }
    //endregion

    //region MARKUP TOOLBAR

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

    //region MOVE TO
    private fun proceedWithMoveToButtonClicked(
        blocks: List<Id>,
        restorePosition: Int?,
        restoreBlock: Id?
    ) {
        dispatch(
            Command.OpenMoveToScreen(
                blocks = blocks,
                restorePosition = restorePosition,
                restoreBlock = restoreBlock,
                ctx = context
            )
        )
    }

    fun proceedWithMoveToAction(
        target: Id,
        text: String,
        icon: ObjectIcon,
        blocks: List<Id>,
        isDataView: Boolean
    ) {
        if (BuildConfig.DEBUG) {
            Timber.d("onMoveToTargetClicked, target:[$target], blocks:[$blocks]")
        }
        viewModelScope.launch {
            if (mode == EditorMode.Select) {
                mode = EditorMode.Edit
                clearSelections()
                delay(100)
                controlPanelInteractor.onEvent(ControlPanelMachine.Event.MultiSelect.OnExit)
            }
            orchestrator.proxies.intents.send(
                Intent.Document.Move(
                    context = context,
                    target = "",
                    targetContext = target,
                    blocks = blocks,
                    position = Position.BOTTOM,
                    onSuccess = {
                        dispatch(
                            Command.OpenObjectSnackbar(
                                id = target,
                                fromText = "${blocks.size} block${if (blocks.size > 1) "s" else ""} ",
                                toText = text,
                                icon = icon,
                                isDataView = isDataView
                            )
                        )
                        viewModelScope.sendAnalyticsBlockMoveToEvent(analytics, blocks.size)
                    }
                )
            )
        }
    }

    fun proceedWithMoveToExit(
        blocks: List<Id>,
        restorePosition: Int?,
        restoreBlock: Id?
    ) {
        if (BuildConfig.DEBUG) {
            Timber.d("proceedWithMoveToExit, blocks:[$blocks], restoreBlock:[$restoreBlock] position:[$restorePosition]")
        }
        if (restorePosition != null && restoreBlock != null) {
            proceedWithSettingTextSelection(
                block = restoreBlock,
                textSelection = restorePosition
            )
        }
    }

    private fun proceedWithSettingTextSelection(block: Id, textSelection: Int?) {
        mode = EditorMode.Edit
        val range = IntRange(textSelection ?: 0, textSelection ?: 0)
        val cursor = if (textSelection != null) {
            Editor.Cursor.Range(range)
        } else {
            Editor.Cursor.End
        }

        viewModelScope.launch {
            orchestrator.stores.focus.update(
                Editor.Focus(
                    target = Editor.Focus.Target.Block(block),
                    cursor = cursor
                )
            )
            orchestrator.stores.textSelection.update(
                Editor.TextSelection(block, range)
            )

            orchestrator.stores.views.update(
                views.updateCursorAndEditMode(
                    target = block,
                    cursor = range.first
                )
            )
        }
        viewModelScope.launch {
            renderCommand.send(Unit)
        }
    }
    //endregion

    //region LINK TO
    private fun proceedWithLinkToButtonClicked(block: Id, position: Int?) {
        dispatch(Command.OpenLinkToScreen(target = block, position = position))
    }

    fun proceedWithLinkToAction(
        link: Id,
        target: Id,
        isBookmark: Boolean
    ) {
        Timber.d("proceedWithLinkToAction, link:[$link], target:[$target], isBookmark:[$isBookmark]")
        val targetBlock = blocks.firstOrNull { it.id == target }
        if (targetBlock != null) {
            val targetContent = targetBlock.content
            val position = if (targetContent is Content.Text && targetContent.text.isEmpty()) {
                Position.REPLACE
            } else {
                Position.BOTTOM
            }
            viewModelScope.launch {
                orchestrator.proxies.intents.send(
                    Intent.CRUD.Create(
                        context = context,
                        target = target,
                        position = position,
                        prototype = if (isBookmark)
                            Prototype.Bookmark.Existing(target = link)
                        else
                            Prototype.Link(target = link),
                        onSuccess = {}
                    )
                )
            }
        } else {
            Timber.e("Can't find target block for link")
            sendToast("Error while creating link")
        }
    }

    fun proceedWithLinkToExit(block: Id, position: Int?) {
        Timber.d("proceedWithLinkToExit, block:[$block], position:[$position]")
        if (position != null) {
            proceedWithSettingTextSelection(
                block = block,
                textSelection = position
            )
        }
    }
    //endregion

    //region KEY EVENTS
    fun onKeyPressedEvent(event: KeyPressedEvent) {
        Timber.d("onKeyPressedEvent, event:[$event]")
        when (event) {
            is KeyPressedEvent.OnTitleBlockEnterKeyEvent -> {
                sendHideObjectTypeWidgetEvent()
                proceedWithTitleEnterClicked(
                    title = event.target,
                    text = event.text,
                    range = event.range
                )
                viewModelScope.sendAnalyticsSetTitleEvent(analytics, analyticsContext)
            }
        }
    }

    private fun proceedWithTitleEnterClicked(
        title: Id,
        text: String,
        range: IntRange
    ) {
        if (text.isEndLineClick(range)) {
            onEndLineEnterTitleClicked()
        } else {
            proceedWithSplitEvent(title, range, text, emptyList())
        }
    }

    private fun onEndLineEnterTitleClicked() {
        val description = blocks.firstOrNull { block ->
            val cnt = block.content
            cnt is Content.Text && cnt.style == Content.Text.Style.DESCRIPTION
        }
        if (description != null) {
            proceedWithSettingTextSelection(
                block = description.id,
                textSelection = description.content<Content.Text>().text.length
            )
        } else {
            val page = blocks.first { it.id == context }
            val next = page.children.getOrElse(0) { "" }
            proceedWithCreatingNewTextBlock(
                id = next,
                style = Content.Text.Style.P,
                position = Position.TOP
            )
        }
    }
    //endregion

    //region MULTI-SELECT

    fun onBlockActionPanelHidden() {
        proceedWithExitingMultiSelectMode()
    }

    fun onMultiSelectAction(action: ActionItemType) {
        Timber.d("onMultiSelectAction, action:[$action]")
        when (action) {
            ActionItemType.AddBelow -> {
                onMultiSelectAddBelow()
                onSendBlockActionAnalyticsEvent(EventsDictionary.BlockAction.addBelow)
            }
            ActionItemType.Delete -> {
                onMultiSelectModeDeleteClicked()
                onSendBlockActionAnalyticsEvent(EventsDictionary.BlockAction.delete)
            }
            ActionItemType.Duplicate -> {
                onMultiSelectDuplicateClicked()
                onSendBlockActionAnalyticsEvent(EventsDictionary.BlockAction.duplicate)
            }
            ActionItemType.MoveTo -> {
                proceedWithMoveToButtonClicked(
                    blocks = currentSelection().toList(),
                    restoreBlock = null,
                    restorePosition = null
                )
                onSendBlockActionAnalyticsEvent(EventsDictionary.BlockAction.moveTo)
            }
            ActionItemType.SAM -> {
                onEnterScrollAndMoveClicked()
                onSendBlockActionAnalyticsEvent(EventsDictionary.BlockAction.move)
            }
            ActionItemType.Style -> {
                onMultiSelectStyleButtonClicked()
                onSendBlockActionAnalyticsEvent(EventsDictionary.BlockAction.style)
            }
            ActionItemType.Download -> {
                startDownloadingFiles(ids = currentSelection().toList())
                proceedWithExitingMultiSelectMode()
                onSendBlockActionAnalyticsEvent(EventsDictionary.BlockAction.download)
            }
            ActionItemType.Preview -> {
                proceedWithObjectAppearanceSettingClicked()
                onSendBlockActionAnalyticsEvent(EventsDictionary.BlockAction.preview)
            }
            ActionItemType.Copy -> {
                onMultiSelectCopyClicked()
                onSendBlockActionAnalyticsEvent(EventsDictionary.BlockAction.copy)
            }
            ActionItemType.Paste -> {
                onMultiSelectPasteClicked()
                proceedWithExitingMultiSelectMode()
                onSendBlockActionAnalyticsEvent(EventsDictionary.BlockAction.paste)
            }
            ActionItemType.OpenObject -> {
                proceedWithOpeningTargetForCurrentSelection()
                onSendBlockActionAnalyticsEvent(EventsDictionary.BlockAction.openObject)
            }
            else -> {
                sendToast("TODO")
            }
        }
    }

    private fun proceedWithOpeningTargetForCurrentSelection() {
        val selected = blocks.firstOrNull { currentSelection().contains(it.id) }
        proceedWithExitingMultiSelectMode()
        if (selected != null) {
            when (val content = selected.content) {
                is Content.Bookmark -> {
                    val target = content.targetObjectId
                    if (target != null) {
                        proceedWithOpeningObject(target)
                        viewModelScope.sendAnalyticsOpenAsObject(
                            analytics = analytics,
                            type = EventsDictionary.Type.bookmark
                        )
                    } else {
                        sendToast("This bookmark doesn’t have a source.")
                    }
                }
                else -> sendToast("Unexpected object")
            }
        } else {
            sendToast("No blocks were selected. Please, try again.")
        }
    }

    private fun onSendBlockActionAnalyticsEvent(type: String) {
        viewModelScope.launch {
            sendAnalyticsBlockActionEvent(
                analytics = analytics,
                context = context,
                type = type
            )
        }
    }

    private fun onMultiSelectAddBelow() {
        mode = EditorMode.Edit
        controlPanelInteractor.onEvent(ControlPanelMachine.Event.MultiSelect.OnExit)
        val target = currentSelection().first()
        clearSelections()
        proceedWithCreatingNewTextBlock(
            id = target,
            style = Content.Text.Style.P
        )
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

        proceedWithExitingMultiSelectMode()
    }

    private fun onMultiSelectDuplicateClicked() {
        val parents = blocks.parents(currentSelection())
        val targets = views.mapNotNull { view ->
            if (parents.contains(view.id))
                view.id
            else
                null
        }
        duplicateBlock(
            blocks = targets,
            target = targets.last()
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
    }

    private fun onMultiSelectPasteClicked() {
        Timber.d("onMultiSelectPasteClicked, ")
        viewModelScope.launch {
            orchestrator.proxies.intents.send(
                Intent.Clipboard.Paste(
                    context = context,
                    focus = Editor.Focus.EMPTY_FOCUS,
                    selected = currentSelection().toList(),
                    range = DEFAULT_RANGE
                )
            )
        }
    }

    fun onMultiSelectStyleButtonClicked() {
        Timber.d("onMultiSelectStyleButtonClicked, ")
        proceedWithMultiStyleToolbarEvent()
    }

    fun onExitMultiSelectModeClicked() {
        when (mode) {
            is EditorMode.Table -> proceedWithExitingTableMode()
            else -> proceedWithExitingMultiSelectMode()
        }
    }

    private fun proceedWithExitingMultiSelectMode() {
        Timber.d("onExitMultiSelectModeClicked, ")
        mode = EditorMode.Edit
        clearSelections()
        viewModelScope.launch {
            delay(DELAY_REFRESH_DOCUMENT_ON_EXIT_MULTI_SELECT_MODE)
            orchestrator.stores.focus.update(Editor.Focus.empty())
            refresh()
        }
        controlPanelInteractor.onEvent(ControlPanelMachine.Event.MultiSelect.OnExit)
    }

    fun onEnterMultiSelectModeClicked() {
        Timber.d("onEnterMultiSelectModeClicked, ")
        controlPanelInteractor.onEvent(ControlPanelMachine.Event.MultiSelect.OnEnter())
        mode = EditorMode.Select
        viewModelScope.launch { orchestrator.stores.focus.update(Editor.Focus.empty()) }
        viewModelScope.launch {
            delay(DELAY_REFRESH_DOCUMENT_TO_ENTER_MULTI_SELECT_MODE)
            refresh()
        }
    }

    //endregion

    //region MENTION WIDGET
    /**
     * Current position of last mentionFilter or -1 if none
     */
    private var mentionFrom = -1
    private val mentionFilter = MutableStateFlow("")
    val mentionSearchQuery = mentionFilter.asStateFlow()
    private var jobMentionFilter: Job? = null

    fun onStartMentionWidgetClicked() {
        dispatch(Command.AddMentionWidgetTriggerToFocusedBlock)
        viewModelScope.sendAnalyticsMentionMenuEvent(analytics)
    }

    fun onMentionEvent(mentionEvent: MentionEvent) {
        Timber.d("onMentionEvent, mentionEvent:[$mentionEvent]")
        when (mentionEvent) {
            is MentionEvent.MentionSuggestText -> {
                mentionFilter.value = mentionEvent.text.toString()
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
                jobMentionFilter?.cancel()
                mentionFilter.value = ""
                jobMentionFilter = viewModelScope.launch {
                    mentionSearchQuery
                        .debounce(300)
                        .collect { onMentionFilter(it) }
                }
            }
            MentionEvent.MentionSuggestStop -> {
                mentionFrom = -1
                jobMentionFilter?.cancel()
                mentionFilter.value = ""
                controlPanelInteractor.onEvent(ControlPanelMachine.Event.Mentions.OnStop)
            }
            MentionEvent.MentionSuggestStopCell -> {
                mentionFrom = -1
                jobMentionFilter?.cancel()
                mentionFilter.value = ""
                controlPanelInteractor.onEvent(ControlPanelMachine.Event.Mentions.OnStopCell)
            }
        }
    }

    fun onAddMentionNewPageClicked(mentionText: String) {
        Timber.d("onAddMentionNewPageClicked, mentionText:[$mentionText]")
        viewModelScope.launch {
            getDefaultPageType.async(Unit).fold(
                onFailure = {
                    Timber.e(it, "Error while getting default object type")
                    proceedWithCreateNewObject(objectType = null, mentionText = mentionText)
                },
                onSuccess = {
                    proceedWithCreateNewObject(objectType = it.type, mentionText = mentionText)
                }
            )
        }
    }

    private fun proceedWithCreateNewObject(objectType: String?, mentionText: String) {
        val params = CreateObjectAsMentionOrLink.Params(
            name = mentionText.removePrefix(MENTION_PREFIX),
            type = objectType
        )

        val startTime = System.currentTimeMillis()

        viewModelScope.launch {
            createObjectAsMentionOrLink.async(
                params = params
            ).fold(
                onFailure = {
                    Timber.e(it, "Error while creating new page with params: $params")
                },
                onSuccess = { result ->
                    onCreateMentionInText(
                        id = result.id,
                        name = result.name.getMentionName(MENTION_TITLE_EMPTY),
                        mentionTrigger = mentionText
                    )
                    sendAnalyticsObjectCreateEvent(
                        analytics = analytics,
                        type = objectType,
                        storeOfObjectTypes = storeOfObjectTypes,
                        route = EventsDictionary.Routes.objCreateMention,
                        startTime = startTime
                    )
                }
            )
        }
    }

    fun onMentionSuggestClick(mention: DefaultObjectView, mentionTrigger: String, pos: Int) {
        Timber.d("onMentionSuggestClick, mention:[$mention] mentionTrigger:[$mentionTrigger]")
        viewModelScope.sendAnalyticsSearchResultEvent(
            analytics = analytics,
            pos = pos,
            length = mentionTrigger.length - 1,
            context = analyticsContext
        )
        onCreateMentionInText(id = mention.id, name = mention.name, mentionTrigger = mentionTrigger)
    }

    fun onCreateMentionInText(id: Id, name: String, mentionTrigger: String) {
        Timber.d("onCreateMentionInText, id:[$id], name:[$name], mentionTrigger:[$mentionTrigger]")

        controlPanelInteractor.onEvent(ControlPanelMachine.Event.Mentions.OnMentionClicked)

        val target = blocks.first { it.id == focus.value }

        val new = target.addMention(
            mentionText = name.getMentionName(MENTION_TITLE_EMPTY),
            mentionId = id,
            from = mentionFrom,
            mentionTrigger = mentionTrigger
        )

        val update = blocks.map { block ->
            if (block.id != target.id)
                block
            else
                new
        }

        orchestrator.stores.document.update(update)

        viewModelScope.launch {
            val position = mentionFrom + name.length + 1
            orchestrator.stores.focus.update(
                t = Editor.Focus(
                    target = Editor.Focus.Target.Block(new.id),
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

    fun onMentionClicked(target: String) {
        if (isObjectTemplate()) return
        proceedWithOpeningObjectByLayout(target)
    }

    private suspend fun onMentionFilter(filter: String) {
        controlPanelViewState.value?.let { state ->
            if (!state.mentionToolbar.isVisible) {
                jobMentionFilter?.cancel()
                return
            }
            val fullText = filter.removePrefix(MENTION_PREFIX)
            val params = SearchObjects.Params(
                limit = ObjectSearchViewModel.SEARCH_LIMIT,
                filters = ObjectSearchConstants.getFilterLinkTo(
                    ignore = context,
                    space = workspaceManager.getCurrentWorkspace()
                ),
                sorts = ObjectSearchConstants.sortLinkTo,
                fulltext = fullText,
                keys = ObjectSearchConstants.defaultKeys
            )
            viewModelScope.launch {
                searchObjects(params).process(
                    success = { result ->
                        val objects = result
                            .toView(
                                urlBuilder = urlBuilder,
                                objectTypes = storeOfObjectTypes.getAll()
                            )
                            .filter {
                                SupportedLayouts.layouts.contains(it.layout)
                                        && it.type != ObjectTypeIds.TEMPLATE
                            }
                        controlPanelInteractor.onEvent(
                            ControlPanelMachine.Event.Mentions.OnResult(
                                objects,
                                filter
                            )
                        )
                    },
                    failure = { Timber.e(it, "Error while searching for mention objects") }
                )
            }
        }
    }

    fun onDragAndDrop(
        dragged: Id,
        target: Id,
        position: Position
    ) {
        val descendants = blocks.asMap().descendants(parent = dragged)

        if (descendants.contains(target)) {
            sendToast(CANNOT_MOVE_PARENT_INTO_CHILD)
            return
        }

        val targetBlock = blocks.find { it.id == target }

        val targetContext =
            if (targetBlock?.content is Content.Link && position == Position.INNER) {
                targetBlock.content<Content.Link>().target
            } else {
                context
            }

        viewModelScope.launch {
            orchestrator.proxies.intents.send(
                Intent.Document.Move(
                    context = context,
                    target = target,
                    targetContext = targetContext,
                    blocks = listOf(dragged),
                    position = position
                )
            )
            sendAnalyticsBlockReorder(
                analytics = analytics,
                count = 1,
                context = analyticsContext
            )
        }
    }
    //endregion

    //region OBJECT TYPES WIDGET
    private val isObjectTypesWidgetVisible: Boolean
        get() =
            controlPanelViewState.value?.objectTypesToolbar?.isVisible ?: false

    fun onObjectTypesWidgetItemClicked(typeId: Id, key: Key) {
        Timber.d("onObjectTypesWidgetItemClicked, type:[$typeId]")
        proceedWithObjectTypeChange(
            type = typeId,
            key = key,
            applyTemplate = true
        )
    }

    fun onObjectTypesWidgetSearchClicked() {
        Timber.d("onObjectTypesWidgetSearchClicked, ")
        proceedWithOpeningSelectingObjectTypeScreen()
    }

    fun onObjectTypesWidgetDoneClicked() {
        Timber.d("onObjectTypesWidgetDoneClicked, ")
        sendHideObjectTypeWidgetEvent()
        proceedWithCheckingInternalFlagShouldSelectTemplate()
    }

    private fun proceedWithShowingObjectTypesWidget() {
        val restrictions = orchestrator.stores.objectRestrictions.current()
        if (restrictions.contains(ObjectRestriction.TYPE_CHANGE)) {
            Timber.d("proceedWithShowingObjectTypesWidget, type change is restricted")
            return
        }
        proceedWithCheckingInternalFlagShouldSelectType()
    }

    private fun proceedWithGettingObjectTypesForObjectTypeWidget() {
        viewModelScope.launch {
            val excludeTypes = orchestrator.stores.details.current().details[context]?.type
            val params = GetObjectTypes.Params(
                sorts = emptyList(),
                filters = buildList {
                    addAll(
                        ObjectSearchConstants.filterObjectTypeLibrary(
                            space = spaceManager.get()
                        )
                    )
                    add(
                        DVFilter(
                            relation = Relations.RECOMMENDED_LAYOUT,
                            condition = DVFilterCondition.IN,
                            value = SupportedLayouts.createObjectLayouts.map { layout ->
                                layout.code.toDouble()
                            }
                        )
                    )
                },
                keys = ObjectSearchConstants.defaultKeysObjectType
            )
            getObjectTypes.async(params).fold(
                onFailure = { Timber.e(it, "Error while getting library object types") },
                onSuccess = { types ->
                    val views = types.getObjectTypeViewsForSBPage(
                        isWithCollection = true,
                        isWithBookmark = false,
                        selectedTypes = emptyList(),
                        excludeTypes = excludeTypes ?: emptyList()
                    )
                    proceedWithSortingObjectTypesForObjectTypeWidget(views = views)
                }
            )
        }
    }

    private suspend fun proceedWithSortingObjectTypesForObjectTypeWidget(views: List<ObjectTypeView>) {
        getDefaultPageType.async(Unit).fold(
            onFailure = {
                Timber.e(it, "Error while getting default object type")
            },
            onSuccess = { response ->
                val filtered = views.filter { it.id != response.type }
                controlPanelInteractor.onEvent(
                    ControlPanelMachine.Event.ObjectTypesWidgetEvent.Show(filtered)
                )
            }
        )
    }

    private fun proceedWithOpeningSelectingObjectTypeScreen() {
        val excludeTypes = orchestrator.stores.details.current().details[context]?.type
        val command = if (isObjectTypesWidgetVisible) {
            Command.OpenDraftObjectSelectTypeScreen(
                excludedTypes = excludeTypes ?: emptyList()
            )
        } else {
            Command.OpenObjectSelectTypeScreen(
                excludedTypes = excludeTypes ?: emptyList()
            )
        }
        dispatch(command)
    }

    private fun sendHideObjectTypeWidgetEvent() {
        if (isObjectTypesWidgetVisible) controlPanelInteractor.onEvent(ObjectTypesWidgetEvent.Hide)
    }
    //endregion

    //region OBJECT APPEARANCE SETTING
    private fun proceedWithObjectAppearanceSettingClicked() {
        val selected = currentSelection().toList()
        if (selected.size == 1) {
            val block = blocks.firstOrNull { it.id == selected[0] } ?: return
            commands.value = EventWrapper(
                Command.OpenObjectAppearanceSettingScreen(
                    ctx = context,
                    block = block.id
                )
            )
        } else {
            sendToast("Couldn't show Object Appearance Setting screen")
        }
    }
    //endregion

    fun onCreateNewSetForType(type: Id) {
        viewModelScope.launch {
            createObjectSet(CreateObjectSet.Params(type = type)).process(
                failure = { Timber.e(it, "Error while creating a set of type: $type") },
                success = { response -> proceedWithOpeningDataViewObject(response.target) }
            )
        }
    }

    //region ADD URI OR OBJECT ID TO SELECTED TEXT
    fun proceedToCreateObjectAndAddToTextAsLink(name: String) {
        Timber.d("proceedToCreateObjectAndAddToTextAsLink, name:[$name]")
        viewModelScope.launch {
            getDefaultPageType.async(Unit).fold(
                onFailure = {
                    Timber.e(it, "Error while getting default object type")
                },
                onSuccess = { response ->
                    createObjectAddProceedToAddToTextAsLink(
                        name = name,
                        type = response.type
                    )
                }
            )
        }
    }

    fun onEditLinkClicked() {
        Timber.d("onEditLinkClicked, ")
        val target = orchestrator.stores.focus.current().targetOrNull() ?: return
        val range = orchestrator.stores.textSelection.current().selection
        val block = blocks.firstOrNull { it.id == target }
        if (block != null && range != null) {
            dispatch(
                Command.OpenLinkToObjectOrWebScreen(
                    ctx = context,
                    target = target,
                    range = range,
                    isWholeBlockMarkup = false
                )
            )
        }
    }

    fun onCopyLinkClicked(link: String) {
        dispatch(Command.SaveTextToSystemClipboard(link))
    }

    private suspend fun createObjectAddProceedToAddToTextAsLink(name: String, type: String?) {
        val startTime = System.currentTimeMillis()
        val params = CreateObjectAsMentionOrLink.Params(name, type)
        createObjectAsMentionOrLink.async(params).fold(
            onFailure = { Timber.e(it, "Error while creating new page with params: $params") },
            onSuccess = { result ->
                proceedToAddObjectToTextAsLink(id = result.id)
                viewModelScope.sendAnalyticsObjectCreateEvent(
                    analytics = analytics,
                    type = type,
                    storeOfObjectTypes = storeOfObjectTypes,
                    route = EventsDictionary.Routes.objTurnInto,
                    startTime = startTime
                )
            }
        )
    }

    fun proceedToAddObjectToTextAsLink(id: Id) {
        Timber.d("proceedToAddObjectToTextAsLink, target:[$id], mode:$mode")
        when (mode) {
            EditorMode.Edit -> {
                val range = orchestrator.stores.textSelection.current().selection
                if (range != null) {
                    dispatch(Command.ShowKeyboard)
                    viewModelScope.launch {
                        markupActionPipeline.send(
                            MarkupAction(
                                type = Markup.Type.OBJECT,
                                param = id
                            )
                        )
                    }
                }
            }
            is EditorMode.Styling.Single -> {
                val target = (mode as EditorMode.Styling.Single).target
                onUpdateBlockListMarkup(
                    ids = listOf(target),
                    type = Markup.Type.OBJECT,
                    param = id
                )
            }
            is EditorMode.Styling.Multi -> {
                val targets = (mode as EditorMode.Styling.Multi).targets.toList()
                if (targets.size == 1) {
                    onUpdateBlockListMarkup(
                        ids = targets,
                        type = Markup.Type.OBJECT,
                        param = id
                    )
                }
            }
            else -> {
                Timber.e("Error to proceedToAddObjectToTextAsLink, wrong mode:[$mode]")
            }
        }
    }

    fun proceedToAddUriToTextAsLink(uri: String) {
        Timber.d("proceedToAddUriToTextAsLink, uri:[$uri]")
        val range = orchestrator.stores.textSelection.current().selection
        if (range != null) {
            val target = orchestrator.stores.focus.current().targetOrNull()
            if (target != null) {
                applyLinkMarkup(
                    blockId = target,
                    link = uri,
                    range = range.first..range.last.dec()
                )
            } else {
                Timber.e("No target")
            }
        } else {
            Timber.e("Can't add uri to text, range is null")
        }
    }

    fun onUndoRedoActionClicked() {
        isUndoRedoToolbarIsVisible.value = true
    }

    fun onUndoRedoToolbarIsHidden() {
        isUndoRedoToolbarIsVisible.value = false
    }

    //endregion

    //region FOOTER
    private fun getFooterState(root: Block, details: Block.Details): EditorFooter {
        return when (details.details[root.id]?.layout?.toInt()) {
            ObjectType.Layout.NOTE.code -> EditorFooter.Note
            else -> EditorFooter.None
        }
    }
    //endregion

    //region COPY FILE TO CACHE
    val copyFileStatus = MutableSharedFlow<CopyFileStatus>(replay = 0)

    override fun onStartCopyFileToCacheDir(uri: Uri) {
        copyFileToCache.execute(
            uri = uri,
            scope = viewModelScope,
            listener = copyFileListener
        )
    }

    override fun onCancelCopyFileToCacheDir() {
        copyFileToCache.cancel()
    }

    private val copyFileListener = object : OnCopyFileToCacheAction {
        override fun onCopyFileStart() {
            viewModelScope.launch {
                copyFileStatus.emit(CopyFileStatus.Started)
            }
        }

        override fun onCopyFileResult(result: String?) {
            viewModelScope.launch {
                copyFileStatus.emit(CopyFileStatus.Completed(result))
            }
        }

        override fun onCopyFileError(msg: String) {
            viewModelScope.launch {
                copyFileStatus.emit(CopyFileStatus.Error(msg))
            }
        }
    }
    //endregion

    //region TEMPLATING

    fun onShowTemplateClicked() {
        viewModelScope.launch { onEvent(SelectTemplateEvent.OnAccepted) }
    }

    fun onTypeHasTemplateToolbarHidden() {
        viewModelScope.launch { onEvent(SelectTemplateEvent.OnSkipped) }
    }

    private fun proceedWithStartTemplateEvent(objTypeId: Id) {
        viewModelScope.launch {
            val objType = storeOfObjectTypes.get(objTypeId)
            if (objType != null) {
                onEvent(
                    SelectTemplateEvent.OnStart(
                        ctx = context,
                        type = objTypeId,
                        typeName = objType.name.orEmpty()
                    )
                )
            } else {
                Timber.e("Error while getting object type from storeOfObjectTypes by id: $objTypeId")
            }
        }
    }

    private fun getObjectTypeFromDetails(): Id? {
        val details = orchestrator.stores.details.current()
        val wrapper = ObjectWrapper.Basic(details.details[context]?.map ?: emptyMap())
        return wrapper.getProperType()
    }

    fun isObjectTemplate(): Boolean {
        val details = orchestrator.stores.details.current().details[context]
        return details?.type?.firstOrNull() == ObjectTypeIds.TEMPLATE
    }
    //endregion

    //region SIMPLE TABLES
    fun onHideSimpleTableWidget() {
        Timber.d("onHideSimpleTableWidget, ")
        proceedWithExitingTableMode()
    }

    fun onSimpleTableWidgetItemClicked(item: SimpleTableWidgetItem) {
        Timber.d("onSimpleTableWidgetItemClicked, item:[$item]")
        viewModelScope.launch {
            when (item) {
                SimpleTableWidgetItem.Cell.ClearContents -> {
                    proceedTableWidgetClearContentClicked()
                }
                is SimpleTableWidgetItem.Row.ClearContents -> {
                    tableDelegate.onEditorTableRowEvent(
                        EditorTableEvent.Row.ClearContents(
                            ctx = context,
                            rows = item.rows.map { it.value }
                        )
                    )
                    proceedTableWidgetClearContentClicked()
                }
                is SimpleTableWidgetItem.Column.ClearContents -> {
                    tableDelegate.onEditorTableColumnEvent(
                        EditorTableEvent.Column.ClearContents(
                            ctx = context,
                            columns = item.columns.map { it.value }
                        )
                    )
                    proceedTableWidgetClearContentClicked()
                }
                SimpleTableWidgetItem.Cell.ResetStyle -> {
                    proceedTableWidgetResetStyleClicked()
                }
                is SimpleTableWidgetItem.Column.ResetStyle -> {
                    tableDelegate.onEditorTableColumnEvent(
                        EditorTableEvent.Column.ResetStyle(
                            ctx = context,
                            columns = item.columns.map { it.value }
                        )
                    )
                    proceedTableWidgetResetStyleClicked()
                }
                is SimpleTableWidgetItem.Row.ResetStyle -> {
                    tableDelegate.onEditorTableRowEvent(
                        EditorTableEvent.Row.ResetStyle(
                            ctx = context,
                            rows = item.rows.map { it.value }
                        )
                    )
                    proceedTableWidgetResetStyleClicked()
                }
                SimpleTableWidgetItem.Cell.Color -> {
                    onShowColorBackgroundToolbarEvent(
                        ids = currentSelection().toList(),
                        navigatedFromCellsMenu = true,
                        navigateFromStylingTextToolbar = false
                    )
                }
                is SimpleTableWidgetItem.Column.Color -> {
                    tableDelegate.onEditorTableColumnEvent(
                        event = EditorTableEvent.Column.Color(
                            ctx = context,
                            columns = item.columns.map { it.value }
                        )
                    )
                    onShowColorBackgroundToolbarEvent(
                        ids = currentSelection().toList(),
                        navigatedFromCellsMenu = true,
                        navigateFromStylingTextToolbar = false
                    )
                }
                is SimpleTableWidgetItem.Row.Color -> {
                    tableDelegate.onEditorTableRowEvent(
                        event = EditorTableEvent.Row.Color(
                            ctx = context,
                            rows = item.rows.map { it.value }
                        )
                    )
                    onShowColorBackgroundToolbarEvent(
                        ids = currentSelection().toList(),
                        navigatedFromCellsMenu = true,
                        navigateFromStylingTextToolbar = false
                    )
                }
                is SimpleTableWidgetItem.Column.Style -> {
                    tableDelegate.onEditorTableColumnEvent(
                        event = EditorTableEvent.Column.Style(
                            ctx = context,
                            columns = item.columns.map { it.value }
                        )
                    )
                    proceedTableWidgetStyleClicked()
                }
                SimpleTableWidgetItem.Cell.Style -> {
                    proceedTableWidgetStyleClicked()
                }
                is SimpleTableWidgetItem.Row.Style -> {
                    tableDelegate.onEditorTableRowEvent(
                        event = EditorTableEvent.Row.Style(
                            ctx = context,
                            rows = item.rows.map { it.value }
                        )
                    )
                    proceedTableWidgetStyleClicked()
                }
                SimpleTableWidgetItem.Tab.Cell -> {
                    val currentMode = mode
                    if (currentMode is EditorMode.Table) {
                        proceedWithUpdateTabInTableMode(
                            tableId = currentMode.tableId,
                            tab = BlockView.Table.Tab.CELL,
                            modeTable = currentMode
                        )
                    }
                }
                SimpleTableWidgetItem.Tab.Row -> {
                    val currentMode = mode
                    if (currentMode is EditorMode.Table) {
                        proceedWithUpdateTabInTableMode(
                            tableId = currentMode.tableId,
                            tab = BlockView.Table.Tab.ROW,
                            modeTable = currentMode
                        )
                    }
                }
                SimpleTableWidgetItem.Tab.Column -> {
                    val currentMode = mode
                    if (currentMode is EditorMode.Table) {
                        proceedWithUpdateTabInTableMode(
                            tableId = currentMode.tableId,
                            tab = BlockView.Table.Tab.COLUMN,
                            modeTable = currentMode
                        )
                    }
                }
                is SimpleTableWidgetItem.Column.Delete -> {
                    proceedWithExitingTableMode()
                    tableDelegate.onEditorTableColumnEvent(
                        EditorTableEvent.Column.Delete(
                            ctx = context,
                            columns = listOf(item.column.value)
                        )
                    )

                }
                is SimpleTableWidgetItem.Column.Duplicate -> {
                    tableDelegate.onEditorTableColumnEvent(
                        EditorTableEvent.Column.Duplicate(
                            ctx = context,
                            columns = listOf(item.column.value)
                        )
                    )

                }
                is SimpleTableWidgetItem.Column.InsertLeft -> {
                    tableDelegate.onEditorTableColumnEvent(
                        EditorTableEvent.Column.CreateLeft(
                            ctx = context,
                            columns = listOf(item.column.value)
                        )
                    )

                }
                is SimpleTableWidgetItem.Column.InsertRight -> {
                    tableDelegate.onEditorTableColumnEvent(
                        EditorTableEvent.Column.CreateRight(
                            ctx = context,
                            columns = listOf(item.column.value)
                        )
                    )

                }
                is SimpleTableWidgetItem.Column.MoveLeft -> {
                    val response = views.getTableColumnsById(mode, item.column)
                    tableDelegate.onEditorTableColumnEvent(
                        EditorTableEvent.Column.MoveLeft(
                            ctx = context,
                            columns = listOf(item.column.id.value),
                            targetDrop = response.columnLeft?.id?.value
                        )
                    )
                }
                is SimpleTableWidgetItem.Column.MoveRight -> {
                    val response = views.getTableColumnsById(mode, item.column)
                    tableDelegate.onEditorTableColumnEvent(
                        EditorTableEvent.Column.MoveRight(
                            ctx = context,
                            columns = listOf(item.column.id.value),
                            targetDrop = response.columnRight?.id?.value
                        )
                    )
                }
                is SimpleTableWidgetItem.Row.Delete -> {
                    proceedWithExitingTableMode()
                    tableDelegate.onEditorTableRowEvent(
                        EditorTableEvent.Row.Delete(
                            ctx = context,
                            rows = listOf(item.row.value)
                        )
                    )
                }
                is SimpleTableWidgetItem.Row.Duplicate -> {
                    tableDelegate.onEditorTableRowEvent(
                        EditorTableEvent.Row.Duplicate(
                            ctx = context,
                            rows = listOf(item.row.value)
                        )
                    )
                }
                is SimpleTableWidgetItem.Row.InsertAbove -> {
                    tableDelegate.onEditorTableRowEvent(
                        EditorTableEvent.Row.CreateAbove(
                            ctx = context,
                            rows = listOf(item.row.value)
                        )
                    )
                }
                is SimpleTableWidgetItem.Row.InsertBelow -> {
                    tableDelegate.onEditorTableRowEvent(
                        EditorTableEvent.Row.CreateBelow(
                            ctx = context,
                            rows = listOf(item.row.value)
                        )
                    )
                }
                is SimpleTableWidgetItem.Row.MoveDown -> {
                    val response = views.getTableRowsById(
                        mode = mode,
                        row = item.row
                    )
                    tableDelegate.onEditorTableRowEvent(
                        EditorTableEvent.Row.MoveDown(
                            ctx = context,
                            rows = listOf(item.row.id.value),
                            targetDrop = response.rowBottom?.id?.value
                        )
                    )
                }
                is SimpleTableWidgetItem.Row.MoveUp -> {
                    val response = views.getTableRowsById(
                        mode = mode,
                        row = item.row
                    )
                    tableDelegate.onEditorTableRowEvent(
                        EditorTableEvent.Row.MoveUp(
                            ctx = context,
                            rows = listOf(item.row.id.value),
                            targetDrop = response.rowTop?.id?.value
                        )
                    )
                }
                else -> {
                    Timber.w("Simple table action $item not implemented")
                }
            }
        }
    }

    private fun proceedTableWidgetStyleClicked() {
        val selected = blocks.filter { currentSelection().contains(it.id) }
        val state = selected.map { it.content.asText() }.getStyleOtherStateForTableCells()
        controlPanelInteractor.onEvent(
            ControlPanelMachine.Event.OtherToolbar.Show(
                state = state,
                navigatedFromCellsMenu = true,
                navigateFromStylingTextToolbar = false
            )
        )
    }

    private fun proceedTableWidgetClearContentClicked() {
        viewModelScope.launch {
            orchestrator.proxies.intents.send(
                Intent.Text.ClearContent(
                    context = context,
                    targets = currentSelection().toList()
                )
            )
        }
    }

    private fun proceedTableWidgetResetStyleClicked() {
        viewModelScope.launch {
            orchestrator.proxies.intents.send(
                Intent.Text.ClearStyle(
                    context = context,
                    targets = currentSelection().toList()
                )
            )
        }
    }

    /**
     * Enter EditorMode.Table
     */
    private fun proceedWithEnterTableMode(cell: BlockView.Table.Cell) {
        viewModelScope.launch {
            clearSelections()
            toggleSelection(target = cell.getId())
            mode = EditorMode.Table(
                tableId = cell.tableId,
                targets = currentSelection(),
                initialTargets = currentSelection(),
                tab = BlockView.Table.Tab.CELL
            )

            orchestrator.stores.focus.update(Editor.Focus.empty())
            orchestrator.stores.views.update(
                views.toggleTableMode(
                    cellsMode = BlockView.Mode.READ,
                    selectedCellsIds = currentSelection().toList()
                )
            )
            renderCommand.send(Unit)
            controlPanelInteractor.onEvent(
                ControlPanelMachine.Event.SimpleTableWidget.ShowCellTab(
                    cellItems = getSimpleTableWidgetCellItems(),
                    tableId = cell.tableId,
                    cellSize = currentSelection().size
                )
            )
        }
    }

    /**
     * Exit EditorMode.Table
     */
    private fun proceedWithExitingTableMode() {
        Timber.d("proceedWithExitingTableMode, mode:[$mode]")
        if (currentSelection().isNotEmpty()) clearSelections()
        val currentMode = mode
        if (currentMode is EditorMode.Table) {
            val tableId = currentMode.tableId
            mode = EditorMode.Edit
            controlPanelInteractor.onEvent(
                ControlPanelMachine.Event.SimpleTableWidget.Hide(
                    tableId = tableId
                )
            )
            viewModelScope.launch {
                orchestrator.stores.views.update(
                    views.toggleTableMode(
                        cellsMode = BlockView.Mode.EDIT,
                        selectedCellsIds = currentSelection().toList()
                    )
                )
                renderCommand.send(Unit)
            }
        } else {
            Timber.w("Can't exit Mode.Table, current mode is $mode")
        }
    }

    private fun proceedWithClickingOnCellInTableMode(
        cell: BlockView.Table.Cell,
        modeTable: EditorMode.Table
    ) {
        val tableBlock = views.find { it.id == modeTable.tableId } as BlockView.Table
        val event = when (modeTable.tab) {
            BlockView.Table.Tab.CELL -> {
                toggleSelection(target = cell.getId())
                mode = modeTable.copy(
                    initialTargets = currentSelection(),
                    targets = currentSelection()
                )

                ControlPanelMachine.Event.SimpleTableWidget.ShowCellTab(
                    cellItems = getSimpleTableWidgetCellItems(),
                    tableId = cell.tableId,
                    cellSize = currentSelection().size
                )
            }
            BlockView.Table.Tab.COLUMN -> {
                val columnCellIds = tableBlock.getIdsInColumn(index = cell.column.index)
                if (isSelected(cell.getId())) {
                    unselect(columnCellIds)
                } else {
                    select(columnCellIds)
                }

                val selectedColumns = mutableSetOf<BlockView.Table.Column>()
                tableBlock.cells.forEach {
                    if (currentSelection().contains(it.getId())) {
                        selectedColumns.add(it.column)
                    }
                }

                mode = modeTable.copy(
                    targets = currentSelection()
                )

                ControlPanelMachine.Event.SimpleTableWidget.ShowColumnTab(
                    columnItems = getSimpleTableWidgetColumnItems(
                        selectedColumns = selectedColumns,
                        columnsSize = tableBlock.columns.size
                    ),
                    tableId = cell.tableId,
                    columnsSize = selectedColumns.size
                )
            }
            BlockView.Table.Tab.ROW -> {
                val rowCellIds = tableBlock.getIdsInRow(index = cell.row.index)
                if (isSelected(cell.getId())) {
                    unselect(rowCellIds)
                } else {
                    select(rowCellIds)
                }
                val selectedRows = mutableSetOf<BlockView.Table.Row>()
                tableBlock.cells.forEach {
                    if (currentSelection().contains(it.getId())) {
                        selectedRows.add(it.row)
                    }
                }
                mode = modeTable.copy(
                    targets = currentSelection()
                )

                ControlPanelMachine.Event.SimpleTableWidget.ShowRowTab(
                    rowItems = getSimpleTableWidgetRowItems(
                        selectedRows = selectedRows,
                        rowsSize = tableBlock.rows.size
                    ),
                    tableId = cell.tableId,
                    rowsSize = selectedRows.size
                )
            }
        }
        if (currentSelection().isEmpty()) {
            proceedWithExitingTableMode()
        } else {
            viewModelScope.launch {
                orchestrator.stores.views.update(
                    views.updateTableBlockSelection(
                        tableId = tableBlock.id,
                        selection = currentSelection().toList()
                    )
                )
                renderCommand.send(Unit)
                controlPanelInteractor.onEvent(event)
            }
        }
    }

    private fun proceedWithUpdateTabInTableMode(
        tableId: Id,
        tab: BlockView.Table.Tab,
        modeTable: EditorMode.Table
    ) {
        val tableBlock = views.find { it.id == tableId } as? BlockView.Table
        if (tableBlock == null) {
            Timber.e("Couldn't find table block by id:$tableId")
            return
        }
        val event = when (tab) {
            BlockView.Table.Tab.CELL -> {
                clearSelections()
                select(modeTable.initialTargets.toList())
                mode = modeTable.copy(
                    initialTargets = currentSelection(),
                    targets = currentSelection(),
                    tab = BlockView.Table.Tab.CELL
                )
                ControlPanelMachine.Event.SimpleTableWidget.ShowCellTab(
                    cellItems = getSimpleTableWidgetCellItems(),
                    tableId = tableId,
                    cellSize = currentSelection().size
                )
            }
            BlockView.Table.Tab.COLUMN -> {
                clearSelections()
                select(modeTable.initialTargets.toList())
                val selectedColumns = tableBlock.getAllSelectedColumns(
                    selectedCellsIds = currentSelection()
                )
                select(selectedColumns.cellsInColumns)
                mode = modeTable.copy(
                    targets = currentSelection(),
                    tab = BlockView.Table.Tab.COLUMN
                )
                ControlPanelMachine.Event.SimpleTableWidget.ShowColumnTab(
                    columnItems = getSimpleTableWidgetColumnItems(
                        selectedColumns = selectedColumns.selectedColumns,
                        columnsSize = tableBlock.columns.size
                    ),
                    tableId = tableId,
                    columnsSize = selectedColumns.selectedColumns.size
                )
            }
            BlockView.Table.Tab.ROW -> {
                clearSelections()
                select(modeTable.initialTargets.toList())
                val selectedRows = tableBlock.getAllSelectedRows(
                    selectedCellsIds = currentSelection()
                )
                select(selectedRows.cellsInRows)
                mode = modeTable.copy(
                    targets = currentSelection(),
                    tab = BlockView.Table.Tab.ROW
                )
                ControlPanelMachine.Event.SimpleTableWidget.ShowRowTab(
                    rowItems = getSimpleTableWidgetRowItems(
                        selectedRows = selectedRows.selectedRows,
                        rowsSize = tableBlock.rows.size
                    ),
                    tableId = tableId,
                    rowsSize = selectedRows.selectedRows.size
                )
            }
        }
        if (currentSelection().isEmpty()) {
            proceedWithExitingTableMode()
        } else {
            viewModelScope.launch {
                orchestrator.stores.views.update(
                    views.updateTableBlockTab(
                        tableId = tableId,
                        selection = currentSelection().toList(),
                        tab = tab
                    )
                )
                renderCommand.send(Unit)
                controlPanelInteractor.onEvent(event)
            }
        }
    }

    private fun onTableRowEmptyCellClicked(cellId: Id, rowId: Id) {
        fillTableRow(
            cellId = cellId,
            targetIds = listOf(rowId)
        )
    }

    private fun fillTableRow(cellId: Id, targetIds: List<Id>) {
        viewModelScope.launch {
            setFocusInCellWhenInEditMode(cellId = cellId)
            orchestrator.proxies.intents.send(
                Intent.Table.FillTableRow(
                    ctx = context,
                    targetIds = targetIds
                )
            )
        }
    }

    private fun setFocusInCellWhenInEditMode(cellId: Id) {
        if (mode == EditorMode.Edit) {
            val focus = Editor.Focus(
                target = Editor.Focus.Target.Block(cellId),
                cursor = null
            )
            viewModelScope.launch {
                orchestrator.stores.focus.update(focus)
            }
        }
    }

    fun onSetBlockTextValueScreenDismiss() {
        clearSelections()
    }
    //endregion

    //region RELATIONS
    private fun proceedWithRelationBlockClicked(
        relationView: ObjectRelationView
    ) {
        Timber.d("proceedWithRelationBlockClicked, relationView:${relationView}")
        val relationId = relationView.id
        viewModelScope.launch {
            val relation = storeOfRelations.getById(relationId)
            if (relation == null) {
                Timber.w("Couldn't find relation in store by id:${relationId}")
                return@launch
            }
            if (relation.isReadonlyValue) {
                _toasts.emit(NOT_ALLOWED_FOR_RELATION)
                Timber.d("No interaction allowed with this relation")
                return@launch
            }
            if (checkRelationIsInObject(relationView)) {
                openRelationValueScreen(
                    relation = relation,
                    relationView = relationView
                )
            } else {
                proceedWithAddingRelationToObject(context, relationView) {
                    openRelationValueScreen(
                        relation = relation,
                        relationView = relationView
                    )
                }
            }
            openRelationValueScreen(relation, relationView)
        }
    }

    private fun checkRelationIsInObject(
        view: ObjectRelationView
    ): Boolean {
        val relationLinks = orchestrator.stores.relationLinks.current()
        return relationLinks.any { it.key == view.key }
    }

    private suspend fun proceedWithAddingRelationToObject(
        ctx: Id,
        view: ObjectRelationView,
        action: () -> Unit
    ) {
        val params = AddRelationToObject.Params(
            ctx = ctx,
            relationKey = view.key
        )
        addRelationToObject.run(params).process(
            failure = { Timber.e(it, "Error while adding relation to object") },
            success = {
                dispatcher.send(it)
                action.invoke()
            }
        )
    }

    private fun openRelationValueScreen(
        relation: ObjectWrapper.Relation,
        relationView: ObjectRelationView
    ) {
        when (relation.format) {
            RelationFormat.SHORT_TEXT,
            RelationFormat.LONG_TEXT,
            RelationFormat.URL,
            RelationFormat.PHONE,
            RelationFormat.NUMBER,
            RelationFormat.EMAIL -> {
                dispatch(
                    Command.OpenObjectRelationScreen.Value.Text(
                        ctx = context,
                        target = context,
                        relationKey = relation.key,
                        isLocked = mode == EditorMode.Locked
                    )
                )
            }
            RelationFormat.CHECKBOX -> {
                check(relationView is ObjectRelationView.Checkbox)
                proceedWithSetObjectDetails(
                    ctx = context,
                    key = relation.key,
                    value = !relationView.isChecked
                )
            }
            RelationFormat.DATE -> {
                dispatch(
                    Command.OpenObjectRelationScreen.Value.Date(
                        ctx = context,
                        target = context,
                        relationKey = relation.key
                    )
                )
            }
            else -> {
                dispatch(
                    Command.OpenObjectRelationScreen.Value.Default(
                        ctx = context,
                        target = context,
                        relationKey = relation.key,
                        targetObjectTypes = relation.relationFormatObjectTypes,
                        isLocked = mode == EditorMode.Locked
                    )
                )
            }
        }
    }
    //endregion

    //region FILE LIMITS
    private fun observeFileLimitsEvents() {
        jobs += viewModelScope.launch {
            interceptFileLimitEvents
                .run(Unit)
                .collect { event ->
                    if (event.any { it is FileLimitsEvent.FileLimitReached }) {
                        _toasts.emit("You exceeded file limit upload")
                    }
                }
        }
    }
    //endregion

    //region INTERNAL FLAGS
    private fun proceedWithCheckingInternalFlagShouldSelectType() {
        val internalFlags = getInternalFlagsFromDetails()
        if (internalFlags.contains(InternalFlags.ShouldSelectType)) {
            //We use this flag to show object type widget and then we don't need it anymore
            proceedWithGettingObjectTypesForObjectTypeWidget()
            proceedWithOptOutTypeInternalFlag()
        } else {
            Timber.d("Object doesn't have internal flag: ShouldSelectType")
        }
    }

    private fun proceedWithCheckingInternalFlagShouldSelectTemplate(objTypeId: Id? = null) {
        val internalFlags = getInternalFlagsFromDetails()
        if (internalFlags.contains(InternalFlags.ShouldSelectTemplate)) {
            //We use this flag to show template widget and then we don't need it anymore
            val properObjTypeId = objTypeId ?: getObjectTypeFromDetails() ?: return
            proceedWithStartTemplateEvent(objTypeId = properObjTypeId)
            proceedWithOptOutTemplateInternalFlag()
        } else {
            Timber.d("Object doesn't have internal flag: ShouldSelectTemplate")
        }
    }

    private fun proceedWithOptOutTypeInternalFlag() {
        val internalFlags = getInternalFlagsFromDetails()
        if (!internalFlags.contains(InternalFlags.ShouldSelectType)) return
        val flagsWithoutType = filterOutInternalFlags(
                flags = internalFlags,
                out = InternalFlags.ShouldSelectType
        )
        updateFlagsAndProceed(
                flags = flagsWithoutType,
                action = this::sendHideObjectTypeWidgetEvent
        )
    }

    private fun proceedWithOptOutTemplateInternalFlag() {
        val internalFlags = getInternalFlagsFromDetails()
        if (!internalFlags.contains(InternalFlags.ShouldSelectTemplate)) return
        val flagsWithoutTemplate = filterOutInternalFlags(
            flags = internalFlags,
            out = InternalFlags.ShouldSelectTemplate
        )
        updateFlagsAndProceed(flags = flagsWithoutTemplate)
    }

    private fun getInternalFlagsFromDetails(): List<InternalFlags> {
        val details = orchestrator.stores.details.current()
        val obj = ObjectWrapper.Basic(details.details[context]?.map ?: emptyMap())
        return obj.internalFlags
    }

    private fun filterOutInternalFlags(flags: List<InternalFlags>, out: InternalFlags): List<InternalFlags> {
        return flags.filter { it != out }
    }

    private fun updateFlagsAndProceed(flags: List<InternalFlags>, action: () -> Unit = {}) {
        viewModelScope.launch {
            val params = SetObjectInternalFlags.Params(
                    ctx = context,
                    flags = flags
            )
            setObjectInternalFlags.async(params).fold(
                    onSuccess = {
                        dispatcher.send(it)
                        Timber.d("Internal flags updated")
                        action.invoke()
                    },
                    onFailure = {
                        Timber.e(it, "Error while updating internal flags")
                        action.invoke()
                    }
            )
        }
    }
    //endregion
}

private const val NO_POSITION = -1
private const val OPEN_OBJECT_POSITION = 4