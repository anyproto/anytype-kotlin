package com.anytypeio.anytype.domain.config

import com.anytypeio.anytype.core_models.Wallpaper

interface UserSettingsRepository {
    suspend fun setWallpaper(wallpaper: Wallpaper)
    suspend fun getWallpaper(): Wallpaper
    suspend fun setDefaultObjectType(type: String, name: String)
    suspend fun getDefaultObjectType(): Pair<String?, String?>
}