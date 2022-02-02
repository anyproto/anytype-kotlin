package com.anytypeio.anytype.persistence.dao

import androidx.room.Dao
import androidx.room.Query
import com.anytypeio.anytype.persistence.common.Config
import com.anytypeio.anytype.persistence.model.AccountTable

@Dao
abstract class AccountDao : BaseDao<AccountTable> {

    @Query(Config.CLEAR_ACCOUNT_TABLE)
    abstract fun clear()

    @Query(Config.QUERY_LAST_ACCOUNT)
    abstract fun lastAccount(): List<AccountTable>

    @Query(Config.QUERY_ACCOUNT_BY_ID)
    abstract fun getAccount(id: String): AccountTable?

    @Query(Config.GET_ACCOUNTS)
    abstract fun getAccounts(): List<AccountTable>
}