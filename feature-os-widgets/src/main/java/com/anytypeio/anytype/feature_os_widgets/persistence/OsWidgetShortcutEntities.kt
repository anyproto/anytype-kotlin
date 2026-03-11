package com.anytypeio.anytype.feature_os_widgets.persistence

import kotlinx.serialization.Serializable

/**
 * Persistence entity for "Space Shortcut" widget configuration.
 * Each widget instance has its own configuration identified by appWidgetId.
 */
@Serializable
data class OsWidgetSpaceShortcutEntity(
    /**
     * The Android app widget ID (unique per widget instance).
     */
    val appWidgetId: Int,

    /**
     * The target space ID to open when widget is tapped.
     */
    val spaceId: String,

    /**
     * The display name of the space.
     */
    val spaceName: String,

    /**
     * The space icon image hash (for image-based icons).
     */
    val spaceIconImage: String? = null,

    /**
     * The space icon color option (0-9).
     */
    val spaceIconOption: Int? = null,

    /**
     * Local cached path for the icon image.
     * Only available after the image has been downloaded and cached.
     */
    val cachedIconPath: String? = null
)

/**
 * Wrapper for serializing a map of space shortcut widget configurations.
 */
@Serializable
data class OsWidgetSpaceShortcutCache(
    val configs: Map<Int, OsWidgetSpaceShortcutEntity> = emptyMap()
)

/**
 * Persistence entity for "Object Shortcut" widget configuration.
 * Each widget instance has its own configuration identified by appWidgetId.
 */
@Serializable
data class OsWidgetObjectShortcutEntity(
    /**
     * The Android app widget ID (unique per widget instance).
     */
    val appWidgetId: Int,

    /**
     * The space ID containing the object.
     */
    val spaceId: String,

    /**
     * The display name of the space.
     */
    val spaceName: String,

    /**
     * The object ID to open when widget is tapped.
     */
    val objectId: String,

    /**
     * The display name of the object.
     */
    val objectName: String,

    /**
     * The emoji icon for the object (if any).
     */
    val objectIconEmoji: String? = null,

    /**
     * The image hash for the object icon (if any).
     */
    val objectIconImage: String? = null,

    /**
     * The custom icon name for the object (e.g., for type icons).
     */
    val objectIconName: String? = null,

    /**
     * The icon color option (0-9) for custom icons.
     */
    val objectIconOption: Int? = null,

    /**
     * The object layout code (for determining icon style).
     */
    val objectLayout: Int? = null,

    /**
     * Local cached path for the object icon image.
     * Only available after the image has been downloaded and cached.
     */
    val cachedIconPath: String? = null
)

/**
 * Wrapper for serializing a map of object shortcut widget configurations.
 */
@Serializable
data class OsWidgetObjectShortcutCache(
    val configs: Map<Int, OsWidgetObjectShortcutEntity> = emptyMap()
)
