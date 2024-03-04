package com.anytypeio.anytype.domain.multiplayer

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import javax.inject.Inject

class SendJoinSpaceRequest @Inject constructor(
    private val repo: BlockRepository,
    dispatchers: AppCoroutineDispatchers
): ResultInteractor<SendJoinSpaceRequest.Params, Unit>(dispatchers.io) {

    override suspend fun doWork(params: Params) {
        repo.sendJoinSpaceRequest(
            space = params.space,
            network = params.network,
            inviteContentId = params.inviteContentId,
            inviteFileKey = params.inviteFileKey
        )
    }

    /**
     * @property [network] provide network id in case of the self-hosted configuration.
     */
    data class Params(
        val space: SpaceId,
        val network: Id?,
        val inviteContentId: Id,
        val inviteFileKey: Id
    )
}