package com.anytypeio.anytype.presentation.sets.viewer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.DVViewerCardSize
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.dataview.interactor.UpdateDataViewViewer
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.sets.ObjectSet
import com.anytypeio.anytype.presentation.sets.ObjectSetSession
import com.anytypeio.anytype.presentation.sets.viewerById
import com.anytypeio.anytype.presentation.util.Dispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import timber.log.Timber

class ViewerCardSizeSelectViewModel(
    private val objectSetState: StateFlow<ObjectSet>,
    private val session: ObjectSetSession,
    private val dispatcher: Dispatcher<Payload>,
    private val updateDataViewViewer: UpdateDataViewViewer
) : BaseViewModel() {

    val state = MutableStateFlow(STATE_IDLE)

    init {
        viewModelScope.launch {
            objectSetState.filter { it.isInitialized }.collect {
                val viewer = it.viewerById(session.currentViewerId.value)
                when(viewer.cardSize) {
                    Block.Content.DataView.Viewer.Size.SMALL -> {
                        state.value = STATE_SMALL_CARD_SELECTED
                    }
                    Block.Content.DataView.Viewer.Size.MEDIUM -> {
                        state.value = STATE_LARGE_CARD_SELECTED
                    }
                    Block.Content.DataView.Viewer.Size.LARGE -> {
                        state.value = STATE_LARGE_CARD_SELECTED
                    }
                }
            }
        }
    }

    fun onSmallCardClicked(ctx: Id) {
        if (state.value == STATE_SMALL_CARD_SELECTED) {
            state.value = STATE_DISMISSED
        } else {
            proceedWithUpdatingCardSize(
                ctx = ctx,
                size = DVViewerCardSize.SMALL
            )
        }
    }

    fun onLargeCardClicked(ctx: Id) {
        if (state.value == STATE_LARGE_CARD_SELECTED) {
            state.value = STATE_DISMISSED
        } else {
            proceedWithUpdatingCardSize(
                ctx = ctx,
                size = DVViewerCardSize.LARGE
            )
        }
    }

    private fun proceedWithUpdatingCardSize(ctx: Id, size: DVViewerCardSize) {
        viewModelScope.launch {
            val currObjectSetState = objectSetState.value
            if (currObjectSetState.isInitialized) {
                updateDataViewViewer(
                    UpdateDataViewViewer.Params(
                        context = ctx,
                        target = currObjectSetState.dataview.id,
                        viewer = currObjectSetState.viewerById(session.currentViewerId.value).copy(
                            cardSize = size
                        )
                    )
                ).process(
                    success = {
                        dispatcher.send(it)
                        state.value = STATE_DISMISSED
                    },
                    failure = {
                        Timber.e(it, "Error while updating card size for a view")
                    }
                )
            }
        }
    }

    class Factory(
        private val objectSetState: StateFlow<ObjectSet>,
        private val session: ObjectSetSession,
        private val dispatcher: Dispatcher<Payload>,
        private val updateDataViewViewer: UpdateDataViewViewer
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ViewerCardSizeSelectViewModel(
                objectSetState = objectSetState,
                session = session,
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