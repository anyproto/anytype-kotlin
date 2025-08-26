package com.anytypeio.anytype.core_models.multiplayer

/**
 * Represents the access level for space invitation links
 */
sealed class SpaceInviteLinkAccessLevel {

    /**
     * Link is disabled - no active invitation link
     */
    data object LinkDisabled : SpaceInviteLinkAccessLevel()

    /**
     * Editor access - users can edit the space content
     */
    data class EditorAccess(val link: String) : SpaceInviteLinkAccessLevel() {
        companion object {
            val EMPTY = EditorAccess("")
        }
    }

    /**
     * Viewer access - users can only view the space content
     */
    data class ViewerAccess(val link: String) : SpaceInviteLinkAccessLevel() {
        companion object {
            val EMPTY = ViewerAccess("")
        }
    }

    /**
     * Request access - users need approval to join
     */
    data class RequestAccess(val link: String) : SpaceInviteLinkAccessLevel() {
        companion object {
            val EMPTY = RequestAccess("")
        }
    }

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