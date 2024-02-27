package com.anytypeio.anytype.domain.multiplayer

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import javax.inject.Inject

class DeclineSpaceJoinRequest @Inject constructor(
    private val repo: BlockRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<DeclineSpaceJoinRequest.Params, Unit>(dispatchers.io) {

    override suspend fun doWork(params: Params) {
        repo.declineSpaceRequest(
            space = params.space,
            identity = params.identity
        )
    }

    /**
     * @property [identity] members identity
     * @see Relations.IDENTITY
     */
    data class Params(
        val space: SpaceId,
        val identity: Id
    )
}