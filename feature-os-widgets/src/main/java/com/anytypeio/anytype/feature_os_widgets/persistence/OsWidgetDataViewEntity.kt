package com.anytypeio.anytype.feature_os_widgets.persistence

import kotlinx.serialization.Serializable

/**
 * Persistence entity for a single item displayed in the Data View widget.
 */
@Serializable
data class OsWidgetDataViewItemEntity(
    val id: String,
    val name: String,
    val typeName: String
)

/**
 * Persistence entity for "Data View" widget configuration.
 * Each widget instance has its own configuration identified by appWidgetId.
 */
@Serializable
data class OsWidgetDataViewEntity(
    /**
     * The Android app widget ID (unique per widget instance).
     */
    val appWidgetId: Int,

    /**
     * The space ID containing the set/collection.
     */
    val spaceId: String,

    /**
     * The object ID of the Set or Collection.
     */
    val objectId: String,

    /**
     * The display name of the Set or Collection.
     */
    val objectName: String,

    /**
     * The object's layout code.
     */
    val objectLayout: Int,

    /**
     * The selected viewer ID within the data view.
     */
    val viewerId: String,

    /**
     * The display name of the selected viewer.
     */
    val viewerName: String,

    /**
     * Cached list of items from the data view (up to 10).
     */
    val items: List<OsWidgetDataViewItemEntity> = emptyList()
)

/**
 * Wrapper for serializing a map of data view widget configurations.
 */
@Serializable
data class OsWidgetDataViewCache(
    val configs: Map<Int, OsWidgetDataViewEntity> = emptyMap()
)
