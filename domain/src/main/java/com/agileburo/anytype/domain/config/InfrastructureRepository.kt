package com.agileburo.anytype.domain.config

interface InfrastructureRepository {
    suspend fun enableAnytypeContextMenu()
    suspend fun disableAnytypeContextMenu()
    suspend fun getAnytypeContextMenu(): Boolean
}