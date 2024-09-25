package com.anytypeio.anytype.data.auth.repo.config

import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.config.Gateway
import com.anytypeio.anytype.domain.debugging.Logger

class GatewayProvider(
    private val configStorage: ConfigStorage,
    private val logger: Logger
) : Gateway {
    override fun provide(): String {
        val gateway = configStorage.getOrNull()?.gateway
        if (gateway == null) {
            logger.logException(IllegalStateException("Gateway not found"))
        }
        return gateway.orEmpty()
    }

}