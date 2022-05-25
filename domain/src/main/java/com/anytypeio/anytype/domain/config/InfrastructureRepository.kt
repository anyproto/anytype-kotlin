package com.anytypeio.anytype.domain.config

interface InfrastructureRepository {
    suspend fun enableAnytypeContextMenu()
    suspend fun disableAnytypeContextMenu()
    suspend fun getAnytypeContextMenu(): Boolean
}