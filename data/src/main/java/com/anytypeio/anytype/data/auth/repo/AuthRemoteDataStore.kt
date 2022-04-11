package com.anytypeio.anytype.data.auth.repo

import com.anytypeio.anytype.core_models.AccountStatus
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.data.auth.model.AccountEntity
import com.anytypeio.anytype.data.auth.model.WalletEntity

class AuthRemoteDataStore(
    private val authRemote: AuthRemote
) : AuthDataStore {

    override suspend fun startAccount(
        id: String, path: String
    ) = authRemote.startAccount(id, path)

    override suspend fun createAccount(
        name: String,
        avatarPath: String?,
        invitationCode: String
    ) = authRemote.createAccount(name, avatarPath, invitationCode)

    override suspend fun deleteAccount(): AccountStatus = authRemote.deleteAccount()

    override suspend fun restoreAccount(): AccountStatus = authRemote.restoreAccount()

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

    override suspend fun convertWallet(entropy: String): String =
        authRemote.convertWallet(entropy)

    override suspend fun saveMnemonic(mnemonic: String) {
        throw UnsupportedOperationException()
    }

    override suspend fun getMnemonic(): String {
        throw UnsupportedOperationException()
    }

    override suspend fun logout(clearLocalRepositoryData: Boolean) {
        authRemote.logout(clearLocalRepositoryData)
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

    override suspend fun getVersion(): String = authRemote.getVersion()

    override suspend fun saveLastOpenedObject(id: Id) {
        throw UnsupportedOperationException()
    }

    override suspend fun getLastOpenedObject(): Id? {
        throw UnsupportedOperationException()
    }

    override suspend fun clearLastOpenedObject() {
        throw UnsupportedOperationException()
    }
}