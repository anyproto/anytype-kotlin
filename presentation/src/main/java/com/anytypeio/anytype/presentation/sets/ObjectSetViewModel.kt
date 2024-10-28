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
import com.anytypeio.anytype.core_models.DVViewer
import com.anytypeio.anytype.core_models.DVViewerCardSize
import com.anytypeio.anytype.core_models.DVViewerType
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.isDataView
import com.anytypeio.anytype.core_models.multiplayer.SpaceMemberPermissions
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncAndP2PStatusState
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.primitives.TypeId
import com.anytypeio.anytype.core_models.primitives.TypeKey
import com.anytypeio.anytype.core_models.restrictions.DataViewRestriction
import com.anytypeio.anytype.core_utils.common.EventWrapper
import com.anytypeio.anytype.core_utils.ext.cancel
import com.anytypeio.anytype.domain.auth.interactor.ClearLastOpenedObject
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.Result
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.block.interactor.UpdateText
import com.anytypeio.anytype.domain.block.interactor.sets.GetObjectTypes
import com.anytypeio.anytype.domain.collections.AddObjectToCollection
import com.anytypeio.anytype.domain.cover.SetDocCoverImage
import com.anytypeio.anytype.domain.dataview.interactor.CreateDataViewObject
import com.anytypeio.anytype.domain.error.Error
import com.anytypeio.anytype.domain.event.interactor.InterceptEvents
import com.anytypeio.anytype.domain.event.interactor.SpaceSyncAndP2PStatusProvider
import com.anytypeio.anytype.domain.library.StoreSearchByIdsParams
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.DateProvider
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider
import com.anytypeio.anytype.domain.`object`.ConvertObjectToCollection
import com.anytypeio.anytype.domain.`object`.DuplicateObjects
import com.anytypeio.anytype.domain.`object`.UpdateDetail
import com.anytypeio.anytype.domain.objects.ObjectStore
import com.anytypeio.anytype.domain.objects.SetObjectListIsArchived
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.page.CloseBlock
import com.anytypeio.anytype.domain.page.CreateObject
import com.anytypeio.anytype.domain.search.DataViewState
import com.anytypeio.anytype.domain.search.DataViewSubscriptionContainer
import com.anytypeio.anytype.domain.sets.OpenObjectSet
import com.anytypeio.anytype.domain.sets.SetQueryToObjectSet
import com.anytypeio.anytype.domain.templates.CreateTemplate
import com.anytypeio.anytype.domain.unsplash.DownloadUnsplashImage
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import com.anytypeio.anytype.presentation.common.Action
import com.anytypeio.anytype.presentation.common.Delegator
import com.anytypeio.anytype.presentation.editor.cover.CoverImageHashProvider
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.model.TextUpdate
import com.anytypeio.anytype.presentation.extension.ObjectStateAnalyticsEvent
import com.anytypeio.anytype.presentation.extension.logEvent
import com.anytypeio.anytype.presentation.extension.sendAnalyticsObjectCreateEvent
import com.anytypeio.anytype.presentation.extension.sendAnalyticsRelationEvent
import com.anytypeio.anytype.presentation.home.HomeScreenViewModel.Companion.HOME_SCREEN_PROFILE_OBJECT_SUBSCRIPTION
import com.anytypeio.anytype.presentation.mapper.toTemplateObjectTypeViewItems
import com.anytypeio.anytype.presentation.navigation.AppNavigation
import com.anytypeio.anytype.presentation.navigation.SupportNavigation
import com.anytypeio.anytype.presentation.objects.SupportedLayouts
import com.anytypeio.anytype.presentation.objects.getCreateObjectParams
import com.anytypeio.anytype.presentation.objects.isCreateObjectAllowed
import com.anytypeio.anytype.presentation.objects.isTemplatesAllowed
import com.anytypeio.anytype.presentation.profile.ProfileIconView
import com.anytypeio.anytype.presentation.profile.profileIcon
import com.anytypeio.anytype.presentation.relations.ObjectRelationView
import com.anytypeio.anytype.presentation.relations.ObjectSetConfig.DEFAULT_LIMIT
import com.anytypeio.anytype.presentation.relations.RelationListViewModel
import com.anytypeio.anytype.presentation.relations.render
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import com.anytypeio.anytype.presentation.sets.model.CellView
import com.anytypeio.anytype.presentation.sets.model.Viewer
import com.anytypeio.anytype.presentation.sets.state.ObjectState
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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber

