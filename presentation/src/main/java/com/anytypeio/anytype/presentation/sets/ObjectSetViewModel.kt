package com.anytypeio.anytype.presentation.sets

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.core_models.DVViewer
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.SyncStatus
import com.anytypeio.anytype.core_models.restrictions.DataViewRestriction
import com.anytypeio.anytype.core_utils.common.EventWrapper
import com.anytypeio.anytype.core_utils.ext.cancel
import com.anytypeio.anytype.domain.base.Result
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.block.interactor.UpdateText
import com.anytypeio.anytype.domain.collections.AddObjectToCollection
import com.anytypeio.anytype.domain.cover.SetDocCoverImage
import com.anytypeio.anytype.domain.dataview.interactor.CreateDataViewObject
import com.anytypeio.anytype.domain.error.Error
import com.anytypeio.anytype.domain.event.interactor.InterceptEvents
import com.anytypeio.anytype.domain.launch.GetDefaultPageType
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.`object`.ConvertObjectToCollection
import com.anytypeio.anytype.domain.`object`.UpdateDetail
import com.anytypeio.anytype.domain.objects.ObjectStore
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
import com.anytypeio.anytype.domain.templates.GetTemplates
import com.anytypeio.anytype.domain.unsplash.DownloadUnsplashImage
import com.anytypeio.anytype.domain.workspace.WorkspaceManager
import com.anytypeio.anytype.presentation.common.Action
import com.anytypeio.anytype.presentation.common.Delegator
import com.anytypeio.anytype.presentation.editor.cover.CoverImageHashProvider
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.model.TextUpdate
import com.anytypeio.anytype.presentation.editor.template.EditorTemplateDelegate
import com.anytypeio.anytype.presentation.extension.ObjectStateAnalyticsEvent
import com.anytypeio.anytype.presentation.extension.logEvent
import com.anytypeio.anytype.presentation.extension.sendAnalyticsObjectCreateEvent
import com.anytypeio.anytype.presentation.extension.sendAnalyticsRelationValueEvent
import com.anytypeio.anytype.presentation.navigation.AppNavigation
import com.anytypeio.anytype.presentation.navigation.SupportNavigation
import com.anytypeio.anytype.presentation.objects.isTemplatesAllowed
import com.anytypeio.anytype.presentation.relations.ObjectRelationView
import com.anytypeio.anytype.presentation.relations.ObjectSetConfig.DEFAULT_LIMIT
import com.anytypeio.anytype.presentation.relations.RelationListViewModel
import com.anytypeio.anytype.presentation.relations.render
import com.anytypeio.anytype.presentation.sets.model.CellView
import com.anytypeio.anytype.presentation.sets.model.Viewer
import com.anytypeio.anytype.presentation.sets.state.ObjectState
import com.anytypeio.anytype.presentation.sets.state.ObjectStateReducer
import com.anytypeio.anytype.presentation.sets.subscription.DataViewSubscription
import com.anytypeio.anytype.presentation.templates.TemplateView
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.presentation.widgets.TemplatesWidgetUiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
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
    private val workspaceManager: WorkspaceManager,
    private val objectStore: ObjectStore,
    private val addObjectToCollection: AddObjectToCollection,
    private val objectToCollection: ConvertObjectToCollection,
    private val storeOfObjectTypes: StoreOfObjectTypes,
    private val templateDelegate: EditorTemplateDelegate,
    private val getDefaultPageType: GetDefaultPageType,
    private val getTemplates: GetTemplates
) : ViewModel(), SupportNavigation<EventWrapper<AppNavigation.Command>>,
    EditorTemplateDelegate by templateDelegate {

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

    private val _templatesStore = MutableStateFlow<List<TemplateView>>(emptyList())

    private val _header = MutableStateFlow<SetOrCollectionHeaderState>(
        SetOrCollectionHeaderState.None
    )
    val header: StateFlow<SetOrCollectionHeaderState> = _header

    val isCustomizeViewPanelVisible = MutableStateFlow(false)
    val templatesWidgetState = MutableStateFlow(TemplatesWidgetUiState.empty())

    @Deprecated("could be deleted")
    val isLoading = MutableStateFlow(false)

    private var context: Id = ""

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
                    gettingTemplatesForDataViewState(state)
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
                                workspaceId = workspaceManager.getCurrentWorkspace(),
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
                                workspaceId = workspaceManager.getCurrentWorkspace(),
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
                session.currentViewerId,
                _templatesStore
            ) { dataViewState, objectState, currentViewId, templates ->
                processViewState(dataViewState, objectState, currentViewId, templates)
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
        templates: List<TemplateView>
    ): DataViewViewState {
        return when (objectState) {
            is ObjectState.DataView.Collection -> processCollectionState(
                dataViewState = dataViewState,
                objectState = objectState,
                currentViewId = currentViewId,
                templates = templates
            )
            is ObjectState.DataView.Set -> processSetState(
                dataViewState = dataViewState,
                objectState = objectState,
                currentViewId = currentViewId,
                templates = templates
            )
            ObjectState.Init -> DataViewViewState.Init
            ObjectState.ErrorLayout -> DataViewViewState.Error(msg = "Wrong layout, couldn't open object")
        }
    }

    private suspend fun processCollectionState(
        dataViewState: DataViewState,
        objectState: ObjectState.DataView.Collection,
        currentViewId: String?,
        templates: List<TemplateView>
    ): DataViewViewState {
        if (!objectState.isInitialized) return DataViewViewState.Init

        val dvViewer = objectState.viewerById(currentViewId)

        return when (dataViewState) {
            DataViewState.Init -> {
                if (dvViewer == null) {
                    DataViewViewState.Collection.NoView
                } else {
                    DataViewViewState.Init
                }
            }
            is DataViewState.Loaded -> {
                val relations = objectState.dataViewContent.relationLinks.mapNotNull {
                    storeOfRelations.getByKey(it.key)
                }
                val viewer = renderViewer(objectState, dataViewState, dvViewer, relations)

                when {
                    viewer == null -> DataViewViewState.Collection.NoView
                    viewer.isEmpty() -> {
                        val isTemplatesPresent = templates.isNotEmpty() &&
                                storeOfObjectTypes.isTemplatesAllowedForDefaultType(
                                    getDefaultPageType = getDefaultPageType
                                )
                        DataViewViewState.Collection.NoItems(
                            title = viewer.title,
                            isTemplatesPresent = isTemplatesPresent
                        )
                    }
                    else -> {
                        val isTemplatesPresent = templates.isNotEmpty() &&
                                storeOfObjectTypes.isTemplatesAllowedForDefaultType(
                                    getDefaultPageType = getDefaultPageType
                                )
                        DataViewViewState.Collection.Default(
                            viewer = viewer,
                            isTemplatesPresent = isTemplatesPresent
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
        templates: List<TemplateView>
    ): DataViewViewState {
        if (!objectState.isInitialized) return DataViewViewState.Init

        val setOfValue = objectState.getSetOfValue(ctx = context)
        val query = objectState.filterOutDeletedAndMissingObjects(query = setOfValue)
        val viewer = objectState.viewerById(currentViewId)

        return when (dataViewState) {
            DataViewState.Init -> {
                when {
                    setOfValue.isEmpty() || query.isEmpty() -> DataViewViewState.Set.NoQuery
                    viewer == null -> DataViewViewState.Set.NoView
                    else -> DataViewViewState.Init
                }
            }
            is DataViewState.Loaded -> {
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
                        val isTemplatesAllowed = templates.isNotEmpty() &&
                                objectState.isTemplatesAllowed(
                                    setOfValue,
                                    storeOfObjectTypes,
                                    getDefaultPageType
                                )
                        DataViewViewState.Set.NoItems(
                            title = render.title,
                            isTemplatesPresent = isTemplatesAllowed
                        )
                    }
                    else -> {
                        val isTemplatesAllowed = templates.isNotEmpty() &&
                                objectState.isTemplatesAllowed(
                                    setOfValue,
                                    storeOfObjectTypes,
                                    getDefaultPageType
                                )
                        DataViewViewState.Set.Default(
                            viewer = render,
                            isTemplatesPresent = isTemplatesAllowed
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
        jobs.cancel()
    }

    fun onSystemBackPressed() {
        Timber.d("onSystemBackPressed, ")
        proceedWithExiting()
    }

    private fun proceedWithExiting() {
        viewModelScope.launch {
            cancelSearchSubscription(CancelSearchSubscription.Params(listOf(context))).process(
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
            val viewer = state.viewerById(session.currentViewerId.value)
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

    fun onCreateNewDataViewObject(templatesId: Id? = null) {
        Timber.d("onCreateNewDataViewObject, ")
        val state = stateReducer.state.value.dataViewState() ?: return
        when (state) {
            is ObjectState.DataView.Collection -> proceedWithAddingObjectToCollection(templatesId)
            is ObjectState.DataView.Set -> proceedWithCreatingSetObject(state, templatesId)
        }
    }

    fun onNewButtonIconClicked() {
        Timber.d("onNewButtonIconClicked, ")
        templatesWidgetState.value = TemplatesWidgetUiState(
            items = _templatesStore.value,
            showWidget = true
        )
    }

    fun onHideTemplatesWidget() {
        Timber.d("onHideTemplatesWidget, ")
        templatesWidgetState.value = TemplatesWidgetUiState(
            items = emptyList(),
            showWidget = false
        )
    }

    private fun proceedWithCreatingSetObject(currentState: ObjectState.DataView.Set, templateId: Id?) {
        if (isRestrictionPresent(DataViewRestriction.CREATE_OBJECT)) {
            toast(NOT_ALLOWED)
        } else {
            val setObject = ObjectWrapper.Basic(
                currentState.details[context]?.map ?: emptyMap()
            )
            val viewer = currentState.viewerById(session.currentViewerId.value)
            if (viewer == null) {
                Timber.e("onCreateNewDataViewObject, Viewer is empty")
                return
            }
            val sourceId = setObject.setOf.singleOrNull()
            if (sourceId == null) {
                toast("Unable to define a source for a new object.")
            } else {
                val sourceDetails = currentState.details[sourceId]
                if (sourceDetails != null && sourceDetails.map.isNotEmpty()) {
                    when (sourceDetails.type.firstOrNull()) {
                        ObjectTypeIds.OBJECT_TYPE -> {
                            if (sourceId == ObjectTypeIds.BOOKMARK) {
                                dispatch(
                                    ObjectSetCommand.Modal.CreateBookmark(
                                        ctx = context
                                    )
                                )
                            } else {
                                proceedWithCreatingDataViewObject(
                                    CreateDataViewObject.Params.SetByType(
                                        type = sourceId,
                                        filters = viewer.filters,
                                        template = templateId
                                    )
                                )
                            }
                        }
                        ObjectTypeIds.RELATION -> {
                            proceedWithCreatingDataViewObject(
                                CreateDataViewObject.Params.SetByRelation(
                                    filters = viewer.filters,
                                    relations = setObject.setOf,
                                    template = templateId
                                )
                            )
                        }
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

    fun onCreateObjectInCollectionClicked() {
        Timber.d("onCreateObjectInCollectionClicked, ")
        proceedWithAddingObjectToCollection()
    }

    private fun proceedWithAddingObjectToCollection(templateId: Id? = null) {
        val createObjectParams = CreateDataViewObject.Params.Collection(
            templateId = templateId
        )
        proceedWithCreatingDataViewObject(createObjectParams) { result ->
            val params = AddObjectToCollection.Params(
                ctx = context,
                after = "",
                targets = listOf(result.objectId)
            )
            viewModelScope.launch {
                addObjectToCollection.execute(params).fold(
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
                    proceedWithNewDataViewObject(params, result.objectId)
                    action?.invoke(result)
                    sendAnalyticsObjectCreateEvent(
                        startTime = startTime,
                        objectType = result.objectType,
                    )
                }
            )
        }
    }

    private suspend fun proceedWithNewDataViewObject(params: CreateDataViewObject.Params, newObject: Id) {
        when (params) {
            is CreateDataViewObject.Params.Collection -> {
                proceedWithOpeningObject(newObject)
            }
            is CreateDataViewObject.Params.SetByRelation -> {
                proceedWithOpeningObject(newObject)
            }
            is CreateDataViewObject.Params.SetByType -> {
                if (params.type == ObjectTypeIds.NOTE) {
                    proceedWithOpeningObject(newObject)
                } else {
                    dispatch(
                        ObjectSetCommand.Modal.SetNameForCreatedObject(
                            ctx = context,
                            target = newObject
                        )
                    )
                }
            }
        }
    }

    fun onViewerCustomizeButtonClicked() {
        Timber.d("onViewerCustomizeButtonClicked, ")
        stateReducer.state.value.dataViewState() ?: return
        isCustomizeViewPanelVisible.value = !isCustomizeViewPanelVisible.value
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
            dispatch(
                ObjectSetCommand.Modal.ManageViewer(
                    ctx = context,
                    dataview = state.dataViewBlock.id
                )
            )
        }
    }

    fun onViewerEditClicked() {
        Timber.d("onViewerEditClicked, ")
        val state = stateReducer.state.value.dataViewState() ?: return
        val viewer = state.viewerById(session.currentViewerId.value) ?: return
        dispatch(
            ObjectSetCommand.Modal.EditDataViewViewer(
                ctx = context,
                viewer = viewer.id
            )
        )
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

    fun onViewerSettingsClicked() {
        Timber.d("onViewerSettingsClicked, ")
        if (isRestrictionPresent(DataViewRestriction.RELATION)) {
            toast(NOT_ALLOWED)
        } else {
            val state = stateReducer.state.value.dataViewState() ?: return
            val dataViewBlock = state.dataViewBlock
            val dataViewContent = state.dataViewContent
            if (dataViewContent.viewers.isNotEmpty()) {
                val viewer = state.viewerById(session.currentViewerId.value)
                if (viewer == null) {
                    Timber.e("onViewerSettingsClicked, Viewer is empty")
                    return
                }
                dispatch(
                    ObjectSetCommand.Modal.OpenSettings(
                        ctx = context,
                        dv = dataViewBlock.id,
                        viewer = viewer.id
                    )
                )
            } else {
                toast(DATA_VIEW_HAS_NO_VIEW_MSG)
            }
        }
    }

    fun onViewerFiltersClicked() {
        Timber.d("onViewerFiltersClicked, ")
        val state = stateReducer.state.value.dataViewState() ?: return
        if (state.viewers.isNotEmpty()) {
            if (isRestrictionPresent(DataViewRestriction.VIEWS)) {
                toast(NOT_ALLOWED)
            } else {
                dispatch(ObjectSetCommand.Modal.ModifyViewerFilters(ctx = context))
            }
        } else {
            toast(DATA_VIEW_HAS_NO_VIEW_MSG)
        }
    }

    fun onViewerSortsClicked() {
        Timber.d("onViewerSortsClicked, ")
        val state = stateReducer.state.value.dataViewState() ?: return
        if (state.viewers.isNotEmpty()) {
            if (isRestrictionPresent(DataViewRestriction.VIEWS)) {
                toast(NOT_ALLOWED)
            } else {
                dispatch(ObjectSetCommand.Modal.ModifyViewerSorts(ctx = context))
            }
        } else {
            toast(DATA_VIEW_HAS_NO_VIEW_MSG)
        }
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

    private suspend fun proceedWithOpeningObject(target: Id) {
        isCustomizeViewPanelVisible.value = false
        jobs += viewModelScope.launch {
            closeBlock.async(context).fold(
                onSuccess = {
                    navigate(EventWrapper(AppNavigation.Command.OpenObject(id = target)))
                },
                onFailure = {
                    Timber.e(it, "Error while closing object set: $context")
                    navigate(EventWrapper(AppNavigation.Command.OpenObject(id = target)))
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
        toast("This view is not supported on Android yet.")
    }

    override fun onCleared() {
        Timber.d("onCleared, ")
        super.onCleared()
        titleUpdateChannel.cancel()
        stateReducer.clear()
        templateDelegate.clear()
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

    fun onAddNewDocumentClicked() {
        Timber.d("onAddNewDocumentClicked, ")

        val startTime = System.currentTimeMillis()
        jobs += viewModelScope.launch {
            createObject.async(CreateObject.Param(type = null)).fold(
                onSuccess = { result ->
                    proceedWithOpeningObject(result.objectId)
                    sendAnalyticsObjectCreateEvent(
                        analytics = analytics,
                        startTime = startTime,
                        storeOfObjectTypes = storeOfObjectTypes,
                        type = result.type,
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
            setQueryToObjectSet.execute(params).fold(
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
            objectToCollection.execute(params).fold(
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

    // region TEMPLATES
    private suspend fun gettingTemplatesForDataViewState(state: ObjectState.DataView) {
        when (state) {
            is ObjectState.DataView.Collection -> {
                proceedWithGettingTemplates(typeId = null)
            }
            is ObjectState.DataView.Set -> {
                val sourceId = proceedWithGettingSetSourceId(state)
                val sourceMap = state.details[sourceId] ?: return
                when (sourceMap.type.firstOrNull()) {
                    ObjectTypeIds.RELATION -> proceedWithGettingTemplates(typeId = null)
                    ObjectTypeIds.OBJECT_TYPE -> proceedWithGettingTemplates(typeId = sourceId)
                    else -> {}
                }
            }
        }
    }

    private suspend fun proceedWithGettingTemplates(typeId: Id?) {
        val objectType = getObjectType(typeId)
        if (objectType?.isTemplatesAllowed() == true) {
            viewModelScope.launch {
                getTemplates.execute(GetTemplates.Params(objectType.id)).fold(
                    onSuccess = { templates ->
                        if (templates.isNotEmpty()) {
                            _templatesStore.value =
                                listOf(templates.first().toTemplateViewBlank(objectType.id)) +
                                        templates.map { it.toTemplateView(typeId = objectType.id) }
                        }
                    },
                    onFailure = { e ->
                        Timber.e(e, "Error getting templates for type ${objectType.id}")
                    }
                )
            }
        }
    }

    private suspend fun getObjectType(type: Id?): ObjectWrapper.Type? {
        return if (type == null) {
            val defaultObjectType = getDefaultPageType.run(Unit).type ?: return null
            storeOfObjectTypes.get(defaultObjectType)
        } else {
            storeOfObjectTypes.get(type)
        }
    }

    fun onTemplateItemClicked(item: TemplateView) {
        when(item) {
            is TemplateView.Blank -> {
                templatesWidgetState.value = TemplatesWidgetUiState.empty()
                onCreateNewDataViewObject()
            }
            is TemplateView.Template -> {
                templatesWidgetState.value = TemplatesWidgetUiState.empty()
                onCreateNewDataViewObject(templatesId = item.id)
            }
        }
    }

    private fun proceedWithGettingSetSourceId(currentState: ObjectState.DataView.Set): Id? {
        if (isRestrictionPresent(DataViewRestriction.CREATE_OBJECT) || !currentState.isInitialized) {
            toast(NOT_ALLOWED)
            return null
        }

        val setObject = ObjectWrapper.Basic(currentState.details[context]?.map ?: emptyMap())
        val sourceId = setObject.setOf.singleOrNull()
        if (sourceId == null) {
            Timber.e("Unable to define a source for a new object.")
            toast("Unable to define a source for a new object.")
        }
        return sourceId
    }
    //endregion

    companion object {
        const val NOT_ALLOWED = "Not allowed for this set"
        const val NOT_ALLOWED_CELL = "Not allowed for this cell"
        const val DATA_VIEW_HAS_NO_VIEW_MSG = "Data view has no view."
        const val TOAST_SET_NOT_EXIST = "This object doesn't exist"
    }
}