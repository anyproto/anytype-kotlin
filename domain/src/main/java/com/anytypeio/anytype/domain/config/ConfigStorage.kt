package com.anytypeio.anytype.domain.config

import com.anytypeio.anytype.core_models.Config
import com.anytypeio.anytype.core_models.primitives.SpaceId

@Deprecated("Refactoring needed")
interface ConfigStorage : TechSpaceProvider {
    fun getAccountId(): String?
    fun getOrNull(): Config?
    fun set(config: Config, accountId: String)
    fun clear()
    class CacheStorage : ConfigStorage {
        @Volatile
        private var instance: Config? = null

        @Volatile
        private var accountId: String? = null

        override fun getOrNull(): Config? = instance

        override fun getAccountId(): String? {
            return accountId
        }

        @Synchronized
        override fun set(config: Config, accountId: String) {
            this.instance = config
            this.accountId = accountId
        }

        @Synchronized
        override fun clear() {
            instance = null
            accountId = null
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