package com.agileburo.anytype.data.auth.repo

import com.agileburo.anytype.data.auth.model.AccountEntity

interface AuthCache {
    suspend fun saveAccount(account: AccountEntity)
    suspend fun saveMnemonic(mnemonic: String)
    suspend fun getMnemonic(): String
    suspend fun getAccount(): AccountEntity
    suspend fun logout()
}