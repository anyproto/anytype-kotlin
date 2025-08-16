package com.anytypeio.anytype.domain.multiplayer

import com.anytypeio.anytype.core_models.multiplayer.SpaceInviteLinkAccessLevel
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import javax.inject.Inject

/**
 * Updates the space invite link access level.
 * Simplified use case - complex orchestration logic moved to ViewModel.
 */
class UpdateSpaceInviteLinkAccess @Inject constructor(
    dispatchers: AppCoroutineDispatchers,
    private val changeSpaceInvitePermissions: ChangeSpaceInvitePermissions
) : ResultInteractor<UpdateSpaceInviteLinkAccess.Params, Unit>(dispatchers.io) {

    override suspend fun doWork(params: Params): Unit {
        val (_, permissions) = params.newLevel.toInviteTypeAndPermissions() 
            ?: throw IllegalStateException("Cannot get permissions for level: ${params.newLevel}")
        
        changeSpaceInvitePermissions.run(
            ChangeSpaceInvitePermissions.Params(
                space = params.space,
                permissions = permissions!!
            )
        )
    }

    data class Params(
        val space: SpaceId,
        val newLevel: SpaceInviteLinkAccessLevel
    )
}