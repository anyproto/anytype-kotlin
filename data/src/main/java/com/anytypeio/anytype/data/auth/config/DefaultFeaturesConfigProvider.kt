package com.anytypeio.anytype.data.auth.config

import com.anytypeio.anytype.core_models.FeaturesConfig
import com.anytypeio.anytype.domain.config.FeaturesConfigProvider

/**
 * Features config provider for real-world application.
 */
class DefaultFeaturesConfigProvider : FeaturesConfigProvider {

    private var config: FeaturesConfig = FeaturesConfig(
        enableDataView = false,
        enableDebug = false,
        enableChannelSwitch = false,
        enableSpaces = false
    )

    override fun get(): FeaturesConfig = config

    override fun set(
        enableDataView: Boolean,
        enableDebug: Boolean,
        enableChannelSwitch: Boolean,
        enableSpaces: Boolean
    ) {
        config = FeaturesConfig(
            enableDataView = enableDataView,
            enableDebug = enableDebug,
            enableChannelSwitch = enableChannelSwitch,
            enableSpaces = enableSpaces
        )
    }
}