package com.anytypeio.anytype.domain.config

import com.anytypeio.anytype.core_models.Config

@Deprecated("Refactoring needed")
interface ConfigStorage {
    @Deprecated("Unsafe method. Use getOrNull() instead")
    @Throws(IllegalStateException::class)
    fun get(): Config
    fun getOrNull(): Config?
    fun set(config: Config)
    fun clear()
    class CacheStorage : ConfigStorage {
        private var instance: Config? = null

        override fun getOrNull(): Config? = instance

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