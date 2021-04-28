package com.anytypeio.anytype.presentation.sets.filter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.*
import com.anytypeio.anytype.core_utils.ext.EMPTY_TIMESTAMP
import com.anytypeio.anytype.core_utils.ext.toTimeSeconds
import com.anytypeio.anytype.domain.dataview.interactor.SearchObjects
import com.anytypeio.anytype.domain.dataview.interactor.UpdateDataViewViewer
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.presentation.extension.hasValue
import com.anytypeio.anytype.presentation.extension.index
import com.anytypeio.anytype.presentation.extension.toConditionView
import com.anytypeio.anytype.presentation.extension.type
import com.anytypeio.anytype.presentation.mapper.toDomain
import com.anytypeio.anytype.presentation.relations.*
import com.anytypeio.anytype.presentation.sets.ObjectSet
import com.anytypeio.anytype.presentation.sets.ObjectSetSession
import com.anytypeio.anytype.presentation.sets.model.ColumnView
import com.anytypeio.anytype.presentation.sets.model.FilterValue
import com.anytypeio.anytype.presentation.sets.model.SimpleRelationView
import com.anytypeio.anytype.presentation.sets.model.Viewer
import com.anytypeio.anytype.presentation.util.Dispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber

open class FilterViewModel(
    private val objectSetState: StateFlow<ObjectSet>,
    private val session: ObjectSetSession,
    private val dispatcher: Dispatcher<Payload>,
    private val updateDataViewViewer: UpdateDataViewViewer,
    private val searchObjects: SearchObjects,
    private val urlBuilder: UrlBuilder
) : ViewModel() {

    val commands = MutableSharedFlow<Commands>()
    val isCompleted = MutableSharedFlow<Boolean>(0)

    private var filterIndex: Int? = null
    private var relationId: Id? = null
    private var relation: Relation? = null

    val conditionState = MutableStateFlow<FilterConditionView?>(null)
    val relationState = MutableStateFlow<SimpleRelationView?>(null)
    val filterValueState = MutableStateFlow<FilterValue?>(null)
    val filterValueListState = MutableStateFlow<List<CreateFilterView>>(emptyList())
    val optionCountState = MutableStateFlow(0)

    init {
        startObservingCondition()
    }

    private fun startObservingCondition() {
        viewModelScope.launch {
            conditionState.collect { condition ->
                setValueStates(
                    objectSet = objectSetState.value,
                    condition = condition?.condition,
                    index = filterIndex
                )
            }
        }
    }

    fun onStart(relationId: Id, filterIndex: Int?) {
        this.filterIndex = filterIndex
        this.relationId = relationId
        initStates()
    }

    private fun initStates() {
        val block = objectSetState.value.dataview
        val dv = block.content as DV
        val viewer = dv.viewers.find { it.id == session.currentViewerId } ?: dv.viewers.first()
        val relation = dv.relations.first { it.key == relationId }
        this.relation = relation

        setRelationState(viewer, relation)
        setConditionState(viewer, relation, filterIndex)
        setValueStates(
            objectSet = objectSetState.value,
            condition = conditionState.value?.condition,
            index = filterIndex
        )
    }

    private fun setRelationState(viewer: DVViewer, relation: Relation) {
        relationState.value = viewer.toViewRelation(relation)
    }

    private fun setConditionState(viewer: DVViewer, relation: Relation, index: Int?) {
        getCondition(relation, viewer, index).let { condition ->
            conditionState.value = FilterConditionView(condition)
            updateUi(relation.format, condition)
        }
    }

    private fun updateUi(format: Relation.Format, condition: Viewer.Filter.Condition) {
        when(format) {
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
            Relation.Format.STATUS -> {
                if (condition.hasValue()) {
                    viewModelScope.launch { commands.emit(Commands.ShowSearchbar) }
                    viewModelScope.launch { commands.emit(Commands.HideCount) }
                } else {
                    viewModelScope.launch { commands.emit(Commands.HideSearchbar) }
                    viewModelScope.launch { commands.emit(Commands.HideCount) }
                }
            }
            Relation.Format.TAG,
            Relation.Format.OBJECT,
            Relation.Format.FILE -> {
                if (condition.hasValue()) {
                    viewModelScope.launch { commands.emit(Commands.ShowSearchbar) }
                    viewModelScope.launch { commands.emit(Commands.ShowCount) }
                } else {
                    viewModelScope.launch { commands.emit(Commands.HideSearchbar) }
                    viewModelScope.launch { commands.emit(Commands.HideCount) }
                }
            }
            Relation.Format.DATE -> {
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
        relation: Relation,
        viewer: DVViewer,
        index: Int?
    ): Viewer.Filter.Condition {
        return if (index == null) {
            relation.toConditionView(condition = null)
        } else {
            val filter = viewer.filters[index]
            check(filter.relationKey == relation.key) { "Incorrect filter state" }
            relation.toConditionView(condition = filter.condition)
        }
    }

    private fun setValueStates(
        objectSet: ObjectSet,
        condition: Viewer.Filter.Condition?,
        index: Int?
    ) {
        if (condition == null || !condition.hasValue()) {
            filterValueState.value = null
            filterValueListState.value = listOf()
            return
        }

        val block = objectSet.dataview
        val dv = block.content as DV
        val viewer = dv.viewers.find { it.id == session.currentViewerId } ?: dv.viewers.first()
        val relation = dv.relations.first { it.key == relationId }

        if (index == null) {
            filterValueState.value =
                relation.toFilterValue(null, objectSet.details, urlBuilder)
            proceedWithFilterValueList(
                relation = relation,
                filter = null
            )
        } else {
            val filter = viewer.filters[index]
            check(filter.relationKey == relation.key) { "Incorrect filter state" }
            filterValueState.value =
                relation.toFilterValue(filter.value, objectSet.details, urlBuilder)
            proceedWithFilterValueList(
                relation = relation,
                filter = filter
            )
        }
    }

    private fun proceedWithFilterValueList(
        relation: Relation,
        filter: DVFilter?
    ) = when (relation.format) {
        Relation.Format.DATE -> {
            val timestamp = (filter?.value as? Double)?.toLong() ?: EMPTY_TIMESTAMP
            filterValueListState.value = relation.toCreateFilterDateView(timestamp)
        }
        Relation.Format.TAG -> {
            val ids = filter?.value as? List<*>
            filterValueListState.value = relation.toCreateFilterTagView(ids)
                .also {
                    optionCountState.value = it.count { view -> view.isSelected }
                }
        }
        Relation.Format.STATUS -> {
            val ids = filter?.value as? List<*>
            filterValueListState.value = relation.toCreateFilterStatusView(ids)
                .also {
                    optionCountState.value = it.count { view -> view.isSelected }
                }
        }
        Relation.Format.OBJECT -> {
            val ids = filter?.value as? List<*>
            proceedWithSearchObjects(ids, relation)
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

    private fun proceedWithSearchObjects(ids: List<*>? = null, relation: Relation) {
        val filters = relation.searchObjectsFilter()
        val sorts = arrayListOf(
            DVSort(
                relationKey = ObjectSetConfig.NAME_KEY,
                type = Block.Content.DataView.Sort.Type.ASC
            )
        )
        viewModelScope.launch {
            searchObjects(
                SearchObjects.Params(
                    sorts = sorts,
                    filters = filters,
                    fulltext = SearchObjects.EMPTY_TEXT,
                    offset = SearchObjects.INIT_OFFSET,
                    limit = SearchObjects.LIMIT
                )
            ).process(
                failure = { Timber.e(it, "Error while getting objects") },
                success = {
                    filterValueListState.value = it.toCreateFilterObjectView(ids, urlBuilder).also {
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
                    if (view is CreateFilterView.Status) {
                        if (view.id == item.id)
                            view.copy(isSelected = true)
                        else
                            view.copy(isSelected = false)
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
        viewModelScope.launch {
            proceedWithCreatingFilter(
                ctx = ctx,
                filter = DVFilter(
                    relationKey = relation,
                    value = if (condition.hasValue()) input else "",
                    condition = condition.toDomain()
                )
            )
        }
    }

    fun onExactDayPicked(timeInSeconds: Long) {
        filterValueListState.value = filterValueListState.value.map { view ->
            if (view is CreateFilterView.Date) {
                if (view.type == DateDescription.EXACT_DAY) {
                    view.copy(
                        timeInMillis = timeInSeconds * 1000,
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
                            filter = DVFilter(
                                relationKey = relation,
                                value = selected,
                                condition = condition.toDomain()
                            )
                        )
                    }
                    ColumnView.Format.STATUS -> {
                        val statuses =
                            filterValueListState.value.filterIsInstance<CreateFilterView.Status>()
                        val selected = statuses.filter { it.isSelected }.map { status -> status.id }
                        proceedWithCreatingFilter(
                            ctx = ctx,
                            filter = DVFilter(
                                relationKey = relation,
                                value = selected,
                                condition = condition.toDomain()
                            )
                        )
                    }
                    ColumnView.Format.DATE -> {
                        val dates =
                            filterValueListState.value.filterIsInstance<CreateFilterView.Date>()
                        val selected = dates.first { it.isSelected }
                        proceedWithCreatingFilter(
                            ctx = ctx,
                            filter = DVFilter(
                                relationKey = relation,
                                value = selected.timeInMillis.toTimeSeconds(),
                                condition = condition.toDomain()
                            )
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
                            filter = DVFilter(
                                relationKey = relation,
                                value = selected,
                                condition = condition.toDomain()
                            )
                        )
                    }
                    ColumnView.Format.CHECKBOX -> {
                        val filter = filterValueListState.value.checkboxFilter(
                            relationKey = relation,
                            condition = condition
                        )
                        proceedWithCreatingFilter(
                            ctx = ctx,
                            filter = filter
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
        val relation = this.relationId
        checkNotNull(relation)
        val idx = filterIndex
        checkNotNull(idx)
        viewModelScope.launch {
            val block = objectSetState.value.dataview
            val dv = block.content as DV
            val viewer = dv.viewers.find { it.id == session.currentViewerId } ?: dv.viewers.first()
            proceedWithUpdatingFilter(
                ctx = ctx,
                target = block.id,
                idx = idx,
                viewer = viewer,
                updatedFilter = DVFilter(
                    relationKey = relation,
                    condition = condition.toDomain(),
                    value = if (condition.hasValue()) input else ""
                )
            )
        }
    }

    fun onModifyApplyClicked(ctx: Id) {
        val condition = conditionState.value?.condition
        checkNotNull(condition)
        val relation = this.relationId
        checkNotNull(relation)
        val idx = filterIndex
        checkNotNull(idx)
        viewModelScope.launch {
            val block = objectSetState.value.dataview
            val dv = block.content as DV
            val viewer = dv.viewers.find { it.id == session.currentViewerId } ?: dv.viewers.first()
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
                            target = block.id,
                            idx = idx,
                            viewer = viewer,
                            updatedFilter = DVFilter(
                                relationKey = relation,
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
                            target = block.id,
                            idx = idx,
                            viewer = viewer,
                            updatedFilter = DVFilter(
                                relationKey = relation,
                                condition = condition.toDomain(),
                                value = value
                            )
                        )
                    }
                    ColumnView.Format.DATE -> {
                        val value = filterValueListState.value
                            .filterIsInstance<CreateFilterView.Date>()
                            .filter { it.isSelected }
                            .map { date -> date.timeInMillis.toTimeSeconds() }
                            .firstOrNull()
                        proceedWithUpdatingFilter(
                            ctx = ctx,
                            target = block.id,
                            idx = idx,
                            viewer = viewer,
                            updatedFilter = DVFilter(
                                relationKey = relation,
                                condition = condition.toDomain(),
                                value = value
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
                            target = block.id,
                            idx = idx,
                            viewer = viewer,
                            updatedFilter = DVFilter(
                                relationKey = relation,
                                condition = condition.toDomain(),
                                value = value
                            )
                        )
                    }
                    ColumnView.Format.CHECKBOX -> {
                        val filter = filterValueListState.value.checkboxFilter(
                            relationKey = relation,
                            condition = condition
                        )
                        proceedWithUpdatingFilter(
                            ctx = ctx,
                            target = block.id,
                            idx = idx,
                            viewer = viewer,
                            updatedFilter = filter
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
        idx: Int,
        viewer: DVViewer,
        updatedFilter: DVFilter
    ) {
        updateDataViewViewer(
            UpdateDataViewViewer.Params(
                context = ctx,
                target = target,
                viewer = viewer.copy(
                    filters = viewer.filters.mapIndexed { index, filter ->
                        if (index == idx)
                            updatedFilter
                        else
                            filter
                    }
                )
            )
        ).process(
            failure = { Timber.e(it, "Error while creating filter") },
            success = { dispatcher.send(it).also { isCompleted.emit(true) } }
        )
    }

    private suspend fun proceedWithCreatingFilter(ctx: Id, filter: DVFilter) {
        val block = objectSetState.value.dataview
        val dv = block.content as DV
        val viewer = dv.viewers.find { it.id == session.currentViewerId } ?: dv.viewers.first()
        updateDataViewViewer(
            UpdateDataViewViewer.Params(
                context = ctx,
                target = block.id,
                viewer = viewer.copy(filters = viewer.filters + listOf(filter))
            )
        ).process(
            failure = { Timber.e(it, "Error while creating filter") },
            success = { dispatcher.send(it).also { isCompleted.emit(true) } }
        )
    }

    private fun onFilterDateItemClicked(dateItem: CreateFilterView.Date) {
        if (dateItem.type == DateDescription.EXACT_DAY) {
            viewModelScope.launch { proceedWithDatePickerScreen(dateItem.timeInMillis) }
        } else {
            filterValueListState.value = updateSelectedState(dateItem)
        }
    }

    private suspend fun proceedWithDatePickerScreen(timeInMillis: Long) {
        commands.emit(Commands.OpenDatePicker(timeInSeconds = timeInMillis / 1000))
    }

    private suspend fun proceedWithConditionPickerScreen(type: Viewer.Filter.Type, index: Int) {
        commands.emit(Commands.OpenConditionPicker(type, index))
    }

    private fun updateSelectedState(
        selectedItem: CreateFilterView.Date
    ): List<CreateFilterView> = filterValueListState.value.map { view ->
        if (view is CreateFilterView.Date) {
            if (view.type == DateDescription.EXACT_DAY) {
                view.copy(
                    isSelected = false,
                    timeInMillis = EMPTY_TIMESTAMP
                )
            } else {
                if (view.type == selectedItem.type) {
                    view.copy(isSelected = true)
                } else {
                    view.copy(isSelected = false)
                }
            }
        } else {
            view
        }
    }

    class Factory(
        private val objectSetState: StateFlow<ObjectSet>,
        private val session: ObjectSetSession,
        private val dispatcher: Dispatcher<Payload>,
        private val updateDataViewViewer: UpdateDataViewViewer,
        private val searchObjects: SearchObjects,
        private val urlBuilder: UrlBuilder
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return FilterViewModel(
                objectSetState = objectSetState,
                session = session,
                dispatcher = dispatcher,
                updateDataViewViewer = updateDataViewViewer,
                searchObjects = searchObjects,
                urlBuilder = urlBuilder
            ) as T
        }
    }

    sealed class Commands {
        object ShowInput: Commands()
        object HideInput: Commands()
        object ShowSearchbar: Commands()
        object HideSearchbar: Commands()
        object ShowCount: Commands()
        object HideCount: Commands()
        data class OpenDatePicker(val timeInSeconds: Long) : Commands()
        data class OpenConditionPicker(val type: Viewer.Filter.Type, val index: Int) : Commands()
    }
}