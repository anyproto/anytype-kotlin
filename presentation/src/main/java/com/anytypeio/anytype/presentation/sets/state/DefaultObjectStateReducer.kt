package com.anytypeio.anytype.presentation.sets.state

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.DVViewer
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.Event.Command
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ext.amend
import com.anytypeio.anytype.core_models.ext.unset
import com.anytypeio.anytype.core_utils.ext.replace
import com.anytypeio.anytype.presentation.sets.updateFields
import com.anytypeio.anytype.presentation.sets.updateFilters
import com.anytypeio.anytype.presentation.sets.updateSorts
import com.anytypeio.anytype.presentation.sets.updateViewerRelations
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.map
import timber.log.Timber

class DefaultObjectStateReducer : ObjectStateReducer {

    private val eventChannel: Channel<List<Event>> = Channel()
    override val state: MutableStateFlow<ObjectState> = MutableStateFlow(ObjectState.Init)
    private val _effects = MutableSharedFlow<List<StateSideEffect>>(0)
    override val effects: SharedFlow<List<StateSideEffect>> = _effects

    override suspend fun run() {
        eventChannel
            .consumeAsFlow()
            .map { events -> reduce(state.value, events) }
            .collect { transformation ->
                state.value = transformation.state
                _effects.emit(transformation.effects)
            }
    }

    override suspend fun dispatch(events: List<Event>) {
        eventChannel.send(events)
    }

