package com.anytypeio.anytype.presentation.sets.viewer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.*
import com.anytypeio.anytype.domain.dataview.interactor.UpdateDataViewViewer
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.sets.ObjectSetSession
import com.anytypeio.anytype.presentation.sets.dataViewState
import com.anytypeio.anytype.presentation.sets.state.ObjectState
import com.anytypeio.anytype.presentation.sets.viewerById
import com.anytypeio.anytype.presentation.util.Dispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch
import timber.log.Timber

class ViewerImagePreviewSelectViewModel(
    private val objectState: StateFlow<ObjectState>,
    private val session: ObjectSetSession,
    private val dispatcher: Dispatcher<Payload>,
    private val updateDataViewViewer: UpdateDataViewViewer,
    private val storeOfRelations: StoreOfRelations
) : BaseViewModel() {

    val views = MutableStateFlow<List<ViewerImagePreviewSelectView>>(emptyList())
    val isDismissed = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            objectState.filterIsInstance<ObjectState.DataView>().collect { state ->
                val viewer = state.viewerById(session.currentViewerId.value) ?: return@collect
                val dv = state.dataViewContent
                val result = mutableListOf<ViewerImagePreviewSelectView>().apply {
                    add(ViewerImagePreviewSelectView.Item.None(isSelected = viewer.coverRelationKey == null))
                    add(ViewerImagePreviewSelectView.Item.Cover(isSelected = viewer.coverRelationKey == Relations.PAGE_COVER))
                }
                val relations = dv.relationLinks
                    .mapNotNull { storeOfRelations.getById(it.key) }
                    .filter { it.isHidden != true && it.format == Relation.Format.FILE }

                if (relations.isNotEmpty()) {
                    result.add(ViewerImagePreviewSelectView.Section.Relations)
                    result.addAll(
                        relations.map { r ->
                            ViewerImagePreviewSelectView.Item.Relation(
                                id = r.key,
                                name = r.name.orEmpty(),
                                isSelected = r.key == viewer.coverRelationKey
                            )
                        }
                    )
                }
                views.value = result
            }
        }
    }

    fun onViewerCoverItemClicked(ctx: Id, item: ViewerImagePreviewSelectView.Item) {
        if (item.isSelected) return
        val state = objectState.value.dataViewState() ?: return
        val viewer = state.viewerById(session.currentViewerId.value) ?: return
        viewModelScope.launch {
            updateDataViewViewer(
                UpdateDataViewViewer.Params.Fields(
                    context = ctx,
                    target = state.dataViewBlock.id,
                    viewer = viewer.copy(
                        coverRelationKey = when (item) {
                            is ViewerImagePreviewSelectView.Item.Relation -> {
                                item.id
                            }
                            is ViewerImagePreviewSelectView.Item.Cover -> {
                                Relations.PAGE_COVER
                            }
                            is ViewerImagePreviewSelectView.Item.None -> {
                                null
                            }
                        }
                    )
                )
            ).process(
                success = {
                    dispatcher.send(it)
                    isDismissed.value = true
                },
                failure = {
                    Timber.e(it, "Error while updating card size for a view")
                }
            )
        }
    }

    class Factory(
        private val objectState: StateFlow<ObjectState>,
        private val session: ObjectSetSession,
        private val dispatcher: Dispatcher<Payload>,
        private val updateDataViewViewer: UpdateDataViewViewer,
        private val storeOfRelations: StoreOfRelations
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ViewerImagePreviewSelectViewModel(
                objectState = objectState,
                session = session,
                dispatcher = dispatcher,
                updateDataViewViewer = updateDataViewViewer,
                storeOfRelations = storeOfRelations
            ) as T
        }
    }
}