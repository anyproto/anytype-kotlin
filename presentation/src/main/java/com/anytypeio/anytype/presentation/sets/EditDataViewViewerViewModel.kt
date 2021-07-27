package com.anytypeio.anytype.presentation.sets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.DVViewer
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.dataview.interactor.DeleteDataViewViewer
import com.anytypeio.anytype.domain.dataview.interactor.DuplicateDataViewViewer
import com.anytypeio.anytype.domain.dataview.interactor.RenameDataViewViewer
import com.anytypeio.anytype.presentation.util.Dispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber

class EditDataViewViewerViewModel(
    private val renameDataViewViewer: RenameDataViewViewer,
    private val dispatcher: Dispatcher<Payload>,
    private val objectSetState: StateFlow<ObjectSet>,
    private val deleteDataViewViewer: DeleteDataViewViewer,
    private val duplicateDataViewViewer: DuplicateDataViewViewer
) : ViewModel() {

    private val viewerNameUpdatePipeline = Channel<ViewerNameUpdate>()

    val isDismissed = MutableSharedFlow<Boolean>(replay = 0)
    val popupCommands = MutableSharedFlow<PopupMenuCommand>(replay = 0)

    private val _toasts = MutableSharedFlow<String>()
    val toasts: SharedFlow<String> = _toasts

    init {
        runViewerNameUpdatePipeline()
    }

    private fun runViewerNameUpdatePipeline() {
        viewModelScope.launch {
            viewerNameUpdatePipeline
                .consumeAsFlow()
                .distinctUntilChanged()
                .mapLatest { update ->
                    renameDataViewViewer(
                        RenameDataViewViewer.Params(
                            context = update.ctx,
                            target = update.dataview,
                            viewer = update.viewer.copy(name = update.name)
                        )
                    ).process(
                        failure = { Timber.e(it, "Error while renaming viewer") },
                        success = { dispatcher.send(it) }
                    )
                }
                .collect()
        }
    }

    fun onViewerNameChanged(ctx: Id, viewer: Id, name: String) {
        viewModelScope.launch {
            viewerNameUpdatePipeline.send(
                ViewerNameUpdate(
                    ctx = ctx,
                    viewer = objectSetState.value.viewers.first { it.id == viewer },
                    name = name,
                    dataview = objectSetState.value.dataview.id
                )
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

    fun onDeleteClicked(ctx: Id, viewer: Id) {
        viewModelScope.launch {
            if (objectSetState.value.viewers.size > 1) {
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
            } else {
                _toasts.emit("Data view should have at least one view")
            }
        }
    }

    fun onMenuClicked() {
        viewModelScope.launch {
            popupCommands.emit(
                PopupMenuCommand(isDeletionAllowed = objectSetState.value.viewers.size > 1)
            )
        }
    }

    fun onDoneClicked() {
        viewModelScope.launch { isDismissed.emit(true) }
    }

    class Factory(
        private val renameDataViewViewer: RenameDataViewViewer,
        private val deleteDataViewViewer: DeleteDataViewViewer,
        private val duplicateDataViewViewer: DuplicateDataViewViewer,
        private val dispatcher: Dispatcher<Payload>,
        private val objectSetState: StateFlow<ObjectSet>
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return EditDataViewViewerViewModel(
                renameDataViewViewer = renameDataViewViewer,
                deleteDataViewViewer = deleteDataViewViewer,
                duplicateDataViewViewer = duplicateDataViewViewer,
                dispatcher = dispatcher,
                objectSetState = objectSetState
            ) as T
        }
    }

    private data class ViewerNameUpdate(
        val ctx: Id,
        val dataview: Id,
        val viewer: DVViewer,
        val name: String
    )

    data class PopupMenuCommand(val isDeletionAllowed: Boolean = false)
}