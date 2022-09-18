package com.anytypeio.anytype.domain.search

import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVSort
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.SearchResult
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.block.repo.BlockRepository

class SearchObjectsWithSubscription(
    private val repo: BlockRepository
) : BaseUseCase<SearchResult, SearchObjectsWithSubscription.Params>() {

    override suspend fun run(params: Params) = safe {
        repo.searchObjectsWithSubscription(
            subscription = params.subscription,
            sorts = params.sorts,
            filters = params.filters,
            keys = params.keys,
            offset = params.offset,
            limit = params.limit,
            afterId = params.afterId,
            beforeId = params.beforeId,
            source = params.source
        )
    }

    data class Params(
        val subscription: Id,
        val sorts: List<DVSort> = emptyList(),
        val filters: List<DVFilter> = emptyList(),
        val fulltext: String = EMPTY_TEXT,
        val keys: List<String>,
        val source: List<String> = emptyList(),
        val offset: Long = INIT_OFFSET,
        val limit: Int = LIMIT,
        val beforeId: Id?,
        val afterId: Id?,
    )

    companion object {
        const val EMPTY_TEXT = ""
        const val LIMIT = 1000
        const val INIT_OFFSET = 0L
    }
}