package com.anytypeio.anytype.presentation.relations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.core_models.*
import com.anytypeio.anytype.domain.dataview.interactor.AddNewRelationToDataView
import com.anytypeio.anytype.domain.dataview.interactor.UpdateDataViewViewer
import com.anytypeio.anytype.domain.relations.AddNewRelationToObject
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.extension.sendAnalyticsCreateRelationEvent
import com.anytypeio.anytype.presentation.relations.model.CreateFromScratchState
import com.anytypeio.anytype.presentation.relations.model.LimitObjectTypeValueView
import com.anytypeio.anytype.presentation.relations.model.RelationView
import com.anytypeio.anytype.presentation.relations.model.StateHolder
import com.anytypeio.anytype.presentation.sets.ObjectSet
import com.anytypeio.anytype.presentation.sets.ObjectSetSession
import com.anytypeio.anytype.presentation.util.Dispatcher
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber

abstract class RelationCreateFromScratchBaseViewModel : BaseViewModel() {

    abstract val createFromScratchSession: Flow<CreateFromScratchState>

    val limitObjectTypeValueView: Flow<LimitObjectTypeValueView?>
        get() = createFromScratchSession.map { session ->
            if (session.format == RelationFormat.OBJECT)
                LimitObjectTypeValueView(
                    types = session.limitObjectTypes
                )
            else
                null
        }

    protected val name = MutableStateFlow("")
    private val notAllowedFormats = listOf(
        Relation.Format.SHORT_TEXT,
        Relation.Format.EMOJI,
        Relation.Format.RELATIONS
    )

    val views = MutableStateFlow(
        Relation.orderedFormatList()
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
    private val analytics: Analytics,
    private val createFromScratchState: StateHolder<CreateFromScratchState>
) : RelationCreateFromScratchBaseViewModel() {

    override val createFromScratchSession get() = createFromScratchState.state

    fun onCreateRelationClicked(ctx: Id) {
        viewModelScope.launch {
            val state = createFromScratchState.state.value
            val format = state.format
            addNewRelationToObject(
                AddNewRelationToObject.Params(
                    ctx = ctx,
                    format = format,
                    name = name.value,
                    limitObjectTypes = state.limitObjectTypes.map { it.id }
                )
            ).process(
                success = { (_, payload) ->
                    dispatcher.send(payload).also {
                        sendAnalyticsCreateRelationEvent(
                            analytics = analytics,
                            type = EventsDictionary.Type.menu,
                            format = format.name
                        )
                        isDismissed.value = true
                    }
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
        private val analytics: Analytics,
        private val createFromScratchState: StateHolder<CreateFromScratchState>
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return RelationCreateFromScratchForObjectViewModel(
                dispatcher = dispatcher,
                addNewRelationToObject = addNewRelationToObject,
                analytics = analytics,
                createFromScratchState = createFromScratchState
            ) as T
        }
    }
}

class RelationCreateFromScratchForObjectBlockViewModel(
    private val addNewRelationToObject: AddNewRelationToObject,
    private val dispatcher: Dispatcher<Payload>,
    private val analytics: Analytics,
    private val createFromScratchState: StateHolder<CreateFromScratchState>
) : RelationCreateFromScratchBaseViewModel() {

    override val createFromScratchSession get() = createFromScratchState.state

    val commands = MutableSharedFlow<Command>(replay = 0)

    fun onCreateRelationClicked(ctx: Id) {
        viewModelScope.launch {
            val state = createFromScratchState.state.value
            val format = state.format
            addNewRelationToObject(
                AddNewRelationToObject.Params(
                    ctx = ctx,
                    format = format,
                    name = name.value,
                    limitObjectTypes = state.limitObjectTypes.map { it.id }
                )
            ).process(
                success = { (relation, payload) ->
                    dispatcher.send(payload)
                    sendAnalyticsCreateRelationEvent(
                        analytics = analytics,
                        type = EventsDictionary.Type.block,
                        format = format.name
                    )
                    commands.emit(Command.OnSuccess(relation))
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
        private val analytics: Analytics,
        private val createFromScratchState: StateHolder<CreateFromScratchState>
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return RelationCreateFromScratchForObjectBlockViewModel(
                dispatcher = dispatcher,
                addNewRelationToObject = addNewRelationToObject,
                analytics = analytics,
                createFromScratchState = createFromScratchState
            ) as T
        }
    }

    sealed class Command {
        data class OnSuccess(val relation: Id) : Command()
    }
}

class RelationCreateFromScratchForDataViewViewModel(
    private val state: StateFlow<ObjectSet>,
    private val session: ObjectSetSession,
    private val updateDataViewViewer: UpdateDataViewViewer,
    private val addNewRelationToDataView: AddNewRelationToDataView,
    private val dispatcher: Dispatcher<Payload>,
    private val analytics: Analytics,
    private val createFromScratchState: StateHolder<CreateFromScratchState>
) : RelationCreateFromScratchBaseViewModel() {

    override val createFromScratchSession: Flow<CreateFromScratchState> get() = createFromScratchState.state

    fun onCreateRelationClicked(ctx: Id, dv: Id) {
        viewModelScope.launch {
            val state = createFromScratchState.state.value
            val format = state.format
            addNewRelationToDataView(
                AddNewRelationToDataView.Params(
                    ctx = ctx,
                    format = format,
                    name = name.value,
                    target = dv,
                    limitObjectTypes = state.limitObjectTypes.map { it.id }
                )
            ).process(
                success = { (relation, payload) ->
                    dispatcher.send(payload).also {
                        sendAnalyticsCreateRelationEvent(
                            analytics = analytics,
                            type = EventsDictionary.Type.dataView,
                            format = format.name
                        )
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
        private val createFromScratchState: StateHolder<CreateFromScratchState>,
        private val dispatcher: Dispatcher<Payload>,
        private val analytics: Analytics
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return RelationCreateFromScratchForDataViewViewModel(
                dispatcher = dispatcher,
                addNewRelationToDataView = addNewRelationToDataView,
                session = session,
                updateDataViewViewer = updateDataViewViewer,
                state = state,
                analytics = analytics,
                createFromScratchState = createFromScratchState
            ) as T
        }
    }
}