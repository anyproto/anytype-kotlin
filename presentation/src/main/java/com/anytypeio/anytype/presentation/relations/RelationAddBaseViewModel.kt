package com.anytypeio.anytype.presentation.relations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.dataview.interactor.ObjectRelationList
import com.anytypeio.anytype.domain.relations.AddRelationToObject
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.relations.model.RelationView
import com.anytypeio.anytype.presentation.util.Dispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Base view model for adding a relation either to an object or to a set.
 */
abstract class RelationAddBaseViewModel(
    private val objectRelationList: ObjectRelationList,
) : BaseViewModel() {

    val views = MutableStateFlow<List<RelationView.Existing>>(emptyList())
    val isDismissed = MutableStateFlow(false)

    fun onStart(ctx: Id) {
        viewModelScope.launch {
            objectRelationList(ObjectRelationList.Params(ctx)).process(
                success = { relations ->
                    views.value = relations.map { relation ->
                        RelationView.Existing(
                            id = relation.key,
                            name = relation.name,
                            format = relation.format
                        )
                    }
                },
                failure = { Timber.e(it, "Error while fetching list of available relations") }
            )
        }
    }

    abstract fun onRelationSelected(ctx: Id, relation: Id)

    companion object {
        const val ERROR_MESSAGE = "Error while adding relation to object"
    }
}

class RelationAddToObjectViewModel(
    private val addRelationToObject: AddRelationToObject,
    private val dispatcher: Dispatcher<Payload>,
    objectRelationList: ObjectRelationList
) : RelationAddBaseViewModel(objectRelationList = objectRelationList) {

    override fun onRelationSelected(ctx: Id, relation: Id) {
        viewModelScope.launch {
            addRelationToObject(
                AddRelationToObject.Params(
                    ctx = ctx,
                    relation = relation
                )
            ).process(
                success = { dispatcher.send(it).also { isDismissed.value = true } },
                failure = {
                    Timber.e(it, ERROR_MESSAGE)
                    _toasts.emit("$ERROR_MESSAGE: ${it.localizedMessage}")
                }
            )
        }
    }

    class Factory(
        private val objectRelationList: ObjectRelationList,
        private val addRelationToObject: AddRelationToObject,
        private val dispatcher: Dispatcher<Payload>
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return RelationAddToObjectViewModel(
                addRelationToObject = addRelationToObject,
                objectRelationList = objectRelationList,
                dispatcher = dispatcher
            ) as T
        }
    }
}