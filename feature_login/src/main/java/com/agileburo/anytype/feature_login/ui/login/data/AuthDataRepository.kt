package com.agileburo.anytype.feature_login.ui.login.data

import com.agileburo.anytype.feature_login.ui.login.domain.model.Wallet
import com.agileburo.anytype.feature_login.ui.login.domain.repository.AuthRepository
import com.agileburo.anytype.middleware.interactor.Middleware
import timber.log.Timber

class AuthDataRepository(
    private val cache: AuthCacheDataStore,
    private val middleware: Middleware
) : AuthRepository {

    override suspend fun isSignedIn() = cache.isSignedIn()

    override suspend fun saveMnemonic(mnemonic: String) {
        Timber.d("Saving mnemonic: $mnemonic")
        cache.saveMnemonic(mnemonic)
    }

    override suspend fun getMnemonic() = cache.getMnemonic()

    override suspend fun createWallet(path: String): Wallet {
        return Wallet(mnemonic = middleware.createWallet(path).mnemonic)
    }

    override suspend fun recoverWallet(path: String, mnemonic: String) {
        middleware.recoverWallet(path, mnemonic)
    }
}