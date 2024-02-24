package com.anytypeio.anytype.domain.multiplayer

import com.anytypeio.anytype.core_models.multiplayer.SpaceInviteLink
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import javax.inject.Inject

class GenerateSpaceInviteLink @Inject constructor(
    private val dispatchers: AppCoroutineDispatchers,
    private val repo: BlockRepository
): ResultInteractor<SpaceId, SpaceInviteLink>(dispatchers.io) {
    override suspend fun doWork(params: SpaceId): SpaceInviteLink = repo.generateSpaceInviteLink(
        space = params
    )
}