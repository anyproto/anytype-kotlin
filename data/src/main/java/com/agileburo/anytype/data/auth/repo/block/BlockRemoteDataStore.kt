package com.agileburo.anytype.data.auth.repo.block

class BlockRemoteDataStore(private val remote: BlockRemote) : BlockDataStore {

    override suspend fun openDashboard(contextId: String, id: String) {
        remote.openDashboard(id = id, contextId = contextId)
    }

    override suspend fun observeBlocks() = remote.observeBlocks()
    override suspend fun observePages() = remote.observePages()

    override suspend fun openPage(id: String) {
        remote.openPage(id)
    }
}