package com.anytypeio.anytype.domain.dashboard.interactor

import com.anytypeio.anytype.core_models.*
import com.anytypeio.anytype.domain.`object`.ObjectTypes
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.relations.Relations

/**
 * Request for searching unarchived object sets
 */
class SearchObjectSets(
    private val repo: BlockRepository
) : BaseUseCase<List<Map<String, Any?>>, Unit>() {

    override suspend fun run(params: Unit) = safe {
        val sorts = listOf(
            DVSort(
                relationKey = Relations.LAST_OPENED_DATE,
                type = DVSortType.DESC
            )
        )

        val filters = listOf(
            DVFilter(
                condition = DVFilterCondition.EQUAL,
                value = false,
                relationKey = Relations.IS_ARCHIVED,
                operator = DVFilterOperator.AND
            ),
            DVFilter(
                condition = DVFilterCondition.EQUAL,
                value = ObjectTypes.SET,
                relationKey = Relations.TYPE,
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
        private const val LIMIT = 100
        private const val INIT_OFFSET = 0
    }
}