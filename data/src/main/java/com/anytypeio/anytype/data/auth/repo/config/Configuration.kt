package com.anytypeio.anytype.data.auth.repo.config

class Configuration(private val configurator: Configurator) {
    fun init() = configurator.configure()
}