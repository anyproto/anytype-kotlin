package com.anytypeio.anytype.domain.dataview.interactor

import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVSort
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.block.repo.BlockRepository

class SearchObjects(
    private val repo: BlockRepository
) : BaseUseCase<List<Map<String, Any?>>, SearchObjects.Params>() {

    override suspend fun run(params: Params): Either<Throwable, List<Map<String, Any?>>> = safe {
        repo.searchObjects(
            sorts = params.sorts,
            filters = params.filters,
            fulltext = params.fulltext,
            offset = params.offset,
            limit = params.limit
        )
    }

    data class Params(
        val sorts: List<DVSort> = emptyList(),
        val filters: List<DVFilter> = emptyList(),
        val fulltext: String = EMPTY_TEXT,
        val offset: Int = INIT_OFFSET,
        val limit: Int = LIMIT
    )

    companion object {
        const val EMPTY_TEXT = ""
        const val LIMIT = 1000
        const val INIT_OFFSET = 0
    }
}