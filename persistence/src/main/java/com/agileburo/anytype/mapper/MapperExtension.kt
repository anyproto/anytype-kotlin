package com.agileburo.anytype.mapper

import com.agileburo.anytype.data.auth.model.AccountEntity
import com.agileburo.anytype.model.AccountTable

fun AccountTable.toEntity(): AccountEntity {
    return AccountEntity(
        id = id,
        name = name
    )
}