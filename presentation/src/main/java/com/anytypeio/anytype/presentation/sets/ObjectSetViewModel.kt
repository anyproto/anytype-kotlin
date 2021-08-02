package com.anytypeio.anytype.presentation.sets

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary.SCREEN_SET
import com.anytypeio.anytype.analytics.base.EventsDictionary.SETS_RECORD_CREATE
import com.anytypeio.anytype.analytics.base.EventsDictionary.SETS_RELATION_TEXT_VALUE_UPDATE
import com.anytypeio.anytype.analytics.base.EventsDictionary.SETS_VIEWER_ACTIVE
import com.anytypeio.anytype.analytics.base.EventsDictionary.SETS_VIEWER_FILTER_UPDATE
import com.anytypeio.anytype.analytics.base.EventsDictionary.SETS_VIEWER_RELATION_UPDATE
import com.anytypeio.anytype.analytics.base.EventsDictionary.SETS_VIEWER_SORT_UPDATE
import com.anytypeio.anytype.analytics.base.sendEvent
import com.anytypeio.anytype.analytics.event.EventAnalytics
import com.anytypeio.anytype.core_models.*
import com.anytypeio.anytype.core_models.ext.content
import com.anytypeio.anytype.core_models.restrictions.DataViewRestriction
import com.anytypeio.anytype.core_utils.common.EventWrapper
import com.anytypeio.anytype.domain.block.interactor.UpdateText
import com.anytypeio.anytype.domain.dataview.interactor.*
import com.anytypeio.anytype.domain.event.interactor.InterceptEvents
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.page.CloseBlock
import com.anytypeio.anytype.domain.sets.OpenObjectSet
import com.anytypeio.anytype.domain.status.InterceptThreadStatus
import com.anytypeio.anytype.presentation.mapper.toDomain
import com.anytypeio.anytype.presentation.navigation.AppNavigation
import com.anytypeio.anytype.presentation.navigation.SupportNavigation
import com.anytypeio.anytype.presentation.page.editor.Proxy
import com.anytypeio.anytype.presentation.page.editor.model.BlockView
import com.anytypeio.anytype.presentation.page.model.TextUpdate
import com.anytypeio.anytype.presentation.relations.*
import com.anytypeio.anytype.presentation.sets.model.*
import com.anytypeio.anytype.presentation.util.Dispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.math.ceil

