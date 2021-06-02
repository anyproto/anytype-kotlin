package com.anytypeio.anytype.presentation.sets

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.*
import com.anytypeio.anytype.core_utils.common.EventWrapper
import com.anytypeio.anytype.domain.block.interactor.UpdateText
import com.anytypeio.anytype.domain.dataview.interactor.*
import com.anytypeio.anytype.domain.event.interactor.InterceptEvents
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.page.CloseBlock
import com.anytypeio.anytype.domain.sets.OpenObjectSet
import com.anytypeio.anytype.presentation.mapper.toDomain
import com.anytypeio.anytype.presentation.navigation.AppNavigation
import com.anytypeio.anytype.presentation.navigation.SupportNavigation
import com.anytypeio.anytype.presentation.page.editor.Proxy
import com.anytypeio.anytype.presentation.page.editor.model.BlockView
import com.anytypeio.anytype.presentation.page.model.TextUpdate
import com.anytypeio.anytype.presentation.relations.ObjectSetConfig
import com.anytypeio.anytype.presentation.relations.render
import com.anytypeio.anytype.presentation.relations.tabs
import com.anytypeio.anytype.presentation.sets.model.*
import com.anytypeio.anytype.presentation.util.Dispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber

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
    private val dispatcher: Dispatcher<Payload>,
    private val objectSetRecordCache: ObjectSetRecordCache,
    private val urlBuilder: UrlBuilder,
    private val session: ObjectSetSession
) : ViewModel(), SupportNavigation<EventWrapper<AppNavigation.Command>> {

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

    private lateinit var context: Id

    init {
        viewModelScope.launch {
            dispatcher.flow().collect(defaultPayloadConsumer)
        }

        viewModelScope.launch {
            reducer.state.filter { it.isInitialized }.collect { set ->
                Timber.d("Set updated!")
                _viewerTabs.value = set.tabs(session.currentViewerId)
                val viewerIndex = reducer.state.value.viewers.indexOfFirst { it.id == session.currentViewerId }
                set.render(viewerIndex, context, urlBuilder).let { vs ->
                    _viewerGrid.value = vs.viewer
                    _header.value = vs.title
                }
            }
        }

        viewModelScope.launch {
            reducer.effects.collect { effect ->
                when (effect) {
                    is ObjectSetReducer.SideEffect.ViewerUpdate -> {
                        updateDataViewViewer(
                            UpdateDataViewViewer.Params(
                                context = context,
                                target = effect.target,
                                viewer = effect.viewer
                            )
                        ).process(
                            success = defaultPayloadConsumer,
                            failure = { Timber.e(it, "Error while updating data view's viewer") }
                        )
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
        context = ctx
        jobs += viewModelScope.launch {
            interceptEvents
                .build(InterceptEvents.Params(context))
                .collect { events -> reducer.dispatch(events) }
        }
        viewModelScope.launch {
            openObjectSet(ctx).process(
                success = defaultPayloadConsumer,
                failure = { Timber.e(it, "Error while opening object set: $ctx") }
            )
        }
    }

    fun onStop() {
        jobs.forEach { it.cancel() }
    }

    fun onSystemBackPressed() {
        proceedWithExiting()
    }

    fun onBackButtonPressed() {
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

    fun onAddNewDataViewRelation() {
        dispatch(
            ObjectSetCommand.Modal.CreateDataViewRelation(
                ctx = context,
                target = reducer.state.value.dataview.id
            )
        )
    }

    fun onCreateNewViewerClicked() {
        dispatch(
            ObjectSetCommand.Modal.CreateViewer(
                ctx = context,
                target = reducer.state.value.dataview.id
            )
        )
    }

    fun onViewerTabClicked(viewer: Id) {
        viewModelScope.launch {
            session.currentViewerId = viewer
            setActiveViewer(
                SetActiveViewer.Params(
                    context = context,
                    block = reducer.state.value.dataview.id,
                    view = viewer,
                    limit = 0,
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
        viewModelScope.launch {
            addDataViewRelation(
                AddNewRelationToDataView.Params(
                    context = context,
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
            success = defaultPayloadConsumer,
            failure = { Timber.e(it, "Error while updating data view's viewer") }
        )
    }

    fun onTitleChanged(txt: String) {
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
        val state = reducer.state.value
        val block = state.dataview
        val dv = block.content as DV
        val viewer = dv.viewers.find { it.id == session.currentViewerId }?.id
            ?: dv.viewers.first().id

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

    fun onObjectHeaderClicked(id: Id, type: String) {
        val set = reducer.state.value
        val objectType = set.objectTypes.find { it.url == type }
        if (objectType == null) {
            toast("Object type not found: $type")
            return
        }
        when (objectType.layout) {
            ObjectType.Layout.PAGE -> {
                navigate(
                    EventWrapper(
                        AppNavigation.Command.OpenPage(
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
        val block = reducer.state.value.dataview
        val dv = block.content as DV
        val records = reducer.state.value.viewerDb[dv.viewers.first().id] ?: return
        val record = records.records.find { it[ObjectSetConfig.ID_KEY] == objectId }
        if (record != null) {
            val updated = record.toMutableMap()
            updated[relationKey] = value
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
                    success = { Timber.d("Data view record updated successfully") }
                )
            }
        } else {
            Timber.e("Couldn't found record for the edited soft-input value cell")
        }
    }

    fun onUpdateViewerSorting(sorts: List<SortingExpression>) {
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
                success = defaultPayloadConsumer,
                failure = { Timber.e(it, "Error while updating data view's viewer") }
            )
        }
    }

    fun onUpdateViewerFilters(filters: List<FilterExpression>) {
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
                success = defaultPayloadConsumer,
                failure = { Timber.e(it, "Error while updating data view's viewer") }
            )
        }
    }

    fun onCreateNewRecord() {
        viewModelScope.launch {
            createDataViewRecord(
                CreateDataViewRecord.Params(
                    context = context,
                    target = reducer.state.value.dataview.id
                )
            ).process(
                failure = { Timber.e(it, "Error while creating new record") },
                success = { record ->
                    objectSetRecordCache.map[context] = record
                    dispatch(ObjectSetCommand.Modal.SetNameForCreatedRecord(context))
                }
            )
        }
    }

    fun onViewerCustomizeButtonClicked() {
        isCustomizeViewPanelVisible.value = !isCustomizeViewPanelVisible.value
    }

    fun onHideViewerCustomizeSwiped() {
        isCustomizeViewPanelVisible.value = false
    }

    fun onViewerCustomizeClicked() {
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
        dispatch(
            ObjectSetCommand.Modal.ManageViewer(
                ctx = context,
                dataview = reducer.state.value.dataview.id
            )
        )
    }

    fun onViewerEditClicked() {
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

    fun onIconClicked() {
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
        val set = reducer.state.value
        if (set.isInitialized) {
            val block = set.dataview
            val dv = block.content as DV
            val viewer = dv.viewers.find { it.id == session.currentViewerId } ?: dv.viewers.first()
            dispatch(
                ObjectSetCommand.Modal.ModifyViewerRelationOrder(
                    ctx = context,
                    dv = block.id,
                    viewer = viewer.id
                )
            )
        }
    }

    fun onViewerFiltersClicked() {
        dispatch(
            ObjectSetCommand.Modal.ModifyViewerFilters(ctx = context)
        )
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

    override fun onCleared() {
        super.onCleared()
        titleUpdateChannel.cancel()
        reducer.clear()
    }

    companion object {
        const val TITLE_CHANNEL_DISPATCH_DELAY = 300L
    }
}