package com.anytypeio.anytype.persistence.oswidgets

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Top-level DataStore delegate - MUST be at file level for proper singleton behavior.
 * This ensures all contexts share the same DataStore instance.
 */
private val Context.osWidgetsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "os_widgets_data_store"
)

/**
 * Shared Json instance for serialization.
 */
private val osWidgetsJson = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}

/**
 * DataStore-based cache for OS widget spaces data.
 * Provides persistence for widget data that can be accessed even when the app is not running.
 */
class OsWidgetsDataStore(private val context: Context) {

    private val dataStore: DataStore<Preferences>
        get() = context.applicationContext.osWidgetsDataStore

    /**
     * Observes the cached spaces list.
     */
    fun observeSpaces(): Flow<List<OsWidgetSpaceEntity>> {
        return dataStore.data.map { preferences ->
            val jsonString = preferences[SPACES_CACHE_KEY]
            if (jsonString != null) {
                try {
                    osWidgetsJson.decodeFromString<OsWidgetSpacesCache>(jsonString).spaces
                } catch (e: Exception) {
                    emptyList()
                }
            } else {
                emptyList()
            }
        }
    }

    /**
     * Gets the cached spaces list (suspending, for widget updates).
     */
    suspend fun getSpaces(): List<OsWidgetSpaceEntity> {
        val prefs = dataStore.data.first()
        val jsonString = prefs[SPACES_CACHE_KEY]
        return if (jsonString != null) {
            try {
                osWidgetsJson.decodeFromString<OsWidgetSpacesCache>(jsonString).spaces
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    /**
     * Updates the cached spaces list.
     */
    suspend fun saveSpaces(spaces: List<OsWidgetSpaceEntity>) {
        val cache = OsWidgetSpacesCache(
            spaces = spaces,
            lastUpdated = System.currentTimeMillis()
        )
        dataStore.edit { preferences ->
            preferences[SPACES_CACHE_KEY] = osWidgetsJson.encodeToString(cache)
        }
    }

    /**
     * Clears the cached spaces (e.g., on logout).
     */
    suspend fun clear() {
        dataStore.edit { preferences ->
            preferences.remove(SPACES_CACHE_KEY)
        }
    }

    // ==================== Create Object Widget Config ====================

    /**
     * Saves a Create Object widget configuration.
     */
    suspend fun saveCreateObjectConfig(config: OsWidgetCreateObjectEntity) {
        dataStore.edit { preferences ->
            val cache = preferences[CREATE_OBJECT_CONFIGS_KEY]?.let { jsonString ->
                try {
                    osWidgetsJson.decodeFromString<OsWidgetCreateObjectCache>(jsonString)
                } catch (e: Exception) {
                    OsWidgetCreateObjectCache()
                }
            } ?: OsWidgetCreateObjectCache()

            val updatedConfigs = cache.configs.toMutableMap().apply {
                put(config.appWidgetId, config)
            }
            preferences[CREATE_OBJECT_CONFIGS_KEY] = osWidgetsJson.encodeToString(
                OsWidgetCreateObjectCache(updatedConfigs)
            )
        }
    }

    /**
     * Gets a Create Object widget configuration by appWidgetId.
     */
    suspend fun getCreateObjectConfig(appWidgetId: Int): OsWidgetCreateObjectEntity? {
        val jsonString = dataStore.data.first()[CREATE_OBJECT_CONFIGS_KEY] ?: return null
        return try {
            osWidgetsJson.decodeFromString<OsWidgetCreateObjectCache>(jsonString).configs[appWidgetId]
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Observes a Create Object widget configuration by appWidgetId.
     */
    fun observeCreateObjectConfig(appWidgetId: Int): Flow<OsWidgetCreateObjectEntity?> {
        return dataStore.data.map { preferences ->
            val jsonString = preferences[CREATE_OBJECT_CONFIGS_KEY]
            if (jsonString != null) {
                try {
                    osWidgetsJson.decodeFromString<OsWidgetCreateObjectCache>(jsonString)
                        .configs[appWidgetId]
                } catch (e: Exception) {
                    null
                }
            } else {
                null
            }
        }
    }

    /**
     * Deletes a Create Object widget configuration.
     */
    suspend fun deleteCreateObjectConfig(appWidgetId: Int) {
        dataStore.edit { preferences ->
            val jsonString = preferences[CREATE_OBJECT_CONFIGS_KEY]
            if (jsonString != null) {
                try {
                    val cache = osWidgetsJson.decodeFromString<OsWidgetCreateObjectCache>(jsonString)
                    val updatedConfigs = cache.configs.toMutableMap()
                    updatedConfigs.remove(appWidgetId)
                    preferences[CREATE_OBJECT_CONFIGS_KEY] = osWidgetsJson.encodeToString(
                        OsWidgetCreateObjectCache(updatedConfigs)
                    )
                } catch (e: Exception) {
                    // Ignore decode errors
                }
            }
        }
    }

    /**
     * Clears all Create Object widget configurations (e.g., on logout).
     */
    suspend fun clearCreateObjectConfigs() {
        dataStore.edit { preferences ->
            preferences.remove(CREATE_OBJECT_CONFIGS_KEY)
        }
    }

    companion object {
        private val SPACES_CACHE_KEY = stringPreferencesKey("spaces_cache")
        private val CREATE_OBJECT_CONFIGS_KEY = stringPreferencesKey("create_object_configs")
    }
}
