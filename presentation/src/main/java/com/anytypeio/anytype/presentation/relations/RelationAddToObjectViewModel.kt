package com.anytypeio.anytype.presentation.relations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.relations.AddRelationToObject
import com.anytypeio.anytype.domain.relations.GetRelations
import com.anytypeio.anytype.domain.workspace.AddObjectToWorkspace
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.domain.workspace.WorkspaceManager
import com.anytypeio.anytype.presentation.extension.getPropName
import com.anytypeio.anytype.presentation.extension.sendAnalyticsAddRelationEvent
import com.anytypeio.anytype.presentation.relations.providers.ObjectRelationProvider
import com.anytypeio.anytype.presentation.util.Dispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class RelationAddToObjectViewModel(
    relationsProvider: ObjectRelationProvider,
    private val addRelationToObject: AddRelationToObject,
    private val dispatcher: Dispatcher<Payload>,
    private val analytics: Analytics,
    val storeOfRelations: StoreOfRelations,
    appCoroutineDispatchers: AppCoroutineDispatchers,
    getRelations: GetRelations,
    addObjectToWorkspace: AddObjectToWorkspace,
    workspaceManager: WorkspaceManager,
    spaceManager: SpaceManager
) : RelationAddViewModelBase(
    relationsProvider = relationsProvider,
    appCoroutineDispatchers = appCoroutineDispatchers,
    getRelations = getRelations,
    addObjectToWorkspace = addObjectToWorkspace,
    workspaceManager = workspaceManager,
    spaceManager = spaceManager
) {

    val commands = MutableSharedFlow<Command>(replay = 0)

    fun onRelationSelected(
        ctx: Id,
        relation: Key,
        format: RelationFormat,
        screenType: String
    ) {
        viewModelScope.launch {
            addRelationToObject(
                AddRelationToObject.Params(
                    ctx = ctx,
                    relationKey = relation
                )
            ).process(
                success = {
                    dispatcher.send(it).also {
                        commands.emit(Command.OnRelationAdd(relation = relation))
                        sendAnalyticsAddRelationEvent(
                            analytics = analytics,
                            type = screenType,
                            format = format.getPropName()
                        )
                        isDismissed.value = true
                    }
                },
                failure = {
                    Timber.e(it, ERROR_MESSAGE)
                    _toasts.emit("$ERROR_MESSAGE: ${it.localizedMessage}")
                }
            )
        }
    }

    class Factory(
        private val storeOfRelations: StoreOfRelations,
        private val addRelationToObject: AddRelationToObject,
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
            return RelationAddToObjectViewModel(
                relationsProvider = relationsProvider,
                addRelationToObject = addRelationToObject,
                storeOfRelations = storeOfRelations,
                dispatcher = dispatcher,
                analytics = analytics,
                appCoroutineDispatchers = appCoroutineDispatchers,
                getRelations = getRelations,
                addObjectToWorkspace = addObjectToWorkspace,
                workspaceManager = workspaceManager,
                spaceManager = spaceManager
            ) as T
        }
    }

    sealed class Command {
        data class OnRelationAdd(val relation: Id) : Command()
    }
}