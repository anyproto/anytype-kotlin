package com.agileburo.anytype.data.auth.repo

import com.agileburo.anytype.data.auth.model.AccountEntity
import com.agileburo.anytype.data.auth.model.WalletEntity

class AuthRemoteDataStore(
    private val authRemote: AuthRemote
) : AuthDataStore {

    override suspend fun startAccount(
        id: String, path: String
    ) = authRemote.startAccount(id, path)

    override suspend fun createAccount(
        name: String,
        avatarPath: String?
    ) = authRemote.createAccount(name, avatarPath)

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

    override suspend fun saveMnemonic(mnemonic: String) {
        throw UnsupportedOperationException()
    }

    override suspend fun getMnemonic(): String {
        throw UnsupportedOperationException()
    }

    override suspend fun logout() {
        authRemote.logout()
    }

    override suspend fun getAccounts(): List<AccountEntity> {
        throw UnsupportedOperationException()
    }

    override suspend fun getCurrentAccount(): AccountEntity {
        throw UnsupportedOperationException()
    }

    override suspend fun setCurrentAccount(id: String) {
        throw UnsupportedOperationException()
    }

    override suspend fun getCurrentAccountId(): String {
        throw UnsupportedOperationException()
    }

    override suspend fun updateAccount(account: AccountEntity) {
        throw UnsupportedOperationException()
    }
}