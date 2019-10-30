package com.agileburo.anytype.data.auth.repo

import com.agileburo.anytype.data.auth.mapper.toDomain
import com.agileburo.anytype.data.auth.mapper.toEntity
import com.agileburo.anytype.domain.auth.model.Account
import com.agileburo.anytype.domain.auth.model.Wallet
import com.agileburo.anytype.domain.auth.repo.AuthRepository
import kotlinx.coroutines.flow.map

class AuthDataRepository(
    private val factory: AuthDataStoreFactory
) : AuthRepository {

    override suspend fun selectAccount(
        id: String, path: String
    ): Account = factory.remote.selectAccount(id, path).toDomain()

    override suspend fun createAccount(
        name: String,
        avatarPath: String?
    ): Account = factory.remote.createAccount(name, avatarPath).toDomain()

    override suspend fun recoverAccount() {
        factory.remote.recoverAccount()
    }

    override suspend fun saveAccount(account: Account) {
        factory.cache.saveAccount(account.toEntity())
    }

    override fun observeAccounts() = factory.remote.observeAccounts().map { it.toDomain() }

    override suspend fun createWallet(
        path: String
    ): Wallet = factory.remote.createWallet(path).toDomain()

    override suspend fun recoverWallet(path: String, mnemonic: String) {
        factory.remote.recoverWallet(path, mnemonic)
    }

    override suspend fun getAccount() = factory.cache.getAccount().toDomain()

    override suspend fun saveMnemonic(
        mnemonic: String
    ) = factory.cache.saveMnemonic(mnemonic)

    override suspend fun getMnemonic() = factory.cache.getMnemonic()

    override suspend fun logout() {
        factory.cache.logout()
    }

    override suspend fun getAvailableAccounts(): List<Account> =
        factory.cache.getStoredAccounts().map { it.toDomain() }
}