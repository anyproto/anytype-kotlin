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
import com.anytypeio.anytype.domain.dataview.interactor.SetActiveViewer
import com.anytypeio.anytype.presentation.relations.ObjectSetConfig
import com.anytypeio.anytype.presentation.util.Dispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber

class EditDataViewViewerViewModel(
    private val renameDataViewViewer: RenameDataViewViewer,
    private val deleteDataViewViewer: DeleteDataViewViewer,
    private val duplicateDataViewViewer: DuplicateDataViewViewer,
    private val setActiveViewer: SetActiveViewer,
    private val dispatcher: Dispatcher<Payload>,
    private val objectSetState: StateFlow<ObjectSet>,
    private val objectSetSession: ObjectSetSession
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
        val state = objectSetState.value
        if (state.viewers.size > 1) {
            val targetIdx = state.viewers.indexOfFirst { it.id == viewer }
            val isActive = if (objectSetSession.currentViewerId != null) {
                objectSetSession.currentViewerId == viewer
            } else {
                targetIdx == 0
            }
            var nextViewerId: Id? = null
            if (isActive) {
                nextViewerId = if (targetIdx != state.viewers.lastIndex)
                    state.viewers[targetIdx.inc()].id
                else
                    state.viewers[targetIdx.dec()].id
            }
            proceedWithDeletion(
                ctx = ctx,
                dv = state.dataview.id,
                viewer = viewer,
                nextViewerId = nextViewerId,
                state = state
            )
        } else {
            viewModelScope.launch {
                _toasts.emit("Data view should have at least one view")
            }
        }
    }

    private fun proceedWithDeletion(
        ctx: Id,
        dv: Id,
        viewer: Id,
        nextViewerId: Id?,
        state: ObjectSet
    ) {
        viewModelScope.launch {
            deleteDataViewViewer(
                DeleteDataViewViewer.Params(
                    ctx = ctx,
                    viewer = viewer,
                    dataview = dv
                )
            ).process(
                failure = { e ->
                    Timber.e(e, "Error while deleting viewer: $viewer")
                    _toasts.emit("Error while deleting viewer: ${e.localizedMessage}")
                },
                success = { firstPayload ->
                    dispatcher.send(firstPayload)
                    if (nextViewerId != null) {
                        proceedWithSettingActiveView(ctx, state, nextViewerId)
                    } else {
                        isDismissed.emit(true)
                    }
                }
            )
        }
    }

    private fun proceedWithSettingActiveView(
        ctx: Id,
        state: ObjectSet,
        nextViewerId: Id
    ) {
        viewModelScope.launch {
            setActiveViewer(
                SetActiveViewer.Params(
                    context = ctx,
                    block = state.dataview.id,
                    view = nextViewerId,
                    limit = ObjectSetConfig.DEFAULT_LIMIT,
                    offset = 0
                )
            ).process(
                failure = { e ->
                    Timber.e(e, "Error while setting active viewer after deletion")
                },
                success = { secondPayload ->
                    dispatcher.send(secondPayload).also { isDismissed.emit(true) }
                }
            )
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
        private val setActiveViewer: SetActiveViewer,
        private val dispatcher: Dispatcher<Payload>,
        private val objectSetState: StateFlow<ObjectSet>,
        private val objectSetSession: ObjectSetSession
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return EditDataViewViewerViewModel(
                renameDataViewViewer = renameDataViewViewer,
                deleteDataViewViewer = deleteDataViewViewer,
                duplicateDataViewViewer = duplicateDataViewViewer,
                setActiveViewer = setActiveViewer,
                dispatcher = dispatcher,
                objectSetState = objectSetState,
                objectSetSession = objectSetSession
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