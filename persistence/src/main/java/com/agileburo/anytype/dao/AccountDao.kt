package com.agileburo.anytype.dao

import androidx.room.Dao
import androidx.room.Query
import com.agileburo.anytype.common.Config
import com.agileburo.anytype.model.AccountTable

@Dao
abstract class AccountDao : BaseDao<AccountTable> {

    @Query(Config.CLEAR_ACCOUNT_TABLE)
    abstract suspend fun clear()

}