package com.anytypeio.anytype.presentation.sets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.DVViewerType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.dataview.interactor.AddDataViewViewer
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.extension.sendAnalyticsAddViewEvent
import com.anytypeio.anytype.presentation.util.Dispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class CreateDataViewViewerViewModel(
    private val addDataViewViewer: AddDataViewViewer,
    private val dispatcher: Dispatcher<Payload>,
    private val analytics: Analytics,
    private val objectSetState: StateFlow<ObjectSet>
) : BaseViewModel() {

    val state = MutableStateFlow<ViewState>(ViewState.Init)
    private var dvType = DVViewerType.GRID

    fun onAddViewer(
        name: String,
        ctx: String,
        target: String,
    ) {
        viewModelScope.launch {
            addDataViewViewer(
                AddDataViewViewer.Params(
                    ctx = ctx,
                    target = target,
                    name = name.ifEmpty { "Untitled" },
                    type = dvType,
                    source = buildList {
                        val detail = objectSetState.value.details[ctx]
                        val wrapper = ObjectWrapper.Basic(detail?.map ?: emptyMap())
                        addAll(wrapper.setOf)
                    }
                )
            ).process(
                failure = { error ->
                    Timber.e(error, ERROR_ADD_NEW_VIEW).also {
                        state.value = ViewState.Error(ERROR_ADD_NEW_VIEW)
                    }
                },
                success = {
                    dispatcher.send(it).also {
                        sendAnalyticsAddViewEvent(analytics, dvType.name)
                        state.value = ViewState.Completed
                    }
                }
            )
        }
    }

    fun onGridClicked() {
        dvType = DVViewerType.GRID
        state.value = ViewState.Grid
    }

    fun onListClicked() {
        dvType = DVViewerType.LIST
        state.value = ViewState.List
    }

    fun onGalleryClicked() {
        dvType = DVViewerType.GALLERY
        state.value = ViewState.Gallery
    }


    class Factory(
        private val addDataViewViewer: AddDataViewViewer,
        private val dispatcher: Dispatcher<Payload>,
        private val analytics: Analytics,
        private val objectSetState: StateFlow<ObjectSet>
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CreateDataViewViewerViewModel(
                addDataViewViewer = addDataViewViewer,
                dispatcher = dispatcher,
                analytics = analytics,
                objectSetState = objectSetState
            ) as T
        }
    }

    companion object {
        const val ERROR_ADD_NEW_VIEW = "Error while creating a new data view view"
    }

    sealed class ViewState {
        object Init : ViewState()
        object Completed : ViewState()
        object Grid : ViewState()
        object Gallery : ViewState()
        object List : ViewState()
        data class Error(val msg: String) : ViewState()
    }
}