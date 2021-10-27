package com.anytypeio.anytype.domain.config

import com.anytypeio.anytype.core_models.FlavourConfig

interface FlavourConfigProvider {

    fun get(): FlavourConfig

    fun set(
        enableDataView: Boolean,
        enableDebug: Boolean,
        enableChannelSwitch: Boolean,
        enableSpaces: Boolean
    )
}