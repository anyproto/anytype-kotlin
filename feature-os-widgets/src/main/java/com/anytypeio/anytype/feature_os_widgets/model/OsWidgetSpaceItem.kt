package com.anytypeio.anytype.feature_os_widgets.model

/**
 * Represents a space item for display in the OS home screen widget.
 * This is the domain/UI model used by the widget composables.
 */
data class OsWidgetSpaceItem(
    /**
     * The target space ID used for navigation when tapped.
     */
    val spaceId: String,

    /**
     * The display name of the space.
     */
    val name: String,

    /**
     * The icon representation (image or placeholder).
     */
    val icon: OsWidgetSpaceIcon,

    /**
     * Whether this is a 1-1 (DM) space. Drives icon styling similar to VaultScreen.
     */
    val isOneToOneSpace: Boolean
)
