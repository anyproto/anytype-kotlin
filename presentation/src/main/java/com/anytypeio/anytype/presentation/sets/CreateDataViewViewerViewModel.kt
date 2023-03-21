package com.anytypeio.anytype.presentation.sets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.DVViewerType
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.dataview.interactor.AddDataViewViewer
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.extension.ObjectStateAnalyticsEvent
import com.anytypeio.anytype.presentation.extension.logEvent
import com.anytypeio.anytype.presentation.sets.state.ObjectState
import com.anytypeio.anytype.presentation.util.Dispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class CreateDataViewViewerViewModel(
    private val addDataViewViewer: AddDataViewViewer,
    private val dispatcher: Dispatcher<Payload>,
    private val analytics: Analytics,
    private val objectState: MutableStateFlow<ObjectState>
) : BaseViewModel() {

    val state = MutableStateFlow<ViewState>(ViewState.Init)
    private var dvType = DVViewerType.GRID

    fun onAddViewer(
        name: String,
        ctx: String,
        target: String,
    ) {
        val startTime = System.currentTimeMillis()
        viewModelScope.launch {
            addDataViewViewer(
                AddDataViewViewer.Params(
                    ctx = ctx,
                    target = target,
                    name = name,
                    type = dvType
                )
            ).process(
                failure = { error ->
                    Timber.e(error, ERROR_ADD_NEW_VIEW).also {
                        state.value = ViewState.Error(ERROR_ADD_NEW_VIEW)
                    }
                },
                success = {
                    dispatcher.send(it).also {
                        logEvent(
                            state = objectState.value,
                            analytics = analytics,
                            event = ObjectStateAnalyticsEvent.ADD_VIEW,
                            startTime = startTime,
                            type = dvType.formattedName
                        )
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
        private val objectState: MutableStateFlow<ObjectState>
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CreateDataViewViewerViewModel(
                addDataViewViewer = addDataViewViewer,
                dispatcher = dispatcher,
                analytics = analytics,
                objectState = objectState
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