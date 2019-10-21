package com.agileburo.anytype.data.auth.repo

import com.agileburo.anytype.data.auth.model.AccountEntity
import com.agileburo.anytype.data.auth.model.WalletEntity

class AuthRemoteDataStore(
    private val authRemote: AuthRemote
) : AuthDataStore {

    override suspend fun selectAccount(
        id: String, path: String
    ) = authRemote.selectAccount(id, path)

    override suspend fun createAccount(
        name: String
    ) = authRemote.createAccount(name)

    override suspend fun recoverAccount() {
        authRemote.recoverAccount()
    }

    override suspend fun saveAccount(account: AccountEntity) {
        throw UnsupportedOperationException()
    }

    override fun observeAccounts() = authRemote.observeAccounts()

    override suspend fun createWallet(
        path: String
    ): WalletEntity = authRemote.createWallet(path)

    override suspend fun recoverWallet(path: String, mnemonic: String) {
        authRemote.recoverWallet(path, mnemonic)
    }

    override suspend fun isSignedIn(): Boolean {
        throw UnsupportedOperationException()
    }

    override suspend fun saveMnemonic(mnemonic: String) {
        throw UnsupportedOperationException()
    }

    override suspend fun getMnemonic(): String {
        throw UnsupportedOperationException()
    }

    override suspend fun logout() {
        throw UnsupportedOperationException()
    }
}