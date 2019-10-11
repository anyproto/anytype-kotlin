package com.agileburo.anytype.feature_login.ui.login.data

class AuthCacheDataStore(
    private val authCache: AuthCache
) : AuthDataStore {

    override suspend fun isSignedIn() = authCache.isSignedIn()

    override suspend fun saveMnemonic(mnemonic: String) {
        authCache.saveMnemonic(mnemonic)
    }

    override suspend fun getMnemonic() = authCache.getMnemonic()
}