package com.anytypeio.anytype.persistence.repo

import android.content.SharedPreferences
import com.anytypeio.anytype.core_models.Wallpaper
import com.anytypeio.anytype.data.auth.repo.UserSettingsCache

class DefaultUserSettingsCache(private val prefs: SharedPreferences) : UserSettingsCache {

    override suspend fun setDefaultObjectType(type: String, name: String) {
        prefs.edit()
            .putString(DEFAULT_OBJECT_TYPE_ID_KEY, type)
            .putString(DEFAULT_OBJECT_TYPE_NAME_KEY, name)
            .apply()
    }

    override suspend fun getDefaultObjectType(): Pair<String?, String?> {
        val type = prefs.getString(DEFAULT_OBJECT_TYPE_ID_KEY, null)
        val name = prefs.getString(DEFAULT_OBJECT_TYPE_NAME_KEY, null)
        return Pair(type, name)
    }

    override suspend fun setWallpaper(wallpaper: Wallpaper) {
        when (wallpaper) {
            is Wallpaper.Default -> {
                prefs.edit()
                    .remove(WALLPAPER_VALUE_KEY)
                    .remove(WALLPAPER_TYPE_KEY)
                    .apply()
            }
            is Wallpaper.Color -> {
                prefs
                    .edit()
                    .putInt(WALLPAPER_TYPE_KEY, WALLPAPER_TYPE_COLOR)
                    .putString(WALLPAPER_VALUE_KEY, wallpaper.code)
                    .apply()
            }
            is Wallpaper.Gradient -> {
                prefs
                    .edit()
                    .putInt(WALLPAPER_TYPE_KEY, WALLPAPER_TYPE_GRADIENT)
                    .putString(WALLPAPER_VALUE_KEY, wallpaper.code)
                    .apply()
            }
            is Wallpaper.Image -> {
                prefs
                    .edit()
                    .putInt(WALLPAPER_TYPE_KEY, WALLPAPER_TYPE_IMAGE)
                    .putString(WALLPAPER_VALUE_KEY, wallpaper.hash)
                    .apply()
            }
        }
    }

    override suspend fun getWallpaper(): Wallpaper {
        val type = prefs.getInt(WALLPAPER_TYPE_KEY, -1)
        if (type != -1) {
            val value = prefs.getString(WALLPAPER_VALUE_KEY, null)
            if (value != null && value.isNotEmpty()) {
                return when (type) {
                    WALLPAPER_TYPE_COLOR -> {
                        Wallpaper.Color(value)
                    }
                    WALLPAPER_TYPE_GRADIENT -> {
                        Wallpaper.Gradient(value)
                    }
                    WALLPAPER_TYPE_IMAGE -> {
                        Wallpaper.Image(value)
                    }
                    else -> {
                        Wallpaper.Default
                    }
                }
            } else {
                return Wallpaper.Default
            }
        } else {
            return Wallpaper.Default
        }
    }

    companion object {
        const val DEFAULT_OBJECT_TYPE_ID_KEY = "prefs.user_settings.default_object_type.id"
        const val DEFAULT_OBJECT_TYPE_NAME_KEY = "prefs.user_settings.default_object_type.name"

        const val WALLPAPER_TYPE_COLOR = 1
        const val WALLPAPER_TYPE_GRADIENT = 2
        const val WALLPAPER_TYPE_IMAGE = 3

        const val WALLPAPER_TYPE_KEY = "prefs.user_settings.wallpaper_type"
        const val WALLPAPER_VALUE_KEY = "prefs.user_settings.wallpaper_value"
    }
}