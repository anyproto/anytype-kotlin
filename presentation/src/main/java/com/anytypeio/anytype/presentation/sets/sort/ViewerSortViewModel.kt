package com.anytypeio.anytype.presentation.sets.sort

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.*
import com.anytypeio.anytype.core_utils.diff.DefaultObjectDiffIdentifier
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.dataview.interactor.UpdateDataViewViewer
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.presentation.common.BaseListViewModel
import com.anytypeio.anytype.presentation.extension.ObjectStateAnalyticsEvent
import com.anytypeio.anytype.presentation.extension.logEvent
import com.anytypeio.anytype.presentation.extension.toView
import com.anytypeio.anytype.presentation.sets.dataViewState
import com.anytypeio.anytype.presentation.sets.state.ObjectState
import com.anytypeio.anytype.presentation.sets.viewerById
import com.anytypeio.anytype.presentation.util.Dispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber

class ViewerSortViewModel(
    private val objectState: StateFlow<ObjectState>,
    private val dispatcher: Dispatcher<Payload>,
    private val updateDataViewViewer: UpdateDataViewViewer,
    private val analytics: Analytics,
    private val storeOfRelations: StoreOfRelations
) : BaseListViewModel<ViewerSortViewModel.ViewerSortView>() {

    val isDismissed = MutableSharedFlow<Boolean>(replay = 0)
    val screenState = MutableStateFlow(ScreenState.READ)
    private val jobs = mutableListOf<Job>()

    fun onStart(viewerId: Id) {
        jobs += viewModelScope.launch {
            objectState.filterIsInstance<ObjectState.DataView>().collect { state ->
                val viewer = state.viewerById(viewerId) ?: return@collect
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
                        screenState = screenState.value,
                        storeOfRelations = storeOfRelations
                    )
                } catch (e: Exception) {
                    Timber.e(e, "Error while building views")
                }
            }
        }
    }

    fun onStop() {
        jobs.forEach { it.cancel() }
        jobs.clear()
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

    fun onRemoveViewerSortClicked(ctx: Id, viewerId: Id, view: ViewerSortView) {
        val state = objectState.value.dataViewState() ?: return
        val viewer = state.viewerById(viewerId) ?: return
        val startTime = System.currentTimeMillis()
        viewModelScope.launch {
            val params = UpdateDataViewViewer.Params.Sort.Remove(
                ctx = ctx,
                dv = state.dataViewBlock.id,
                view = viewer.id,
                ids = listOf(view.sortId)
            )
            updateDataViewViewer.async(params).fold(
                onFailure = { Timber.e(it, "Error while removing a sort") },
                onSuccess = {
                    dispatcher.send(it).also {
                        logEvent(
                            state = objectState.value,
                            analytics = analytics,
                            event = ObjectStateAnalyticsEvent.REMOVE_SORT,
                            startTime = startTime,
                        )
                    }
                }
            )
        }
    }

    private suspend fun buildViews(
        sorts: List<DVSort>,
        screenState: ScreenState,
        storeOfRelations: StoreOfRelations
    ): List<ViewerSortView> {
        Timber.d("Build Viewer Sorting Views, sorts:[$sorts], screenState:[$screenState]")
        return sorts.toView(
            storeOfRelations = storeOfRelations,
            screenState = screenState
        )
    }

    /**
     * @property [sortId] sort id
     * @property [relation] key of the relation, to which this sort is applied.
     * @property [name] relation name
     * @property [format] relation format
     * @property [type] sort type
     */
    data class ViewerSortView(
        val sortId: Id,
        val relation: Key,
        val name: String,
        val format: Relation.Format,
        val type: DVSortType,
        val mode: ScreenState
    ) : DefaultObjectDiffIdentifier {
        override val identifier: String get() = relation
    }

    enum class ScreenState { READ, EDIT, EMPTY }

    class Factory(
        private val state: StateFlow<ObjectState>,
        private val dispatcher: Dispatcher<Payload>,
        private val updateDataViewViewer: UpdateDataViewViewer,
        private val analytics: Analytics,
        private val storeOfRelations: StoreOfRelations
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ViewerSortViewModel(
                objectState = state,
                updateDataViewViewer = updateDataViewViewer,
                dispatcher = dispatcher,
                analytics = analytics,
                storeOfRelations = storeOfRelations
            ) as T
        }
    }
}