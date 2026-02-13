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
import timber.log.Timber

/**
 * DataStore-based cache for OS widget spaces data.
 * Provides persistence for widget data that can be accessed even when the app is not running.
 */
class OsWidgetsDataStore(private val context: Context) {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    /**
     * Observes the cached spaces list.
     */
    fun observeSpaces(): Flow<List<OsWidgetSpaceEntity>> {
        return context.osWidgetsDataStore.data.map { preferences ->
            val jsonString = preferences[SPACES_CACHE_KEY]
            if (jsonString != null) {
                try {
                    json.decodeFromString<OsWidgetSpacesCache>(jsonString).spaces
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
        val prefs = context.osWidgetsDataStore.data.first()
        val jsonString = prefs[SPACES_CACHE_KEY]
        return if (jsonString != null) {
            try {
                json.decodeFromString<OsWidgetSpacesCache>(jsonString).spaces
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
        context.osWidgetsDataStore.edit { preferences ->
            preferences[SPACES_CACHE_KEY] = json.encodeToString(cache)
        }
    }

    /**
     * Clears the cached spaces (e.g., on logout).
     */
    suspend fun clear() {
        context.osWidgetsDataStore.edit { preferences ->
            preferences.remove(SPACES_CACHE_KEY)
        }
    }

    // ==================== Create Object Widget Config ====================

    /**
     * Saves a Create Object widget configuration.
     */
    suspend fun saveCreateObjectConfig(config: OsWidgetCreateObjectEntity) {
        Timber.d("OsWidgetsDataStore: saveCreateObjectConfig called for appWidgetId=${config.appWidgetId}")
        Timber.d("OsWidgetsDataStore: context=$context, contextClass=${context.javaClass.name}")
        context.osWidgetsDataStore.edit { preferences ->
            val jsonString = preferences[CREATE_OBJECT_CONFIGS_KEY]
            Timber.d("OsWidgetsDataStore: existing jsonString=$jsonString")
            val cache = if (jsonString != null) {
                try {
                    json.decodeFromString<OsWidgetCreateObjectCache>(jsonString)
                } catch (e: Exception) {
                    Timber.e(e, "OsWidgetsDataStore: Failed to decode existing cache")
                    OsWidgetCreateObjectCache()
                }
            } else {
                OsWidgetCreateObjectCache()
            }
            val updatedConfigs = cache.configs.toMutableMap()
            updatedConfigs[config.appWidgetId] = config
            val newJson = json.encodeToString(OsWidgetCreateObjectCache(updatedConfigs))
            Timber.d("OsWidgetsDataStore: saving newJson=$newJson")
            preferences[CREATE_OBJECT_CONFIGS_KEY] = newJson
        }
        Timber.d("OsWidgetsDataStore: saveCreateObjectConfig completed")
    }

    /**
     * Gets a Create Object widget configuration by appWidgetId.
     */
    suspend fun getCreateObjectConfig(appWidgetId: Int): OsWidgetCreateObjectEntity? {
        Timber.d("OsWidgetsDataStore: getCreateObjectConfig called for appWidgetId=$appWidgetId")
        Timber.d("OsWidgetsDataStore: context=$context, contextClass=${context.javaClass.name}")
        val prefs = context.osWidgetsDataStore.data.first()
        val jsonString = prefs[CREATE_OBJECT_CONFIGS_KEY]
        Timber.d("OsWidgetsDataStore: read jsonString=$jsonString")
        return if (jsonString != null) {
            try {
                val cache = json.decodeFromString<OsWidgetCreateObjectCache>(jsonString)
                val config = cache.configs[appWidgetId]
                Timber.d("OsWidgetsDataStore: found config=$config for appWidgetId=$appWidgetId")
                config
            } catch (e: Exception) {
                Timber.e(e, "OsWidgetsDataStore: Failed to decode cache")
                null
            }
        } else {
            Timber.d("OsWidgetsDataStore: no data found")
            null
        }
    }

    /**
     * Observes a Create Object widget configuration by appWidgetId.
     */
    fun observeCreateObjectConfig(appWidgetId: Int): Flow<OsWidgetCreateObjectEntity?> {
        return context.osWidgetsDataStore.data.map { preferences ->
            val jsonString = preferences[CREATE_OBJECT_CONFIGS_KEY]
            if (jsonString != null) {
                try {
                    json.decodeFromString<OsWidgetCreateObjectCache>(jsonString)
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
        context.osWidgetsDataStore.edit { preferences ->
            val jsonString = preferences[CREATE_OBJECT_CONFIGS_KEY]
            if (jsonString != null) {
                try {
                    val cache = json.decodeFromString<OsWidgetCreateObjectCache>(jsonString)
                    val updatedConfigs = cache.configs.toMutableMap()
                    updatedConfigs.remove(appWidgetId)
                    preferences[CREATE_OBJECT_CONFIGS_KEY] = json.encodeToString(
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
        context.osWidgetsDataStore.edit { preferences ->
            preferences.remove(CREATE_OBJECT_CONFIGS_KEY)
        }
    }

    companion object {
        private val Context.osWidgetsDataStore: DataStore<Preferences> by preferencesDataStore(
            name = "os_widgets_data_store"
        )

        private val SPACES_CACHE_KEY = stringPreferencesKey("spaces_cache")
        private val CREATE_OBJECT_CONFIGS_KEY = stringPreferencesKey("create_object_configs")
    }
}
