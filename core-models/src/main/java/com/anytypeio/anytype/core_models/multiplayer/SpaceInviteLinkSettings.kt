package com.anytypeio.anytype.core_models.multiplayer

/**
 * Represents the access level for space invitation links as per Task #24
 */
sealed class SpaceInviteLinkAccessLevel {

    /**
     * Link is disabled - no active invitation link
     */
    data object LinkDisabled : SpaceInviteLinkAccessLevel()

    /**
     * Editor access - users can edit the space content
     * Maps to: InviteType.WITHOUT_APPROVE + SpaceMemberPermissions.WRITER
     */
    data class EditorAccess(val link: String) : SpaceInviteLinkAccessLevel()

    /**
     * Viewer access - users can only view the space content
     * Maps to: InviteType.WITHOUT_APPROVE + SpaceMemberPermissions.READER
     */
    data class ViewerAccess(val link: String) : SpaceInviteLinkAccessLevel()

    /**
     * Request access - users need approval to join
     * Maps to: InviteType.MEMBER (requires approval)
     */
    data class RequestAccess(val link: String) : SpaceInviteLinkAccessLevel()

    /**
     * Checks if changing to the new access level requires user confirmation
     */
    fun needsConfirmationToChangeTo(newLevel: SpaceInviteLinkAccessLevel): Boolean {
        // No confirmation needed when enabling from disabled state
        if (this is LinkDisabled) return false

        return when (newLevel) {
            is EditorAccess -> this !is ViewerAccess
            is ViewerAccess -> this !is EditorAccess
            is RequestAccess -> true // Always needs confirmation
            is LinkDisabled -> true // Always needs confirmation
        }
    }
}