package com.anytypeio.anytype.data.auth.repo

import com.anytypeio.anytype.core_models.AccountStatus
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.data.auth.model.AccountEntity
import com.anytypeio.anytype.data.auth.model.FlavourConfigEntity
import com.anytypeio.anytype.data.auth.model.WalletEntity
import kotlinx.coroutines.flow.Flow

class AuthCacheDataStore(private val cache: AuthCache) : AuthDataStore {

    override suspend fun startAccount(
        id: String,
        path: String
    ): Triple<AccountEntity, FlavourConfigEntity, AccountStatus> {
        throw UnsupportedOperationException()
    }

    override suspend fun createAccount(name: String, avatarPath: String?, invitationCode: String): AccountEntity {
        throw UnsupportedOperationException()
    }

    override suspend fun deleteAccount(): AccountStatus {
        throw UnsupportedOperationException()
    }

    override suspend fun restoreAccount(): AccountStatus {
        throw UnsupportedOperationException()
    }

    override suspend fun recoverAccount() {
        throw UnsupportedOperationException()
    }

    override suspend fun saveAccount(account: AccountEntity) {
        cache.saveAccount(account)
    }

    override suspend fun updateAccount(account: AccountEntity) {
        cache.updateAccount(account)
    }

    override fun observeAccounts(): Flow<AccountEntity> {
        throw UnsupportedOperationException()
    }

    override suspend fun createWallet(path: String): WalletEntity {
        throw UnsupportedOperationException()
    }

    override suspend fun convertWallet(entropy: String): String {
        throw UnsupportedOperationException()
    }

    override suspend fun recoverWallet(path: String, mnemonic: String) {
        throw UnsupportedOperationException()
    }

    override suspend fun saveMnemonic(mnemonic: String) {
        cache.saveMnemonic(mnemonic)
    }

    override suspend fun getMnemonic() = cache.getMnemonic()

    override suspend fun logout(clearLocalRepositoryData: Boolean) {
        cache.logout()
    }

    override suspend fun getAccounts() = cache.getAccounts()

    override suspend fun getCurrentAccount() = cache.getCurrentAccount()

    override suspend fun getCurrentAccountId() = cache.getCurrentAccountId()

    override suspend fun setCurrentAccount(id: String) {
        cache.setCurrentAccount(id)
    }

    override suspend fun getVersion(): String {
        throw UnsupportedOperationException()
    }

    override suspend fun saveLastOpenedObject(id: Id) { cache.saveLastOpenedObject(id) }
    override suspend fun getLastOpenedObject(): Id? = cache.getLastOpenedObject()
    override suspend fun clearLastOpenedObject() { cache.clearLastOpenedObject() }
}