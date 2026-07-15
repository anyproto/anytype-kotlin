package com.anytypeio.anytype.presentation.sets

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.analytics.base.EventsPropertiesKey
import com.anytypeio.anytype.analytics.base.sendEvent
import com.anytypeio.anytype.analytics.props.Props
import com.anytypeio.anytype.analytics.props.Props.Companion.OBJ_TYPE_CUSTOM
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.DVViewer
import com.anytypeio.anytype.core_models.DVViewerCardSize
import com.anytypeio.anytype.core_models.DVViewerType
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.DataViewGroup
import com.anytypeio.anytype.core_models.GroupOrder
import com.anytypeio.anytype.core_models.ObjectOrder
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Position
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.DVSort
import com.anytypeio.anytype.core_models.DVSortType
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.SupportedLayouts
import com.anytypeio.anytype.core_models.SupportedLayouts.getCreateObjectLayouts
import com.anytypeio.anytype.core_models.TimeInMillis
import com.anytypeio.anytype.core_models.isDataView
import com.anytypeio.anytype.core_models.multiplayer.SpaceMemberPermissions
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncAndP2PStatusState
import com.anytypeio.anytype.core_models.permissions.layoutsSupportsEmojiAndImages
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.primitives.TypeId
import com.anytypeio.anytype.core_models.primitives.TypeKey
import com.anytypeio.anytype.core_models.restrictions.DataViewRestriction
import com.anytypeio.anytype.core_models.restrictions.ObjectRestriction
import com.anytypeio.anytype.core_utils.common.EventWrapper
import com.anytypeio.anytype.core_utils.ext.cancel
import com.anytypeio.anytype.core_utils.ext.typeOf
import com.anytypeio.anytype.domain.base.Result
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.block.interactor.CreateBlock
import com.anytypeio.anytype.domain.block.interactor.UpdateText
import com.anytypeio.anytype.domain.collections.AddObjectToCollection
import com.anytypeio.anytype.domain.collections.RemoveObjectFromCollection
import com.anytypeio.anytype.domain.cover.SetDocCoverImage
import com.anytypeio.anytype.domain.dataview.SetDataViewProperties
import com.anytypeio.anytype.domain.dataview.interactor.SetDataViewObjectOrder
import com.anytypeio.anytype.domain.search.BoardGroupSubscriptionContainer
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.library.StoreSearchParams
import com.anytypeio.anytype.domain.search.BoardRecordsSubscriptionContainer
import com.anytypeio.anytype.presentation.extension.removeUnsupportedFilters
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants.defaultDataViewFilters
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants.defaultDataViewKeys
import com.anytypeio.anytype.presentation.sets.subscription.updateWithRelationFormat
import com.anytypeio.anytype.domain.dataview.interactor.CreateDataViewObject
import com.anytypeio.anytype.domain.error.Error
import com.anytypeio.anytype.domain.event.interactor.InterceptEvents
import com.anytypeio.anytype.domain.event.interactor.SpaceSyncAndP2PStatusProvider
import com.anytypeio.anytype.domain.launch.GetDefaultObjectType
import com.anytypeio.anytype.domain.misc.DateProvider
import com.anytypeio.anytype.domain.misc.DeepLinkResolver
import com.anytypeio.anytype.core_models.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider
import com.anytypeio.anytype.domain.`object`.ConvertObjectToCollection
import com.anytypeio.anytype.domain.`object`.DuplicateObjects
import com.anytypeio.anytype.domain.`object`.UpdateDetail
import com.anytypeio.anytype.domain.objects.ObjectStore
import com.anytypeio.anytype.domain.objects.SetObjectListIsArchived
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.discussions.AddDiscussion
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.objects.getTypeOfObject
import com.anytypeio.anytype.domain.page.CloseObject
import com.anytypeio.anytype.domain.page.CreateObject
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.domain.resources.StringResourceProvider
import com.anytypeio.anytype.domain.search.DataViewState
import com.anytypeio.anytype.domain.search.DataViewSubscriptionContainer
import com.anytypeio.anytype.domain.sets.OpenObjectSet
import com.anytypeio.anytype.domain.sets.SetQueryToObjectSet
import com.anytypeio.anytype.domain.templates.CreateTemplate
import com.anytypeio.anytype.domain.unsplash.DownloadUnsplashImage
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.emojifier.data.EmojiProvider
import com.anytypeio.anytype.emojifier.suggest.EmojiSuggester
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import com.anytypeio.anytype.presentation.navigation.backstack.BackHistoryDelegate
import com.anytypeio.anytype.presentation.navigation.backstack.BackHistoryMenuItem
import com.anytypeio.anytype.presentation.vault.ExitToVaultDelegate
import com.anytypeio.anytype.presentation.common.Action
import com.anytypeio.anytype.presentation.common.Delegator
import com.anytypeio.anytype.presentation.editor.cover.CoverImageHashProvider
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.model.TextUpdate
import com.anytypeio.anytype.presentation.extension.ObjectStateAnalyticsEvent
import com.anytypeio.anytype.presentation.extension.getObject
import com.anytypeio.anytype.presentation.extension.getTypeObject
import com.anytypeio.anytype.presentation.extension.getUrlBasedOnFileLayout
import com.anytypeio.anytype.presentation.extension.logEvent
import com.anytypeio.anytype.presentation.extension.sendAnalyticsObjectCreateEvent
import com.anytypeio.anytype.presentation.extension.sendAnalyticsRelationEvent
import com.anytypeio.anytype.presentation.home.HomeScreenViewModel.Companion.HOME_SCREEN_PROFILE_OBJECT_SUBSCRIPTION
import com.anytypeio.anytype.core_models.ui.objectIcon
import com.anytypeio.anytype.presentation.mapper.toTemplateObjectTypeViewItems
import com.anytypeio.anytype.presentation.navigation.AppNavigation
import com.anytypeio.anytype.presentation.navigation.NavPanelState
import com.anytypeio.anytype.presentation.navigation.SupportNavigation
import com.anytypeio.anytype.presentation.navigation.leftButtonClickAnalytics
import com.anytypeio.anytype.core_models.ui.ObjectIcon
import com.anytypeio.anytype.presentation.objects.getCreateObjectParams
import com.anytypeio.anytype.presentation.objects.getTypeForObjectAndTargetTypeForTemplate
import com.anytypeio.anytype.presentation.objects.hasLayoutConflict
import com.anytypeio.anytype.presentation.objects.isCreateObjectAllowed
import com.anytypeio.anytype.presentation.objects.isTemplateObject
import com.anytypeio.anytype.presentation.objects.isTemplatesAllowed
import com.anytypeio.anytype.presentation.objects.sortByTypePriority
import com.anytypeio.anytype.presentation.objects.toFeaturedPropertiesViews
import com.anytypeio.anytype.presentation.relations.ObjectRelationView
import com.anytypeio.anytype.presentation.relations.ObjectSetConfig.DEFAULT_LIMIT
import com.anytypeio.anytype.presentation.relations.RelationListViewModel
import com.anytypeio.anytype.presentation.relations.render
import com.anytypeio.anytype.presentation.sets.model.CellView
import com.anytypeio.anytype.presentation.sets.model.Viewer
import com.anytypeio.anytype.presentation.sets.state.ObjectState
import com.anytypeio.anytype.presentation.sets.state.ObjectState.Companion.VIEW_DEFAULT_OBJECT_TYPE
import com.anytypeio.anytype.presentation.sets.state.ObjectStateReducer
import com.anytypeio.anytype.presentation.sets.subscription.DataViewSubscription
import com.anytypeio.anytype.presentation.sets.subscription.DefaultDataViewSubscription.Companion.getDataViewSubscriptionId
import com.anytypeio.anytype.presentation.sets.viewer.ViewerDelegate
import com.anytypeio.anytype.presentation.sets.viewer.ViewerEvent
import com.anytypeio.anytype.presentation.sets.viewer.ViewerView
import com.anytypeio.anytype.presentation.sync.SyncStatusWidgetState
import com.anytypeio.anytype.presentation.sync.toSyncStatusWidgetState
import com.anytypeio.anytype.presentation.sync.updateStatus
import com.anytypeio.anytype.presentation.templates.ObjectTypeTemplatesContainer
import com.anytypeio.anytype.presentation.templates.TemplateMenuClick
import com.anytypeio.anytype.presentation.templates.TemplateObjectTypeView
import com.anytypeio.anytype.presentation.templates.TemplateView
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.presentation.widgets.TypeTemplatesWidgetUI
import com.anytypeio.anytype.presentation.widgets.TypeTemplatesWidgetUIAction
import com.anytypeio.anytype.presentation.widgets.enterEditing
import com.anytypeio.anytype.presentation.widgets.exitEditing
import com.anytypeio.anytype.presentation.widgets.hideMoreMenu
import com.anytypeio.anytype.presentation.widgets.showMoreMenu
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

