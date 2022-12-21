package com.anytypeio.anytype.domain.block.interactor.sets

import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVSort
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository

class GetObjectTypes(
    private val repo: BlockRepository
) : ResultInteractor<GetObjectTypes.Params, List<ObjectWrapper.Type>>() {

    override suspend fun doWork(params: Params): List<ObjectWrapper.Type> {
        val result = repo.searchObjects(
            keys = params.keys,
            filters = params.filters,
            sorts = params.sorts,
            limit = params.limit,
            offset = params.offset,
            fulltext = params.query
        )
        return result.map { struct ->
            ObjectWrapper.Type(struct)
        }
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