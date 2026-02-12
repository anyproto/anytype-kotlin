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

    companion object {
        private val Context.osWidgetsDataStore: DataStore<Preferences> by preferencesDataStore(
            name = "os_widgets_data_store"
        )

        private val SPACES_CACHE_KEY = stringPreferencesKey("spaces_cache")
    }
}
