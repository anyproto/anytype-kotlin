package com.agileburo.anytype.core_utils.data

import com.agileburo.anytype.core_utils.model.AccountEntity

interface UserCache {
    suspend fun saveAccount(account: AccountEntity)
}