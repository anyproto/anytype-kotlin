package com.anytypeio.anytype.data.auth.repo

import com.anytypeio.anytype.core_models.ThemeMode
import com.anytypeio.anytype.core_models.Wallpaper
import com.anytypeio.anytype.core_models.WidgetSession

interface UserSettingsCache {
    suspend fun setDefaultObjectType(type: String, name: String)
    suspend fun getDefaultObjectType(): Pair<String?, String?>
    suspend fun setWallpaper(wallpaper: Wallpaper)
    suspend fun getWallpaper() : Wallpaper
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun getThemeMode(): ThemeMode
    suspend fun getWidgetSession() : WidgetSession
    suspend fun saveWidgetSession(session: WidgetSession)
    suspend fun clear()
}