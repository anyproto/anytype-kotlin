package com.anytypeio.anytype.domain.workspace

import com.anytypeio.anytype.core_models.Id
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

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