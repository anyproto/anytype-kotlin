package com.anytypeio.anytype.domain.multiplayer

import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import javax.inject.Inject

class ApproveLeaveSpaceRequest @Inject constructor(
    private val repo: BlockRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<SpaceId, Unit>(dispatchers.io) {

    override suspend fun doWork(params: SpaceId) {
        repo.approveSpaceLeaveRequest(space = params)
    }
}