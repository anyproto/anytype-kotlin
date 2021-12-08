package com.anytypeio.anytype.data.auth.repo

import com.anytypeio.anytype.core_models.Wallpaper

interface UserSettingsCache {
    suspend fun setDefaultObjectType(type: String, name: String)
    suspend fun getDefaultObjectType(): Pair<String?, String?>
    suspend fun setWallpaper(wallpaper: Wallpaper)
    suspend fun getWallpaper() : Wallpaper
}