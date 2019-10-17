package com.agileburo.anytype.dao

import androidx.room.Dao
import com.agileburo.anytype.model.AccountTable

@Dao
abstract class AccountDao : BaseDao<AccountTable>