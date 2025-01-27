package com.anytypeio.anytype.presentation.relations.providers

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.presentation.editor.Editor
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

class DefaultObjectRelationProvider(
    private val storeOfRelations: StoreOfRelations,
    private val storage: Editor.Storage
) : ObjectRelationProvider {

    override suspend fun getOrNull(relation: Key): ObjectWrapper.Relation? {
        return storeOfRelations.getByKey(relation)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun observeAll(id: Id): Flow<List<ObjectWrapper.Relation>> =
        combine(
            storeOfRelations.trackChanges(),
            storage.details.stream()
        ) { _, details ->
            details
        }.flatMapLatest { details ->
            val objectKeys = details.details[id]?.keys.orEmpty()
            flow {
                objectKeys.mapNotNull {
                    storeOfRelations.getByKey(it)
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