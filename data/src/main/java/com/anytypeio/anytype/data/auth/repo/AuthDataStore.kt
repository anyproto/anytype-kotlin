package com.anytypeio.anytype.data.auth.repo

import com.anytypeio.anytype.core_models.AccountSetup
import com.anytypeio.anytype.core_models.AccountStatus
import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.NetworkModeConfig
import com.anytypeio.anytype.data.auth.model.AccountEntity
import com.anytypeio.anytype.data.auth.model.WalletEntity
import kotlinx.coroutines.flow.Flow

interface AuthDataStore {

    suspend fun selectAccount(command: Command.AccountSelect): AccountSetup
    suspend fun createAccount(command: Command.AccountCreate): AccountSetup

    suspend fun deleteAccount() : AccountStatus
    suspend fun restoreAccount() : AccountStatus

    suspend fun migrateAccount(account: Id, path: String)
    suspend fun cancelAccountMigration(account: Id)

    suspend fun recoverAccount()

    suspend fun saveAccount(account: AccountEntity)

    suspend fun updateAccount(account: AccountEntity)

    fun observeAccounts(): Flow<AccountEntity>

    suspend fun getCurrentAccount(): AccountEntity
    suspend fun getCurrentAccountId(): String

    suspend fun createWallet(path: String): WalletEntity
    suspend fun recoverWallet(path: String, mnemonic: String)
    suspend fun convertWallet(entropy: String): String
    suspend fun saveMnemonic(mnemonic: String)
    suspend fun getMnemonic(): String?

    suspend fun logout(clearLocalRepositoryData: Boolean)
    suspend fun getAccounts(): List<AccountEntity>
    suspend fun setCurrentAccount(id: String)

    suspend fun getVersion(): String
    suspend fun setInitialParams(command: Command.SetInitialParams)

    suspend fun getNetworkMode(): NetworkModeConfig
    suspend fun setNetworkMode(modeConfig: NetworkModeConfig)
    suspend fun debugExportLogs(dir: String): String
    suspend fun debugRunProfiler(durationInSeconds: Int): String

    suspend fun registerDeviceToken(request: Command.RegisterDeviceToken)

    suspend fun appShutdown()
}