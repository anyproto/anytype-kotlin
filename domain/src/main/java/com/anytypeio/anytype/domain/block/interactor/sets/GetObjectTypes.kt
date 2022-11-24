package com.anytypeio.anytype.domain.block.interactor.sets

import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVSort
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.domain.base.CacheUseCase
import com.anytypeio.anytype.domain.block.repo.BlockRepository

class GetObjectTypes(
    private val repo: BlockRepository
) : CacheUseCase<List<ObjectWrapper.Type>, GetObjectTypes.Params>() {

    override suspend fun run(
        params: Params
    ) = safe {
        repo.searchObjects(
            keys = params.keys,
            filters = params.filters,
            sorts = params.sorts,
            limit = params.limit,
            offset = params.offset,
            fulltext = ""
        ).map { struct ->
            ObjectWrapper.Type(struct)
        }
    }

    data class Params(
        val sorts: List<DVSort>,
        val filters: List<DVFilter>,
        val offset: Int = 0,
        val limit: Int = 0,
        val keys: List<Key>
    )
}