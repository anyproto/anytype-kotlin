package com.agileburo.anytype.data.auth.repo.config

import com.agileburo.anytype.data.auth.mapper.toDomain

class Configuration(private val configurator: Configurator) {
    fun init() = configurator.configure().toDomain()
}