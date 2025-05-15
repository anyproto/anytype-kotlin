package com.anytypeio.anytype.domain.multiplayer

import com.anytypeio.anytype.core_models.multiplayer.InviteType
import com.anytypeio.anytype.core_models.multiplayer.SpaceInviteLink
import com.anytypeio.anytype.core_models.multiplayer.SpaceMemberPermissions
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import javax.inject.Inject

class GenerateSpaceInviteLink @Inject constructor(
    dispatchers: AppCoroutineDispatchers,
    private val repo: BlockRepository
) : ResultInteractor<GenerateSpaceInviteLink.Params, SpaceInviteLink>(dispatchers.io) {

    override suspend fun doWork(params: Params): SpaceInviteLink {
        return repo.generateSpaceInviteLink(
            space = params.space,
            inviteType = params.inviteType,
            permissions = params.permissions
        )
    }

    data class Params(
        val space: SpaceId,
        val inviteType: InviteType,
        val permissions: SpaceMemberPermissions
    )
}