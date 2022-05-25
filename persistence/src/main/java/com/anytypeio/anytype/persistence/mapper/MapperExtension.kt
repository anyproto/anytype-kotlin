package com.anytypeio.anytype.persistence.mapper

import com.anytypeio.anytype.data.auth.model.AccountEntity
import com.anytypeio.anytype.persistence.model.AccountTable

fun AccountTable.toEntity(): AccountEntity {
    return AccountEntity(
        id = id,
        name = name,
        color = color
    )
}

fun AccountEntity.toTable(): AccountTable {
    return AccountTable(
        id = id,
        name = name,
        timestamp = System.currentTimeMillis()
    )
}