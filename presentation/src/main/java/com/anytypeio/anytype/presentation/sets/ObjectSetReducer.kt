package com.anytypeio.anytype.presentation.sets

import com.anytypeio.anytype.core_models.DV
import com.anytypeio.anytype.core_models.DVViewer
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.Event.Command
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.presentation.extension.updateFields
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import timber.log.Timber

class ObjectSetReducer {

    private val eventChannel: Channel<List<Event>> = Channel()
    private val effectChannel: Channel<List<SideEffect>> = Channel()

    val state = MutableStateFlow(ObjectSet.init())

    val effects = effectChannel
        .consumeAsFlow()
        .filter { it.isNotEmpty() }
        .flatMapConcat { it.asFlow() }
        .distinctUntilChanged()

    suspend fun run() {
        eventChannel
            .consumeAsFlow()
            .scan(Transformation.init()) { transformation, events ->
                reduce(
                    transformation.state,
                    events
                )
            }
            .collect { transformation ->
                state.value = transformation.state
                effectChannel.send(transformation.effects)
            }
    }

    suspend fun dispatch(events: List<Event>) {
        eventChannel.send(events)
    }

    fun reduce(state: ObjectSet, events: List<Event>): Transformation {
        var current = Transformation(state = state)
        events.forEach { event ->
            val transformed = reduce(current.state, event)
            current = Transformation(
                state = transformed.state,
                effects = current.effects + transformed.effects
            )
        }
        return current
    }

    private fun reduce(state: ObjectSet, event: Event): Transformation {
        Timber.d("Reducing event: $event")
        val effects = mutableListOf<SideEffect>()
        val newState = when (event) {
            is Command.ShowBlock -> {
                state.copy(
                    blocks = event.blocks,
                    details = state.details.updateFields(event.details.details),
                    objectTypes = event.objectTypes,
                    restrictions = event.dataViewRestrictions
                )
            }
            is Command.DataView.SetView -> {
                state.copy(
                    blocks = state.blocks.map { block ->
                        if (block.id == event.target) {
                            val content = block.content
                            check(content is DV)
                            val result = content.viewers.toMutableList()
                            if (result.any { it.id == event.viewerId }) {
                                result.replaceAll { viewer ->
                                    if (viewer.id == event.viewerId)
                                        event.viewer
                                    else
                                        viewer
                                }
                            } else {
                                result.add(event.viewer)
                            }
                            block.copy(
                                content = content.copy(
                                    source = content.source,
                                    relations = content.relations,
                                    viewers = result
                                )
                            )
                        } else {
                            block
                        }
                    }
                )
            }
            is Command.DataView.DeleteView -> {
                state.copy(
                    blocks = state.blocks.map { block ->
                        if (block.id == event.target) {
                            val content = block.content
                            check(content is DV)
                            val viewers = content.viewers.toMutableList()
                            block.copy(
                                content = content.copy(
                                    viewers = viewers.apply {
                                        removeIf { viewer -> viewer.id == event.viewer }
                                    }
                                )
                            )
                        } else {
                            block
                        }
                    }
                )
            }
            is Command.DataView.SetRecords -> {
                val current = state.viewerDb.toMutableMap().apply { clear() }
                val result = current.apply {
                    put(
                        event.view,
                        ObjectSet.ViewerData(
                            records = event.records,
                            total = event.total
                        )
                    )
                }
                state.copy(viewerDb = result)
            }
            is Command.DataView.UpdateRecord -> {
                state.updateRecord(event.viewer, event.records)
            }
            is Command.DataView.SetRelation -> {
                state.copy(
                    blocks = state.blocks.map { block ->
                        if (block.id == event.id) {
                            val content = block.content
                            check(content is DV)
                            block.copy(
                                content = content.copy(
                                    relations = content.relations.toMutableList().apply {
                                        removeIf { it.key == event.key }
                                        add(event.relation)
                                    }
                                )
                            )
                        } else
                            block
                    }
                )
            }
            is Command.Details.Set -> {
                state.copy(
                    details = state.details.toMutableMap().apply {
                        put(event.target, event.details)
                    }
                )
            }
            else -> {
                Timber.d("Ignoring event: $event")
                state.copy()
            }
        }
        return Transformation(
            state = newState,
            effects = effects
        )
    }

    data class Transformation(
        val state: ObjectSet,
        val effects: List<SideEffect> = emptyList()
    ) {
        companion object {
            fun init() = Transformation(ObjectSet.init(), emptyList())
        }
    }

    sealed class SideEffect {
        data class ViewerUpdate(
            val target: Id,
            val viewer: DVViewer
        ) : SideEffect()
    }

    fun clear() {
        eventChannel.close()
        effectChannel.close()
    }
}