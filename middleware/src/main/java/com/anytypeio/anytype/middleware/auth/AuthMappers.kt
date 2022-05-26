package com.anytypeio.anytype.middleware.auth

import anytype.Rpc
import com.anytypeio.anytype.core_models.Account
import com.anytypeio.anytype.core_models.AccountSetup
import com.anytypeio.anytype.core_models.Config
import com.anytypeio.anytype.core_models.FeaturesConfig
import com.anytypeio.anytype.middleware.mappers.core

fun Rpc.Account.Select.Response.core(): AccountSetup {
    val acc = account
    checkNotNull(acc) { "Account can't be empty" }
    val configuration = acc.config
    checkNotNull(configuration) { "Config can't be empty" }
    val info = acc.info
    checkNotNull(info) { "Info can't be empty" }
    val status = acc.status
    checkNotNull(status) { "Status can't be empty" }

    return AccountSetup(
        account = Account(
            id = acc.id,
            name = acc.name,
            color = acc.avatar?.color,
            avatar = null
        ),
        features = FeaturesConfig(
            enableDataView = configuration.enableDataview,
            enableDebug = configuration.enableDebug,
            enableChannelSwitch = configuration.enableReleaseChannelSwitch,
            enableSpaces = configuration.enableSpaces
        ),
        config = Config(
            home = info.homeObjectId,
            profile = info.profileObjectId,
            gateway = info.gatewayUrl
        ),
        status = status.core()
    )
}