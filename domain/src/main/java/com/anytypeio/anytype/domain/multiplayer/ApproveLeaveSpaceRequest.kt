package com.anytypeio.anytype.domain.multiplayer

import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import javax.inject.Inject

class ApproveLeaveSpaceRequest @Inject constructor(
    private val repo: BlockRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<ApproveLeaveSpaceRequest.Params, Unit>(dispatchers.io) {

    override suspend fun doWork(params: Params) {
        repo.approveSpaceLeaveRequest(
            command = Command.ApproveSpaceLeaveRequest(
                space = params.space,
                identities = params.identities
            )
        )
    }

    data class Params(
        val space: SpaceId,
        val identities: List<Id>
    )
}