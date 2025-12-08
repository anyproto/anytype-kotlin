package com.anytypeio.anytype.middleware.auth

import anytype.Rpc
import com.anytypeio.anytype.core_models.Account
import com.anytypeio.anytype.core_models.AccountSetup
import com.anytypeio.anytype.core_models.AccountStatus
import com.anytypeio.anytype.middleware.mappers.config
import com.anytypeio.anytype.middleware.mappers.core

fun Rpc.Account.Create.Response.toAccountSetup() : AccountSetup {
    val acc = account
    checkNotNull(acc) { "Account can't be empty" }
    val info = acc.info
    checkNotNull(info) { "Info can't be empty" }
    val status = acc.status

    return AccountSetup(
        account = Account(
            id = acc.id
        ),
        config = info.config(),
        status = status?.core() ?: AccountStatus.Unknown
    )
}

fun Rpc.Account.Select.Response.toAccountSetup(): AccountSetup {
    val acc = account
    checkNotNull(acc) { "Account can't be empty" }
    val info = acc.info
    checkNotNull(info) { "Info can't be empty" }
    val status = acc.status

    return AccountSetup(
        account = Account(acc.id),
        config = info.config(),
        status = status?.core() ?: AccountStatus.Unknown
    )
}