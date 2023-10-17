package com.anytypeio.anytype.presentation.sets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.*
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.dataview.interactor.*
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.extension.ObjectStateAnalyticsEvent
import com.anytypeio.anytype.presentation.extension.logEvent
import com.anytypeio.anytype.presentation.sets.state.ObjectState
import com.anytypeio.anytype.presentation.util.Dispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class EditDataViewViewerViewModel(
    private val deleteDataViewViewer: DeleteDataViewViewer,
    private val duplicateDataViewViewer: DuplicateDataViewViewer,
    private val updateDataViewViewer: UpdateDataViewViewer,
    private val dispatcher: Dispatcher<Payload>,
    private val objectState: StateFlow<ObjectState>,
    private val objectSetSession: ObjectSetSession,
    private val paginator: ObjectSetPaginator,
    private val analytics: Analytics
) : BaseViewModel() {

    val viewState = MutableStateFlow<ViewState>(ViewState.Init)
    val isDismissed = MutableSharedFlow<Boolean>(replay = 0)
    val isLoading = MutableStateFlow(false)
    val popupCommands = MutableSharedFlow<PopupMenuCommand>(replay = 0)

    var initialName: String = ""
    var initialType: DVViewerType = DVViewerType.GRID

    private var viewerType: DVViewerType = DVViewerType.GRID
    private var viewerName: String = ""

    fun onStart(viewerId: Id) {
        val state = objectState.value.dataViewState() ?: return
        val viewer = state.viewers.firstOrNull { it.id == viewerId }
        if (viewer != null) {
            initialName = viewer.name
            initialType = viewer.type
            viewState.value = ViewState.Name(viewer.name)
            updateViewState(viewer.type)
        } else {
            Timber.e("Can't find viewer by id : $viewerId")
        }
    }

    fun onViewerNameChanged(name: String) {
        viewerName = name
    }

    fun onDuplicateClicked(ctx: Id, viewer: Id) {
        val startTime = System.currentTimeMillis()
        val state = objectState.value.dataViewState() ?: return
        viewModelScope.launch {
            duplicateDataViewViewer.async(
                DuplicateDataViewViewer.Params(
                    context = ctx,
                    target = state.dataViewBlock.id,
                    viewer = state.viewers.first { it.id == viewer }
                )
            ).fold(
                onFailure = { e ->
                    Timber.e(e, "Error while duplicating viewer: $viewer")
                    _toasts.emit("Error while deleting viewer: ${e.localizedMessage}")
                },
                onSuccess = { (_, payload) ->
                    dispatcher.send(payload).also {
                        logEvent(
                            state = objectState.value,
                            analytics = analytics,
                            event = ObjectStateAnalyticsEvent.DUPLICATE_VIEW,
                            startTime = startTime,
                            type = viewerType.formattedName
                        )
                        isDismissed.emit(true)
                    }
                }
            )
        }
    }

    fun onDeleteClicked(ctx: Id, viewer: Id) {
        val state = objectState.value.dataViewState() ?: return
        if (state.viewers.size > 1) {
            val targetIdx = state.viewers.indexOfFirst { it.id == viewer }
            val isActive = if (objectSetSession.currentViewerId.value != null) {
                objectSetSession.currentViewerId.value == viewer
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
                dv = state.dataViewBlock.id,
                viewer = viewer,
                nextViewerId = nextViewerId
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
        nextViewerId: Id?
    ) {
        val startTime = System.currentTimeMillis()
        viewModelScope.launch {
            deleteDataViewViewer.async(
                DeleteDataViewViewer.Params(
                    ctx = ctx,
                    viewer = viewer,
                    dataview = dv
                )
            ).fold(
                onFailure = { e ->
                    Timber.e(e, "Error while deleting viewer: $viewer")
                    _toasts.emit("Error while deleting viewer: ${e.localizedMessage}")
                },
                onSuccess = { firstPayload ->
                    dispatcher.send(firstPayload)
                    logEvent(
                        state = objectState.value,
                        analytics = analytics,
                        event = ObjectStateAnalyticsEvent.REMOVE_VIEW,
                        startTime = startTime
                    )
                    if (nextViewerId != null) {
                        objectSetSession.currentViewerId.value = nextViewerId
                        paginator.offset.value = 0
                    }
                    isDismissed.emit(true)
                }
            )
        }
    }

    fun onMenuClicked(viewer: Id) {
        val isDeletionAllowed = isDeletionAllowed(viewer)
        viewModelScope.launch {
            popupCommands.emit(PopupMenuCommand(isDeletionAllowed = isDeletionAllowed))
        }
    }

    private fun isDeletionAllowed(viewerId: Id): Boolean {
        val activeViewerId = objectSetSession.currentViewerId.value
            ?: (objectState.value.dataViewState()?.viewers?.firstOrNull()?.id ?: false)
        return viewerId != activeViewerId
    }

    fun onDoneClicked(ctx: Id, viewerId: Id) {
        Timber.d("onDoneClicked, ctx:[$ctx], viewerId:[$viewerId], viewerType:[${viewerType.formattedName}], viewerName:[$viewerName]")
        if (initialName != viewerName || initialType != viewerType) {
            updateDVViewerType(ctx, viewerId, viewerType, viewerName)
        } else {
            viewModelScope.launch { isDismissed.emit(true) }
        }
    }

    fun onGridClicked() {
        updateViewState(DVViewerType.GRID)
    }

    fun onListClicked() {
        updateViewState(DVViewerType.LIST)
    }

    fun onGalleryClicked() {
        updateViewState(DVViewerType.GALLERY)
    }

    private fun updateDVViewerType(ctx: Id, viewerId: Id, type: DVViewerType, name: String) {
        val startTime = System.currentTimeMillis()
        val state = objectState.value.dataViewState() ?: return
        val viewer = state.viewers.find { it.id == viewerId }
        if (viewer != null) {
            viewModelScope.launch {
                isLoading.value = true
                updateDataViewViewer.async(
                    UpdateDataViewViewer.Params.UpdateView(
                        context = ctx,
                        target = state.dataViewBlock.id,
                        viewer = viewer.copy(type = type, name = name)
                    )
                ).fold(
                    onSuccess = { payload ->
                        dispatcher.send(payload).also {
                            logEvent(
                                state = objectState.value,
                                analytics = analytics,
                                event = ObjectStateAnalyticsEvent.CHANGE_VIEW_TYPE,
                                startTime = startTime,
                                type = type.formattedName
                            )
                            isLoading.value = false
                            isDismissed.emit(true)
                        }
                    },
                    onFailure = {
                        isLoading.value = false
                        Timber.e(it, "Error while updating Viewer type")
                        isDismissed.emit(true)
                    }
                )
            }
        } else {
            sendToast("View not found. Please, try again later.")
        }
    }

    private fun updateViewState(type: DVViewerType) {
        viewerType = type
        viewState.value = when (type) {
            Block.Content.DataView.Viewer.Type.GRID -> ViewState.Grid
            Block.Content.DataView.Viewer.Type.LIST -> ViewState.List
            Block.Content.DataView.Viewer.Type.GALLERY -> ViewState.Gallery
            Block.Content.DataView.Viewer.Type.BOARD -> ViewState.Kanban
        }
    }

    class Factory(
        private val deleteDataViewViewer: DeleteDataViewViewer,
        private val duplicateDataViewViewer: DuplicateDataViewViewer,
        private val updateDataViewViewer: UpdateDataViewViewer,
        private val dispatcher: Dispatcher<Payload>,
        private val objectState: StateFlow<ObjectState>,
        private val objectSetSession: ObjectSetSession,
        private val paginator: ObjectSetPaginator,
        private val analytics: Analytics
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return EditDataViewViewerViewModel(
                deleteDataViewViewer = deleteDataViewViewer,
                duplicateDataViewViewer = duplicateDataViewViewer,
                updateDataViewViewer = updateDataViewViewer,
                dispatcher = dispatcher,
                objectState = objectState,
                objectSetSession = objectSetSession,
                paginator = paginator,
                analytics = analytics
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

    sealed class ViewState {
        object Init : ViewState()
        data class Name(val name: String) : ViewState()
        object Completed : ViewState()
        object Grid : ViewState()
        object Gallery : ViewState()
        object List : ViewState()
        object Kanban : ViewState()
        data class Error(val msg: String) : ViewState()
    }
}