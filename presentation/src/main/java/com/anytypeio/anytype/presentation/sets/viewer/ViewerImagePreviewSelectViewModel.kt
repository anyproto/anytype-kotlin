package com.anytypeio.anytype.presentation.sets.viewer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.*
import com.anytypeio.anytype.core_models.ext.content
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

class ViewerImagePreviewSelectViewModel(
    private val objectSetState: StateFlow<ObjectSet>,
    private val session: ObjectSetSession,
    private val dispatcher: Dispatcher<Payload>,
    private val updateDataViewViewer: UpdateDataViewViewer
) : BaseViewModel() {

    val views = MutableStateFlow<List<ViewerImagePreviewSelectView>>(emptyList())
    val isDismissed = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            objectSetState.filter { it.isInitialized }.collect { objectSet ->
                val viewer = objectSet.viewerById(session.currentViewerId.value)
                val dv = objectSet.dataview.content<DV>()
                val result = mutableListOf<ViewerImagePreviewSelectView>().apply {
                    add(ViewerImagePreviewSelectView.Item.None(isSelected = viewer.coverRelationKey == null))
                    add(ViewerImagePreviewSelectView.Item.Cover(isSelected = viewer.coverRelationKey == Relations.PAGE_COVER))
                }
                val relations = dv.relations.filter { !it.isHidden && it.format == Relation.Format.FILE }
                if (relations.isNotEmpty()) {
                    result.add(ViewerImagePreviewSelectView.Section.Relations)
                    result.addAll(
                        relations.map { r ->
                            ViewerImagePreviewSelectView.Item.Relation(
                                id = r.key,
                                name = r.name,
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
        viewModelScope.launch {
            val currObjectSetState = objectSetState.value
            if (currObjectSetState.isInitialized) {
                updateDataViewViewer(
                    UpdateDataViewViewer.Params(
                        context = ctx,
                        target = currObjectSetState.dataview.id,
                        viewer = currObjectSetState.viewerById(session.currentViewerId.value).copy(
                            coverRelationKey = when(item) {
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
    }

    class Factory(
        private val objectSetState: StateFlow<ObjectSet>,
        private val session: ObjectSetSession,
        private val dispatcher: Dispatcher<Payload>,
        private val updateDataViewViewer: UpdateDataViewViewer
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ViewerImagePreviewSelectViewModel(
                objectSetState = objectSetState,
                session = session,
                dispatcher = dispatcher,
                updateDataViewViewer = updateDataViewViewer
            ) as T
        }
    }
}