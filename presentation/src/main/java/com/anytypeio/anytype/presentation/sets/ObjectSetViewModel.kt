package com.anytypeio.anytype.presentation.sets

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.analytics.base.EventsPropertiesKey
import com.anytypeio.anytype.analytics.base.sendEvent
import com.anytypeio.anytype.analytics.props.Props
import com.anytypeio.anytype.core_models.DV
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.RelationLink
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.SyncStatus
import com.anytypeio.anytype.core_models.ext.title
import com.anytypeio.anytype.core_models.restrictions.DataViewRestriction
import com.anytypeio.anytype.core_utils.common.EventWrapper
import com.anytypeio.anytype.core_utils.ext.cancel
import com.anytypeio.anytype.domain.`object`.UpdateDetail
import com.anytypeio.anytype.domain.base.Result
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.block.interactor.UpdateText
import com.anytypeio.anytype.domain.cover.SetDocCoverImage
import com.anytypeio.anytype.domain.dataview.SetDataViewSource
import com.anytypeio.anytype.domain.dataview.interactor.CreateDataViewObject
import com.anytypeio.anytype.domain.dataview.interactor.UpdateDataViewViewer
import com.anytypeio.anytype.domain.error.Error
import com.anytypeio.anytype.domain.event.interactor.InterceptEvents
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.page.CloseBlock
import com.anytypeio.anytype.domain.page.CreateNewObject
import com.anytypeio.anytype.domain.search.CancelSearchSubscription
import com.anytypeio.anytype.domain.search.DataViewSubscriptionContainer
import com.anytypeio.anytype.domain.sets.OpenObjectSet
import com.anytypeio.anytype.domain.status.InterceptThreadStatus
import com.anytypeio.anytype.domain.unsplash.DownloadUnsplashImage
import com.anytypeio.anytype.presentation.common.Action
import com.anytypeio.anytype.presentation.common.Delegator
import com.anytypeio.anytype.presentation.editor.cover.CoverImageHashProvider
import com.anytypeio.anytype.presentation.editor.editor.Proxy
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.model.TextUpdate
import com.anytypeio.anytype.presentation.extension.sendAnalyticsShowSetEvent
import com.anytypeio.anytype.presentation.mapper.toDomain
import com.anytypeio.anytype.presentation.navigation.AppNavigation
import com.anytypeio.anytype.presentation.navigation.SupportNavigation
import com.anytypeio.anytype.presentation.relations.ObjectSetConfig.DEFAULT_LIMIT
import com.anytypeio.anytype.presentation.relations.render
import com.anytypeio.anytype.presentation.relations.tabs
import com.anytypeio.anytype.presentation.relations.title
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import com.anytypeio.anytype.presentation.sets.model.CellView
import com.anytypeio.anytype.presentation.sets.model.FilterExpression
import com.anytypeio.anytype.presentation.sets.model.SortingExpression
import com.anytypeio.anytype.presentation.sets.model.Viewer
import com.anytypeio.anytype.presentation.sets.model.ViewerTabView
import com.anytypeio.anytype.presentation.util.Dispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import timber.log.Timber

