package com.agileburo.anytype.feature_login.ui.login.domain.repository

import com.agileburo.anytype.feature_login.ui.login.domain.model.Wallet

interface AuthRepository {
    suspend fun createWallet(path: String): Wallet
    suspend fun recoverWallet(path: String, mnemonic: String)
    suspend fun isSignedIn(): Boolean
    suspend fun saveMnemonic(mnemonic: String)
    suspend fun getMnemonic(): String
}