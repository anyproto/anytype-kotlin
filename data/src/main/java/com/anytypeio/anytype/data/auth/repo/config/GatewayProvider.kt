package com.anytypeio.anytype.data.auth.repo.config

import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.config.Gateway

class GatewayProvider(
    private val configStorage: ConfigStorage
) : Gateway {
    override fun provide(): String = configStorage.get().gateway
}