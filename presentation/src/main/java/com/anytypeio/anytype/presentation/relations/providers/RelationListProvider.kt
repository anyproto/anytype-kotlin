package com.anytypeio.anytype.presentation.relations.providers

import com.anytypeio.anytype.presentation.editor.Editor
import com.anytypeio.anytype.core_models.AllObjectsDetails
import com.anytypeio.anytype.presentation.sets.state.ObjectState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map

interface RelationListProvider {

    val details: Flow<AllObjectsDetails>

    fun getDetails(): AllObjectsDetails

    class EditorRelationListProvider(
        private val storage: Editor.Storage
    ) : RelationListProvider {
        override val details: Flow<AllObjectsDetails>
            get() = storage.details.stream()

        override fun getDetails() = storage.details.current()
    }

    class ObjectSetRelationListProvider(
        private val objectStates: StateFlow<ObjectState>
    ) : RelationListProvider {

        override val details = objectStates.map { state ->
            mapDetails(state)
        }

        override fun getDetails(): AllObjectsDetails = mapDetails(objectStates.value)

        private fun mapDetails(state: ObjectState) = when (state) {
            is ObjectState.DataView.Collection -> {
                state.details
            }

            is ObjectState.DataView.Set -> {
                state.details
            }

            else -> AllObjectsDetails.EMPTY
        }
    }
}