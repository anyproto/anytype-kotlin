package com.agileburo.anytype.feature_login.ui.login.data

import android.content.SharedPreferences

class AuthCacheImpl(
    private val prefs: SharedPreferences
) : AuthCache {

    companion object {
        const val MNEMONIC_KEY = "mnemonic"
    }

    override suspend fun isSignedIn(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun saveMnemonic(mnemonic: String) {
        prefs.edit().putString(MNEMONIC_KEY, mnemonic).apply()
    }

    override suspend fun getMnemonic() = prefs.getString(MNEMONIC_KEY, null)
        ?: throw IllegalStateException("Mnemonic is missing.")
}