package com.agileburo.anytype.feature_login.ui.login.data

import com.agileburo.anytype.feature_login.ui.login.domain.model.Wallet
import com.agileburo.anytype.feature_login.ui.login.domain.repository.AuthRepository
import kotlinx.coroutines.delay

class AuthDataRepository(
    private val cache: AuthCacheDataStore
) : AuthRepository {

    override suspend fun isSignedIn() = cache.isSignedIn()

    override suspend fun saveMnemonic(mnemonic: String) {
        cache.saveMnemonic(mnemonic)
    }

    override suspend fun getMnemonic() = cache.getMnemonic()

    override suspend fun createWallet(path: String): Wallet {
        return Wallet(mnemonic = "123467890")
    }

    override suspend fun recoverWallet(path: String, mnemonic: String) {
        delay(2000)
        if (mnemonic != "mock")
            throw IllegalStateException("Invalid mnemonic phrase")
    }
}