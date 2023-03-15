package com.anytypeio.anytype.presentation.relations.providers

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.RelationLink
import com.anytypeio.anytype.core_models.ext.content
import com.anytypeio.anytype.presentation.editor.Editor
import com.anytypeio.anytype.presentation.sets.state.ObjectState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map

interface RelationListProvider {

    val links: Flow<List<RelationLink>>
    val details: Flow<Block.Details>

    fun getLinks() : List<RelationLink>
    fun getDetails(): Block.Details

    class EditorRelationListProvider(
        private val storage: Editor.Storage
    ) : RelationListProvider {
        override val links: Flow<List<RelationLink>>
            get() = storage.relationLinks.stream()
        override val details: Flow<Block.Details>
            get() = storage.details.stream()

        override fun getLinks() = storage.relationLinks.current()
        override fun getDetails() = storage.details.current()
    }

    class DataViewRelationListProvider(
        private val objectStates: StateFlow<ObjectState>
    ) : RelationListProvider {

        override val links = objectStates.map {  state -> mapLinks(state) }
        override val details = objectStates.map {  state -> mapDetails(state) }

        override fun getLinks(): List<RelationLink> = mapLinks(objectStates.value)
        override fun getDetails(): Block.Details = mapDetails(objectStates.value)

        private fun mapDetails(state: ObjectState) = when (state) {
                is ObjectState.DataView.Collection -> {
                    Block.Details(state.details)
                }
                is ObjectState.DataView.Set -> {
                    Block.Details(state.details)
                }
                else -> Block.Details(emptyMap())
        }

        private fun mapLinks(state: ObjectState) = when (state) {
            is ObjectState.DataView.Collection -> {
                state.dataViewBlock.content<Block.Content.DataView>().relationLinks
            }
            is ObjectState.DataView.Set -> {
                state.dataViewBlock.content<Block.Content.DataView>().relationLinks
            }
            else -> emptyList()
        }
    }
}