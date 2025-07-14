package com.anytypeio.anytype.domain.vault

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import javax.inject.Inject

class ReorderPinnedSpaces @Inject constructor(
    private val repository: BlockRepository,
    dispatchers: AppCoroutineDispatchers
): ResultInteractor<ReorderPinnedSpaces.Params, Unit>(dispatchers.io) {

    data class Params(
        val movedSpaceId: Id,
        val newOrder: List<Id>
    )

    override suspend fun doWork(params: Params): Unit {
        // Use the spaceSetOrder method with the complete reordered list
        repository.spaceSetOrder(
            spaceViewId = params.movedSpaceId,
            spaceViewOrder = params.newOrder
        )
    }
} 