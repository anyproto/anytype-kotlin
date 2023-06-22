package com.anytypeio.anytype.presentation.sets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.DVViewerType
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.dataview.interactor.DeleteDataViewViewer
import com.anytypeio.anytype.domain.dataview.interactor.SetDataViewViewerPosition
import com.anytypeio.anytype.presentation.common.BaseListViewModel
import com.anytypeio.anytype.presentation.extension.ObjectStateAnalyticsEvent
import com.anytypeio.anytype.presentation.extension.logEvent
import com.anytypeio.anytype.presentation.sets.ManageViewerViewModel.ViewerView
import com.anytypeio.anytype.presentation.sets.state.ObjectState
import com.anytypeio.anytype.presentation.util.Dispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch
import timber.log.Timber

class ManageViewerViewModel(
    private val objectState: StateFlow<ObjectState>,
    private val session: ObjectSetSession,
    private val dispatcher: Dispatcher<Payload>,
    private val analytics: Analytics,
    private val deleteDataViewViewer: DeleteDataViewViewer,
    private val setDataViewViewerPosition: SetDataViewViewerPosition
) : BaseListViewModel<ViewerView>() {

    val isEditEnabled = MutableStateFlow(false)
    val isDismissed = MutableSharedFlow<Boolean>(replay = 0)
    val commands = MutableSharedFlow<Command>(replay = 0)

    init {
        viewModelScope.launch {
            objectState.filterIsInstance<ObjectState.DataView>().collect { s ->
                _views.value = s.viewers.mapIndexed { index, viewer ->
                    ViewerView(
                        id = viewer.id,
                        name = viewer.name,
                        type = viewer.type,
                        isActive = if (session.currentViewerId.value != null)
                            viewer.id == session.currentViewerId.value
                        else
                            index == 0,
                        showActionMenu = isEditEnabled.value
                    )
                }
            }
        }
    }

    fun onOrderChanged(
        ctx: Id,
        dv: Id,
        newPosition: Int,
        newOrder: List<Id>,
    ) {
        val currentOrder = views.value.map { it.id }
        if (newOrder == currentOrder) return
        val viewer = newOrder.getOrNull(newPosition)
        if (viewer != null) {
            viewModelScope.launch {
                // Workaround for preserving the previously first view as the active view
                val startTime = System.currentTimeMillis()
                if (newPosition == 0 && session.currentViewerId.value.isNullOrEmpty()) {
                    session.currentViewerId.value = views.value.firstOrNull()?.id
                }
                setDataViewViewerPosition.stream(
                    params = SetDataViewViewerPosition.Params(
                        ctx = ctx,
                        dv = dv,
                        viewer = viewer,
                        pos = newPosition
                    )
                ).collect { result ->
                    logEvent(
                        state = objectState.value,
                        analytics = analytics,
                        event = ObjectStateAnalyticsEvent.REPOSITION_VIEW,
                        startTime = startTime,
                        type = views.value.find { it.id  == viewer }?.type?.formattedName
                    )
                    result.fold(
                        onSuccess = { dispatcher.send(it) },
                        onFailure = { Timber.e(it, "Error while changing view order") }
                    )
                }
            }
        } else {
            sendToast("Something went wrong. Please, try again later.")
        }
    }

    fun onViewerActionClicked(view: ViewerView) {
        viewModelScope.launch {
            commands.emit(Command.OpenEditScreen(view.id, view.name))
        }
    }

    fun onDeleteView(
        ctx: Id,
        dv: Id,
        view: ViewerView
    ) {
        viewModelScope.launch {
            deleteDataViewViewer(
                DeleteDataViewViewer.Params(
                    ctx = ctx,
                    dataview = dv,
                    viewer = view.id
                )
            ).process(
                failure = { Timber.e(it, "Error while deleting view") },
                success = { dispatcher.send(it) }
            )
        }
    }

    fun onButtonEditClicked() {
        isEditEnabled.value = !isEditEnabled.value
        _views.value = views.value.map { view ->
            view.copy(showActionMenu = isEditEnabled.value)
        }
    }

    fun onButtonAddClicked() {
        viewModelScope.launch {
            commands.emit(Command.OpenCreateScreen)
        }
    }

    fun onViewerClicked(
        ctx: Id,
        view: ViewerView
    ) {
        val startTime = System.currentTimeMillis()
        if (!isEditEnabled.value)
            viewModelScope.launch {
                session.currentViewerId.value = view.id
                logEvent(
                    state = objectState.value,
                    analytics = analytics,
                    event = ObjectStateAnalyticsEvent.SWITCH_VIEW,
                    startTime = startTime,
                    type = view.type.formattedName
                )
                isDismissed.emit(true)
            }
        else
            Timber.d("Skipping click in edit mode")
    }

    sealed class Command {
        data class OpenEditScreen(val id: Id, val name: String) : Command()
        object OpenCreateScreen : Command()
    }

    class Factory(
        private val objectState: StateFlow<ObjectState>,
        private val session: ObjectSetSession,
        private val dispatcher: Dispatcher<Payload>,
        private val analytics: Analytics,
        private val deleteDataViewViewer: DeleteDataViewViewer,
        private val setDataViewViewerPosition: SetDataViewViewerPosition
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ManageViewerViewModel(
                objectState = objectState,
                session = session,
                dispatcher = dispatcher,
                analytics = analytics,
                deleteDataViewViewer = deleteDataViewViewer,
                setDataViewViewerPosition = setDataViewViewerPosition
            ) as T
        }
    }

    data class ViewerView(
        val id: Id,
        val name: String,
        val type: DVViewerType,
        val isActive: Boolean,
        val showActionMenu: Boolean = false,
    )
}