class ObjectSetViewModel(
    private val vmParams: Params,
    private val permissions: UserPermissionProvider,
    private val database: ObjectSetDatabase,
    private val openObjectSet: OpenObjectSet,
    private val closeBlock: CloseBlock,
    private val setObjectDetails: UpdateDetail,
    private val downloadUnsplashImage: DownloadUnsplashImage,
    private val setDocCoverImage: SetDocCoverImage,
    private val updateText: UpdateText,
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
    private val objectToCollection: ConvertObjectToCollection,
    private val storeOfObjectTypes: StoreOfObjectTypes,
    private val getObjectTypes: GetObjectTypes,
    private val duplicateObjects: DuplicateObjects,
    private val templatesContainer: ObjectTypeTemplatesContainer,
    private val setObjectListIsArchived: SetObjectListIsArchived,
    private val spaceManager: SpaceManager,
    private val viewerDelegate: ViewerDelegate,
    private val createTemplate: CreateTemplate,
    private val storelessSubscriptionContainer: StorelessSubscriptionContainer,
    private val dispatchers: AppCoroutineDispatchers,
    private val dateProvider: DateProvider,
    private val analyticSpaceHelperDelegate: AnalyticSpaceHelperDelegate,
    private val spaceSyncAndP2PStatusProvider: SpaceSyncAndP2PStatusProvider,
    private val clearLastOpenedObject: ClearLastOpenedObject,
) : ViewModel(), SupportNavigation<EventWrapper<AppNavigation.Command>>,
    ViewerDelegate by viewerDelegate,
    AnalyticSpaceHelperDelegate by analyticSpaceHelperDelegate
{

    val icon = MutableStateFlow<ProfileIconView>(ProfileIconView.Loading)

    val permission = MutableStateFlow<SpaceMemberPermissions?>(SpaceMemberPermissions.NO_PERMISSIONS)

    private val isOwnerOrEditor get() = permission.value?.isOwnerOrEditor() ==  true

    val error = MutableStateFlow<String?>(null)

    val featured = MutableStateFlow<BlockView.FeaturedRelation?>(null)

    override val navigation = MutableLiveData<EventWrapper<AppNavigation.Command>>()

    private val titleUpdateChannel = Channel<TextUpdate>()

    private val defaultPayloadConsumer: suspend (Payload) -> Unit = { payload ->
        stateReducer.dispatch(payload.events)
    }

    val pagination get() = paginator.pagination

    private val jobs = mutableListOf<Job>()

    private val _commands = MutableSharedFlow<ObjectSetCommand>(replay = 0)
    val commands: SharedFlow<ObjectSetCommand> = _commands
    val toasts = MutableSharedFlow<String>(replay = 0)

    private val _currentViewer: MutableStateFlow<DataViewViewState> =
        MutableStateFlow(DataViewViewState.Init)
    val currentViewer = _currentViewer

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

    private var context: Id = ""

    private val selectedTypeFlow: MutableStateFlow<ObjectWrapper.Type?> = MutableStateFlow(null)

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
                    featured.value = state.featuredRelations(
                        ctx = vmParams.ctx,
                        urlBuilder = urlBuilder,
                        relations = storeOfRelations.getAll()
                    )
                    _header.value = state.header(
                        ctx = vmParams.ctx,
                        urlBuilder = urlBuilder,
                        coverImageHashProvider = coverImageHashProvider,
                        isReadOnlyMode = permission == SpaceMemberPermissions.NO_PERMISSIONS || permission == SpaceMemberPermissions.READER
                    )
                }
        }

        subscribeToObjectState()
        subscribeToDataViewViewer()

        viewModelScope.launch {
            dispatcher.flow().collect { defaultPayloadConsumer(it) }
        }

        viewModelScope.launch {
            dataViewSubscriptionContainer.counter.collect { counter ->
                Timber.d("SET-DB: counter —>\n$counter")
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
                .filter { context.isNotEmpty() }
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
                            session = session
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

        subscribeToSelectedType()
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

    private fun proceedWithObservingProfileIcon() {
        viewModelScope.launch {
            val config = spaceManager.getConfig(vmParams.space)
            if (config != null) {
                storelessSubscriptionContainer.subscribe(
                    StoreSearchByIdsParams(
                        space = SpaceId(config.techSpace),
                        subscription = HOME_SCREEN_PROFILE_OBJECT_SUBSCRIPTION,
                        targets = listOf(config.profile),
                        keys = listOf(
                            Relations.ID,
                            Relations.SPACE_ID,
                            Relations.NAME,
                            Relations.ICON_EMOJI,
                            Relations.ICON_IMAGE,
                            Relations.ICON_OPTION
                        )
                    )
                ).map { result ->
                    val obj = result.firstOrNull()
                    obj?.profileIcon(urlBuilder) ?: ProfileIconView.Placeholder(null)
                }.catch {
                    Timber.e(it, "Error while observing space icon")
                }.flowOn(dispatchers.io).collect { icon.value = it }
            } else {
                Timber.w("Config not found to get profile object id")
            }
        }
    }

    private suspend fun proceedWithSettingUnsplashImage(
        action: Action.SetUnsplashImage
    ) {
        downloadUnsplashImage(
            DownloadUnsplashImage.Params(
                picture = action.img,
                space = vmParams.space
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

    fun onStart(ctx: Id, space: Id, view: Id? = null) {
        Timber.d("onStart, ctx:[$ctx], view:[$view]")
        this.context = ctx
        if (view != null) {
            session.currentViewerId.value = view
        }
        subscribeToEvents(ctx = ctx)
        proceedWithOpeningCurrentObject(ctx = ctx)
        proceedWithObservingProfileIcon()
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
        Timber.d("subscribeToObjectState, ctx:[$context]")
        viewModelScope.launch {
            combine(
                stateReducer.state,
                paginator.offset,
                session.currentViewerId,
            ) { state, offset, view ->
                Query(
                    state = state,
                    offset = offset,
                    currentViewerId = view
                )
            }.flatMapLatest { query  ->
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
                                dataViewRelationLinks = query.state.dataViewContent.relationLinks
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
                                dataViewRelationLinks = query.state.dataViewContent.relationLinks
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
                                        dispatch(AppNavigation.Command.Exit)
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

    private fun subscribeToDataViewViewer() {
        Timber.d("subscribeToDataViewViewer, START SUBSCRIPTION by ctx:[$context]")
        viewModelScope.launch {
            combine(
                database.index,
                stateReducer.state,
                session.currentViewerId,
                permission
            ) { dataViewState, objectState, currentViewId, permission ->
                processViewState(dataViewState, objectState, currentViewId, permission)
            }.distinctUntilChanged().collect { viewState ->
                Timber.d("subscribeToDataViewViewer, newViewerState:[$viewState]")
                _currentViewer.value = viewState
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
        }
    }

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
                    ctx = context,
                    session = session,
                    storeOfRelations = storeOfRelations
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
                    viewer.isEmpty() -> {
                        val isCreateObjectAllowed = objectState.isCreateObjectAllowed() && permission?.isOwnerOrEditor() == true
                        DataViewViewState.Collection.NoItems(
                            title = viewer.title,
                            isCreateObjectAllowed = isCreateObjectAllowed,
                            isEditingViewAllowed = permission?.isOwnerOrEditor() == true
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

        val setOfValue = objectState.getSetOfValue(ctx = context)
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
                    ctx = context,
                    session = session,
                    storeOfRelations = storeOfRelations
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
                    storeOfRelations = storeOfRelations
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
                    render.isEmpty() -> {
                        val (defType, _) = objectState.getActiveViewTypeAndTemplate(
                            context, viewer, storeOfObjectTypes
                        )
                        DataViewViewState.Set.NoItems(
                            title = render.title,
                            isCreateObjectAllowed = objectState.isCreateObjectAllowed(defType) && (permission?.isOwnerOrEditor() == true),
                            isEditingViewAllowed = permission?.isOwnerOrEditor() == true
                        )
                    }
                    else -> {
                        val (defType, _) = objectState.getActiveViewTypeAndTemplate(
                            context, viewer, storeOfObjectTypes
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
                storeOfRelations = storeOfRelations
            )
        }
    }

    fun onStop() {
        Timber.d("onStop, ")
        hideTemplatesWidget()
        unsubscribeFromAllSubscriptions()
        jobs.cancel()
    }

    fun onSystemBackPressed() {
        Timber.d("onSystemBackPressed, ")
        proceedWithClosingAndExit()
    }

    private fun unsubscribeFromAllSubscriptions() {
        viewModelScope.launch {
            val ids = listOf(
                getDataViewSubscriptionId(context),
                HOME_SCREEN_PROFILE_OBJECT_SUBSCRIPTION,
                "$context$SUBSCRIPTION_TEMPLATES_ID"
            )
            dataViewSubscription.unsubscribe(ids)
        }
    }

    private fun proceedWithClosingAndExit() {
        viewModelScope.launch {
            closeBlock.async(context).fold(
                onSuccess = { dispatch(AppNavigation.Command.Exit) },
                onFailure = {
                    Timber.e(it, "Error while closing object set: $context").also {
                        dispatch(AppNavigation.Command.Exit)
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
            if (context.isNotEmpty()) {
                viewModelScope.launch {
                    setObjectDetails(
                        UpdateDetail.Params(
                            target = context,
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
            } else {
                Timber.w("Skipping dispatching title update, because set of objects was not ready.")
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
                    target = context,
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
                            ctx = context,
                            relationKey = cell.relationKey,
                            recordId = cell.id,
                            space = vmParams.space.id
                        )
                    )
                }
                is CellView.Date -> {
                    dispatch(
                        ObjectSetCommand.Modal.EditGridDateCell(
                            ctx = context,
                            objectId = cell.id,
                            relationKey = cell.relationKey,
                            space = vmParams.space.id
                        )
                    )
                }
                is CellView.Tag, is CellView.Status -> {
                    dispatch(
                        ObjectSetCommand.Modal.EditTagOrStatusCell(
                            ctx = context,
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
                                ctx = context,
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
                    target = target,
                    layout = obj.layout,
                    space = vmParams.space.id,
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

    fun onNewButtonIconClicked() {
        Timber.d("onNewButtonIconClicked, ")
        showTypeTemplatesWidgetForObjectCreation()
    }

    // TODO Multispaces refactor this method
    private suspend fun proceedWithCreatingSetObject(currentState: ObjectState.DataView.Set, templateChosenBy: Id?) {
        if (isRestrictionPresent(DataViewRestriction.CREATE_OBJECT)) {
            toast(NOT_ALLOWED)
        } else {
            val setObject = ObjectWrapper.Basic(
                currentState.details[context]?.map ?: emptyMap()
            )
            val viewer = currentState.viewerByIdOrFirst(session.currentViewerId.value)
            if (viewer == null) {
                Timber.e("onCreateNewDataViewObject, Viewer is empty")
                return
            }

            val (defaultObjectType, defaultTemplate) = currentState.getActiveViewTypeAndTemplate(
                ctx = context,
                activeView = viewer,
                storeOfObjectTypes = storeOfObjectTypes
            )

            val objectTypeUniqueKey = defaultObjectType?.uniqueKey

            val sourceId = setObject.setOf.singleOrNull()
            if (objectTypeUniqueKey == null) {
                toast("Unable to define a source for a new object.")
            } else {
                val sourceDetails = currentState.details[sourceId]
                if (sourceDetails != null && sourceDetails.map.isNotEmpty()) {
                    val wrapper = ObjectWrapper.Basic(sourceDetails.map)
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
                                            ctx = context,
                                            space = requireNotNull(wrapper.spaceId)
                                        )
                                )
                            } else {
                                val validTemplateId = templateChosenBy ?: defaultTemplate
                                val dvRelationLinks = currentState.dataViewContent.relationLinks
                                val prefilled = viewer.prefillNewObjectDetails(
                                    storeOfRelations = storeOfRelations,
                                    dateProvider = dateProvider,
                                    dataViewRelationLinks = dvRelationLinks
                                )
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
                                        ctx = context,
                                        space = requireNotNull(wrapper.spaceId)
                                    )
                                )
                            } else {
                                val validTemplateId = templateChosenBy ?: defaultTemplate
                                val prefilled = viewer.resolveSetByRelationPrefilledObjectData(
                                    storeOfRelations = storeOfRelations,
                                    dateProvider = dateProvider,
                                    dataViewRelationLinks = currentState.dataViewContent.relationLinks,
                                    objSetByRelation = ObjectWrapper.Relation(sourceDetails.map)
                                )
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

    fun onSelectQueryButtonClicked() {
        dispatch(ObjectSetCommand.Modal.OpenEmptyDataViewSelectQueryScreen)
    }

    private suspend fun proceedWithAddingObjectToCollection(
        typeChosenByUser: TypeKey? = null,
        templateChosenBy: Id? = null
    ) {
        val state = stateReducer.state.value.dataViewState() ?: return
        val viewer = state.viewerByIdOrFirst(session.currentViewerId.value) ?: return

        val (defaultObjectType, defaultTemplate) = state.getActiveViewTypeAndTemplate(
            ctx = context,
            activeView = viewer,
            storeOfObjectTypes = storeOfObjectTypes
        )

        val defaultObjectTypeUniqueKey = defaultObjectType?.uniqueKey?.let {
            TypeKey(it)
        }

        if (typeChosenByUser == null && defaultObjectTypeUniqueKey == null) {
            toast("Could not define type for new object")
            return
        }

        val validTemplateId = templateChosenBy ?: defaultTemplate
        val prefilled = viewer.prefillNewObjectDetails(
            storeOfRelations = storeOfRelations,
            dateProvider = dateProvider,
            dataViewRelationLinks = state.dataViewContent.relationLinks
        )
        val type = typeChosenByUser ?: defaultObjectTypeUniqueKey!!
        val createObjectParams = CreateDataViewObject.Params.Collection(
            template = validTemplateId,
            type = type,
            filters = viewer.filters,
            prefilled = prefilled
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
                    ctx = context,
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
        if (obj.layout == ObjectType.Layout.NOTE) {
            proceedWithOpeningObject(
                target = response.objectId,
                layout = obj.layout,
                space = vmParams.space.id
            )
        } else {
            dispatch(
                ObjectSetCommand.Modal.SetNameForCreatedObject(
                    ctx = context,
                    target = response.objectId,
                    space = vmParams.space.id
                )
            )
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
        val struct = state.details[context]?.map ?: return
        val wrapper = ObjectWrapper.Basic(struct)
        Timber.d("Wrapper: $wrapper")
        val space = wrapper.spaceId
        if (space != null) {
            dispatch(
                ObjectSetCommand.Modal.Menu(
                    ctx = context,
                    space = space,
                    isArchived = state.details[context]?.isArchived ?: false,
                    isFavorite = state.details[context]?.isFavorite ?: false,
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
        val struct = state.details[context]
        val wrapper = ObjectWrapper.Basic(struct?.map.orEmpty())
        val space = wrapper.spaceId
        if (space != null) {
            dispatch(
                ObjectSetCommand.Modal.OpenIconActionMenu(
                    target = context,
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
                    ctx = context,
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
        if (target == context) {
            toast("You are already here")
            Timber.d("proceedWithOpeningObject, target == context")
            return
        }
        isCustomizeViewPanelVisible.value = false
        val navigateCommand = AppNavigation.Command.OpenObject(
            target = target,
            space = space
        )
        closeBlock.async(context).fold(
            onSuccess = { navigate(EventWrapper(navigateCommand)) },
            onFailure = {
                Timber.e(it, "Error while closing object set: $context")
                navigate(EventWrapper(navigateCommand))
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
        if (target == context) {
            toast("You are already here")
            Timber.d("proceedWithOpeningObject, target == context")
            return
        }
        isCustomizeViewPanelVisible.value = false
        jobs += viewModelScope.launch {
            closeBlock.async(context).fold(
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
                    Timber.e(it, "Error while closing object set: $context")
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

    private suspend fun proceedWithNavigation(
        target: Id,
        space: Id,
        layout: ObjectType.Layout?,
        identityProfileLink: Id? = null
    ) {
        if (target == context) {
            toast("You are already here")
            Timber.d("proceedWithOpeningObject, target == context")
            return
        }
        when (layout) {
            ObjectType.Layout.BASIC,
            ObjectType.Layout.TODO,
            ObjectType.Layout.NOTE,
            ObjectType.Layout.IMAGE,
            ObjectType.Layout.FILE,
            ObjectType.Layout.VIDEO,
            ObjectType.Layout.AUDIO,
            ObjectType.Layout.PDF,
            ObjectType.Layout.BOOKMARK,
            ObjectType.Layout.PARTICIPANT -> proceedWithOpeningObject(
                target = target,
                space = space
            )
            ObjectType.Layout.PROFILE -> proceedWithOpeningObject(
                target = identityProfileLink ?: target,
                space = space
            )
            ObjectType.Layout.SET, ObjectType.Layout.COLLECTION -> {
                closeBlock.async(context).fold(
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
                        Timber.e(it, "Error while closing object set: $context")
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
            else -> {
                toast("Unexpected layout: $layout")
                Timber.e("Unexpected layout: $layout")
            }
        }
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

    fun onHomeButtonClicked() {
        viewModelScope.launch {
            clearLastOpenedObject(ClearLastOpenedObject.Params(vmParams.space))
            closeBlock.async(context).fold(
                onSuccess = { dispatch(AppNavigation.Command.ExitToVault) },
                onFailure = {
                    Timber.e(it, "Error while closing object set: $context").also {
                        dispatch(AppNavigation.Command.ExitToVault)
                    }
                }
            )
        }
    }

    fun onBackButtonClicked() {
        proceedWithClosingAndExit()
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
            props = Props(mapOf(EventsPropertiesKey.route to EventsDictionary.Routes.navigation))
        )
        viewModelScope.launch {
            dispatch(
                AppNavigation.Command.OpenGlobalSearch(
                    space = vmParams.space.id
                )
            )
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
                                    val setOfValue = state.getSetOfValue(context)
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
                        }
                    }
                    else -> {
                        Timber.d("Ignoring click on relation, relation:[${clicked.relation}]")
                    }
                }
            }
            is ListenerType.Relation.Featured -> {
                onRelationClickedListMode(
                    ctx = context,
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
                    _commands.emit(
                        ObjectSetCommand.Modal.EditGridDateCell(
                            ctx = context,
                            objectId = context,
                            relationKey = relation.key,
                            space = vmParams.space.id
                        )
                    )
                }
                RelationFormat.STATUS,
                RelationFormat.TAG -> {
                    _commands.emit(
                        ObjectSetCommand.Modal.EditTagOrStatusRelationValue(
                            ctx = context,
                            relation = relation.key,
                            space = requireNotNull(relation.spaceId)
                        )
                    )
                }
                RelationFormat.FILE,
                RelationFormat.OBJECT -> {
                    _commands.emit(
                        ObjectSetCommand.Modal.EditObjectRelationValue(
                            ctx = context,
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
                ctx = context,
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
        val params = ConvertObjectToCollection.Params(ctx = context)
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
            val (type, _) = dataView.getActiveViewTypeAndTemplate(context, viewer, storeOfObjectTypes)
            if (type == null) return@launch
            typeTemplatesWidgetState.value = createState(viewer)
            selectedTypeFlow.value = type
        }
        logEvent(ObjectStateAnalyticsEvent.SHOW_TEMPLATES)
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
                        ObjectSetCommand.Modal.OpenSelectTypeScreen(
                            excludedTypes = emptyList()
                        )
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
                        subscription = "$context$SUBSCRIPTION_TEMPLATES_ID"
                    )
                }.map { templates ->
                    val state = stateReducer.state.value
                    val viewerId = typeTemplatesWidgetState.value.getWidgetViewerId()
                    val dataView = state.dataViewState() ?: return@map emptyList<TemplateView>()
                    val viewer = dataView.viewerById(viewerId) ?: return@map emptyList<TemplateView>()
                    val selectedTypeId = selectedTypeFlow.value?.id ?: return@map emptyList<TemplateView>()
                    val (type, template) = dataView.getActiveViewTypeAndTemplate(
                        context,
                        viewer,
                        storeOfObjectTypes
                    )
                    when (typeTemplatesWidgetState.value) {
                        is TypeTemplatesWidgetUI.Data -> {
                            if (type?.id == selectedTypeFlow.value?.id) {
                                processTemplates(
                                    templates = templates,
                                    viewerDefType = type ?: storeOfObjectTypes.get(selectedTypeId),
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
        val filters = ObjectSearchConstants.filterTypes(
            recommendedLayouts = SupportedLayouts.createObjectLayouts
        )
        val params = GetObjectTypes.Params(
            space = vmParams.space,
            filters = filters,
            keys = ObjectSearchConstants.defaultKeysObjectType
        )
        getObjectTypes.async(params).fold(
            onSuccess = { types ->
                val list = buildList {
                    add(TemplateObjectTypeView.Search)
                    addAll(types.toTemplateObjectTypeViewItems(selectedType))
                }
                typeTemplatesWidgetState.value = widgetState.copy(objectTypes = list)
            },
            onFailure = { error ->
                Timber.e(error, "Error while fetching object types")
                typeTemplatesWidgetState.value = widgetState.copy(objectTypes = emptyList())
            }
        )
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

    private fun processTemplates(
        templates: List<ObjectWrapper.Basic>,
        viewerDefType: ObjectWrapper.Type?,
        viewerDefTemplate: Id?
    ): List<TemplateView> {

        if (viewerDefType == null) {
            Timber.e("processTemplates, Viewer def type is null")
            return emptyList()
        }

        val viewerDefTypeId = viewerDefType.id
        val viewerDefTypeKey = TypeKey(viewerDefType.uniqueKey)

        val isTemplatesAllowed = viewerDefType.isTemplatesAllowed()

        typeTemplatesWidgetState.value = when (val state = typeTemplatesWidgetState.value) {
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

        val blankTemplate = listOf(
            TemplateView.Blank(
                id = TemplateView.DEFAULT_TEMPLATE_ID_BLANK,
                targetTypeId = TypeId(viewerDefTypeId),
                targetTypeKey = viewerDefTypeKey,
                layout = viewerDefType.recommendedLayout?.code ?: ObjectType.Layout.BASIC.code,
                isDefault = viewerDefTemplate.isNullOrEmpty()
                        || viewerDefTemplate == TemplateView.DEFAULT_TEMPLATE_ID_BLANK,
            )
        )
        return blankTemplate + templates.map { objTemplate ->
            objTemplate.toTemplateView(
                urlBuilder = urlBuilder,
                coverImageHashProvider = coverImageHashProvider,
                viewerDefTemplateId = viewerDefTemplate,
                viewerDefTypeKey = viewerDefTypeKey
            )
        } + newTemplate
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
                            ctx = context,
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
                if (action.to == 0 && session.currentViewerId.value.isNullOrEmpty()) {
                    state.dataViewContent.viewers.firstOrNull()?.let {
                        session.currentViewerId.value = it.id
                    }
                }
                viewModelScope.launch {
                    val startTime = System.currentTimeMillis()
                    val type = action.currentViews[action.to].type
                    viewerDelegate.onEvent(
                        ViewerEvent.UpdatePosition(
                            ctx = context,
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
                }
            }
            is ViewersWidgetUi.Action.SetActive -> {
                val startTime = System.currentTimeMillis()
                viewModelScope.launch {
                    onEvent(ViewerEvent.SetActive(
                        viewer = action.id,
                        onResult = {
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
                            ctx = context,
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
                        ctx = context,
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
                        ctx = context,
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
                viewerLayoutWidgetState.value = viewerLayoutWidgetState.value.copy(showWidget = true)
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
                            ctx = context,
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
                            ctx = context,
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
                            ctx = context,
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

        if (isRestrictionPresent(DataViewRestriction.CREATE_OBJECT)) {
            toast(NOT_ALLOWED)
            return
        }

        val state = stateReducer.state.value.dataViewState() ?: return

        viewModelScope.launch {
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
            }
        }
    }

    //region Viewer Layout Widget
    fun onViewerLayoutWidgetAction(action: ViewerLayoutWidgetUi.Action) {
        Timber.d("onViewerLayoutWidgetAction, action:[$action]")
        when (action) {
            ViewerLayoutWidgetUi.Action.Dismiss -> {
                val isCoverMenuVisible = viewerLayoutWidgetState.value.showCoverMenu
                viewerLayoutWidgetState.value = if (isCoverMenuVisible) {
                    viewerLayoutWidgetState.value.copy(showCoverMenu = false)
                } else {
                    viewerLayoutWidgetState.value.copy(
                        showWidget = false,
                        showCardSize = false,
                        showCoverMenu = false
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
                ctx = context,
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

    companion object {
        const val NOT_ALLOWED = "Not allowed for this set"
        const val NOT_ALLOWED_CELL = "Not allowed for this cell"
        const val DATA_VIEW_HAS_NO_VIEW_MSG = "Data view has no view."
        const val TOAST_SET_NOT_EXIST = "This object doesn't exist"
        const val DELAY_BEFORE_CREATING_TEMPLATE = 200L
        private const val SUBSCRIPTION_TEMPLATES_ID = "-SUBSCRIPTION_TEMPLATES_ID"
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
}