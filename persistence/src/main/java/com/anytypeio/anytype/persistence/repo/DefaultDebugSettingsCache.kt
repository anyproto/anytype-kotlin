package com.anytypeio.anytype.persistence.repo

import android.content.SharedPreferences
import com.anytypeio.anytype.data.auth.repo.DebugSettingsCache

class DefaultDebugSettingsCache(private val prefs: SharedPreferences) : DebugSettingsCache {

    override suspend fun enableAnytypeContextMenu() {
        prefs.edit().putBoolean(ACTION_MODE, true).apply()
    }

    override suspend fun disableAnytypeContextMenu() {
        prefs.edit().putBoolean(ACTION_MODE, false).apply()
    }

    override suspend fun getAnytypeContextMenu(): Boolean = prefs.getBoolean(ACTION_MODE, true)

    companion object {
        val ACTION_MODE = "prefs.action_mode"
    }
}