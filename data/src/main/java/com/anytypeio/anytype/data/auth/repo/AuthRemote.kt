package com.anytypeio.anytype.data.auth.repo

import com.anytypeio.anytype.data.auth.model.AccountEntity
import com.anytypeio.anytype.data.auth.model.WalletEntity
import kotlinx.coroutines.flow.Flow

interface AuthRemote {
    suspend fun startAccount(id: String, path: String): AccountEntity
    suspend fun createAccount(name: String, avatarPath: String?, invitationCode: String): AccountEntity
    suspend fun recoverAccount()
    suspend fun logout()
    fun observeAccounts(): Flow<AccountEntity>

    suspend fun createWallet(path: String): WalletEntity
    suspend fun recoverWallet(path: String, mnemonic: String)
}