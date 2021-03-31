package com.anytypeio.anytype.middleware.converters

import anytype.Event
import com.anytypeio.anytype.data.auth.model.AccountEntity

fun Event.Account.Show.toAccountEntity(): AccountEntity {
    val acc = account
    checkNotNull(acc)
    return AccountEntity(
        id = acc.id,
        name = acc.name,
        color = acc.avatar?.color
    )
}