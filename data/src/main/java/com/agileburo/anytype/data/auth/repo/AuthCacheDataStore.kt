package com.agileburo.anytype.data.auth.repo

import com.agileburo.anytype.data.auth.model.AccountEntity
import com.agileburo.anytype.data.auth.model.WalletEntity
import kotlinx.coroutines.flow.Flow

class AuthCacheDataStore(private val cache: AuthCache) : AuthDataStore {

    override suspend fun selectAccount(id: String, path: String): AccountEntity {
        throw UnsupportedOperationException()
    }

    override suspend fun createAccount(name: String, avatarPath: String?): AccountEntity {
        throw UnsupportedOperationException()
    }

    override suspend fun recoverAccount() {
        throw UnsupportedOperationException()
    }

    override suspend fun saveAccount(account: AccountEntity) {
        cache.saveAccount(account)
    }

    override fun observeAccounts(): Flow<AccountEntity> {
        throw UnsupportedOperationException()
    }

    override suspend fun createWallet(path: String): WalletEntity {
        throw UnsupportedOperationException()
    }

    override suspend fun recoverWallet(path: String, mnemonic: String) {
        throw UnsupportedOperationException()
    }

    override suspend fun isSignedIn(): Boolean {
        throw UnsupportedOperationException()
    }

    override suspend fun saveMnemonic(mnemonic: String) {
        cache.saveMnemonic(mnemonic)
    }

    override suspend fun getMnemonic() = cache.getMnemonic()

    override suspend fun logout() {
        cache.logout()
    }

    override suspend fun getAccount() = cache.getAccount()
}