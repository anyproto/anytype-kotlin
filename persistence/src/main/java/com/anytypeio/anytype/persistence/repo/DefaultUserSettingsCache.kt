package com.anytypeio.anytype.persistence.repo

import android.content.SharedPreferences
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

    companion object {
        const val DEFAULT_OBJECT_TYPE_ID_KEY = "prefs.user_settings.default_object_type.id"
        const val DEFAULT_OBJECT_TYPE_NAME_KEY = "prefs.user_settings.default_object_type.name"
    }
}