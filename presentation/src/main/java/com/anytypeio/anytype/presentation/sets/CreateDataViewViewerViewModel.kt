package com.anytypeio.anytype.presentation.sets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary.PROP_VIEWER_TYPE
import com.anytypeio.anytype.analytics.base.EventsDictionary.SETS_VIEWER_CREATE
import com.anytypeio.anytype.analytics.base.sendEvent
import com.anytypeio.anytype.analytics.props.Props
import com.anytypeio.anytype.core_models.DVViewerType
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.dataview.interactor.AddDataViewViewer
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.util.Dispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class CreateDataViewViewerViewModel(
    private val addDataViewViewer: AddDataViewViewer,
    private val dispatcher: Dispatcher<Payload>,
    private val analytics: Analytics
) : BaseViewModel() {

    val state = MutableStateFlow<ViewState>(ViewState.Init)
    private var dvType = DVViewerType.GRID

    fun onAddViewer(
        name: String,
        ctx: String,
        target: String,
    ) {
        viewModelScope.launch {
            val start = System.currentTimeMillis()
            addDataViewViewer(
                AddDataViewViewer.Params(
                    ctx = ctx,
                    target = target,
                    name = name.ifEmpty { "Untitled" },
                    type = dvType
                )
            ).process(
                failure = { error ->
                    Timber.e(error, ERROR_ADD_NEW_VIEW).also {
                        state.value = ViewState.Error(ERROR_ADD_NEW_VIEW)
                    }
                },
                success = {
                    sendEvent(
                        analytics = analytics,
                        eventName = SETS_VIEWER_CREATE,
                        startTime = start,
                        middleTime = System.currentTimeMillis(),
                        props = Props(mapOf(PROP_VIEWER_TYPE to dvType.name))
                    )
                    dispatcher.send(it).also { state.value = ViewState.Completed }
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
        private val analytics: Analytics
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CreateDataViewViewerViewModel(
                addDataViewViewer = addDataViewViewer,
                dispatcher = dispatcher,
                analytics = analytics
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