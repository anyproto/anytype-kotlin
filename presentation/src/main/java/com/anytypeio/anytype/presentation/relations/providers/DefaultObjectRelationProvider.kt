package com.anytypeio.anytype.presentation.relations.providers

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.presentation.editor.Editor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull

class DefaultObjectRelationProvider(
    private val storeOfRelations: StoreOfRelations,
    private val storage: Editor.Storage
) : ObjectRelationProvider {

    override suspend fun getById(relation: Id): ObjectWrapper.Relation {
        return storeOfRelations.getById(relation) ?: throw IllegalStateException("Could not find relation by id: $relation")
    }

    override suspend fun get(relation: Key): ObjectWrapper.Relation {
        return storeOfRelations.getByKey(relation) ?: throw IllegalStateException("Could not find relation by id: $relation")
    }

    override fun observeAll(): Flow<List<ObjectWrapper.Relation>> {
        return storage.relationLinks.stream().map { links ->
            links.mapNotNull { relationLink ->
                storeOfRelations.getByKey(relationLink.key)
            }
        }
    }

    override fun observe(relation: Key): Flow<ObjectWrapper.Relation> {
        return observeAll().mapNotNull { relations ->
            relations.find { it.key == relation }
        }
    }
}