package com.anytypeio.anytype.domain.config

import com.anytypeio.anytype.core_models.Config

interface ConfigStorage {
    fun get(): Config
    fun set(config: Config)
    fun clear()
    class CacheStorage : ConfigStorage {
        private var instance: Config? = null
        override fun get(): Config {
            return instance ?: throw IllegalStateException("Config is not initialized")
        }

        override fun set(config: Config) {
            instance = config
        }

        override fun clear() {
            instance = null
        }
    }
}