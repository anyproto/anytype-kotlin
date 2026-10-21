package com.anytypeio.anytype.core_models.multiplayer

/**
 * Represents the access level for space invitation links
 */
sealed class SpaceInviteLinkAccessLevel {

    /**
     * Link is disabled - no active invitation link, possibleToUpdate indicates if user can change invite link type
     */
    data class LinkDisabled(val possibleToUpdate: Boolean = true) : SpaceInviteLinkAccessLevel()

    /**
     * Invite exists but is held by the space owner — its cid and key sync only to the
     * owner's devices. This is what a non-owner member sees: there is no link to render.
     */
    data object HeldByOwner : SpaceInviteLinkAccessLevel()

    /**
     * Editor access - users can edit the space content
     *
     * @property isShared true when the invite is shared within the space (every member can see and share it)
     */
    data class EditorAccess(
        val link: String,
        val isShared: Boolean = false
    ) : SpaceInviteLinkAccessLevel() {
        companion object {
            val EMPTY = EditorAccess("")
        }
    }

    /**
     * Viewer access - users can only view the space content
     *
     * @property isShared true when the invite is shared within the space (every member can see and share it)
     */
    data class ViewerAccess(
        val link: String,
        val isShared: Boolean = false
    ) : SpaceInviteLinkAccessLevel() {
        companion object {
            val EMPTY = ViewerAccess("")
        }
    }

    /**
     * Request access - users need approval to join
     *
     * @property isShared true when the invite is shared within the space (every member can see and share it)
     */
    data class RequestAccess(
        val link: String,
        val isShared: Boolean = false
    ) : SpaceInviteLinkAccessLevel() {
        companion object {
            val EMPTY = RequestAccess("")
        }
    }

    /**
     * True when there is an active invite shared within the space.
     */
    val isSharedWithinSpace: Boolean
        get() = when (this) {
            is EditorAccess -> isShared
            is ViewerAccess -> isShared
            is RequestAccess -> isShared
            is LinkDisabled, is HeldByOwner -> false
        }

    /**
     * The invite link when there is one to render on this device, null otherwise.
     * An invite held by the owner has no link on a member's device — never render it.
     */
    val linkOrNull: String?
        get() = when (this) {
            is EditorAccess -> link.ifEmpty { null }
            is ViewerAccess -> link.ifEmpty { null }
            is RequestAccess -> link.ifEmpty { null }
            is LinkDisabled, is HeldByOwner -> null
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
            is HeldByOwner -> false // Never a selection target
        }
    }
}
