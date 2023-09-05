package com.anytypeio.anytype.presentation.relations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_utils.ext.cancel
import com.anytypeio.anytype.domain.dataview.interactor.UpdateDataViewViewer
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.relations.DeleteRelationFromDataView
import com.anytypeio.anytype.presentation.common.BaseListViewModel
import com.anytypeio.anytype.presentation.extension.sendAnalyticsRelationDeleteEvent
import com.anytypeio.anytype.presentation.mapper.toSimpleRelationView
import com.anytypeio.anytype.presentation.sets.ObjectSetSession
import com.anytypeio.anytype.presentation.sets.dataViewState
import com.anytypeio.anytype.presentation.sets.filterHiddenRelations
import com.anytypeio.anytype.presentation.sets.model.SimpleRelationView
import com.anytypeio.anytype.presentation.sets.model.ViewerRelationListView
import com.anytypeio.anytype.presentation.sets.state.ObjectState
import com.anytypeio.anytype.presentation.sets.viewerById
import com.anytypeio.anytype.presentation.util.Dispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch
import timber.log.Timber

class ObjectSetSettingsViewModel(
    private val objectState: StateFlow<ObjectState>,
    private val dispatcher: Dispatcher<Payload>,
    private val updateDataViewViewer: UpdateDataViewViewer,
    private val storeOfRelations: StoreOfRelations,
    private val analytics: Analytics,
    private val deleteRelationFromDataView: DeleteRelationFromDataView
) : BaseListViewModel<ViewerRelationListView>() {

    val screenState = MutableStateFlow(ScreenState.LIST)
    private val jobs = mutableListOf<Job>()

    fun onStart(viewerId: Id) {
        jobs += viewModelScope.launch {
            objectState.filterIsInstance<ObjectState.DataView>().collect { state ->
                Timber.d("New update, viewerId: $viewerId, state: $state")
                val result = mutableListOf<ViewerRelationListView>()
                val viewer = state.viewerById(viewerId) ?: return@collect
                when (viewer.type) {
                    Block.Content.DataView.Viewer.Type.GALLERY -> {
                        result.add(ViewerRelationListView.Section.Settings)
                        when (viewer.cardSize) {
                            Block.Content.DataView.Viewer.Size.SMALL -> {
                                result.add(ViewerRelationListView.Setting.CardSize.Small)
                            }
                            Block.Content.DataView.Viewer.Size.MEDIUM -> {
                                result.add(ViewerRelationListView.Setting.CardSize.Small)
                            }
                            Block.Content.DataView.Viewer.Size.LARGE -> {
                                result.add(ViewerRelationListView.Setting.CardSize.Large)
                            }
                        }

                        val coverRelationKey = viewer.coverRelationKey
                        result.add(
                            when {
                                coverRelationKey.isNullOrBlank() -> ViewerRelationListView.Setting.ImagePreview.None
                                coverRelationKey == Relations.PAGE_COVER -> ViewerRelationListView.Setting.ImagePreview.Cover
                                else -> {
                                    val preview = storeOfRelations.getByKey(coverRelationKey)
                                    if (preview != null) {
                                        ViewerRelationListView.Setting.ImagePreview.Custom(preview.name.orEmpty())
                                    } else {
                                        ViewerRelationListView.Setting.ImagePreview.None
                                    }
                                }
                            }
                        )

                        result.add(ViewerRelationListView.Setting.Toggle.HideIcon(toggled = viewer.hideIcon))
                        result.add(ViewerRelationListView.Setting.Toggle.FitImage(toggled = viewer.coverFit))
                    }
                    Block.Content.DataView.Viewer.Type.GRID -> {
                        result.add(ViewerRelationListView.Section.Settings)
                        result.add(ViewerRelationListView.Setting.Toggle.HideIcon(toggled = viewer.hideIcon))
                    }
                    Block.Content.DataView.Viewer.Type.LIST -> {
                        result.add(ViewerRelationListView.Section.Settings)
                        result.add(ViewerRelationListView.Setting.Toggle.HideIcon(toggled = viewer.hideIcon))
                    }
                    else -> {}
                }

                Timber.d("Relation index: ${state.dataViewContent.relationLinks}")

                val inStore = state.dataViewContent.relationLinks.mapNotNull {
                    storeOfRelations.getByKey(it.key)
                }


                Timber.d("Found in store: ${inStore.size}, available in index: ${state.dataViewContent.relationLinks.size}")

                val relations = viewer.viewerRelations.toSimpleRelationView(inStore)
                    .filterHiddenRelations()
                    .map { view -> ViewerRelationListView.Relation(view) }

                result.add(ViewerRelationListView.Section.Relations)
                result.addAll(relations)

                Timber.d("New views: $result")

                _views.value = result
            }
        }
    }

    fun onStop() {
        jobs.cancel()
    }

    fun onEditButtonClicked() {
        screenState.value = ScreenState.EDIT
    }

    fun onDoneButtonClicked() {
        screenState.value = ScreenState.LIST
    }

    fun onSwitchClicked(ctx: Id, viewerId: Id, item: SimpleRelationView) {
        proceedWithVisibilityUpdate(ctx = ctx, viewerId = viewerId, item)
    }

    fun onSettingToggleChanged(
        ctx: Id,
        viewerId: Id,
        toggle: ViewerRelationListView.Setting.Toggle,
        isChecked: Boolean
    ) {
        val state = objectState.value.dataViewState() ?: return
        val viewer = state.viewerById(viewerId) ?: return
        viewModelScope.launch {
            val updated = when (toggle) {
                is ViewerRelationListView.Setting.Toggle.FitImage -> {
                    viewer.copy(coverFit = isChecked)
                }
                is ViewerRelationListView.Setting.Toggle.HideIcon -> {
                    viewer.copy(hideIcon = isChecked)
                }
            }
            updateDataViewViewer(
                UpdateDataViewViewer.Params.Fields(
                    context = ctx,
                    target = state.dataViewBlock.id,
                    viewer = updated
                )
            ).process(
                success = { dispatcher.send(it) },
                failure = { Timber.w("Error while updating") }
            )
        }
    }

    fun onDeleteClicked(ctx: Id, viewerId: Id, item: SimpleRelationView) {
        proceedWithDeletingRelationFromViewer(ctx = ctx, viewerId = viewerId, relation = item.key)
    }

    private fun proceedWithDeletingRelationFromViewer(ctx: Id, viewerId: Id, relation: Id) {
        val state = objectState.value.dataViewState() ?: return
        val viewer = state.viewerById(viewerId) ?: return
        viewModelScope.launch {
            val params = UpdateDataViewViewer.Params.ViewerRelation.Remove(
                ctx = ctx,
                dv = state.dataViewBlock.id,
                view = viewer.id,
                keys = listOf(relation)
            )
            updateDataViewViewer(params).process(
                failure = { e -> Timber.e(e, "Error while deleting relation from dv") },
                success = { payload ->
                    dispatcher.send(payload)
                    proceedWithUpdatingCurrentViewAfterRelationDeletion(
                        ctx = ctx,
                        relation = relation,
                        viewerId = viewerId
                    )
                    proceedWithDeletingRelationFromDataView(ctx = ctx, relation = relation)
                    sendAnalyticsRelationDeleteEvent(analytics)
                }
            )
        }
    }

    private fun proceedWithDeletingRelationFromDataView(ctx: Id, relation: Id) {
        val state = objectState.value.dataViewState() ?: return
        viewModelScope.launch {
            val params = DeleteRelationFromDataView.Params(
                ctx = ctx,
                dv = state.dataViewBlock.id,
                relation = relation
            )
            deleteRelationFromDataView(params).process(
                failure = { e -> Timber.e(e, "Error while deleting relation from dv") },
                success = { payload -> dispatcher.send(payload) }
            )
        }
    }

    private fun proceedWithUpdatingCurrentViewAfterRelationDeletion(ctx: Id, viewerId: Id, relation: Id) {
        val state = objectState.value.dataViewState() ?: return
        val viewer = state.viewerById(viewerId) ?: return
        viewModelScope.launch {
            val updated = viewer.copy(
                viewerRelations = viewer.viewerRelations.filter { it.key != relation },
                filters = viewer.filters.filter { it.relation != relation },
                sorts = viewer.sorts.filter { it.relationKey != relation }
            )
            val params = UpdateDataViewViewer.Params.Fields(
                context = ctx,
                target = state.dataViewBlock.id,
                viewer = updated
            )
            updateDataViewViewer(params).process(
                success = { dispatcher.send(it) },
                failure = { Timber.e("Error while updating") }
            )
        }
    }

    /**
     * @param [order] order of relation keys
     */
    fun onOrderChanged(ctx: Id, viewerId: Id, order: List<ViewerRelationListView>) {
        proceedWithChangeOrderUpdate(
            ctx = ctx,
            viewerId = viewerId,
            order = order.filterIsInstance<ViewerRelationListView.Relation>().map { it.view.key }
        )
    }

    private fun proceedWithChangeOrderUpdate(ctx: Id, viewerId: Id, order: List<String>) {
        val state = objectState.value.dataViewState() ?: return
        val viewer = state.viewerById(viewerId) ?: return
        viewModelScope.launch {
            val params = UpdateDataViewViewer.Params.ViewerRelation.Sort(
                ctx = ctx,
                dv = state.dataViewBlock.id,
                view = viewer.id,
                keys = order
            )
            updateDataViewViewer(params).process(
                success = { dispatcher.send(it) },
                failure = {
                    Timber.e(it, DND_ERROR_MSG)
                    _toasts.emit("$DND_ERROR_MSG : ${it.localizedMessage ?: UNKNOWN_ERROR}")
                }
            )
        }
    }

    private fun proceedWithVisibilityUpdate(ctx: Id, viewerId: Id, item: SimpleRelationView) {
        val state = objectState.value.dataViewState() ?: return
        val viewer = state.viewerById(viewerId) ?: return
        val viewerRelation = viewer.viewerRelations
            .find { it.key == item.key }
            ?.copy(isVisible = item.isVisible)
            ?: return
        val params = UpdateDataViewViewer.Params.ViewerRelation.Replace(
            ctx = ctx,
            dv = state.dataViewBlock.id,
            view = viewer.id,
            key = item.key,
            relation = viewerRelation
        )
        viewModelScope.launch {
            updateDataViewViewer(params).process(
                success = { dispatcher.send(it) },
                failure = { Timber.e("Error while updating") }
            )
        }
    }

    class Factory(
        private val objectState: StateFlow<ObjectState>,
        private val dispatcher: Dispatcher<Payload>,
        private val updateDataViewViewer: UpdateDataViewViewer,
        private val store: StoreOfRelations,
        private val analytics: Analytics,
        private val deleteRelationFromDataView: DeleteRelationFromDataView
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ObjectSetSettingsViewModel(
                objectState = objectState,
                dispatcher = dispatcher,
                updateDataViewViewer = updateDataViewViewer,
                storeOfRelations = store,
                analytics = analytics,
                deleteRelationFromDataView = deleteRelationFromDataView
            ) as T
        }
    }

    companion object {
        private const val DND_ERROR_MSG = "Error while changing relation order"
        private const val UNKNOWN_ERROR = "Error unknown"
    }

    enum class ScreenState { LIST, EDIT }
}