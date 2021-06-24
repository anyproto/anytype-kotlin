package com.anytypeio.anytype.presentation.relations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.*
import com.anytypeio.anytype.domain.dataview.interactor.AddRelationToDataView
import com.anytypeio.anytype.domain.dataview.interactor.ObjectRelationList
import com.anytypeio.anytype.domain.dataview.interactor.UpdateDataViewViewer
import com.anytypeio.anytype.domain.relations.AddRelationToObject
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.relations.model.RelationView
import com.anytypeio.anytype.presentation.sets.ObjectSet
import com.anytypeio.anytype.presentation.sets.ObjectSetSession
import com.anytypeio.anytype.presentation.util.Dispatcher
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Base view model for adding a relation either to an object or to a set.
 */
abstract class RelationAddBaseViewModel(
    private val objectRelationList: ObjectRelationList,
) : BaseViewModel() {

    private val userInput = MutableStateFlow(DEFAULT_INPUT)
    private val searchQuery = userInput.take(1).onCompletion {
        emitAll(userInput.debounce(DEBOUNCE_DURATION).distinctUntilChanged())
    }

    val views = MutableStateFlow<List<RelationView.Existing>>(emptyList())
    val isDismissed = MutableStateFlow(false)

    val results = combine(searchQuery, views) { query, views ->
        if (query.isEmpty())
            views
        else
            views.filter { view -> view.name.contains(query, true) }
    }

    fun onStart(ctx: Id) {
        viewModelScope.launch {
            objectRelationList(ObjectRelationList.Params(ctx)).process(
                success = { relations ->
                    views.value = toNotHiddenRelationViews(relations)
                },
                failure = { Timber.e(it, "Error while fetching list of available relations") }
            )
        }
    }

    fun onQueryChanged(input: String) {
        userInput.value = input
    }

    private fun toNotHiddenRelationViews(relations: List<Relation>): List<RelationView.Existing> {
        return relations.filter { !it.isHidden }
            .map {
                RelationView.Existing(
                    id = it.key,
                    name = it.name,
                    format = it.format
                )
            }
    }

    companion object {
        const val ERROR_MESSAGE = "Error while adding relation to object"
        const val DEBOUNCE_DURATION = 300L
        const val DEFAULT_INPUT = ""
    }
}

class RelationAddToObjectViewModel(
    private val addRelationToObject: AddRelationToObject,
    private val dispatcher: Dispatcher<Payload>,
    objectRelationList: ObjectRelationList
) : RelationAddBaseViewModel(objectRelationList = objectRelationList) {

    fun onRelationSelected(ctx: Id, relation: Id) {
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

class RelationAddToDataViewViewModel(
    private val state: StateFlow<ObjectSet>,
    private val session: ObjectSetSession,
    private val updateDataViewViewer: UpdateDataViewViewer,
    private val addRelationToDataView: AddRelationToDataView,
    private val dispatcher: Dispatcher<Payload>,
    objectRelationList: ObjectRelationList
) : RelationAddBaseViewModel(objectRelationList = objectRelationList) {

    fun onRelationSelected(ctx: Id, relation: Id, dv: Id) {
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
                        proceedWithAddingNewRelationToCurrentViewer(ctx = ctx, relation = relation)
                    }
                },
                failure = {
                    Timber.e(it, ERROR_MESSAGE)
                    _toasts.emit("$ERROR_MESSAGE: ${it.localizedMessage}")
                }
            )
        }
    }

    private suspend fun proceedWithAddingNewRelationToCurrentViewer(ctx: Id, relation: Id) {
        val state = state.value
        val block = state.dataview
        val dv = block.content as DV
        val viewer = dv.viewers.find { it.id == session.currentViewerId } ?: dv.viewers.first()

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
        private val objectRelationList: ObjectRelationList,
        private val addRelationToDataView: AddRelationToDataView,
        private val dispatcher: Dispatcher<Payload>
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return RelationAddToDataViewViewModel(
                addRelationToDataView = addRelationToDataView,
                objectRelationList = objectRelationList,
                dispatcher = dispatcher,
                session = session,
                updateDataViewViewer = updateDataViewViewer,
                state = state
            ) as T
        }
    }
}