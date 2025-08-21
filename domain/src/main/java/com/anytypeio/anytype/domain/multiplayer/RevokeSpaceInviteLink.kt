package com.anytypeio.anytype.domain.multiplayer

import com.anytypeio.anytype.core_models.multiplayer.SpaceInviteLinkAccessLevel
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.invite.SpaceInviteLinkStore
import javax.inject.Inject

class RevokeSpaceInviteLink @Inject constructor(
    private val repo: BlockRepository,
    private val store: SpaceInviteLinkStore,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<SpaceId, Unit>(dispatchers.io) {
    override suspend fun doWork(params: SpaceId) {
        val result = repo.revokeSpaceInviteLink(space = params)

        // Update the store to reflect that the invite link is now disabled
        store.update(params, SpaceInviteLinkAccessLevel.LinkDisabled)

        return result
    }
}