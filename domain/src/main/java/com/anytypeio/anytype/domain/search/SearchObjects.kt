package com.anytypeio.anytype.domain.search

import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVSort
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.block.repo.BlockRepository

class SearchObjects(
    private val repo: BlockRepository
) : BaseUseCase<List<ObjectWrapper.Basic>, SearchObjects.Params>() {

    override suspend fun run(params: Params) = safe {
        repo.searchObjects(
            sorts = params.sorts,
            filters = params.filters,
            fulltext = params.fulltext,
            offset = params.offset,
            limit = params.limit,
            keys = params.keys
        ).map { response ->
            ObjectWrapper.Basic(response)
        }
    }

    data class Params(
        val sorts: List<DVSort> = emptyList(),
        val filters: List<DVFilter> = emptyList(),
        val fulltext: String = EMPTY_TEXT,
        val offset: Int = INIT_OFFSET,
        val limit: Int = LIMIT,
        val keys: List<Id> = emptyList()
    )

    companion object {
        const val EMPTY_TEXT = ""
        const val LIMIT = 1000
        const val INIT_OFFSET = 0
    }
}