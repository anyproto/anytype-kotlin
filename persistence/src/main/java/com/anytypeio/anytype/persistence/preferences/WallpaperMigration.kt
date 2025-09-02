package com.anytypeio.anytype.persistence.preferences

import android.content.SharedPreferences
import androidx.datastore.core.DataMigration
import com.anytypeio.anytype.persistence.SpacePreference
import com.anytypeio.anytype.persistence.SpacePreferences
import com.anytypeio.anytype.persistence.WallpaperSetting
import com.anytypeio.anytype.persistence.common.deserializeWallpaperSettings
import timber.log.Timber

class WallpaperMigration(
    private val sharedPreferences: SharedPreferences
) : DataMigration<SpacePreferences> {

    override suspend fun shouldMigrate(currentData: SpacePreferences): Boolean {
        val hasSharedPrefsData = sharedPreferences.contains(WALLPAPER_SETTINGS_KEY) &&
                !sharedPreferences.getString(WALLPAPER_SETTINGS_KEY, "").isNullOrEmpty()
        
        // Only migrate if we have SharedPreferences data and no migrated data exists
        return hasSharedPrefsData && !hasMigratedWallpaperData(currentData)
    }

    override suspend fun migrate(currentData: SpacePreferences): SpacePreferences {
        val rawSettings = sharedPreferences.getString(WALLPAPER_SETTINGS_KEY, "")
        
        return if (!rawSettings.isNullOrEmpty()) {
            val deserializedWallpapers = rawSettings.deserializeWallpaperSettings()
            if (deserializedWallpapers.isNotEmpty()) {
                Timber.d("Migrating wallpaper settings for ${deserializedWallpapers.size} spaces")
                
                val updatedPreferences = buildMap {
                    putAll(currentData.preferences)
                    deserializedWallpapers.forEach { (spaceId, wallpaperSetting) ->
                        val existingSpacePref = get(spaceId) ?: SpacePreference()
                        val protoWallpaperSetting = WallpaperSetting(
                            type = wallpaperSetting.type,
                            value_ = wallpaperSetting.value
                        )
                        put(spaceId, existingSpacePref.copy(wallpaperSetting = protoWallpaperSetting))
                    }
                }
                SpacePreferences(preferences = updatedPreferences)
            } else {
                currentData
            }
        } else {
            currentData
        }
    }

    override suspend fun cleanUp() {
        // Clean up SharedPreferences after successful migration
        sharedPreferences.edit().remove(WALLPAPER_SETTINGS_KEY).apply()
        Timber.d("Cleaned up wallpaper settings from SharedPreferences")
    }

    private fun hasMigratedWallpaperData(currentData: SpacePreferences): Boolean {
        return currentData.preferences.values.any { it.wallpaperSetting != null }
    }

    companion object {
        const val WALLPAPER_SETTINGS_KEY = "prefs.user_settings.wallpaper_settings"
    }
}