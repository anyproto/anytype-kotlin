package com.anytypeio.anytype.feature_os_widgets.persistence

import kotlinx.serialization.Serializable

/**
 * Persistence entity for space data used by OS home screen widgets.
 * Flat structure optimized for JSON serialization in DataStore.
 */
@Serializable
data class OsWidgetSpaceEntity(
    /**
     * The target space ID for navigation.
     */
    val spaceId: String,

    /**
     * The display name of the space.
     */
    val name: String,

    /**
     * The full image URL for custom icon, null if using placeholder.
     * Built from hash using UrlBuilder during sync.
     */
    val iconImageUrl: String? = null,

    /**
     * The SystemColor index (0-9) for icon gradient/background.
     */
    val iconColorIndex: Int,

    /**
     * Whether this is a 1-1 (DM) space. Drives icon shape in the widget.
     *
     * Defaults to false so existing cached payloads (which stored the legacy
     * `spaceUxType: Int` ordinal) deserialize cleanly via `ignoreUnknownKeys`
     * — the correct value is re-populated by the next `sync()` pass.
     */
    val isOneToOneSpace: Boolean = false
)

/**
 * Wrapper for serializing a list of spaces.
 */
@Serializable
data class OsWidgetSpacesCache(
    val spaces: List<OsWidgetSpaceEntity> = emptyList(),
    val lastUpdated: Long = 0L
)
