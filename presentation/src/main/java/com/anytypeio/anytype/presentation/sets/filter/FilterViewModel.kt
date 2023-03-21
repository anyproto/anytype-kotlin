package com.anytypeio.anytype.presentation.sets.filter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.DVFilterOperator
import com.anytypeio.anytype.core_models.DVFilterQuickOption
import com.anytypeio.anytype.core_models.DVViewer
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_utils.ext.typeOf
import com.anytypeio.anytype.domain.dataview.interactor.UpdateDataViewViewer
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.objects.options.GetOptions
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.domain.workspace.WorkspaceManager
import com.anytypeio.anytype.presentation.extension.ObjectStateAnalyticsEvent
import com.anytypeio.anytype.presentation.extension.checkboxFilterValue
import com.anytypeio.anytype.presentation.extension.hasValue
import com.anytypeio.anytype.presentation.extension.index
import com.anytypeio.anytype.presentation.extension.logEvent
import com.anytypeio.anytype.presentation.extension.toConditionView
import com.anytypeio.anytype.presentation.extension.type
import com.anytypeio.anytype.presentation.mapper.toDomain
import com.anytypeio.anytype.presentation.objects.toCreateFilterObjectView
import com.anytypeio.anytype.presentation.relations.FilterInputValueParser
import com.anytypeio.anytype.presentation.relations.toCreateFilterCheckboxView
import com.anytypeio.anytype.presentation.relations.toCreateFilterDateView
import com.anytypeio.anytype.presentation.relations.toCreateFilterStatusView
import com.anytypeio.anytype.presentation.relations.toCreateFilterTagView
import com.anytypeio.anytype.presentation.relations.toFilterValue
import com.anytypeio.anytype.presentation.relations.toViewRelation
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import com.anytypeio.anytype.presentation.sets.ObjectSetDatabase
import com.anytypeio.anytype.presentation.sets.ObjectSetSession
import com.anytypeio.anytype.presentation.sets.dataViewState
import com.anytypeio.anytype.presentation.sets.model.ColumnView
import com.anytypeio.anytype.presentation.sets.model.FilterValue
import com.anytypeio.anytype.presentation.sets.model.SimpleRelationView
import com.anytypeio.anytype.presentation.sets.model.Viewer
import com.anytypeio.anytype.presentation.sets.state.ObjectState
import com.anytypeio.anytype.presentation.sets.viewerById
import com.anytypeio.anytype.presentation.util.Dispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch
import timber.log.Timber

