package com.anytypeio.anytype.presentation.sets.filter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.*
import com.anytypeio.anytype.domain.dataview.interactor.UpdateDataViewViewer
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.presentation.common.BaseListViewModel
import com.anytypeio.anytype.presentation.extension.sendAnalyticsRemoveFilterEvent
import com.anytypeio.anytype.presentation.extension.toView
import com.anytypeio.anytype.presentation.relations.filterExpression
import com.anytypeio.anytype.presentation.sets.ObjectSetDatabase
import com.anytypeio.anytype.presentation.sets.ObjectSetSession
import com.anytypeio.anytype.presentation.sets.dataViewState
import com.anytypeio.anytype.presentation.sets.filter.ViewerFilterCommand.Modal
import com.anytypeio.anytype.presentation.sets.model.FilterView
import com.anytypeio.anytype.presentation.sets.state.ObjectState
import com.anytypeio.anytype.presentation.sets.viewerById
import com.anytypeio.anytype.presentation.util.Dispatcher
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber

class ViewerFilterViewModel(
    private val objectState: StateFlow<ObjectState>,
    private val session: ObjectSetSession,
    private val dispatcher: Dispatcher<Payload>,
    private val updateDataViewViewer: UpdateDataViewViewer,
    private val urlBuilder: UrlBuilder,
    private val storeOfRelations: StoreOfRelations,
    private val db: ObjectSetDatabase,
    private val analytics: Analytics
) : BaseListViewModel<FilterView>() {

    val screenState = MutableStateFlow(ScreenState.LIST)

    val commands = MutableSharedFlow<ViewerFilterCommand>()

    init {
        viewModelScope.launch {
            objectState.filterIsInstance<ObjectState.DataView>().collect { objectSet ->
                val filterExpression = objectSet.filterExpression(session.currentViewerId.value)
                if (filterExpression.isEmpty()) {
                    screenState.value = ScreenState.EMPTY
                } else {
                    screenState.value = when (screenState.value) {
                        ScreenState.LIST -> ScreenState.LIST
                        ScreenState.EDIT -> ScreenState.EDIT
                        ScreenState.EMPTY -> ScreenState.LIST
                    }
                }
                _views.value = filterExpression.toView(
                    storeOfRelations = storeOfRelations,
                    storeOfObjects = db.store,
                    details = objectSet.details,
                    screenState = screenState.value,
                    urlBuilder = urlBuilder
                )
            }
        }
    }

    fun onFilterClicked(ctx: Id, click: FilterClick) {
        when (click) {
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
                emitCommand(Modal.UpdateInputValueFilter(filter.relation, filterIndex))
            }
            is FilterView.Expression.Number -> {
                emitCommand(Modal.UpdateInputValueFilter(filter.relation, filterIndex))
            }
            is FilterView.Expression.Tag -> {
                emitCommand(Modal.UpdateSelectValueFilter(filter.relation, filterIndex))
            }
            is FilterView.Expression.Status -> {
                emitCommand(Modal.UpdateSelectValueFilter(filter.relation, filterIndex))
            }
            is FilterView.Expression.Date -> {
                emitCommand(Modal.UpdateSelectValueFilter(filter.relation, filterIndex))
            }
            is FilterView.Expression.Email -> {
                emitCommand(Modal.UpdateInputValueFilter(filter.relation, filterIndex))
            }
            is FilterView.Expression.Object -> {
                emitCommand(Modal.UpdateSelectValueFilter(filter.relation, filterIndex))
            }
            is FilterView.Expression.Phone -> {
                emitCommand(Modal.UpdateInputValueFilter(filter.relation, filterIndex))
            }
            is FilterView.Expression.TextShort -> {
                emitCommand(Modal.UpdateInputValueFilter(filter.relation, filterIndex))
            }
            is FilterView.Expression.Url -> {
                emitCommand(Modal.UpdateInputValueFilter(filter.relation, filterIndex))
            }
            is FilterView.Expression.Checkbox -> {
                emitCommand(Modal.UpdateSelectValueFilter(filter.relation, filterIndex))
            }
        }
    }

    private fun onRemoveFilterClicked(ctx: Id, filterIndex: Int) {
        val state = objectState.value.dataViewState() ?: return
        val viewer = state.viewerById(session.currentViewerId.value) ?: return
        val filter = viewer.filters.getOrNull(filterIndex)
        if (filter == null) {
            Timber.e("Filter with index $filterIndex not found")
            return
        }
        viewModelScope.launch {
            val params = UpdateDataViewViewer.Params.Filter.Remove(
                ctx = ctx,
                dv = state.dataViewBlock.id,
                view = viewer.id,
                ids = listOf(filter.id),
            )
            updateDataViewViewer(params).process(
                success = { dispatcher.send(it) },
                failure = { Timber.e("Error while reset all filters") }
            )
            sendAnalyticsRemoveFilterEvent(analytics)
        }
    }

    private fun emitCommand(command: ViewerFilterCommand) {
        viewModelScope.launch {
            commands.emit(command)
        }
    }

    class Factory(
        private val state: StateFlow<ObjectState>,
        private val session: ObjectSetSession,
        private val dispatcher: Dispatcher<Payload>,
        private val updateDataViewViewer: UpdateDataViewViewer,
        private val urlBuilder: UrlBuilder,
        private val analytics: Analytics,
        private val storeOfRelations: StoreOfRelations,
        private val objectSetDatabase: ObjectSetDatabase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ViewerFilterViewModel(
                objectState = state,
                session = session,
                dispatcher = dispatcher,
                updateDataViewViewer = updateDataViewViewer,
                urlBuilder = urlBuilder,
                analytics = analytics,
                storeOfRelations = storeOfRelations,
                db = objectSetDatabase
            ) as T
        }
    }

    enum class ScreenState { LIST, EDIT, EMPTY }
}