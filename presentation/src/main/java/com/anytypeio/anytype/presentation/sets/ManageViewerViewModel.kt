package com.anytypeio.anytype.presentation.sets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.DVViewerType
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.dataview.interactor.SetActiveViewer
import com.anytypeio.anytype.presentation.common.BaseListViewModel
import com.anytypeio.anytype.presentation.sets.ManageViewerViewModel.ViewerView
import com.anytypeio.anytype.presentation.util.Dispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber

class ManageViewerViewModel(
    private val state: StateFlow<ObjectSet>,
    private val session: ObjectSetSession,
    private val dispatcher: Dispatcher<Payload>,
    private val setActiveViewer: SetActiveViewer
) : BaseListViewModel<ViewerView>() {

    val isEditEnabled = MutableStateFlow(false)
    val isDismissed = MutableSharedFlow<Boolean>(replay = 0)
    val commands = MutableSharedFlow<Command>(replay = 0)

    init {
        viewModelScope.launch {
            state.collect { s ->
                _views.value = s.viewers.mapIndexed { index, viewer ->
                    ViewerView(
                        id = viewer.id,
                        name = viewer.name,
                        type = viewer.type,
                        isActive = if (session.currentViewerId != null)
                            viewer.id == session.currentViewerId
                        else
                            index == 0,
                        showActionMenu = isEditEnabled.value
                    )
                }
            }
        }
    }

    fun onOrderChanged(ctx: Id, order: List<String>) {

    }

    fun onViewerActionClicked(view: ViewerView) {
        viewModelScope.launch {
            commands.emit(Command.OpenEditScreen(view.id, view.name))
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
        if (!isEditEnabled.value)
            viewModelScope.launch {
                session.currentViewerId = view.id
                setActiveViewer(
                    SetActiveViewer.Params(
                        context = ctx,
                        block = state.value.dataview.id,
                        view = view.id
                    )
                ).process(
                    failure = { Timber.e(it, "Error while setting active viewer") },
                    success = { dispatcher.send(it).also { isDismissed.emit(true) } }
                )
            }
        else
            Timber.d("Skipping click in edit mode")
    }

    sealed class Command {
        data class OpenEditScreen(val id: Id, val name: String) : Command()
        object OpenCreateScreen : Command()
    }

    class Factory(
        private val state: StateFlow<ObjectSet>,
        private val session: ObjectSetSession,
        private val dispatcher: Dispatcher<Payload>,
        private val setActiveViewer: SetActiveViewer
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return ManageViewerViewModel(
                state = state,
                session = session,
                setActiveViewer = setActiveViewer,
                dispatcher = dispatcher
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