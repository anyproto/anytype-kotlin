package com.anytypeio.anytype.persistence.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.anytypeio.anytype.persistence.common.Config

@Entity(tableName = Config.ACCOUNT_TABLE_NAME)
data class AccountTable(
    @PrimaryKey val id: String,
    val timestamp: Long,
    @Deprecated("Should not be used")
    val color: String? = null,
    @Deprecated("Should not be used")
    val name: String,
)