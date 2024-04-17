package com.anytypeio.anytype.domain.`object`

import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import javax.inject.Inject

class FetchObject @Inject constructor(
    private val repo: BlockRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<FetchObject.Params, ObjectWrapper.Basic?>(dispatchers.io) {

    override suspend fun doWork(params: Params): ObjectWrapper.Basic? {
        val result = repo.searchObjects(
            filters = buildList {
                add(
                    DVFilter(
                        relation = Relations.ID,
                        value = params.obj,
                        condition = DVFilterCondition.EQUAL
                    )
                )
            },
            limit = 1,
            keys = params.keys
        )
        return if (result.isNotEmpty() && result.first().isNotEmpty()) {
            ObjectWrapper.Basic(result.first())
        } else {
            null
        }
    }

    data class Params(
        val obj: Id,
        val keys: List<Key> = listOf(
            Relations.ID,
            Relations.SPACE_ID,
            Relations.LAYOUT,
            Relations.IS_ARCHIVED,
            Relations.IS_DELETED,
            Relations.IS_HIDDEN
        )
    )
}