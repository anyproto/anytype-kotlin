package com.anytypeio.anytype.domain.multiplayer

import com.anytypeio.anytype.core_models.multiplayer.InviteType
import com.anytypeio.anytype.core_models.multiplayer.SpaceInviteLink
import com.anytypeio.anytype.core_models.multiplayer.SpaceInviteLinkAccessLevel
import com.anytypeio.anytype.core_models.multiplayer.SpaceMemberPermissions
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import javax.inject.Inject

/**
 * Use case to determine the current invite access level from middleware
 * This will need to be implemented when we have access to invite type and permissions from the API
 */
class GetCurrentInviteAccessLevel @Inject constructor(
    dispatchers: AppCoroutineDispatchers,
    private val getSpaceInviteLink: GetSpaceInviteLink
) : ResultInteractor<GetCurrentInviteAccessLevel.Params, SpaceInviteLinkAccessLevel>(dispatchers.io) {

    override suspend fun doWork(params: Params): SpaceInviteLinkAccessLevel {
        return try {
            val invite = getSpaceInviteLink.async(params.space).getOrThrow()
            
            // TODO: Once middleware provides invite type and permissions in the response,
            // we can properly determine the access level here
            // For now, we assume EDITOR_ACCESS if a link exists
            // This should be updated to:
            // mapInviteToAccessLevel(invite.inviteType, invite.permissions)
            
            SpaceInviteLinkAccessLevel.EDITOR_ACCESS
            
        } catch (e: Exception) {
            // No active invite link found
            SpaceInviteLinkAccessLevel.LINK_DISABLED
        }
    }
    
    /**
     * TODO: Implement when middleware provides invite type and permissions
     */
    private fun mapInviteToAccessLevel(
        inviteType: InviteType, 
        permissions: SpaceMemberPermissions?
    ): SpaceInviteLinkAccessLevel {
        return when (inviteType) {
            InviteType.WITHOUT_APPROVE -> {
                when (permissions) {
                    SpaceMemberPermissions.WRITER -> SpaceInviteLinkAccessLevel.EDITOR_ACCESS
                    SpaceMemberPermissions.READER -> SpaceInviteLinkAccessLevel.VIEWER_ACCESS
                    else -> SpaceInviteLinkAccessLevel.VIEWER_ACCESS // default
                }
            }
            InviteType.MEMBER -> SpaceInviteLinkAccessLevel.REQUEST_ACCESS
            else -> SpaceInviteLinkAccessLevel.LINK_DISABLED
        }
    }

    data class Params(
        val space: SpaceId
    )
}