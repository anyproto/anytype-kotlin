package com.anytypeio.anytype.persistence.oswidgets

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
     * The SpaceUxType code (DATA=0, CHAT=1, STREAM=2, ONE_TO_ONE=3).
     */
    val spaceUxType: Int
)

/**
 * Wrapper for serializing a list of spaces.
 */
@Serializable
data class OsWidgetSpacesCache(
    val spaces: List<OsWidgetSpaceEntity> = emptyList(),
    val lastUpdated: Long = 0L
)
