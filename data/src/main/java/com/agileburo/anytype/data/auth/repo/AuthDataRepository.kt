package com.agileburo.anytype.data.auth.repo

import com.agileburo.anytype.data.auth.mapper.toDomain
import com.agileburo.anytype.data.auth.mapper.toEntity
import com.agileburo.anytype.data.auth.repo.config.Configurator
import com.agileburo.anytype.domain.auth.model.Account
import com.agileburo.anytype.domain.auth.model.Wallet
import com.agileburo.anytype.domain.auth.repo.AuthRepository
import kotlinx.coroutines.flow.map

class AuthDataRepository(
    private val factory: AuthDataStoreFactory,
    private val configurator: Configurator
) : AuthRepository {

    override suspend fun startAccount(
        id: String, path: String
    ): Account = factory.remote.startAccount(id, path).toDomain()

    override suspend fun createAccount(
        name: String,
        avatarPath: String?
    ): Account = factory.remote.createAccount(name, avatarPath).toDomain()

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

    override suspend fun recoverWallet(path: String, mnemonic: String) {
        factory.remote.recoverWallet(path, mnemonic)
    }

    override suspend fun getCurrentAccount() = factory.cache.getCurrentAccount().toDomain()

    override suspend fun getCurrentAccountId() = factory.cache.getCurrentAccountId()

    override suspend fun saveMnemonic(
        mnemonic: String
    ) = factory.cache.saveMnemonic(mnemonic)

    override suspend fun getMnemonic() = factory.cache.getMnemonic()

    override suspend fun logout() {
        configurator.release()
        factory.remote.logout()
        factory.cache.logout()
    }

    override suspend fun getAccounts() = factory.cache.getAccounts().map { it.toDomain() }

    override suspend fun setCurrentAccount(id: String) {
        factory.cache.setCurrentAccount(id)
    }
}