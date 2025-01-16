package com.anytypeio.anytype.presentation.relations.providers

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.presentation.sets.state.ObjectState
import kotlin.collections.mapNotNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

class DataViewObjectRelationProvider(
    private val objectState: StateFlow<ObjectState>,
    private val storeOfRelations: StoreOfRelations
) : ObjectRelationProvider {

    override suspend fun getOrNull(relation: Key): ObjectWrapper.Relation? {
        return storeOfRelations.getByKey(relation)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun observeRelation(relation: Key): Flow<ObjectWrapper.Relation> {
        return storeOfRelations.trackChanges()
            .flatMapLatest { _ ->
                val relation = storeOfRelations.getByKey(relation)
                if (relation != null) {
                    flowOf(relation)
                } else {
                    emptyFlow()
                }
            }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun observeAll(id: Id): Flow<List<ObjectWrapper.Relation>> {
        return combine(
            storeOfRelations.trackChanges(),
            objectState
        ) { _, state ->
            state
        }.flatMapLatest { state ->
            when (state) {
                is ObjectState.DataView.Collection -> {
                    val objectKeys = state.dataViewContent.relationLinks.map { it.key }
                    flow {
                        objectKeys.mapNotNull {
                            storeOfRelations.getByKey(it)
                        }
                    }
                }
                is ObjectState.DataView.Set -> {
                    val objectKeys = state.dataViewContent.relationLinks.map { it.key }
                    flow {
                        objectKeys.mapNotNull {
                            storeOfRelations.getByKey(it)
                        }
                    }
                }
                else -> emptyFlow()
            }
        }
    }
}

class SetOrCollectionRelationProvider(
    private val objectState: StateFlow<ObjectState>,
    private val storeOfRelations: StoreOfRelations
) : ObjectRelationProvider {

    override suspend fun getOrNull(relation: Key): ObjectWrapper.Relation? {
        return storeOfRelations.getByKey(relation)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun observeAll(id: Id): Flow<List<ObjectWrapper.Relation>> {
        return combine(
            storeOfRelations.trackChanges(),
            objectState
        ) { _, state ->
            state
        }.flatMapLatest { state ->
            when (state) {
                is ObjectState.DataView.Collection -> {
                    val objectKeys = state.details[id]?.map?.keys.orEmpty()
                    flow {
                        objectKeys.mapNotNull {
                            storeOfRelations.getByKey(it)
                        }
                    }
                }
                is ObjectState.DataView.Set -> {
                    val objectKeys = state.details[id]?.map?.keys.orEmpty()
                    flow {
                        objectKeys.mapNotNull {
                            storeOfRelations.getByKey(it)
                        }
                    }
                }
                else -> emptyFlow()
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun observeRelation(relation: Key): Flow<ObjectWrapper.Relation> {
        return storeOfRelations.trackChanges()
            .flatMapLatest { _ ->
                val relation = storeOfRelations.getByKey(relation)
                if (relation != null) {
                    flowOf(relation)
                } else {
                    emptyFlow()
                }
            }
    }

}