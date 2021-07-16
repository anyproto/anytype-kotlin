package com.anytypeio.anytype.domain.auth.repo

import com.anytypeio.anytype.domain.auth.model.Account
import com.anytypeio.anytype.domain.auth.model.Wallet
import com.anytypeio.anytype.core_models.FlavourConfig
import kotlinx.coroutines.flow.Flow

interface AuthRepository {

    /**
     * Launches an account.
     * @param id user account id
     * @param path wallet repository path
     */
    suspend fun startAccount(id: String, path: String): Pair<Account, FlavourConfig>

    suspend fun createAccount(name: String, avatarPath: String?, invitationCode: String): Account

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

    suspend fun logout()

    suspend fun getAccounts(): List<Account>

    /**
     * Sets currently selected user account
     * @param id account's id
     */
    suspend fun setCurrentAccount(id: String)

    suspend fun getVersion(): String
}