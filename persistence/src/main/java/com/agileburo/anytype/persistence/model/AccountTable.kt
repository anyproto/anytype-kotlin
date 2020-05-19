package com.agileburo.anytype.persistence.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.agileburo.anytype.persistence.common.Config

@Entity(tableName = Config.ACCOUNT_TABLE_NAME)
data class AccountTable(
    @PrimaryKey val id: String,
    val name: String,
    val timestamp: Long,
    val color: String? = null
)