package com.anytypeio.anytype.domain.multiplayer

import com.anytypeio.anytype.core_models.multiplayer.InviteType
import com.anytypeio.anytype.core_models.multiplayer.SpaceInviteLink
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
            mapInviteToAccessLevel(result)
        } catch (e: Exception) {
            // No active invite link found or error occurred
            logger.logException(e, "GetCurrentInviteAccessLevel")
            SpaceInviteLinkAccessLevel.LinkDisabled
        }
    }

    /**
     * Maps middleware invite type and permissions to SpaceInviteLinkAccessLevel
     * Based on iOS SpaceRichInviteType mapping logic
     */
    private fun mapInviteToAccessLevel(
        spaceInviteLink: SpaceInviteLink
    ): SpaceInviteLinkAccessLevel {
        return when (spaceInviteLink.inviteType) {
            InviteType.MEMBER -> {
                // MEMBER type = request access (manual approval)
                SpaceInviteLinkAccessLevel.RequestAccess(spaceInviteLink.scheme)
            }

            InviteType.GUEST -> {
                // GUEST type is not supported in our current UI
                SpaceInviteLinkAccessLevel.LinkDisabled
            }

            InviteType.WITHOUT_APPROVE -> {
                // WITHOUT_APPROVE type - check permissions to determine editor vs viewer
                when (spaceInviteLink.permissions) {
                    SpaceMemberPermissions.OWNER, SpaceMemberPermissions.WRITER -> SpaceInviteLinkAccessLevel.EditorAccess(
                        spaceInviteLink.scheme
                    )

                    SpaceMemberPermissions.READER -> SpaceInviteLinkAccessLevel.ViewerAccess(
                        spaceInviteLink.scheme
                    )

                    SpaceMemberPermissions.NO_PERMISSIONS -> SpaceInviteLinkAccessLevel.LinkDisabled
                }
            }
        }
    }

    data class Params(
        val space: SpaceId
    )
}