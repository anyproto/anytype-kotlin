package com.anytypeio.anytype.presentation.sets.filter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.*
import com.anytypeio.anytype.domain.dataview.interactor.UpdateDataViewViewer
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.presentation.common.BaseListViewModel
import com.anytypeio.anytype.presentation.relations.filterExpression
import com.anytypeio.anytype.presentation.relations.toView
import com.anytypeio.anytype.presentation.sets.ObjectSet
import com.anytypeio.anytype.presentation.sets.ObjectSetSession
import com.anytypeio.anytype.presentation.sets.filter.ViewerFilterCommand.Modal
import com.anytypeio.anytype.presentation.sets.model.FilterScreenData
import com.anytypeio.anytype.presentation.sets.model.FilterView
import com.anytypeio.anytype.presentation.sets.viewerById
import com.anytypeio.anytype.presentation.util.Dispatcher
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber

class ViewerFilterViewModel(
    private val objectSetState: StateFlow<ObjectSet>,
    private val session: ObjectSetSession,
    private val dispatcher: Dispatcher<Payload>,
    private val updateDataViewViewer: UpdateDataViewViewer,
    private val urlBuilder: UrlBuilder
) : BaseListViewModel<FilterView>() {

    val screenState = MutableStateFlow(ScreenState.LIST)

    val data: MutableStateFlow<FilterScreenData> = MutableStateFlow(FilterScreenData.empty())
    val commands = MutableSharedFlow<ViewerFilterCommand>()

    init {
        viewModelScope.launch {
            objectSetState.filter { it.isInitialized }.collect { objectSet ->
                val block = objectSet.dataview
                val dv = block.content as DV
                val filterExpression = objectSet.filterExpression(session.currentViewerId)
                if (filterExpression.isEmpty()) {
                    screenState.value = ScreenState.EMPTY
                } else {
                    screenState.value = when (screenState.value) {
                        ScreenState.LIST -> ScreenState.LIST
                        ScreenState.EDIT -> ScreenState.EDIT
                        ScreenState.EMPTY -> ScreenState.LIST
                    }
                }
                _views.value = getFilterViews(
                    relations = dv.relations,
                    filters = filterExpression,
                    details = objectSet.details,
                    screenState = screenState.value,
                    urlBuilder = urlBuilder
                )
            }
        }
    }

    fun onFilterClicked(ctx: Id, click: FilterClick) {
        when (click) {
            FilterClick.Apply -> {
                onApplyFiltersClicked()
            }
            is FilterClick.Remove -> {
                onRemoveFilterClicked(ctx, click.index)
            }
            is FilterClick.Value -> {
                onValueClicked(click.index)
            }
        }
    }

    fun onDoneButtonClicked() {
        screenState.value = ScreenState.LIST
        _views.value = _views.value.map { setStateInFilterView(it) }
    }

    fun onEditButtonClicked() {
        screenState.value = ScreenState.EDIT
        _views.value = _views.value.map { setStateInFilterView(it) }
    }

    private fun setStateInFilterView(filterView: FilterView): FilterView =
        when (filterView) {
            is FilterView.Expression.Text ->
                filterView.copy(isInEditMode = screenState.value == ScreenState.EDIT)
            is FilterView.Expression.Number ->
                filterView.copy(isInEditMode = screenState.value == ScreenState.EDIT)
            is FilterView.Expression.Status ->
                filterView.copy(isInEditMode = screenState.value == ScreenState.EDIT)
            is FilterView.Expression.Tag ->
                filterView.copy(isInEditMode = screenState.value == ScreenState.EDIT)
            is FilterView.Expression.Date ->
                filterView.copy(isInEditMode = screenState.value == ScreenState.EDIT)
            is FilterView.Expression.Object ->
                filterView.copy(isInEditMode = screenState.value == ScreenState.EDIT)
            is FilterView.Expression.Email ->
                filterView.copy(isInEditMode = screenState.value == ScreenState.EDIT)
            is FilterView.Expression.Phone ->
                filterView.copy(isInEditMode = screenState.value == ScreenState.EDIT)
            is FilterView.Expression.TextShort ->
                filterView.copy(isInEditMode = screenState.value == ScreenState.EDIT)
            is FilterView.Expression.Url ->
                filterView.copy(isInEditMode = screenState.value == ScreenState.EDIT)
            is FilterView.Expression.Checkbox ->
                filterView.copy(isInEditMode = screenState.value == ScreenState.EDIT)
        }

    private fun onApplyFiltersClicked() {
        emitCommand(ViewerFilterCommand.Apply(data.value.filters))
    }

    fun onAddNewFilterClicked() {
        emitCommand(Modal.ShowRelationList)
    }

    private fun onValueClicked(filterIndex: Int) {
        if (screenState.value == ScreenState.EDIT) return
        proceedToFilterValueScreen(filterIndex)
    }

    private fun proceedToFilterValueScreen(filterIndex: Int) {
        when (val filter = _views.value[filterIndex]) {
            is FilterView.Expression.Text -> {
                emitCommand(Modal.UpdateInputValueFilter(filter.key, filterIndex))
            }
            is FilterView.Expression.Number -> {
                emitCommand(Modal.UpdateInputValueFilter(filter.key, filterIndex))
            }
            is FilterView.Expression.Tag -> {
                emitCommand(Modal.UpdateSelectValueFilter(filter.key, filterIndex))
            }
            is FilterView.Expression.Status -> {
                emitCommand(Modal.UpdateSelectValueFilter(filter.key, filterIndex))
            }
            is FilterView.Expression.Date -> {
                emitCommand(Modal.UpdateSelectValueFilter(filter.key, filterIndex))
            }
            is FilterView.Expression.Email -> {
                emitCommand(Modal.UpdateInputValueFilter(filter.key, filterIndex))
            }
            is FilterView.Expression.Object -> {
                emitCommand(Modal.UpdateSelectValueFilter(filter.key, filterIndex))
            }
            is FilterView.Expression.Phone -> {
                emitCommand(Modal.UpdateInputValueFilter(filter.key, filterIndex))
            }
            is FilterView.Expression.TextShort -> {
                emitCommand(Modal.UpdateInputValueFilter(filter.key, filterIndex))
            }
            is FilterView.Expression.Url -> {
                emitCommand(Modal.UpdateInputValueFilter(filter.key, filterIndex))
            }
            is FilterView.Expression.Checkbox -> {
                emitCommand(Modal.UpdateSelectValueFilter(filter.key, filterIndex))
            }
        }
    }

    private fun onRemoveFilterClicked(ctx: Id, filterIndex: Int) {
        val viewer = objectSetState.value.viewerById(session.currentViewerId)
        val block = objectSetState.value.blocks.first { it.content is DV }
        val filters = viewer.filters.mapIndexedNotNull { index, filter ->
            if (index != filterIndex) {
                filter
            } else {
                null
            }
        }
        val updated = viewer.copy(
            filters = filters
        )
        viewModelScope.launch {
            updateDataViewViewer(
                UpdateDataViewViewer.Params(
                    context = ctx,
                    target = block.id,
                    viewer = updated
                )
            ).process(
                success = { dispatcher.send(it) },
                failure = { Timber.e("Error while reset all filters") }
            )
        }
    }

    private fun getFilterViews(
        relations: List<Relation>,
        filters: List<DVFilter>,
        details: Map<Id, Block.Fields>,
        screenState: ScreenState,
        urlBuilder: UrlBuilder
    ): List<FilterView> {
        val list = mutableListOf<FilterView>()
        filters.forEach { filter ->
            //todo Fast fix. In feature we should proper handle DVFilterCondition.NONE
            if (filter.condition != DVFilterCondition.NONE) {
                list.add(
                    filter.toView(
                        relation = relations.first { it.key == filter.relationKey },
                        details = details,
                        isInEditMode = screenState == ScreenState.EDIT,
                        urlBuilder = urlBuilder
                    )
                )
            }
        }
        return list
    }

    private fun emitCommand(command: ViewerFilterCommand) {
        viewModelScope.launch {
            commands.emit(command)
        }
    }

    class Factory(
        private val state: StateFlow<ObjectSet>,
        private val session: ObjectSetSession,
        private val dispatcher: Dispatcher<Payload>,
        private val updateDataViewViewer: UpdateDataViewViewer,
        private val urlBuilder: UrlBuilder
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return ViewerFilterViewModel(
                objectSetState = state,
                session = session,
                dispatcher = dispatcher,
                updateDataViewViewer = updateDataViewViewer,
                urlBuilder = urlBuilder
            ) as T
        }
    }

    enum class ScreenState { LIST, EDIT, EMPTY }
}