package com.anytypeio.anytype.data.auth.repo

import com.anytypeio.anytype.core_models.Wallpaper
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
}