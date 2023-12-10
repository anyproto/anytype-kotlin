package com.anytypeio.anytype.data.auth.repo

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.NetworkMode
import com.anytypeio.anytype.core_models.NetworkModeConfig
import com.anytypeio.anytype.data.auth.model.AccountEntity

interface AuthCache {

    suspend fun saveAccount(account: AccountEntity)
    suspend fun updateAccount(account: AccountEntity)

    suspend fun saveMnemonic(mnemonic: String)
    suspend fun getMnemonic(): String

    suspend fun getCurrentAccount(): AccountEntity
    suspend fun getCurrentAccountId(): String

    suspend fun logout()
    suspend fun getAccounts(): List<AccountEntity>
    suspend fun setCurrentAccount(id: String)

    suspend fun saveLastOpenedObject(id: Id)
    suspend fun getLastOpenedObject() : Id?
    suspend fun clearLastOpenedObject()

    suspend fun getNetworkMode(): NetworkModeConfig
    suspend fun setNetworkMode(modeConfig: NetworkModeConfig)
}