package com.anytypeio.anytype.presentation.sets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.DVViewerType
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.dataview.interactor.AddDataViewViewer
import com.anytypeio.anytype.presentation.util.Dispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class CreateDataViewViewerViewModel(
    private val addDataViewViewer: AddDataViewViewer,
    private val dispatcher: Dispatcher<Payload>
) : ViewModel() {

    val state = MutableStateFlow(STATE_IDLE)

    private val _toasts = MutableSharedFlow<String>()
    val toasts: SharedFlow<String> = _toasts

    fun onAddViewer(
        name: String,
        ctx: String,
        target: String
    ) {
        state.value = STATE_LOADING
        viewModelScope.launch {
            addDataViewViewer(
                AddDataViewViewer.Params(
                    ctx = ctx,
                    target = target,
                    name = name.ifEmpty { "Untitled" },
                    type = DVViewerType.GRID
                )
            ).process(
                failure = {
                    Timber.e(it, ERROR_ADD_NEW_VIEW).also {
                        _toasts.emit(ERROR_ADD_NEW_VIEW)
                        state.value = STATE_ERROR
                    }
                },
                success = { dispatcher.send(it).also { state.value = STATE_COMPLETED } }
            )
        }
    }


    class Factory(
        private val addDataViewViewer: AddDataViewViewer,
        private val dispatcher: Dispatcher<Payload>
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return CreateDataViewViewerViewModel(
                addDataViewViewer = addDataViewViewer,
                dispatcher = dispatcher
            ) as T
        }
    }

    companion object {
        const val ERROR_ADD_NEW_VIEW = "Error while creating a new data view view"

        const val STATE_IDLE = 0
        const val STATE_LOADING = 1
        const val STATE_COMPLETED = 2
        const val STATE_ERROR = 3
    }
}