class ObjectSetViewModel(
    private val reducer: ObjectSetReducer,
    private val openObjectSet: OpenObjectSet,
    private val closeBlock: CloseBlock,
    private val setActiveViewer: SetActiveViewer,
    private val addDataViewRelation: AddNewRelationToDataView,
    private val updateDataViewViewer: UpdateDataViewViewer,
    private val updateDataViewRecord: UpdateDataViewRecord,
    private val createDataViewRecord: CreateDataViewRecord,
    private val updateText: UpdateText,
    private val interceptEvents: InterceptEvents,
    private val interceptThreadStatus: InterceptThreadStatus,
    private val dispatcher: Dispatcher<Payload>,
    private val objectSetRecordCache: ObjectSetRecordCache,
    private val urlBuilder: UrlBuilder,
    private val session: ObjectSetSession,
    private val analytics: Analytics
) : ViewModel(), SupportNavigation<EventWrapper<AppNavigation.Command>> {

    val status = MutableStateFlow(SyncStatus.UNKNOWN)
    val error = MutableStateFlow<String?>(null)

    private val total = MutableStateFlow(0)
    private val offset = MutableStateFlow(0)

    val featured = MutableStateFlow<BlockView.FeaturedRelation?>(null)

    val pagination = total.combine(offset) { t, o ->
        val idx = ceil(o.toDouble() / ObjectSetConfig.DEFAULT_LIMIT).toInt()
        val pages = ceil(t.toDouble() / ObjectSetConfig.DEFAULT_LIMIT).toInt()
        Pair(idx, pages)
    }

    private val _viewerTabs = MutableStateFlow<List<ViewerTabView>>(emptyList())
    val viewerTabs = _viewerTabs.asStateFlow()

    override val navigation = MutableLiveData<EventWrapper<AppNavigation.Command>>()

    private val titleUpdateChannel = Channel<TextUpdate>()

    private val defaultPayloadConsumer: suspend (Payload) -> Unit = { payload ->
        reducer.dispatch(payload.events)
    }

    private val defaultPayloadWithEvent: suspend (Pair<Payload, EventAnalytics.Anytype>) -> Unit =
        { (payload, event) ->
            reducer.dispatch(payload.events)
            analytics.registerEvent(
                event.copy(
                    duration = event.duration?.copy(
                        render = System.currentTimeMillis()
                    )
                )
            )
        }

    private val jobs = mutableListOf<Job>()

    private val _commands = MutableSharedFlow<ObjectSetCommand>(replay = 0)
    val commands: SharedFlow<ObjectSetCommand> = _commands
    val toasts = Proxy.Subject<String>()

    private val _viewerGrid = MutableStateFlow(Viewer.GridView.empty())
    val viewerGrid = _viewerGrid

    private val _header = MutableStateFlow<BlockView.Title.Basic?>(null)
    val header: StateFlow<BlockView.Title.Basic?> = _header

    val isCustomizeViewPanelVisible = MutableStateFlow(false)

    val isLoading = MutableStateFlow(false)

    private lateinit var context: Id

    init {
        viewModelScope.launch {
            dispatcher.flow().collect(defaultPayloadConsumer)
        }

        viewModelScope.launch {
            reducer.state.filter { it.isInitialized }.collect { set ->
                Timber.d("Set updated!")
                if (set.viewers.isNotEmpty()) {
                    _viewerTabs.value = set.tabs(session.currentViewerId)
                    val viewerIndex = set.viewers.indexOfFirst { it.id == session.currentViewerId }
                    set.render(viewerIndex, context, urlBuilder).let { vs ->
                        _viewerGrid.value = vs.viewer
                        _header.value = vs.title
                    }
                    featured.value = set.featuredRelations(
                        ctx = context,
                        urlBuilder = urlBuilder
                    )
                } else {
                    Timber.e("Data view contained no view")
                    error.value = DATA_VIEW_HAS_NO_VIEW_MSG
                    _header.value = set.title(
                        ctx = context,
                        urlBuilder = urlBuilder
                    )
                    featured.value = set.featuredRelations(
                        ctx = context,
                        urlBuilder = urlBuilder
                    )
                }
            }
        }

        viewModelScope.launch {
            reducer.effects.collect { effects ->
                effects.forEach { effect ->
                    when (effect) {
                        is ObjectSetReducer.SideEffect.ResetOffset -> {
                            offset.value = effect.offset
                        }
                        is ObjectSetReducer.SideEffect.ResetCount -> {
                            total.value = effect.count
                        }
                    }
                }
            }
        }

        viewModelScope.launch { reducer.run() }

        // Title updates pipleine

        viewModelScope.launch {
            titleUpdateChannel
                .consumeAsFlow()
                .distinctUntilChanged()
                .debounce(TITLE_CHANNEL_DISPATCH_DELAY)
                .map {
                    UpdateText.Params(
                        context = context,
                        target = it.target,
                        text = it.text,
                        marks = emptyList()
                    )
                }
                .mapLatest { params -> updateText(params) }
                .collect()
        }
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

        viewModelScope.launch {
            isLoading.value = true
            openObjectSet(ctx).process(
                success = { payload ->
                    defaultPayloadConsumer(payload).also { isLoading.value = false }
                    proceedWithStartupPaging()
                },
                failure = {
                    isLoading.value = false
                    Timber.e(it, "Error while opening object set: $ctx")
                }
            )
        }
    }

    fun onResume() {
        viewModelScope.sendEvent(
            analytics = analytics,
            eventName = SCREEN_SET
        )
    }

    fun onStop() {
        Timber.d("onStop, ")
        jobs.forEach { it.cancel() }
    }

    fun onSystemBackPressed() {
        Timber.d("onSystemBackPressed, ")
        proceedWithExiting()
    }

    private fun proceedWithExiting() {
        viewModelScope.launch {
            closeBlock(CloseBlock.Params(context)).process(
                success = { dispatch(AppNavigation.Command.Exit) },
                failure = { Timber.e(it, "Error while closing object set: $context") }
            )
        }
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

    @Deprecated("uses only in tests")
    fun onViewerTabClicked(viewer: Id) {
        Timber.d("onViewerTabClicked, viewer:[$viewer]")
        viewModelScope.launch {
            session.currentViewerId = viewer
            setActiveViewer(
                SetActiveViewer.Params(
                    context = context,
                    block = reducer.state.value.dataview.id,
                    view = viewer,
                    limit = ObjectSetConfig.DEFAULT_LIMIT,
                    offset = 0
                )
            ).process(
                failure = { Timber.e(it, "Error while setting active viewer") },
                success = defaultPayloadConsumer
            )
        }
    }

    fun onRelationPrototypeCreated(
        context: Id,
        target: Id,
        name: String,
        format: Relation.Format
    ) {
        Timber.d("onRelationPrototypeCreated, context:[$context], target:[$target], name:[$name], format:[$format]")
        viewModelScope.launch {
            addDataViewRelation(
                AddNewRelationToDataView.Params(
                    ctx = context,
                    target = target,
                    name = name,
                    format = format
                )
            ).process(
                failure = { Timber.e(it, "Error while adding data view relation") },
                success = { (key, payload) ->
                    reducer.dispatch(payload.events).also {
                        proceedWithAddingNewRelationToCurrentViewer(relation = key)
                    }
                }
            )
        }
    }

    private suspend fun proceedWithAddingNewRelationToCurrentViewer(relation: Id) {
        val state = reducer.state.value
        val block = state.dataview
        val dv = block.content as DV
        val viewer = dv.viewers.find {
            it.id == session.currentViewerId
        } ?: dv.viewers.first()
        val start = System.currentTimeMillis()
        updateDataViewViewer(
            UpdateDataViewViewer.Params(
                context = context,
                target = block.id,
                viewer = viewer.copy(
                    viewerRelations = viewer.viewerRelations + listOf(
                        DVViewerRelation(
                            key = relation,
                            isVisible = true
                        )
                    )
                )
            )
        ).process(
            success = { payload ->
                val event = EventAnalytics.Anytype(
                    name = SETS_VIEWER_RELATION_UPDATE,
                    duration = EventAnalytics.Duration(
                        start = start,
                        middleware = System.currentTimeMillis()
                    )
                )
                defaultPayloadWithEvent(Pair(payload, event))
            },
            failure = { Timber.e(it, "Error while updating data view's viewer") }
        )
    }

    fun onTitleChanged(txt: String) {
        Timber.d("onTitleChanged, txt:[$txt]")
        val target = header.value?.id
        checkNotNull(target) { "Title block was missing or not ready" }
        viewModelScope.launch {
            titleUpdateChannel.send(
                TextUpdate.Default(
                    text = txt,
                    target = target,
                    markup = emptyList()
                )
            )
        }
    }

    fun onGridCellClicked(cell: CellView) {
        Timber.d("onGridCellClicked, cell:[$cell]")

        if (cell.key == Relations.NAME) return

        val state = reducer.state.value
        val block = state.dataview
        val dv = block.content as DV
        val viewer = dv.viewers.find { it.id == session.currentViewerId }?.id
            ?: dv.viewers.first().id

        if (dv.isRelationReadOnly(relationKey = cell.key)) {
            val relation = dv.relations.first { it.key == cell.key }
            if (relation.format == Relation.Format.OBJECT) {
                // TODO terrible workaround, which must be removed in the future!
                if (cell is CellView.Object && cell.objects.isNotEmpty()) {
                    val obj = cell.objects.first()
                    onObjectClicked(
                        id = obj.id,
                        types = obj.types
                    )
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
            is CellView.Description, is CellView.Number, is CellView.Email,
            is CellView.Url, is CellView.Phone -> {
                dispatch(
                    ObjectSetCommand.Modal.EditGridTextCell(
                        ctx = context,
                        relationId = cell.key,
                        recordId = cell.id
                    )
                )
            }
            is CellView.Date -> {
                dispatch(
                    ObjectSetCommand.Modal.EditGridDateCell(
                        ctx = context,
                        objectId = cell.id,
                        relationId = cell.key
                    )
                )
            }
            is CellView.Tag, is CellView.Status, is CellView.Object, is CellView.File -> {
                dispatch(
                    ObjectSetCommand.Modal.EditRelationCell(
                        ctx = context,
                        target = cell.id,
                        dataview = block.id,
                        relation = cell.key,
                        viewer = viewer
                    )
                )
            }
            is CellView.Checkbox -> {
                val records = reducer.state.value.viewerDb[viewer] ?: return
                val record = records.records.find { it[ObjectSetConfig.ID_KEY] == cell.id }
                if (record != null) {
                    val updated = record.toMutableMap()
                    updated[cell.key] = !cell.isChecked
                    viewModelScope.launch {
                        updateDataViewRecord(
                            UpdateDataViewRecord.Params(
                                context = context,
                                record = cell.id,
                                target = block.id,
                                values = updated
                            )
                        ).process(
                            failure = { Timber.e(it, "Error while updating data view record") },
                            success = { Timber.d("Data view record updated successfully") }
                        )
                    }
                } else {
                    Timber.e("Couldn't found record for this checkobx")
                }
            }
            else -> toast("Not implemented")
        }
    }

    fun onObjectClicked(id: Id, types: List<String>?) {
        Timber.d("onObjectClicked, id:[$id], type:[$types]")

        if (types.isNullOrEmpty()) {
            Timber.e("onObjectClicked, types is null or empty, layout type unknown")
            toast(OBJECT_TYPE_UNKNOWN)
            return
        }

        val targetType = reducer.state.value.objectTypes.getObjectTypeById(types)

        if (targetType != null) {
            when (targetType.layout) {
                ObjectType.Layout.BASIC, ObjectType.Layout.PROFILE -> {
                    navigate(EventWrapper(AppNavigation.Command.OpenObject(id)))
                }
                ObjectType.Layout.SET -> {
                    viewModelScope.launch {
                        navigate(EventWrapper(AppNavigation.Command.OpenObjectSet(id)))
                    }
                }
                else -> Timber.d("Unexpected layout: ${targetType.layout}")
            }
        } else {
            Timber.e("onObjectClicked, types is null or empty, layout type unknown")
            toast(OBJECT_TYPE_UNKNOWN)
        }
    }

    fun onObjectHeaderClicked(id: Id, type: String?) {
        Timber.d("onObjectHeaderClicked, id:[$id], type:[$type]")
        val set = reducer.state.value
        val objectType = set.objectTypes.find { it.url == type }
        if (objectType == null) {
            toast("Object type not found: $type")
            return
        }
        when (objectType.layout) {
            ObjectType.Layout.BASIC, ObjectType.Layout.PROFILE, ObjectType.Layout.TODO -> {
                navigate(
                    EventWrapper(
                        AppNavigation.Command.OpenObject(
                            id = id
                        )
                    )
                )
            }
            else -> toast("Routing not implemented for this object type: $objectType")
        }
    }

    fun onRelationTextValueChanged(
        ctx: String,
        value: Any?,
        objectId: Id,
        relationKey: Id
    ) {
        Timber.d("onRelationTextValueChanged, ctx:[$ctx], value:[$value], objectId:[$objectId], relationKey:[$relationKey]")
        val block = reducer.state.value.dataview
        val dv = block.content as DV
        val records = reducer.state.value.viewerDb[dv.viewers.first().id] ?: return
        val record = records.records.find { it[ObjectSetConfig.ID_KEY] == objectId }
        if (record != null) {
            val updated = record.toMutableMap()
            updated[relationKey] = value
            viewModelScope.launch {
                val start = System.currentTimeMillis()
                updateDataViewRecord(
                    UpdateDataViewRecord.Params(
                        context = ctx,
                        record = objectId,
                        target = block.id,
                        values = updated
                    )
                ).process(
                    failure = { Timber.e(it, "Error while updating data view record") },
                    success = {
                        sendEvent(
                            analytics = analytics,
                            eventName = SETS_RELATION_TEXT_VALUE_UPDATE,
                            startTime = start,
                            middleTime = System.currentTimeMillis()
                        )
                        Timber.d("Data view record updated successfully")
                    }
                )
            }
        } else {
            Timber.e("Couldn't found record for the edited soft-input value cell")
        }
    }

    fun onUpdateViewerSorting(sorts: List<SortingExpression>) {
        Timber.d("onUpdateViewerSorting, sorts:[$sorts]")
        viewModelScope.launch {
            val start = System.currentTimeMillis()
            val block = reducer.state.value.dataview
            val dv = block.content as DV
            val viewer = dv.viewers.find { it.id == session.currentViewerId } ?: dv.viewers.first()
            updateDataViewViewer(
                UpdateDataViewViewer.Params(
                    context = context,
                    target = block.id,
                    viewer = viewer.copy(sorts = sorts.map { it.toDomain() })
                )
            ).process(
                success = { payload ->
                    val event = EventAnalytics.Anytype(
                        name = SETS_VIEWER_SORT_UPDATE,
                        duration = EventAnalytics.Duration(
                            start = start,
                            middleware = System.currentTimeMillis()
                        )
                    )
                    defaultPayloadWithEvent(Pair(payload, event))
                },
                failure = { Timber.e(it, "Error while updating data view's viewer") }
            )
        }
    }

    fun onUpdateViewerFilters(filters: List<FilterExpression>) {
        Timber.d("onUpdateViewerFilters, filters:[$filters]")
        viewModelScope.launch {
            val start = System.currentTimeMillis()
            val block = reducer.state.value.dataview
            val dv = block.content as DV
            val viewer = dv.viewers.find { it.id == session.currentViewerId } ?: dv.viewers.first()
            updateDataViewViewer(
                UpdateDataViewViewer.Params(
                    context = context,
                    target = block.id,
                    viewer = viewer.copy(filters = filters.map { it.toDomain() })
                )
            ).process(
                success = { payload ->
                    val event = EventAnalytics.Anytype(
                        name = SETS_VIEWER_FILTER_UPDATE,
                        duration = EventAnalytics.Duration(
                            start = start,
                            middleware = System.currentTimeMillis()
                        )
                    )
                    defaultPayloadWithEvent(Pair(payload, event))
                },
                failure = { Timber.e(it, "Error while updating data view's viewer") }
            )
        }
    }

    fun onCreateNewRecord() {
        Timber.d("onCreateNewRecord, ")
        if (reducer.state.value.isInitialized) {
            if (isRestrictionPresent(DataViewRestriction.CREATE_OBJECT)) {
                toast(NOT_ALLOWED)
            } else {
                val start = System.currentTimeMillis()
                viewModelScope.launch {
                    createDataViewRecord(
                        CreateDataViewRecord.Params(
                            context = context,
                            target = reducer.state.value.dataview.id
                        )
                    ).process(
                        failure = { Timber.e(it, "Error while creating new record") },
                        success = { record ->
                            val middle = System.currentTimeMillis()
                            sendEvent(
                                eventName = SETS_RECORD_CREATE,
                                startTime = start,
                                middleTime = middle,
                                analytics = analytics
                            )
                            total.value = total.value.inc()
                            proceedWithRefreshingViewerAfterObjectCreation()
                            objectSetRecordCache.map[context] = record
                            dispatch(ObjectSetCommand.Modal.SetNameForCreatedRecord(context))
                        }
                    )
                }
            }
        } else {
            toast("Data view is not initialized yet.")
        }
    }

    private suspend fun proceedWithRefreshingViewerAfterObjectCreation() {
        val set = reducer.state.value
        val viewer = set.viewerById(session.currentViewerId).id

        setActiveViewer(
            SetActiveViewer.Params(
                context = context,
                block = set.dataview.id,
                view = viewer,
                limit = ObjectSetConfig.DEFAULT_LIMIT,
                offset = offset.value
            )
        ).process(
            success = { payload -> defaultPayloadConsumer(payload) },
            failure = { Timber.e(it, "Error while refreshing viewer") }
        )
    }

    fun onViewerCustomizeButtonClicked() {
        Timber.d("onViewerCustomizeButtonClicked, ")
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
            val viewer = dv.viewers.find { it.id == session.currentViewerId } ?: dv.viewers.first()
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
            val viewer = dv.viewers.find { it.id == session.currentViewerId } ?: dv.viewers.first()
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
        if (set.isInitialized) {
            dispatch(
                ObjectSetCommand.Modal.Menu(
                    ctx = context,
                    isArchived = set.details[context]?.isArchived ?: false
                )
            )
        } else {
            toast("Set is not initialized. Please, try again later.")
        }
    }

    fun onIconClicked() {
        Timber.d("onIconClicked, ")
        val set = reducer.state.value
        if (set.isInitialized) {
            val header = _header.value
            dispatch(
                ObjectSetCommand.Modal.OpenIconActionMenu(
                    ctx = context,
                    emoji = header?.emoji,
                    image = header?.image
                )
            )
        }
    }

    fun onViewerRelationsClicked() {
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
                        dv.viewers.find { it.id == session.currentViewerId } ?: dv.viewers.first()
                    dispatch(
                        ObjectSetCommand.Modal.ModifyViewerRelationOrder(
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

    private suspend fun proceedWithStartupPaging() {
        val set = reducer.state.value.dataview
        val dv = set.content<Block.Content.DataView>()
        if (dv.viewers.isNotEmpty()) {
            val viewer = dv.viewers.find { it.id == session.currentViewerId } ?: dv.viewers.first()
            proceedWithViewerPaging(set = set, viewer = viewer.id)
        } else {
            Timber.e("Stopped initial paging: data view contained no view")
        }
    }

    fun onPaginatorToolbarNumberClicked(number: Int, isSelected: Boolean) {
        Timber.d("onPaginatorToolbarNumberClicked, number:[$number], isSelected:[$isSelected]")
        if (isSelected) {
            Timber.d("This page is already selected")
        } else {
            viewModelScope.launch {
                offset.value = number * ObjectSetConfig.DEFAULT_LIMIT
                val set = reducer.state.value.dataview
                val dv = set.content<Block.Content.DataView>()
                val viewer =
                    dv.viewers.find { it.id == session.currentViewerId } ?: dv.viewers.first()
                proceedWithViewerPaging(set = set, viewer = viewer.id)
            }
        }
    }

    fun onPaginatorNextElsePrevious(next: Boolean) {
        Timber.d("onPaginatorNextElsePrevious, next:[$next]")
        viewModelScope.launch {
            offset.value = if (next) {
                offset.value + ObjectSetConfig.DEFAULT_LIMIT
            } else {
                offset.value - ObjectSetConfig.DEFAULT_LIMIT
            }
            val set = reducer.state.value.dataview
            val dv = set.content<Block.Content.DataView>()
            val viewer = dv.viewers.find { it.id == session.currentViewerId } ?: dv.viewers.first()
            proceedWithViewerPaging(set = set, viewer = viewer.id)
        }
    }

    private suspend fun proceedWithViewerPaging(
        set: Block,
        viewer: Id
    ) {
        val start = System.currentTimeMillis()
        setActiveViewer(
            SetActiveViewer.Params(
                context = context,
                block = set.id,
                view = viewer,
                limit = ObjectSetConfig.DEFAULT_LIMIT,
                offset = offset.value
            )
        ).process(
            success = { payload ->
                val event = EventAnalytics.Anytype(
                    name = SETS_VIEWER_ACTIVE,
                    duration = EventAnalytics.Duration(
                        start = start,
                        middleware = System.currentTimeMillis()
                    )
                )
                defaultPayloadWithEvent(Pair(payload, event))
            },
            failure = { Timber.e(it, "Error while setting view during pagination") }
        )
    }

    //endregion

    override fun onCleared() {
        super.onCleared()
        titleUpdateChannel.cancel()
        reducer.clear()
    }

    companion object {
        const val TITLE_CHANNEL_DISPATCH_DELAY = 300L
        const val NOT_ALLOWED = "Not allowed for this set"
        const val NOT_ALLOWED_CELL = "Not allowed for this cell"
        const val OBJECT_TYPE_UNKNOWN = "Can't open object, object type unknown"
        const val DATA_VIEW_HAS_NO_VIEW_MSG = "Data view has no view."
    }
}