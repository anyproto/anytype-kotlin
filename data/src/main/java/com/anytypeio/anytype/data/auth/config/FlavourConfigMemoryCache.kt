package com.anytypeio.anytype.data.auth.config

import com.anytypeio.anytype.core_models.FlavourConfig
import com.anytypeio.anytype.domain.config.FlavourConfigProvider

class FlavourConfigMemoryCache : FlavourConfigProvider {

    private var config: FlavourConfig = FlavourConfig(
        enableDataView = false,
        enableDebug = false,
        enableChannelSwitch = false
    )

    override fun get(): FlavourConfig = config

    override fun set(enableDataView: Boolean, enableDebug: Boolean, enableChannelSwitch: Boolean) {
        config = FlavourConfig(
            enableDataView = enableDataView,
            enableDebug = enableDebug,
            enableChannelSwitch = enableChannelSwitch
        )
    }
}