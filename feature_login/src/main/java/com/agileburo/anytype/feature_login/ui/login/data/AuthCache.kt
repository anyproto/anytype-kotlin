package com.agileburo.anytype.feature_login.ui.login.data

interface AuthCache {
    suspend fun isSignedIn(): Boolean
    suspend fun saveMnemonic(mnemonic: String)
    suspend fun getMnemonic(): String
}