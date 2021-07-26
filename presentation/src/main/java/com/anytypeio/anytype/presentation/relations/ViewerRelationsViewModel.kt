package com.anytypeio.anytype.presentation.relations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.DV
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.dataview.interactor.ModifyDataViewViewerRelationOrder
import com.anytypeio.anytype.domain.dataview.interactor.UpdateDataViewViewer
import com.anytypeio.anytype.presentation.common.BaseListViewModel
import com.anytypeio.anytype.presentation.sets.ObjectSet
import com.anytypeio.anytype.presentation.sets.ObjectSetSession
import com.anytypeio.anytype.presentation.sets.filterHiddenRelations
import com.anytypeio.anytype.presentation.sets.model.SimpleRelationView
import com.anytypeio.anytype.presentation.sets.viewerById
import com.anytypeio.anytype.presentation.util.Dispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber

class ViewerRelationsViewModel(
    private val setState: StateFlow<ObjectSet>,
    private val session: ObjectSetSession,
    private val dispatcher: Dispatcher<Payload>,
    private val modifyViewerRelationOrder: ModifyDataViewViewerRelationOrder,
    private val updateDataViewViewer: UpdateDataViewViewer
) : BaseListViewModel<SimpleRelationView>() {

    val screenState = MutableStateFlow(ScreenState.LIST)

    init {
        viewModelScope.launch {
            setState.collect { objectSet ->
                _views.value =
                    objectSet
                        .simpleRelations(session.currentViewerId)
                        .filterHiddenRelations()
            }
        }
    }

    fun onEditButtonClicked() {
        screenState.value = ScreenState.EDIT
    }

    fun onDoneButtonClicked() {
        screenState.value = ScreenState.LIST
    }

    fun onSwitchClicked(ctx: Id, item: SimpleRelationView) {
        proceedWithVisibilityUpdate(ctx, item)
    }

    /**
     * @param [order] order of relation keys
     */
    fun onOrderChanged(ctx: Id, order: List<String>) {
        proceedWithChangeOrderUpdate(ctx, order)
    }

    private fun proceedWithChangeOrderUpdate(ctx: Id, order: List<String>) {
        viewModelScope.launch {
            val viewer = setState.value.viewerById(session.currentViewerId)
            val relations = viewer.viewerRelations.toMutableList()
                .apply { sortBy { order.indexOf(it.key) } }
            modifyViewerRelationOrder(
                params = ModifyDataViewViewerRelationOrder.Params(
                    context = ctx,
                    target = setState.value.dataview.id,
                    viewer = viewer.copy(viewerRelations = relations)
                )
            ).process(
                success = { dispatcher.send(it) },
                failure = {
                    Timber.e(it, DND_ERROR_MSG)
                    _toasts.emit("$DND_ERROR_MSG : ${it.localizedMessage ?: UNKNOWN_ERROR}")
                }
            )
        }
    }

    private fun proceedWithVisibilityUpdate(ctx: Id, item: SimpleRelationView) {
        val viewer = setState.value.viewerById(session.currentViewerId)
        val block = setState.value.blocks.first { it.content is DV }
        val updated = viewer.copy(
            viewerRelations = viewer.viewerRelations.map {
                if (it.key == item.key) {
                    it.copy(isVisible = item.isVisible)
                } else {
                    it
                }
            }
        )
        viewModelScope.launch {
            updateDataViewViewer(
                UpdateDataViewViewer.Params(
                    context = ctx,
                    target = block.id,
                    viewer = updated
                )
            ).process(
                success = { dispatcher.send(it) },
                failure = { Timber.e("Error while updating") }
            )
        }
    }

    class Factory(
        private val state: StateFlow<ObjectSet>,
        private val session: ObjectSetSession,
        private val dispatcher: Dispatcher<Payload>,
        private val modifyViewerRelationOrder: ModifyDataViewViewerRelationOrder,
        private val updateDataViewViewer: UpdateDataViewViewer
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return ViewerRelationsViewModel(
                setState = state,
                session = session,
                dispatcher = dispatcher,
                modifyViewerRelationOrder = modifyViewerRelationOrder,
                updateDataViewViewer = updateDataViewViewer
            ) as T
        }
    }

    companion object {
        private const val DND_ERROR_MSG = "Error while changing relation order"
        private const val UNKNOWN_ERROR = "Error unknown"
    }

    enum class ScreenState { LIST, EDIT }

}