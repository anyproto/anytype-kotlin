package com.anytypeio.anytype.data.auth.repo

import com.anytypeio.anytype.core_models.AccountSetup
import com.anytypeio.anytype.core_models.AccountStatus
import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.data.auth.model.AccountEntity
import com.anytypeio.anytype.data.auth.model.WalletEntity
import kotlinx.coroutines.flow.Flow

interface AuthRemote {
    suspend fun selectAccount(command: Command.AccountSelect): AccountSetup
    suspend fun createAccount(command: Command.AccountCreate): AccountSetup

    suspend fun migrateAccount(account: Id, path: String)
    suspend fun cancelAccountMigration(account: Id)

    suspend fun deleteAccount() : AccountStatus
    suspend fun restoreAccount() : AccountStatus
    suspend fun recoverAccount()
    suspend fun logout(clearLocalRepositoryData: Boolean)
    fun observeAccounts(): Flow<AccountEntity>

    suspend fun createWallet(path: String): WalletEntity
    suspend fun recoverWallet(path: String, mnemonic: String)
    suspend fun convertWallet(entropy: String): String

    suspend fun getVersion(): String
    suspend fun setInitialParams(command: Command.SetInitialParams)
    suspend fun debugExportLogs(dir: String): String

    suspend fun registerDeviceToken(command: Command.RegisterDeviceToken)
}