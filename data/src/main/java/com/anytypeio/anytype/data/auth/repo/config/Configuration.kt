package com.anytypeio.anytype.data.auth.repo.config

import com.anytypeio.anytype.data.auth.mapper.toDomain

class Configuration(private val configurator: Configurator) {
    fun init() = configurator.configure().toDomain()
}