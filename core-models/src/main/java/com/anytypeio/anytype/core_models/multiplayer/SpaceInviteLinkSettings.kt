package com.anytypeio.anytype.core_models.multiplayer

/**
 * Represents the access level for space invitation links as per Task #24
 */
enum class SpaceInviteLinkAccessLevel(val code: Int) {
    /**
     * Link is disabled - no active invitation link
     */
    LINK_DISABLED(0),
    
    /**
     * Editor access - users can edit the space content
     * Maps to: InviteType.MEMBER + SpaceMemberPermissions.WRITER
     */
    EDITOR_ACCESS(1),
    
    /**
     * Viewer access - users can only view the space content
     * Maps to: InviteType.MEMBER + SpaceMemberPermissions.READER
     */
    VIEWER_ACCESS(2),
    
    /**
     * Request access - users need approval to join
     * Maps to: InviteType.GUEST (requires approval)
     */
    REQUEST_ACCESS(3);
    
    fun toInviteTypeAndPermissions(): Pair<InviteType, SpaceMemberPermissions?>? {
        return when (this) {
            LINK_DISABLED -> null
            EDITOR_ACCESS -> InviteType.MEMBER to SpaceMemberPermissions.WRITER
            VIEWER_ACCESS -> InviteType.MEMBER to SpaceMemberPermissions.READER
            REQUEST_ACCESS -> InviteType.GUEST to null // Guest type requires approval, no permissions
        }
    }
    
    /**
     * Checks if changing to the new access level requires user confirmation
     * Based on iOS implementation logic
     */
    fun needsConfirmationToChangeTo(newLevel: SpaceInviteLinkAccessLevel): Boolean {
        // No confirmation needed when enabling from disabled state
        if (this == LINK_DISABLED) return false
        
        return when (newLevel) {
            EDITOR_ACCESS -> this != VIEWER_ACCESS
            VIEWER_ACCESS -> this != EDITOR_ACCESS
            REQUEST_ACCESS -> true // Always needs confirmation
            LINK_DISABLED -> true // Always needs confirmation
        }
    }
    
    /**
     * Checks if this transition can use changeInvite API vs needs revoke+regenerate
     */
    fun canUseChangeInviteApi(newLevel: SpaceInviteLinkAccessLevel): Boolean {
        return (this == EDITOR_ACCESS && newLevel == VIEWER_ACCESS) ||
               (this == VIEWER_ACCESS && newLevel == EDITOR_ACCESS)
    }
    
    companion object {
        fun getDefaultForSpaceType(spaceUxType: SpaceUxType): SpaceInviteLinkAccessLevel {
            return when (spaceUxType) {
                SpaceUxType.CHAT -> EDITOR_ACCESS
                SpaceUxType.DATA -> LINK_DISABLED
                else -> LINK_DISABLED
            }
        }
    }
}

/**
 * Settings for space invitation link
 */
data class SpaceInviteLinkSettings(
    val accessLevel: SpaceInviteLinkAccessLevel,
    val activeLink: SpaceInviteLink? = null
)