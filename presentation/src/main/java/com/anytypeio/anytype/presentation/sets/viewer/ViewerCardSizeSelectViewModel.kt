package com.anytypeio.anytype.presentation.sets.viewer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.DVViewerCardSize
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.dataview.interactor.UpdateDataViewViewer
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.sets.dataViewState
import com.anytypeio.anytype.presentation.sets.state.ObjectState
import com.anytypeio.anytype.presentation.sets.viewerByIdOrFirst
import com.anytypeio.anytype.presentation.util.Dispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch
import timber.log.Timber

class ViewerCardSizeSelectViewModel(
    private val objectState: StateFlow<ObjectState>,
    private val dispatcher: Dispatcher<Payload>,
    private val updateDataViewViewer: UpdateDataViewViewer
) : BaseViewModel() {

    val viewState = MutableStateFlow(STATE_IDLE)

    fun onStart(viewerId: Id) {
        viewModelScope.launch {
            objectState.filterIsInstance<ObjectState.DataView>().collect {
                val viewer = it.viewerByIdOrFirst(viewerId) ?: return@collect
                viewState.value = when (viewer.cardSize) {
                    Block.Content.DataView.Viewer.Size.SMALL -> STATE_SMALL_CARD_SELECTED
                    Block.Content.DataView.Viewer.Size.MEDIUM -> STATE_SMALL_CARD_SELECTED
                    Block.Content.DataView.Viewer.Size.LARGE -> STATE_LARGE_CARD_SELECTED
                }
            }
        }
    }

    fun onSmallCardClicked(ctx: Id, viewerId: Id) {
        if (viewState.value == STATE_SMALL_CARD_SELECTED) {
            viewState.value = STATE_DISMISSED
        } else {
            proceedWithUpdatingCardSize(
                ctx = ctx,
                size = DVViewerCardSize.SMALL,
                viewerId = viewerId
            )
        }
    }

    fun onLargeCardClicked(ctx: Id, viewerId: Id) {
        if (viewState.value == STATE_LARGE_CARD_SELECTED) {
            viewState.value = STATE_DISMISSED
        } else {
            proceedWithUpdatingCardSize(
                ctx = ctx,
                viewerId = viewerId,
                size = DVViewerCardSize.LARGE
            )
        }
    }

    private fun proceedWithUpdatingCardSize(ctx: Id, viewerId: Id, size: DVViewerCardSize) {
        val state = objectState.value.dataViewState() ?: return
        val viewer = state.viewerByIdOrFirst(viewerId) ?: return
        viewModelScope.launch {
            updateDataViewViewer.async(
                UpdateDataViewViewer.Params.UpdateView(
                    context = ctx,
                    target = state.dataViewBlock.id,
                    viewer = viewer.copy(cardSize = size)
                )
            ).fold(
                onSuccess = {
                    dispatcher.send(it)
                    viewState.value = STATE_DISMISSED
                },
                onFailure = {
                    Timber.e(it, "Error while updating card size for a view")
                }
            )
        }
    }

    class Factory(
        private val objectState: StateFlow<ObjectState>,
        private val dispatcher: Dispatcher<Payload>,
        private val updateDataViewViewer: UpdateDataViewViewer
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ViewerCardSizeSelectViewModel(
                objectState = objectState,
                dispatcher = dispatcher,
                updateDataViewViewer = updateDataViewViewer
            ) as T
        }
    }

    companion object {
        const val STATE_IDLE = 0
        const val STATE_SMALL_CARD_SELECTED = 1
        const val STATE_LARGE_CARD_SELECTED = 2
        const val STATE_DISMISSED = 3
    }
}