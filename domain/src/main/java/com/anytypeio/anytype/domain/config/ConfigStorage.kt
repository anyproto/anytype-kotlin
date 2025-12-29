package com.anytypeio.anytype.domain.config

import com.anytypeio.anytype.core_models.Config
import com.anytypeio.anytype.core_models.primitives.SpaceId

@Deprecated("Refactoring needed")
interface ConfigStorage : TechSpaceProvider {
    @Deprecated("Unsafe method. Use getOrNull() instead")
    @Throws(IllegalStateException::class)
    fun get(): Config
    fun getAccountId(): String?
    fun getOrNull(): Config?
    fun set(config: Config, accountId: String)
    fun clear()
    class CacheStorage : ConfigStorage {
        private var instance: Config? = null
        private var accountId: String? = null

        override fun getOrNull(): Config? = instance

        override fun getAccountId(): String? {
            return accountId
        }

        override fun get(): Config {
            return instance ?: throw IllegalStateException("Config is not initialized")
        }

        override fun set(config: Config, accountId: String) {
            instance = config
            this.accountId = accountId
        }

        override fun clear() {
            instance = null
        }

        override fun provide(): SpaceId? {
            return instance?.let {
                SpaceId(it.techSpace)
            }
        }
    }
}

interface TechSpaceProvider {
    fun provide(): SpaceId?
}