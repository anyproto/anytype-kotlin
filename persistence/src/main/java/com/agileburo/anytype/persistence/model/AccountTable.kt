package com.agileburo.anytype.persistence.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.agileburo.anytype.persistence.common.Config
import com.agileburo.anytype.persistence.util.Converters

@Entity(tableName = Config.ACCOUNT_TABLE_NAME)
data class AccountTable(
    @PrimaryKey val id: String,
    val name: String,
    val timestamp: Long,
    val color: String? = null,
    @Embedded val avatar: Avatar? = null
) {

    data class Avatar(
        val avatarId: String,
        val sizes: List<Size>
    )

    @TypeConverters(Converters::class)
    enum class Size { SMALL, LARGE, THUMB }

}