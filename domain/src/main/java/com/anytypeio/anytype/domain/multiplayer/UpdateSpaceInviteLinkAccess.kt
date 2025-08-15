package com.anytypeio.anytype.domain.multiplayer

import com.anytypeio.anytype.core_models.multiplayer.SpaceInviteLink
import com.anytypeio.anytype.core_models.multiplayer.SpaceInviteLinkAccessLevel
import com.anytypeio.anytype.core_models.multiplayer.SpaceMemberPermissions
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.base.getOrThrow
import javax.inject.Inject

/**
 * Updates the space invite link access level following iOS implementation logic:
 * - Editor ↔ Viewer: Uses optimized changeInvite API (preserves existing link)
 * - Any → Request/Disabled: Requires revokeInvite + generateInvite (new link generated)
 * - Disabled → Any: Make shareable first, then generate new invite
 */
class UpdateSpaceInviteLinkAccess @Inject constructor(
    dispatchers: AppCoroutineDispatchers,
    private val makeSpaceShareable: MakeSpaceShareable,
    private val revokeSpaceInviteLink: RevokeSpaceInviteLink,
    private val generateSpaceInviteLink: GenerateSpaceInviteLink,
    private val changeSpaceInvitePermissions: ChangeSpaceInvitePermissions,
    private val getSpaceInviteLink: GetSpaceInviteLink
) : ResultInteractor<UpdateSpaceInviteLinkAccess.Params, SpaceInviteLink?>(dispatchers.io) {

    override suspend fun doWork(params: Params): SpaceInviteLink? {
        val currentLevel = params.currentLevel
        val newLevel = params.newLevel
        val space = params.space
        
        // If setting to disabled, just revoke the invite
        if (newLevel == SpaceInviteLinkAccessLevel.LINK_DISABLED) {
            revokeSpaceInviteLink.async(space).getOrThrow()
            return null
        }
        
        // If current is disabled, make space shareable first
        if (currentLevel == SpaceInviteLinkAccessLevel.LINK_DISABLED) {
            makeSpaceShareable.async(space).getOrThrow()
        }
        
        // Use optimized changeInvite API for Editor ↔ Viewer transitions
        if (currentLevel.canUseChangeInviteApi(newLevel)) {
            val (_, permissions) = newLevel.toInviteTypeAndPermissions() 
                ?: throw IllegalStateException("Cannot get permissions for level: $newLevel")
            
            changeSpaceInvitePermissions.async(
                ChangeSpaceInvitePermissions.Params(
                    space = space,
                    permissions = permissions!!
                )
            ).getOrThrow()
            
            // Return the existing invite link (permissions changed but link remains the same)
            return getSpaceInviteLink.async(space).getOrThrow()
        }
        
        // For other transitions, use revoke + regenerate approach
        if (currentLevel != SpaceInviteLinkAccessLevel.LINK_DISABLED) {
            revokeSpaceInviteLink.async(space).getOrThrow()
        }
        
        // Generate new invite with appropriate settings
        val (inviteType, permissions) = newLevel.toInviteTypeAndPermissions() 
            ?: throw IllegalStateException("Cannot generate invite for disabled state")
        
        return generateSpaceInviteLink.async(
            GenerateSpaceInviteLink.Params(
                space = space,
                inviteType = inviteType,
                permissions = permissions ?: SpaceMemberPermissions.READER
            )
        ).getOrThrow()
    }

    data class Params(
        val space: SpaceId,
        val currentLevel: SpaceInviteLinkAccessLevel,
        val newLevel: SpaceInviteLinkAccessLevel
    )
}