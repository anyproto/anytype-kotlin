package com.anytypeio.anytype.presentation.relations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary.OBJECT_RELATION_CREATE
import com.anytypeio.anytype.analytics.base.EventsDictionary.PROP_RELATION_FORMAT
import com.anytypeio.anytype.analytics.base.EventsDictionary.SETS_RELATION_CREATE
import com.anytypeio.anytype.analytics.base.sendEvent
import com.anytypeio.anytype.analytics.props.Props
import com.anytypeio.anytype.core_models.*
import com.anytypeio.anytype.domain.dataview.interactor.AddNewRelationToDataView
import com.anytypeio.anytype.domain.dataview.interactor.UpdateDataViewViewer
import com.anytypeio.anytype.domain.relations.AddNewRelationToObject
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.relations.model.RelationView
import com.anytypeio.anytype.presentation.sets.ObjectSet
import com.anytypeio.anytype.presentation.sets.ObjectSetSession
import com.anytypeio.anytype.presentation.util.Dispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber

abstract class RelationCreateFromScratchBaseViewModel : BaseViewModel() {

    protected val name = MutableStateFlow("")
    private val notAllowedFormats = listOf(
        Relation.Format.SHORT_TEXT,
        Relation.Format.EMOJI,
        Relation.Format.RELATIONS
    )

    val views = MutableStateFlow(
        Relation.Format.values()
            .filterNot { notAllowedFormats.contains(it) }
            .map { format ->
                RelationView.CreateFromScratch(
                    format = format,
                    isSelected = format == Relation.Format.LONG_TEXT
                )
            }
    )

    val isActionButtonEnabled = name.map { it.isNotEmpty() }
    val isDismissed = MutableStateFlow(false)

    fun onRelationFormatClicked(format: RelationFormat) {
        views.value = views.value.map { view ->
            view.copy(isSelected = view.format == format)
        }
    }

    fun onNameChanged(input: String) {
        name.value = input
    }

    companion object {
        const val ACTION_FAILED_ERROR =
            "Error while creating a new relation. Please, try again later"
    }
}

class RelationCreateFromScratchForObjectViewModel(
    private val addNewRelationToObject: AddNewRelationToObject,
    private val dispatcher: Dispatcher<Payload>,
    private val analytics: Analytics
) : RelationCreateFromScratchBaseViewModel() {

    fun onCreateRelationClicked(ctx: Id) {
        val startTime = System.currentTimeMillis()
        viewModelScope.launch {
            val format =  views.value.first { it.isSelected }.format
            addNewRelationToObject(
                AddNewRelationToObject.Params(
                    ctx = ctx,
                    format = format,
                    name = name.value
                )
            ).process(
                success = {
                    viewModelScope.sendEvent(
                        analytics = analytics,
                        eventName = OBJECT_RELATION_CREATE,
                        props = Props(mapOf(PROP_RELATION_FORMAT to format.name)),
                        startTime = startTime,
                        middleTime = System.currentTimeMillis()
                    )
                    dispatcher.send(it).also { isDismissed.value = true }
                },
                failure = {
                    Timber.e(it, ACTION_FAILED_ERROR).also { _toasts.emit(ACTION_FAILED_ERROR) }
                }
            )
        }
    }

    class Factory(
        private val addNewRelationToObject: AddNewRelationToObject,
        private val dispatcher: Dispatcher<Payload>,
        private val analytics: Analytics
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return RelationCreateFromScratchForObjectViewModel(
                dispatcher = dispatcher,
                addNewRelationToObject = addNewRelationToObject,
                analytics = analytics
            ) as T
        }
    }
}

class RelationCreateFromScratchForDataViewViewModel(
    private val state: StateFlow<ObjectSet>,
    private val session: ObjectSetSession,
    private val updateDataViewViewer: UpdateDataViewViewer,
    private val addNewRelationToDataView: AddNewRelationToDataView,
    private val dispatcher: Dispatcher<Payload>,
    private val analytics: Analytics
) : RelationCreateFromScratchBaseViewModel() {

    fun onCreateRelationClicked(ctx: Id, dv: Id) {
        val startTime = System.currentTimeMillis()
        viewModelScope.launch {
            val format =  views.value.first { it.isSelected }.format
            addNewRelationToDataView(
                AddNewRelationToDataView.Params(
                    ctx = ctx,
                    format = format,
                    name = name.value,
                    target = dv
                )
            ).process(
                success = { (relation, payload) ->
                    viewModelScope.sendEvent(
                        analytics = analytics,
                        eventName = SETS_RELATION_CREATE,
                        props = Props(mapOf(PROP_RELATION_FORMAT to format.name)),
                        startTime = startTime,
                        middleTime = System.currentTimeMillis()
                    )
                    dispatcher.send(payload).also {
                        proceedWithAddingNewRelationToCurrentViewer(
                            ctx = ctx,
                            relation = relation
                        )
                    }
                },
                failure = {
                    Timber.e(it, ACTION_FAILED_ERROR).also { _toasts.emit(ACTION_FAILED_ERROR) }
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
        private val addNewRelationToDataView: AddNewRelationToDataView,
        private val dispatcher: Dispatcher<Payload>,
        private val analytics: Analytics
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return RelationCreateFromScratchForDataViewViewModel(
                dispatcher = dispatcher,
                addNewRelationToDataView = addNewRelationToDataView,
                session = session,
                updateDataViewViewer = updateDataViewViewer,
                state = state,
                analytics = analytics
            ) as T
        }
    }
}