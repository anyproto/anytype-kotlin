package com.anytypeio.anytype.presentation.sets.state

import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.restrictions.ObjectRestriction
import com.anytypeio.anytype.presentation.objects.ObjectRestrictionProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow

interface ObjectStateReducer : ObjectRestrictionProvider {

    val state: MutableStateFlow<ObjectState>
    val effects: SharedFlow<List<StateSideEffect>>

    suspend fun run()
    suspend fun dispatch(events: List<Event>)
    fun reduce(state: ObjectState, events: List<Event>): DefaultObjectStateReducer.Transformation
    fun clear()

    override fun provide(): List<ObjectRestriction> {
        return when(val value = state.value) {
            is ObjectState.DataView.Collection -> value.objectRestrictions
            is ObjectState.DataView.Set -> value.objectRestrictions
            ObjectState.ErrorLayout -> emptyList()
            ObjectState.Init -> emptyList()
        }
    }
}