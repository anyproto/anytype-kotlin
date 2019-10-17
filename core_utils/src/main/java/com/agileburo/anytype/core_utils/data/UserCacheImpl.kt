package com.agileburo.anytype.core_utils.data

import com.agileburo.anytype.core_utils.model.AccountEntity
import com.agileburo.anytype.db.AnytypeDatabase
import com.agileburo.anytype.model.AccountTable

class UserCacheImpl(
    private val db: AnytypeDatabase
) : UserCache {

    override suspend fun saveAccount(account: AccountEntity) {
        db.accountDao().insert(
            AccountTable(
                id = account.id,
                name = account.name
            )
        )
    }
}