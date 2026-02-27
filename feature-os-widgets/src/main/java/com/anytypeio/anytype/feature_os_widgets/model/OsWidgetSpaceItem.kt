package com.anytypeio.anytype.feature_os_widgets.model

import com.anytypeio.anytype.core_models.multiplayer.SpaceUxType

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
     * The space UX type (DATA, CHAT, STREAM, ONE_TO_ONE).
     * Determines icon styling similar to VaultScreen.
     */
    val spaceUxType: SpaceUxType
)