open class FilterViewModel(
    private val objectState: StateFlow<ObjectState>,
    private val session: ObjectSetSession,
    private val dispatcher: Dispatcher<Payload>,
    private val updateDataViewViewer: UpdateDataViewViewer,
    private val searchObjects: SearchObjects,
    private val urlBuilder: UrlBuilder,
    private val storeOfObjectTypes: StoreOfObjectTypes,
    private val storeOfRelations: StoreOfRelations,
    private val objectSetDatabase: ObjectSetDatabase,
    private val analytics: Analytics,
    private val getOptions: GetOptions,
    private val workspaceManager: WorkspaceManager
) : ViewModel() {

    val commands = MutableSharedFlow<Commands>()
    val isCompleted = MutableSharedFlow<Boolean>(0)

    private var filterIndex: Int? = null
    private var relationKey: Key? = null
    private var relation: ObjectWrapper.Relation? = null
    private val jobs = mutableListOf<Job>()

    val conditionState = MutableStateFlow<FilterConditionView?>(null)
    val relationState = MutableStateFlow<SimpleRelationView?>(null)
    val filterValueState = MutableStateFlow<FilterValue?>(null)
    val filterValueListState = MutableStateFlow<List<CreateFilterView>>(emptyList())
    val optionCountState = MutableStateFlow(0)

    private fun startObservingCondition() {
        jobs += viewModelScope.launch {
            conditionState.collect { condition ->
                setValueStates(
                    condition = condition?.condition,
                    index = filterIndex
                )
            }
        }
    }

    fun onStart(relationKey: Key, filterIndex: Int?) {
        Timber.d("onStart, relationKey:[$relationKey], filterIndex:[$filterIndex]")
        this.filterIndex = filterIndex
        this.relationKey = relationKey
        startObservingCondition()
        initStates()
    }

    fun onStop() {
        jobs.forEach { it.cancel() }
        jobs.clear()
    }

    private fun initStates() {
        jobs += viewModelScope.launch {
            objectState.filterIsInstance<ObjectState.DataView>().collect { state ->
                val viewer = state.viewerById(session.currentViewerId.value) ?: return@collect
                val key = relationKey
                if (key != null) {
                    val relation = storeOfRelations.getByKey(key)
                    if (relation != null) {
                        this@FilterViewModel.relation = relation
                        setRelationState(
                            viewer = viewer,
                            relation = relation
                        )
                        setConditionState(viewer, relation, filterIndex)
                        setValueStates(
                            condition = conditionState.value?.condition,
                            index = filterIndex
                        )
                    } else {
                        Timber.e("Couldn't find relation in StoreOfRelations by relationKey:[$relationKey]")
                    }
                }
            }
        }
    }

    private fun setRelationState(viewer: DVViewer, relation: ObjectWrapper.Relation) {
        relationState.value = viewer.toViewRelation(relation)
    }

    private fun setConditionState(viewer: DVViewer, relation: ObjectWrapper.Relation, index: Int?) {
        getCondition(relation, viewer, index).let { condition ->
            conditionState.value = FilterConditionView(condition)
            updateUi(relation.format, condition)
        }
    }

    private fun updateUi(format: Relation.Format, condition: Viewer.Filter.Condition) {
        when (format) {
            Relation.Format.SHORT_TEXT,
            Relation.Format.LONG_TEXT,
            Relation.Format.URL,
            Relation.Format.EMAIL,
            Relation.Format.NUMBER,
            Relation.Format.PHONE -> {
                if (condition.hasValue()) {
                    viewModelScope.launch { commands.emit(Commands.ShowInput) }
                } else {
                    viewModelScope.launch { commands.emit(Commands.HideInput) }
                }
            }
            Relation.Format.OBJECT -> {
                viewModelScope.launch { commands.emit(Commands.ObjectDivider) }
                if (condition.hasValue()) {
                    viewModelScope.launch { commands.emit(Commands.ShowSearchbar) }
                    viewModelScope.launch { commands.emit(Commands.ShowCount) }
                } else {
                    viewModelScope.launch { commands.emit(Commands.HideSearchbar) }
                    viewModelScope.launch { commands.emit(Commands.HideCount) }
                }
            }
            Relation.Format.TAG,
            Relation.Format.STATUS,
            Relation.Format.FILE -> {
                viewModelScope.launch { commands.emit(Commands.TagDivider) }
                if (condition.hasValue()) {
                    viewModelScope.launch { commands.emit(Commands.ShowSearchbar) }
                    viewModelScope.launch { commands.emit(Commands.ShowCount) }
                } else {
                    viewModelScope.launch { commands.emit(Commands.HideSearchbar) }
                    viewModelScope.launch { commands.emit(Commands.HideCount) }
                }
            }
            Relation.Format.DATE -> {
                viewModelScope.launch { commands.emit(Commands.DateDivider) }
                viewModelScope.launch { commands.emit(Commands.HideSearchbar) }
                viewModelScope.launch { commands.emit(Commands.HideCount) }
            }
            Relation.Format.CHECKBOX -> {
                viewModelScope.launch { commands.emit(Commands.HideCount) }
                viewModelScope.launch { commands.emit(Commands.HideSearchbar) }
            }
            else -> {
            }
        }
    }

    private fun getCondition(
        relation: ObjectWrapper.Relation,
        viewer: DVViewer,
        index: Int?
    ): Viewer.Filter.Condition {
        return if (index == null) {
            relation.toConditionView(condition = null)
        } else {
            val filter = viewer.filters[index]
            check(filter.relation == relation.key) { "Incorrect filter state" }
            relation.toConditionView(condition = filter.condition)
        }
    }

    private suspend fun setValueStates(
        condition: Viewer.Filter.Condition?,
        index: Int?
    ) {
        if (condition == null || !condition.hasValue()) {
            filterValueState.value = null
            filterValueListState.value = listOf()
            return
        }

        val state = objectState.value.dataViewState() ?: return
        val viewer = state.viewerById(session.currentViewerId.value) ?: return
        val key = relationKey
        if (key != null) {
            val relation = storeOfRelations.getByKey(key) ?: return
            if (index == null) {
                filterValueState.value = relation.toFilterValue(
                    value = null,
                    details = state.details,
                    urlBuilder = urlBuilder,
                    store = objectSetDatabase.store
                )
                proceedWithFilterValueList(
                    relation = relation,
                    filter = null,
                    condition = condition
                )
            } else {
                val filter = viewer.filters[index]
                check(filter.relation == relation.key) { "Incorrect filter state" }
                filterValueState.value = relation.toFilterValue(
                    value = filter.value,
                    details = state.details,
                    urlBuilder = urlBuilder,
                    store = objectSetDatabase.store
                )
                proceedWithFilterValueList(
                    relation = relation,
                    filter = filter,
                    condition = condition,
                )
            }
        }
    }

    private suspend fun proceedWithFilterValueList(
        relation: ObjectWrapper.Relation,
        filter: DVFilter?,
        condition: Viewer.Filter.Condition
    ): Unit = when (relation.format) {
        Relation.Format.DATE -> {
            val value = (filter?.value as? Double)?.toLong() ?: 0L
            filterValueListState.value = relation.toCreateFilterDateView(
                quickOption = filter?.quickOption,
                condition = condition.toDomain(),
                value = value
            )
        }
        Relation.Format.TAG -> {
            proceedWithParsingTagValues(filter, relation)
        }
        Relation.Format.STATUS -> {
            proceedWithParsingStatusValues(filter, relation)
        }
        Relation.Format.OBJECT -> {
            val ids = filter?.value as? List<*>
            proceedWithSearchObjects(
                ids = ids,
                objectTypes = storeOfObjectTypes.getAll()
            )
        }
        Relation.Format.CHECKBOX -> {
            filterValueListState.value =
                relation.toCreateFilterCheckboxView(filter?.value as? Boolean)
        }
        else -> {
            filterValueListState.value = emptyList()
            Timber.e("No need values list for format ${relation.format}")
        }
    }

    private fun proceedWithParsingTagValues(
        filter: DVFilter?,
        relation: ObjectWrapper.Relation
    ) {
        val ids: List<Id> = (filter?.value as? List<*>)?.typeOf() ?: emptyList()
        viewModelScope.launch {
            getOptions(
                GetOptions.Params(
                    relation = relation.key
                )
            ).process(
                success = { options ->
                    filterValueListState.value = options.toCreateFilterTagView(
                        selected = ids
                    ).also {
                        optionCountState.value = it.count { view -> view.isSelected }
                    }
                },
                failure = {
                    filterValueListState.value = emptyList<CreateFilterView.Tag>().also {
                        optionCountState.value = it.count { view -> view.isSelected }
                    }
                }
            )
        }
    }

    private fun proceedWithParsingStatusValues(
        filter: DVFilter?,
        relation: ObjectWrapper.Relation
    ) {
        val ids: List<Id> = (filter?.value as? List<*>)?.typeOf() ?: emptyList()
        viewModelScope.launch {
            getOptions(
                GetOptions.Params(
                    relation = relation.key
                )
            ).process(
                success = { options ->
                    filterValueListState.value = options.toCreateFilterStatusView(
                        selected = ids
                    ).also {
                        optionCountState.value = it.count { view -> view.isSelected }
                    }
                },
                failure = {
                    filterValueListState.value = emptyList<CreateFilterView.Status>().also {
                        optionCountState.value = it.count { view -> view.isSelected }
                    }
                }
            )
        }
    }

    private fun proceedWithSearchObjects(
        ids: List<*>? = null,
        objectTypes: List<ObjectWrapper.Type>
    ) {
        viewModelScope.launch {
            searchObjects(
                SearchObjects.Params(
                    sorts = ObjectSearchConstants.sortAddObjectToFilter,
                    filters = ObjectSearchConstants.filterAddObjectToFilter(
                        workspaceId = workspaceManager.getCurrentWorkspace()
                    ),
                    fulltext = SearchObjects.EMPTY_TEXT,
                    offset = SearchObjects.INIT_OFFSET,
                    limit = SearchObjects.LIMIT
                )
            ).process(
                failure = { Timber.e(it, "Error while getting objects") },
                success = { objects ->
                    filterValueListState.value = objects.toCreateFilterObjectView(
                        ids = ids,
                        urlBuilder = urlBuilder,
                        objectTypes = objectTypes
                    ).also {
                        optionCountState.value = it.count { view -> view.isSelected }
                    }
                }
            )
        }
    }

    fun onConditionClicked() {
        val condition = conditionState.value?.condition
        checkNotNull(condition)
        viewModelScope.launch {
            proceedWithConditionPickerScreen(
                type = condition.type(),
                index = condition.index()
            )
        }
    }

    fun onConditionUpdate(condition: Viewer.Filter.Condition) {
        conditionState.value = FilterConditionView(condition)
        val format = relation?.format
        if (format != null) {
            updateUi(condition = condition, format = format)
        }
    }

    fun onFilterItemClicked(item: CreateFilterView) {
        when (item) {
            is CreateFilterView.Tag -> {
                filterValueListState.value = filterValueListState.value.map { view ->
                    if (view is CreateFilterView.Tag && view.id == item.id) {
                        view.copy(isSelected = !view.isSelected)
                    } else {
                        view
                    }
                }.also {
                    optionCountState.value = it.count { view -> view.isSelected }
                }
            }
            is CreateFilterView.Status -> {
                filterValueListState.value = filterValueListState.value.map { view ->
                    if (view is CreateFilterView.Status && view.id == item.id) {
                        view.copy(isSelected = !view.isSelected)
                    } else {
                        view
                    }
                }.also {
                    optionCountState.value = it.count { view -> view.isSelected }
                }
            }
            is CreateFilterView.Date -> {
                onFilterDateItemClicked(item)
            }
            is CreateFilterView.Object -> {
                filterValueListState.value = filterValueListState.value.map { view ->
                    if (view is CreateFilterView.Object && view.id == item.id) {
                        view.copy(isSelected = !view.isSelected)
                    } else {
                        view
                    }
                }.also {
                    optionCountState.value = it.count { view -> view.isSelected }
                }
            }
            is CreateFilterView.Checkbox -> {
                filterValueListState.value = filterValueListState.value.map { view ->
                    if (view is CreateFilterView.Checkbox) {
                        if (view.isChecked == item.isChecked) {
                            view.copy(isSelected = true)
                        } else {
                            view.copy(isSelected = false)
                        }
                    } else {
                        view
                    }
                }
            }
        }
    }

    fun onCreateInputValueFilterClicked(ctx: Id, relation: Id, input: String) {
        val condition = conditionState.value?.condition
        checkNotNull(condition)
        val format = relationState.value?.format
        checkNotNull(format)
        val value = FilterInputValueParser.parse(
            value = input,
            condition = condition,
            format = format
        )

        viewModelScope.launch {
            proceedWithCreatingFilter(
                ctx = ctx,
                relationKey = relation,
                value = value,
                condition = condition.toDomain(),
                relationFormat = if (format == ColumnView.Format.DATE)
                    RelationFormat.DATE
                else
                    null
            )
        }
    }

    fun onExactDayPicked(timeInSeconds: Long) {
        setFilterState(DVFilterQuickOption.EXACT_DATE, timeInSeconds)
    }

    private fun setFilterState(quickOption: DVFilterQuickOption, value: Long) {
        filterValueListState.value = filterValueListState.value.map { view ->
            if (view is CreateFilterView.Date) {
                if (view.type == quickOption) {
                    view.copy(
                        value = value,
                        isSelected = true
                    )
                } else {
                    view.copy(isSelected = false)
                }
            } else {
                view
            }
        }
    }

    fun onExactNumberOfDays(quickOption: DVFilterQuickOption, numberOfDays: Long) {
        setFilterState(quickOption, numberOfDays)
    }

    fun onCreateFilterFromSelectedValueClicked(ctx: Id, relation: Id) {
        val condition = conditionState.value?.condition
        checkNotNull(condition)
        viewModelScope.launch {
            val format = relationState.value?.format
            if (format != null) {
                when (format) {
                    ColumnView.Format.TAG -> {
                        val tags =
                            filterValueListState.value.filterIsInstance<CreateFilterView.Tag>()
                        val selected = tags.filter { it.isSelected }.map { tag -> tag.id }
                        proceedWithCreatingFilter(
                            ctx = ctx,
                            relationKey = relation,
                            value = selected,
                            condition = condition.toDomain(),
                        )
                    }
                    ColumnView.Format.STATUS -> {
                        val statuses =
                            filterValueListState.value.filterIsInstance<CreateFilterView.Status>()
                        val selected = statuses.filter { it.isSelected }.map { status -> status.id }
                        proceedWithCreatingFilter(
                            ctx = ctx,
                            relationKey = relation,
                            value = selected,
                            condition = condition.toDomain()
                        )
                    }
                    ColumnView.Format.DATE -> {
                        val dates =
                            filterValueListState.value.filterIsInstance<CreateFilterView.Date>()
                        val selected = dates.firstOrNull { it.isSelected }
                        proceedWithCreatingFilter(
                            ctx = ctx,
                            relationKey = relation,
                            value = selected?.value?.toDouble(),
                            quickOption = selected?.type ?: DVFilterQuickOption.EXACT_DATE,
                            condition = condition.toDomain()
                        )
                    }
                    ColumnView.Format.OBJECT -> {
                        val objects =
                            filterValueListState.value.filterIsInstance<CreateFilterView.Object>()
                        val selected = objects
                            .filter { it.isSelected }
                            .map { obj -> obj.id }
                        proceedWithCreatingFilter(
                            ctx = ctx,
                            relationKey = relation,
                            value = selected,
                            condition = condition.toDomain(),
                        )
                    }
                    ColumnView.Format.CHECKBOX -> {
                        val value = filterValueListState.value.checkboxFilterValue()
                        proceedWithCreatingFilter(
                            ctx = ctx,
                            relationKey = relation,
                            value = value,
                            condition = condition.toDomain()
                        )
                    }
                    else -> {
                        Timber.e("Wrong filter format $format")
                    }
                }
            }
        }
    }

    fun onModifyApplyClicked(ctx: Id, input: String) {
        val condition = conditionState.value?.condition
        checkNotNull(condition)
        val relation = this.relationKey
        checkNotNull(relation)
        val idx = filterIndex
        checkNotNull(idx)
        val format = relationState.value?.format
        checkNotNull(format)
        val value = FilterInputValueParser.parse(
            value = input,
            condition = condition,
            format = format
        )
        val state = objectState.value.dataViewState() ?: return
        val viewer = state.viewerById(session.currentViewerId.value) ?: return
        viewModelScope.launch {
            val filterId = viewer.filters.getOrNull(idx)?.id
            if (filterId == null) {
                commands.emit(Commands.Toast("Filter with index $idx not found"))
                Timber.e("Filter with index $idx not found")
                return@launch
            }
            proceedWithUpdatingFilter(
                ctx = ctx,
                target = state.dataViewBlock.id,
                viewer = viewer,
                updatedFilter = DVFilter(
                    id = filterId,
                    relation = relation,
                    condition = condition.toDomain(),
                    value = value
                )
            )
        }
    }

    fun onModifyApplyClicked(ctx: Id) {
        val condition = conditionState.value?.condition
        checkNotNull(condition)
        val relation = this.relationKey
        checkNotNull(relation)
        val idx = filterIndex
        checkNotNull(idx)
        val state = objectState.value.dataViewState() ?: return
        val viewer = state.viewerById(session.currentViewerId.value) ?: return
        viewModelScope.launch {
            val filterId = viewer.filters.getOrNull(idx)?.id
            if (filterId == null) {
                commands.emit(Commands.Toast("Filter with index $idx not found"))
                Timber.e("Filter with index $idx not found")
                return@launch
            }
            val format = relationState.value?.format
            if (format != null) {
                when (format) {
                    ColumnView.Format.TAG -> {
                        val value = filterValueListState.value.mapNotNull { view ->
                            if (view is CreateFilterView.Tag && view.isSelected)
                                view.id
                            else
                                null
                        }
                        proceedWithUpdatingFilter(
                            ctx = ctx,
                            target = state.dataViewBlock.id,
                            viewer = viewer,
                            updatedFilter = DVFilter(
                                id = filterId,
                                relation = relation,
                                condition = condition.toDomain(),
                                value = value
                            )
                        )
                    }
                    ColumnView.Format.STATUS -> {
                        val value = filterValueListState.value.mapNotNull { view ->
                            if (view is CreateFilterView.Status && view.isSelected)
                                view.id
                            else
                                null
                        }
                        proceedWithUpdatingFilter(
                            ctx = ctx,
                            target = state.dataViewBlock.id,
                            viewer = viewer,
                            updatedFilter = DVFilter(
                                id = filterId,
                                relation = relation,
                                condition = condition.toDomain(),
                                value = value
                            )
                        )
                    }
                    ColumnView.Format.DATE -> {
                        val date = filterValueListState.value
                            .filterIsInstance<CreateFilterView.Date>()
                            .firstOrNull { it.isSelected }
                        proceedWithUpdatingFilter(
                            ctx = ctx,
                            target = state.dataViewBlock.id,
                            viewer = viewer,
                            updatedFilter = DVFilter(
                                id = filterId,
                                relation = relation,
                                condition = condition.toDomain(),
                                quickOption = date?.type ?: DVFilterQuickOption.EXACT_DATE,
                                value = date?.value?.toDouble()
                            )
                        )
                    }
                    ColumnView.Format.OBJECT -> {
                        val value = filterValueListState.value.mapNotNull { view ->
                            if (view is CreateFilterView.Object && view.isSelected)
                                view.id
                            else
                                null
                        }
                        proceedWithUpdatingFilter(
                            ctx = ctx,
                            target = state.dataViewBlock.id,
                            viewer = viewer,
                            updatedFilter = DVFilter(
                                id = filterId,
                                relation = relation,
                                condition = condition.toDomain(),
                                value = value
                            )
                        )
                    }
                    ColumnView.Format.CHECKBOX -> {
                        val value = filterValueListState.value.checkboxFilterValue()
                        proceedWithUpdatingFilter(
                            ctx = ctx,
                            target = state.dataViewBlock.id,
                            viewer = viewer,
                            updatedFilter = DVFilter(
                                id = filterId,
                                relation = relation,
                                condition = condition.toDomain(),
                                value = value
                            )
                        )
                    }
                    else -> {
                        Timber.e("Wrong selected relation format : $format")
                    }
                }
            }
        }
    }

    private suspend fun proceedWithUpdatingFilter(
        ctx: Id,
        target: Id,
        viewer: DVViewer,
        updatedFilter: DVFilter
    ) {
        val startTime = System.currentTimeMillis()
        val params = UpdateDataViewViewer.Params.Filter.Replace(
            ctx = ctx,
            dv = target,
            view = viewer.id,
            filter = updatedFilter
        )
        updateDataViewViewer(params).process(
            failure = { Timber.e(it, "Error while creating filter") },
            success = {
                dispatcher.send(it).also {
                    viewModelScope.logEvent(
                        state = objectState.value,
                        analytics = analytics,
                        event = ObjectStateAnalyticsEvent.CHANGE_FILTER_VALUE,
                        startTime = startTime,
                        condition = updatedFilter.condition
                    )
                    isCompleted.emit(true)
                }
            }
        )
    }

    private suspend fun proceedWithCreatingFilter(
        ctx: Id,
        relationKey: String,
        relationFormat: RelationFormat? = null,
        operator: DVFilterOperator = DVFilterOperator.AND,
        condition: DVFilterCondition,
        quickOption: DVFilterQuickOption = DVFilterQuickOption.EXACT_DATE,
        value: Any? = null
    ) {
        val startTime = System.currentTimeMillis()
        val state = objectState.value.dataViewState() ?: return
        val viewer = state.viewerById(session.currentViewerId.value) ?: return
        val params = UpdateDataViewViewer.Params.Filter.Add(
            ctx = ctx,
            dv = state.dataViewBlock.id,
            view = viewer.id,
            relationKey = relationKey,
            relationFormat = relationFormat,
            operator = operator,
            condition = condition,
            quickOption = quickOption,
            value = value
        )
        updateDataViewViewer(params).process(
            failure = { Timber.e(it, "Error while creating filter") },
            success = {
                dispatcher.send(it).also {
                    viewModelScope.logEvent(
                        state = objectState.value,
                        analytics = analytics,
                        event = ObjectStateAnalyticsEvent.ADD_FILTER,
                        startTime = startTime,
                        condition = condition
                    )
                    isCompleted.emit(true)
                }
            }
        )
    }

    private fun onFilterDateItemClicked(dateItem: CreateFilterView.Date) {
        when (dateItem.type) {
            DVFilterQuickOption.EXACT_DATE -> {
                viewModelScope.launch { proceedWithDatePickerScreen(dateItem.value) }
            }
            DVFilterQuickOption.DAYS_AGO, DVFilterQuickOption.DAYS_AHEAD -> {
                viewModelScope.launch {
                    proceedWithNumberPickerScreen(
                        dateItem.type,
                        if (dateItem.isSelected) dateItem.value else null
                    )
                }
            }
            else -> {
                filterValueListState.value = updateSelectedState(dateItem)
            }
        }
    }

    private suspend fun proceedWithDatePickerScreen(timeInSeconds: Long) {
        commands.emit(Commands.OpenDatePicker(timeInSeconds))
    }

    private suspend fun proceedWithNumberPickerScreen(option: DVFilterQuickOption, value: Long?) {
        commands.emit(Commands.OpenNumberPicker(option, value))
    }

    private suspend fun proceedWithConditionPickerScreen(type: Viewer.Filter.Type, index: Int) {
        commands.emit(Commands.OpenConditionPicker(type, index))
    }

    private fun updateSelectedState(
        selectedItem: CreateFilterView.Date
    ): List<CreateFilterView> = filterValueListState.value.map { view ->
        if (view is CreateFilterView.Date) {
            if (view.type == DVFilterQuickOption.EXACT_DATE) {
                view.copy(isSelected = false, value = CreateFilterView.Date.NO_VALUE)
            } else {
                if (view.type == selectedItem.type) {
                    view.copy(isSelected = true)
                } else {
                    view.copy(isSelected = false, value = CreateFilterView.Date.NO_VALUE)
                }
            }
        } else {
            view
        }
    }

    class Factory(
        private val objectState: StateFlow<ObjectState>,
        private val session: ObjectSetSession,
        private val dispatcher: Dispatcher<Payload>,
        private val updateDataViewViewer: UpdateDataViewViewer,
        private val searchObjects: SearchObjects,
        private val urlBuilder: UrlBuilder,
        private val storeOfObjectTypes: StoreOfObjectTypes,
        private val storeOfRelations: StoreOfRelations,
        private val objectSetDatabase: ObjectSetDatabase,
        private val getOptions: GetOptions,
        private val analytics: Analytics,
        private val workspaceManager: WorkspaceManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return FilterViewModel(
                objectState = objectState,
                session = session,
                dispatcher = dispatcher,
                updateDataViewViewer = updateDataViewViewer,
                searchObjects = searchObjects,
                urlBuilder = urlBuilder,
                storeOfObjectTypes = storeOfObjectTypes,
                storeOfRelations = storeOfRelations,
                objectSetDatabase = objectSetDatabase,
                getOptions = getOptions,
                analytics = analytics,
                workspaceManager = workspaceManager
            ) as T
        }
    }

    sealed class Commands {
        object ShowInput : Commands()
        object HideInput : Commands()
        object ShowSearchbar : Commands()
        object HideSearchbar : Commands()
        object ShowCount : Commands()
        object HideCount : Commands()
        data class OpenDatePicker(val timeInSeconds: Long) : Commands()
        data class OpenNumberPicker(val option: DVFilterQuickOption, val value: Long?) : Commands()
        data class OpenConditionPicker(val type: Viewer.Filter.Type, val index: Int) : Commands()
        object TagDivider : Commands()
        object ObjectDivider : Commands()
        object DateDivider : Commands()
        data class Toast(val message: String) : Commands()
    }
}