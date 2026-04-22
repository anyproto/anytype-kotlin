package com.anytypeio.anytype.presentation.home

import com.anytypeio.anytype.core_models.multiplayer.SpaceMemberPermissions

/**
 * DROID-4463: homepage-setting visibility rule.
 *
 * In regular channels, only the space owner may configure the homepage.
 * In 1-on-1 channels Chat is always the homepage (see DROID-4469), so no
 * participant — owner or otherwise — configures it via the UI.
 */
object HomepageManagementRule {
    fun canManageHomepage(
        isOneToOneSpace: Boolean,
        permission: SpaceMemberPermissions?
    ): Boolean = !isOneToOneSpace && permission?.isOwner() == true
}
