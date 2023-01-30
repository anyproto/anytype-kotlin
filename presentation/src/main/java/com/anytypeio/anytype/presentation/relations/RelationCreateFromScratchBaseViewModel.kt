package com.anytypeio.anytype.presentation.relations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.core_models.*
import com.anytypeio.anytype.domain.dataview.interactor.AddRelationToDataView
import com.anytypeio.anytype.domain.dataview.interactor.UpdateDataViewViewer
import com.anytypeio.anytype.domain.relations.AddRelationToObject
import com.anytypeio.anytype.domain.relations.CreateRelation
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.extension.sendAnalyticsCreateRelationEvent
import com.anytypeio.anytype.presentation.relations.model.CreateFromScratchState
import com.anytypeio.anytype.presentation.relations.model.LimitObjectTypeValueView
import com.anytypeio.anytype.presentation.relations.model.RelationView
import com.anytypeio.anytype.presentation.relations.model.StateHolder
import com.anytypeio.anytype.presentation.sets.ObjectSet
import com.anytypeio.anytype.presentation.sets.ObjectSetSession
import com.anytypeio.anytype.presentation.util.Dispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
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
    private val createFromScratchState: StateHolder<CreateFromScratchState>,
    private val createRelation: CreateRelation,
    private val addRelationToObject: AddRelationToObject,
    private val dispatcher: Dispatcher<Payload>,
    private val analytics: Analytics,
): RelationCreateFromScratchBaseViewModel() {

    override val createFromScratchSession get() = createFromScratchState.state

    fun onCreateRelationClicked(ctx: Id) {
        proceedWithCreatingRelation(ctx)
    }

    private fun proceedWithCreatingRelation(ctx: Id) {
        viewModelScope.launch {
            val state = createFromScratchState.state.value
            val format = state.format
            createRelation(
                CreateRelation.Params(
                    format = format,
                    name = name.value,
                    limitObjectTypes = state.limitObjectTypes.map { it.id },
                    prefilled = emptyMap()
                )
            ).process(
                success = { relation ->
                    proceedWithAddingRelationToObject(
                        ctx = ctx,
                        relation = relation.key
                    ).also {
                        sendAnalyticsCreateRelationEvent(
                            analytics = analytics,
                            type = EventsDictionary.Type.menu,
                            format = format.name
                        )
                    }
                },
                failure = {
                    Timber.e(it, ACTION_FAILED_ERROR).also { _toasts.emit(ACTION_FAILED_ERROR) }
                }
            )
        }
    }

    private fun proceedWithAddingRelationToObject(ctx: Id, relation: Key) {
        viewModelScope.launch {
            addRelationToObject(
                AddRelationToObject.Params(
                    ctx = ctx,
                    relationKey = relation
                )
            ).process(
                success = { payload ->
                    dispatcher.send(payload).also { isDismissed.value = true }
                },
                failure = {
                    Timber.e(it, ACTION_FAILED_ERROR).also { _toasts.emit(ACTION_FAILED_ERROR) }
                }
            )
        }
    }

    class Factory(
        private val createFromScratchState: StateHolder<CreateFromScratchState>,
        private val createRelation: CreateRelation,
        private val addRelationToObject: AddRelationToObject,
        private val dispatcher: Dispatcher<Payload>,
        private val analytics: Analytics,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return RelationCreateFromScratchForObjectViewModel(
                dispatcher = dispatcher,
                analytics = analytics,
                createFromScratchState = createFromScratchState,
                createRelation = createRelation,
                addRelationToObject = addRelationToObject
            ) as T
        }
    }
}

