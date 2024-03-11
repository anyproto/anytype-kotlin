package com.anytypeio.anytype.domain.workspace

import com.anytypeio.anytype.core_models.Config
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.PERSONAL_SPACE_TYPE
import com.anytypeio.anytype.core_models.PRIVATE_SPACE_TYPE
import com.anytypeio.anytype.core_models.SpaceType
import com.anytypeio.anytype.core_models.UNKNOWN_SPACE_TYPE
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.debugging.Logger
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

@Deprecated("To be deleted")
interface WorkspaceManager {
    suspend fun getCurrentWorkspace(): Id
    suspend fun setCurrentWorkspace(id: Id)
    class DefaultWorkspaceManager : WorkspaceManager {
        private val mutex = Mutex()
        private lateinit var workspace: Id
        override suspend fun getCurrentWorkspace(): Id = mutex.withLock { workspace }
        override suspend fun setCurrentWorkspace(id: Id) = mutex.withLock { workspace = id }
    }
}

interface SpaceManager {

    suspend fun get(): Id
    suspend fun set(space: Id): Result<Config>
    fun getConfig(): Config?
    fun getConfig(space: SpaceId) : Config?
    fun getType(space: Id): SpaceType
    fun observe() : Flow<Config>
    fun clear()

    class Impl @Inject constructor(
        private val repo: BlockRepository,
        private val dispatchers: AppCoroutineDispatchers,
        private val configStorage: ConfigStorage,
        private val logger: Logger
    ) : SpaceManager {

        private val currentSpace = MutableStateFlow(NO_SPACE_OR_DEFAULT)
        private val info = mutableMapOf<Id, Config>()

        override suspend fun get(): Id {
            val curr = currentSpace.value
            return curr.ifEmpty {
                configStorage.getOrNull()?.space.orEmpty()
            }
        }

        override fun getConfig(): Config? {
            val curr = currentSpace.value
            return if (curr.isNotEmpty()) {
                info[curr]
            } else {
                configStorage.getOrNull()
            }
        }

        override fun getType(space: Id): SpaceType {
            val accountConfig = configStorage.getOrNull()
            return if (accountConfig != null) {
                if (space == accountConfig.space)
                    PERSONAL_SPACE_TYPE
                else
                    PRIVATE_SPACE_TYPE
            } else {
                UNKNOWN_SPACE_TYPE
            }
        }

        override fun getConfig(space: SpaceId): Config? {
            return info[space.id]
        }

        override suspend fun set(space: Id) : Result<Config> = withContext(dispatchers.io) {
            runCatching { repo.getSpaceConfig(space) }.also { result ->
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
                    configStorage.getOrNull()
                } else {
                    val config = info[space]
                    if (config != null)
                        config
                    else {
                        val default = configStorage.getOrNull()
                        if (default != null && default.space == space)
                            default
                        else
                            null
                    }
                }
            }
        }

        override fun clear() {
            info.clear()
            currentSpace.value = NO_SPACE_OR_DEFAULT
        }

        companion object {
            const val NO_SPACE_OR_DEFAULT = ""
        }
    }
}

suspend fun SpaceManager.getSpaceWithTechSpace(): List<Id> {
    val config = getConfig()
    return if (config != null) {
        listOf(config.space, config.techSpace)
    } else {
        listOf(get())
    }
}