package com.anytypeio.anytype.data.auth.repo.config

import com.anytypeio.anytype.domain.config.Gateway

class GatewayProvider(private val configurator: Configurator) : Gateway {
    override fun obtain(): String = configurator.configure().gateway
}