class ObjectSetViewModel(
    private val database: ObjectSetDatabase,
    private val reducer: ObjectSetReducer,
    private val openObjectSet: OpenObjectSet,
    private val closeBlock: CloseBlock,
    private val updateDataViewViewer: UpdateDataViewViewer,
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
    private val createNewObject: CreateNewObject,
    private val dataViewSubscriptionContainer: DataViewSubscriptionContainer,
    private val cancelSearchSubscription: CancelSearchSubscription,
    private val setDataViewSource: SetDataViewSource,
    private val paginator: ObjectSetPaginator,
    private val storeOfRelations: StoreOfRelations
) : ViewModel(), SupportNavigation<EventWrapper<AppNavigation.Command>> {

    val status = MutableStateFlow(SyncStatus.UNKNOWN)
    val error = MutableStateFlow<String?>(null)

    val title = MutableStateFlow<BlockView.Title?>(null)
    val featured = MutableStateFlow<BlockView.FeaturedRelation?>(null)

    private val _viewerTabs = MutableStateFlow<List<ViewerTabView>>(emptyList())
    val viewerTabs = _viewerTabs.asStateFlow()

    override val navigation = MutableLiveData<EventWrapper<AppNavigation.Command>>()

    private val titleUpdateChannel = Channel<TextUpdate>()

    private val defaultPayloadConsumer: suspend (Payload) -> Unit = { payload ->
        reducer.dispatch(payload.events)
    }

    val pagination get() = paginator.pagination

    private val jobs = mutableListOf<Job>()

    private val _commands = MutableSharedFlow<ObjectSetCommand>(replay = 0)
    val commands: SharedFlow<ObjectSetCommand> = _commands
    val toasts = Proxy.Subject<String>()

    private val _currentViewer: MutableStateFlow<Viewer?> = MutableStateFlow(null)
    val currentViewer = _currentViewer

    private val _header = MutableStateFlow<BlockView.Title.Basic?>(null)
    val header: StateFlow<BlockView.Title.Basic?> = _header

    val isCustomizeViewPanelVisible = MutableStateFlow(false)

    val isLoading = MutableStateFlow(false)

    private var analyticsContext: String? = null

    private var context: Id = ""

    init {

        viewModelScope.launch {
            dispatcher.flow().collect { defaultPayloadConsumer(it) }
        }

        viewModelScope.launch {
            reducer.state.collect { set ->
                Timber.d("FLOW:: Updating header and tabs")
                featured.value = set.featuredRelations(
                    ctx = context,
                    urlBuilder = urlBuilder,
                    relations = storeOfRelations.getAll()
                )
                _header.value = set.blocks.title()?.let {
                    title(
                        ctx = context,
                        coverImageHashProvider = coverImageHashProvider,
                        urlBuilder = urlBuilder,
                        details = set.details,
                        title = it
                    )
                }
                if (set.isInitialized) {
                    if (set.viewers.isEmpty()) {
                        error.value = DATA_VIEW_HAS_NO_VIEW_MSG
                        _viewerTabs.value = emptyList()
                    } else {
                        _viewerTabs.value = set.tabs(session.currentViewerId.value)
                    }
                }
            }
        }

        viewModelScope.launch {
            combine(
                reducer.state.filter { it.isInitialized },
                session.currentViewerId,
                paginator.offset
            ) { s, v, o ->
                val dv = s.dv
                val view = dv.viewers.find { it.id == v } ?: dv.viewers.firstOrNull()
                if (view != null) {
                    val dataViewKeys = dv.relationsIndex.map { it.key }
                    val defaultKeys = ObjectSearchConstants.defaultDataViewKeys
                    DataViewSubscriptionContainer.Params(
                        subscription = context,
                        sorts = view.sorts,
                        filters = view.filters.map { f : DVFilter ->
                            val r = storeOfRelations.getByKey(f.relationKey)
                            if (r != null && r.relationFormat == RelationFormat.DATE) {
                                f.copy(
                                    relationFormat = r.relationFormat
                                )
                            } else {
                                f
                            }
                        },
                        sources = dv.sources,
                        keys = defaultKeys + dataViewKeys,
                        limit = DEFAULT_LIMIT,
                        offset = o
                    )
                } else {
                    null
                }
            }.distinctUntilChanged().flatMapLatest { params ->
                if (params != null) {
                    dataViewSubscriptionContainer.observe(params)
                } else {
                    emptyFlow()
                }
            }.collect { index ->
                Timber.d("New index: ${index.objects.map { it.takeLast(5) }.joinToString { it }}")
                database.update(update = index)
            }
        }

        viewModelScope.launch {
            combine(
                reducer.state.filter { it.isInitialized },
                database.index,
                session.currentViewerId
            ) { state, db, view ->
                val dv = state.dv
                Timber.d("FLOW:: Rendering")
                val relations = dv.relationsIndex.mapNotNull { r: RelationLink ->
                    storeOfRelations.getByKey(r.key)
                }
                Timber.d("Relation index before rendering: ${dv.relationsIndex}")
                (dv.viewers.find { it.id == view } ?: dv.viewers.firstOrNull())?.render(
                    coverImageHashProvider = coverImageHashProvider,
                    builder = urlBuilder,
                    objects = db.objects,
                    dataViewRelations = relations,
                    details = state.details,
                    store = dataViewSubscriptionContainer.store
                )
            }.collect {
                _currentViewer.value = it?.viewer
            }
        }

        viewModelScope.launch {
            dataViewSubscriptionContainer.counter.collect { counter ->
                Timber.d("SET-DB: counter —>\n$counter")
                paginator.total.value = counter.total
            }
        }

        viewModelScope.launch {
            reducer.effects.collect { effects ->
                effects.forEach { effect ->
                    Timber.d("Received side effect: $effect")
                }
            }
        }

        viewModelScope.launch { reducer.run() }

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
                        success = { Timber.d("Sets' title updated successfully") }
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

        jobs += viewModelScope.launch {
            interceptEvents
                .build(InterceptEvents.Params(context))
                .collect { events -> reducer.dispatch(events) }
        }

        jobs += viewModelScope.launch {
            interceptThreadStatus
                .build(InterceptThreadStatus.Params(ctx))
                .collect { status.value = it }
        }

        jobs += viewModelScope.launch {
            isLoading.value = true
            openObjectSet(ctx).process(
                success = { result ->
                    isLoading.value = false
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
                            defaultPayloadConsumer(result.data)
                            proceedWithSourceCheck()
                            setAnalyticsContext(result.data.events)
                            sendAnalyticsShowSetEvent(analytics, analyticsContext)
                        }
                    }
                },
                failure = {
                    isLoading.value = false
                    Timber.e(it, "Error while opening object set: $ctx")
                }
            )
        }
    }

    private fun proceedWithSourceCheck() {
        val state = reducer.state.value
        val obj = ObjectWrapper.Basic(state.details[context]?.map ?: emptyMap())
        if (obj.setOf.isNotEmpty()) {
            if (!state.isInitialized) {
                error.value = DATA_VIEW_NOT_FOUND_ERROR
            }
        } else {
            dispatch(ObjectSetCommand.Modal.OpenEmptyDataViewSelectSourceScreen)
        }
    }

    private fun setAnalyticsContext(events: List<Event>) {
        if (events.isNotEmpty()) {
            val event = events[0]
            if (event is Event.Command.ShowObject) {
                val block = event.blocks.firstOrNull { it.id == event.context }
                analyticsContext = block?.fields?.analyticsContext
            }
        }
    }

    fun onStop() {
        Timber.d("onStop, ")
        reducer.state.value = ObjectSet.reset()
        _header.value = null
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
        closeBlock.execute(context).fold(
            onSuccess = { dispatch(AppNavigation.Command.Exit) },
            onFailure = {
                Timber.e(it, "Error while closing object set: $context").also {
                    dispatch(AppNavigation.Command.Exit)
                }
            }
        )
    }

    fun onCreateNewViewerClicked() {
        Timber.d("onCreateNewViewerClicked, ")
        dispatch(
            ObjectSetCommand.Modal.CreateViewer(
                ctx = context,
                target = reducer.state.value.dataview.id
            )
        )
    }

    fun onViewerTabClicked(viewer: Id) {
        Timber.d("onViewerTabClicked, viewer:[$viewer]")
        session.currentViewerId.value = viewer
    }

    fun onTitleChanged(txt: String) {
        Timber.d("onTitleChanged, txt:[$txt]")
        val target = header.value?.id
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
                            ctx = context,
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

    fun onGridCellClicked(cell: CellView) {
        Timber.d("onGridCellClicked, cell:[$cell]")

        if (cell.relationKey == Relations.NAME) return

        val state = reducer.state.value

        if (!state.isInitialized) {
            Timber.e("State was not initialized or cleared when cell is clicked")
            return
        }

        val block = state.dataview
        val dv = block.content as DV
        val viewer =
            dv.viewers.find { it.id == session.currentViewerId.value }?.id ?: dv.viewers.first().id

        if (dv.isRelationReadOnly(relationKey = cell.relationKey)) {
            val relation = dv.relations.first { it.key == cell.relationKey }
            if (relation.format == Relation.Format.OBJECT) {
                // TODO terrible workaround, which must be removed in the future!
                if (cell is CellView.Object && cell.objects.isNotEmpty()) {
                    val obj = cell.objects.first()
                    onRelationObjectClicked(target = obj.id)
                    return
                } else {
                    toast(NOT_ALLOWED_CELL)
                    return
                }
            } else {
                Timber.d("onGridCellClicked, relation is ReadOnly")
                toast(NOT_ALLOWED_CELL)
                return
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
                        dataview = block.id,
                        relationKey = cell.relationKey,
                        viewer = viewer,
                        targetObjectTypes = emptyList()
                    )
                )
            }
            is CellView.Object -> {
                viewModelScope.launch {
                    val targetObjectTypes = mutableListOf<String>()
                    val relation = storeOfRelations.getByKey(cell.relationKey)
                    if (relation != null) {
                        targetObjectTypes.addAll(relation.relationFormatObjectTypes)
                    }
                    dispatch(
                        ObjectSetCommand.Modal.EditRelationCell(
                            ctx = context,
                            target = cell.id,
                            dataview = block.id,
                            relationKey = cell.relationKey,
                            viewer = viewer,
                            targetObjectTypes = targetObjectTypes
                        )
                    )
                }
            }
            is CellView.Checkbox -> {
                viewModelScope.launch {
                    setObjectDetails(
                        UpdateDetail.Params(
                            ctx = cell.id,
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
    private fun onRelationObjectClicked(target: Id) {
        Timber.d("onCellObjectClicked, id:[$target]")
        val set = reducer.state.value
        if (set.isInitialized) {
            val obj = ObjectWrapper.Basic(set.details[target]?.map ?: emptyMap())
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
        val set = reducer.state.value
        if (set.isInitialized) {
            viewModelScope.launch {
                val obj = database.store.get(target)
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
    }

    fun onTaskCheckboxClicked(target: Id) {
        Timber.d("onTaskCheckboxClicked: $target")
        viewModelScope.launch {
            val obj = database.store.get(target)
            if (obj != null) {
                setObjectDetails(
                    UpdateDetail.Params(
                        ctx = target,
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
        viewModelScope.launch {
            setObjectDetails(
                UpdateDetail.Params(
                    ctx = objectId,
                    key = relationKey,
                    value = value
                )
            ).process(
                failure = { Timber.e(it, "Error while updating data view record") },
                success = {
                    Timber.d("Data view record updated successfully")
                }
            )
        }
    }

    fun onUpdateViewerSorting(sorts: List<SortingExpression>) {
        Timber.d("onUpdateViewerSorting, sorts:[$sorts]")
        viewModelScope.launch {
            val block = reducer.state.value.dataview
            val dv = block.content as DV
            val viewer =
                dv.viewers.find { it.id == session.currentViewerId.value } ?: dv.viewers.first()
            updateDataViewViewer(
                UpdateDataViewViewer.Params(
                    context = context,
                    target = block.id,
                    viewer = viewer.copy(sorts = sorts.map { it.toDomain() })
                )
            ).process(
                success = { payload ->
                    defaultPayloadConsumer(payload)
                },
                failure = { Timber.e(it, "Error while updating data view's viewer") }
            )
        }
    }

    fun onUpdateViewerFilters(filters: List<FilterExpression>) {
        Timber.d("onUpdateViewerFilters, filters:[$filters]")
        viewModelScope.launch {
            val block = reducer.state.value.dataview
            val dv = block.content as DV
            val viewer =
                dv.viewers.find { it.id == session.currentViewerId.value } ?: dv.viewers.first()
            updateDataViewViewer(
                UpdateDataViewViewer.Params(
                    context = context,
                    target = block.id,
                    viewer = viewer.copy(filters = filters.map { it.toDomain() })
                )
            ).process(
                success = { payload ->
                    defaultPayloadConsumer(payload)
                },
                failure = { Timber.e(it, "Error while updating data view's viewer") }
            )
        }
    }

    fun onCreateNewDataViewObject() {
        Timber.d("onCreateNewRecord, ")
        val currentState = reducer.state.value
        if (currentState.isInitialized) {
            if (isRestrictionPresent(DataViewRestriction.CREATE_OBJECT)) {
                toast(NOT_ALLOWED)
            } else {
                val setObject = ObjectWrapper.Basic(
                    currentState.details[context]?.map ?: emptyMap()
                )
                val viewer = currentState.viewerById(session.currentViewerId.value)
                val sourceId = setObject.setOf.singleOrNull()
                if (sourceId == null) {
                    toast("Unabled to define source for new object")
                } else {
                    val sourceDetails = currentState.details[sourceId]
                    if (sourceDetails != null && sourceDetails.map.isNotEmpty()) {
                        when(sourceDetails.type.firstOrNull()) {
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
                                            filters = viewer.filters
                                        )
                                    )
                                }
                            }
                            ObjectTypeIds.RELATION -> {
                                proceedWithCreatingDataViewObject(
                                    CreateDataViewObject.Params.SetByRelation(
                                        filters = viewer.filters,
                                        relations = setObject.setOf
                                    )
                                )
                            }
                        }
                    } else {
                        toast("Unabled to define source for new object")
                    }
                }
            }
        } else {
            toast("Data view is not initialized yet.")
        }
    }

    private fun proceedWithCreatingDataViewObject(params: CreateDataViewObject.Params) {
        viewModelScope.launch {
            createDataViewObject(params).process(
                failure = { Timber.e(it, "Error while creating new record") },
                success = { record ->
                    dispatch(
                        ObjectSetCommand.Modal.SetNameForCreatedObject(
                            ctx = context,
                            target = record
                        )
                    )
                }
            )
        }
    }

    fun onViewerCustomizeButtonClicked() {
        Timber.d("onViewerCustomizeButtonClicked, ")
        if (!reducer.state.value.isInitialized) {
            toast("Set is not initialized.")
            return
        }
        isCustomizeViewPanelVisible.value = !isCustomizeViewPanelVisible.value
    }

    fun onHideViewerCustomizeSwiped() {
        Timber.d("onHideViewerCustomizeSwiped, ")
        isCustomizeViewPanelVisible.value = false
    }

    fun onViewerCustomizeClicked() {
        Timber.d("onViewerCustomizeClicked, ")
        val set = reducer.state.value
        if (set.isInitialized) {
            val block = set.dataview
            val dv = block.content as DV
            val viewer =
                dv.viewers.find { it.id == session.currentViewerId.value } ?: dv.viewers.first()
            dispatch(
                ObjectSetCommand.Modal.ViewerCustomizeScreen(
                    ctx = context,
                    viewer = viewer.id
                )
            )
        }
    }

    fun onExpandViewerMenuClicked() {
        Timber.d("onExpandViewerMenuClicked, ")
        if (!reducer.state.value.isInitialized) {
            toast("Set is not initialized.")
            return
        }
        if (isRestrictionPresent(DataViewRestriction.VIEWS)
        ) {
            toast(NOT_ALLOWED)
        } else {
            dispatch(
                ObjectSetCommand.Modal.ManageViewer(
                    ctx = context,
                    dataview = reducer.state.value.dataview.id
                )
            )
        }
    }

    fun onViewerEditClicked() {
        Timber.d("onViewerEditClicked, ")
        val set = reducer.state.value
        if (set.isInitialized) {
            val block = set.dataview
            val dv = block.content as DV
            val viewer =
                dv.viewers.find { it.id == session.currentViewerId.value } ?: dv.viewers.first()
            dispatch(
                ObjectSetCommand.Modal.EditDataViewViewer(
                    ctx = context,
                    dataview = block.id,
                    viewer = viewer.id,
                    name = viewer.name
                )
            )
        }
    }

    fun onMenuClicked() {
        Timber.d("onMenuClicked, ")
        val set = reducer.state.value
        if (!isLoading.value) {
            dispatch(
                ObjectSetCommand.Modal.Menu(
                    ctx = context,
                    isArchived = set.details[context]?.isArchived ?: false,
                    isFavorite = set.details[context]?.isFavorite ?: false,
                )
            )
        } else {
            toast("Still loading...")
        }
    }

    fun onIconClicked() {
        Timber.d("onIconClicked, ")
        if (!isLoading.value) {
            dispatch(
                ObjectSetCommand.Modal.OpenIconActionMenu(target = context)
            )
        } else {
            toast("Still loading ...")
        }
    }

    fun onCoverClicked() {
        Timber.d("onCoverClicked, ")
        if (!isLoading.value) {
            dispatch(
                ObjectSetCommand.Modal.OpenCoverActionMenu(ctx = context)
            )
        } else {
            toast("Still loading ...")
        }
    }

    fun onViewerSettingsClicked() {
        Timber.d("onViewerRelationsClicked, ")
        if (isRestrictionPresent(DataViewRestriction.RELATION)) {
            toast(NOT_ALLOWED)
        } else {
            val set = reducer.state.value
            if (set.isInitialized) {
                val block = set.dataview
                val dv = block.content as DV
                if (dv.viewers.isNotEmpty()) {
                    val viewer =
                        dv.viewers.find { it.id == session.currentViewerId.value }
                            ?: dv.viewers.first()
                    dispatch(
                        ObjectSetCommand.Modal.OpenSettings(
                            ctx = context,
                            dv = block.id,
                            viewer = viewer.id
                        )
                    )
                } else {
                    toast(DATA_VIEW_HAS_NO_VIEW_MSG)
                }
            }
        }
    }

    fun onViewerFiltersClicked() {
        Timber.d("onViewerFiltersClicked, ")
        val set = reducer.state.value
        if (set.isInitialized) {
            if (set.viewers.isNotEmpty()) {
                if (isRestrictionPresent(DataViewRestriction.VIEWS)) {
                    toast(NOT_ALLOWED)
                } else {
                    dispatch(
                        ObjectSetCommand.Modal.ModifyViewerFilters(ctx = context)
                    )
                }
            } else {
                toast(DATA_VIEW_HAS_NO_VIEW_MSG)
            }
        }
    }

    fun onViewerSortsClicked() {
        Timber.d("onViewerSortsClicked, ")
        val set = reducer.state.value
        if (set.isInitialized) {
            if (set.viewers.isNotEmpty()) {
                if (isRestrictionPresent(DataViewRestriction.VIEWS)) {
                    toast(NOT_ALLOWED)
                } else {
                    dispatch(
                        ObjectSetCommand.Modal.ModifyViewerSorts(ctx = context)
                    )
                }
            } else {
                toast(DATA_VIEW_HAS_NO_VIEW_MSG)
            }
        }
    }

    private fun dispatch(command: ObjectSetCommand) {
        viewModelScope.launch { _commands.emit(command) }
    }

    private fun dispatch(command: AppNavigation.Command) {
        navigate(EventWrapper(command))
    }

    private fun toast(toast: String) {
        viewModelScope.launch { toasts.send(toast) }
    }

    private fun isRestrictionPresent(restriction: DataViewRestriction): Boolean {
        val set = reducer.state.value
        val block = set.dataview
        val dVRestrictions = set.restrictions.firstOrNull { it.block == block.id }
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

    private fun proceedWithOpeningObject(target: Id) {
        jobs += viewModelScope.launch {
            closeBlock.execute(context).fold(
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

    private fun proceedWithNavigation(target: Id, layout: ObjectType.Layout?) {
        when (layout) {
            ObjectType.Layout.BASIC,
            ObjectType.Layout.PROFILE,
            ObjectType.Layout.TODO,
            ObjectType.Layout.NOTE,
            ObjectType.Layout.IMAGE,
            ObjectType.Layout.FILE,
            ObjectType.Layout.BOOKMARK -> proceedWithOpeningObject(target)
            ObjectType.Layout.SET -> {
                viewModelScope.launch {
                    closeBlock.execute(context).fold(
                        onSuccess = {
                            navigate(EventWrapper(AppNavigation.Command.OpenObjectSet(target)))
                        },
                        onFailure = {
                            Timber.e(it, "Error while closing object set: $context")
                            navigate(EventWrapper(AppNavigation.Command.OpenObjectSet(target)))
                        }
                    )
                }
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
        super.onCleared()
        titleUpdateChannel.cancel()
        reducer.clear()
    }

    fun onHomeButtonClicked() {
        viewModelScope.launch {
            closeBlock.execute(context).fold(
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

        viewModelScope.sendEvent(
            analytics = analytics,
            eventName = EventsDictionary.createObjectNavBar,
            props = Props(mapOf(EventsPropertiesKey.context to analyticsContext))
        )
        jobs += viewModelScope.launch {
            createNewObject.execute(Unit).fold(
                onSuccess = { id ->
                    proceedWithOpeningObject(id)
                },
                onFailure = { e -> Timber.e(e, "Error while creating a new page") }
            )
        }
    }

    fun onSearchButtonClicked() {
        viewModelScope.launch {
            closeBlock.execute(context).fold(
                onSuccess = { dispatch(AppNavigation.Command.OpenPageSearch) },
                onFailure = { Timber.e(it, "Error while closing object set: $context") }
            )
        }
    }

    fun onClickListener(clicked: ListenerType) {
        Timber.d("onClickListener, clicked:[$clicked]")
        when (clicked) {
            is ListenerType.Relation.SetSource -> {
                val sources = clicked.sources.map { it.id }
                val command = if (sources.isEmpty()) {
                    ObjectSetCommand.Modal.OpenEmptyDataViewSelectSourceScreen
                } else {
                    ObjectSetCommand.Modal.OpenDataViewSelectSourceScreen(
                        selectedTypes = sources
                    )
                }
                dispatch(command)
            }
            is ListenerType.Relation.ChangeSourceByRelation -> {
                toast("Currently, this source can be changed via Desktop only")
            }
            else -> {}
        }
    }

    fun onDataViewSourcePicked(source: Id) {
        viewModelScope.launch {
            val params = SetDataViewSource.Params(
                ctx = context,
                sources = listOf(source)
            )
            setDataViewSource(params).proceed(
                failure = { e -> Timber.e(e, "Error while setting Set source") },
                success = { payload -> defaultPayloadConsumer(payload) }
            )
        }
    }

    companion object {
        const val TITLE_CHANNEL_DISPATCH_DELAY = 300L
        const val NOT_ALLOWED = "Not allowed for this set"
        const val NOT_ALLOWED_CELL = "Not allowed for this cell"
        const val DATA_VIEW_HAS_NO_VIEW_MSG = "Data view has no view."
        const val DATA_VIEW_NOT_FOUND_ERROR =
            "Content missing for this set. Please, try again later."
        const val OBJECT_SET_HAS_EMPTY_SOURCE_ERROR =
            "Object type is not defined for this set. Please, setup object type on Desktop."
        const val TOAST_SET_NOT_EXIST = "This object doesn't exist"
    }
}