class RelationCreateFromScratchForObjectBlockViewModel(
    private val addRelationToObject: AddRelationToObject,
    private val dispatcher: Dispatcher<Payload>,
    private val analytics: Analytics,
    private val createFromScratchState: StateHolder<CreateFromScratchState>,
    private val createRelation: CreateRelation
) : RelationCreateFromScratchBaseViewModel() {

    override val createFromScratchSession get() = createFromScratchState.state

    val commands = MutableSharedFlow<Command>(replay = 0)

    fun onCreateRelationClicked(ctx: Id) {
        proceedWithCreatingRelation(ctx = ctx)
    }

    private fun proceedWithCreatingRelation(ctx: Id) {
        viewModelScope.launch {
            val state = createFromScratchState.state.value
            val format = state.format
            createRelation(
                CreateRelation.Params(
                    format = format,
                    name = name.value,
                    limitObjectTypes = state.limitObjectTypes.map { it.id },
                    prefilled = emptyMap()
                )
            ).process(
                success = { relation ->
                    sendToast("Relation `${relation.name}` added to your library")
                    proceedWithAddingRelationToObject(
                        ctx = ctx,
                        relationKey = relation.key
                    ).also {
                        sendAnalyticsCreateRelationEvent(
                            analytics = analytics,
                            type = EventsDictionary.Type.block,
                            format = format.name
                        )
                    }
                },
                failure = {
                    Timber.e(it, ACTION_FAILED_ERROR).also { _toasts.emit(ACTION_FAILED_ERROR) }
                }
            )
        }
    }

    private fun proceedWithAddingRelationToObject(ctx: Id, relationKey: Id) {
        viewModelScope.launch {
            addRelationToObject(
                AddRelationToObject.Params(
                    ctx = ctx,
                    relationKey = relationKey
                )
            ).process(
                success = { payload ->
                    dispatcher.send(payload).also { commands.emit(Command.OnSuccess(relationKey)) }
                },
                failure = {
                    Timber.e(it, ACTION_FAILED_ERROR).also { _toasts.emit(ACTION_FAILED_ERROR) }
                }
            )
        }
    }

    class Factory(
        private val addRelationToObject: AddRelationToObject,
        private val dispatcher: Dispatcher<Payload>,
        private val analytics: Analytics,
        private val createFromScratchState: StateHolder<CreateFromScratchState>,
        private val createRelation: CreateRelation
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return RelationCreateFromScratchForObjectBlockViewModel(
                dispatcher = dispatcher,
                addRelationToObject = addRelationToObject,
                analytics = analytics,
                createFromScratchState = createFromScratchState,
                createRelation = createRelation
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
    private val addRelationToDataView: AddRelationToDataView,
    private val dispatcher: Dispatcher<Payload>,
    private val analytics: Analytics,
    private val createFromScratchState: StateHolder<CreateFromScratchState>,
    private val createRelation: CreateRelation
) : RelationCreateFromScratchBaseViewModel() {

    override val createFromScratchSession: Flow<CreateFromScratchState> get() = createFromScratchState.state

    fun onCreateRelationClicked(ctx: Id, dv: Id) {
        proceedWithCreatingRelation(ctx = ctx, dv = dv)
    }

    private fun proceedWithCreatingRelation(ctx: Id, dv: Id) {
        viewModelScope.launch {
            val state = createFromScratchState.state.value
            val format = state.format
            createRelation(
                CreateRelation.Params(
                    format = format,
                    name = name.value,
                    limitObjectTypes = state.limitObjectTypes.map { it.id },
                    prefilled = emptyMap()
                )
            ).process(
                success = { relation ->
                    proceedWithAddingRelationToDataView(
                        ctx = ctx,
                        relationKey = relation.key,
                        dv = dv
                    ).also {
                        sendAnalyticsCreateRelationEvent(
                            analytics = analytics,
                            type = EventsDictionary.Type.dataView,
                            format = format.name
                        )
                    }
                },
                failure = {
                    Timber.e(it, ACTION_FAILED_ERROR).also { _toasts.emit(ACTION_FAILED_ERROR) }
                }
            )
        }
    }

    private fun proceedWithAddingRelationToDataView(ctx: Id, dv: Id, relationKey: Key) {
        viewModelScope.launch {
            addRelationToDataView(
                AddRelationToDataView.Params(
                    ctx = ctx,
                    dv = dv,
                    relation = relationKey
                )
            ).process(
                success = { payload ->
                    dispatcher.send(payload).also {
                        proceedWithAddingNewRelationToCurrentViewer(
                            ctx = ctx,
                            relationKey = relationKey
                        )
                    }
                },
                failure = {
                    Timber.d(it, "Error while adding relation with key: $relationKey to data view: $dv")
                }
            )
        }
    }

    private suspend fun proceedWithAddingNewRelationToCurrentViewer(ctx: Id, relationKey: Key) {
        val state = state.value
        val block = state.dataview
        val dv = block.content as DV
        val viewer = dv.viewers.find { it.id == session.currentViewerId.value } ?: dv.viewers.first()
        updateDataViewViewer(
            UpdateDataViewViewer.Params.ViewerRelation.Add(
                ctx = ctx,
                dv = block.id,
                view = viewer.id,
                relation = DVViewerRelation(
                    key = relationKey,
                    isVisible = true
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
        private val addRelationToDataView: AddRelationToDataView,
        private val createFromScratchState: StateHolder<CreateFromScratchState>,
        private val createRelation: CreateRelation,
        private val dispatcher: Dispatcher<Payload>,
        private val analytics: Analytics
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return RelationCreateFromScratchForDataViewViewModel(
                dispatcher = dispatcher,
                session = session,
                updateDataViewViewer = updateDataViewViewer,
                state = state,
                analytics = analytics,
                createFromScratchState = createFromScratchState,
                createRelation = createRelation,
                addRelationToDataView = addRelationToDataView
            ) as T
        }
    }
}