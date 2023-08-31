package com.anytypeio.anytype.domain.workspace

import com.anytypeio.anytype.core_models.Config
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.ConfigStorage
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
    suspend fun getConfig(): Config?
    suspend fun set(space: Id)
    fun observe() : Flow<Config>
    fun clear()

    class Impl @Inject constructor(
        private val repo: BlockRepository,
        private val dispatchers: AppCoroutineDispatchers,
        private val configStorage: ConfigStorage
    ) : SpaceManager {

        private val currentSpace = MutableStateFlow("")
        private val info = mutableMapOf<Id, Config>()

        override suspend fun get(): Id {
            val curr = currentSpace.value
            return curr.ifEmpty {
                configStorage.getOrNull()?.space.orEmpty()
            }
        }

        override suspend fun getConfig(): Config? {
            // TODO MULTISPACES refact Add fallback logic
            val curr = currentSpace.value
            return if (curr.isNotEmpty()) {
                info[curr]
            } else {
                configStorage.getOrNull()
            }
        }

        override suspend fun set(space: Id)  = withContext(dispatchers.io) {
            if (!info.containsKey(space)) {
                val config = repo.getSpaceConfig(space)
                info[space] = config
            }
            currentSpace.value = space
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
            TODO("Not yet implemented")
        }
    }
}