    override fun reduce(state: ObjectState, events: List<Event>): Transformation {
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

    private fun reduce(state: ObjectState, event: Event): Transformation {
        val effects = mutableListOf<StateSideEffect>()
        val newState = when (event) {
            is Command.ShowObject -> {
                handleShowObject(event)
            }
            is Command.DataView.SetView -> {
                handleSetView(state, event)
            }
            is Command.DataView.DeleteView -> {
                handleDeleteView(state, event)
            }
            is Command.DataView.SetRelation -> {
                handelSetRelation(state, event)
            }
            is Command.DataView.DeleteRelation -> {
                handleDeleteRelation(state, event)
            }
            is Command.DataView.SetTargetObjectId -> {
                handleSetTargetObjectId(state, event)
            }
            is Command.DataView.UpdateView -> {
                handleUpdateView(state, event)
            }
            is Command.Details.Set -> {
                handleSetDetails(state, event)
            }
            is Command.Details.Amend -> {
                handleAmendDetails(state, event)
            }
            is Command.Details.Unset -> {
                handleUnsetDetails(state, event)
            }
            is Command.UpdateStructure -> {
                handleUpdateStructure(state, event)
            }
            is Command.AddBlock -> {
                handleAddBlock(state, event)
            }
            else -> {
                Timber.d("Ignoring event: $event")
                state
            }
        }
        return Transformation(
            state = newState,
            effects = effects
        )
    }


    //region EVENTS
    /**
     * @see Command.ShowObject
     */
    private fun handleShowObject(event: Command.ShowObject): ObjectState {
        val objectState = when (val layout = event.details.details[event.root]?.layout?.toInt()) {
            ObjectType.Layout.COLLECTION.code -> ObjectState.DataView.Collection(
                blocks = event.blocks,
                details = event.details.details,
                objectRestrictions = event.objectRestrictions,
                dataViewRestrictions = event.dataViewRestrictions
            )
            ObjectType.Layout.SET.code -> ObjectState.DataView.Set(
                blocks = event.blocks,
                details = event.details.details,
                objectRestrictions = event.objectRestrictions,
                dataViewRestrictions = event.dataViewRestrictions
            )
            else -> {
                Timber.e("Wrong layout type: $layout")
                ObjectState.ErrorLayout
            }
        }
        return objectState
    }

    /**
     * @see Command.DataView.SetView
     */
    private fun handleSetView(state: ObjectState, event: Command.DataView.SetView): ObjectState {
        val updateBlockContent = { content: Block.Content.DataView ->
            content.copy(viewers = content.viewers.updateOrAdd(event))
        }
        return when (state) {
            is ObjectState.DataView.Collection -> state.updateBlockContent(
                target = event.target,
                blockContentUpdate = updateBlockContent
            )
            is ObjectState.DataView.Set -> state.updateBlockContent(
                target = event.target,
                blockContentUpdate = updateBlockContent
            )
            else -> state
        }
    }

    /**
     * @see Command.DataView.DeleteView
     */
    private fun handleDeleteView(
        state: ObjectState,
        event: Command.DataView.DeleteView
    ): ObjectState {
        val updateBlockContent = { content: Block.Content.DataView ->
            content.copy(
                viewers = content.viewers.toMutableList().apply {
                    removeIf { viewer -> viewer.id == event.viewer }
                }
            )
        }
        return when (state) {
            is ObjectState.DataView.Collection -> state.updateBlockContent(
                target = event.target,
                blockContentUpdate = updateBlockContent
            )
            is ObjectState.DataView.Set -> state.updateBlockContent(
                target = event.target,
                blockContentUpdate = updateBlockContent
            )
            else -> state
        }
    }

    /**
     * @see Command.DataView.SetRelation
     */
    private fun handelSetRelation(
        state: ObjectState,
        event: Command.DataView.SetRelation
    ): ObjectState {
        val updateBlockContent = { content: Block.Content.DataView ->
            content.copy(relationLinks = content.relationLinks + event.links)
        }
        return when (state) {
            is ObjectState.DataView.Collection -> state.updateBlockContent(
                target = event.dv,
                blockContentUpdate = updateBlockContent
            )
            is ObjectState.DataView.Set -> state.updateBlockContent(
                target = event.dv,
                blockContentUpdate = updateBlockContent
            )
            else -> state
        }
    }

    /**
     * @see Command.DataView.DeleteRelation
     */
    private fun handleDeleteRelation(
        state: ObjectState,
        event: Command.DataView.DeleteRelation
    ): ObjectState {
        val updateBlockContent = { content: Block.Content.DataView ->
            content.copy(relationLinks = content.relationLinks.filter { link ->
                !event.keys.contains(link.key)
            })
        }
        return when (state) {
            is ObjectState.DataView.Collection -> state.updateBlockContent(
                target = event.dv,
                blockContentUpdate = updateBlockContent
            )
            is ObjectState.DataView.Set -> state.updateBlockContent(
                target = event.dv,
                blockContentUpdate = updateBlockContent
            )
            else -> state
        }
    }

    /**
     * @see Command.DataView.SetTargetObjectId
     */
    private fun handleSetTargetObjectId(
        state: ObjectState,
        event: Command.DataView.SetTargetObjectId
    ): ObjectState {
        val updateBlockContent = { content: Block.Content.DataView ->
            content.copy(targetObjectId = event.targetObjectId)
        }
        return when (state) {
            is ObjectState.DataView.Collection -> state.updateBlockContent(
                target = event.dv,
                blockContentUpdate = updateBlockContent
            )
            is ObjectState.DataView.Set -> state.updateBlockContent(
                target = event.dv,
                blockContentUpdate = updateBlockContent
            )
            else -> state
        }
    }

    /**
     * @see Command.DataView.UpdateView
     */
    private fun handleUpdateView(
        state: ObjectState,
        event: Command.DataView.UpdateView
    ): ObjectState {
        val updateBlockContent = { content: Block.Content.DataView ->
            content.copy(viewers = content.viewers.map { viewer ->
                if (viewer.id == event.viewerId) {
                    val updatedFilters = viewer.filters.updateFilters(event.filterUpdates)
                    val updatedSorts = viewer.sorts.updateSorts(event.sortUpdates)
                    val updatedRelations =
                        viewer.viewerRelations.updateViewerRelations(event.relationUpdates)
                    val updatedViewer = viewer.updateFields(event.fields)
                        .copy(
                            filters = updatedFilters,
                            sorts = updatedSorts,
                            viewerRelations = updatedRelations
                        )
                    updatedViewer
                } else {
                    viewer
                }
            })
        }
        return when (state) {
            is ObjectState.DataView.Collection -> state.updateBlockContent(
                target = event.block,
                blockContentUpdate = updateBlockContent
            )
            is ObjectState.DataView.Set -> state.updateBlockContent(
                target = event.block,
                blockContentUpdate = updateBlockContent
            )
            else -> state
        }
    }

    /**
     * @see Command.Details.Set
     */
    private fun handleSetDetails(
        state: ObjectState,
        event: Command.Details.Set
    ): ObjectState {
        return when (state) {
            is ObjectState.DataView.Collection -> state.copy(
                details = state.details.toMutableMap().apply {
                    put(event.target, event.details)
                }
            )
            is ObjectState.DataView.Set -> state.copy(
                details = state.details.toMutableMap().apply {
                    put(event.target, event.details)
                }
            )
            else -> state
        }
    }

    /**
     * @see Command.Details.Amend
     */
    private fun handleAmendDetails(
        state: ObjectState,
        event: Command.Details.Amend
    ): ObjectState {
        return when (state) {
            is ObjectState.DataView.Collection -> state.copy(
                details = state.details.amend(
                    target = event.target,
                    slice = event.details
                )
            )
            is ObjectState.DataView.Set -> state.copy(
                details = state.details.amend(
                    target = event.target,
                    slice = event.details
                )
            )
            else -> state
        }
    }

    /**
     * @see Command.Details.Unset
     */
    private fun handleUnsetDetails(
        state: ObjectState,
        event: Command.Details.Unset
    ): ObjectState {
        return when (state) {
            is ObjectState.DataView.Collection -> state.copy(
                details = state.details.unset(
                    target = event.target,
                    keys = event.keys
                )
            )
            is ObjectState.DataView.Set -> state.copy(
                details = state.details.unset(
                    target = event.target,
                    keys = event.keys
                )
            )
            else -> state
        }
    }

    /**
     * @see Command.UpdateStructure
     */
    private fun handleUpdateStructure(
        state: ObjectState,
        event: Command.UpdateStructure
    ): ObjectState {
        return when (state) {
            is ObjectState.DataView.Collection -> state.copy(
                blocks = state.blocks.replace(
                    replacement = { target -> target.copy(children = event.children) },
                    target = { block -> block.id == event.id }
                )
            )
            is ObjectState.DataView.Set -> state.copy(
                blocks = state.blocks.replace(
                    replacement = { target -> target.copy(children = event.children) },
                    target = { block -> block.id == event.id }
                )
            )
            else -> state
        }
    }

    /**
     * @see Command.AddBlock
     */
    private fun handleAddBlock(state: ObjectState, event: Command.AddBlock): ObjectState {
        return when (state) {
            is ObjectState.DataView.Collection -> state.copy(blocks = state.blocks + event.blocks)
            is ObjectState.DataView.Set -> state.copy(blocks = state.blocks + event.blocks)
            ObjectState.Init -> state
            ObjectState.ErrorLayout -> state
        }
    }
    //endregion

    private inline fun ObjectState.DataView.updateBlockContent(
        target: Id,
        blockContentUpdate: (Block.Content.DataView) -> Block.Content.DataView
    ): ObjectState {
        val updatedBlocks = blocks.map { block ->
            val content = block.content
            if (block.id == target && content is Block.Content.DataView) {
                block.copy(content = blockContentUpdate(content))
            } else {
                block
            }
        }
        return when (this) {
            is ObjectState.DataView.Collection -> copy(blocks = updatedBlocks)
            is ObjectState.DataView.Set -> copy(blocks = updatedBlocks)
        }
    }

    private fun List<DVViewer>.updateOrAdd(event: Command.DataView.SetView): List<DVViewer> {
        val result = map { viewer ->
            if (viewer.id == event.viewerId) {
                event.viewer
            } else {
                viewer
            }
        }.toMutableList()

        if (result.none { it.id == event.viewerId }) {
            result.add(event.viewer)
        }
        return result
    }

    data class Transformation(
        val state: ObjectState,
        val effects: List<StateSideEffect> = emptyList()
    ) {
        companion object {
            fun init() = Transformation(ObjectState.Init, emptyList())
        }
    }

    override fun clear() {
        eventChannel.close()
        state.value = ObjectState.Init
        _effects.tryEmit(emptyList())
    }
}