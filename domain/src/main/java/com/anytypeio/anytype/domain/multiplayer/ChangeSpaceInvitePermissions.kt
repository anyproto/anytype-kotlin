package com.anytypeio.anytype.domain.multiplayer

import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.multiplayer.SpaceMemberPermissions
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import javax.inject.Inject

/**
 * Use case for changing space invite link permissions
 * This is used for Editor ↔ Viewer transitions without regenerating the link
 */
class ChangeSpaceInvitePermissions @Inject constructor(
    dispatchers: AppCoroutineDispatchers,
    private val repo: BlockRepository
) : ResultInteractor<ChangeSpaceInvitePermissions.Params, Unit>(dispatchers.io) {

    override suspend fun doWork(params: Params) {
        val command = Command.SpaceChangeInvite(
            space = params.space,
            permissions = params.permissions
        )
        repo.spaceChangeInvite(command)
    }

    data class Params(
        val space: SpaceId,
        val permissions: SpaceMemberPermissions
    )
}