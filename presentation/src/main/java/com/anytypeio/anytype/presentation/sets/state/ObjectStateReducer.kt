package com.anytypeio.anytype.presentation.sets.state

import com.anytypeio.anytype.core_models.Event
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow

interface ObjectStateReducer {

    val state: MutableStateFlow<ObjectState>
    val effects: SharedFlow<List<StateSideEffect>>

    suspend fun run()
    suspend fun dispatch(events: List<Event>)
    suspend fun reduce(state: ObjectState, events: List<Event>): DefaultObjectStateReducer.Transformation
    fun clear()
}