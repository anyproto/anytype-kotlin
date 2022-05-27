package com.anytypeio.anytype.presentation.relations.providers

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.StubRelation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

internal class FakeObjectRelationProvider : ObjectRelationProvider {

    private var relations: List<Relation>

    var relation: Relation = StubRelation()
        set(value) {
            relations = listOf(value)
        }

    constructor(relations: List<Relation>) {
        this.relations = relations
    }

    constructor(relation: Relation = StubRelation()) : this(listOf(relation))

    override fun get(relation: Id): Relation {
        return relations.first { it.key == relation }
    }

    override fun observe(relationId: Id): Flow<Relation> {
        return flow {
            emit(get(relationId))
        }
    }

    override fun observeAll(): Flow<List<Relation>> {
        return flow {
            emit(relations)
        }
    }
}