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
     * Checks if changing to Editor access requires user confirmation
     */
    fun needsConfirmationToChangeToEditor(): Boolean {
        // No confirmation needed when enabling from disabled state
        if (this is LinkDisabled) return false
        // No confirmation needed when changing from Viewer to Editor
        return this !is ViewerAccess
    }

    /**
     * Checks if changing to Viewer access requires user confirmation
     */
    fun needsConfirmationToChangeToViewer(): Boolean {
        // No confirmation needed when enabling from disabled state
        if (this is LinkDisabled) return false
        // No confirmation needed when changing from Editor to Viewer
        return this !is EditorAccess
    }

    /**
     * Checks if changing to Request access requires user confirmation
     */
    fun needsConfirmationToChangeToRequest(): Boolean {
        // No confirmation needed when enabling from disabled state
        if (this is LinkDisabled) return false
        // Always needs confirmation when changing to Request access
        return true
    }

    /**
     * Checks if disabling the link requires user confirmation
     */
    fun needsConfirmationToDisable(): Boolean {
        // Already disabled, no confirmation needed
        if (this is LinkDisabled) return false
        // Always needs confirmation when disabling an active link
        return true
    }
}