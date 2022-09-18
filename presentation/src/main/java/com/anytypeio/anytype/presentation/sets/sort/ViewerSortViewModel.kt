package com.anytypeio.anytype.presentation.sets.sort

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.*
import com.anytypeio.anytype.core_utils.diff.DefaultObjectDiffIdentifier
import com.anytypeio.anytype.domain.dataview.interactor.UpdateDataViewViewer
import com.anytypeio.anytype.presentation.common.BaseListViewModel
import com.anytypeio.anytype.presentation.extension.sendAnalyticsRemoveSortEvent
import com.anytypeio.anytype.presentation.sets.ObjectSet
import com.anytypeio.anytype.presentation.sets.ObjectSetSession
import com.anytypeio.anytype.presentation.sets.viewerById
import com.anytypeio.anytype.presentation.util.Dispatcher
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber

class ViewerSortViewModel(
    private val objectSetState: StateFlow<ObjectSet>,
    private val session: ObjectSetSession,
    private val dispatcher: Dispatcher<Payload>,
    private val updateDataViewViewer: UpdateDataViewViewer,
    private val analytics: Analytics
) : BaseListViewModel<ViewerSortViewModel.ViewerSortView>() {

    val isDismissed = MutableSharedFlow<Boolean>(replay = 0)

    val screenState = MutableStateFlow(ScreenState.READ)

    init {
        viewModelScope.launch {
            objectSetState.filter { it.isInitialized }.collect { state ->
                val dv = state.dataview.content as DV
                val viewer = dv.viewers.find { it.id == session.currentViewerId.value }
                    ?: dv.viewers.first()
                val sorts = viewer.sorts
                if (sorts.isEmpty()) {
                    screenState.value = ScreenState.EMPTY
                } else {
                    screenState.value = when (screenState.value) {
                        ScreenState.READ -> ScreenState.READ
                        ScreenState.EDIT -> ScreenState.EDIT
                        ScreenState.EMPTY -> ScreenState.READ
                    }
                }
                try {
                    _views.value = buildViews(
                        sorts = sorts,
                        dv = dv,
                        screenState = screenState.value
                    )
                } catch (e: Exception) {
                    Timber.e(e, "Error while building views")
                }
            }
        }
    }

    fun onEditClicked() {
        screenState.value = ScreenState.EDIT
        _views.value = views.value.map { view ->
            view.copy(
                mode = ScreenState.EDIT
            )
        }
    }

    fun onDoneClicked() {
        screenState.value = ScreenState.READ
        _views.value = views.value.map { view ->
            view.copy(
                mode = ScreenState.READ
            )
        }
    }

    fun onRemoveViewerSortClicked(ctx: Id, view: ViewerSortView) {
        viewModelScope.launch {
            val state = objectSetState.value
            val viewer = state.viewerById(session.currentViewerId.value)
            val sorts = viewer.sorts.filter { it.relationKey != view.relation }
            updateDataViewViewer(
                UpdateDataViewViewer.Params(
                    context = ctx,
                    target = state.dataview.id,
                    viewer = viewer.copy(sorts = sorts)
                )
            ).process(
                failure = { Timber.e(it, "Error while removing a sort") },
                success = { dispatcher.send(it) }
            )
            sendAnalyticsRemoveSortEvent(analytics)
        }
    }

    private fun buildViews(
        sorts: List<DVSort>,
        dv: DV,
        screenState: ScreenState
    ): List<ViewerSortView> = sorts
        .associateWith { sort -> dv.relations.first { it.key == sort.relationKey } }
        .map { (sort, relation) ->
            ViewerSortView(
                relation = sort.relationKey,
                name = relation.name,
                type = sort.type,
                format = relation.format,
                mode = screenState
            )
        }

    /**
     * @property [relation] id of the relation, to which this sort is applied.
     * @property [name] relation name
     * @property [format] relation format
     * @property [type] sort type
     */
    data class ViewerSortView(
        val relation: Id,
        val name: String,
        val format: Relation.Format,
        val type: DVSortType,
        val mode: ScreenState
    ) : DefaultObjectDiffIdentifier {
        override val identifier: String get() = relation
    }

    enum class ScreenState { READ, EDIT, EMPTY }

    class Factory(
        private val state: StateFlow<ObjectSet>,
        private val session: ObjectSetSession,
        private val dispatcher: Dispatcher<Payload>,
        private val updateDataViewViewer: UpdateDataViewViewer,
        private val analytics: Analytics
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ViewerSortViewModel(
                objectSetState = state,
                session = session,
                updateDataViewViewer = updateDataViewViewer,
                dispatcher = dispatcher,
                analytics = analytics
            ) as T
        }
    }
}