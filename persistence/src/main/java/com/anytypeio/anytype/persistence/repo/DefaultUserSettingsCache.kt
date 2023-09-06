package com.anytypeio.anytype.persistence.repo

import android.content.SharedPreferences
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.NO_VALUE
import com.anytypeio.anytype.core_models.ThemeMode
import com.anytypeio.anytype.core_models.Wallpaper
import com.anytypeio.anytype.core_models.WidgetSession
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.primitives.TypeId
import com.anytypeio.anytype.data.auth.repo.UserSettingsCache
import com.anytypeio.anytype.persistence.common.JsonString
import com.anytypeio.anytype.persistence.common.deserializeWallpaperSettings
import com.anytypeio.anytype.persistence.common.serializeWallpaperSettings
import com.anytypeio.anytype.persistence.common.toJsonString
import com.anytypeio.anytype.persistence.common.toStringMap
import com.anytypeio.anytype.persistence.model.asSettings
import com.anytypeio.anytype.persistence.model.asWallpaper

class DefaultUserSettingsCache(private val prefs: SharedPreferences) : UserSettingsCache {

    override suspend fun setDefaultObjectType(space: SpaceId, type: TypeId) {
        val curr = prefs
            .getString(DEFAULT_OBJECT_TYPES_KEY, NO_VALUE)
            .orEmpty()
            .toStringMap()
        val updated = buildMap {
            putAll(curr)
            put(space.id, type.id)
        }
        prefs
            .edit()
            .putString(DEFAULT_OBJECT_TYPES_KEY, updated.toJsonString())
            .apply()
    }

    override suspend fun getDefaultObjectType(space: SpaceId): TypeId? {
        val curr = prefs
            .getString(DEFAULT_OBJECT_TYPES_KEY, "")
            .orEmpty()
            .toStringMap()

        val result = curr[space.id]
        return if (result.isNullOrEmpty())
            null
        else
            TypeId(result)
    }

    override suspend fun setWallpaper(space: Id, wallpaper: Wallpaper) {
        if (space.isEmpty()) return

        val curr = prefs.getString(WALLPAPER_SETTINGS_KEY, "")

        val result: JsonString

        val setting = wallpaper.asSettings()

        if (!curr.isNullOrEmpty()) {
            val map = curr.deserializeWallpaperSettings().toMutableMap()
            if (setting != null) {
                map[space] = setting
            } else {
                map.remove(space)
            }
            result = map.serializeWallpaperSettings()
        } else {
            result = if (setting != null) {
                mapOf(space to setting).serializeWallpaperSettings()
            } else {
                ""
            }
        }

        prefs
            .edit()
            .putString(WALLPAPER_SETTINGS_KEY, result)
            .apply()
    }

    override suspend fun getWallpaper(space: Id): Wallpaper {
        val rawSettings = prefs.getString(WALLPAPER_SETTINGS_KEY, "")
        return if (rawSettings.isNullOrEmpty()) {
            Wallpaper.Default
        } else {
            val deserialized = rawSettings.deserializeWallpaperSettings()
            val setting = deserialized[space]
            setting?.asWallpaper() ?: Wallpaper.Default
        }
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        prefs
            .edit()
            .putInt(THEME_KEY, mode.toInt())
            .apply()
    }

    override suspend fun getThemeMode(): ThemeMode {
        return if (prefs.contains(THEME_KEY)) {
            prefs.getInt(THEME_KEY, -1).toTheme()
        } else {
            ThemeMode.System
        }
    }

    private fun ThemeMode.toInt() = when (this) {
        ThemeMode.Light -> {
            THEME_TYPE_LIGHT
        }
        ThemeMode.Night -> {
            THEME_TYPE_NIGHT
        }
        ThemeMode.System -> {
            THEME_TYPE_SYSTEM
        }
    }

    private fun Int.toTheme() = when (this) {
        THEME_TYPE_LIGHT -> {
            ThemeMode.Light
        }
        THEME_TYPE_NIGHT -> {
            ThemeMode.Night
        }
        THEME_TYPE_SYSTEM -> {
            ThemeMode.System
        }
        else -> {
            throw IllegalStateException("Illegal theme key!")
        }
    }

    override suspend fun getWidgetSession(): WidgetSession = WidgetSession(
        collapsed = if (prefs.contains(COLLAPSED_WIDGETS_KEY)) {
            prefs.getStringSet(COLLAPSED_WIDGETS_KEY, emptySet())
                .orEmpty()
                .toList()
        } else {
            emptyList()
        },
        widgetsToActiveViews = if (prefs.contains(ACTIVE_WIDGETS_VIEWS_KEY)) {
            prefs.getString(ACTIVE_WIDGETS_VIEWS_KEY, "")!!.toStringMap()
        } else {
            emptyMap()
        }
    )

    override suspend fun saveWidgetSession(session: WidgetSession) {
        prefs
            .edit()
            .putStringSet(COLLAPSED_WIDGETS_KEY, session.collapsed.toSet())
            .putString(ACTIVE_WIDGETS_VIEWS_KEY, session.widgetsToActiveViews.toJsonString())
            .apply()
    }

    override suspend fun clear() {
        prefs.edit()
            .remove(DEFAULT_OBJECT_TYPE_ID_KEY)
            .remove(DEFAULT_OBJECT_TYPE_NAME_KEY)
            .remove(COLLAPSED_WIDGETS_KEY)
            .apply()
    }

    companion object {
        const val DEFAULT_OBJECT_TYPE_ID_KEY = "prefs.user_settings.default_object_type.id"
        const val DEFAULT_OBJECT_TYPE_NAME_KEY = "prefs.user_settings.default_object_type.name"

        const val DEFAULT_OBJECT_TYPES_KEY = "prefs.user_settings.default_object_types"
        const val WALLPAPER_SETTINGS_KEY = "prefs.user_settings.wallpaper_settings"

        const val THEME_KEY = "prefs.user_settings.theme_mode"
        const val THEME_TYPE_SYSTEM = 1
        const val THEME_TYPE_LIGHT = 2
        const val THEME_TYPE_NIGHT = 3

        const val COLLAPSED_WIDGETS_KEY = "prefs.user_settings.collapsed-widgets"
        const val ACTIVE_WIDGETS_VIEWS_KEY = "prefs.user_settings.active-widget-views"
    }
}