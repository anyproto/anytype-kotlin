package com.anytypeio.anytype.domain.multiplayer

import com.anytypeio.anytype.core_models.multiplayer.InviteType
import com.anytypeio.anytype.core_models.multiplayer.SpaceInviteLinkAccessLevel
import com.anytypeio.anytype.core_models.multiplayer.SpaceMemberPermissions
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.debugging.Logger
import javax.inject.Inject

/**
 * Use case to determine the current invite access level from middleware
 * Uses spaceInviteGetCurrent to get detailed invite information including type and permissions
 */
class GetCurrentInviteAccessLevel @Inject constructor(
    dispatchers: AppCoroutineDispatchers,
    private val repo: BlockRepository,
    private val logger: Logger
) : ResultInteractor<GetCurrentInviteAccessLevel.Params, SpaceInviteLinkAccessLevel>(dispatchers.io) {

    override suspend fun doWork(params: Params): SpaceInviteLinkAccessLevel {
        return try {
            val result = repo.getSpaceInviteLink(params.space)
            mapInviteToAccessLevel(
                inviteType = result.inviteType,
                permissions = result.permissions
            )
        } catch (e: Exception) {
            // No active invite link found or error occurred
            logger.logException(e, "GetCurrentInviteAccessLevel")
            SpaceInviteLinkAccessLevel.LINK_DISABLED
        }
    }
    
    /**
     * Maps middleware invite type and permissions to SpaceInviteLinkAccessLevel
     */
    private fun mapInviteToAccessLevel(
        inviteType: InviteType, 
        permissions: SpaceMemberPermissions?
    ): SpaceInviteLinkAccessLevel {
        return when (inviteType) {
            InviteType.MEMBER -> {
                SpaceInviteLinkAccessLevel.REQUEST_ACCESS
            }
            InviteType.GUEST -> {
                // GUEST type requires approval (request access)
                SpaceInviteLinkAccessLevel.LINK_DISABLED
            }
            InviteType.WITHOUT_APPROVE -> {
                // WITHOUT_APPROVE type - need to check permissions
                when (permissions) {
                    SpaceMemberPermissions.WRITER -> SpaceInviteLinkAccessLevel.EDITOR_ACCESS
                    SpaceMemberPermissions.READER -> SpaceInviteLinkAccessLevel.VIEWER_ACCESS
                    SpaceMemberPermissions.OWNER -> SpaceInviteLinkAccessLevel.LINK_DISABLED
                    SpaceMemberPermissions.NO_PERMISSIONS -> SpaceInviteLinkAccessLevel.LINK_DISABLED
                    null -> SpaceInviteLinkAccessLevel.LINK_DISABLED
                }
            }
        }
    }

    data class Params(
        val space: SpaceId
    )
}