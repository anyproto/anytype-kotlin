package com.anytypeio.anytype.domain.config

import com.anytypeio.anytype.core_models.FeaturesConfig

interface FeaturesConfigProvider {

    fun get(): FeaturesConfig

    fun set(
        enableDataView: Boolean,
        enableDebug: Boolean,
        enableChannelSwitch: Boolean,
        enableSpaces: Boolean
    )
}