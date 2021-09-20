package com.anytypeio.anytype.presentation.relations.providers

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.presentation.relations.ObjectSetConfig
import com.anytypeio.anytype.presentation.sets.ObjectSet
import com.anytypeio.anytype.presentation.sets.ObjectSetSession
import com.anytypeio.anytype.presentation.sets.viewerById
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

class DataViewObjectValueProvider(
    private val objectSetState: StateFlow<ObjectSet>,
    private val session: ObjectSetSession
) : ObjectValueProvider {
    override fun get(target: Id): Map<String, Any?> {
        val state = objectSetState.value
        val viewer = state.viewerById(session.currentViewerId).id
        val records = state.viewerDb[viewer]
        checkNotNull(records) { "Could not found records for: $viewer" }
        return records.records.first { it[ObjectSetConfig.ID_KEY] == target }
    }

    override fun subscribe(target: Id) = objectSetState
        .filter { state -> state.isInitialized }
        .map { state ->
            val viewer = state.viewerById(session.currentViewerId).id
            val records = state.viewerDb[viewer]
            checkNotNull(records) { "Could not found records for: $viewer" }
            records.records.first { it[ObjectSetConfig.ID_KEY] == target }
        }
}