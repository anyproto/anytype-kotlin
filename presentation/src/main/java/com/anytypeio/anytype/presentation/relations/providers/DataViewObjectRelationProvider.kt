package com.anytypeio.anytype.presentation.relations.providers

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.presentation.sets.ObjectSet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull

class DataViewObjectRelationProvider(
    private val objectSetState: StateFlow<ObjectSet>,
    private val storeOfRelations: StoreOfRelations
) : ObjectRelationProvider {

    override suspend fun get(relation: Key): ObjectWrapper.Relation {
        return storeOfRelations.getByKey(relation)
            ?: throw IllegalStateException("Could not found relation by key: $relation")
    }

    override suspend fun getById(relation: Id): ObjectWrapper.Relation {
        return storeOfRelations.getById(relation)
            ?: throw IllegalStateException("Could not find relation by id: $relation")
    }

    override fun observeAll(): Flow<List<ObjectWrapper.Relation>> {
        return objectSetState.filter { it.isInitialized }.map { set ->
            set.dv.relationsIndex.mapNotNull {
                storeOfRelations.getByKey(it.key)
            }
        }
    }

    override fun observe(relation: Key): Flow<ObjectWrapper.Relation> {
        return objectSetState
            .filter { it.isInitialized }
            .map { it.dv.relationsIndex }
            .distinctUntilChanged()
            .mapNotNull { storeOfRelations.getByKey(relation) }
    }
}