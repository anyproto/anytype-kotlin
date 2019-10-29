package com.agileburo.anytype.dao

import androidx.room.Dao
import androidx.room.Query
import com.agileburo.anytype.common.Config
import com.agileburo.anytype.model.AccountTable

@Dao
abstract class AccountDao : BaseDao<AccountTable> {

    @Query(Config.CLEAR_ACCOUNT_TABLE)
    abstract suspend fun clear()

    @Query(Config.QUERY_LAST_ACCOUNT)
    abstract suspend fun lastAccount(): List<AccountTable>

    @Query(Config.GET_ACCOUNTS)
    abstract suspend fun getAccounts(): List<AccountTable>
}