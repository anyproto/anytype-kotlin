package com.anytypeio.anytype.presentation.relations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.DVViewerRelation
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.dataview.interactor.AddRelationToDataView
import com.anytypeio.anytype.domain.dataview.interactor.UpdateDataViewViewer
import com.anytypeio.anytype.domain.relations.GetRelations
import com.anytypeio.anytype.domain.workspace.AddObjectToWorkspace
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.domain.workspace.WorkspaceManager
import com.anytypeio.anytype.presentation.extension.getPropName
import com.anytypeio.anytype.presentation.extension.sendAnalyticsAddRelationEvent
import com.anytypeio.anytype.presentation.relations.providers.ObjectRelationProvider
import com.anytypeio.anytype.presentation.sets.ObjectSetSession
import com.anytypeio.anytype.presentation.sets.dataViewState
import com.anytypeio.anytype.presentation.sets.state.ObjectState
import com.anytypeio.anytype.presentation.sets.viewerById
import com.anytypeio.anytype.presentation.util.Dispatcher
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class RelationAddToDataViewViewModel(
    relationsProvider: ObjectRelationProvider,
    private val objectState: StateFlow<ObjectState>,
    private val session: ObjectSetSession,
    private val updateDataViewViewer: UpdateDataViewViewer,
    private val addRelationToDataView: AddRelationToDataView,
    private val getRelations: GetRelations,
    private val dispatcher: Dispatcher<Payload>,
    private val analytics: Analytics,
    private val addObjectToWorkspace: AddObjectToWorkspace,
    private val appCoroutineDispatchers: AppCoroutineDispatchers,
    private val workspaceManager: WorkspaceManager,
    spaceManager: SpaceManager
) : RelationAddViewModelBase(
    relationsProvider = relationsProvider,
    appCoroutineDispatchers = appCoroutineDispatchers,
    getRelations = getRelations,
    addObjectToWorkspace = addObjectToWorkspace,
    workspaceManager = workspaceManager,
    spaceManager = spaceManager
) {

    fun onRelationSelected(
        ctx: Id,
        relation: Key,
        format: RelationFormat,
        dv: Id,
        screenType: String
    ) {
        viewModelScope.launch {
            addRelationToDataView(
                AddRelationToDataView.Params(
                    ctx = ctx,
                    relation = relation,
                    dv = dv
                )
            ).process(
                success = {
                    dispatcher.send(it).also {
                        proceedWithAddingNewRelationToCurrentViewer(
                            ctx = ctx,
                            relation = relation
                        )
                    }
                    sendAnalyticsAddRelationEvent(
                        analytics = analytics,
                        type = screenType,
                        format = format.getPropName()
                    )
                },
                failure = {
                    Timber.e(it, ERROR_MESSAGE)
                    _toasts.emit("$ERROR_MESSAGE: ${it.localizedMessage}")
                }
            )
        }
    }
    
    private suspend fun proceedWithAddingNewRelationToCurrentViewer(ctx: Id, relation: Id) {
        val state = objectState.value.dataViewState() ?: return
        val viewer = state.viewerById(session.currentViewerId.value) ?: return

        updateDataViewViewer(
            UpdateDataViewViewer.Params.ViewerRelation.Add(
                ctx = ctx,
                dv = state.dataViewBlock.id,
                view = viewer.id,
                relation = DVViewerRelation(
                    key = relation,
                    isVisible = true
                )
            )
        ).process(
            success = { dispatcher.send(it).also { isDismissed.value = true } },
            failure = { Timber.e(it, "Error while updating data view's viewer") }
        )
    }

    class Factory(
        private val state: StateFlow<ObjectState>,
        private val session: ObjectSetSession,
        private val updateDataViewViewer: UpdateDataViewViewer,
        private val addRelationToDataView: AddRelationToDataView,
        private val dispatcher: Dispatcher<Payload>,
        private val analytics: Analytics,
        private val relationsProvider: ObjectRelationProvider,
        private val appCoroutineDispatchers: AppCoroutineDispatchers,
        private val getRelations: GetRelations,
        private val addObjectToWorkspace: AddObjectToWorkspace,
        private val workspaceManager: WorkspaceManager,
        private val spaceManager: SpaceManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return RelationAddToDataViewViewModel(
                addRelationToDataView = addRelationToDataView,
                dispatcher = dispatcher,
                session = session,
                updateDataViewViewer = updateDataViewViewer,
                objectState = state,
                analytics = analytics,
                relationsProvider = relationsProvider,
                appCoroutineDispatchers = appCoroutineDispatchers,
                getRelations = getRelations,
                addObjectToWorkspace = addObjectToWorkspace,
                workspaceManager = workspaceManager,
                spaceManager = spaceManager
            ) as T
        }
    }
}