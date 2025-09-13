package com.anytypeio.anytype.domain.workspace

import com.anytypeio.anytype.core_models.Config
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.debugging.Logger
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.withContext

/**
 * Maybe convert to AppStateManager, with different states.
 */
interface SpaceManager {

    suspend fun get(): Id
    suspend fun set(space: Id, withChat: Boolean = false): Result<Config>

    fun getConfig(): Config?
    fun getConfig(space: SpaceId) : Config?
    fun observe() : Flow<Config>
    fun observe(space: SpaceId): Flow<Config>
    fun state(): Flow<State>
    fun getState(): State

    fun clear()

    class Impl @Inject constructor(
        private val repo: BlockRepository,
        private val dispatchers: AppCoroutineDispatchers,
        private val logger: Logger
    ) : SpaceManager {

        private val currentSpace = MutableStateFlow(NO_SPACE)
        private val info = mutableMapOf<Id, Config>()

        override suspend fun get(): Id {
            val curr = currentSpace.value
            if (curr.isEmpty()) {
                logger.logWarning("Accessing space manager in no space state")
            }
            return curr
        }

        override fun getConfig(): Config? {
            val curr = currentSpace.value
            return if (curr.isNotEmpty()) {
                info[curr]
            } else {
                null
            }
        }

        override fun getConfig(space: SpaceId): Config? {
            return info[space.id]
        }

        override suspend fun set(space: Id, withChat: Boolean) : Result<Config> = withContext(dispatchers.io) {
            runCatching { repo.spaceOpen(space, withChat) }.also { result ->
                result.fold(
                    onSuccess = { config ->
                        info[space] = config
                        currentSpace.value = space
                    },
                    onFailure = logger::logException
                )
            }
        }

        override fun observe(): Flow<Config> {
            return currentSpace.mapNotNull { space ->
                if (space.isEmpty()) {
                    null
                } else {
                    info[space]
                }
            }
        }

        override fun observe(space: SpaceId): Flow<Config> {
            return currentSpace.mapNotNull {
                info[space.id]
            }
        }

        override fun state(): Flow<State> {
            return currentSpace.map { space ->
                if (space == NO_SPACE) {
                    State.NoSpace
                } else {
                    val config = info[space]
                    if (config != null) {
                        State.Space.Active(config)
                    } else {
                        State.Space.Idle(SpaceId(space))
                    }
                }
            }
        }

        override fun getState(): State {
            val space = currentSpace.value
            return if (space == NO_SPACE) {
                State.NoSpace
            } else {
                val config = info[space]
                if (config != null) {
                    State.Space.Active(config)
                } else {
                    State.Space.Idle(SpaceId(space))
                }
            }
        }

        override fun clear() {
            info.clear()
            currentSpace.value = NO_SPACE
        }

        companion object {
            const val NO_SPACE = ""
        }
    }

    sealed class State {
        data object Init: State()
        data object NoSpace: State()
        sealed class Space: State() {
            data class Idle(val space: SpaceId): Space()
            data class Active(val config: Config): Space()
        }
    }
}

@Deprecated("Do not use.")
suspend fun SpaceManager.getSpaceWithTechSpace(): List<Id> {
    val config = getConfig()
    return if (config != null) {
        listOf(config.space, config.techSpace)
    } else {
        listOf(get())
    }
}