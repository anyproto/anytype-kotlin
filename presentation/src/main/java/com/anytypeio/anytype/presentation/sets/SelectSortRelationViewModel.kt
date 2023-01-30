package com.anytypeio.anytype.presentation.sets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.DV
import com.anytypeio.anytype.core_models.DVSort
import com.anytypeio.anytype.core_models.DVSortType
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.dataview.interactor.UpdateDataViewViewer
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.presentation.extension.sendAnalyticsAddSortEvent
import com.anytypeio.anytype.presentation.relations.simpleRelations
import com.anytypeio.anytype.presentation.sets.model.SimpleRelationView
import com.anytypeio.anytype.presentation.util.Dispatcher
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class SelectSortRelationViewModel(
    private val objectSetState: StateFlow<ObjectSet>,
    private val session: ObjectSetSession,
    private val dispatcher: Dispatcher<Payload>,
    private val updateDataViewViewer: UpdateDataViewViewer,
    private val storeOfRelations: StoreOfRelations,
    private val analytics: Analytics
) : SearchRelationViewModel(
    objectSetState = objectSetState,
    session = session,
    storeOfRelations = storeOfRelations
) {

    fun onRelationClicked(ctx: Id, relation: SimpleRelationView) {
        viewModelScope.launch {
            val params = UpdateDataViewViewer.Params.Sort.Add(
                ctx = ctx,
                dv = objectSetState.value.dataview.id,
                view = objectSetState.value.viewerById(session.currentViewerId.value).id,
                sort = DVSort(
                    relationKey = relation.key,
                    type = DVSortType.ASC
                )
            )
            updateDataViewViewer(params).process(
                success = {
                    dispatcher.send(it).also {
                        sendAnalyticsAddSortEvent(analytics)
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
        private val state: StateFlow<ObjectSet>,
        private val session: ObjectSetSession,
        private val dispatcher: Dispatcher<Payload>,
        private val updateDataViewViewer: UpdateDataViewViewer,
        private val storeOfRelations: StoreOfRelations,
        private val analytics: Analytics
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SelectSortRelationViewModel(
                objectSetState = state,
                session = session,
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
        set: ObjectSet,
        viewerId: String?,
        storeOfRelations: StoreOfRelations
    ): List<SimpleRelationView> {
        val relationsInUse: List<String> = run {
            val block = set.blocks.first { it.content is DV }
            val dv = block.content as DV
            val viewer = dv.viewers.find { it.id == viewerId } ?: dv.viewers.first()
            viewer.sorts.map { it.relationKey }.toList()
        }
        return set.simpleRelations(
            viewerId = viewerId,
            storeOfRelations = storeOfRelations
        ).filterNot { it.key in relationsInUse }
    }
}