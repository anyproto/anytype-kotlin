package com.anytypeio.anytype.presentation.relations.providers

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.StubRelation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

internal class FakeObjectRelationProvider(
    var relation: Relation = StubRelation()
) : ObjectRelationProvider {

    override fun get(relation: Id): Relation {
        return this.relation
    }

    override fun subscribe(relationId: Id): Flow<Relation> {
        return flow {
            emit(relation)
        }
    }
}