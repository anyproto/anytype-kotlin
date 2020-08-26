package com.agileburo.anytype.data.auth.repo.config

import com.agileburo.anytype.domain.config.Gateway

class GatewayProvider(private val configurator: Configurator) : Gateway {
    override fun obtain(): String = configurator.configure().gateway
}