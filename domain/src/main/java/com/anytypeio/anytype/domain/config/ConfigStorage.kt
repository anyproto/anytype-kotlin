package com.anytypeio.anytype.domain.config

import com.anytypeio.anytype.core_models.Config
import com.anytypeio.anytype.core_models.primitives.SpaceId
import javax.inject.Inject

@Deprecated("Refactoring needed")
interface ConfigStorage : TechSpaceProvider {
    fun getAccountId(): String?
    fun getOrNull(): Config?
    fun set(config: Config, accountId: String)
    fun clear()
}

class CacheStorage @Inject constructor() : ConfigStorage {

    private data class State(
        val config: Config,
        val accountId: String
    )

    @Volatile
    private var state: State? = null

    override fun getOrNull(): Config? = state?.config

    override fun getAccountId(): String? = state?.accountId

    override fun set(config: Config, accountId: String) {
        this.state = State(config = config, accountId = accountId)
    }

    override fun clear() {
        state = null
    }

    override fun provide(): SpaceId? {
        return state?.config?.let { cfg ->
            SpaceId(cfg.techSpace)
        }
    }
}


interface TechSpaceProvider {
    fun provide(): SpaceId?
}