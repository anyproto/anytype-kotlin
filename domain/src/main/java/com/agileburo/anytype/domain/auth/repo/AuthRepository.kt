package com.agileburo.anytype.domain.auth.repo

import com.agileburo.anytype.domain.auth.model.Account
import com.agileburo.anytype.domain.auth.model.Wallet
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun selectAccount(id: String, path: String): Account
    suspend fun createAccount(name: String, avatarPath: String?): Account
    suspend fun recoverAccount()
    suspend fun saveAccount(account: Account)
    fun observeAccounts(): Flow<Account>

    suspend fun getAccount(): Account

    suspend fun createWallet(path: String): Wallet
    suspend fun recoverWallet(path: String, mnemonic: String)
    suspend fun isSignedIn(): Boolean
    suspend fun saveMnemonic(mnemonic: String)
    suspend fun getMnemonic(): String

    suspend fun logout()
}