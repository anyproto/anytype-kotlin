package com.anytypeio.anytype.presentation.sets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.dataview.interactor.DeleteDataViewViewer
import com.anytypeio.anytype.domain.dataview.interactor.DuplicateDataViewViewer
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.util.Dispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class DataViewViewerActionViewModel(
    private val duplicateDataViewViewer: DuplicateDataViewViewer,
    private val deleteDataViewViewer: DeleteDataViewViewer,
    private val dispatcher: Dispatcher<Payload>,
    private val objectSetState: StateFlow<ObjectSet>
) : BaseViewModel() {

    val isDismissed = MutableSharedFlow<Boolean>(replay = 0)

    fun onDeleteClicked(ctx: Id, viewer: Id) {
        viewModelScope.launch {
            deleteDataViewViewer(
                DeleteDataViewViewer.Params(
                    ctx = ctx,
                    viewer = viewer,
                    dataview = objectSetState.value.dataview.id
                )
            ).process(
                failure = { e ->
                    Timber.e(e, "Error while deleting viewer: $viewer")
                    _toasts.emit("Error while deleting viewer: ${e.localizedMessage}")
                },
                success = {
                    dispatcher.send(it).also { isDismissed.emit(true) }
                }
            )
        }
    }

    fun onDuplicateClicked(ctx: Id, viewer: Id) {
        viewModelScope.launch {
            duplicateDataViewViewer(
                DuplicateDataViewViewer.Params(
                    context = ctx,
                    target = objectSetState.value.dataview.id,
                    viewer = objectSetState.value.viewers.first { it.id == viewer }
                )
            ).process(
                failure = { e ->
                    Timber.e(e, "Error while duplicating viewer: $viewer")
                    _toasts.emit("Error while deleting viewer: ${e.localizedMessage}")
                },
                success = {
                    dispatcher.send(it).also { isDismissed.emit(true) }
                }
            )
        }
    }

    class Factory(
        private val duplicateDataViewViewer: DuplicateDataViewViewer,
        private val deleteDataViewViewer: DeleteDataViewViewer,
        private val dispatcher: Dispatcher<Payload>,
        private val objectSetState: StateFlow<ObjectSet>
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return DataViewViewerActionViewModel(
                duplicateDataViewViewer = duplicateDataViewViewer,
                deleteDataViewViewer = deleteDataViewViewer,
                dispatcher = dispatcher,
                objectSetState = objectSetState
            ) as T
        }
    }
}