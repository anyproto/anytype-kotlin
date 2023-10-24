package com.anytypeio.anytype.middleware.auth

import anytype.Rpc
import com.anytypeio.anytype.core_models.Account
import com.anytypeio.anytype.core_models.AccountSetup
import com.anytypeio.anytype.core_models.AccountStatus
import com.anytypeio.anytype.core_models.Config
import com.anytypeio.anytype.core_models.FeaturesConfig
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
            id = acc.id,
            name = acc.name,
            color = acc.avatar?.color,
            avatar = null
        ),
        features = FeaturesConfig(),
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
        account = Account(
            id = acc.id,
            name = acc.name,
            color = acc.avatar?.color,
            avatar = null
        ),
        features = FeaturesConfig(),
        config = Config(
            home = info.homeObjectId,
            profile = info.profileObjectId,
            gateway = info.gatewayUrl,
            spaceView = info.spaceViewId,
            space = info.accountSpaceId,
            widgets = info.widgetsId,
            analytics = info.analyticsId,
            device = info.deviceId,
        ),
        status = status?.core() ?: AccountStatus.Unknown
    )
}