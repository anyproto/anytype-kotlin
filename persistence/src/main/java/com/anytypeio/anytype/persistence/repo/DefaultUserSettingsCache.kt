package com.anytypeio.anytype.persistence.repo

import android.content.SharedPreferences
import com.anytypeio.anytype.core_models.ThemeMode
import com.anytypeio.anytype.core_models.Wallpaper
import com.anytypeio.anytype.core_models.WidgetSession
import com.anytypeio.anytype.data.auth.repo.UserSettingsCache
import com.anytypeio.anytype.persistence.common.toJsonString
import com.anytypeio.anytype.persistence.common.toStringMap

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
            if (!value.isNullOrEmpty()) {
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

        const val WALLPAPER_TYPE_COLOR = 1
        const val WALLPAPER_TYPE_GRADIENT = 2
        const val WALLPAPER_TYPE_IMAGE = 3

        const val WALLPAPER_TYPE_KEY = "prefs.user_settings.wallpaper_type"
        const val WALLPAPER_VALUE_KEY = "prefs.user_settings.wallpaper_value"

        const val THEME_KEY = "prefs.user_settings.theme_mode"
        const val THEME_TYPE_SYSTEM = 1
        const val THEME_TYPE_LIGHT = 2
        const val THEME_TYPE_NIGHT = 3

        const val COLLAPSED_WIDGETS_KEY = "prefs.user_settings.collapsed-widgets"
        const val ACTIVE_WIDGETS_VIEWS_KEY = "prefs.user_settings.active-widget-views"
    }
}