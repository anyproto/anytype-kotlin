package com.anytypeio.anytype.presentation.relations.providers

import com.anytypeio.anytype.presentation.editor.Editor
import com.anytypeio.anytype.core_models.ObjectViewDetails
import com.anytypeio.anytype.presentation.sets.state.ObjectState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map

interface RelationListProvider {

    val details: Flow<ObjectViewDetails>

    fun getDetails(): ObjectViewDetails

    class EditorRelationListProvider(
        private val storage: Editor.Storage
    ) : RelationListProvider {
        override val details: Flow<ObjectViewDetails>
            get() = storage.details.stream()

        override fun getDetails() = storage.details.current()
    }

    class ObjectSetRelationListProvider(
        private val objectStates: StateFlow<ObjectState>
    ) : RelationListProvider {

        override val details = objectStates.map { state ->
            mapDetails(state)
        }

        override fun getDetails(): ObjectViewDetails = mapDetails(objectStates.value)

        private fun mapDetails(state: ObjectState) = when (state) {
            is ObjectState.DataView.Collection -> {
                state.details
            }

            is ObjectState.DataView.Set -> {
                state.details
            }

            else -> ObjectViewDetails.EMPTY
        }
    }
}