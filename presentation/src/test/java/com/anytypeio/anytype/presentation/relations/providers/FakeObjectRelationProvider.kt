package com.anytypeio.anytype.presentation.relations.providers

import com.anytypeio.anytype.core_models.Id
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

    override suspend fun observeAll(id: Id): Flow<List<ObjectWrapper.Relation>> {
        return flow {
            emit(relations)
        }
    }
}