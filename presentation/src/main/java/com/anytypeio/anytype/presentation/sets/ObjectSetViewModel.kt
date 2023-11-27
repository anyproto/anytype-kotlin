package com.anytypeio.anytype.presentation.sets

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.core_models.DVViewer
import com.anytypeio.anytype.core_models.DVViewerCardSize
import com.anytypeio.anytype.core_models.DVViewerType
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.InternalFlags
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.SyncStatus
import com.anytypeio.anytype.core_models.primitives.TypeId
import com.anytypeio.anytype.core_models.primitives.TypeKey
import com.anytypeio.anytype.core_models.restrictions.DataViewRestriction
import com.anytypeio.anytype.core_models.restrictions.DataViewRestrictions
import com.anytypeio.anytype.core_utils.common.EventWrapper
import com.anytypeio.anytype.core_utils.ext.cancel
import com.anytypeio.anytype.domain.base.Result
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.block.interactor.UpdateText
import com.anytypeio.anytype.domain.block.interactor.sets.GetObjectTypes
import com.anytypeio.anytype.domain.collections.AddObjectToCollection
import com.anytypeio.anytype.domain.cover.SetDocCoverImage
import com.anytypeio.anytype.domain.dataview.interactor.CreateDataViewObject
import com.anytypeio.anytype.domain.error.Error
import com.anytypeio.anytype.domain.event.interactor.InterceptEvents
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.`object`.ConvertObjectToCollection
import com.anytypeio.anytype.domain.`object`.DuplicateObjects
import com.anytypeio.anytype.domain.`object`.UpdateDetail
import com.anytypeio.anytype.domain.objects.ObjectStore
import com.anytypeio.anytype.domain.objects.SetObjectListIsArchived
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.page.CloseBlock
import com.anytypeio.anytype.domain.page.CreateObject
import com.anytypeio.anytype.domain.search.CancelSearchSubscription
import com.anytypeio.anytype.domain.search.DataViewState
import com.anytypeio.anytype.domain.search.DataViewSubscriptionContainer
import com.anytypeio.anytype.domain.sets.OpenObjectSet
import com.anytypeio.anytype.domain.sets.SetQueryToObjectSet
import com.anytypeio.anytype.domain.status.InterceptThreadStatus
import com.anytypeio.anytype.domain.templates.CreateTemplate
import com.anytypeio.anytype.domain.unsplash.DownloadUnsplashImage
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.BuildConfig
import com.anytypeio.anytype.presentation.common.Action
import com.anytypeio.anytype.presentation.common.Delegator
import com.anytypeio.anytype.presentation.editor.cover.CoverImageHashProvider
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.model.TextUpdate
import com.anytypeio.anytype.presentation.extension.ObjectStateAnalyticsEvent
import com.anytypeio.anytype.presentation.extension.logEvent
import com.anytypeio.anytype.presentation.extension.sendAnalyticsObjectCreateEvent
import com.anytypeio.anytype.presentation.extension.sendAnalyticsRelationValueEvent
import com.anytypeio.anytype.presentation.mapper.toTemplateObjectTypeViewItems
import com.anytypeio.anytype.presentation.navigation.AppNavigation
import com.anytypeio.anytype.presentation.navigation.SupportNavigation
import com.anytypeio.anytype.presentation.objects.SupportedLayouts
import com.anytypeio.anytype.presentation.objects.isCreateObjectAllowed
import com.anytypeio.anytype.presentation.objects.isTemplatesAllowed
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
import com.anytypeio.anytype.presentation.sets.subscription.DefaultDataViewSubscription
import com.anytypeio.anytype.presentation.sets.viewer.ViewerDelegate
import com.anytypeio.anytype.presentation.sets.viewer.ViewerEvent
import com.anytypeio.anytype.presentation.sets.viewer.ViewerView
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import timber.log.Timber

