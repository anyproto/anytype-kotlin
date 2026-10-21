package com.anytypeio.anytype.domain.workspace

import com.anytypeio.anytype.core_models.Config
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.debugging.Logger
import javax.inject.Inject
import kotlin.math.log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.withContext

/**
 * Maybe convert to AppStateManager, with different states.
 */
interface SpaceManager {

    suspend fun get(): Id
    suspend fun set(space: Id, withChat: Boolean = false): Result<Config>

    /**
     * Points the app's space-scoped subscriptions (types, relations, relation options, sync
     * status) at [space] without opening the workspace, leaving the manager in
     * [State.Space.Idle] — active, config unknown.
     *
     * Workspace.Open exists to produce a [Config], and on a cold space it is expensive:
     * measured at ~5s on device, against ~1.2s for the Object.Create that follows it. Every
     * subscription built off the active space needs nothing from that config but the space
     * id, and Object.Create/Object.Open carry the space themselves — so a screen that only
     * writes an object into a space (quick capture) can activate it and skip the open. Use
     * [set] wherever the config itself is needed: widgets, home, tech space, profile.
     */
    fun activate(space: Id)

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

        /**
         * A flow rather than a plain map because a config can arrive for a space that is
         * already the current one — a full [set] after a lightweight [activate]. With a map,
         * [state] and [observe] are driven by [currentSpace] alone, which does not change in
         * that case, so both would stay stuck on the id-only reading forever.
         */
        private val configs = MutableStateFlow<Map<Id, Config>>(emptyMap())

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
                configs.value[curr]
            } else {
                null
            }
        }

        override fun getConfig(space: SpaceId): Config? {
            return configs.value[space.id]
        }

        override fun activate(space: Id) {
            logger.logInfo("SPACE MANAGER: activating space without workspace open: $space")
            // Deliberately does not touch [configs]: a config this space already has (opened
            // earlier in the session) stays valid and keeps the state Active.
            currentSpace.value = space
        }

        override suspend fun set(space: Id, withChat: Boolean) : Result<Config> = withContext(dispatchers.io) {
            logger.logInfo("SPACE MANAGER: setting space: $space")
            runCatching { repo.spaceOpen(space, withChat) }.also { result ->
                result.fold(
                    onSuccess = { config ->
                        logger.logInfo("SPACE MANAGER: space opened: $space")
                        configs.value = configs.value + (space to config)
                        currentSpace.value = space
                    },
                    onFailure = { error ->
                        if (error.isSpaceNotReady()) {
                            logger.logWarning(
                                "SPACE MANAGER: space is not ready: $space, reason: ${error.message}"
                            )
                        } else {
                            logger.logException(error, "SPACE MANAGER: failed to open space: $space")
                        }
                    }
                )
            }
        }

        override fun observe(): Flow<Config> {
            return combine(currentSpace, configs) { space, known ->
                if (space.isEmpty()) null else known[space]
            }.filterNotNull()
        }

        override fun observe(space: SpaceId): Flow<Config> {
            return combine(currentSpace, configs) { _, known ->
                known[space.id]
            }.filterNotNull()
        }

        override fun state(): Flow<State> {
            return combine(currentSpace, configs) { space, known ->
                stateOf(space, known)
            }
        }

        override fun getState(): State = stateOf(currentSpace.value, configs.value)

        private fun stateOf(space: Id, known: Map<Id, Config>): State {
            if (space == NO_SPACE) return State.NoSpace
            val config = known[space]
            return if (config != null) {
                State.Space.Active(config)
            } else {
                State.Space.Idle(SpaceId(space))
            }
        }

        override fun clear() {
            configs.value = emptyMap()
            currentSpace.value = NO_SPACE
        }

        companion object {
            const val NO_SPACE = ""
            private const val SPACE_NOT_READY_ERROR = "space is not ready"

            private fun Throwable.isSpaceNotReady(): Boolean {
                return message?.contains(SPACE_NOT_READY_ERROR, ignoreCase = true) == true
            }
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

/**
 * The space the state points at, or null when there is none to follow.
 *
 * [SpaceManager.State.Space.Active] and [SpaceManager.State.Space.Idle] answer with the same
 * id on purpose: subscriptions are built from the id alone, so a config arriving after an
 * [SpaceManager.activate] must not restart them.
 */
fun SpaceManager.State.spaceIdOrNull(): SpaceId? = when (this) {
    is SpaceManager.State.Space.Active -> SpaceId(config.space)
    is SpaceManager.State.Space.Idle -> space
    is SpaceManager.State.NoSpace -> null
    is SpaceManager.State.Init -> null
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
