package com.anytypeio.anytype.feature_os_widgets.persistence

import kotlinx.serialization.Serializable

/**
 * Persistence entity for "Create Object" widget configuration.
 * Each widget instance has its own configuration identified by appWidgetId.
 */
@Serializable
data class OsWidgetCreateObjectEntity(
    /**
     * The Android app widget ID (unique per widget instance).
     */
    val appWidgetId: Int,

    /**
     * The target space ID where objects will be created.
     */
    val spaceId: String,

    /**
     * The object type unique key for creation.
     */
    val typeKey: String,

    /**
     * The display name of the object type.
     */
    val typeName: String,

    /**
     * The emoji icon for the object type, null if not an emoji type.
     */
    val typeIconEmoji: String? = null,

    /**
     * The custom icon name for the object type (e.g., "extension-puzzle").
     * Used for non-emoji type icons.
     */
    val typeIconName: String? = null,

    /**
     * The icon color option (0-9) for custom icons.
     */
    val typeIconOption: Int? = null,

    /**
     * The display name of the space.
     */
    val spaceName: String = ""
)

/**
 * Wrapper for serializing a map of widget configurations.
 */
@Serializable
data class OsWidgetCreateObjectCache(
    val configs: Map<Int, OsWidgetCreateObjectEntity> = emptyMap()
)
