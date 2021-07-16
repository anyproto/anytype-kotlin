package com.anytypeio.anytype.domain.config

// Use-case for getting flavor-specific config.
class GetFlavourConfig(
    private val configProvider: FlavourConfigProvider
) {

    fun isDataViewEnabled(): Boolean = configProvider.get().enableDataView == true
}