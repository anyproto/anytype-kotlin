package com.anytypeio.anytype.domain.vault

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import javax.inject.Inject

class PinSpace @Inject constructor(
    private val repo: BlockRepository,
    dispatchers: AppCoroutineDispatchers
): ResultInteractor<PinSpace.Params, Unit>(dispatchers.io) {

    data class Params(
        val spaceId: Id,
        val currentPinnedOrder: List<Id>
    )

    override suspend fun doWork(params: Params): Unit {
        // Filter out the space being pinned if it's already in the list
        val newOrder = params.currentPinnedOrder.filter { it != params.spaceId }.toMutableList()
        // Insert the space at the beginning (position 0)
        newOrder.add(0, params.spaceId)
        
        // Use the spaceSetOrder method with the complete new order
        repo.spaceSetOrder(
            spaceViewId = params.spaceId,
            spaceViewOrder = newOrder
        )
    }
} 