package com.anytypeio.anytype.persistence.repo

import android.content.SharedPreferences
import com.anytypeio.anytype.data.auth.repo.UserSettingsCache

class DefaultUserSettingsCache(private val prefs: SharedPreferences) : UserSettingsCache {

    override suspend fun setDefaultPageType(type: String) {
        prefs.edit().putString(DEFAULT_PAGE_KEY, type).apply()
    }

    override suspend fun getDefaultPageType(): String? {
        return prefs.getString(DEFAULT_PAGE_KEY, null)
    }

    companion object {
        const val DEFAULT_PAGE_KEY = "prefs.user_settings.default_page"
    }
}