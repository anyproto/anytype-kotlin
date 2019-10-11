package com.agileburo.anytype.feature_login.ui.login.domain.repository

import com.agileburo.anytype.feature_login.ui.login.domain.model.Account
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun selectAccount(id: String, path: String): Account
    suspend fun createAccount(name: String): Account
    suspend fun recoverAccount()
    suspend fun saveAccount(account: Account)
    fun observeAccounts(): Flow<Account>
}