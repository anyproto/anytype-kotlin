package com.anytypeio.anytype.presentation.sets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.DVSort
import com.anytypeio.anytype.core_models.DVSortType
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.dataview.interactor.UpdateDataViewViewer
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.presentation.extension.ObjectStateAnalyticsEvent
import com.anytypeio.anytype.presentation.extension.logEvent
import com.anytypeio.anytype.presentation.relations.simpleRelations
import com.anytypeio.anytype.presentation.sets.model.SimpleRelationView
import com.anytypeio.anytype.presentation.sets.state.ObjectState
import com.anytypeio.anytype.presentation.util.Dispatcher
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class SelectSortRelationViewModel(
    private val objectState: StateFlow<ObjectState>,
    private val dispatcher: Dispatcher<Payload>,
    private val updateDataViewViewer: UpdateDataViewViewer,
    private val storeOfRelations: StoreOfRelations,
    private val analytics: Analytics
) : SearchRelationViewModel(
    objectState = objectState,
    storeOfRelations = storeOfRelations
) {

    fun onRelationClicked(ctx: Id, viewerId: Id, relation: SimpleRelationView) {
        val state = objectState.value.dataViewState() ?: return
        val viewer = state.viewerById(viewerId) ?: return
        val startTime = System.currentTimeMillis()
        viewModelScope.launch {
            val params = UpdateDataViewViewer.Params.Sort.Add(
                ctx = ctx,
                dv = state.dataViewBlock.id,
                view = viewer.id,
                sort = DVSort(
                    relationKey = relation.key,
                    type = DVSortType.ASC
                )
            )
            updateDataViewViewer(params).process(
                success = {
                    dispatcher.send(it).also {
                        logEvent(
                            state = objectState.value,
                            analytics = analytics,
                            event = ObjectStateAnalyticsEvent.ADD_SORT,
                            startTime = startTime,
                            type = DVSortType.ASC.formattedName
                        )
                        isDismissed.emit(true)
                    }
                },
                failure = {
                    Timber.e(it, "Error while adding a sort").also {
                        _toasts.emit(USE_CASE_ERROR)
                    }
                }
            )
        }
    }

    class Factory(
        private val objectState: StateFlow<ObjectState>,
        private val dispatcher: Dispatcher<Payload>,
        private val updateDataViewViewer: UpdateDataViewViewer,
        private val storeOfRelations: StoreOfRelations,
        private val analytics: Analytics
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SelectSortRelationViewModel(
                objectState = objectState,
                dispatcher = dispatcher,
                updateDataViewViewer = updateDataViewViewer,
                storeOfRelations = storeOfRelations,
                analytics = analytics
            ) as T
        }
    }

    companion object {
        const val USE_CASE_ERROR = "Couldn't add a sort. Please, try again."
    }

    override suspend fun filterRelationsFromAlreadyInUse(
        objectState: ObjectState,
        viewerId: String?,
        storeOfRelations: StoreOfRelations
    ): List<SimpleRelationView> {
        val dataViewState = objectState.dataViewState() ?: return emptyList()
        val relationsInUse: List<String> = run {
            val dv = dataViewState.dataViewContent
            val viewer = dv.viewers.find { it.id == viewerId } ?: dv.viewers.first()
            viewer.sorts.map { it.relationKey }.toList()
        }
        return objectState.simpleRelations(
            viewerId = viewerId,
            storeOfRelations = storeOfRelations
        ).filterNot { it.key in relationsInUse }
    }
}