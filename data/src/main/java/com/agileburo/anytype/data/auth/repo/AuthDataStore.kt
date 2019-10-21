package com.agileburo.anytype.data.auth.repo

import com.agileburo.anytype.data.auth.model.AccountEntity
import com.agileburo.anytype.data.auth.model.WalletEntity
import kotlinx.coroutines.flow.Flow

interface AuthDataStore {
    suspend fun selectAccount(id: String, path: String): AccountEntity
    suspend fun createAccount(name: String): AccountEntity
    suspend fun recoverAccount()
    suspend fun saveAccount(account: AccountEntity)
    fun observeAccounts(): Flow<AccountEntity>

    suspend fun createWallet(path: String): WalletEntity
    suspend fun recoverWallet(path: String, mnemonic: String)
    suspend fun isSignedIn(): Boolean
    suspend fun saveMnemonic(mnemonic: String)
    suspend fun getMnemonic(): String

    suspend fun logout()
}