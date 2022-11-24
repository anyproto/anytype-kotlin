package com.anytypeio.anytype.presentation.relations.providers

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.ObjectWrapper
import kotlinx.coroutines.flow.Flow

interface ObjectRelationProvider {
    suspend fun get(relation: Key): ObjectWrapper.Relation
    suspend fun getById(relation: Id) : ObjectWrapper.Relation
    fun observe(relation: Key): Flow<ObjectWrapper.Relation>
    fun observeAll(): Flow<List<ObjectWrapper.Relation>>
}