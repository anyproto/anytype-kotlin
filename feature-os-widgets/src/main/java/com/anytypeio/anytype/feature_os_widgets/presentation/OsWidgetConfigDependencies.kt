package com.anytypeio.anytype.feature_os_widgets.presentation

import com.anytypeio.anytype.feature_os_widgets.persistence.OsWidgetDataViewEntity
import com.anytypeio.anytype.feature_os_widgets.persistence.OsWidgetCreateObjectEntity
import com.anytypeio.anytype.feature_os_widgets.persistence.OsWidgetObjectShortcutEntity
import com.anytypeio.anytype.feature_os_widgets.persistence.OsWidgetSpaceShortcutEntity

interface ObjectShortcutWidgetConfigStore {
    suspend fun save(config: OsWidgetObjectShortcutEntity)
}

interface DataViewWidgetConfigStore {
    suspend fun save(config: OsWidgetDataViewEntity)
}

interface ObjectShortcutIconCache {
    suspend fun cacheForWidget(url: String, appWidgetId: Int): String?
}

fun interface ObjectShortcutWidgetUpdater {
    fun update(appWidgetId: Int)
}

fun interface DataViewWidgetUpdater {
    fun update(appWidgetId: Int)
}

interface CreateObjectWidgetConfigStore {
    suspend fun save(config: OsWidgetCreateObjectEntity)
}

fun interface CreateObjectWidgetUpdater {
    fun update(appWidgetId: Int)
}

interface SpaceShortcutWidgetConfigStore {
    suspend fun save(config: OsWidgetSpaceShortcutEntity)
}

interface SpaceShortcutIconCache {
    suspend fun cacheForWidget(url: String, appWidgetId: Int): String?
}

fun interface SpaceShortcutWidgetUpdater {
    fun update(appWidgetId: Int)
}