class ObjectSetViewModel(
    private val database: ObjectSetDatabase,
    private val openObjectSet: OpenObjectSet,
    private val closeBlock: CloseBlock,
    private val setObjectDetails: UpdateDetail,
    private val downloadUnsplashImage: DownloadUnsplashImage,
    private val setDocCoverImage: SetDocCoverImage,
    private val updateText: UpdateText,
    private val interceptEvents: InterceptEvents,
    private val interceptThreadStatus: InterceptThreadStatus,
    private val dispatcher: Dispatcher<Payload>,
    private val delegator: Delegator<Action>,
    private val urlBuilder: UrlBuilder,
    private val coverImageHashProvider: CoverImageHashProvider,
    private val session: ObjectSetSession,
    private val analytics: Analytics,
    private val createDataViewObject: CreateDataViewObject,
    private val createObject: CreateObject,
    private val dataViewSubscriptionContainer: DataViewSubscriptionContainer,
    private val cancelSearchSubscription: CancelSearchSubscription,
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
    private val createTemplate: CreateTemplate
) : ViewModel(), SupportNavigation<EventWrapper<AppNavigation.Command>>, ViewerDelegate by viewerDelegate {

    val status = MutableStateFlow(SyncStatus.UNKNOWN)
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

    @Deprecated("could be deleted")
    val isLoading = MutableStateFlow(false)

    private var context: Id = ""

    private val selectedTypeFlow: MutableStateFlow<ObjectWrapper.Type?> = MutableStateFlow(null)

    init {
        Timber.d("ObjectSetViewModel, init")
        viewModelScope.launch {
            stateReducer.state
                .filterIsInstance<ObjectState.DataView>()
                .distinctUntilChanged()
                .collectLatest { state ->
                    featured.value = state.featuredRelations(
                        ctx = context,
                        urlBuilder = urlBuilder,
                        relations = storeOfRelations.getAll()
                    )
                    _header.value = state.header(
                        ctx = context,
                        urlBuilder = urlBuilder,
                        coverImageHashProvider = coverImageHashProvider
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
                        context = context,
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
                    is Action.OpenObject -> proceedWithOpeningObject(action.id)
                    is Action.OpenCollection -> proceedWithOpeningObjectCollection(action.id)
                    is Action.Duplicate -> proceedWithNavigation(
                        target = action.id,
                        layout = ObjectType.Layout.SET
                    )
                    else -> {}
                }
            }
        }

        viewModelScope.launch {
            _dvViews.collectLatest {
                viewersWidgetState.value = viewersWidgetState.value.copy(items = it)
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
                            storeOfRelations = storeOfRelations
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

    fun onStart(ctx: Id) {
        Timber.d("onStart, ctx:[$ctx]")
        context = ctx
        subscribeToEvents(ctx = ctx)
        subscribeToThreadStatus(ctx = ctx)
        proceedWithOpeningCurrentObject(ctx = ctx)
    }

    private fun subscribeToEvents(ctx: Id) {
        jobs += viewModelScope.launch {
            interceptEvents
                .build(InterceptEvents.Params(ctx))
                .collect { events -> stateReducer.dispatch(events) }
        }
    }

    private fun subscribeToThreadStatus(ctx: Id) {
        jobs += viewModelScope.launch {
            interceptThreadStatus
                .build(InterceptThreadStatus.Params(ctx))
                .collect { status.value = it }
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
                Triple(state, offset, view)
            }.flatMapLatest { (state, offset, view) ->
                when (state) {
                    is ObjectState.DataView.Collection -> {
                        Timber.d("subscribeToObjectState, NEW COLLECTION STATE")
                        if (state.isInitialized) {
                            dataViewSubscription.startObjectCollectionSubscription(
                                collection = context,
                                state = state,
                                currentViewerId = view,
                                offset = offset,
                                context = context,
                                space = spaceManager.get(),
                                storeOfRelations = storeOfRelations
                            )
                        } else {
                            emptyFlow()
                        }
                    }

                    is ObjectState.DataView.Set -> {
                        Timber.d("subscribeToObjectState, NEW SET STATE")
                        if (state.isInitialized) {
                            dataViewSubscription.startObjectSetSubscription(
                                state = state,
                                currentViewerId = view,
                                offset = offset,
                                context = context,
                                space = spaceManager.get(),
                                storeOfRelations = storeOfRelations
                            )
                        } else {
                            emptyFlow()
                        }
                    }

                    else -> {
                        Timber.d("subscribeToObjectState, NEW STATE, $state")
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
            openObjectSet(ctx).process(
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
                                startTime = startTime
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
                session.currentViewerId
            ) { dataViewState, objectState, currentViewId ->
                processViewState(dataViewState, objectState, currentViewId)
            }.distinctUntilChanged().collect { viewState ->
                Timber.d("subscribeToDataViewViewer, newViewerState:[$viewState]")
                _currentViewer.value = viewState
            }
        }
    }

    private suspend fun processViewState(
        dataViewState: DataViewState,
        objectState: ObjectState,
        currentViewId: String?
    ): DataViewViewState {
        return when (objectState) {
            is ObjectState.DataView.Collection -> processCollectionState(
                dataViewState = dataViewState,
                objectState = objectState,
                currentViewId = currentViewId
            )
            is ObjectState.DataView.Set -> processSetState(
                dataViewState = dataViewState,
                objectState = objectState,
                currentViewId = currentViewId
            )
            ObjectState.Init -> DataViewViewState.Init
            ObjectState.ErrorLayout -> DataViewViewState.Error(msg = "Wrong layout, couldn't open object")
        }
    }

    private suspend fun processCollectionState(
        dataViewState: DataViewState,
        objectState: ObjectState.DataView.Collection,
        currentViewId: String?,
    ): DataViewViewState {
        if (!objectState.isInitialized) return DataViewViewState.Init

        val dvViewer = objectState.viewerByIdOrFirst(currentViewId)

        return when (dataViewState) {
            DataViewState.Init -> {
                _dvViews.value = emptyList()
                if (dvViewer == null) {
                    DataViewViewState.Collection.NoView
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
                    viewer == null -> DataViewViewState.Collection.NoView
                    viewer.isEmpty() -> {
                        val isCreateObjectAllowed = objectState.isCreateObjectAllowed()
                        DataViewViewState.Collection.NoItems(
                            title = viewer.title,
                            isCreateObjectAllowed = isCreateObjectAllowed
                        )
                    }
                    else -> {
                        val isCreateObjectAllowed = objectState.isCreateObjectAllowed()
                        DataViewViewState.Collection.Default(
                            viewer = viewer,
                            isCreateObjectAllowed = isCreateObjectAllowed
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
    ): DataViewViewState {
        if (!objectState.isInitialized) return DataViewViewState.Init

        val setOfValue = objectState.getSetOfValue(ctx = context)
        val query = objectState.filterOutDeletedAndMissingObjects(query = setOfValue)
        val viewer = objectState.viewerByIdOrFirst(currentViewId)

        return when (dataViewState) {
            DataViewState.Init -> {
                _dvViews.value = emptyList()
                when {
                    setOfValue.isEmpty() || query.isEmpty() -> DataViewViewState.Set.NoQuery
                    viewer == null -> DataViewViewState.Set.NoView
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
                    details = objectState.details,
                    store = objectStore
                )

                when {
                    query.isEmpty() || setOfValue.isEmpty() -> DataViewViewState.Set.NoQuery
                    render == null -> DataViewViewState.Set.NoView
                    render.isEmpty() -> {
                        val (defType, _) = objectState.getActiveViewTypeAndTemplate(
                            context, viewer, storeOfObjectTypes
                        )
                        DataViewViewState.Set.NoItems(
                            title = render.title,
                            isCreateObjectAllowed = objectState.isCreateObjectAllowed(defType)
                        )
                    }
                    else -> {
                        val (defType, _) = objectState.getActiveViewTypeAndTemplate(
                            context, viewer, storeOfObjectTypes
                        )
                        DataViewViewState.Set.Default(
                            viewer = render,
                            isCreateObjectAllowed = objectState.isCreateObjectAllowed(defType)
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
                details = objectState.details,
                store = objectStore,
                objectOrderIds = objectOrderIds
            )
        }
    }

    fun onStop() {
        Timber.d("onStop, ")
        hideTemplatesWidget()
        unsubscribeFromTypesTemplates()
        jobs.cancel()
    }

    fun onSystemBackPressed() {
        Timber.d("onSystemBackPressed, ")
        proceedWithExiting()
    }

    private fun proceedWithExiting() {
        viewModelScope.launch {
            val timeout = if (BuildConfig.DEBUG) 3000 else Long.MAX_VALUE
            withTimeout(timeout) {
                cancelSearchSubscription(
                    CancelSearchSubscription.Params(
                        subscriptions = buildList {
                            add(DefaultDataViewSubscription.getSubscriptionId(context))
                        }
                    )
                ).process(
                    failure = {
                        Timber.e(it, "Failed to cancel subscription")
                        proceedWithClosingAndExit()
                    },
                    success = {
                        proceedWithClosingAndExit()
                    }
                )
            }
        }
    }

    private suspend fun proceedWithClosingAndExit() {
        closeBlock.async(context).fold(
            onSuccess = { dispatch(AppNavigation.Command.Exit) },
            onFailure = {
                Timber.e(it, "Error while closing object set: $context").also {
                    dispatch(AppNavigation.Command.Exit)
                }
            }
        )
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
                Timber.e("Skipping dispatching title update, because set of objects was not ready.")
            }
        }
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
            val dataViewBlock = state.dataViewBlock
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

            if (relation.isReadonlyValue) {
                if (relation.format == Relation.Format.OBJECT) {

                    if (relation.key == Relations.TYPE) {
                        Timber.w("Cannot open Object Type from here.")
                        toast(NOT_ALLOWED_CELL)
                        return@launch
                    }

                    // TODO terrible workaround, which must be removed in the future!
                    if (cell is CellView.Object && cell.objects.isNotEmpty()) {
                        val obj = cell.objects.first()
                        onRelationObjectClicked(target = obj.id)
                        return@launch
                    } else {
                        toast(NOT_ALLOWED_CELL)
                        return@launch
                    }
                } else {
                    Timber.d("onGridCellClicked, relation is ReadOnly")
                    toast(NOT_ALLOWED_CELL)
                    return@launch
                }
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
                            recordId = cell.id
                        )
                    )
                }
                is CellView.Date -> {
                    dispatch(
                        ObjectSetCommand.Modal.EditGridDateCell(
                            ctx = context,
                            objectId = cell.id,
                            relationKey = cell.relationKey
                        )
                    )
                }
                is CellView.Tag, is CellView.Status, is CellView.File -> {
                    dispatch(
                        ObjectSetCommand.Modal.EditRelationCell(
                            ctx = context,
                            target = cell.id,
                            dataview = dataViewBlock.id,
                            relationKey = cell.relationKey,
                            viewer = viewer.id,
                            targetObjectTypes = emptyList()
                        )
                    )
                }
                is CellView.Object -> {
                    if (cell.relationKey != Relations.TYPE) {
                        val targetObjectTypes = mutableListOf<String>()
                        targetObjectTypes.addAll(relation.relationFormatObjectTypes)
                        dispatch(
                            ObjectSetCommand.Modal.EditRelationCell(
                                ctx = context,
                                target = cell.id,
                                dataview = dataViewBlock.id,
                                relationKey = cell.relationKey,
                                viewer = viewer.id,
                                targetObjectTypes = targetObjectTypes
                            )
                        )
                    } else {
                        toast("You cannot change type from here.")
                    }
                }
                is CellView.Checkbox -> {
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
     *  @param [target] Object is a dependent object, therefore we look for data in details.
     */
    private suspend fun onRelationObjectClicked(target: Id) {
        Timber.d("onCellObjectClicked, id:[$target]")
        stateReducer.state.value.dataViewState() ?: return
        val obj = objectStore.get(target) ?: return
        if (obj.type.contains(ObjectTypeIds.OBJECT_TYPE)) {
            toast("You cannot change type from here.")
        } else {
            proceedWithNavigation(
                target = target,
                layout = obj.layout
            )
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
                    layout = obj.layout
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
                                    ObjectSetCommand.Modal.CreateBookmark(ctx = context)
                                )
                            } else {
                                val validTemplateId = templateChosenBy ?: defaultTemplate
                                proceedWithCreatingDataViewObject(
                                    CreateDataViewObject.Params.SetByType(
                                        type = TypeKey(uniqueKey),
                                        filters = viewer.filters,
                                        template = validTemplateId
                                    )
                                )
                            }
                        }
                        ObjectType.Layout.RELATION -> {
                            val validTemplateId = templateChosenBy ?: defaultTemplate
                            proceedWithCreatingDataViewObject(
                                CreateDataViewObject.Params.SetByRelation(
                                    filters = viewer.filters,
                                    relations = setObject.setOf,
                                    template = validTemplateId,
                                    type = TypeKey(objectTypeUniqueKey)
                                )
                            )
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
        val createObjectParams = CreateDataViewObject.Params.Collection(
            templateId = validTemplateId,
            type = typeChosenByUser ?: defaultObjectTypeUniqueKey!!
        )
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

    private fun proceedWithCreatingDataViewObject(
        params: CreateDataViewObject.Params,
        action: ((CreateDataViewObject.Result) -> Unit)? = null
    ) {
        val startTime = System.currentTimeMillis()
        viewModelScope.launch {
            createDataViewObject.async(params).fold(
                onFailure = { Timber.e(it, "Error while creating new record") },
                onSuccess = { result ->
                    action?.invoke(result)
                    proceedWithNewDataViewObject(result)
                    sendAnalyticsObjectCreateEvent(
                        startTime = startTime,
                        objectType = result.objectType.key,
                    )
                }
            )
        }
    }

    private suspend fun proceedWithNewDataViewObject(
        response: CreateDataViewObject.Result,
    ) {
        val obj = ObjectWrapper.Basic(response.struct.orEmpty())
        if (obj.layout == ObjectType.Layout.NOTE) {
            proceedWithOpeningObject(
                target = response.objectId,
                layout = obj.layout
            )
        } else {
            dispatch(
                ObjectSetCommand.Modal.SetNameForCreatedObject(
                    ctx = context,
                    target = response.objectId
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
        dispatch(
            ObjectSetCommand.Modal.Menu(
                ctx = context,
                isArchived = state.details[context]?.isArchived ?: false,
                isFavorite = state.details[context]?.isFavorite ?: false,
            )
        )
    }

    fun onIconClicked() {
        Timber.d("onIconClicked, ")
        dispatch(ObjectSetCommand.Modal.OpenIconActionMenu(target = context))
    }

    fun onCoverClicked() {
        Timber.d("onCoverClicked, ")
        dispatch(ObjectSetCommand.Modal.OpenCoverActionMenu(ctx = context))
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

    private suspend fun proceedWithOpeningObject(target: Id, layout: ObjectType.Layout? = null) {
        isCustomizeViewPanelVisible.value = false
        jobs += viewModelScope.launch {
            val navigateCommand = when (layout) {
                ObjectType.Layout.SET,
                ObjectType.Layout.COLLECTION -> AppNavigation.Command.OpenSetOrCollection(target = target)
                else -> AppNavigation.Command.OpenObject(id = target)
            }
            closeBlock.async(context).fold(
                onSuccess = { navigate(EventWrapper(navigateCommand)) },
                onFailure = {
                    Timber.e(it, "Error while closing object set: $context")
                    navigate(EventWrapper(navigateCommand))
                }
            )
        }
    }

    private suspend fun proceedWithOpeningTemplate(target: Id, targetTypeId: Id, targetTypeKey: Id) {
        isCustomizeViewPanelVisible.value = false
        val event = AppNavigation.Command.OpenModalTemplateSelect(
            template = target,
            templateTypeId = targetTypeId,
            templateTypeKey = targetTypeKey
        )
        viewModelScope.launch {
            closeBlock.async(context).fold(
                onSuccess = { navigate(EventWrapper(event)) },
                onFailure = {
                    Timber.e(it, "Error while closing object set: $context")
                    navigate(EventWrapper(event))
                }
            )
        }
    }

    private suspend fun proceedWithOpeningObjectCollection(target: Id) {
        isCustomizeViewPanelVisible.value = false
        jobs += viewModelScope.launch {
            closeBlock.async(context).fold(
                onSuccess = {
                    navigate(EventWrapper(AppNavigation.Command.OpenSetOrCollection(target = target)))
                },
                onFailure = {
                    Timber.e(it, "Error while closing object set: $context")
                    navigate(EventWrapper(AppNavigation.Command.OpenSetOrCollection(target = target)))
                }
            )
        }
    }

    private suspend fun proceedWithNavigation(target: Id, layout: ObjectType.Layout?) {
        when (layout) {
            ObjectType.Layout.BASIC,
            ObjectType.Layout.PROFILE,
            ObjectType.Layout.TODO,
            ObjectType.Layout.NOTE,
            ObjectType.Layout.IMAGE,
            ObjectType.Layout.FILE,
            ObjectType.Layout.VIDEO,
            ObjectType.Layout.BOOKMARK -> proceedWithOpeningObject(target)
            ObjectType.Layout.SET, ObjectType.Layout.COLLECTION -> {
                closeBlock.async(context).fold(
                    onSuccess = {
                        navigate(EventWrapper(AppNavigation.Command.OpenSetOrCollection(target)))
                    },
                    onFailure = {
                        Timber.e(it, "Error while closing object set: $context")
                        navigate(EventWrapper(AppNavigation.Command.OpenSetOrCollection(target)))
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
            closeBlock.async(context).fold(
                onSuccess = { dispatch(AppNavigation.Command.ExitToDesktop) },
                onFailure = {
                    Timber.e(it, "Error while closing object set: $context").also {
                        dispatch(AppNavigation.Command.ExitToDesktop)
                    }
                }
            )
        }
    }

    fun onBackButtonClicked() {
        proceedWithExiting()
    }

    fun onAddNewDocumentClicked(type: Key? = null) {
        Timber.d("onAddNewDocumentClicked, ")

        val startTime = System.currentTimeMillis()
        jobs += viewModelScope.launch {
            createObject.async(
                CreateObject.Param(
                    type = type?.let { TypeKey(it) },
                    internalFlags = buildList {
                        add(InternalFlags.ShouldSelectTemplate)
                        add(InternalFlags.ShouldEmptyDelete)
                        if (type.isNullOrBlank()) {
                            add(InternalFlags.ShouldSelectType)
                        }
                    }
                )
            ).fold(
                onSuccess = { result ->
                    proceedWithOpeningObject(result.objectId)
                    sendAnalyticsObjectCreateEvent(
                        analytics = analytics,
                        startTime = startTime,
                        storeOfObjectTypes = storeOfObjectTypes,
                        type = result.typeKey.key,
                        route = EventsDictionary.Routes.navigation,
                        view = EventsDictionary.View.viewNavbar
                    )
                },
                onFailure = { e ->
                    Timber.e(e, "Error while creating a new object")
                    toast("Error while creating a new object")
                }
            )
        }
    }

    private fun sendAnalyticsObjectCreateEvent(startTime: Long, objectType: String?) {
        viewModelScope.launch {
            val sourceType = objectType?.let { storeOfObjectTypes.get(it) }
            logEvent(
                state = stateReducer.state.value,
                analytics = analytics,
                event = ObjectStateAnalyticsEvent.OBJECT_CREATE,
                startTime = startTime,
                type = sourceType?.sourceObject
            )
        }
    }

    fun onSearchButtonClicked() {
        viewModelScope.launch {
            closeBlock.async(context).fold(
                onSuccess = { dispatch(AppNavigation.Command.OpenPageSearch) },
                onFailure = { Timber.e(it, "Error while closing object set: $context") }
            )
        }
    }

    fun onClickListener(clicked: ListenerType) {
        Timber.d("onClickListener, clicked:[$clicked]")
        when (clicked) {
            is ListenerType.Relation.SetQuery -> {
                val queries = clicked.queries.map { it.id }
                val command = if (queries.isEmpty()) {
                    ObjectSetCommand.Modal.OpenEmptyDataViewSelectQueryScreen
                } else {
                    ObjectSetCommand.Modal.OpenDataViewSelectQueryScreen(
                        selectedTypes = queries
                    )
                }
                dispatch(command)
            }
            is ListenerType.Relation.ChangeQueryByRelation -> {
                toast("Currently, this query can be changed via Desktop only")
            }
            is ListenerType.Relation.TurnIntoCollection -> {
                proceedWithConvertingToCollection()
            }
            is ListenerType.Relation.Featured -> {
                onRelationClickedListMode(
                    ctx = context,
                    view = clicked.relation
                )
            }
            else -> {
                Timber.d("Ignoring click")
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
                    sendAnalyticsRelationValueEvent(analytics)
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
            if (relation.isReadonlyValue) {
                toast(RelationListViewModel.NOT_ALLOWED_FOR_RELATION)
                Timber.d("No interaction allowed with this relation")
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
                            relation = relation.key
                        )
                    )
                }
                RelationFormat.CHECKBOX -> {
                    proceedWithTogglingRelationCheckboxValue(view, ctx)
                }
                RelationFormat.DATE -> {
                    _commands.emit(
                        ObjectSetCommand.Modal.EditGridDateCell(
                            ctx = context,
                            objectId = context,
                            relationKey = relation.key
                        )
                    )
                }
                RelationFormat.STATUS,
                RelationFormat.TAG,
                RelationFormat.FILE,
                RelationFormat.OBJECT -> {
                    _commands.emit(
                        ObjectSetCommand.Modal.EditIntrinsicRelationValue(
                            ctx = context,
                            relation = relation.key
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
                        startTime = startTime
                    )
                    defaultPayloadConsumer(payload)
                },
                onFailure = { e -> Timber.e(e, "Error while setting Set query") }
            )
        }
    }

    private fun proceedWithConvertingToCollection() {
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
                        startTime = startTime
                    )
                }
            )
        }
    }

    //region TYPES AND TEMPLATES WIDGET
    private var viewerTemplatesJob = mutableListOf<Job>()
    private var templatesSubId: Id? = null

    private fun unsubscribeFromTypesTemplates() {
        Timber.d("unsubscribeFromTypesTemplates, ")
        viewModelScope.launch {
            templatesContainer.unsubscribeFromTemplates("$context$SUBSCRIPTION_TEMPLATES_ID")
        }
    }

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
        viewModelScope.launch {
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
                    _commands.emit(
                        ObjectSetCommand.Modal.OpenSelectTypeScreen(
                            excludedTypes = emptyList()
                        )
                    )
                }
                is TypeTemplatesWidgetUIAction.TemplateClick -> {
                    when (uiState) {
                        is TypeTemplatesWidgetUI.Data -> uiState.onTemplateClick(action.template)
                        is TypeTemplatesWidgetUI.Init -> Unit
                    }
                }
            }
        }
    }

    private suspend fun TypeTemplatesWidgetUI.Data.onTemplateClick(
        templateView: TemplateView
    ) {
        if (moreMenuItem != null) {
            typeTemplatesWidgetState.value = hideMoreMenu()
            return
        }
        typeTemplatesWidgetState.value = copy(showWidget = false)
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
                hideTemplatesWidget()
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
                type = type
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
                        subId = "$context$SUBSCRIPTION_TEMPLATES_ID"
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
            spaces = listOf(spaceManager.get()),
            recommendedLayouts = SupportedLayouts.createObjectLayouts
        )
        val params = GetObjectTypes.Params(
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
        Timber.d("proceedWithSelectedTemplate, template:[$template], objectType:[$typeId]")
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

    private fun proceedWithCreatingTemplate(targetTypeId: Id, targetTypeKey: Id) {
        viewModelScope.launch {
            delay(DELAY_BEFORE_CREATING_TEMPLATE)
            val params = CreateTemplate.Params(
                targetObjectTypeId = targetTypeId
            )
            createTemplate.async(params).fold(
                onSuccess = { id ->
                    logEvent(
                        state = stateReducer.state.value,
                        analytics = analytics,
                        event = ObjectStateAnalyticsEvent.CREATE_TEMPLATE,
                        type = storeOfObjectTypes.get(targetTypeId)?.sourceObject
                    )
                    proceedWithOpeningTemplate(
                        target = id,
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

    private fun proceedWithUpdatingViewDefaultTemplate() {
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
                        type = template.targetTypeId.id
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
                        type = template.targetTypeKey.key
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
                        type = template.targetTypeKey.key
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
                                    startTime = startTime
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
                                    type = type.formattedName
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
                                type = action.type.formattedName
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
                                    type = newView.type.formattedName
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
                                    startTime = startTime
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
                                    type = viewer.type.formattedName
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
                proceedWithUpdateViewer(
                    viewerId = viewerLayoutWidgetState.value.viewer
                ) { it.copy(coverFit = action.toggled) }
            }
            is ViewerLayoutWidgetUi.Action.Icon -> {
                proceedWithUpdateViewer(
                    viewerId = viewerLayoutWidgetState.value.viewer
                ) { it.copy(hideIcon = !action.toggled) }
            }
            is ViewerLayoutWidgetUi.Action.CardSize -> {
                viewerLayoutWidgetState.value =
                    viewerLayoutWidgetState.value.copy(showCardSize = false)
                when (action.cardSize) {
                    ViewerLayoutWidgetUi.State.CardSize.Small -> {
                        proceedWithUpdateViewer(
                            viewerId = viewerLayoutWidgetState.value.viewer
                        ) { it.copy(cardSize = DVViewerCardSize.SMALL) }
                    }
                    ViewerLayoutWidgetUi.State.CardSize.Large -> {
                        proceedWithUpdateViewer(
                            viewerId = viewerLayoutWidgetState.value.viewer
                        ) { it.copy(cardSize = DVViewerCardSize.LARGE) }
                    }
                }
            }
            is ViewerLayoutWidgetUi.Action.Cover -> {
                when (action.cover) {
                    ViewerLayoutWidgetUi.State.ImagePreview.Cover -> {
                        proceedWithUpdateViewer(
                            viewerId = viewerLayoutWidgetState.value.viewer
                        ) { it.copy(coverRelationKey = Relations.PAGE_COVER) }
                    }
                    is ViewerLayoutWidgetUi.State.ImagePreview.Custom -> {
                        proceedWithUpdateViewer(
                            viewerId = viewerLayoutWidgetState.value.viewer
                        ) { it.copy(coverRelationKey = action.cover.name) }
                    }
                    ViewerLayoutWidgetUi.State.ImagePreview.None -> {
                        proceedWithUpdateViewer(
                            viewerId = viewerLayoutWidgetState.value.viewer
                        ) { it.copy(coverRelationKey = null) }
                    }
                }
            }
            is ViewerLayoutWidgetUi.Action.Type -> {
                proceedWithUpdateViewer(
                    action = {
                        val startTime = System.currentTimeMillis()
                        viewModelScope.launch {
                            logEvent(
                                state = stateReducer.state.value,
                                analytics = analytics,
                                event = ObjectStateAnalyticsEvent.CHANGE_VIEW_TYPE,
                                startTime = startTime,
                                type = action.type.formattedName
                            )
                        }
                    },
                    viewerId = viewerLayoutWidgetState.value.viewer
                ) { it.copy(type = action.type) }
            }

            ViewerLayoutWidgetUi.Action.DismissCoverMenu -> viewerLayoutWidgetState.value =
                viewerLayoutWidgetState.value.copy(
                    showCoverMenu = false
                )
        }
    }

    private fun proceedWithUpdateViewer(action: () -> Unit = {}, viewerId: Id?, update: (DVViewer) -> DVViewer) {
        val state = stateReducer.state.value.dataViewState() ?: return
        val viewer = state.viewerById(viewerId)
        if (viewer == null) {
            Timber.e("Couldn't find viewer by id: ${viewerLayoutWidgetState.value.viewer}")
            return
        }
        viewModelScope.launch {
            viewerDelegate.onEvent(
                ViewerEvent.UpdateView(
                    ctx = context,
                    dv = state.dataViewBlock.id,
                    viewer = update.invoke(viewer),
                    onResult = action
                )
            )
        }
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
}