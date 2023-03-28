package com.anytypeio.anytype.presentation.relations.providers

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.presentation.sets.state.ObjectState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull

class DataViewObjectRelationProvider(
    private val objectState: StateFlow<ObjectState>,
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
        return objectState.filterIsInstance<ObjectState.DataView>().map { set ->
            set.dataViewContent.relationLinks.mapNotNull {
                storeOfRelations.getByKey(it.key)
            }
        }
    }

    override fun observe(relation: Key): Flow<ObjectWrapper.Relation> {
        return objectState
            .filterIsInstance<ObjectState.DataView>()
            .map { it.dataViewContent.relationLinks }
            .distinctUntilChanged()
            .mapNotNull { storeOfRelations.getByKey(relation) }
    }
}

class SetOrCollectionRelationProvider(
    private val objectState: StateFlow<ObjectState>,
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
        return objectState.map { set ->
            when (set) {
                is ObjectState.DataView.Collection -> {
                    set.objectRelationLinks.mapNotNull {
                        storeOfRelations.getByKey(it.key)
                    }
                }
                is ObjectState.DataView.Set ->
                    set.objectRelationLinks.mapNotNull {
                        storeOfRelations.getByKey(it.key)
                    }
                ObjectState.ErrorLayout -> emptyList()
                ObjectState.Init -> emptyList()
            }
        }
    }

    override fun observe(relation: Key): Flow<ObjectWrapper.Relation> {
        return objectState
            .filterIsInstance<ObjectState.DataView>()
            .map { it.dataViewContent.relationLinks }
            .distinctUntilChanged()
            .mapNotNull { storeOfRelations.getByKey(relation) }
    }
}