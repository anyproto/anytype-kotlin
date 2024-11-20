package com.anytypeio.anytype.data.auth.repo

import com.anytypeio.anytype.core_models.Account
import com.anytypeio.anytype.core_models.AccountSetup
import com.anytypeio.anytype.core_models.AccountStatus
import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.NetworkModeConfig
import com.anytypeio.anytype.data.auth.mapper.toDomain
import com.anytypeio.anytype.data.auth.mapper.toEntity
import com.anytypeio.anytype.domain.auth.model.Wallet
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.debugging.DebugConfig
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withTimeout

class AuthDataRepository(
    private val factory: AuthDataStoreFactory,
    private val debugConfig: DebugConfig
) : AuthRepository {

    override suspend fun setInitialParams(command: Command.SetInitialParams) {
        factory.remote.setInitialParams(command = command)
    }

    override suspend fun selectAccount(
        command: Command.AccountSelect
    ): AccountSetup {
        return if (debugConfig.setTimeouts) {
            withTimeout(DebugConfig.SELECT_ACCOUNT_TIMEOUT) {
                factory.remote.selectAccount(command)
            }
        } else { factory.remote.selectAccount(command)}
    }

    override suspend fun createAccount(
        command: Command.AccountCreate
    ): AccountSetup {
        return if (debugConfig.setTimeouts) {
            withTimeout(DebugConfig.CREATE_ACCOUNT_TIMEOUT) { factory.remote.createAccount(command)}
        } else {
            factory.remote.createAccount(command)
        }
    }

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

    override suspend fun getNetworkMode(): NetworkModeConfig {
        return factory.cache.getNetworkMode()
    }

    override suspend fun setNetworkMode(modeConfig: NetworkModeConfig) {
        factory.cache.setNetworkMode(modeConfig)
    }
}