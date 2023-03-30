package com.anytypeio.anytype.data.auth.repo

import com.anytypeio.anytype.core_models.ThemeMode
import com.anytypeio.anytype.core_models.Wallpaper
import com.anytypeio.anytype.core_models.WidgetSession
import com.anytypeio.anytype.domain.config.UserSettingsRepository

class UserSettingsDataRepository(private val cache: UserSettingsCache) : UserSettingsRepository {

    override suspend fun setWallpaper(wallpaper: Wallpaper) {
        cache.setWallpaper(wallpaper)
    }

    override suspend fun getWallpaper(): Wallpaper = cache.getWallpaper()

    override suspend fun setDefaultObjectType(type: String, name: String) {
        cache.setDefaultObjectType(type, name)
    }

    override suspend fun getDefaultObjectType(): Pair<String?, String?> = cache.getDefaultObjectType()
    override suspend fun setThemeMode(mode: ThemeMode) {
        cache.setThemeMode(mode)
    }

    override suspend fun getThemeMode(): ThemeMode = cache.getThemeMode()

    override suspend fun getWidgetSession(): WidgetSession = cache.getWidgetSession()

    override suspend fun saveWidgetSession(session: WidgetSession) = cache.saveWidgetSession(
        session = session
    )

    override suspend fun clear() = cache.clear()
}