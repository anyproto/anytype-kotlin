package com.anytypeio.anytype.presentation.relations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.relations.AddRelationToObject
import com.anytypeio.anytype.presentation.extension.getPropName
import com.anytypeio.anytype.presentation.extension.sendAnalyticsAddRelationEvent
import com.anytypeio.anytype.presentation.extension.sendAnalyticsSearchQueryEvent
import com.anytypeio.anytype.presentation.relations.model.RelationView
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
    val storeOfRelations: StoreOfRelations
) : RelationAddViewModelBase(
    relationsProvider = relationsProvider,
    storeOfRelations = storeOfRelations
) {

    val commands = MutableSharedFlow<Command>(replay = 0)

    fun onRelationSelected(ctx: Id, relation: RelationView.Existing, screenType: String) {
        viewModelScope.launch {
            addRelationToObject(
                AddRelationToObject.Params(
                    ctx = ctx,
                    relationKey = relation.key
                )
            ).process(
                success = {
                    dispatcher.send(it).also {
                        commands.emit(Command.OnRelationAdd(relation = relation.key))
                        sendAnalyticsAddRelationEvent(
                            analytics = analytics,
                            type = screenType,
                            format = relation.format.getPropName()
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

    override fun sendAnalyticsEvent(length: Int) {
        viewModelScope.sendAnalyticsSearchQueryEvent(
            analytics = analytics,
            route = EventsDictionary.Routes.searchMenu,
            length = length
        )
    }

    class Factory(
        private val storeOfRelations: StoreOfRelations,
        private val addRelationToObject: AddRelationToObject,
        private val dispatcher: Dispatcher<Payload>,
        private val analytics: Analytics,
        private val relationsProvider: ObjectRelationProvider,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return RelationAddToObjectViewModel(
                relationsProvider = relationsProvider,
                addRelationToObject = addRelationToObject,
                storeOfRelations = storeOfRelations,
                dispatcher = dispatcher,
                analytics = analytics
            ) as T
        }
    }

    sealed class Command {
        data class OnRelationAdd(val relation: Id) : Command()
    }
}