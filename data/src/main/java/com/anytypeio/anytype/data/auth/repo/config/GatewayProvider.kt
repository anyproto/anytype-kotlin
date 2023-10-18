package com.anytypeio.anytype.data.auth.repo.config

import com.anytypeio.anytype.domain.config.Gateway
import com.anytypeio.anytype.domain.debugging.Logger
import com.anytypeio.anytype.domain.workspace.SpaceManager

class GatewayProvider(
    private val spaceManager: SpaceManager,
    private val logger: Logger
) : Gateway {
    override fun provide(): String {
        val gateway = spaceManager.getConfig()?.gateway
        if (gateway != null) {
            logger.logException(IllegalStateException("Gateway not found"))
        }
        return gateway.orEmpty()
    }

}