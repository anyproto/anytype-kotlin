package com.agileburo.anytype.feature_login.ui.login.domain.repository

import com.agileburo.anytype.feature_login.ui.login.domain.model.Account

interface UserRepository {
    suspend fun selectAccount(id: String)
    suspend fun getCurrentAccount(): Account
    suspend fun getAccounts(): List<Account>
    suspend fun createAccount(name: String)
}