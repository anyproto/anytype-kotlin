package com.anytypeio.anytype.presentation.sets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.DVSort
import com.anytypeio.anytype.core_models.DVSortType
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.dataview.interactor.AddDataViewViewerSort
import com.anytypeio.anytype.presentation.sets.model.SimpleRelationView
import com.anytypeio.anytype.presentation.util.Dispatcher
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class SelectSortRelationViewModel(
    private val objectSetState: StateFlow<ObjectSet>,
    private val session: ObjectSetSession,
    private val dispatcher: Dispatcher<Payload>,
    private val addDataViewViewerSort: AddDataViewViewerSort
) : SearchRelationViewModel(objectSetState, session) {

    fun onRelationClicked(ctx: Id, relation: SimpleRelationView) {
        viewModelScope.launch {
            addDataViewViewerSort(
                AddDataViewViewerSort.Params(
                    ctx = ctx,
                    sort = DVSort(
                        relationKey = relation.key,
                        type = DVSortType.ASC
                    ),
                    dataview = objectSetState.value.dataview.id,
                    viewer = objectSetState.value.viewerById(session.currentViewerId)
                )
            ).process(
                success = { dispatcher.send(it).also { isDismissed.emit(true) } },
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
        private val addDataViewViewerSort: AddDataViewViewerSort
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SelectSortRelationViewModel(
                objectSetState = state,
                session = session,
                dispatcher = dispatcher,
                addDataViewViewerSort = addDataViewViewerSort
            ) as T
        }
    }

    companion object {
        const val USE_CASE_ERROR = "Couldn't add a sort. Please, try again."
    }
}