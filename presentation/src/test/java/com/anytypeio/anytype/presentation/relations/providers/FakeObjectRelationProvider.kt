package com.anytypeio.anytype.presentation.relations.providers

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.StubRelationObject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

internal class FakeObjectRelationProvider : ObjectRelationProvider {

    private var relations: List<ObjectWrapper.Relation>

    var relation: ObjectWrapper.Relation = StubRelationObject()
        set(value) {
            relations = listOf(value)
        }

    constructor(relations: List<ObjectWrapper.Relation>) {
        this.relations = relations
    }

    constructor(relation: ObjectWrapper.Relation = StubRelationObject()) : this(listOf(relation))

    override suspend fun get(relation: Key): ObjectWrapper.Relation {
        return relations.first { it.key == relation }
    }

    override suspend fun getById(relation: Id): ObjectWrapper.Relation {
        return relations.first { it.id == relation }
    }

    override fun observe(relation: Key): Flow<ObjectWrapper.Relation> {
        return flow {
            emit(get(relation))
        }
    }

    override fun observeAll(): Flow<List<ObjectWrapper.Relation>> {
        return flow {
            emit(relations)
        }
    }
}