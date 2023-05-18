package com.anytypeio.anytype.data.auth.repo

import com.anytypeio.anytype.core_models.Account
import com.anytypeio.anytype.core_models.AccountSetup
import com.anytypeio.anytype.core_models.AccountStatus
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.data.auth.mapper.toDomain
import com.anytypeio.anytype.data.auth.mapper.toEntity
import com.anytypeio.anytype.domain.auth.model.Wallet
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import kotlinx.coroutines.flow.map

class AuthDataRepository(
    private val factory: AuthDataStoreFactory
) : AuthRepository {

    override suspend fun selectAccount(
        id: String, path: String
    ): AccountSetup = factory.remote.selectAccount(
        id = id,
        path = path
    )

    override suspend fun createAccount(
        name: String,
        avatarPath: String?,
        invitationCode: String,
        icon: Int
    ): AccountSetup = factory.remote.createAccount(
        name = name,
        avatarPath = avatarPath,
        invitationCode = invitationCode,
        iconGradientValue = icon
    )

    override suspend fun deleteAccount(): AccountStatus = factory.remote.deleteAccount()
    override suspend fun restoreAccount(): AccountStatus = factory.remote.restoreAccount()

    override suspend fun startLoadingAccounts() {
        factory.remote.recoverAccount()
    }

    override suspend fun saveAccount(account: Account) {
        factory.cache.saveAccount(account.toEntity())
    }

    override suspend fun updateAccount(account: Account) {
        factory.cache.updateAccount(account.toEntity())
    }

    override fun observeAccounts() = factory.remote.observeAccounts().map { it.toDomain() }

    override suspend fun createWallet(
        path: String
    ): Wallet = factory.remote.createWallet(path).toDomain()

    override suspend fun convertWallet(entropy: String): String =
        factory.remote.convertWallet(entropy)

    override suspend fun recoverWallet(path: String, mnemonic: String) {
        factory.remote.recoverWallet(path, mnemonic)
    }

    override suspend fun getCurrentAccount() = factory.cache.getCurrentAccount().toDomain()

    override suspend fun getCurrentAccountId() = factory.cache.getCurrentAccountId()

    override suspend fun saveMnemonic(
        mnemonic: String
    ) = factory.cache.saveMnemonic(mnemonic)

    override suspend fun getMnemonic() = factory.cache.getMnemonic()

    override suspend fun logout(clearLocalRepositoryData: Boolean) {
        factory.remote.logout(clearLocalRepositoryData)
        factory.cache.logout(clearLocalRepositoryData)
    }

    override suspend fun getAccounts() = factory.cache.getAccounts().map { it.toDomain() }

    override suspend fun setCurrentAccount(id: String) {
        factory.cache.setCurrentAccount(id)
    }

    override suspend fun getVersion(): String = factory.remote.getVersion()

    override suspend fun saveLastOpenedObjectId(id: Id) { factory.cache.saveLastOpenedObject(id) }
    override suspend fun getLastOpenedObjectId(): Id? = factory.cache.getLastOpenedObject()
    override suspend fun clearLastOpenedObject() { factory.cache.clearLastOpenedObject() }
}