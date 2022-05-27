package com.anytypeio.anytype.presentation.relations.providers

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.presentation.editor.editor.Store
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DefaultObjectRelationProvider(
    private val relations: Store.Relations
) : ObjectRelationProvider {

    override fun get(relation: Id): Relation {
        return relations.current().find(relation)
    }

    override fun observeAll(): Flow<List<Relation>> {
        return relations.stream()
    }

    override fun observe(relationId: Id): Flow<Relation> {
        return observeAll().map { relations ->
            relations.find(relationId)
        }
    }

    private fun List<Relation>.find(id: Id): Relation {
        return single { it.key == id }
    }
}