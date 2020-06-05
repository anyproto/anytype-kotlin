package com.agileburo.anytype.data.auth.repo

import com.agileburo.anytype.domain.config.InfrastructureRepository

class InfrastructureDataRepository(
    private val cache: DebugSettingsCache
) : InfrastructureRepository {

    override suspend fun enableAnytypeContextMenu() {
        cache.enableAnytypeContextMenu()
    }

    override suspend fun disableAnytypeContextMenu() {
        cache.disableAnytypeContextMenu()
    }

    override suspend fun getAnytypeContextMenu(): Boolean = cache.getAnytypeContextMenu()
}