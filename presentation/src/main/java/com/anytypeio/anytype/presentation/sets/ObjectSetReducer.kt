package com.anytypeio.anytype.presentation.sets

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.DV
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.Event.Command
import com.anytypeio.anytype.core_models.ext.amend
import com.anytypeio.anytype.core_models.ext.unset
import com.anytypeio.anytype.core_utils.ext.replace
import com.anytypeio.anytype.presentation.extension.updateFields
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.scan
import timber.log.Timber

class ObjectSetReducer {

    private val eventChannel: Channel<List<Event>> = Channel()

    val state = MutableStateFlow(ObjectSet.init())
    val effects = MutableSharedFlow<List<SideEffect>>(0)

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
                effects.emit(transformation.effects)
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
        val effects = mutableListOf<SideEffect>()
        val newState = when (event) {
            is Command.ShowObject -> {
                state.copy(
                    blocks = event.blocks,
                    details = state.details.updateFields(event.details.details),
                    restrictions = event.dataViewRestrictions,
                    objectRestrictions = event.objectRestrictions
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
            is Command.DataView.SetRelation -> {
                state.copy(
                    blocks = state.blocks.map { block ->
                        if (block.id == event.dv) {
                            val content = block.content
                            check(content is DV)
                            block.copy(
                                content = content.copy(
                                    relationsIndex = content.relationsIndex + event.links
                                )
                            )
                        } else
                            block
                    }
                )
            }
            is Command.DataView.SetTargetObjectId -> {
                state.copy(
                    blocks = state.blocks.map { block ->
                        if (block.id == event.dv) {
                            val content = block.content
                            if (content is DV) {
                                block.copy(
                                    content = content.copy(
                                        targetObjectId = event.targetObjectId
                                    )
                                )
                            } else {
                                block
                            }
                        } else
                            block
                    }
                )
            }
            is Command.DataView.UpdateView -> {
                val updatedBlocks = state.blocks.map { block: Block ->
                    val content = block.content
                    if (block.id == event.block && content is DV) {
                        block.copy(
                            content = content.copy(
                                viewers = content.viewers.map { viewer ->
                                    if (viewer.id == event.viewerId) {
                                        val filters = if (event.filterUpdates.isNotEmpty()) {
                                            viewer.filters.updateFilters(
                                                updates = event.filterUpdates
                                            )
                                        } else {
                                            viewer.filters
                                        }
                                        val sorts = if (event.sortUpdates.isNotEmpty()) {
                                            viewer.sorts.updateSorts(
                                                updates = event.sortUpdates
                                            )
                                        } else {
                                            viewer.sorts
                                        }
                                        val viewerRelations =
                                            if (event.relationUpdates.isNotEmpty()) {
                                                viewer.viewerRelations.updateViewerRelations(
                                                    updates = event.relationUpdates
                                                )
                                            } else {
                                                viewer.viewerRelations
                                            }
                                        viewer.copy(
                                            filters = filters,
                                            sorts = sorts,
                                            viewerRelations = viewerRelations
                                        )
                                    } else {
                                        viewer
                                    }
                                }
                            )
                        )
                    } else {
                        block
                    }
                }
                state.copy(blocks = updatedBlocks)
            }
            is Command.Details.Set -> {
                state.copy(
                    details = state.details.toMutableMap().apply {
                        put(event.target, event.details)
                    }
                )
            }
            is Command.Details.Amend -> {
                state.copy(
                    details = state.details.amend(
                        target = event.target,
                        slice = event.details
                    )
                )
            }
            is Command.Details.Unset -> {
                state.copy(
                    details = state.details.unset(
                        target = event.target,
                        keys = event.keys
                    )
                )
            }
            is Command.UpdateStructure -> {
                state.copy(
                    blocks = state.blocks.replace(
                        replacement = { target -> target.copy(children = event.children) },
                        target = { block -> block.id == event.id }
                    )
                )
            }
            is Command.AddBlock -> {
                state.copy(blocks = state.blocks + event.blocks)
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

    /**
     * Can be used to send side effects from reducer
     */
    sealed class SideEffect

    fun clear() {
        eventChannel.close()
    }
}