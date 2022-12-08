package com.anytypeio.anytype.domain.relations

import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVSort
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.block.repo.BlockRepository

class GetRelations(
    private val repo: BlockRepository
) : BaseUseCase<List<ObjectWrapper.Relation>, GetRelations.Params>() {

    override suspend fun run(params: Params) = safe { proceedWithUseCase(params) }

    suspend fun execute(params: Params) : List<ObjectWrapper.Relation> {
        return proceedWithUseCase(params)
    }

    private suspend fun proceedWithUseCase(params: Params) = repo.searchObjects(
        keys = params.keys,
        filters = params.filters,
        sorts = params.sorts,
        limit = params.limit,
        offset = params.offset,
        fulltext = params.query
    ).map { struct ->
        ObjectWrapper.Relation(struct)
    }

    data class Params(
        val sorts: List<DVSort> = emptyList(),
        val filters: List<DVFilter> = emptyList(),
        val keys: List<Key> = emptyList(),
        val offset: Int = 0,
        val limit: Int = 0,
        val query: String = ""
    )
}