package com.anytypeio.anytype.presentation.sets

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.DV
import com.anytypeio.anytype.core_models.DVViewerRelation
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.SyncStatus
import com.anytypeio.anytype.core_models.ext.content
import com.anytypeio.anytype.core_models.restrictions.DataViewRestriction
import com.anytypeio.anytype.core_utils.common.EventWrapper
import com.anytypeio.anytype.domain.base.Result
import com.anytypeio.anytype.domain.block.interactor.UpdateText
import com.anytypeio.anytype.domain.cover.SetDocCoverImage
import com.anytypeio.anytype.domain.dataview.interactor.AddNewRelationToDataView
import com.anytypeio.anytype.domain.dataview.interactor.CreateDataViewRecord
import com.anytypeio.anytype.domain.dataview.interactor.SetActiveViewer
import com.anytypeio.anytype.domain.dataview.interactor.UpdateDataViewRecord
import com.anytypeio.anytype.domain.dataview.interactor.UpdateDataViewViewer
import com.anytypeio.anytype.domain.error.Error
import com.anytypeio.anytype.domain.event.interactor.InterceptEvents
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.page.CloseBlock
import com.anytypeio.anytype.domain.sets.OpenObjectSet
import com.anytypeio.anytype.domain.status.InterceptThreadStatus
import com.anytypeio.anytype.domain.templates.GetTemplates
import com.anytypeio.anytype.domain.unsplash.DownloadUnsplashImage
import com.anytypeio.anytype.presentation.common.Action
import com.anytypeio.anytype.presentation.common.Delegator
import com.anytypeio.anytype.presentation.editor.editor.Proxy
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.model.TextUpdate
import com.anytypeio.anytype.presentation.extension.sendAnalyticsObjectCreateEvent
import com.anytypeio.anytype.presentation.extension.sendAnalyticsShowSetEvent
import com.anytypeio.anytype.presentation.mapper.toDomain
import com.anytypeio.anytype.presentation.navigation.AppNavigation
import com.anytypeio.anytype.presentation.navigation.SupportNavigation
import com.anytypeio.anytype.presentation.relations.ObjectSetConfig
import com.anytypeio.anytype.presentation.relations.render
import com.anytypeio.anytype.presentation.relations.tabs
import com.anytypeio.anytype.presentation.relations.title
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
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
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
    private val downloadUnsplashImage: DownloadUnsplashImage,
    private val setDocCoverImage: SetDocCoverImage,
    private val updateText: UpdateText,
    private val interceptEvents: InterceptEvents,
    private val interceptThreadStatus: InterceptThreadStatus,
    private val dispatcher: Dispatcher<Payload>,
    private val delegator: Delegator<Action>,
    private val objectSetRecordCache: ObjectSetRecordCache,
    private val urlBuilder: UrlBuilder,
    private val session: ObjectSetSession,
    private val analytics: Analytics,
    private val getTemplates: GetTemplates
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

    private var analyticsContext: String? = null

    private lateinit var context: Id

    init {
        viewModelScope.launch {
            dispatcher.flow().collect { defaultPayloadConsumer(it) }
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

        viewModelScope.launch {
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
                                Error.NotFoundObject -> toast(TOAST_SET_NOT_EXIST)
                            }
                        }
                        is Result.Success -> {
                            defaultPayloadConsumer(result.data)
                            proceedWithStartupPaging()
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
                    format = format,
                    limitObjectTypes = emptyList()
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
                defaultPayloadConsumer(payload)
            },
            failure = { Timber.e(it, "Error while updating data view's viewer") }
        )
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
            // TODO Use loading state to prevent user from editing title if set of objects is not ready.
            Timber.e("Skipping dispatching title update, because set of objects was not ready.")
        }
    }

    fun onGridCellClicked(cell: CellView) {
        Timber.d("onGridCellClicked, cell:[$cell]")

        if (cell.key == Relations.NAME) return

        val state = reducer.state.value

        if (!state.isInitialized) {
            Timber.e("State was not initialized or cleared when cell is clicked")
            return
        }

        val block = state.dataview
        val dv = block.content as DV
        val viewer =
            dv.viewers.find { it.id == session.currentViewerId }?.id ?: dv.viewers.first().id

        if (dv.isRelationReadOnly(relationKey = cell.key)) {
            val relation = dv.relations.first { it.key == cell.key }
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
                val targetObjectTypes = mutableListOf<String>()
                if (cell is CellView.Object) {
                    val relation = reducer.state.value.dataview.content<DV>().relations.find { relation ->
                        relation.key == cell.key
                    }
                    if (relation != null) {
                        targetObjectTypes.addAll(relation.objectTypes)
                    }
                }
                dispatch(
                    ObjectSetCommand.Modal.EditRelationCell(
                        ctx = context,
                        target = cell.id,
                        dataview = block.id,
                        relation = cell.key,
                        viewer = viewer,
                        targetObjectTypes = targetObjectTypes
                    )
                )
            }
            is CellView.Checkbox -> {
                val records = reducer.state.value.viewerDb[viewer] ?: return
                val record = records.records.find { it[ObjectSetConfig.ID_KEY] == cell.id }
                if (record != null) {
                    val updated = mapOf<String, Any?>(
                        Relations.ID to cell.id,
                        cell.key to !cell.isChecked
                    )
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
            val viewer = session.currentViewerId ?: set.viewers.first().id
            val records = reducer.state.value.viewerDb[viewer] ?: return
            val record = records.records.find { rec -> rec[Relations.ID] == target }
            if (record != null) {
                val obj = ObjectWrapper.Basic(record)
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
        val set = reducer.state.value
        if (set.isInitialized) {
            val viewer = session.currentViewerId ?: set.viewers.first().id
            val records = reducer.state.value.viewerDb[viewer] ?: return
            val record = records.records.find { rec -> rec[Relations.ID] == target }
            if (record != null) {
                val obj = ObjectWrapper.Basic(record)
                viewModelScope.launch {
                    updateDataViewRecord(
                        UpdateDataViewRecord.Params(
                            context = context,
                            target = set.dataview.id,
                            record = target,
                            values = mapOf(
                                Relations.DONE to !(obj.done ?: false)
                            )
                        )
                    ).process(
                        failure = {
                            Timber.e(it, "Error while updating checkbox")
                        },
                        success = {
                            Timber.d("Checkbox successfully updated for record: $target")
                        }
                    )
                }
            } else {
                toast("Record not found. Please, try again later.")
            }
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
        val viewer = reducer.state.value.viewerById(session.currentViewerId)
        val records = reducer.state.value.viewerDb[viewer.id]
        if (records == null) {
            Timber.e("Error onRelationTextValueChanged, records is null")
            return
        }
        val record = records.records.find { it[ObjectSetConfig.ID_KEY] == objectId }
        if (record != null) {
            val updated = mapOf(relationKey to value)
            viewModelScope.launch {
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
            val viewer = dv.viewers.find { it.id == session.currentViewerId } ?: dv.viewers.first()
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

    fun onCreateNewRecord() {
        Timber.d("onCreateNewRecord, ")
        if (reducer.state.value.isInitialized) {
            if (isRestrictionPresent(DataViewRestriction.CREATE_OBJECT)) {
                toast(NOT_ALLOWED)
            } else {
                val startTime = System.currentTimeMillis()
                viewModelScope.launch {
                    val template = resolveTemplateForNewRecord()
                    createDataViewRecord(
                        CreateDataViewRecord.Params(
                            context = context,
                            target = reducer.state.value.dataview.id,
                            template = template
                        )
                    ).process(
                        failure = { Timber.e(it, "Error while creating new record") },
                        success = { record ->
                            val middleTime = System.currentTimeMillis()
                            val wrapper = ObjectWrapper.Basic(record)
                            total.value = total.value.inc()
                            proceedWithRefreshingViewerAfterObjectCreation()
                            sendAnalyticsObjectCreateEvent(
                                analytics = analytics,
                                objType = wrapper.type.firstOrNull(),
                                layout = wrapper.layout?.code?.toDouble(),
                                route = EventsDictionary.Routes.objCreateSet,
                                startTime = startTime,
                                middleTime = middleTime,
                                context = analyticsContext
                            )
                            if (wrapper.layout != ObjectType.Layout.NOTE) {
                                objectSetRecordCache.map[context] = record
                                dispatch(ObjectSetCommand.Modal.SetNameForCreatedRecord(context))
                            }
                        }
                    )
                }
            }
        } else {
            toast("Data view is not initialized yet.")
        }
    }

    private suspend fun resolveTemplateForNewRecord() : Id? {
        val obj = ObjectWrapper.Basic(reducer.state.value.details[context]?.map ?: emptyMap())
        val type = if (obj.setOf.size == 1) obj.setOf.first() else null
        return if (type != null) {
            val templates  = try {
                getTemplates.run(GetTemplates.Params(type))
            } catch (e: Exception) {
                emptyList()
            }
            if (templates.size == 1)
                templates.first().id
            else
                null
        } else {
            null
        }
    }

    private suspend fun proceedWithRefreshingViewerAfterObjectCreation() {
        val set = reducer.state.value
        if (set.isInitialized) {
            val viewer = try { set.viewerById(session.currentViewerId).id } catch (e: Exception) {
                null
            }
            if (viewer != null) {
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
            } else {
                toast("Target view was missing on refresh")
            }
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
                    isArchived = set.details[context]?.isArchived ?: false,
                    isFavorite = set.details[context]?.isFavorite ?: false,
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
            dispatch(
                ObjectSetCommand.Modal.OpenIconActionMenu(target = context)
            )
        }
    }

    fun onCoverClicked() {
        Timber.d("onCoverClicked, ")
        val set = reducer.state.value
        if (set.isInitialized) {
            dispatch(
                ObjectSetCommand.Modal.OpenCoverActionMenu(ctx = context)
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
        val state = reducer.state.value
        val obj = ObjectWrapper.Basic(state.details[context]?.map ?: emptyMap())
        if (obj.setOf.isNotEmpty()) {
            if (state.isInitialized) {
                val set = state.dataview
                val dv = set.content<Block.Content.DataView>()
                if (dv.viewers.isNotEmpty()) {
                    val viewer =
                        dv.viewers.find { it.id == session.currentViewerId } ?: dv.viewers.first()
                    proceedWithViewerPaging(set = set, viewer = viewer.id)
                } else {
                    Timber.e("Stopped initial paging: data view contained no view")
                }
            } else {
                error.value = DATA_VIEW_NOT_FOUND_ERROR
            }
        } else {
            error.value = OBJECT_SET_HAS_EMPTY_SOURCE_ERROR
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
                defaultPayloadConsumer(payload)
            },
            failure = { Timber.e(it, "Error while setting view during pagination") }
        )
    }

    //endregion

    //region NAVIGATION

    private fun proceedWithNavigation(target: Id, layout: ObjectType.Layout?) {
        when (layout) {
            ObjectType.Layout.BASIC,
            ObjectType.Layout.PROFILE,
            ObjectType.Layout.TODO,
            ObjectType.Layout.NOTE,
            ObjectType.Layout.IMAGE,
            ObjectType.Layout.FILE -> {
                viewModelScope.launch {
                    closeBlock(CloseBlock.Params(context)).process(
                        success = {
                            navigate(EventWrapper(AppNavigation.Command.OpenObject(id = target)))
                        },
                        failure = {
                            Timber.e(it, "Error while closing object set: $context")
                            navigate(EventWrapper(AppNavigation.Command.OpenObject(id = target)))
                        }
                    )
                }
            }
            ObjectType.Layout.SET -> {
                viewModelScope.launch {
                    closeBlock(CloseBlock.Params(context)).process(
                        success = {
                            navigate(EventWrapper(AppNavigation.Command.OpenObjectSet(target)))
                        },
                        failure = {
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
        val set = reducer.state.value
        if (set.isInitialized) {
            val viewerIndex = set.viewers.indexOfFirst { it.id == session.currentViewerId }
            val state = set.render(
                index = viewerIndex,
                ctx = context,
                builder = urlBuilder,
                useFallbackView = true
            )
            _viewerGrid.value = state.viewer
            _header.value = state.title
        }
    }

    override fun onCleared() {
        super.onCleared()
        titleUpdateChannel.cancel()
        reducer.clear()
    }

    fun onHomeButtonClicked() {
        viewModelScope.launch {
            closeBlock(CloseBlock.Params(context)).process(
                success = { dispatch(AppNavigation.Command.ExitToDesktop) },
                failure = { Timber.e(it, "Error while closing object set: $context") }
            )
        }
    }

    fun onBackButtonClicked() {
        proceedWithExiting()
    }

    fun onSearchButtonClicked() {
        viewModelScope.launch {
            closeBlock(CloseBlock.Params(context)).process(
                success = { dispatch(AppNavigation.Command.OpenPageSearch) },
                failure = { Timber.e(it, "Error while closing object set: $context") }
            )
        }
    }

    companion object {
        const val TITLE_CHANNEL_DISPATCH_DELAY = 300L
        const val NOT_ALLOWED = "Not allowed for this set"
        const val NOT_ALLOWED_CELL = "Not allowed for this cell"
        const val OBJECT_TYPE_UNKNOWN = "Can't open object, object type unknown"
        const val DATA_VIEW_HAS_NO_VIEW_MSG = "Data view has no view."
        const val DATA_VIEW_NOT_FOUND_ERROR = "Content missing for this set. Please, try again later."
        const val OBJECT_SET_HAS_EMPTY_SOURCE_ERROR = "Object type is not defined for this set. Please, setup object type on Desktop."
        const val TOAST_SET_NOT_EXIST = "This object doesn't exist"
    }
}