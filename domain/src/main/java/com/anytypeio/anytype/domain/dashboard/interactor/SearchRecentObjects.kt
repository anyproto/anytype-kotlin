package com.anytypeio.anytype.domain.dashboard.interactor

import com.anytypeio.anytype.core_models.DVSort
import com.anytypeio.anytype.core_models.DVSortType
import com.anytypeio.anytype.core_models.ObjectTypeConst
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.core_models.Relations

/**
 * Request for searching recently opened objects, including object sets.
 */
class SearchRecentObjects(
    private val repo: BlockRepository
) : BaseUseCase<List<Map<String, Any?>>, Unit>() {

    override suspend fun run(params: Unit) = safe {
        val sorts = listOf(
            DVSort(
                relationKey = Relations.LAST_OPENED_DATE,
                type = DVSortType.DESC
            )
        )
        repo.searchObjects(
            sorts = sorts,
            filters = emptyList(),
            fulltext = EMPTY_TEXT,
            offset = INIT_OFFSET,
            limit = LIMIT,
            objectTypeFilter = listOf(ObjectTypeConst.SET, ObjectTypeConst.PAGE)
        )
    }

    companion object {
        private const val EMPTY_TEXT = ""
        private const val LIMIT = 30
        private const val INIT_OFFSET = 0
    }
}