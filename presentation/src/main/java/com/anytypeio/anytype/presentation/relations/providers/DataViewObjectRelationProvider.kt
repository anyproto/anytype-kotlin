package com.anytypeio.anytype.presentation.relations.providers

import com.anytypeio.anytype.core_models.DV
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.presentation.sets.ObjectSet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

class DataViewObjectRelationProvider(
    private val objectSetState: StateFlow<ObjectSet>,
) : ObjectRelationProvider {

    override fun observeAll(): Flow<List<Relation>> {
        return objectSetState
            .filter { state -> state.isInitialized }
            .map { state ->
                val block = state.dataview
                val dv = block.content as DV
                dv.relations
            }

    }

    override fun get(relation: Id): Relation {
        val state = objectSetState.value
        val block = state.dataview
        val dv = block.content as DV
        return dv.relations.first { it.key == relation }
    }

    override fun observe(relationId: Id): Flow<Relation> =
        observeAll().map { relations ->
            relations.single { relation -> relation.key == relationId }
        }
}