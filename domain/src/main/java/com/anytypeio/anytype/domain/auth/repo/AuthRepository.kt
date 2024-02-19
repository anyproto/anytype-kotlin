package com.anytypeio.anytype.domain.auth.repo

import com.anytypeio.anytype.core_models.Account
import com.anytypeio.anytype.core_models.AccountSetup
import com.anytypeio.anytype.core_models.AccountStatus
import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.NetworkModeConfig
import com.anytypeio.anytype.domain.auth.model.Wallet
import kotlinx.coroutines.flow.Flow

interface AuthRepository {

    suspend fun setMetrics(platform: String, version: String)

    /**
     * Launches an account.
     * @param id user account id
     * @param path wallet repository path
     */
    suspend fun selectAccount(command: Command.AccountSelect): AccountSetup
    suspend fun createAccount(command: Command.AccountCreate): AccountSetup

    suspend fun deleteAccount() : AccountStatus
    suspend fun restoreAccount() : AccountStatus

    suspend fun startLoadingAccounts()

    suspend fun saveAccount(account: Account)

    suspend fun updateAccount(account: Account)

    fun observeAccounts(): Flow<Account>

    suspend fun getCurrentAccount(): Account

    suspend fun getCurrentAccountId(): String

    suspend fun convertWallet(entropy: String): String

    suspend fun createWallet(path: String): Wallet

    suspend fun recoverWallet(path: String, mnemonic: String)

    suspend fun saveMnemonic(mnemonic: String)

    suspend fun getMnemonic(): String

    suspend fun logout(clearLocalRepositoryData: Boolean)

    suspend fun getAccounts(): List<Account>

    /**
     * Sets currently selected user account
     * @param id account's id
     */
    suspend fun setCurrentAccount(id: String)

    suspend fun getVersion(): String

    suspend fun saveLastOpenedObjectId(id: Id)
    suspend fun getLastOpenedObjectId() : Id?
    suspend fun clearLastOpenedObject()

    suspend fun getNetworkMode(): NetworkModeConfig
    suspend fun setNetworkMode(modeConfig: NetworkModeConfig)
}