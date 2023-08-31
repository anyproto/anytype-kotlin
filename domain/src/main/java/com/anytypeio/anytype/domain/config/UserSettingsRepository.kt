package com.anytypeio.anytype.domain.config

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ThemeMode
import com.anytypeio.anytype.core_models.Wallpaper
import com.anytypeio.anytype.core_models.WidgetSession

interface UserSettingsRepository {

    suspend fun setWallpaper(space: Id, wallpaper: Wallpaper)
    suspend fun getWallpaper(space: Id): Wallpaper

    suspend fun setDefaultObjectType(type: String, name: String)
    suspend fun getDefaultObjectType(): Pair<String?, String?>
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun getThemeMode(): ThemeMode
    suspend fun getWidgetSession() : WidgetSession
    suspend fun saveWidgetSession(session: WidgetSession)
    suspend fun clear()
}