package com.anytypeio.anytype.presentation.relations.providers

import com.anytypeio.anytype.core_models.DV
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.presentation.sets.ObjectSet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map

class DataViewObjectRelationProvider(
    private val objectSetState: StateFlow<ObjectSet>,
) : ObjectRelationProvider {
    override fun get(relation: Id): Relation {
        val state = objectSetState.value
        val block = state.dataview
        val dv = block.content as DV
        return dv.relations.first { it.key == relation }
    }

    override fun subscribe(relation: Id): Flow<Relation> {
        return objectSetState.map { state ->
            val block = state.dataview
            val dv = block.content as DV
            dv.relations.first { it.key == relation }
        }
    }
}