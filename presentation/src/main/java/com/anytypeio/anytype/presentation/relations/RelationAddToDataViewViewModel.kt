package com.anytypeio.anytype.presentation.relations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.DV
import com.anytypeio.anytype.core_models.DVViewerRelation
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.dataview.interactor.AddRelationToDataView
import com.anytypeio.anytype.domain.dataview.interactor.UpdateDataViewViewer
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.presentation.extension.getPropName
import com.anytypeio.anytype.presentation.extension.sendAnalyticsAddRelationEvent
import com.anytypeio.anytype.presentation.relations.model.RelationView
import com.anytypeio.anytype.presentation.relations.providers.ObjectRelationProvider
import com.anytypeio.anytype.presentation.sets.ObjectSet
import com.anytypeio.anytype.presentation.sets.ObjectSetSession
import com.anytypeio.anytype.presentation.util.Dispatcher
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class RelationAddToDataViewViewModel(
    storeOfRelations: StoreOfRelations,
    relationsProvider: ObjectRelationProvider,
    private val state: StateFlow<ObjectSet>,
    private val session: ObjectSetSession,
    private val updateDataViewViewer: UpdateDataViewViewer,
    private val addRelationToDataView: AddRelationToDataView,
    private val dispatcher: Dispatcher<Payload>,
    private val analytics: Analytics
) : RelationAddViewModelBase(
    storeOfRelations = storeOfRelations,
    relationsProvider = relationsProvider
) {

    fun onRelationSelected(ctx: Id, relation: RelationView.Existing, dv: Id, screenType: String) {
        viewModelScope.launch {
            addRelationToDataView(
                AddRelationToDataView.Params(
                    ctx = ctx,
                    relation = relation.key,
                    dv = dv
                )
            ).process(
                success = {
                    dispatcher.send(it).also {
                        proceedWithAddingNewRelationToCurrentViewer(
                            ctx = ctx,
                            relation = relation.key
                        )
                    }
                    sendAnalyticsAddRelationEvent(
                        analytics = analytics,
                        type = screenType,
                        format = relation.format.getPropName()
                    )
                },
                failure = {
                    Timber.e(it, ERROR_MESSAGE)
                    _toasts.emit("$ERROR_MESSAGE: ${it.localizedMessage}")
                }
            )
        }
    }

    override fun sendAnalyticsEvent(length: Int) {}

    private suspend fun proceedWithAddingNewRelationToCurrentViewer(ctx: Id, relation: Id) {
        val state = state.value
        val block = state.dataview
        val dv = block.content as DV
        val viewer = dv.viewers.find { it.id == session.currentViewerId.value } ?: dv.viewers.first()

        updateDataViewViewer(
            UpdateDataViewViewer.Params(
                context = ctx,
                target = block.id,
                viewer = viewer.copy(
                    viewerRelations = viewer.viewerRelations + listOf(
                        DVViewerRelation(
                            key = relation,
                            isVisible = true
                        )
                    )
                )
            )
        ).process(
            success = { dispatcher.send(it).also { isDismissed.value = true } },
            failure = { Timber.e(it, "Error while updating data view's viewer") }
        )
    }

    class Factory(
        private val state: StateFlow<ObjectSet>,
        private val session: ObjectSetSession,
        private val updateDataViewViewer: UpdateDataViewViewer,
        private val storeOfRelations: StoreOfRelations,
        private val addRelationToDataView: AddRelationToDataView,
        private val dispatcher: Dispatcher<Payload>,
        private val analytics: Analytics,
        private val relationsProvider: ObjectRelationProvider,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return RelationAddToDataViewViewModel(
                addRelationToDataView = addRelationToDataView,
                storeOfRelations = storeOfRelations,
                dispatcher = dispatcher,
                session = session,
                updateDataViewViewer = updateDataViewViewer,
                state = state,
                analytics = analytics,
                relationsProvider = relationsProvider,
            ) as T
        }
    }
}