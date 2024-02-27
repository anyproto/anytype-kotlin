package com.anytypeio.anytype.domain.multiplayer

import com.anytypeio.anytype.core_models.primitives.Id
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository

class DeclineSpaceJoinRequest(
    private val repo: BlockRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<DeclineSpaceJoinRequest.Params, Unit>(dispatchers.io) {

    override suspend fun doWork(params: Params) {
        TODO("Not yet implemented")
    }

    data class Params(
        val space: SpaceId,
        val identity: Id
    )
}