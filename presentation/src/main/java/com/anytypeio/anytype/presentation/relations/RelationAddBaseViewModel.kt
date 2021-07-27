package com.anytypeio.anytype.presentation.relations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary.OBJECT_RELATION_ADD
import com.anytypeio.anytype.analytics.base.EventsDictionary.SETS_RELATION_ADD
import com.anytypeio.anytype.analytics.base.sendEvent
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
                    views.value = relations.toNotHiddenRelationViews()
                },
                failure = { Timber.e(it, "Error while fetching list of available relations") }
            )
        }
    }

    fun onQueryChanged(input: String) {
        userInput.value = input
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
    objectRelationList: ObjectRelationList,
    private val analytics: Analytics
) : RelationAddBaseViewModel(objectRelationList = objectRelationList) {

    fun onRelationSelected(ctx: Id, relation: Id) {
        val startTime = System.currentTimeMillis()
        viewModelScope.launch {
            addRelationToObject(
                AddRelationToObject.Params(
                    ctx = ctx,
                    relation = relation
                )
            ).process(
                success = {
                    viewModelScope.sendEvent(
                        analytics = analytics,
                        eventName = OBJECT_RELATION_ADD,
                        startTime = startTime,
                        middleTime = System.currentTimeMillis()
                    )
                    dispatcher.send(it).also { isDismissed.value = true }
                },
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
        private val dispatcher: Dispatcher<Payload>,
        private val analytics: Analytics
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return RelationAddToObjectViewModel(
                addRelationToObject = addRelationToObject,
                objectRelationList = objectRelationList,
                dispatcher = dispatcher,
                analytics = analytics
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
    objectRelationList: ObjectRelationList,
    private val analytics: Analytics
) : RelationAddBaseViewModel(objectRelationList = objectRelationList) {

    fun onRelationSelected(ctx: Id, relation: Id, dv: Id) {
        val startTime = System.currentTimeMillis()
        viewModelScope.launch {
            addRelationToDataView(
                AddRelationToDataView.Params(
                    ctx = ctx,
                    relation = relation,
                    dv = dv
                )
            ).process(
                success = {
                    viewModelScope.sendEvent(
                        analytics = analytics,
                        eventName = SETS_RELATION_ADD,
                        startTime = startTime,
                        middleTime = System.currentTimeMillis()
                    )
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
        private val dispatcher: Dispatcher<Payload>,
        private val analytics: Analytics
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return RelationAddToDataViewViewModel(
                addRelationToDataView = addRelationToDataView,
                objectRelationList = objectRelationList,
                dispatcher = dispatcher,
                session = session,
                updateDataViewViewer = updateDataViewViewer,
                state = state,
                analytics = analytics
            ) as T
        }
    }
}