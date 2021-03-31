package com.anytypeio.anytype.presentation.relations.providers

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.presentation.page.editor.Store
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DefaultObjectRelationProvider(
    private val relations: Store.Relations
) : ObjectRelationProvider {
    override fun get(relation: Id): Relation {
        return relations.current().first { it.key == relation }
    }

    override fun subscribe(relation: Id): Flow<Relation> {
        return relations.stream().map { relations ->
            relations.first { it.key == relation }
        }
    }
}