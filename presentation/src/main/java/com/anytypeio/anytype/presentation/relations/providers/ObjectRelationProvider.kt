package com.anytypeio.anytype.presentation.relations.providers

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Relation
import kotlinx.coroutines.flow.Flow

interface ObjectRelationProvider {
    fun get(relation: Id): Relation
    fun observe(relationId: Id): Flow<Relation>
    fun observeAll(): Flow<List<Relation>>
}