@OptIn(ExperimentalCoroutinesApi::class)
class ObjectSetViewModel(
    private val vmParams: Params,
    private val permissions: UserPermissionProvider,
    private val database: ObjectSetDatabase,
    private val openObjectSet: OpenObjectSet,
    private val closeObject: CloseObject,
    private val setObjectDetails: UpdateDetail,
    private val downloadUnsplashImage: DownloadUnsplashImage,
    private val setDocCoverImage: SetDocCoverImage,
    private val updateText: UpdateText,
    private val createBlock: CreateBlock,
    private val interceptEvents: InterceptEvents,
    private val dispatcher: Dispatcher<Payload>,
    private val delegator: Delegator<Action>,
    private val urlBuilder: UrlBuilder,
    private val coverImageHashProvider: CoverImageHashProvider,
    private val session: ObjectSetSession,
    private val analytics: Analytics,
    private val createDataViewObject: CreateDataViewObject,
    private val createObject: CreateObject,
    private val dataViewSubscriptionContainer: DataViewSubscriptionContainer,
    private val setQueryToObjectSet: SetQueryToObjectSet,
    private val paginator: ObjectSetPaginator,
    private val storeOfRelations: StoreOfRelations,
    private val stateReducer: ObjectStateReducer,
    private val dataViewSubscription: DataViewSubscription,
    private val objectStore: ObjectStore,
    private val addObjectToCollection: AddObjectToCollection,
    private val removeObjectFromCollection: RemoveObjectFromCollection,
    private val objectToCollection: ConvertObjectToCollection,
    private val storeOfObjectTypes: StoreOfObjectTypes,
    private val duplicateObjects: DuplicateObjects,
    private val templatesContainer: ObjectTypeTemplatesContainer,
    private val setObjectListIsArchived: SetObjectListIsArchived,
    private val spaceManager: SpaceManager,
    private val viewerDelegate: ViewerDelegate,
    private val createTemplate: CreateTemplate,
    private val dateProvider: DateProvider,
    private val analyticSpaceHelperDelegate: AnalyticSpaceHelperDelegate,
    private val spaceSyncAndP2PStatusProvider: SpaceSyncAndP2PStatusProvider,
    private val fieldParser: FieldParser,
    private val spaceViews: SpaceViewSubscriptionContainer,
    private val deepLinkResolver: DeepLinkResolver,
    private val setDataViewProperties: SetDataViewProperties,
    private val setDataViewObjectOrder: SetDataViewObjectOrder,
    private val boardGroupSubscriptionContainer: BoardGroupSubscriptionContainer,
    private val boardRecordsSubscriptionContainer: BoardRecordsSubscriptionContainer,
    private val storelessSubscriptionContainer: StorelessSubscriptionContainer,
    private val emojiProvider: EmojiProvider,
    private val emojiSuggester: EmojiSuggester,
    private val stringResourceProvider: StringResourceProvider,
    private val getDefaultObjectType: GetDefaultObjectType,
    private val addDiscussion: AddDiscussion,
    private val userSettingsRepository: UserSettingsRepository,
    private val backHistoryDelegate: BackHistoryDelegate,
    private val exitToVaultDelegate: ExitToVaultDelegate,
    private val viewStateDispatcher: CoroutineDispatcher = Dispatchers.Default
) : ViewModel(), SupportNavigation<EventWrapper<AppNavigation.Command>>,
    ViewerDelegate by viewerDelegate,
    BackHistoryDelegate by backHistoryDelegate,
    ExitToVaultDelegate by exitToVaultDelegate,
    AnalyticSpaceHelperDelegate by analyticSpaceHelperDelegate
{

    val permission = MutableStateFlow<SpaceMemberPermissions?>(SpaceMemberPermissions.NO_PERMISSIONS)

    private val isOwnerOrEditor get() = permission.value?.isOwnerOrEditor() ==  true

    val error = MutableStateFlow<String?>(null)

    val featured = MutableStateFlow<BlockView.FeaturedRelation?>(null)

    /**
     * State of the bottom-left "Discussion" button — mirrors [DiscussionButtonState]
     * in EditorViewModel so all object screens share the same two-button layout.
     */
    private val _discussionButtonState =
        MutableStateFlow<DiscussionButtonState>(DiscussionButtonState.Hidden)
    val discussionButtonState: StateFlow<DiscussionButtonState> = _discussionButtonState

    override val navigation = MutableLiveData<EventWrapper<AppNavigation.Command>>()

    private val titleUpdateChannel = Channel<TextUpdate>()

    private val defaultPayloadConsumer: suspend (Payload) -> Unit = { payload ->
        stateReducer.dispatch(payload.events)
    }

    val pagination get() = paginator.pagination

    private val jobs = mutableListOf<Job>()

    /**
     * Teardown launched by [onStop] ([unsubscribeFromAllSubscriptions]). The record
     * subscription restarted by the next [onStart] must wait for it: both the cancel and the
     * new subscribe target the same backend subscription id, and if the new subscribe RPC
     * overtook the still-in-flight cancel, the freshly created subscription would be killed,
     * freezing the grid on its initial results.
     */
    private var unsubscribeJob: Job? = null

    private val _commands = MutableSharedFlow<ObjectSetCommand>(replay = 0)
    val commands: SharedFlow<ObjectSetCommand> = _commands
    val toasts = MutableSharedFlow<String>(replay = 0)

    private val _currentViewer: MutableStateFlow<DataViewViewState> =
        MutableStateFlow(DataViewViewState.Init)
    val currentViewer = _currentViewer

    /**
     * Loaded options (label + color) for the active board's group relation, keyed
     * by option id, used to render readable column headers.
     */
    private val boardGroupOptions = MutableStateFlow<Map<Id, ObjectWrapper.Option>>(emptyMap())

    /**
     * Live groups (columns) of the active board view, from the backend group subscription.
     * `null` means the subscription hasn't delivered yet (board still loading), as opposed
     * to an empty list (loaded, no groups) — the render layer shows Init for the former.
     */
    private val boardGroups = MutableStateFlow<List<DataViewGroup>?>(null)

    /** Per-column record pages (ids + backend total), one paged subscription per column. */
    private val boardRecords = MutableStateFlow<Map<Id, BoardRecordsSubscriptionContainer.GroupPage>>(emptyMap())

    /** Subscription ids of the currently active per-column record subscriptions, for cleanup. */
    private var boardRecordSubscriptionIds: List<Id> = emptyList()

    /** Subscription id of the live options (column headers) of the board's group relation. */
    private val boardOptionsSubscriptionId = "${vmParams.ctx}$SUBSCRIPTION_BOARD_OPTIONS_POSTFIX"

    /**
     * Subscription id of the live options of the active data view's Tag/Status relations. Their
     * objects are merged into the shared [objectStore] so grid/gallery/list cells can resolve their
     * option chips. Needed for TypeSets, where the type's relations are added to the data view only
     * after the record subscription has started, so option objects never arrive via that
     * subscription's dependency snapshot (DROID-4542).
     */
    private val dataViewOptionsSubscriptionId = "${vmParams.ctx}$SUBSCRIPTION_DATA_VIEW_OPTIONS_POSTFIX"

    /** Bumped whenever the data view's Tag/Status options are (re)merged into the store, to trigger a re-render. */
    private val dataViewOptionsVersion = MutableStateFlow(0)

    /** Whether the experimental Kanban (Board) view is enabled; when off, BOARD views are unsupported. */
    private val isKanbanEnabled = userSettingsRepository.observeKanbanEnabled()
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    /**
     * Terminal failure of the board group/record subscription. Once set, the board can never
     * populate (a failed flow stays dead until the screen restarts), so the render layer shows
     * an explicit error instead of an eternal loading/empty board. Cleared in [onStop].
     */
    private val boardSubscriptionError = MutableStateFlow<Throwable?>(null)

    /** Fires a board re-render whenever any board-specific flow changes. */
    private val boardRenderTrigger = combine(
        boardGroupOptions,
        boardGroups,
        boardRecords,
        isKanbanEnabled,
        boardSubscriptionError
    ) { _, _, _, _, _ -> Unit }

    /** Fires a re-render whenever the board flows OR the data view's Tag/Status options change. */
    private val renderTrigger = combine(
        boardRenderTrigger,
        dataViewOptionsVersion
    ) { _, _ -> Unit }

    private val _dvViews = MutableStateFlow<List<ViewerView>>(emptyList())

    private val _header = MutableStateFlow<SetOrCollectionHeaderState>(
        SetOrCollectionHeaderState.None
    )
    val header: StateFlow<SetOrCollectionHeaderState> = _header

    val isCustomizeViewPanelVisible = MutableStateFlow(false)
    val typeTemplatesWidgetState: MutableStateFlow<TypeTemplatesWidgetUI> = MutableStateFlow(TypeTemplatesWidgetUI.Init())
    val viewersWidgetState = MutableStateFlow(ViewersWidgetUi.init())
    val viewerEditWidgetState = MutableStateFlow<ViewerEditWidgetUi>(ViewerEditWidgetUi.Init)
    val viewerLayoutWidgetState = MutableStateFlow(ViewerLayoutWidgetUi.init())
    private val widgetViewerId = MutableStateFlow<String?>(null)
    val isTitleToolbarVisible = MutableStateFlow(false)

    @Deprecated("could be deleted")
    val isLoading = MutableStateFlow(false)

    private val isObjectCreationInProgress = AtomicBoolean(false)

    private val selectedTypeFlow: MutableStateFlow<ObjectWrapper.Type?> = MutableStateFlow(null)


    private val pendingScrollToObject = MutableStateFlow<Id?>(null)

    /**
     * State for the Set Object Name bottom sheet.
     */
    data class SetObjectNameState(
        val isVisible: Boolean = false,
        val targetObjectId: Id? = null,
        val currentIcon: ObjectIcon = ObjectIcon.None,
        val inputText: String = "",
        val isIconChangeAllowed: Boolean = false,
        val targetBlockId: Id? = null  // For Note objects, stores the blockId
    )

    private val _setObjectNameState = MutableStateFlow(SetObjectNameState())
    val setObjectNameState: StateFlow<SetObjectNameState> = _setObjectNameState.asStateFlow()

    val navPanelState = permission.map { permission ->
        NavPanelState.fromPermission(
            permission = permission,
            isOneToOneSpace = spaceViews.get(space = vmParams.space)?.isOneToOneSpace == true,
        )
    }

    init {
        Timber.i("ObjectSetViewModel, init")

        proceedWIthObservingPermissions()

        viewModelScope.launch {
            stateReducer.state
                .filterIsInstance<ObjectState.DataView>()
                .distinctUntilChanged()
                .combine(permission) { state, permission ->
                    state to permission
                }
                .collectLatest { (state, permission) ->
                    val featuredBlock = toFeaturedPropertiesViews(
                        objectId = vmParams.ctx,
                        urlBuilder = urlBuilder,
                        fieldParser = fieldParser,
                        storeOfObjectTypes = storeOfObjectTypes,
                        storeOfRelations = storeOfRelations,
                        blocks = state.blocks,
                        details = state.details,
                        participantCanEdit = permission?.isOwnerOrEditor() == true
                    )
                    featured.value = featuredBlock
                    _header.value = state.header(
                        ctx = vmParams.ctx,
                        urlBuilder = urlBuilder,
                        coverImageHashProvider = coverImageHashProvider,
                        storeOfObjectTypes = storeOfObjectTypes,
                        isReadOnlyMode = permission == SpaceMemberPermissions.NO_PERMISSIONS || permission == SpaceMemberPermissions.READER
                    )
                    updateDiscussionButtonState(state)
                    updateLayoutConflictState(featuredBlock = featuredBlock)
                }
        }

        subscribeToDataViewViewer()

        viewModelScope.launch {
            dispatcher.flow().collect { defaultPayloadConsumer(it) }
        }

        viewModelScope.launch {
            dataViewSubscriptionContainer.counter.collect { counter ->
                Timber.d("SET-DB: counter —> %s", counter)
                paginator.total.value = counter.total
            }
        }

        viewModelScope.launch {
            stateReducer.effects.collect { effects ->
                effects.forEach { effect ->
                    Timber.d("Received side effect: $effect")
                }
            }
        }

        viewModelScope.launch { stateReducer.run() }

        // Title updates pipeline

        viewModelScope.launch {
            titleUpdateChannel
                .consumeAsFlow()
                .distinctUntilChanged()
                .map {
                    UpdateText.Params(
                        context = vmParams.ctx,
                        target = it.target,
                        text = it.text,
                        marks = emptyList()
                    )
                }
                .mapLatest { params ->
                    updateText(params).process(
                        failure = { e -> Timber.e(e, "Error while updating title") },
                        success = {
                            Timber.d("Sets' title updated successfully") }
                    )
                }
                .collect()
        }

        viewModelScope.launch {
            delegator.receive().collect { action ->
                when (action) {
                    is Action.SetUnsplashImage -> {
                        proceedWithSettingUnsplashImage(action)
                    }
                    is Action.OpenObject -> proceedWithOpeningObject(
                        target = action.target,
                        space = action.space
                    )
                    is Action.OpenCollection -> proceedWithOpeningObjectCollection(
                        target = action.target,
                        space = action.space
                    )
                    is Action.Duplicate -> proceedWithNavigation(
                        target = action.target,
                        layout = ObjectType.Layout.SET,
                        space = action.space
                    )
                    else -> {}
                }
            }
        }

        viewModelScope.launch {
            combine(
                _dvViews,
                permission
            ) {  views, permissions ->
                views to permissions
            }.collect { (views, permissions) ->
                viewersWidgetState.value = viewersWidgetState.value.copy(
                    items = views,
                    isReadOnly = permissions?.isOwnerOrEditor() != true
                )
            }
        }

        viewModelScope.launch {
            combine(
                widgetViewerId,
                stateReducer.state,
            ) { viewId, state ->
                if (viewId != null) {
                    val dataView = state.dataViewState()
                    val pair = dataView?.viewerAndIndexById(viewId)
                    if (dataView != null && pair != null) {
                        viewerEditWidgetState.value = pair.first.toViewerEditWidgetState(
                            storeOfRelations = storeOfRelations,
                            index = pair.second,
                            session = session,
                            stringResourceProvider = stringResourceProvider
                        )
                        viewerLayoutWidgetState.value = viewerLayoutWidgetState.value.updateState(
                            viewer = pair.first,
                            storeOfRelations = storeOfRelations,
                            relationLinks = dataView.dataViewContent.relationLinks
                        )
                    } else {
                        viewerEditWidgetState.value = ViewerEditWidgetUi.Init
                        viewerLayoutWidgetState.value = viewerLayoutWidgetState.value.empty()
                    }
                }
            }.collect()
        }

        // Observe icon changes for SetObjectNameBottomSheet
        viewModelScope.launch {
            _setObjectNameState
                .filter { it.isVisible && it.targetObjectId != null }
                .flatMapLatest { sheetState ->
                    database.observe(sheetState.targetObjectId!!)
                        .map { obj ->
                            obj.objectIcon(
                                builder = urlBuilder,
                                objType = storeOfObjectTypes.getTypeOfObject(obj)
                            )
                        }
                }
                .distinctUntilChanged()
                .catch {
                    Timber.w(it, "Error while observing object icon")
                }
                .collect { icon ->
                    _setObjectNameState.update { it.copy(currentIcon = icon) }
                }
        }

        subscribeToSelectedType()
        subscribeToSyncTypeRelations()
    }

    /**
     * Syncs type's recommended relations to DataView's relationLinks.
     * This matches desktop's behavior of calling BlockDataviewRelationSet
     * when opening a TypeSet to ensure type relations (like "Done") are available as filter options.
     */
    private fun subscribeToSyncTypeRelations() {
        viewModelScope.launch {
            stateReducer.state
                .filterIsInstance<ObjectState.DataView>()
                .distinctUntilChanged { old, new ->
                    // Skip emissions where isInitialized remains the same.
                    // Combined with the `if (state.isInitialized)` check below,
                    // this ensures we only sync when transitioning to initialized state.
                    // So the emission only passes when isInitialized changes (either direction):
                    // - false → true ✅ passes through
                    // - true → false ✅ passes through
                    // - true → true ❌ filtered out
                    // - false → false ❌ filtered out

                    old.isInitialized == new.isInitialized
                }
                .collect { state ->
                    if (state.isInitialized) {
                        proceedWithSyncingTypeRelations(state)
                    }
                }
        }
    }

    private suspend fun proceedWithSyncingTypeRelations(state: ObjectState.DataView) {
        val typeId = when (state) {
            is ObjectState.DataView.TypeSet -> {
                // For TypeSet, the context is the type itself
                vmParams.ctx
            }
            is ObjectState.DataView.Set,
            is ObjectState.DataView.Collection -> {
                // Sets and Collections don't need type relation syncing
                return
            }
        }

        val type = storeOfObjectTypes.get(typeId) ?: return

        // Build relation keys list: ['name', 'description'] + type's recommended relations
        val typeRelationKeys = type.allRecommendedRelations.mapNotNull { relationId ->
            storeOfRelations.getById(relationId)?.key
        }

        val relationKeys = buildList {
            add(Relations.NAME)
            add(Relations.DESCRIPTION)
            addAll(typeRelationKeys)
        }.distinct()

        Timber.d("Syncing type relations to DataView: $relationKeys")

        setDataViewProperties.async(
            SetDataViewProperties.Params(
                objectId = vmParams.ctx,
                properties = relationKeys
            )
        ).fold(
            onSuccess = { payload ->
                Timber.d("Successfully synced type relations to DataView")
                dispatcher.send(payload)
            },
            onFailure = { error ->
                Timber.e(error, "Failed to sync type relations to DataView")
            }
        )
    }

    private fun proceedWIthObservingPermissions() {
        viewModelScope.launch {
            permissions
                .observe(vmParams.space)
                .collect {
                    permission.value = it
                }
        }
    }

    private suspend fun proceedWithSettingUnsplashImage(
        action: Action.SetUnsplashImage
    ) {
        downloadUnsplashImage(
            DownloadUnsplashImage.Params(
                picture = action.img,
                space = vmParams.space,
                createdInContext = vmParams.ctx,
                createdInContextRef = Relations.COVER_ID
            )
        ).process(
            failure = {
                Timber.e(it, "Error while download unsplash image")
            },
            success = { hash ->
                setDocCoverImage(
                    SetDocCoverImage.Params.FromHash(
                        context = vmParams.ctx,
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

    private suspend fun updateLayoutConflictState(
        featuredBlock: BlockView.FeaturedRelation?,
    ) {
        val state = stateReducer.state.value.dataViewState() ?: return
        val objectWrapper = state.details.getObject(vmParams.ctx)

        val blocks = featuredBlock?.let { listOf(it) } ?: emptyList()

        val hasConflict = hasLayoutConflict(
            currentObject = objectWrapper,
            blocks = blocks,
            storeOfObjectTypes = storeOfObjectTypes
        )

        dispatcher.send(
            Payload(
                context = vmParams.ctx,
                events = listOf(
                    Event.Command.DataView.UpdateConflictState(
                        context = vmParams.ctx,
                        hasConflict = hasConflict
                    )
                )
            )
        )
    }

    fun onStart(view: Id? = null) {
        Timber.d("onStart, ctx:[${vmParams.ctx}], space:[${vmParams.space}], view:[$view]")
        if (view != null) {
            session.currentViewerId.value = view
        }
        subscribeToEvents(ctx = vmParams.ctx)
        // The record subscription and board group subscriptions are tied to the
        // start/stop lifecycle (registered in `jobs`, cancelled in onStop) so each reopen
        // gets a fresh collector that re-subscribes — otherwise the backend subs,
        // cancelled in onStop, would never be re-established after the first
        // background/foreground cycle.
        subscribeToObjectState()
        subscribeToBoardGroupOptions()
        subscribeToDataViewRelationOptions()
        subscribeToBoardGroups()
        subscribeToBoardRecords()
        proceedWithOpeningCurrentObject(ctx = vmParams.ctx)
        proceedWithObservingSyncStatus()
    }

    private fun subscribeToEvents(ctx: Id) {
        jobs += viewModelScope.launch {
            interceptEvents
                .build(InterceptEvents.Params(ctx))
                .collect { events -> stateReducer.dispatch(events) }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun subscribeToObjectState() {
        Timber.d("subscribeToObjectState, ctx:[${vmParams.ctx}]")
        jobs += viewModelScope.launch {
            // Wait for the previous stop's backend unsubscribe (same subscription id) before
            // issuing any new subscribe request — see [unsubscribeJob].
            unsubscribeJob?.join()
            combine(
                stateReducer.state,
                paginator.offset,
                session.currentViewerId,
                // Subscription params resolve relation formats against storeOfRelations
                // (updateFormatForSubscription / updateWithRelationFormat). A tick on store
                // changes lets the fingerprint below pick up formats that arrive after the
                // first subscription (e.g. set opened before the relations subscription has
                // populated the store) — otherwise the subscription would keep the LONG_TEXT
                // fallback formats for the whole session.
                storeOfRelations.trackChanges()
            ) { state, offset, view, _ ->
                Query(
                    state = state,
                    offset = offset,
                    currentViewerId = view
                )
            }
                // Restart the backend record subscription only when the parts of the state
                // that actually feed its params (filters, sorts, sources, keys, offset,
                // active view, resolved relation formats) change — not on every unrelated
                // object-state emission (e.g. a detail amend on the set object itself).
                .map { query -> query to query.subscriptionFingerprint() }
                .distinctUntilChanged { old, new ->
                    old.second == new.second
                }
                .flatMapLatest { (query, _) ->
                val activeViewer = query.state.dataViewState()?.viewerByIdOrFirst(query.currentViewerId)
                if (activeViewer?.type == DVViewerType.BOARD && isKanbanEnabled.value) {
                    // An enabled board is driven entirely by per-column record subscriptions
                    // (subscribeToBoardRecords), not the single flat 50-record window. When the
                    // experimental flag is off the board is unsupported, so the normal sub runs.
                    return@flatMapLatest flowOf(DataViewState.Loaded(objects = emptyList(), dependencies = emptyList()))
                }
                when (query.state) {
                    is ObjectState.DataView.Collection -> {
                        Timber.d("subscribeToObjectState, NEW COLLECTION STATE")
                        if (query.state.isInitialized) {
                            dataViewSubscription.startObjectCollectionSubscription(
                                space = vmParams.space.id,
                                context = vmParams.ctx,
                                collection = vmParams.ctx,
                                state = query.state,
                                currentViewerId = query.currentViewerId,
                                offset = query.offset,
                                storeOfRelations = storeOfRelations
                            )
                        } else {
                            emptyFlow()
                        }
                    }

                    is ObjectState.DataView.Set -> {
                        Timber.d("subscribeToObjectState, NEW SET STATE")
                        if (query.state.isInitialized) {
                            dataViewSubscription.startObjectSetSubscription(
                                space = vmParams.space.id,
                                context = vmParams.ctx,
                                state = query.state,
                                currentViewerId = query.currentViewerId,
                                offset = query.offset,
                                storeOfRelations = storeOfRelations
                            )
                        } else {
                            emptyFlow()
                        }
                    }

                    is ObjectState.DataView.TypeSet -> {
                        Timber.d("subscribeToObjectState, NEW TYPE SET STATE")
                        if (query.state.isInitialized) {
                            dataViewSubscription.startObjectTypeSetSubscription(
                                space = vmParams.space.id,
                                context = vmParams.ctx,
                                state = query.state,
                                currentViewerId = query.currentViewerId,
                                offset = query.offset,
                                storeOfRelations = storeOfRelations
                            )
                        } else {
                            emptyFlow()
                        }
                    }

                    else -> {
                        Timber.d("subscribeToObjectState, NEW STATE, ${query.state}")
                        emptyFlow()
                    }
                }
            }.onEach { dataViewState ->
                if (dataViewState is DataViewState.Loaded) {
                    Timber.d("subscribeToObjectState, New index size: ${dataViewState.objects.size}")
                }
                database.update(dataViewState)
            }
                .catch { error ->
                    Timber.e("subscribeToObjectState error : $error")
                    _currentViewer.value =
                        DataViewViewState.Error("Error while getting objects:\n${error.message}")
                }
                .collect()
        }
    }

    private fun proceedWithOpeningCurrentObject(ctx: Id) {
        Timber.d("proceedWithOpeningCurrentObject, ctx:[$ctx]")
        val startTime = System.currentTimeMillis()
        viewModelScope.launch {
            openObjectSet(
                OpenObjectSet.Params(
                    obj = ctx,
                    space = vmParams.space
                )
            ).process(
                success = { result ->
                    when (result) {
                        is Result.Failure -> {
                            when (result.error) {
                                Error.BackwardCompatibility -> {
                                    navigation.postValue(
                                        EventWrapper(AppNavigation.Command.OpenUpdateAppScreen)
                                    )
                                }
                                Error.NotFoundObject -> {
                                    toast(TOAST_SET_NOT_EXIST).also {
                                        dispatch(AppNavigation.Command.Exit(vmParams.space.id))
                                    }
                                }
                            }
                        }
                        is Result.Success -> {
                            Timber.d("proceedWithOpeningCurrentObject, ctx:[$ctx] SUCCESS")
                            defaultPayloadConsumer(result.data)
                            logEvent(
                                state = stateReducer.state.value,
                                analytics = analytics,
                                event = ObjectStateAnalyticsEvent.OPEN_OBJECT,
                                startTime = startTime,
                                currentViewId = session.currentViewerId.value,
                                spaceParams = provideParams(vmParams.space.id)
                            )
                        }
                    }
                },
                failure = {
                    Timber.e(it, "Error while opening object set: $ctx")
                }
            )
        }
    }

    /**
     * Subscribes to the backend groups (columns) of the active board view. Resubscribes
     * when the active view or its group relation changes; clears when leaving a board.
     */
    private fun subscribeToBoardGroups() {
        jobs += viewModelScope.launch {
            // Wait for the previous stop's backend unsubscribe (same subscription id) before
            // issuing any new subscribe request — see [unsubscribeJob].
            unsubscribeJob?.join()
            combine(stateReducer.state, session.currentViewerId, isKanbanEnabled) { state, currentViewId, kanbanEnabled ->
                val dataView = state.dataViewState()
                val viewer = dataView?.viewerByIdOrFirst(currentViewId)
                if (kanbanEnabled && dataView != null && viewer != null && viewer.type == DVViewerType.BOARD) {
                    buildBoardGroupParams(dataView, viewer)
                } else {
                    null
                }
            }
                // Only (re)subscribe when the group query actually changes; otherwise every
                // unrelated state emission would re-run objectGroupsSubscribe on the same id.
                .distinctUntilChanged()
                .flatMapLatest { params ->
                    if (params != null) {
                        boardGroupSubscriptionContainer.observe(params)
                    } else {
                        // Left the board view: drop the backend group subscription instead of
                        // leaving it streaming until onStop. Reset to "not loaded" so a return
                        // to the board shows the loading state until groups arrive again.
                        boardGroupSubscriptionContainer.unsubscribe(
                            vmParams.ctx + BoardGroupSubscriptionContainer.SUBSCRIPTION_POSTFIX
                        )
                        flowOf(null)
                    }
                }
                .catch { e ->
                    Timber.e(e, "Error in board groups subscription")
                    boardSubscriptionError.value = e
                    emit(null)
                }
                .collect { groups ->
                    boardGroups.value = groups
                }
        }
    }

    private suspend fun buildBoardGroupParams(
        state: ObjectState.DataView,
        viewer: DVViewer
    ): BoardGroupSubscriptionContainer.Params? {
        val relationKey = viewer.groupRelationKey?.takeIf { it.isNotEmpty() } ?: return null
        val filters = buildList {
            addAll(viewer.filters.updateFormatForSubscription(storeOfRelations).removeUnsupportedFilters())
            addAll(defaultDataViewFilters())
        }
        val sources: List<String>
        val collection: Id?
        when (state) {
            is ObjectState.DataView.Collection -> {
                sources = emptyList()
                collection = vmParams.ctx
            }
            is ObjectState.DataView.Set -> {
                sources = state.filterOutDeletedAndMissingObjects(state.getSetOfValue(vmParams.ctx))
                collection = null
            }
            is ObjectState.DataView.TypeSet -> {
                sources = state.filterOutDeletedAndMissingObjects(state.getSetOfValue(vmParams.ctx))
                collection = null
            }
        }
        return BoardGroupSubscriptionContainer.Params(
            space = vmParams.space,
            subscription = vmParams.ctx + BoardGroupSubscriptionContainer.SUBSCRIPTION_POSTFIX,
            relationKey = relationKey,
            filters = filters,
            sources = sources,
            collection = collection
        )
    }

    /**
     * Subscribes to records per board column — one paged subscription each, server-side
     * filtered to that column's group value — so every column populates independently (instead
     * of distributing one shared 50-record page). Re-subscribes when the columns/query change;
     * clears when leaving a board.
     */
    private fun subscribeToBoardRecords() {
        jobs += viewModelScope.launch {
            // Wait for the previous stop's backend unsubscribe (same subscription ids) before
            // issuing any new subscribe request — see [unsubscribeJob].
            unsubscribeJob?.join()
            combine(stateReducer.state, session.currentViewerId, boardGroups, isKanbanEnabled) { state, currentViewId, groups, kanbanEnabled ->
                val dataView = state.dataViewState()
                val viewer = dataView?.viewerByIdOrFirst(currentViewId)
                if (kanbanEnabled && dataView != null && viewer != null && viewer.type == DVViewerType.BOARD && !groups.isNullOrEmpty()) {
                    buildBoardRecordsParams(dataView, viewer, groups)
                } else {
                    null
                }
            }
                .distinctUntilChanged()
                .flatMapLatest { params ->
                    // flatMapLatest only cancels the client flow; each column's backend
                    // subscription is an external resource that must be cancelled explicitly.
                    // Unchanged columns keep their subscription id (re-subscribing on the same
                    // id replaces server-side); columns absent from the new params are stale
                    // and would otherwise stream on the middleware until the session ends.
                    val newIds = params?.columns?.map { it.subscription }.orEmpty()
                    val stale = boardRecordSubscriptionIds.filterNot { it in newIds }
                    if (stale.isNotEmpty()) boardRecordsSubscriptionContainer.unsubscribe(stale)
                    boardRecordSubscriptionIds = newIds
                    if (params != null) {
                        boardRecordsSubscriptionContainer.observe(params)
                    } else {
                        flowOf(emptyMap())
                    }
                }
                .catch { e ->
                    Timber.e(e, "Error in board records subscription")
                    boardSubscriptionError.value = e
                    emit(emptyMap())
                }
                .collect { records ->
                    boardRecords.value = records
                }
        }
    }

    private suspend fun buildBoardRecordsParams(
        state: ObjectState.DataView,
        viewer: DVViewer,
        groups: List<DataViewGroup>
    ): BoardRecordsSubscriptionContainer.Params? {
        val relationKey = viewer.groupRelationKey?.takeIf { it.isNotEmpty() } ?: return null
        val columnQueries = boardColumnQueries(groups, relationKey)
        if (columnQueries.isEmpty()) return null
        val baseFilters = buildList {
            addAll(viewer.filters.updateFormatForSubscription(storeOfRelations).removeUnsupportedFilters())
            addAll(defaultDataViewFilters())
        }
        val keys = defaultDataViewKeys + state.dataViewContent.relationLinks.map { it.key }
        val sorts = viewer.sorts.ifEmpty {
            listOf(
                DVSort(
                    relationKey = Relations.CREATED_DATE,
                    type = DVSortType.DESC,
                    includeTime = true,
                    relationFormat = RelationFormat.DATE
                )
            )
        }.updateWithRelationFormat(storeOfRelations)
        val sources: List<String>
        val collection: Id?
        when (state) {
            is ObjectState.DataView.Collection -> {
                sources = emptyList()
                collection = vmParams.ctx
            }
            is ObjectState.DataView.Set -> {
                sources = state.filterOutDeletedAndMissingObjects(state.getSetOfValue(vmParams.ctx))
                collection = null
            }
            is ObjectState.DataView.TypeSet -> {
                sources = state.filterOutDeletedAndMissingObjects(state.getSetOfValue(vmParams.ctx))
                collection = null
            }
        }
        val columns = columnQueries.map { query ->
            BoardRecordsSubscriptionContainer.Column(
                subscription = vmParams.ctx + "-board-records-" + query.columnId,
                columnId = query.columnId,
                filter = query.filter
            )
        }
        // Keys the board card actually displays (visible viewer relations); mirrors
        // BoardViewMapper.filteredRelations. Used to keep amend re-render suppression fail-safe.
        val displayedKeys = viewer.viewerRelations
            .filter { it.isVisible }
            .map { it.key }
            .toSet()
        return BoardRecordsSubscriptionContainer.Params(
            space = vmParams.space,
            columns = columns,
            sorts = sorts,
            baseFilters = baseFilters,
            keys = keys,
            displayedKeys = displayedKeys,
            source = sources,
            collection = collection,
            limit = DEFAULT_LIMIT
        )
    }

    /**
     * Live options (labels + colors) for the active board view's group relation, so column
     * headers follow option renames/recolors/additions while the board is open. Replaced when
     * the group relation changes; dropped when leaving the board.
     */
    private fun subscribeToBoardGroupOptions() {
        jobs += viewModelScope.launch {
            // Wait for the previous stop's backend unsubscribe (same subscription id) before
            // issuing any new subscribe request — see [unsubscribeJob].
            unsubscribeJob?.join()
            combine(stateReducer.state, session.currentViewerId, isKanbanEnabled) { state, currentViewId, kanbanEnabled ->
                val viewer = state.dataViewState()?.viewerByIdOrFirst(currentViewId)
                if (kanbanEnabled && viewer?.type == DVViewerType.BOARD) {
                    viewer.groupRelationKey?.takeIf { it.isNotEmpty() }
                } else {
                    null
                }
            }
                .distinctUntilChanged()
                .flatMapLatest { groupRelationKey ->
                    if (groupRelationKey == null) {
                        storelessSubscriptionContainer.unsubscribe(listOf(boardOptionsSubscriptionId))
                        flowOf(emptyMap())
                    } else {
                        storelessSubscriptionContainer.subscribe(
                            StoreSearchParams(
                                space = vmParams.space,
                                subscription = boardOptionsSubscriptionId,
                                filters = relationOptionsFilters(listOf(groupRelationKey)),
                                keys = listOf(
                                    Relations.ID,
                                    Relations.NAME,
                                    Relations.RELATION_OPTION_COLOR
                                )
                            )
                        ).map { options ->
                            options.associate { option -> option.id to ObjectWrapper.Option(option.map) }
                        }
                    }
                }
                .catch { e ->
                    Timber.e(e, "Error in board group options subscription")
                    emit(emptyMap())
                }
                .collect { options ->
                    boardGroupOptions.value = options
                }
        }
    }

    /**
     * Live Tag/Status options (name + color) for the active data view's relations, merged into the
     * shared [objectStore] so grid/gallery/list cells resolve their option chips. Needed for
     * TypeSets, where the type's relations are added to the data view only after the record
     * subscription has started, so the option objects never arrive via that subscription's
     * dependency snapshot. Harmless for Sets/Collections: the same options are simply re-merged
     * idempotently (DROID-4542).
     */
    private fun subscribeToDataViewRelationOptions() {
        jobs += viewModelScope.launch {
            // Wait for the previous stop's backend unsubscribe (same subscription id) before
            // issuing any new subscribe request — see [unsubscribeJob].
            unsubscribeJob?.join()
            // Also re-evaluate on relation-store changes: resolving a link's format below needs the
            // relation object, which may arrive only after the last state emission on cold start.
            combine(
                stateReducer.state,
                storeOfRelations.trackChanges()
            ) { state, _ -> state }
                .map { state ->
                    state.dataViewState()?.dataViewContent?.relationLinks
                        ?.mapNotNull { link ->
                            val format = storeOfRelations.getByKey(link.key)?.format
                            if (format == RelationFormat.TAG || format == RelationFormat.STATUS) {
                                link.key
                            } else {
                                null
                            }
                        }
                        ?.distinct()
                        .orEmpty()
                }
                .distinctUntilChanged()
                .flatMapLatest { tagStatusKeys ->
                    if (tagStatusKeys.isEmpty()) {
                        storelessSubscriptionContainer.unsubscribe(listOf(dataViewOptionsSubscriptionId))
                        flowOf(emptyList())
                    } else {
                        storelessSubscriptionContainer.subscribe(
                            StoreSearchParams(
                                space = vmParams.space,
                                subscription = dataViewOptionsSubscriptionId,
                                filters = relationOptionsFilters(tagStatusKeys),
                                keys = listOf(
                                    Relations.ID,
                                    Relations.SPACE_ID,
                                    Relations.NAME,
                                    Relations.RELATION_OPTION_COLOR,
                                    Relations.RELATION_KEY,
                                    // The grid render checks isDeleted before showing a chip; fetch
                                    // it so a deletion amend can be honoured at render time.
                                    Relations.IS_DELETED
                                )
                            )
                        )
                    }
                }
                .catch { e ->
                    Timber.e(e, "Error in data view relation options subscription")
                    emit(emptyList())
                }
                .collect { options ->
                    // Merge into the shared store (grid/gallery/list read options via store.get),
                    // then bump the version so the render pipeline re-reads the store.
                    objectStore.merge(
                        objects = options,
                        dependencies = emptyList(),
                        subscriptions = listOf(dataViewOptionsSubscriptionId)
                    )
                    dataViewOptionsVersion.update { it + 1 }
                }
        }
    }

    /** The non-deleted options of the given Tag/Status relations (same query as the GetOptions use case). */
    private fun relationOptionsFilters(relationKeys: List<Key>): List<DVFilter> = listOf(
        DVFilter(
            relation = Relations.LAYOUT,
            condition = DVFilterCondition.EQUAL,
            value = ObjectType.Layout.RELATION_OPTION.code.toDouble()
        ),
        DVFilter(
            relation = Relations.IS_DELETED,
            condition = DVFilterCondition.NOT_EQUAL,
            value = true
        ),
        DVFilter(
            relation = Relations.IS_ARCHIVED,
            condition = DVFilterCondition.NOT_EQUAL,
            value = true
        ),
        DVFilter(
            relation = Relations.RELATION_KEY,
            condition = DVFilterCondition.IN,
            value = relationKeys
        )
    )

    private fun subscribeToDataViewViewer() {
        Timber.d("subscribeToDataViewViewer, START SUBSCRIPTION by ctx:[${vmParams.ctx}]")
        viewModelScope.launch {
            combine(
                database.index,
                stateReducer.state,
                session.currentViewerId,
                permission,
                renderTrigger
            ) { dataViewState, objectState, currentViewId, permission, _ ->
                processViewState(dataViewState, objectState, currentViewId, permission)
            }
                .distinctUntilChanged()
                // Building the view state maps every row × column of the current page —
                // keep that work off the main thread; only the state assignment below
                // runs on Main.
                .flowOn(viewStateDispatcher)
                .collect { viewState ->
                Timber.d("subscribeToDataViewViewer, newViewerState:[%s]", viewState::class.simpleName)
                _currentViewer.value = viewState
                pendingScrollToObject.value?.let { objectId ->
                    pendingScrollToObject.value = null
                    dispatch(ObjectSetCommand.ScrollToObject(objectId))
                }
            }
        }
    }

    private suspend fun processViewState(
        dataViewState: DataViewState,
        objectState: ObjectState,
        currentViewId: String?,
        permission: SpaceMemberPermissions?
    ): DataViewViewState {
        return when (objectState) {
            is ObjectState.DataView.Collection -> processCollectionState(
                dataViewState = dataViewState,
                objectState = objectState,
                currentViewId = currentViewId,
                permission = permission
            )
            is ObjectState.DataView.Set -> processSetState(
                dataViewState = dataViewState,
                objectState = objectState,
                currentViewId = currentViewId,
                permission = permission
            )
            ObjectState.Init -> DataViewViewState.Init
            ObjectState.ErrorLayout -> DataViewViewState.Error(msg = "Wrong layout, couldn't open object")
            is ObjectState.DataView.TypeSet -> processTypeSetState(
                dataViewState = dataViewState,
                objectState = objectState,
                currentViewId = currentViewId,
                permission = permission
            )
        }
    }

    /**
     * A board viewer renders empty (no columns) both while its group subscription is still
     * loading and when there are genuinely no groups; only the former should show Init.
     */
    private fun isBoardAwaitingGroups(viewer: Viewer): Boolean =
        viewer is Viewer.Board && boardGroups.value == null

    /**
     * An enabled Board view with no grouping property can never build columns (it has nothing to
     * group by), so it would otherwise sit in the loading state forever. Detect it so the render
     * shows an explicit "choose a grouping property" hint instead of an endless spinner.
     */
    private fun isBoardMissingGroupRelation(viewer: DVViewer?): Boolean =
        isKanbanEnabled.value
                && viewer?.type == DVViewerType.BOARD
                && viewer.groupRelationKey.isNullOrEmpty()

    /** An active board whose group/record subscription died renders an explicit error. */
    private fun hasBoardSubscriptionFailed(viewer: DVViewer?): Boolean =
        boardSubscriptionError.value != null
                && viewer?.type == DVViewerType.BOARD
                && isKanbanEnabled.value

    private suspend fun processCollectionState(
        dataViewState: DataViewState,
        objectState: ObjectState.DataView.Collection,
        currentViewId: String?,
        permission: SpaceMemberPermissions?
    ): DataViewViewState {
        if (!objectState.isInitialized) return DataViewViewState.Init

        val dvViewer = objectState.viewerByIdOrFirst(currentViewId)

        return when (dataViewState) {
            DataViewState.Init -> {
                _dvViews.value = emptyList()
                if (dvViewer == null) {
                    DataViewViewState.Collection.NoView(
                        isCreateObjectAllowed = permission?.isOwnerOrEditor() == true,
                        isEditingViewAllowed = permission?.isOwnerOrEditor() == true
                    )
                } else {
                    DataViewViewState.Init
                }
            }
            is DataViewState.Loaded -> {
                _dvViews.value = objectState.dataViewState()?.toViewersView(
                    ctx = vmParams.ctx,
                    session = session,
                    storeOfRelations = storeOfRelations,
                    stringResourceProvider = stringResourceProvider,
                    kanbanEnabled = isKanbanEnabled.value
                ) ?: emptyList()
                val relations = objectState.dataViewContent.relationLinks.mapNotNull {
                    storeOfRelations.getByKey(it.key)
                }
                val viewer = renderViewer(objectState, dataViewState, dvViewer, relations)

                when {
                    viewer == null -> DataViewViewState.Collection.NoView(
                        isCreateObjectAllowed = permission?.isOwnerOrEditor() == true,
                        isEditingViewAllowed = permission?.isOwnerOrEditor() == true
                    )
                    hasBoardSubscriptionFailed(dvViewer) -> DataViewViewState.Error(
                        msg = BOARD_SUBSCRIPTION_ERROR_MSG
                    )
                    viewer.isEmpty() -> {
                        val missingGroupBy = isBoardMissingGroupRelation(dvViewer)
                        if (!missingGroupBy && isBoardAwaitingGroups(viewer)) return DataViewViewState.Init
                        val isCreateObjectAllowed = objectState.isCreateObjectAllowed() && permission?.isOwnerOrEditor() == true
                        DataViewViewState.Collection.NoItems(
                            title = viewer.title,
                            isCreateObjectAllowed = isCreateObjectAllowed,
                            isEditingViewAllowed = permission?.isOwnerOrEditor() == true,
                            isBoardGroupByRequired = missingGroupBy
                        )
                    }
                    else -> {
                        val isCreateObjectAllowed = objectState.isCreateObjectAllowed() && permission?.isOwnerOrEditor() == true
                        DataViewViewState.Collection.Default(
                            viewer = viewer,
                            isCreateObjectAllowed = isCreateObjectAllowed,
                            isEditingViewAllowed = permission?.isOwnerOrEditor() == true
                        )
                    }
                }
            }
        }
    }

    private suspend fun processSetState(
        dataViewState: DataViewState,
        objectState: ObjectState.DataView.Set,
        currentViewId: String?,
        permission: SpaceMemberPermissions?
    ): DataViewViewState {
        if (!objectState.isInitialized) return DataViewViewState.Init

        val setOfValue = objectState.getSetOfValue(ctx = vmParams.ctx)
        val query = objectState.filterOutDeletedAndMissingObjects(query = setOfValue)
        val viewer = objectState.viewerByIdOrFirst(currentViewId)

        return when (dataViewState) {
            DataViewState.Init -> {
                _dvViews.value = emptyList()
                when {
                    setOfValue.isEmpty() || query.isEmpty() -> DataViewViewState.Set.NoQuery(
                        isCreateObjectAllowed = permission?.isOwnerOrEditor() == true,
                        isEditingViewAllowed = permission?.isOwnerOrEditor() == true
                    )
                    viewer == null -> DataViewViewState.Set.NoView(
                        isCreateObjectAllowed = permission?.isOwnerOrEditor() == true,
                        isEditingViewAllowed = permission?.isOwnerOrEditor() == true
                    )
                    else -> DataViewViewState.Init
                }
            }
            is DataViewState.Loaded -> {
                _dvViews.value = objectState.dataViewState()?.toViewersView(
                    ctx = vmParams.ctx,
                    session = session,
                    storeOfRelations = storeOfRelations,
                    stringResourceProvider = stringResourceProvider,
                    kanbanEnabled = isKanbanEnabled.value
                ) ?: emptyList()
                val relations = objectState.dataViewContent.relationLinks.mapNotNull {
                    storeOfRelations.getByKey(it.key)
                }
                val render = viewer?.render(
                    coverImageHashProvider = coverImageHashProvider,
                    builder = urlBuilder,
                    objects = dataViewState.objects,
                    dataViewRelations = relations,
                    store = objectStore,
                    objectOrders = effectiveBoardOrders(objectState, viewer?.id),
                    storeOfRelations = storeOfRelations,
                    fieldParser = fieldParser,
                    storeOfObjectTypes = storeOfObjectTypes,
                    stringResourceProvider = stringResourceProvider,
                    boardGroupOptions = boardGroupOptions.value,
                    boardGroupOrder = groupOrderForView(objectState, viewer?.id),
                    boardGroups = boardGroups.value.orEmpty(),
                    boardRecordsByColumn = boardRecords.value.mapValues { it.value.ids },
                    boardCountsByColumn = boardRecords.value.mapValues { it.value.total },
                    kanbanEnabled = isKanbanEnabled.value
                )

                when {
                    query.isEmpty() || setOfValue.isEmpty() -> DataViewViewState.Set.NoQuery(
                        isCreateObjectAllowed = permission?.isOwnerOrEditor() == true,
                        isEditingViewAllowed = permission?.isOwnerOrEditor() == true
                    )
                    render == null -> DataViewViewState.Set.NoView(
                        isCreateObjectAllowed = permission?.isOwnerOrEditor() == true,
                        isEditingViewAllowed = permission?.isOwnerOrEditor() == true
                    )
                    hasBoardSubscriptionFailed(viewer) -> DataViewViewState.Error(
                        msg = BOARD_SUBSCRIPTION_ERROR_MSG
                    )
                    render.isEmpty() -> {
                        val missingGroupBy = isBoardMissingGroupRelation(viewer)
                        if (!missingGroupBy && isBoardAwaitingGroups(render)) return DataViewViewState.Init
                        val (defType, _) = objectState.getActiveViewTypeAndTemplate(
                            vmParams.ctx, viewer, storeOfObjectTypes
                        )
                        DataViewViewState.Set.NoItems(
                            title = render.title,
                            isCreateObjectAllowed = objectState.isCreateObjectAllowed(defType) && (permission?.isOwnerOrEditor() == true),
                            isEditingViewAllowed = permission?.isOwnerOrEditor() == true,
                            isBoardGroupByRequired = missingGroupBy
                        )
                    }
                    else -> {
                        val (defType, _) = objectState.getActiveViewTypeAndTemplate(
                            vmParams.ctx, viewer, storeOfObjectTypes
                        )
                        DataViewViewState.Set.Default(
                            viewer = render,
                            isCreateObjectAllowed = objectState.isCreateObjectAllowed(defType) && (permission?.isOwnerOrEditor() == true),
                            isEditingViewAllowed = permission?.isOwnerOrEditor() == true
                        )
                    }
                }
            }
        }
    }

    private suspend fun processTypeSetState(
        dataViewState: DataViewState,
        objectState: ObjectState.DataView.TypeSet,
        currentViewId: String?,
        permission: SpaceMemberPermissions?
    ): DataViewViewState {
        if (!objectState.isInitialized) return DataViewViewState.Init

        val setOfValue = objectState.getSetOfValue(ctx = vmParams.ctx)
        val query = objectState.filterOutDeletedAndMissingObjects(query = setOfValue)
        val viewer = objectState.viewerByIdOrFirst(currentViewId)

        return when (dataViewState) {
            DataViewState.Init -> {
                _dvViews.value = emptyList()
                when {
                    setOfValue.isEmpty() || query.isEmpty() || viewer == null ->
                        DataViewViewState.TypeSet.Error(msg = "Error while rendering viewer")
                    else -> DataViewViewState.Init
                }
            }
            is DataViewState.Loaded -> {
                _dvViews.value = objectState.dataViewState()?.toViewersView(
                    ctx = vmParams.ctx,
                    session = session,
                    storeOfRelations = storeOfRelations,
                    stringResourceProvider = stringResourceProvider,
                    kanbanEnabled = isKanbanEnabled.value
                ) ?: emptyList()
                val relations = objectState.dataViewContent.relationLinks.mapNotNull {
                    storeOfRelations.getByKey(it.key)
                }
                val render = viewer?.render(
                    coverImageHashProvider = coverImageHashProvider,
                    builder = urlBuilder,
                    objects = dataViewState.objects,
                    dataViewRelations = relations,
                    store = objectStore,
                    objectOrders = effectiveBoardOrders(objectState, viewer?.id),
                    storeOfRelations = storeOfRelations,
                    fieldParser = fieldParser,
                    storeOfObjectTypes = storeOfObjectTypes,
                    stringResourceProvider = stringResourceProvider,
                    boardGroupOptions = boardGroupOptions.value,
                    boardGroupOrder = groupOrderForView(objectState, viewer?.id),
                    boardGroups = boardGroups.value.orEmpty(),
                    boardRecordsByColumn = boardRecords.value.mapValues { it.value.ids },
                    boardCountsByColumn = boardRecords.value.mapValues { it.value.total },
                    kanbanEnabled = isKanbanEnabled.value
                )

                when {
                    render == null || query.isEmpty() || setOfValue.isEmpty() -> DataViewViewState.TypeSet.Error(
                        msg = "Error while rendering viewer",
                    )
                    hasBoardSubscriptionFailed(viewer) -> DataViewViewState.TypeSet.Error(
                        msg = BOARD_SUBSCRIPTION_ERROR_MSG
                    )
                    render.isEmpty() -> {
                        val missingGroupBy = isBoardMissingGroupRelation(viewer)
                        if (!missingGroupBy && isBoardAwaitingGroups(render)) return DataViewViewState.Init
                        val (defType, _) = objectState.getActiveViewTypeAndTemplate(
                            vmParams.ctx, viewer, storeOfObjectTypes
                        )
                        DataViewViewState.TypeSet.NoItems(
                            title = render.title,
                            isCreateObjectAllowed = objectState.isCreateObjectAllowed(defType) && (permission?.isOwnerOrEditor() == true),
                            isEditingViewAllowed = permission?.isOwnerOrEditor() == true,
                            isBoardGroupByRequired = missingGroupBy
                        )
                    }
                    else -> {
                        val (defType, _) = objectState.getActiveViewTypeAndTemplate(
                            vmParams.ctx, viewer, storeOfObjectTypes
                        )
                        DataViewViewState.TypeSet.Default(
                            viewer = render,
                            isCreateObjectAllowed = objectState.isCreateObjectAllowed(defType) && (permission?.isOwnerOrEditor() == true),
                            isEditingViewAllowed = permission?.isOwnerOrEditor() == true
                        )
                    }
                }
            }
        }
    }

    private suspend fun renderViewer(
        objectState: ObjectState.DataView.Collection,
        dataViewState: DataViewState.Loaded,
        dvViewer: DVViewer?,
        relations: List<ObjectWrapper.Relation>
    ): Viewer? {
        return dvViewer?.let {
            val objectOrderIds = objectState.getObjectOrderIds(dvViewer.id)
            it.render(
                coverImageHashProvider = coverImageHashProvider,
                builder = urlBuilder,
                objects = dataViewState.objects,
                dataViewRelations = relations,
                store = objectStore,
                objectOrderIds = objectOrderIds,
                objectOrders = effectiveBoardOrders(objectState, it.id),
                storeOfRelations = storeOfRelations,
                fieldParser = fieldParser,
                storeOfObjectTypes = storeOfObjectTypes,
                stringResourceProvider = stringResourceProvider,
                boardGroupOptions = boardGroupOptions.value,
                boardGroupOrder = groupOrderForView(objectState, it.id),
                boardGroups = boardGroups.value.orEmpty(),
                boardRecordsByColumn = boardRecords.value.mapValues { entry -> entry.value.ids },
                boardCountsByColumn = boardRecords.value.mapValues { entry -> entry.value.total },
                kanbanEnabled = isKanbanEnabled.value
            )
        }
    }

    /** The saved group (column) order for the board view [viewId], if any. */
    private fun groupOrderForView(objectState: ObjectState, viewId: Id?): GroupOrder? {
        if (viewId == null) return null
        return objectState.dataViewState()?.dataViewContent?.groupOrders?.find { it.viewId == viewId }
    }

    /** Per-group object orders for the board view [viewId] from reduced state. */
    private fun effectiveBoardOrders(objectState: ObjectState, viewId: Id?): List<ObjectOrder> {
        if (viewId == null) return emptyList()
        return objectState.dataViewState()?.dataViewContent?.objectOrders
            ?.filter { it.view == viewId }
            .orEmpty()
    }

    fun onStop() {
        Timber.d("onStop, ")
        hideTemplatesWidget()
        unsubscribeFromAllSubscriptions()
        jobs.cancel()
        // The board group/record collectors live in `jobs` (cancelled above) and are restarted
        // in onStart; clear their state so a reopen re-subscribes from scratch instead of
        // rendering stale columns.
        boardGroups.value = null
        boardGroupOptions.value = emptyMap()
        boardRecords.value = emptyMap()
        boardSubscriptionError.value = null
        dataViewOptionsVersion.value = 0
    }

    fun onCloseObject() {
        Timber.d("onCloseObject, id:[${vmParams.ctx}]")
        viewModelScope.launch {
            closeObject.async(
                CloseObject.Params(
                    target = vmParams.ctx,
                    space = vmParams.space
                )
            ).fold(
                onSuccess = {
                    Timber.d("Object [${vmParams.ctx}] closed successfully")
                },
                onFailure = {
                    Timber.w(it, "Error while closing object set: ${vmParams.ctx}")
                }
            )
        }
    }

    fun onSystemBackPressed() {
        Timber.d("onSystemBackPressed, ")
        proceedWithClosingAndExit()
    }

    private fun unsubscribeFromAllSubscriptions() {
        unsubscribeJob = viewModelScope.launch {
            val ids = listOf(
                getDataViewSubscriptionId(vmParams.ctx),
                HOME_SCREEN_PROFILE_OBJECT_SUBSCRIPTION,
                "${vmParams.ctx}$SUBSCRIPTION_TEMPLATES_ID"
            )
            dataViewSubscription.unsubscribe(ids)
            boardGroupSubscriptionContainer.unsubscribe(
                vmParams.ctx + BoardGroupSubscriptionContainer.SUBSCRIPTION_POSTFIX
            )
            storelessSubscriptionContainer.unsubscribe(listOf(boardOptionsSubscriptionId))
            storelessSubscriptionContainer.unsubscribe(listOf(dataViewOptionsSubscriptionId))
            // Evict Tag/Status option objects this VM merged into the shared store; options still
            // referenced by the record subscription survive (unsubscribe only drops this sub id).
            objectStore.unsubscribe(listOf(dataViewOptionsSubscriptionId))
            if (boardRecordSubscriptionIds.isNotEmpty()) {
                boardRecordsSubscriptionContainer.unsubscribe(boardRecordSubscriptionIds)
                boardRecordSubscriptionIds = emptyList()
            }
        }
    }

    private fun proceedWithClosingAndExit() {
        viewModelScope.launch {
            closeObject.async(
                CloseObject.Params(
                    target = vmParams.ctx,
                    space = vmParams.space
                )
            ).fold(
                onSuccess = { dispatch(AppNavigation.Command.Exit(vmParams.space.id)) },
                onFailure = {
                    Timber.e(it, "Error while closing object set: ${vmParams.ctx}").also {
                        dispatch(AppNavigation.Command.Exit(vmParams.space.id))
                    }
                }
            )
        }
    }

    fun onTitleChanged(txt: String) {
        Timber.d("onTitleChanged, txt:[$txt]")

        val target = (header.value as? SetOrCollectionHeaderState.Default)?.title?.id
        if (target != null) {
            viewModelScope.launch {
                titleUpdateChannel.send(
                    TextUpdate.Default(
                        text = txt,
                        target = target,
                        markup = emptyList()
                    )
                )
            }
        } else {
            viewModelScope.launch {
                setObjectDetails(
                    UpdateDetail.Params(
                        target = vmParams.ctx,
                        key = Relations.NAME,
                        value = txt
                    )
                ).process(
                    success = { dispatcher.send(it) },
                    failure = {
                        Timber.e(it, "Error while updating object set name")
                    }
                )
            }
        }
    }

    fun onTitleFocusChanged(hasFocus: Boolean) {
        isTitleToolbarVisible.value = hasFocus
    }

    fun hideTitleToolbar() {
        isTitleToolbarVisible.value = false
    }

    fun onDescriptionChanged(text: String) {
        viewModelScope.launch {
            setObjectDetails(
                UpdateDetail.Params(
                    target = vmParams.ctx,
                    key = Relations.DESCRIPTION,
                    value = text
                )
            ).process(
                failure = {
                    Timber.e(it, "Error while updating description")
                },
                success = defaultPayloadConsumer
            )
        }
    }

    fun onGridCellClicked(cell: CellView) {
        Timber.d("onGridCellClicked, cell:[$cell]")
        if (cell.relationKey == Relations.NAME) return
        val state = stateReducer.state.value.dataViewState() ?: return
        viewModelScope.launch {
            val viewer = state.viewerByIdOrFirst(session.currentViewerId.value)
            val relation = storeOfRelations.getByKey(cell.relationKey)
            if (relation == null) {
                toast("Could not found this relation. Please, try again later.")
                Timber.e("onGridCellClicked, Relation [${cell.relationKey}] is empty")
                return@launch
            }
            if (viewer == null) {
                Timber.e("onGridCellClicked, Viewer is empty")
                return@launch
            }
            when (cell) {
                is CellView.Description,
                is CellView.Number,
                is CellView.Email,
                is CellView.Url,
                is CellView.Phone -> {
                    dispatch(
                        ObjectSetCommand.Modal.EditGridTextCell(
                            ctx = vmParams.ctx,
                            relationKey = cell.relationKey,
                            recordId = cell.id,
                            space = vmParams.space.id
                        )
                    )
                }
                is CellView.Date -> {
                    if (relation.isReadonlyValue)  {
                        val timeInMillis = cell.relativeDate?.initialTimeInMillis
                        handleReadOnlyValue(timeInMillis)
                    } else {
                        dispatch(
                            ObjectSetCommand.Modal.EditGridDateCell(
                                ctx = vmParams.ctx,
                                objectId = cell.id,
                                relationKey = cell.relationKey,
                                space = vmParams.space.id
                            )
                        )
                    }
                }
                is CellView.Tag, is CellView.Status -> {
                    dispatch(
                        ObjectSetCommand.Modal.EditTagOrStatusCell(
                            ctx = vmParams.ctx,
                            target = cell.id,
                            relationKey = cell.relationKey,
                            space = vmParams.space.id
                        )
                    )
                }
                is CellView.Object, is CellView.File -> {
                    if (cell.relationKey != Relations.TYPE) {
                        dispatch(
                            ObjectSetCommand.Modal.EditObjectCell(
                                ctx = vmParams.ctx,
                                target = cell.id,
                                relationKey = cell.relationKey,
                                space = vmParams.space.id
                            )
                        )
                    } else {
                        toast("You cannot change type from here.")
                    }
                }
                is CellView.Checkbox -> {
                    if (relation.isReadonlyValue) {
                        Timber.d("onGridCellClicked, relation is ReadOnly")
                        toast(NOT_ALLOWED_CELL)
                        return@launch
                    }
                    setObjectDetails(
                        UpdateDetail.Params(
                            target = cell.id,
                            key = cell.relationKey,
                            value = !cell.isChecked
                        )
                    ).process(
                        failure = { Timber.e(it, "Error while updating data view record") },
                        success = { Timber.d("Data view record updated successfully") }
                    )
                }
            }
        }
    }

    /**
     * @param [target] object is a record contained in this set.
     */
    fun onObjectHeaderClicked(target: Id) {
        Timber.d("onObjectHeaderClicked, id:[$target]")
        stateReducer.state.value.dataViewState() ?: return
        viewModelScope.launch {
            val obj = objectStore.get(target)
            if (obj != null) {
                proceedWithNavigation(
                    obj = obj,
                    identityProfileLink = obj.getSingleValue(Relations.IDENTITY_PROFILE_LINK)
                )
            } else {
                toast("Record not found. Please, try again later.")
            }
        }
    }

    fun onTaskCheckboxClicked(target: Id) {
        Timber.d("onTaskCheckboxClicked: $target")
        stateReducer.state.value.dataViewState() ?: return
        viewModelScope.launch {
            val obj = database.store.get(target)
            if (obj != null) {
                setObjectDetails(
                    UpdateDetail.Params(
                        target = target,
                        key = Relations.DONE,
                        value = !(obj.done ?: false)
                    )
                ).process(
                    failure = {
                        Timber.e(it, "Error while updating checkbox")
                    },
                    success = {
                        Timber.d("Checkbox successfully updated for record: $target")
                    }
                )
            } else {
                toast("Object not found")
            }
        }
    }

    /**
     * Called when user long-clicks on a row header in a view (e.g., grid, gallery, or list view)
     * Shows a context menu with the following options:
     * - "Open as Object"
     * - "Copy Link"
     * - "Move to Bin" (if allowed)
     * - "Unlink from Collection" (if allowed)
     */
    fun onObjectHeaderLongClicked(objectId: Id) {
        Timber.d("onObjectHeaderLongClicked, id:[$objectId]")
        viewModelScope.launch {

            // Check object DELETE restriction
            val obj = objectStore.get(objectId)
            val hasDeleteRestriction = obj?.restrictions?.contains(ObjectRestriction.DELETE) == true

            // Can move to bin only if: user is owner/editor AND object allows delete
            val canMoveToBin = isOwnerOrEditor && !hasDeleteRestriction

            // Check if current state is a Collection AND user has edit permission
            val isCollection = stateReducer.state.value is ObjectState.DataView.Collection
            val canRemoveFromCollection = isCollection && isOwnerOrEditor

            dispatch(
                ObjectSetCommand.Modal.ShowObjectHeaderContextMenu(
                    objectId = objectId,
                    canMoveToBin = canMoveToBin,
                    isCollection = canRemoveFromCollection,
                    layout = obj?.layout
                )
            )
        }
    }

    /**
     * Opens the object as an Anytype object (not as a URL for bookmarks).
     * This bypasses the default bookmark behavior of opening URLs in browser.
     */
    fun onOpenAsObject(targetId: Id) {
        Timber.d("onOpenAsObject, id:[$targetId]")
        viewModelScope.launch {
            val obj = objectStore.get(targetId)
            if (obj != null) {
                navigateByLayout(
                    target = targetId,
                    space = vmParams.space.id,
                    layout = obj.layout
                )
            } else {
                toast("Object not found. Please, try again later.")
            }
        }
    }

    /**
     * Copies the object's deeplink to clipboard.
     */
    fun onCopyLink(targetId: Id) {
        Timber.d("onCopyLink, id:[$targetId]")
        viewModelScope.launch {
            val link = deepLinkResolver.createObjectDeepLink(
                obj = targetId,
                space = vmParams.space
            )
            dispatch(ObjectSetCommand.CopyLinkToClipboard(link = link))
        }
    }

    /**
     * Moves the object to bin (archives it).
     * Only Owner or Editor can perform this action, and the object must not have DELETE restriction.
     */
    fun onMoveToBin(targetId: Id) {
        Timber.d("onMoveToBin, id:[$targetId]")
        viewModelScope.launch {
            // Defensive permission check
            if (!isOwnerOrEditor) {
                toast(NOT_ALLOWED)
                return@launch
            }

            // Defensive restriction check
            val obj = objectStore.get(targetId)
            if (obj?.restrictions?.contains(ObjectRestriction.DELETE) == true) {
                toast(NOT_ALLOWED)
                return@launch
            }

            val params = SetObjectListIsArchived.Params(
                targets = listOf(targetId),
                isArchived = true
            )
            setObjectListIsArchived.async(params).fold(
                onSuccess = {
                    Timber.d("Successfully moved object to bin: $targetId")
                },
                onFailure = { e ->
                    Timber.e(e, "Error while moving object to bin")
                    toast("Error while moving to bin. Please try again.")
                }
            )
        }
    }

    /**
     * Removes the object from the current collection (does not delete the object).
     * Only available when viewing a Collection.
     */
    fun onRemoveFromCollection(targetId: Id) {
        Timber.d("onRemoveFromCollection, id:[$targetId]")
        viewModelScope.launch {
            val params = RemoveObjectFromCollection.Params(
                collectionId = vmParams.ctx,
                objectIdsToRemove = listOf(targetId)
            )
            removeObjectFromCollection.async(params).fold(
                onSuccess = {
                    Timber.d("Successfully removed object from collection: $targetId")
                },
                onFailure = { e ->
                    Timber.e(e, "Error removing object from collection")
                    toast("Error removing from collection. Please try again.")
                }
            )
        }
    }

    /**
     * Opens the bookmark's source URL in the browser.
     */
    fun onOpenBookmarkInBrowser(targetId: Id) {
        Timber.d("onOpenBookmarkInBrowser, id:[$targetId]")
        viewModelScope.launch {
            val obj = objectStore.get(targetId)
            if (obj != null) {
                val url = obj.getSingleValue<String>(Relations.SOURCE)
                if (!url.isNullOrBlank()) {
                    dispatch(ObjectSetCommand.Browse(url))
                } else {
                    toast("Bookmark URL not found.")
                }
            } else {
                toast("Object not found. Please try again later.")
            }
        }
    }

    /**
     * Opens the file object using the appropriate handler (media player for video/audio,
     * browser for other file types).
     */
    fun onOpenFile(targetId: Id) {
        Timber.d("onOpenFile, id:[$targetId]")
        viewModelScope.launch {
            val obj = objectStore.get(targetId)
            if (obj == null) {
                toast("Object not found. Please try again later.")
                return@launch
            }
            val layout = obj.layout
            val name = fieldParser.getObjectName(obj)
            when (layout) {
                ObjectType.Layout.IMAGE -> dispatch(
                    ObjectSetCommand.PlayMedia(
                        targetObjectId = targetId,
                        name = name,
                        layout = ObjectType.Layout.IMAGE
                    )
                )
                ObjectType.Layout.VIDEO -> dispatch(
                    ObjectSetCommand.PlayMedia(
                        targetObjectId = targetId,
                        name = name,
                        layout = ObjectType.Layout.VIDEO
                    )
                )
                ObjectType.Layout.AUDIO -> dispatch(
                    ObjectSetCommand.PlayMedia(
                        targetObjectId = targetId,
                        name = name,
                        layout = ObjectType.Layout.AUDIO
                    )
                )
                else -> {
                    if (layout != null) {
                        val url = urlBuilder.getUrlBasedOnFileLayout(targetId, layout)
                        if (url != null) {
                            dispatch(ObjectSetCommand.Browse(url))
                        } else {
                            toast("Cannot open file")
                        }
                    } else {
                        toast("Cannot open file")
                    }
                }
            }
        }
    }

    fun onRelationTextValueChanged(
        value: Any?,
        objectId: Id,
        relationKey: Id
    ) {
        Timber.d("onRelationTextValueChanged, objectId:[$objectId], relationKey:[$relationKey], value:[$value]")
        stateReducer.state.value.dataViewState() ?: return
        viewModelScope.launch {
            setObjectDetails(
                UpdateDetail.Params(
                    target = objectId,
                    key = relationKey,
                    value = value
                )
            ).process(
                failure = { Timber.e(it, "Error while updating data view record") },
                success = {
                    dispatcher.send(it)
                    Timber.d("Relation text value updated successfully")
                }
            )
        }
    }

    /**
     * Moves a Kanban card from [sourceColumnId] to [targetColumnId] by writing the
     * dragged object's group relation. The write is format-aware and non-lossy: Status →
     * a single option, Checkbox → the boolean, and Tag → a read-modify-write that removes
     * only the source column's option(s) and adds the target's, preserving the card's
     * other tags. The "No value" column clears the relation (or, for Tag, drops only the
     * source tags). See [computeBoardCardMove].
     */
    fun onBoardCardDropped(
        cardId: Id,
        sourceColumnId: String,
        targetColumnId: String,
        targetOrderedIds: List<Id>? = null
    ) {
        Timber.d("onBoardCardDropped, cardId:[$cardId], source:[$sourceColumnId], target:[$targetColumnId]")
        if (!isOwnerOrEditor) {
            toast(NOT_ALLOWED)
            return
        }
        val state = stateReducer.state.value.dataViewState() ?: return
        val viewer = state.viewerByIdOrFirst(session.currentViewerId.value) ?: return
        val groupRelationKey = viewer.groupRelationKey
        if (groupRelationKey.isNullOrEmpty()) {
            Timber.e("onBoardCardDropped: active viewer has no group relation key")
            return
        }
        val groups = boardGroups.value.orEmpty()
        if (groups.isEmpty()) {
            // Columns aren't backed by the authoritative backend group set yet (client-side
            // fallback render). A column id may be an option id there but a group hash once
            // loaded, so refuse the write rather than risk persisting a hash as an option id.
            Timber.w("onBoardCardDropped: board groups not loaded; ignoring move")
            return
        }
        viewModelScope.launch {
            val move = computeBoardCardMove(
                format = storeOfRelations.getByKey(groupRelationKey)?.format,
                currentValue = currentGroupValueIds(cardId, groupRelationKey),
                sourceColumnId = sourceColumnId,
                sourceGroup = groups.firstOrNull { it.id == sourceColumnId }?.value,
                targetColumnId = targetColumnId,
                targetGroup = groups.firstOrNull { it.id == targetColumnId }?.value,
                groupsLoaded = true
            )
            if (move !is BoardCardMove.Write) return@launch
            val value = move.value
            val cleared = value == null || (value is List<*> && value.isEmpty())
            setObjectDetails(
                UpdateDetail.Params(target = cardId, key = groupRelationKey, value = value)
            ).process(
                failure = { Timber.e(it, "Error while moving board card to another column") },
                success = { payload ->
                    dispatcher.send(payload)
                    analytics.sendAnalyticsRelationEvent(
                        eventName = if (cleared) {
                            EventsDictionary.relationDeleteValue
                        } else {
                            EventsDictionary.relationChangeValue
                        },
                        relationKey = groupRelationKey,
                        storeOfRelations = storeOfRelations,
                        spaceParams = provideParams(vmParams.space.id)
                    )
                    // Persist the drop position in the target column so the card lands where it
                    // was dropped (only when the UI provided the full target order).
                    if (targetOrderedIds != null) {
                        persistBoardColumnOrder(viewer.id, state.dataViewBlock.id, targetColumnId, targetOrderedIds)
                    }
                }
            )
        }
    }

    /**
     * Creates a new object pre-set to the group value of the Kanban column [columnId], routing
     * through the existing create-object flow (so the post-create "name your object" sheet is
     * reused). The column's group value is derived with [computeBoardCardMove] — the same
     * format-aware logic as a card drop — and merged into the new object's prefilled details.
     * The "No value" column (and any column whose value can't be resolved) creates an object
     * with no group value.
     */
    fun onBoardCreateObjectInColumn(columnId: String) {
        Timber.d("onBoardCreateObjectInColumn, columnId:[$columnId]")
        if (!isOwnerOrEditor) {
            toast(NOT_ALLOWED)
            return
        }
        // Coordinate with the main "+ New" path (proceedWithDataViewObjectCreate): both entry
        // points share this flag so two fast taps can't spawn two objects.
        if (!isObjectCreationInProgress.compareAndSet(false, true)) {
            Timber.d("onBoardCreateObjectInColumn: creation already in progress, skipping")
            return
        }
        val state = stateReducer.state.value.dataViewState()
        if (state == null) {
            isObjectCreationInProgress.set(false)
            return
        }
        val viewer = state.viewerByIdOrFirst(session.currentViewerId.value)
        if (viewer == null) {
            isObjectCreationInProgress.set(false)
            return
        }
        val groupRelationKey = viewer.groupRelationKey
        val groups = boardGroups.value.orEmpty()
        viewModelScope.launch {
            try {
                val extraPrefilled: Map<Key, Any?> = if (!groupRelationKey.isNullOrEmpty() && groups.isNotEmpty()) {
                    val move = computeBoardCardMove(
                        format = storeOfRelations.getByKey(groupRelationKey)?.format,
                        currentValue = emptyList(),
                        sourceColumnId = BOARD_EMPTY_GROUP_ID,
                        sourceGroup = null,
                        targetColumnId = columnId,
                        targetGroup = groups.firstOrNull { it.id == columnId }?.value,
                        groupsLoaded = true
                    )
                    val value = (move as? BoardCardMove.Write)?.value
                        ?.takeUnless { it == null || (it is List<*> && it.isEmpty()) }
                    if (value != null) mapOf(groupRelationKey to value) else emptyMap()
                } else {
                    // Groups not loaded or no group relation: create without a group value (lands in "No value").
                    emptyMap()
                }
                when (state) {
                    is ObjectState.DataView.Collection ->
                        proceedWithAddingObjectToCollection(extraPrefilled = extraPrefilled)
                    is ObjectState.DataView.TypeSet ->
                        proceedWithCreatingObjectTypeSetObject(
                            currentState = state,
                            templateChosenBy = null,
                            extraPrefilled = extraPrefilled
                        )
                    is ObjectState.DataView.Set ->
                        proceedWithCreatingSetObject(
                            currentState = state,
                            templateChosenBy = null,
                            extraPrefilled = extraPrefilled
                        )
                }
            } finally {
                isObjectCreationInProgress.set(false)
            }
        }
    }

    /**
     * Reads the card's current value for the board's group relation as a list of option
     * ids, normalising the single-value / list / absent cases.
     */
    private suspend fun currentGroupValueIds(cardId: Id, groupRelationKey: Key): List<Id> {
        return when (val raw = objectStore.get(cardId)?.map?.get(groupRelationKey)) {
            is Id -> if (raw.isNotEmpty()) listOf(raw) else emptyList()
            is List<*> -> raw.typeOf()
            else -> emptyList()
        }
    }

    /**
     * Persists a new order of cards within a Kanban column (group). The new order is sent
     * to the backend; its response payload carries the object-order-update event, which is
     * reduced into state, so the board re-renders with the new order.
     */
    fun onBoardCardReordered(columnId: String, orderedCardIds: List<Id>) {
        Timber.d("onBoardCardReordered, columnId:[$columnId], ids:[$orderedCardIds]")
        if (!isOwnerOrEditor) {
            toast(NOT_ALLOWED)
            return
        }
        if (boardGroups.value.isNullOrEmpty()) {
            // Mirror onBoardCardDropped: only persist order keyed by the authoritative
            // backend group id, never the client-fallback column (option) id.
            Timber.w("onBoardCardReordered: board groups not loaded; ignoring reorder")
            return
        }
        val state = stateReducer.state.value.dataViewState() ?: return
        val viewer = state.viewerByIdOrFirst(session.currentViewerId.value) ?: return
        viewModelScope.launch {
            persistBoardColumnOrder(viewer.id, state.dataViewBlock.id, columnId, orderedCardIds)
        }
    }

    /**
     * Persists [orderedCardIds] as the order of a Kanban column (group). The response payload
     * carries the object-order-update event, which is reduced into state, so the board
     * re-renders with the new order.
     */
    private suspend fun persistBoardColumnOrder(
        viewId: Id,
        dv: Id,
        columnId: String,
        orderedCardIds: List<Id>
    ) {
        setDataViewObjectOrder.async(
            SetDataViewObjectOrder.Params(
                ctx = vmParams.ctx,
                dv = dv,
                objectOrders = listOf(ObjectOrder(view = viewId, group = columnId, ids = orderedCardIds))
            )
        ).fold(
            onSuccess = { payload -> dispatcher.send(payload) },
            onFailure = { Timber.e(it, "Error while persisting board card order") }
        )
    }

    /**
     * Loads the next page of cards for a Kanban column when it is scrolled to its end —
     * grows just that column's record subscription limit (other columns are untouched).
     */
    fun onBoardColumnLoadMore(columnId: String) {
        Timber.d("onBoardColumnLoadMore, columnId:[$columnId]")
        boardRecordsSubscriptionContainer.loadMore(columnId, additional = DEFAULT_LIMIT)
    }

    fun onNewButtonIconClicked() {
        Timber.d("onNewButtonIconClicked, ")
        showTypeTemplatesWidgetForObjectCreation()
    }

    // TODO Multispaces refactor this method
    private suspend fun proceedWithCreatingSetObject(
        currentState: ObjectState.DataView,
        templateChosenBy: Id?,
        extraPrefilled: Map<Key, Any?> = emptyMap()
    ) {
        if (isRestrictionPresent(DataViewRestriction.CREATE_OBJECT)) {
            toast(NOT_ALLOWED)
        } else {
            val setObject = currentState.details.getObject(vmParams.ctx)
            val viewer = currentState.viewerByIdOrFirst(session.currentViewerId.value)
            if (viewer == null) {
                Timber.e("onCreateNewDataViewObject, Viewer is empty")
                return
            }

            // Get dataview block ID for cleanup
            val dvBlock = currentState.dataViewBlock.id

            val (resolvedType, defaultTemplate) = currentState.getActiveViewTypeAndTemplate(
                ctx = vmParams.ctx,
                activeView = viewer,
                storeOfObjectTypes = storeOfObjectTypes,
                onDeletedTypeDetected = { deletedViewer ->
                    // Cleanup deleted type in viewer (fire-and-forget)
                    cleanupDeletedTypeInViewer(
                        ctx = vmParams.ctx,
                        dv = dvBlock,
                        viewer = deletedViewer
                    )
                }
            )

            // Resolve type and template with fallback handling
            val (defaultObjectType, resolvedTemplate) = resolveTypeAndTemplateWithFallback(
                resolvedType = resolvedType,
                defaultTemplate = defaultTemplate,
                userChosenTemplate = templateChosenBy
            )

            if (defaultObjectType == null) {
                toast("Unable to create object: default type not found. Please try again or contact support.")
                Timber.e("Both resolved type and space default type are null")
                return
            }

            val objectTypeUniqueKey = defaultObjectType.uniqueKey

            val sourceId = setObject?.setOf?.singleOrNull()
            if (sourceId == null) {
                toast("Unable to define a source for a new object.")
            } else {
                val wrapper = currentState.details.getObject(sourceId)
                if (wrapper != null) {
                    when (wrapper.layout) {
                        ObjectType.Layout.OBJECT_TYPE -> {
                            val uniqueKey = wrapper.getValue<Key>(Relations.UNIQUE_KEY)
                            if (uniqueKey == null) {
                                toast("Could not found key for given type")
                                return
                            }
                            if (uniqueKey == ObjectTypeIds.BOOKMARK) {
                                dispatch(
                                    ObjectSetCommand.Modal
                                        .CreateBookmark(
                                            ctx = vmParams.ctx,
                                            space = requireNotNull(wrapper.spaceId)
                                        )
                                )
                            } else {
                                val validTemplateId = resolvedTemplate
                                val prefilled = viewer.prefillNewObjectDetails(
                                    storeOfRelations = storeOfRelations,
                                    dateProvider = dateProvider
                                ) + extraPrefilled
                                proceedWithCreatingDataViewObject(
                                    CreateDataViewObject.Params.SetByType(
                                        type = TypeKey(uniqueKey),
                                        filters = viewer.filters,
                                        template = validTemplateId,
                                        prefilled = prefilled
                                    )
                                )
                            }
                        }
                        ObjectType.Layout.RELATION -> {
                            if (objectTypeUniqueKey == ObjectTypeIds.BOOKMARK) {
                                dispatch(
                                    ObjectSetCommand.Modal.CreateBookmark(
                                        ctx = vmParams.ctx,
                                        space = requireNotNull(wrapper.spaceId)
                                    )
                                )
                            } else {
                                val validTemplateId = resolvedTemplate
                                val prefilled = viewer.resolveSetByRelationPrefilledObjectData(
                                    storeOfRelations = storeOfRelations,
                                    dateProvider = dateProvider,
                                    objSetByRelation = ObjectWrapper.Relation(wrapper.map)
                                ) + extraPrefilled
                                proceedWithCreatingDataViewObject(
                                    CreateDataViewObject.Params.SetByRelation(
                                        filters = viewer.filters,
                                        template = validTemplateId,
                                        type = TypeKey(objectTypeUniqueKey),
                                        prefilled = prefilled
                                    )
                                )
                            }
                        }
                        else -> toast("Unable to define a source for a new object.")
                    }
                } else {
                    toast("Unable to define a source for a new object.")
                }
            }
        }
    }

    private suspend fun proceedWithCreatingObjectTypeSetObject(
        currentState: ObjectState.DataView.TypeSet,
        templateChosenBy: String?,
        extraPrefilled: Map<Key, Any?> = emptyMap()
    ) {
        val objectType = storeOfObjectTypes.get(vmParams.ctx)

        val objectTypeUniqueKey = objectType?.uniqueKey ?: return

        if (objectTypeUniqueKey == ObjectTypeIds.BOOKMARK) {
            dispatch(
                ObjectSetCommand.Modal
                    .CreateBookmark(
                        ctx = vmParams.ctx,
                        space = vmParams.space.id
                    )
            )
        } else {
            val viewer = currentState.viewerByIdOrFirst(session.currentViewerId.value) ?: return
            val prefilled = viewer.prefillNewObjectDetails(
                storeOfRelations = storeOfRelations,
                dateProvider = dateProvider
            ) + extraPrefilled
            proceedWithCreatingDataViewObject(
                CreateDataViewObject.Params.SetByType(
                    type = TypeKey(objectTypeUniqueKey),
                    filters = viewer.filters,
                    template = templateChosenBy ?: objectType.defaultTemplateId,
                    prefilled = prefilled
                )
            )
        }
    }

    fun onSelectQueryButtonClicked() {
        dispatch(ObjectSetCommand.Modal.OpenEmptyDataViewSelectQueryScreen)
    }

    private suspend fun proceedWithAddingObjectToCollection(
        typeChosenByUser: TypeKey? = null,
        templateChosenBy: Id? = null,
        extraPrefilled: Map<Key, Any?> = emptyMap()
    ) {
        val state = stateReducer.state.value.dataViewState() ?: return
        val viewer = state.viewerByIdOrFirst(session.currentViewerId.value) ?: return

        // Get dataview block ID for cleanup
        val dvBlock = state.dataViewBlock.id

        val (resolvedType, defaultTemplate) = state.getActiveViewTypeAndTemplate(
            ctx = vmParams.ctx,
            activeView = viewer,
            storeOfObjectTypes = storeOfObjectTypes,
            onDeletedTypeDetected = { deletedViewer ->
                // Cleanup deleted type in viewer (fire-and-forget)
                cleanupDeletedTypeInViewer(
                    ctx = vmParams.ctx,
                    dv = dvBlock,
                    viewer = deletedViewer
                )
            }
        )

        // Resolve type and template with fallback handling
        val (defaultObjectType, resolvedTemplate) = resolveTypeAndTemplateWithFallback(
            resolvedType = resolvedType,
            defaultTemplate = defaultTemplate,
            userChosenTemplate = templateChosenBy
        )

        val defaultObjectTypeUniqueKey = defaultObjectType?.uniqueKey?.let {
            TypeKey(it)
        }

        if (typeChosenByUser == null && defaultObjectTypeUniqueKey == null) {
            toast("Could not define type for new object")
            return
        }

        val validTemplateId = resolvedTemplate
        val prefilled = viewer.prefillNewObjectDetails(
            storeOfRelations = storeOfRelations,
            dateProvider = dateProvider
        ) + extraPrefilled
        val type = typeChosenByUser ?: defaultObjectTypeUniqueKey!!
        val createObjectParams = CreateDataViewObject.Params.Collection(
            template = validTemplateId,
            type = type,
            filters = viewer.filters,
            prefilled = prefilled,
            createdInContext = vmParams.ctx
        )
        if (type.key == ObjectTypeIds.BOOKMARK) {
            dispatch(
                ObjectSetCommand.Modal.CreateBookmark(
                    ctx = vmParams.ctx,
                    space = vmParams.space.id
                )
            )
        } else {
            proceedWithCreatingDataViewObject(createObjectParams) { result ->
                val params = AddObjectToCollection.Params(
                    ctx = vmParams.ctx,
                    after = "",
                    targets = listOf(result.objectId)
                )
                viewModelScope.launch {
                    addObjectToCollection.async(params).fold(
                        onSuccess = { payload -> dispatcher.send(payload) },
                        onFailure = { Timber.e(it, "Error while adding object to collection") }
                    )
                }
            }
        }
    }

    private suspend fun proceedWithCreatingDataViewObject(
        params: CreateDataViewObject.Params,
        action: ((CreateDataViewObject.Result) -> Unit)? = null
    ) {
        val startTime = System.currentTimeMillis()
        createDataViewObject.async(params).fold(
            onFailure = { Timber.e(it, "Error while creating new record") },
            onSuccess = { result ->
                action?.invoke(result)
                pendingScrollToObject.value = result.objectId
                proceedWithNewDataViewObject(result)
                sendAnalyticsObjectCreateEvent(
                    startTime = startTime,
                    typeKey = result.objectType.key,
                )
            }
        )
    }

    private suspend fun proceedWithNewDataViewObject(
        response: CreateDataViewObject.Result,
    ) {
        val obj = ObjectWrapper.Basic(response.struct.orEmpty())
        when (obj.layout) {
            ObjectType.Layout.CHAT_DERIVED -> {
                proceedWithOpeningChat(
                    target = obj.id,
                    space = vmParams.space.id
                )
            }
            ObjectType.Layout.NOTE -> {
                proceedWithCreatingNoteObject(obj = obj)
            }
            else -> {
                val isIconChangeAllowed = obj.layout in layoutsSupportsEmojiAndImages
                val icon = obj.objectIcon(
                    builder = urlBuilder,
                    objType = storeOfObjectTypes.getTypeOfObject(obj)
                )
                showSetObjectNameSheet(
                    objectId = response.objectId,
                    icon = icon,
                    isIconChangeAllowed = isIconChangeAllowed,
                    name = obj.name.orEmpty()
                )
            }
        }
    }

    fun onViewerCustomizeButtonClicked() {
        Timber.d("onViewerCustomizeButtonClicked, ")
        val dataView = stateReducer.state.value.dataViewState() ?: return
        val activeViewer = dataView.viewerByIdOrFirst(session.currentViewerId.value) ?: return
        widgetViewerId.value = activeViewer.id
        showViewerEditWidget()
    }

    fun onHideViewerCustomizeSwiped() {
        Timber.d("onHideViewerCustomizeSwiped, ")
        isCustomizeViewPanelVisible.value = false
    }

    fun onExpandViewerMenuClicked() {
        Timber.d("onExpandViewerMenuClicked, ")
        val state = stateReducer.state.value.dataViewState() ?: return
        if (isRestrictionPresent(DataViewRestriction.VIEWS)
        ) {
            toast(NOT_ALLOWED)
        } else {
            viewersWidgetState.value = viewersWidgetState.value.copy(showWidget = true)
        }
    }

    fun onViewerEditClicked() {
        Timber.d("onViewerEditClicked, ")
        val state = stateReducer.state.value.dataViewState() ?: return
        val viewer = state.viewerByIdOrFirst(session.currentViewerId.value) ?: return
    }

    fun onMenuClicked() {
        Timber.d("onMenuClicked, ")
        val state = stateReducer.state.value.dataViewState() ?: return
        val wrapper = state.details.getObject(vmParams.ctx) ?: return
        Timber.d("Wrapper: $wrapper")
        val space = wrapper.spaceId
        if (space != null) {
            dispatch(
                ObjectSetCommand.Modal.Menu(
                    ctx = vmParams.ctx,
                    space = space,
                    isArchived = wrapper.isArchived == true,
                    isFavorite = wrapper.isFavorite == true,
                    isReadOnly = !isOwnerOrEditor
                )
            )
        } else {
            Timber.e("Space not found").also {
                toast("Space not found")
            }
        }
    }

    fun onObjectIconClicked() {
        Timber.d("onIconClicked, ")
        val state = stateReducer.state.value.dataViewState() ?: return
        val wrapper = state.details.getObject(vmParams.ctx)
        val space = wrapper?.spaceId
        if (space != null) {
            dispatch(
                ObjectSetCommand.Modal.OpenIconActionMenu(
                    target = vmParams.ctx,
                    space = space
                )
            )
        } else {
            Timber.e("Space not found").also {
                toast("Space not found")
            }
        }
    }

    fun onCoverClicked() {
        Timber.d("onCoverClicked, ")
        dispatch(
            ObjectSetCommand.Modal.OpenCoverActionMenu(
                ctx = vmParams.ctx,
                space = vmParams.space.id
            )
        )
    }

    fun onViewerSettingsClicked(viewer: Id) {
        Timber.d("onViewerSettingsClicked, viewer: [$viewer]")
        if (isRestrictionPresent(DataViewRestriction.RELATION)) {
            toast(NOT_ALLOWED)
        } else {
            val state = stateReducer.state.value.dataViewState() ?: return
            val dataViewBlock = state.dataViewBlock
            dispatch(
                ObjectSetCommand.Modal.OpenSettings(
                    ctx = vmParams.ctx,
                    dv = dataViewBlock.id,
                    viewer = viewer
                )
            )
        }
    }

    fun onViewerFiltersClicked() {
        Timber.d("onViewerFiltersClicked, ")
        openViewerFilters()
    }

    fun onViewerSortsClicked() {
        Timber.d("onViewerSortsClicked, ")
        openViewerSorts()
    }

    private fun dispatch(command: ObjectSetCommand) {
        viewModelScope.launch { _commands.emit(command) }
    }

    private fun dispatch(command: AppNavigation.Command) {
        navigate(EventWrapper(command))
    }

    private fun toast(toast: String) {
        viewModelScope.launch { toasts.emit(toast) }
    }

    private fun isRestrictionPresent(restriction: DataViewRestriction): Boolean {
        val state = stateReducer.state.value.dataViewState() ?: return false
        val block = state.dataViewBlock
        val dVRestrictions = state.dataViewRestrictions.firstOrNull { it.block == block.id }
        return dVRestrictions != null && dVRestrictions.restrictions.any { it == restriction }
    }

    //region { PAGINATION LOGIC }

    fun onPaginatorToolbarNumberClicked(number: Int, isSelected: Boolean) {
        Timber.d("onPaginatorToolbarNumberClicked, number:[$number], isSelected:[$isSelected]")
        if (isSelected) {
            Timber.d("This page is already selected")
        } else {
            viewModelScope.launch {
                paginator.offset.value = number.toLong() * DEFAULT_LIMIT
            }
        }
    }

    fun onPaginatorNextElsePrevious(next: Boolean) {
        Timber.d("onPaginatorNextElsePrevious, next:[$next]")
        viewModelScope.launch {
            paginator.offset.value = if (next) {
                paginator.offset.value + DEFAULT_LIMIT
            } else {
                paginator.offset.value - DEFAULT_LIMIT
            }
        }
    }

    //endregion

    //region NAVIGATION

    private suspend fun proceedWithOpeningObject(
        target: Id,
        space: Id,
        layout: ObjectType.Layout? = null
    ) {
        Timber.d("proceedWithOpeningObject, target:[$target], layout:[$layout]")
        if (target == vmParams.ctx) {
            toast("You are already here")
            Timber.d("proceedWithOpeningObject, target == vmParams.ctx")
            return
        }
        isCustomizeViewPanelVisible.value = false
        val navigateCommand = AppNavigation.Command.OpenObject(
            target = target,
            space = space
        )
        closeObject.async(
            CloseObject.Params(
                target = vmParams.ctx,
                space = vmParams.space
            )
        ).fold(
            onSuccess = { navigate(EventWrapper(navigateCommand)) },
            onFailure = {
                Timber.e(it, "Error while closing object set: ${vmParams.ctx}")
                navigate(EventWrapper(navigateCommand))
            }
        )
    }

    private suspend fun proceedWithOpeningChat(
        target: Id,
        space: Id
    ) {
        isCustomizeViewPanelVisible.value = false
        val navigateCommand = AppNavigation.Command.OpenChat(
            target = target,
            space = space,
            popUpToVault = false
        )
        closeObject.async(
            CloseObject.Params(
                target = vmParams.ctx,
                space = vmParams.space
            )
        ).fold(
            onSuccess = { navigate(EventWrapper(navigateCommand)) },
            onFailure = {
                Timber.e(it, "Error while closing object set: ${vmParams.ctx}")
                navigate(EventWrapper(navigateCommand))
            }
        )
    }

    /**
     * Handles creation flow for Note objects by creating the first text block.
     * Note objects don't have titles - content goes directly into text blocks.
     */
    private suspend fun proceedWithCreatingNoteObject(obj: ObjectWrapper.Basic) {
        val icon = obj.objectIcon(
            builder = urlBuilder,
            objType = storeOfObjectTypes.getTypeOfObject(obj)
        )
        createBlock.async(
            CreateBlock.Params(
                context = obj.id,
                target = "header",
                position = Position.BOTTOM,
                prototype = Block.Prototype.Text(style = Block.Content.Text.Style.P)
            )
        ).fold(
            onFailure = { error ->
                Timber.e(error, "Error creating text block for Note object")
                // Fallback: show sheet without blockId
                showSetObjectNameSheet(
                    objectId = obj.id,
                    icon = icon,
                    isIconChangeAllowed = false,
                    name = ""
                )
            },
            onSuccess = { (blockId, payload) ->
                // Update local state with payload
                dispatcher.send(payload)

                // Show name sheet with blockId
                showSetObjectNameSheet(
                    objectId = obj.id,
                    icon = icon,
                    isIconChangeAllowed = false,
                    targetBlockId = blockId,
                    name = ""
                )
            }
        )
    }

    private fun proceedWithOpeningTemplate(target: Id, targetTypeId: Id, targetTypeKey: Id) {
        isCustomizeViewPanelVisible.value = false
        val event = AppNavigation.Command.OpenModalTemplateSelect(
            template = target,
            space = vmParams.space.id,
            templateTypeId = targetTypeId,
            templateTypeKey = targetTypeKey
        )
        navigate(EventWrapper(event))
    }

    private suspend fun proceedWithOpeningObjectCollection(
        target: Id,
        space: Id
    ) {
        if (target == vmParams.ctx) {
            toast("You are already here")
            Timber.d("proceedWithOpeningObject, target == vmParams.ctx")
            return
        }
        isCustomizeViewPanelVisible.value = false
        jobs += viewModelScope.launch {
            closeObject.async(
                CloseObject.Params(
                    target = vmParams.ctx,
                    space = vmParams.space
                )
            ).fold(
                onSuccess = {
                    navigate(
                        EventWrapper(
                            AppNavigation.Command.OpenSetOrCollection(
                                target = target,
                                space = space
                            )
                        )
                    )
                },
                onFailure = {
                    Timber.e(it, "Error while closing object set: ${vmParams.ctx}")
                    navigate(
                        EventWrapper(
                            AppNavigation.Command.OpenSetOrCollection(
                                target = target,
                                space = space
                            )
                        )
                    )
                }
            )
        }
    }

    private suspend fun navigateByLayout(
        target: Id,
        space: Id,
        layout: ObjectType.Layout?,
        identityProfileLink: Id? = null
    ) {
        when (layout) {
            ObjectType.Layout.BASIC,
            ObjectType.Layout.TODO,
            ObjectType.Layout.NOTE,
            ObjectType.Layout.IMAGE,
            ObjectType.Layout.FILE,
            ObjectType.Layout.VIDEO,
            ObjectType.Layout.AUDIO,
            ObjectType.Layout.PDF,
            ObjectType.Layout.BOOKMARK -> proceedWithOpeningObject(
                target = target,
                space = space
            )
            ObjectType.Layout.PARTICIPANT -> {
                navigate(
                    EventWrapper(
                        AppNavigation.Command.OpenParticipant(
                            objectId = target,
                            space = space
                        )
                    )
                )
            }
            ObjectType.Layout.OBJECT_TYPE -> {
                navigate(
                    EventWrapper(
                        AppNavigation.Command.OpenTypeObject(
                            target = target,
                            space = space
                        )
                    )
                )
            }
            ObjectType.Layout.PROFILE -> proceedWithOpeningObject(
                target = identityProfileLink ?: target,
                space = space
            )
            ObjectType.Layout.SET, ObjectType.Layout.COLLECTION -> {
                closeObject.async(
                    CloseObject.Params(
                        target = vmParams.ctx,
                        space = vmParams.space
                    )
                ).fold(
                    onSuccess = {
                        navigate(
                            EventWrapper(
                                AppNavigation.Command.OpenSetOrCollection(
                                    target = target,
                                    space = space
                                )
                            )
                        )
                    },
                    onFailure = {
                        Timber.e(it, "Error while closing object set: ${vmParams.ctx}")
                        navigate(
                            EventWrapper(
                                AppNavigation.Command.OpenSetOrCollection(
                                    target = target,
                                    space = space
                                )
                            )
                        )
                    }
                )
            }
            ObjectType.Layout.DATE -> {
                navigate(
                    EventWrapper(
                        AppNavigation.Command.OpenDateObject(
                            objectId = target,
                            space = space
                        )
                    )
                )
            }
            ObjectType.Layout.CHAT_DERIVED -> {
                navigate(
                    EventWrapper(
                        AppNavigation.Command.OpenChat(
                            target = target,
                            space = space,
                            popUpToVault = false
                        )
                    )
                )
            }
            else -> {
                toast("Unexpected layout: $layout")
                Timber.e("Unexpected layout: $layout")
            }
        }
    }

    private suspend fun proceedWithNavigation(
        target: Id,
        space: Id,
        layout: ObjectType.Layout?,
        identityProfileLink: Id? = null
    ) {
        if (target == vmParams.ctx) {
            toast("You are already here")
            Timber.d("proceedWithNavigation, target == vmParams.ctx")
            return
        }
        navigateByLayout(target, space, layout, identityProfileLink)
    }

    private suspend fun proceedWithNavigation(obj: ObjectWrapper.Basic, identityProfileLink: Id? = null) {
        // If the object is a bookmark, open its URL in a Custom Tab
        if (obj.layout == ObjectType.Layout.BOOKMARK) {
            val url = obj.getSingleValue<String>(Relations.SOURCE)
            if (!url.isNullOrBlank()) {
                dispatch(ObjectSetCommand.Browse(url))
                return
            }
        }
        // If the object is a file, open it in the system previewer/player
        if (SupportedLayouts.isFileLayout(obj.layout)) {
            val layout = obj.layout
            val name = fieldParser.getObjectName(obj)
            when (layout) {
                ObjectType.Layout.IMAGE -> {
                    dispatch(
                        ObjectSetCommand.PlayMedia(
                            targetObjectId = obj.id,
                            name = name,
                            layout = ObjectType.Layout.IMAGE
                        )
                    )
                    return
                }
                ObjectType.Layout.VIDEO -> {
                    dispatch(
                        ObjectSetCommand.PlayMedia(
                            targetObjectId = obj.id,
                            name = name,
                            layout = ObjectType.Layout.VIDEO
                        )
                    )
                    return
                }
                ObjectType.Layout.AUDIO -> {
                    dispatch(
                        ObjectSetCommand.PlayMedia(
                            targetObjectId = obj.id,
                            name = name,
                            layout = ObjectType.Layout.AUDIO
                        )
                    )
                    return
                }
                else -> {
                    if (layout != null) {
                        val url = urlBuilder.getUrlBasedOnFileLayout(obj.id, layout)
                        if (url != null) {
                            dispatch(ObjectSetCommand.Browse(url))
                            return
                        }
                    }
                }
            }
        }
        if (obj.id == vmParams.ctx) {
            toast("You are already here")
            Timber.d("proceedWithNavigation, target == vmParams.ctx")
            return
        }

        val target = obj.id
        val space = vmParams.space.id

        // If the object is a Template (and not a Set or Collection), open it in the Modal Template Screen
        if (obj.isTemplateObject(storeOfObjectTypes = storeOfObjectTypes) && !obj.layout.isDataView()) {
            obj.getTypeForObjectAndTargetTypeForTemplate(storeOfObjectTypes = storeOfObjectTypes)
                ?.let { objType ->
                    val event = AppNavigation.Command.OpenModalTemplateEdit(
                        template = target,
                        space = vmParams.space.id,
                        templateTypeId = objType.id,
                        templateTypeKey = objType.uniqueKey
                    )
                    navigate(EventWrapper(event))
                    return
                }
        }

        navigateByLayout(
            target = target,
            space = space,
            layout = obj.layout,
            identityProfileLink = identityProfileLink
        )
    }
    //endregion NAVIGATION

    fun onUnsupportedViewErrorClicked() {
        // Do nothing
    }

    override fun onCleared() {
        Timber.d("onCleared, ")
        super.onCleared()
        titleUpdateChannel.cancel()
        stateReducer.clear()
    }

    fun onBackButtonClicked() {
        proceedWithClosingAndExit()
    }

    fun onBackButtonLongClicked() {
        viewModelScope.launch {
            backHistoryDelegate.onBackButtonLongPressed()
        }
    }

    fun onBackHistoryItemClicked(item: BackHistoryMenuItem) {
        proceedWithBackHistoryJump(item.entryId)
    }

    fun onBackHistoryHomeClicked() {
        val entryId = currentHomeEntryId ?: return
        proceedWithBackHistoryJump(entryId)
    }

    private fun proceedWithBackHistoryJump(entryId: String) {
        onBackHistoryMenuDismissed()
        viewModelScope.launch {
            closeObject.async(
                CloseObject.Params(target = vmParams.ctx, space = vmParams.space)
            ).fold(
                onSuccess = { dispatch(AppNavigation.Command.PopToBackStackEntry(entryId)) },
                onFailure = {
                    Timber.e(it, "Error while closing object set before back-history jump")
                    dispatch(AppNavigation.Command.PopToBackStackEntry(entryId))
                }
            )
        }
    }

    fun onBackHistoryChannelsClicked() {
        onBackHistoryMenuDismissed()
        viewModelScope.launch {
            closeObject.async(CloseObject.Params(target = vmParams.ctx, space = vmParams.space))
            proceedWithClearingSpaceBeforeExitingToVault()
            dispatch(AppNavigation.Command.ExitToVault)
        }
    }

    fun onAddNewDocumentClicked(objType: ObjectWrapper.Type? = null) {
        Timber.d("onAddNewDocumentClicked, objType:[$objType]")

        val startTime = System.currentTimeMillis()
        val params = objType?.uniqueKey.getCreateObjectParams(
            space = vmParams.space,
            objType?.defaultTemplateId
        )
        jobs += viewModelScope.launch {
            createObject.async(params).fold(
                onSuccess = { result ->
                    delegator.delegate(
                        if (objType?.recommendedLayout.isDataView())
                            Action.OpenCollection(
                                target = result.objectId,
                                space = requireNotNull(result.obj.spaceId)
                            )
                        else
                            Action.OpenObject(
                                target = result.objectId,
                                space = requireNotNull(result.obj.spaceId)
                            )
                    )
                    sendAnalyticsObjectCreateEvent(
                        analytics = analytics,
                        route = EventsDictionary.Routes.navigation,
                        startTime = startTime,
                        view = EventsDictionary.View.viewNavbar,
                        objType = objType ?: storeOfObjectTypes.getByKey(result.typeKey.key),
                        spaceParams = provideParams(vmParams.space.id)
                    )
                },
                onFailure = { e ->
                    Timber.e(e, "Error while creating a new object")
                    toast("Error while creating a new object")
                }
            )
        }
    }

    private fun sendAnalyticsObjectCreateEvent(startTime: Long, typeKey: Key?) {
        viewModelScope.launch {
            val objType = typeKey?.let { storeOfObjectTypes.getByKey(it) }
            logEvent(
                state = stateReducer.state.value,
                analytics = analytics,
                event = ObjectStateAnalyticsEvent.OBJECT_CREATE,
                startTime = startTime,
                type = objType?.sourceObject ?: OBJ_TYPE_CUSTOM,
                spaceParams = provideParams(vmParams.space.id)
            )
        }
    }

    fun onSearchButtonClicked() {
        viewModelScope.sendEvent(
            analytics = analytics,
            eventName = EventsDictionary.searchScreenShow,
            props = Props(mapOf(
                EventsPropertiesKey.route to EventsDictionary.Routes.navigation,
                EventsPropertiesKey.spaceId to vmParams.space.id
            ))
        )
        viewModelScope.launch {
            dispatch(
                AppNavigation.Command.OpenGlobalSearch(
                    space = vmParams.space.id
                )
            )
        }
    }

    fun onHomeButtonClicked() {
        viewModelScope.launch {
            navPanelState.firstOrNull()?.leftButtonClickAnalytics(analytics)
        }
        viewModelScope.launch {
            dispatch(AppNavigation.Command.ExitToSpaceHome)
        }
    }

    fun onShareButtonClicked() {
        viewModelScope.launch {
            navPanelState.firstOrNull()?.leftButtonClickAnalytics(analytics)
        }
        viewModelScope.launch {
            dispatch(AppNavigation.Command.OpenShareScreen(vmParams.space))
        }
    }

    fun onClickListener(clicked: ListenerType) {
        Timber.d("onClickListener, clicked:[$clicked]")
        when (clicked) {
            is ListenerType.Relation.SetQuery -> {
                if (isOwnerOrEditor) {
                    val queries = clicked.queries.map { it.id }
                    val command = if (queries.isEmpty()) {
                        ObjectSetCommand.Modal.OpenEmptyDataViewSelectQueryScreen
                    } else {
                        ObjectSetCommand.Modal.OpenDataViewSelectQueryScreen(
                            selectedTypes = queries
                        )
                    }
                    dispatch(command)
                } else {
                    dispatch(ObjectSetCommand.ShowOnlyAccessError)
                }
            }
            is ListenerType.Relation.ChangeQueryByRelation -> {
                toast(clicked.msg)
            }
            is ListenerType.Relation.ObjectType -> {
                when (clicked.relation) {
                    is ObjectRelationView.ObjectType.Base -> {
                        val state = stateReducer.state.value.dataViewState() ?: return
                        when (state) {
                            is ObjectState.DataView.Collection -> {
                                //do nothing
                            }
                            is ObjectState.DataView.Set -> {
                                if (isOwnerOrEditor) {
                                    val setOfValue = state.getSetOfValue(vmParams.ctx)
                                    val command =
                                        if (state.isSetByRelation(setOfValue = setOfValue)) {
                                            ObjectSetCommand.Modal.ShowObjectSetRelationPopupMenu(
                                                ctx = clicked.relation.id,
                                                anchor = clicked.viewId
                                            )
                                        } else {
                                            ObjectSetCommand.Modal.ShowObjectSetTypePopupMenu(
                                                ctx = clicked.relation.id,
                                                anchor = clicked.viewId
                                            )
                                        }
                                    dispatch(command)
                                } else {
                                    dispatch(ObjectSetCommand.ShowOnlyAccessError)
                                }
                            }

                            is ObjectState.DataView.TypeSet -> {
                                //do nothing
                            }
                        }
                    }
                    else -> {
                        Timber.d("Ignoring click on relation, relation:[${clicked.relation}]")
                    }
                }
            }
            is ListenerType.Relation.Featured -> {
                onRelationClickedListMode(
                    ctx = vmParams.ctx,
                    view = clicked.relation
                )
            }
            else -> {
                Timber.d("Ignoring click, listener:[${clicked}]")
            }
        }
    }

    private fun proceedWithTogglingRelationCheckboxValue(view: ObjectRelationView, ctx: Id) {
        viewModelScope.launch {
            check(view is ObjectRelationView.Checkbox)
            setObjectDetails(
                UpdateDetail.Params(
                    target = ctx,
                    key = view.key,
                    value = !view.isChecked
                )
            ).process(
                success = {
                    dispatcher.send(it)
                    analytics.sendAnalyticsRelationEvent(
                        eventName = EventsDictionary.relationChangeValue,
                        storeOfRelations = storeOfRelations,
                        relationKey = view.key,
                        spaceParams = provideParams(spaceManager.get())
                    )
                },
                failure = { Timber.e(it, "Error while updating checkbox relation") }
            )
        }
    }

    private fun onRelationClickedListMode(ctx: Id, view: ObjectRelationView) {
        viewModelScope.launch {
            val relation = storeOfRelations.getById(view.id)
            if (relation == null) {
                Timber.w("Couldn't find relation in store by id:${view.id}")
                return@launch
            }
            when (relation.format) {
                RelationFormat.SHORT_TEXT,
                RelationFormat.LONG_TEXT,
                RelationFormat.NUMBER,
                RelationFormat.URL,
                RelationFormat.EMAIL,
                RelationFormat.PHONE -> {
                    _commands.emit(
                        ObjectSetCommand.Modal.EditIntrinsicTextRelation(
                            ctx = ctx,
                            relation = relation.key,
                            space = requireNotNull(relation.spaceId)
                        )
                    )
                }
                RelationFormat.CHECKBOX -> {
                    if (relation.isReadonlyValue) {
                        toast(RelationListViewModel.NOT_ALLOWED_FOR_RELATION)
                        Timber.d("No interaction allowed with this relation")
                        return@launch
                    }
                    proceedWithTogglingRelationCheckboxValue(view, ctx)
                }
                RelationFormat.DATE -> {
                    if (relation.isReadonlyValue)  {
                        val timeInMillis =
                            (view as? ObjectRelationView.Date)?.relativeDate?.initialTimeInMillis
                        handleReadOnlyValue(timeInMillis = timeInMillis)
                    } else {
                        _commands.emit(
                            ObjectSetCommand.Modal.EditGridDateCell(
                                ctx = vmParams.ctx,
                                objectId = vmParams.ctx,
                                relationKey = relation.key,
                                space = vmParams.space.id
                            )
                        )
                    }
                }
                RelationFormat.STATUS,
                RelationFormat.TAG -> {
                    _commands.emit(
                        ObjectSetCommand.Modal.EditTagOrStatusRelationValue(
                            ctx = vmParams.ctx,
                            relation = relation.key,
                            space = requireNotNull(relation.spaceId)
                        )
                    )
                }
                RelationFormat.FILE,
                RelationFormat.OBJECT -> {
                    _commands.emit(
                        ObjectSetCommand.Modal.EditObjectRelationValue(
                            ctx = vmParams.ctx,
                            relation = relation.key,
                            space = requireNotNull(relation.spaceId)
                        )
                    )
                }
                RelationFormat.EMOJI,
                RelationFormat.RELATIONS,
                RelationFormat.UNDEFINED -> {
                    toast(RelationListViewModel.NOT_SUPPORTED_UPDATE_VALUE)
                    Timber.d("Update value of relation with format:[${relation.format}] is not supported")
                }
                else -> {
                    Timber.d("Ignoring")
                }
            }
        }
    }

    fun onObjectSetQueryPicked(query: Id) {
        Timber.d("onObjectSetQueryPicked, query:[$query]")
        val startTime = System.currentTimeMillis()
        viewModelScope.launch {
            val params = SetQueryToObjectSet.Params(
                ctx = vmParams.ctx,
                query = query
            )
            setQueryToObjectSet.async(params).fold(
                onSuccess = { payload ->
                    logEvent(
                        state = stateReducer.state.value,
                        analytics = analytics,
                        event = ObjectStateAnalyticsEvent.SELECT_QUERY,
                        type = query,
                        startTime = startTime,
                        spaceParams = provideParams(vmParams.space.id)
                    )
                    defaultPayloadConsumer(payload)
                },
                onFailure = { e -> Timber.e(e, "Error while setting Set query") }
            )
        }
    }

    fun proceedWithConvertingToCollection() {
        val startTime = System.currentTimeMillis()
        val params = ConvertObjectToCollection.Params(ctx = vmParams.ctx)
        viewModelScope.launch {
            objectToCollection.async(params).fold(
                onFailure = { error -> Timber.e(error, "Error convert object to collection") },
                onSuccess = {
                    isCustomizeViewPanelVisible.value = false
                    logEvent(
                        state = stateReducer.state.value,
                        analytics = analytics,
                        event = ObjectStateAnalyticsEvent.TURN_INTO_COLLECTION,
                        startTime = startTime,
                        spaceParams = provideParams(vmParams.space.id)
                    )
                }
            )
        }
    }

    //region TYPES AND TEMPLATES WIDGET
    fun onNewTypeForViewerClicked(objType: ObjectWrapper.Type) {
        Timber.d("onNewTypeForViewerClicked, objType:[$objType]")
        selectedTypeFlow.value = objType
    }

    private fun showTypeTemplatesWidgetForObjectCreation() {
        val isPossibleToChangeType = stateReducer.state.value.dataViewState()?.isChangingDefaultTypeAvailable()
        showTypeTemplatesWidget(
            getViewer = { it?.viewerByIdOrFirst(session.currentViewerId.value) },
            createState = { viewer ->
                TypeTemplatesWidgetUI.Data(
                    showWidget = true,
                    isEditing = false,
                    viewerId = viewer.id,
                    isPossibleToChangeType = isPossibleToChangeType == true,
                    isPossibleToChangeTemplate = false
                )
            }
        )
    }

    private fun showTypeTemplatesWidget(
        getViewer: (ObjectState.DataView?) -> DVViewer?,
        createState: (DVViewer) -> TypeTemplatesWidgetUI.Data
    ) {
        viewModelScope.launch {
            val dataView = stateReducer.state.value.dataViewState() ?: return@launch
            val viewer = getViewer(dataView) ?: return@launch

            // Get dataview block ID for cleanup
            val dvBlock = when (dataView) {
                is ObjectState.DataView -> dataView.dataViewBlock.id
                else -> null
            }

            val (type, _) = dataView.getActiveViewTypeAndTemplate(
                ctx = vmParams.ctx,
                activeView = viewer,
                storeOfObjectTypes = storeOfObjectTypes,
                onDeletedTypeDetected = { deletedViewer ->
                    // Cleanup deleted type in viewer (fire-and-forget)
                    if (dvBlock != null) {
                        cleanupDeletedTypeInViewer(
                            ctx = vmParams.ctx,
                            dv = dvBlock,
                            viewer = deletedViewer
                        )
                    }
                }
            )

            // If type is null due to deleted type, use space default
            val effectiveType = type ?: getSpaceDefaultType()
            if (effectiveType == null) return@launch
if (effectiveType.recommendedLayout == ObjectType.Layout.SET || effectiveType.recommendedLayout == ObjectType.Layout.COLLECTION) {
                return@launch
            }
            typeTemplatesWidgetState.value = createState(viewer)
            selectedTypeFlow.value = effectiveType
        }
        logEvent(ObjectStateAnalyticsEvent.SHOW_TEMPLATES)
        logEvent(ObjectStateAnalyticsEvent.SCREEN_TYPE_TEMPLATE_SELECTOR)
    }

    fun onTypeTemplatesWidgetAction(action: TypeTemplatesWidgetUIAction) {
        Timber.d("onTypeTemplatesWidgetAction, action:[$action]")
        val uiState = typeTemplatesWidgetState.value
        when (action) {
            is TypeTemplatesWidgetUIAction.TypeClick.Item -> {
                when (uiState) {
                    is TypeTemplatesWidgetUI.Data -> {
                        selectedTypeFlow.value = action.type
                    }

                    is TypeTemplatesWidgetUI.Init -> Unit
                }
            }
            TypeTemplatesWidgetUIAction.TypeClick.Search -> {
                viewModelScope.launch {
                    _commands.emit(
                        ObjectSetCommand.Modal.OpenSelectTypeScreen
                    )
                }
            }
            is TypeTemplatesWidgetUIAction.TemplateClick -> {
                viewModelScope.launch {
                    uiState.onTemplateClick(action.template)
                }
            }
        }
    }

    private suspend fun TypeTemplatesWidgetUI.onTemplateClick(
        templateView: TemplateView
    ) {
        if (this is TypeTemplatesWidgetUI.Data && moreMenuItem != null) {
            typeTemplatesWidgetState.value = hideMoreMenu()
            return
        }
        if (this is TypeTemplatesWidgetUI.Data) {
            typeTemplatesWidgetState.value = copy(showWidget = false)
        }
        selectedTypeFlow.value = null
        delay(DELAY_BEFORE_CREATING_TEMPLATE)
        when (templateView) {
            is TemplateView.Blank -> {
                logEvent(
                    event = ObjectStateAnalyticsEvent.SET_AS_DEFAULT_TYPE,
                    type = templateView.targetTypeKey.key
                )
                logEvent(ObjectStateAnalyticsEvent.CHANGE_DEFAULT_TEMPLATE)
                proceedWithUpdateViewer(
                    viewerId = getWidgetViewerId()
                ) {
                    it.copy(
                        defaultTemplate = templateView.id,
                        defaultObjectType = templateView.targetTypeId.id
                    )
                }
                proceedWithDataViewObjectCreate(
                    typeChosenBy = templateView.targetTypeKey,
                    templateId = templateView.id
                )

            }
            is TemplateView.Template -> {
                logEvent(
                    event = ObjectStateAnalyticsEvent.SET_AS_DEFAULT_TYPE,
                    type = templateView.targetTypeKey.key
                )
                logEvent(ObjectStateAnalyticsEvent.CHANGE_DEFAULT_TEMPLATE)
                proceedWithUpdateViewer(
                    viewerId = getWidgetViewerId()
                ) {
                    it.copy(
                        defaultTemplate = templateView.id,
                        defaultObjectType = templateView.targetTypeId.id
                    )
                }
                proceedWithDataViewObjectCreate(
                    typeChosenBy = templateView.targetTypeKey,
                    templateId = templateView.id
                )
            }
            is TemplateView.New -> {
                proceedWithCreatingTemplate(
                    targetTypeId = templateView.targetTypeId.id,
                    targetTypeKey = templateView.targetTypeKey.key
                )
            }
        }
    }

    private fun logEvent(event: ObjectStateAnalyticsEvent, type: Id? = null) {
        viewModelScope.launch {
            logEvent(
                state = stateReducer.state.value,
                analytics = analytics,
                event = event,
                type = type,
                spaceParams = provideParams(vmParams.space.id)
            )
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun subscribeToSelectedType() {
        viewModelScope.launch {
            selectedTypeFlow
                .filterNotNull()
                .onEach {
                    updateTypesForTypeTemplatesWidget(it.id)
                }
                .flatMapLatest { selectedType ->
                    templatesContainer.subscribeToTemplates(
                        type = selectedType.id,
                        space = vmParams.space,
                        subscription = "${vmParams.ctx}$SUBSCRIPTION_TEMPLATES_ID"
                    )
                }.map { templates ->
                    val state = stateReducer.state.value
                    val viewerId = typeTemplatesWidgetState.value.getWidgetViewerId()
                    val dataView = state.dataViewState() ?: return@map emptyList<TemplateView>()
                    val viewer = dataView.viewerById(viewerId) ?: return@map emptyList<TemplateView>()
                    val selectedTypeId = selectedTypeFlow.value?.id ?: return@map emptyList<TemplateView>()

                    // Get dataview block ID for cleanup
                    val dvBlock = when (dataView) {
                        is ObjectState.DataView -> dataView.dataViewBlock.id
                        else -> null
                    }

                    val (type, template) = dataView.getActiveViewTypeAndTemplate(
                        ctx = vmParams.ctx,
                        activeView = viewer,
                        storeOfObjectTypes = storeOfObjectTypes,
                        onDeletedTypeDetected = { deletedViewer ->
                            // Cleanup deleted type in viewer (fire-and-forget)
                            if (dvBlock != null) {
                                cleanupDeletedTypeInViewer(
                                    ctx = vmParams.ctx,
                                    dv = dvBlock,
                                    viewer = deletedViewer
                                )
                            }
                        }
                    )

                    // If type is null due to deleted type, use space default
                    val effectiveType = type ?: getSpaceDefaultType()

                    when (typeTemplatesWidgetState.value) {
                        is TypeTemplatesWidgetUI.Data -> {
                            if (effectiveType?.id == selectedTypeFlow.value?.id) {
                                processTemplates(
                                    templates = templates,
                                    viewerDefType = effectiveType ?: storeOfObjectTypes.get(
                                        selectedTypeId
                                    ),
                                    viewerDefTemplate = template
                                        ?: selectedTypeFlow.value?.defaultTemplateId
                                )
                            } else {
                                processTemplates(
                                    templates = templates,
                                    viewerDefType = storeOfObjectTypes.get(selectedTypeId),
                                    viewerDefTemplate = selectedTypeFlow.value?.defaultTemplateId
                                )
                            }
                        }

                        is TypeTemplatesWidgetUI.Init -> emptyList()
                    }
                }
                .catch {
                    Timber.e(it, "Error while processing templates")
                }
                .collect{ templateViews ->
                    typeTemplatesWidgetState.value =
                        when (val uistate = typeTemplatesWidgetState.value) {
                            is TypeTemplatesWidgetUI.Data -> uistate.copy(templates = templateViews)
                            is TypeTemplatesWidgetUI.Init -> uistate
                        }
                }
        }
    }

    private suspend fun updateTypesForTypeTemplatesWidget(selectedType: Id) {
        when (val widgetState = typeTemplatesWidgetState.value) {
            is TypeTemplatesWidgetUI.Data -> {
                if (widgetState.isPossibleToChangeType) {
                    updateWidgetStateWithTypes(selectedType, widgetState)
                }
            }
            else -> {
                // Do nothing
            }
        }
    }

    private suspend fun updateWidgetStateWithTypes(selectedType: Id, widgetState: TypeTemplatesWidgetUI.Data) {
        val objectTypes = widgetState.objectTypes
        val isTypePresent = objectTypes.filterIsInstance<TemplateObjectTypeView.Item>()
            .any { it.type.id == selectedType }
        if (objectTypes.isNotEmpty() && isTypePresent) {
            updateExistingTypes(selectedType, widgetState)
        } else {
            fetchAndProcessObjectTypes(selectedType, widgetState)
        }
    }

    private fun updateExistingTypes(selectedType: Id, widgetState: TypeTemplatesWidgetUI.Data) {
        val types = widgetState.objectTypes.map { it.updateSelectionState(selectedType) }
        typeTemplatesWidgetState.value = widgetState.copy(objectTypes = types)
    }

    private fun TemplateObjectTypeView.updateSelectionState(selectedType: Id): TemplateObjectTypeView {
        return when (this) {
            is TemplateObjectTypeView.Item -> this.copy(isSelected = this.type.id == selectedType)
            is TemplateObjectTypeView.Search -> this
        }
    }

    private suspend fun fetchAndProcessObjectTypes(selectedType: Id, widgetState: TypeTemplatesWidgetUI.Data) {
        // Determine whether this is a 1-1 space for context-aware filtering / sorting.
        val isOneToOneSpace = spaceViews.get(vmParams.space)?.isOneToOneSpace == true
        val createLayouts = getCreateObjectLayouts(isOneToOneSpace)

        val allTypes = storeOfObjectTypes.getAll()
        val filteredTypes = allTypes.filter { type ->
            val layout = type.recommendedLayout
            layout != null && createLayouts.contains(layout)
                && type.recommendedLayout != ObjectType.Layout.PARTICIPANT
                && type.uniqueKey != ObjectTypeIds.TEMPLATE
        }
        val sortedTypes = filteredTypes.sortByTypePriority(isChatSpace = isOneToOneSpace)
        val list = buildList {
            add(TemplateObjectTypeView.Search)
            addAll(sortedTypes.toTemplateObjectTypeViewItems(selectedType))
        }
        typeTemplatesWidgetState.value = widgetState.copy(objectTypes = list)
    }

    fun proceedWithSelectedTemplate(
        template: Id,
        typeId: Id,
        typeKey: Id
    ) {
        Timber.d("proceedWithSelectedTemplate, template:[$template], typeId:[$typeId], typeKey:[$typeKey]")
        val templateView = TemplateView.Template(
            id = template,
            targetTypeId = TypeId(typeId),
            targetTypeKey = TypeKey(typeKey),
            name = ""
        )
        onTypeTemplatesWidgetAction(action = TypeTemplatesWidgetUIAction.TemplateClick(templateView))
    }

    /**
     * Gets the space's default object type with fallback to PAGE type.
     * Used when viewer's type is deleted or not available.
     */
    private suspend fun getSpaceDefaultType(): ObjectWrapper.Type? {
        val typeKey = getDefaultObjectType.async(vmParams.space).getOrNull()?.type
        if (typeKey == null) {
            Timber.w("Space default type key is null, falling back to VIEW_DEFAULT_OBJECT_TYPE type")
            return storeOfObjectTypes.get(VIEW_DEFAULT_OBJECT_TYPE)
        }
        val typeObject = storeOfObjectTypes.getByKey(typeKey.key)
        if (typeObject == null) {
            Timber.w("Space default type not found in store, falling back to VIEW_DEFAULT_OBJECT_TYPE type")
            return storeOfObjectTypes.get(VIEW_DEFAULT_OBJECT_TYPE)
        }
        return typeObject
    }

    /**
     * Resolves type and template with fallback to space default.
     *
     * When the viewer's type is deleted (resolvedType is null), falls back to the space
     * default type AND its default template to maintain consistency between type and template.
     *
     * @param resolvedType The type resolved from the viewer (null if deleted)
     * @param defaultTemplate The template from the viewer's type (may be invalid if type is deleted)
     * @param userChosenTemplate Optional template explicitly chosen by the user (highest priority)
     * @return Pair of (finalType, finalTemplate) where both are aligned to the same type
     */
    private suspend fun resolveTypeAndTemplateWithFallback(
        resolvedType: ObjectWrapper.Type?,
        defaultTemplate: Id?,
        userChosenTemplate: Id?
    ): Pair<ObjectWrapper.Type?, Id?> {
        // User-chosen template always has highest priority
        if (userChosenTemplate != null) {
            val finalType = resolvedType ?: getSpaceDefaultType()
            return Pair(finalType, userChosenTemplate)
        }

        // If type is null (deleted), use space default type AND its template
        if (resolvedType == null) {
            val spaceDefaultType = getSpaceDefaultType()
            val spaceDefaultTemplate = spaceDefaultType?.defaultTemplateId
            return Pair(spaceDefaultType, spaceDefaultTemplate)
        }

        // Normal case: type exists, use its template
        return Pair(resolvedType, defaultTemplate)
    }

    /**
     * Cleans up a viewer's defaultObjectType when it references a deleted type.
     * This is a fire-and-forget operation that updates the view asynchronously.
     *
     * @param ctx The context (object) ID
     * @param dv The dataview block ID
     * @param viewer The viewer with stale type ID
     */
    private fun cleanupDeletedTypeInViewer(
        ctx: Id,
        dv: Id,
        viewer: DVViewer
    ) {
        viewModelScope.launch {
            try {
                Timber.d("Cleaning up deleted type in viewer ${viewer.id}")
                val updatedViewer = viewer.copy(defaultObjectType = null)
                onEvent(
                    ViewerEvent.UpdateView(
                        ctx = ctx,
                        dv = dv,
                        viewer = updatedViewer,
                        onResult = {
                            Timber.d("Successfully cleaned up deleted type in viewer ${viewer.id}")
                        }
                    )
                )
            } catch (e: Exception) {
                Timber.e(e, "Failed to cleanup deleted type in viewer ${viewer.id}")
            }
        }
    }

    /**
     * Refreshes the TypeTemplatesWidget to show only valid types.
     * Called when a deleted type is detected to update the UI.
     */
    private fun refreshTypeTemplatesWidgetTypes() {
        viewModelScope.launch {
            when (val widgetState = typeTemplatesWidgetState.value) {
                is TypeTemplatesWidgetUI.Data -> {
                    // Trigger refresh of available types using existing logic
                    val selectedType = selectedTypeFlow.value?.id ?: VIEW_DEFAULT_OBJECT_TYPE
                    updateTypesForTypeTemplatesWidget(selectedType)
                }

                else -> {
                    // Widget not active, do nothing
                }
            }
        }
    }

    private fun processTemplates(
        templates: List<ObjectWrapper.Basic>,
        viewerDefType: ObjectWrapper.Type?,
        viewerDefTemplate: Id?
    ): List<TemplateView> {

        if (viewerDefType == null) {
            Timber.w("processTemplates, Viewer def type is null (likely deleted type, fallback should have been applied)")
            // Refresh widget to show valid types
            refreshTypeTemplatesWidgetTypes()
            return emptyList()
        }

        val viewerDefTypeId = viewerDefType.id
        val viewerDefTypeKey = TypeKey(viewerDefType.uniqueKey)

        val isTemplatesAllowed = viewerDefType.isTemplatesAllowed()

        val state = typeTemplatesWidgetState.value

        val isPossibleToChangeType = when (state) {
            is TypeTemplatesWidgetUI.Data -> state.isPossibleToChangeType
            is TypeTemplatesWidgetUI.Init -> false
        }

        typeTemplatesWidgetState.value = when (state) {
            is TypeTemplatesWidgetUI.Data -> state.copy(
                isPossibleToChangeTemplate = isTemplatesAllowed
            )
            is TypeTemplatesWidgetUI.Init -> state
        }

        val newTemplate = if (!isTemplatesAllowed) {
            Timber.d("processTemplates, Templates are not allowed for this type")
            emptyList()
        } else {
            listOf(
                TemplateView.New(
                    targetTypeId = TypeId(viewerDefTypeId),
                    targetTypeKey = viewerDefTypeKey
                )
            )
        }

        val blankTemplate = TemplateView.Blank(
            id = TemplateView.DEFAULT_TEMPLATE_ID_BLANK,
            targetTypeId = TypeId(viewerDefTypeId),
            targetTypeKey = viewerDefTypeKey,
            layout = viewerDefType.recommendedLayout?.code ?: ObjectType.Layout.BASIC.code,
            isDefault = viewerDefTemplate.isNullOrEmpty()
                    || viewerDefTemplate == TemplateView.DEFAULT_TEMPLATE_ID_BLANK,
        )

        return if (templates.size == 1 && templates.first().id == viewerDefTemplate) {
            templates.map { objTemplate ->
                objTemplate.toTemplateView(
                    urlBuilder = urlBuilder,
                    coverImageHashProvider = coverImageHashProvider,
                    viewerDefTemplateId = viewerDefTemplate,
                    viewerDefTypeKey = viewerDefTypeKey
                )
            } + newTemplate
        } else {
            buildList {
                if (isPossibleToChangeType) {
                    add(blankTemplate)
                }
                addAll(
                    templates.map { objTemplate ->
                        objTemplate.toTemplateView(
                            urlBuilder = urlBuilder,
                            coverImageHashProvider = coverImageHashProvider,
                            viewerDefTemplateId = viewerDefTemplate,
                            viewerDefTypeKey = viewerDefTypeKey
                        )
                    }
                )
                addAll(newTemplate)
            }
        }
    }

    private suspend fun proceedWithCreatingTemplate(targetTypeId: Id, targetTypeKey: Id) {
        delay(DELAY_BEFORE_CREATING_TEMPLATE)
        val params = CreateTemplate.Params(
            targetObjectTypeId = targetTypeId,
            spaceId = vmParams.space
        )
        createTemplate.async(params).fold(
            onSuccess = { createObjectResult ->
                viewModelScope.logEvent(
                    state = stateReducer.state.value,
                    analytics = analytics,
                    event = ObjectStateAnalyticsEvent.CREATE_TEMPLATE,
                    type = storeOfObjectTypes.get(targetTypeId)?.sourceObject,
                    spaceParams = provideParams(vmParams.space.id)
                )
                proceedWithOpeningTemplate(
                    target = createObjectResult.id,
                    targetTypeId = targetTypeId,
                    targetTypeKey = targetTypeKey
                )
            },
            onFailure = { e ->
                Timber.e(e, "Error while creating new template")
                toast("Error while creating new template")
            }
        )
    }

    fun onEditTemplateButtonClicked() {
        typeTemplatesWidgetState.value = when (val value = typeTemplatesWidgetState.value) {
            is TypeTemplatesWidgetUI.Data -> value.enterEditing()
            else -> value
        }
    }

    fun onDoneTemplateButtonClicked() {
        Timber.d("onDoneTemplateButtonClicked, ")
        typeTemplatesWidgetState.value = when (val value = typeTemplatesWidgetState.value) {
            is TypeTemplatesWidgetUI.Data -> value.exitEditing()
            else -> value
        }
    }

    fun onMoreTemplateButtonClicked(template: TemplateView) {
        Timber.d("onMoreTemplateButtonClicked, template:[$template]")
        val uiState = typeTemplatesWidgetState.value as? TypeTemplatesWidgetUI.Data ?: return
        typeTemplatesWidgetState.value = if (uiState.moreMenuItem != null) {
            uiState.hideMoreMenu()
        } else {
            uiState.showMoreMenu(template)
        }
    }

    fun onDismissTemplatesWidget() {
        Timber.d("onDismissTemplatesWidget, ")
        typeTemplatesWidgetState.value = when (val state = typeTemplatesWidgetState.value) {
            is TypeTemplatesWidgetUI.Data -> {
                when {
                    state.moreMenuItem != null -> state.hideMoreMenu()
                    state.showWidget -> {
                        selectedTypeFlow.value = null
                        TypeTemplatesWidgetUI.Init()
                    }
                    else -> state
                }
            }
            is TypeTemplatesWidgetUI.Init -> TypeTemplatesWidgetUI.Init()
        }
    }

    private fun hideTemplatesWidget() {
        selectedTypeFlow.value = null
        typeTemplatesWidgetState.value = TypeTemplatesWidgetUI.Init()
    }

    fun onMoreMenuClicked(click: TemplateMenuClick) {
        Timber.d("onMoreMenuClicked, click:[$click]")
        viewModelScope.launch {
            when (click) {
                is TemplateMenuClick.Default -> proceedWithUpdatingViewDefaultTemplate()
                is TemplateMenuClick.Delete -> proceedWithDeletionTemplate()
                is TemplateMenuClick.Duplicate -> proceedWithDuplicateTemplate()
                is TemplateMenuClick.Edit -> proceedWithEditingTemplate()
            }
        }
    }

    private suspend fun proceedWithUpdatingViewDefaultTemplate() {
        when (val uiState = typeTemplatesWidgetState.value) {
            is TypeTemplatesWidgetUI.Data -> {
                when (val templateToSetAsDefault = uiState.moreMenuItem) {
                    is TemplateView.Blank -> {
                        typeTemplatesWidgetState.value = uiState.exitEditing()
                        proceedWithUpdateViewer(viewerId = uiState.viewerId) {
                            it.copy(defaultTemplate = templateToSetAsDefault.id)
                        }
                    }
                    is TemplateView.Template -> {
                        typeTemplatesWidgetState.value = uiState.exitEditing()
                        proceedWithUpdateViewer(viewerId = uiState.viewerId) {
                            it.copy(defaultTemplate = templateToSetAsDefault.id)
                        }
                    }
                    else -> Unit
                }
            }
            is TypeTemplatesWidgetUI.Init -> Unit
        }
    }

    private fun proceedWithDuplicateTemplate() {
        val uiState =  (typeTemplatesWidgetState.value as? TypeTemplatesWidgetUI.Data) ?: return
        val template = (uiState.moreMenuItem as? TemplateView.Template) ?: return
        typeTemplatesWidgetState.value = uiState.exitEditing()
        val params = DuplicateObjects.Params(
            ids = listOf(template.id)
        )
        viewModelScope.launch {
            duplicateObjects.async(params).fold(
                onSuccess = { ids ->
                    logEvent(
                        state = stateReducer.state.value,
                        analytics = analytics,
                        event = ObjectStateAnalyticsEvent.DUPLICATE_TEMPLATE,
                        type = template.targetTypeId.id,
                        spaceParams = provideParams(vmParams.space.id)
                    )
                    Timber.d("Successfully duplicated templates: $ids")
                },
                onFailure = { e ->
                    Timber.e(e, "Error while duplicating templates")
                    toast("Error while duplicating templates")
                }
            )
        }
    }

    private fun proceedWithDeletionTemplate() {
        val uiState = (typeTemplatesWidgetState.value as? TypeTemplatesWidgetUI.Data) ?: return
        val template = (uiState.moreMenuItem as? TemplateView.Template) ?: return
        typeTemplatesWidgetState.value = uiState.exitEditing()
        val params = SetObjectListIsArchived.Params(
            targets = listOf(template.id),
            isArchived = true
        )
        viewModelScope.launch {
            setObjectListIsArchived.async(params).fold(
                onSuccess = { ids ->
                    logEvent(
                        state = stateReducer.state.value,
                        analytics = analytics,
                        event = ObjectStateAnalyticsEvent.DELETE_TEMPLATE,
                        type = template.targetTypeKey.key,
                        spaceParams = provideParams(vmParams.space.id)
                    )
                    Timber.d("Successfully archived templates: $ids")
                },
                onFailure = { e ->
                    Timber.e(e, "Error while deleting templates")
                    toast("Error while deleting templates")
                }
            )
        }
    }

    private suspend fun proceedWithEditingTemplate() {
        val uiState = (typeTemplatesWidgetState.value as? TypeTemplatesWidgetUI.Data) ?: return
        val template = uiState.moreMenuItem ?: return
        typeTemplatesWidgetState.value = uiState.exitEditing()
        when (template) {
            is TemplateView.Template -> {
                delay(DELAY_BEFORE_CREATING_TEMPLATE)
                proceedWithOpeningTemplate(
                    target = template.id,
                    targetTypeId = template.targetTypeId.id,
                    targetTypeKey = template.targetTypeKey.key
                )
                viewModelScope.launch {
                    logEvent(
                        state = stateReducer.state.value,
                        analytics = analytics,
                        event = ObjectStateAnalyticsEvent.EDIT_TEMPLATE,
                        type = template.targetTypeKey.key,
                        spaceParams = provideParams(vmParams.space.id)
                    )
                }
            }
            else -> Unit
        }
    }
    //endregion

    // region VIEWS
    fun onViewersWidgetAction(action: ViewersWidgetUi.Action) {
        Timber.d("onViewersWidgetAction, action:[$action]")
        val state = stateReducer.state.value.dataViewState() ?: return
        when (action) {
            ViewersWidgetUi.Action.Dismiss -> {
                viewersWidgetState.value = viewersWidgetState.value.copy(
                    showWidget = false,
                    isEditing = false
                )
            }
            ViewersWidgetUi.Action.DoneMode -> {
                viewersWidgetState.value = viewersWidgetState.value.copy(isEditing = false)
            }
            ViewersWidgetUi.Action.EditMode -> {
                viewersWidgetState.value = viewersWidgetState.value.copy(isEditing = true)
            }
            is ViewersWidgetUi.Action.Delete -> {
                viewModelScope.launch {
                    val startTime = System.currentTimeMillis()
                    onEvent(
                        ViewerEvent.Delete(
                            ctx = vmParams.ctx,
                            dv = state.dataViewBlock.id,
                            viewer = action.viewer,
                            onResult = {
                                logEvent(
                                    state = state,
                                    analytics = analytics,
                                    event = ObjectStateAnalyticsEvent.REMOVE_VIEW,
                                    startTime = startTime,
                                    spaceParams = provideParams(vmParams.space.id)
                                )
                            }
                        )
                    )
                }
            }
            is ViewersWidgetUi.Action.Edit -> {
                widgetViewerId.value = action.id
                showViewerEditWidget()
            }
            is ViewersWidgetUi.Action.OnMove -> {
                if (action.from == action.to) return
                viewModelScope.launch {
                    val startTime = System.currentTimeMillis()
                    val type = action.currentViews[action.to].type
                    viewerDelegate.onEvent(
                        ViewerEvent.UpdatePosition(
                            ctx = vmParams.ctx,
                            dv = state.dataViewBlock.id,
                            viewer = action.currentViews[action.to].id,
                            position = action.to,
                            onResult = {
                                logEvent(
                                    state = state,
                                    analytics = analytics,
                                    event = ObjectStateAnalyticsEvent.REPOSITION_VIEW,
                                    startTime = startTime,
                                    type = type.formattedName,
                                    spaceParams = provideParams(vmParams.space.id)
                                )
                            }
                        )
                    )
                    session.currentViewerId.value = action.currentViews.firstOrNull()?.id
                }
            }
            is ViewersWidgetUi.Action.SetActive -> {
                val startTime = System.currentTimeMillis()
                viewModelScope.launch {
                    onEvent(ViewerEvent.SetActive(
                        viewer = action.id,
                        onResult = {
                            viewersWidgetState.value = viewersWidgetState.value.copy(
                                showWidget = false,
                                isEditing = false
                            )
                            logEvent(
                                state = state,
                                analytics = analytics,
                                event = ObjectStateAnalyticsEvent.SWITCH_VIEW,
                                startTime = startTime,
                                type = action.type.formattedName,
                                spaceParams = provideParams(vmParams.space.id)
                            )
                        }
                    ))
                }
            }

            ViewersWidgetUi.Action.Plus -> {
                val activeView = state.viewerByIdOrFirst(session.currentViewerId.value) ?: return
                val newView = activeView.copy(
                    id = "",
                    name = "",
                    type = DVViewerType.GRID,
                    filters = emptyList(),
                    sorts = emptyList()
                )
                viewModelScope.launch {
                    val startTime = System.currentTimeMillis()
                    viewerDelegate.onEvent(
                        ViewerEvent.AddNew(
                            ctx = vmParams.ctx,
                            dv = state.dataViewBlock.id,
                            viewer = newView,
                            onResult = { newViewId ->
                                logEvent(
                                    state = state,
                                    analytics = analytics,
                                    event = ObjectStateAnalyticsEvent.ADD_VIEW,
                                    startTime = startTime,
                                    type = newView.type.formattedName,
                                    spaceParams = provideParams(vmParams.space.id)
                                )
                                widgetViewerId.value = newViewId
                                showViewerEditWidgetForNewView()
                            }
                        )
                    )
                }
            }
        }
    }

    fun openViewerFilters(viewerId: Id? = null) {
        val state = stateReducer.state.value.dataViewState() ?: return
        if (state.viewers.isNotEmpty()) {
            if (isRestrictionPresent(DataViewRestriction.VIEWS)) {
                toast(NOT_ALLOWED)
            } else {
                val viewer = viewerId ?: state.viewerByIdOrFirst(session.currentViewerId.value)?.id ?: return
                dispatch(
                    ObjectSetCommand.Modal.ModifyViewerFilters(
                        ctx = vmParams.ctx,
                        viewer = viewer
                    )
                )
            }
        } else {
            toast(DATA_VIEW_HAS_NO_VIEW_MSG)
        }
    }

    fun openViewerSorts(viewerId: Id? = null) {
        val state = stateReducer.state.value.dataViewState() ?: return
        if (state.viewers.isNotEmpty()) {
            if (isRestrictionPresent(DataViewRestriction.VIEWS)) {
                toast(NOT_ALLOWED)
            } else {
                val viewer = viewerId ?: state.viewerByIdOrFirst(session.currentViewerId.value)?.id ?: return
                dispatch(
                    ObjectSetCommand.Modal.ModifyViewerSorts(
                        ctx = vmParams.ctx,
                        viewer = viewer
                    )
                )
            }
        } else {
            toast(DATA_VIEW_HAS_NO_VIEW_MSG)
        }
    }

    fun onViewerEditWidgetAction(action: ViewEditAction) {
        Timber.d("onViewerEditWidgetAction, action:[$action]")
        when (action) {
            ViewEditAction.Dismiss -> { hideViewerEditWidget() }
            is ViewEditAction.Filters -> {
                viewModelScope.launch {
                    delay(DELAY_BEFORE_CREATING_TEMPLATE)
                    openViewerFilters(viewerId = action.id)
                }
            }
            is ViewEditAction.Layout -> {
                viewerLayoutWidgetState.value = viewerLayoutWidgetState.value.copy(
                    showWidget = true,
                    kanbanEnabled = isKanbanEnabled.value
                )
            }
            is ViewEditAction.Relations -> {
                viewModelScope.launch {
                    delay(DELAY_BEFORE_CREATING_TEMPLATE)
                    onViewerSettingsClicked(action.id)
                }
            }
            is ViewEditAction.Sorts -> {
                viewModelScope.launch {
                    delay(DELAY_BEFORE_CREATING_TEMPLATE)
                    openViewerSorts(viewerId = action.id)
                }
            }
            is ViewEditAction.UpdateName -> {
                val state = stateReducer.state.value.dataViewState() ?: return
                val viewer = state.viewerById(action.id) ?: return
                viewModelScope.launch {
                    viewerDelegate.onEvent(
                        ViewerEvent.UpdateView(
                            ctx = vmParams.ctx,
                            dv = state.dataViewBlock.id,
                            viewer = viewer.copy(name = action.name),
                            onResult = {}
                        )
                    )
                }
            }

            ViewEditAction.More -> {
                updateViewerEditMoreMenu()
            }
            is ViewEditAction.Delete -> {
                val state = stateReducer.state.value.dataViewState() ?: return
                hideViewerEditWidget()
                viewModelScope.launch {
                    val startTime = System.currentTimeMillis()
                    viewerDelegate.onEvent(
                        ViewerEvent.Delete(
                            ctx = vmParams.ctx,
                            dv = state.dataViewBlock.id,
                            viewer = action.id,
                            onResult = {
                                logEvent(
                                    state = state,
                                    analytics = analytics,
                                    event = ObjectStateAnalyticsEvent.REMOVE_VIEW,
                                    startTime = startTime,
                                    spaceParams = provideParams(vmParams.space.id)
                                )
                            }
                        )
                    )
                }
            }
            is ViewEditAction.Duplicate -> {
                val state = stateReducer.state.value.dataViewState() ?: return
                val viewer = state.viewerById(action.id) ?: return
                viewModelScope.launch {
                    val startTime = System.currentTimeMillis()
                    viewerDelegate.onEvent(
                        ViewerEvent.Duplicate(
                            ctx = vmParams.ctx,
                            dv = state.dataViewBlock.id,
                            viewer = viewer,
                            onResult = {
                                logEvent(
                                    state = state,
                                    analytics = analytics,
                                    event = ObjectStateAnalyticsEvent.DUPLICATE_VIEW,
                                    startTime = startTime,
                                    type = viewer.type.formattedName,
                                    spaceParams = provideParams(vmParams.space.id)
                                )
                            }
                        )
                    )
                }
            }
        }
    }

    private fun showViewerEditWidget() {
        val uiState = viewerEditWidgetState.value
        viewerEditWidgetState.value = when (uiState) {
            is ViewerEditWidgetUi.Data -> uiState.copy(showWidget = true)
            ViewerEditWidgetUi.Init -> uiState
        }
    }

    private fun showViewerEditWidgetForNewView() {
        val show = (viewerEditWidgetState.value as? ViewerEditWidgetUi.Data)?.copy(showWidget = true, isNewMode = true)
            ?: ViewerEditWidgetUi.Init
        viewerEditWidgetState.value = show
    }

    private fun hideViewerEditWidget() {
        widgetViewerId.value = null
        viewerEditWidgetState.value = ViewerEditWidgetUi.Init
    }

    private fun updateViewerEditMoreMenu() {
        when (val value = viewerEditWidgetState.value) {
            is ViewerEditWidgetUi.Data -> {
                val isMoreMenuVisible = value.showMore
                viewerEditWidgetState.value = value.copy(showMore = !isMoreMenuVisible)
            }
            ViewerEditWidgetUi.Init -> {}
        }
    }
    //endregion

    // region CREATE OBJECT
    fun proceedWithDataViewObjectCreate(typeChosenBy: TypeKey? = null, templateId: Id? = null) {
        Timber.d("proceedWithDataViewObjectCreate, typeChosenBy:[$typeChosenBy], templateId:[$templateId]")

        // Skip if already creating an object
        if (!isObjectCreationInProgress.compareAndSet(false, true)) {
            Timber.d("proceedWithDataViewObjectCreate: creation already in progress, skipping")
            return
        }

        if (isRestrictionPresent(DataViewRestriction.CREATE_OBJECT)) {
            isObjectCreationInProgress.set(false)
            toast(NOT_ALLOWED)
            return
        }

        val state = stateReducer.state.value.dataViewState()
        if (state == null) {
            isObjectCreationInProgress.set(false)
            return
        }

        viewModelScope.launch {
            try {
                when (state) {
                    is ObjectState.DataView.Collection -> {
                        proceedWithAddingObjectToCollection(
                            typeChosenByUser = typeChosenBy,
                            templateChosenBy = templateId
                        )
                    }

                    is ObjectState.DataView.Set -> {
                        proceedWithCreatingSetObject(
                            currentState = state,
                            templateChosenBy = templateId
                        )
                    }

                    is ObjectState.DataView.TypeSet -> {
                        proceedWithCreatingObjectTypeSetObject(
                            currentState = state,
                            templateChosenBy = templateId
                        )
                    }
                }
            } finally {
                isObjectCreationInProgress.set(false)
            }
        }
    }

    //region Viewer Layout Widget
    fun onViewerLayoutWidgetAction(action: ViewerLayoutWidgetUi.Action) {
        Timber.d("onViewerLayoutWidgetAction, action:[$action]")
        when (action) {
            ViewerLayoutWidgetUi.Action.Dismiss -> {
                val current = viewerLayoutWidgetState.value
                viewerLayoutWidgetState.value = when {
                    current.showCoverMenu -> current.copy(showCoverMenu = false)
                    current.showGroupByMenu -> current.copy(showGroupByMenu = false)
                    else -> current.copy(
                        showWidget = false,
                        showCardSize = false,
                        showCoverMenu = false,
                        showGroupByMenu = false
                    )
                }
            }
            ViewerLayoutWidgetUi.Action.CardSizeMenu -> {
                val isCardSizeMenuVisible = viewerLayoutWidgetState.value.showCardSize
                viewerLayoutWidgetState.value =
                    viewerLayoutWidgetState.value.copy(showCardSize = !isCardSizeMenuVisible)
            }
            ViewerLayoutWidgetUi.Action.CoverMenu -> {
                val isCoverMenuVisible = viewerLayoutWidgetState.value.showCoverMenu
                viewerLayoutWidgetState.value =
                    viewerLayoutWidgetState.value.copy(showCoverMenu = !isCoverMenuVisible)
            }
            is ViewerLayoutWidgetUi.Action.FitImage -> {
                viewModelScope.launch {
                    proceedWithUpdateViewer(
                        viewerId = viewerLayoutWidgetState.value.viewer
                    ) { it.copy(coverFit = action.toggled) }
                }
            }
            is ViewerLayoutWidgetUi.Action.Icon -> {
                viewModelScope.launch {
                    proceedWithUpdateViewer(
                        viewerId = viewerLayoutWidgetState.value.viewer
                    ) { it.copy(hideIcon = !action.toggled) }
                }
            }
            is ViewerLayoutWidgetUi.Action.CardSize -> {
                viewerLayoutWidgetState.value =
                    viewerLayoutWidgetState.value.copy(showCardSize = false)
                when (action.cardSize) {
                    ViewerLayoutWidgetUi.State.CardSize.Small -> {
                        viewModelScope.launch {
                            proceedWithUpdateViewer(
                                viewerId = viewerLayoutWidgetState.value.viewer
                            ) { it.copy(cardSize = DVViewerCardSize.SMALL) }
                        }
                    }
                    ViewerLayoutWidgetUi.State.CardSize.Large -> {
                        viewModelScope.launch {
                            proceedWithUpdateViewer(
                                viewerId = viewerLayoutWidgetState.value.viewer
                            ) { it.copy(cardSize = DVViewerCardSize.LARGE) }
                        }
                    }
                }
            }
            is ViewerLayoutWidgetUi.Action.ImagePreviewUpdate -> {
                when (action.item) {
                    is ViewerLayoutWidgetUi.State.ImagePreview.PageCover -> {
                        val itemIsChecked = action.item.isChecked
                        if (!itemIsChecked) {
                            viewModelScope.launch {
                                proceedWithUpdateViewer(
                                    viewerId = viewerLayoutWidgetState.value.viewer
                                ) { it.copy(coverRelationKey = action.item.relationKey.key) }
                            }
                        } else {
                            Timber.i("Page cover is already set")
                        }
                    }

                    is ViewerLayoutWidgetUi.State.ImagePreview.Custom -> {
                        val itemIsChecked = action.item.isChecked
                        if (!itemIsChecked) {
                            viewModelScope.launch {
                                proceedWithUpdateViewer(
                                    viewerId = viewerLayoutWidgetState.value.viewer
                                ) { it.copy(coverRelationKey = action.item.relationKey.key) }
                            }
                        } else {
                            Timber.i("Custom cover [${action.item.relationKey.key}] is already set")
                        }
                    }

                    is ViewerLayoutWidgetUi.State.ImagePreview.None -> {
                        val itemIsChecked = action.item.isChecked
                        if (!itemIsChecked) {
                            viewModelScope.launch {
                                proceedWithUpdateViewer(
                                    viewerId = viewerLayoutWidgetState.value.viewer
                                ) { it.copy(coverRelationKey = null) }
                            }
                        } else {
                            Timber.i("No cover is already set")
                        }
                    }
                }
            }
            is ViewerLayoutWidgetUi.Action.Type -> {
                viewModelScope.launch {
                    proceedWithUpdateViewer(
                        action = {
                            val startTime = System.currentTimeMillis()
                            viewModelScope.launch {
                                logEvent(
                                    state = stateReducer.state.value,
                                    analytics = analytics,
                                    event = ObjectStateAnalyticsEvent.CHANGE_VIEW_TYPE,
                                    startTime = startTime,
                                    type = action.type.formattedName,
                                    spaceParams = provideParams(vmParams.space.id)
                                )
                            }
                        },
                        viewerId = viewerLayoutWidgetState.value.viewer
                    ) { it.copy(type = action.type) }
                }
            }

            ViewerLayoutWidgetUi.Action.DismissCoverMenu -> viewerLayoutWidgetState.value =
                viewerLayoutWidgetState.value.copy(
                    showCoverMenu = false
                )

            is ViewerLayoutWidgetUi.Action.ColorColumns -> {
                viewModelScope.launch {
                    proceedWithUpdateViewer(
                        viewerId = viewerLayoutWidgetState.value.viewer
                    ) { it.copy(groupBackgroundColors = action.toggled) }
                }
            }

            ViewerLayoutWidgetUi.Action.GroupByMenu -> {
                val isGroupByMenuVisible = viewerLayoutWidgetState.value.showGroupByMenu
                viewerLayoutWidgetState.value =
                    viewerLayoutWidgetState.value.copy(showGroupByMenu = !isGroupByMenuVisible)
            }

            is ViewerLayoutWidgetUi.Action.GroupByUpdate -> {
                if (!action.item.isChecked) {
                    viewModelScope.launch {
                        proceedWithUpdateViewer(
                            viewerId = viewerLayoutWidgetState.value.viewer
                        ) { it.copy(groupRelationKey = action.item.relationKey.key) }
                    }
                } else {
                    Timber.i("Group-by relation [${action.item.relationKey.key}] is already set")
                }
                // Deliberate divergence from ImagePreviewUpdate: selecting a group-by relation
                // closes the picker, whereas the cover picker stays open after a selection.
                viewerLayoutWidgetState.value =
                    viewerLayoutWidgetState.value.copy(showGroupByMenu = false)
            }
        }
    }

    private suspend fun proceedWithUpdateViewer(
        action: () -> Unit = {},
        viewerId: Id?,
        update: (DVViewer) -> DVViewer
    ) {
        val state = stateReducer.state.value.dataViewState() ?: return
        val viewer = state.viewerById(viewerId)
        if (viewer == null) {
            Timber.e("Couldn't find viewer by id: ${viewerLayoutWidgetState.value.viewer}")
            return
        }
        viewerDelegate.onEvent(
            ViewerEvent.UpdateView(
                ctx = vmParams.ctx,
                dv = state.dataViewBlock.id,
                viewer = update.invoke(viewer),
                onResult = action
            )
        )
    }
    //endregion

    //region SYNC STATUS
    val spaceSyncStatus = MutableStateFlow<SpaceSyncAndP2PStatusState>(SpaceSyncAndP2PStatusState.Init)
    val syncStatusWidget = MutableStateFlow<SyncStatusWidgetState>(SyncStatusWidgetState.Hidden)

    fun onSyncStatusBadgeClicked() {
        Timber.d("onSyncStatusBadgeClicked, ")
        syncStatusWidget.value = spaceSyncStatus.value.toSyncStatusWidgetState()
    }

    private fun proceedWithObservingSyncStatus() {
        jobs += viewModelScope.launch {
            spaceSyncAndP2PStatusProvider
                .observe()
                .catch {
                    Timber.e(it, "Error while observing sync status")
                }
                .collect { syncAndP2pState ->
                    spaceSyncStatus.value = syncAndP2pState
                    syncStatusWidget.value = syncStatusWidget.value.updateStatus(syncAndP2pState)
                }
        }
    }

    fun onSyncWidgetDismiss() {
        syncStatusWidget.value = SyncStatusWidgetState.Hidden
    }

    fun onUpdateAppClick() {
        dispatch(command = ObjectSetCommand.Intent.OpenAppStore)
    }
    //endregion

    //region Date Field
    private suspend fun handleReadOnlyValue(
        timeInMillis: TimeInMillis?,
    ) {
        if ((timeInMillis != null)) {
            fieldParser.getDateObjectByTimeInSeconds(
                timeInSeconds = timeInMillis / 1000,
                spaceId = vmParams.space,
                actionSuccess = { obj ->
                    proceedWithNavigation(obj = obj)
                },
                actionFailure = {
                    toast("Error while parsing date object")
                    Timber.e(it, "Error while parsing date object")
                }
            )
        } else {
            toast("Date is not set")
        }
    }

    fun onOpenDateObjectByTimeInMillis(timeInMillis: TimeInMillis) {
        Timber.d("onOpenDateObjectByTimeInMillis, timeInMillis:[$timeInMillis]")
        viewModelScope.launch {
            handleReadOnlyValue(timeInMillis = timeInMillis)
        }
    }
    //endregion

    //region SET OBJECT NAME BOTTOM SHEET

    /**
     * Shows the set object name bottom sheet for a newly created object.
     */
    fun showSetObjectNameSheet(
        objectId: Id,
        icon: ObjectIcon,
        isIconChangeAllowed: Boolean,
        targetBlockId: Id? = null,
        name: String
    ) {
        _setObjectNameState.value = SetObjectNameState(
            isVisible = true,
            targetObjectId = objectId,
            currentIcon = icon,
            inputText = name,
            isIconChangeAllowed = isIconChangeAllowed,
            targetBlockId = targetBlockId,
        )
    }

    /**
     * Called when user types in the name field. Auto-saves to backend.
     */
    fun onSetObjectNameChanged(text: String) {
        val state = _setObjectNameState.value
        val targetId = state.targetObjectId ?: return

        _setObjectNameState.value = state.copy(inputText = text)

        viewModelScope.launch {
            if (state.targetBlockId != null) {
                // Note object: update text block content
                updateText(
                    UpdateText.Params(
                        context = targetId,
                        target = state.targetBlockId,
                        text = text,
                        marks = emptyList()
                    )
                ).process(
                    failure = { Timber.e(it, "Error while updating note text block") },
                    success = { /* saved successfully */ }
                )
            } else {
                // Other layouts: update name relation
                setObjectDetails(
                    UpdateDetail.Params(
                        target = targetId,
                        key = Relations.NAME,
                        value = text
                    )
                ).process(
                    failure = { Timber.e(it, "Error while updating object name") },
                    success = { /* saved successfully */ }
                )
            }
        }
    }

    /**
     * Called when bottom sheet is dismissed (Done pressed or swipe).
     * Triggers scroll to the object.
     */
    fun onSetObjectNameDismissed() {
        val objectId = _setObjectNameState.value.targetObjectId
        _setObjectNameState.value = SetObjectNameState()

        if (objectId != null) {
            // Dispatch scroll command directly instead of using pendingScrollToObject
            dispatch(ObjectSetCommand.ScrollToObject(objectId))
        }
    }

    /**
     * Opens the icon picker for the newly created object.
     */
    fun onSetObjectNameIconClicked() {
        Timber.d("onSetObjectNameIconClicked, ")
        val objectId = _setObjectNameState.value.targetObjectId ?: return
        dispatch(
            ObjectSetCommand.Modal.OpenIconActionMenu(
                target = objectId,
                space = vmParams.space.id
            )
        )
    }

    /**
     * Opens the object in full editor from the name sheet.
     */
    fun onSetObjectNameOpenClicked() {
        val state = _setObjectNameState.value
        val targetId = state.targetObjectId ?: return

        _setObjectNameState.value = SetObjectNameState()

        viewModelScope.launch {
            proceedWithOpeningObject(
                target = targetId,
                layout = null,
                space = vmParams.space.id
            )
        }
    }

    //endregion

    //region DISCUSSION

    /**
     * Pulls the existing discussion id (if any) off the current object's
     * details and updates the bottom-bar state.
     *
     * Mirrors EditorViewModel's discussion-state derivation so the same
     * Discussion button works across editor / set / type screens.
     */
    private fun updateDiscussionButtonState(state: ObjectState.DataView) {
        val currentObj = state.details.getObject(vmParams.ctx)
        // Object types don't support discussions — keep the button hidden
        // for the type screen embedded in WithSetScreen.
        if (currentObj?.layout == ObjectType.Layout.OBJECT_TYPE) {
            _discussionButtonState.value = DiscussionButtonState.Hidden
            return
        }
        val existingDiscussionId = currentObj
            ?.getSingleValue<String>(Relations.DISCUSSION_ID)
            ?.takeIf { it.isNotEmpty() }
        _discussionButtonState.value = if (existingDiscussionId != null) {
            DiscussionButtonState.Comments(discussionId = existingDiscussionId)
        } else {
            DiscussionButtonState.Empty
        }
    }

    fun onDiscussionButtonClicked() {
        when (val state = discussionButtonState.value) {
            is DiscussionButtonState.Comments -> {
                navigate(
                    EventWrapper(
                        AppNavigation.Command.OpenDiscussion(
                            target = state.discussionId,
                            space = vmParams.space.id
                        )
                    )
                )
            }
            is DiscussionButtonState.Empty -> {
                viewModelScope.launch {
                    addDiscussion.async(vmParams.ctx).fold(
                        onSuccess = { discussionId ->
                            _discussionButtonState.value = DiscussionButtonState.Comments(
                                discussionId = discussionId
                            )
                            navigate(
                                EventWrapper(
                                    AppNavigation.Command.OpenDiscussion(
                                        target = discussionId,
                                        space = vmParams.space.id
                                    )
                                )
                            )
                        },
                        onFailure = { e ->
                            Timber.e(e, "Failed to create discussion")
                            toast("Failed to create discussion")
                        }
                    )
                }
            }
            DiscussionButtonState.Hidden -> {
                // No-op: button is hidden, click shouldn't reach the VM.
            }
        }
    }

    sealed class DiscussionButtonState {
        data object Hidden : DiscussionButtonState()
        data object Empty : DiscussionButtonState()
        data class Comments(val discussionId: Id) : DiscussionButtonState()
    }

    //endregion

    companion object {
        const val NOT_ALLOWED = "Not allowed for this set"
        const val NOT_ALLOWED_CELL = "Not allowed for this cell"
        const val DATA_VIEW_HAS_NO_VIEW_MSG = "Data view has no view."
        const val TOAST_SET_NOT_EXIST = "This object doesn't exist"
        const val DELAY_BEFORE_CREATING_TEMPLATE = 200L
        private const val SUBSCRIPTION_TEMPLATES_ID = "-SUBSCRIPTION_TEMPLATES_ID"
        private const val SUBSCRIPTION_BOARD_OPTIONS_POSTFIX = "-board-options"
        private const val SUBSCRIPTION_DATA_VIEW_OPTIONS_POSTFIX = "-dataview-options"
        const val BOARD_SUBSCRIPTION_ERROR_MSG = "Couldn't load the board. Please reopen the object."
    }

    data class Params(
        val ctx: Id,
        val space: SpaceId
    )

    data class Query(
        val state: ObjectState,
        val offset: Long,
        val currentViewerId: Id?
    )

    /**
     * Snapshot of the [Query] parts that determine the backend record subscription
     * params built by [DataViewSubscription] (plus the active view's type, which
     * selects the subscription strategy in [subscribeToObjectState]). Two queries
     * with equal fingerprints produce an identical subscription, so cancelling and
     * re-creating it would only waste a middleware round-trip.
     */
    private data class SubscriptionFingerprint(
        val kind: String,
        val isInitialized: Boolean,
        val viewerId: Id?,
        val viewerType: DVViewerType?,
        val filters: List<DVFilter>,
        val sorts: List<DVSort>,
        val relationKeys: List<Key>,
        val sources: List<Id>,
        val offset: Long,
        /**
         * Relation formats the subscription params embed for the viewer's filter/sort keys,
         * as currently resolvable from storeOfRelations ([DataViewSubscription] resolves them
         * via updateFormatForSubscription / updateWithRelationFormat, falling back to
         * LONG_TEXT for relations not yet in the store). Including them ensures a
         * re-subscription once the store catches up.
         */
        val resolvedFormats: List<RelationFormat?>
    )

    private suspend fun Query.subscriptionFingerprint(): SubscriptionFingerprint {
        val dataView = state as? ObjectState.DataView
        val isInitialized = dataView?.isInitialized == true
        val viewer = if (isInitialized) dataView?.viewerByIdOrFirst(currentViewerId) else null
        val sources = if (isInitialized) {
            when (dataView) {
                is ObjectState.DataView.Set -> dataView.filterOutDeletedAndMissingObjects(
                    dataView.getSetOfValue(vmParams.ctx)
                )
                is ObjectState.DataView.TypeSet -> dataView.filterOutDeletedAndMissingObjects(
                    dataView.getSetOfValue(vmParams.ctx)
                )
                else -> emptyList()
            }
        } else {
            emptyList()
        }
        // Mirrors the keys whose formats DataViewSubscription resolves when building params:
        // every (leaf) filter's relation — advanced filters resolve their nested filters, like
        // updateFormatForSubscription — plus every sort's relation, or the default createdDate
        // sort substituted when the viewer has none (getSortsWithDefaultCreatedDate).
        val formatKeys = buildList {
            fun collectFilterKeys(filters: List<DVFilter>) {
                filters.forEach { filter ->
                    if (filter.isAdvanced()) {
                        collectFilterKeys(filter.nestedFilters)
                    } else {
                        add(filter.relation)
                    }
                }
            }
            collectFilterKeys(viewer?.filters.orEmpty())
            if (viewer != null) {
                if (viewer.sorts.isEmpty()) {
                    add(Relations.CREATED_DATE)
                } else {
                    viewer.sorts.forEach { add(it.relationKey) }
                }
            }
        }
        val resolvedFormats = formatKeys.map { key -> storeOfRelations.getByKey(key)?.format }
        return SubscriptionFingerprint(
            kind = when (dataView) {
                is ObjectState.DataView.Collection -> "collection"
                is ObjectState.DataView.Set -> "set"
                is ObjectState.DataView.TypeSet -> "typeSet"
                null -> "none"
            },
            isInitialized = isInitialized,
            viewerId = viewer?.id,
            viewerType = viewer?.type,
            filters = viewer?.filters.orEmpty(),
            sorts = viewer?.sorts.orEmpty(),
            relationKeys = if (isInitialized) {
                dataView?.dataViewContent?.relationLinks?.map { it.key }.orEmpty()
            } else {
                emptyList()
            },
            sources = sources,
            offset = offset,
            resolvedFormats = resolvedFormats
        )
    }
}