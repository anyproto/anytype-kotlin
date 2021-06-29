package com.anytypeio.anytype.domain.dashboard.interactor

import com.anytypeio.anytype.core_models.*
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.relations.Relations

class SearchArchivedObjects(
    private val repo: BlockRepository
) : BaseUseCase<List<Map<String, Any?>>, Unit>() {

    override suspend fun run(params: Unit) = safe {
        val sorts = listOf(
            DVSort(
                relationKey = Relations.NAME,
                type = DVSortType.ASC
            )
        )
        val filters = listOf(
            DVFilter(
                condition = DVFilterCondition.EQUAL,
                value = true,
                relationKey = Relations.IS_ARCHIVED,
                operator = DVFilterOperator.AND
            )
        )
        repo.searchObjects(
            sorts = sorts,
            filters = filters,
            fulltext = EMPTY_TEXT,
            offset = INIT_OFFSET,
            limit = LIMIT
        )
    }

    companion object {
        private const val EMPTY_TEXT = ""
        private const val LIMIT = 1000
        private const val INIT_OFFSET = 0
    }
}