package com.anytypeio.anytype.domain.search

import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVSort
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.block.repo.BlockRepository

class SearchObjectsWithSubscription(
    private val repo: BlockRepository
) : BaseUseCase<List<ObjectWrapper.Basic>, SearchObjectsWithSubscription.Params>() {

    override suspend fun run(params: Params) = safe {
        repo.searchObjects(
            sorts = params.sorts,
            filters = params.filters,
            fulltext = params.fulltext,
            offset = params.offset,
            limit = params.limit
        ).map { response ->
            ObjectWrapper.Basic(response)
        }
    }

    data class Params(
        val subscription: Id,
        val sorts: List<DVSort> = emptyList(),
        val filters: List<DVFilter> = emptyList(),
        val fulltext: String = EMPTY_TEXT,
        val keys: List<String>,
        val offset: Int = INIT_OFFSET,
        val limit: Int = LIMIT,
        val beforeId: Id?,
        val afterId: Id?,
    )

    companion object {
        const val EMPTY_TEXT = ""
        const val LIMIT = 1000
        const val INIT_OFFSET = 0
    }
}