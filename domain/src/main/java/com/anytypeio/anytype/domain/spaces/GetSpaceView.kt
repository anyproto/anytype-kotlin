package com.anytypeio.anytype.domain.spaces

import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import javax.inject.Inject

class GetSpaceView @Inject constructor(
    private val repo: BlockRepository,
    dispatchers: AppCoroutineDispatchers
): ResultInteractor<Id, ObjectWrapper.Basic?>(dispatchers.io) {
    override suspend fun doWork(params: Id): ObjectWrapper.Basic? {
        val result = repo.searchObjects(
            filters = buildList {
                add(
                    DVFilter(
                        relation = Relations.ID,
                        value = params,
                        condition = DVFilterCondition.EQUAL
                    )
                )
            },
            limit = 1
        ).firstOrNull()
        return if (result != null) {
            ObjectWrapper.Basic(result)
        } else {
            null
        }
    }
}