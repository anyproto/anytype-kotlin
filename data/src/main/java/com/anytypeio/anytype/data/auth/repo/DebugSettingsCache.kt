package com.anytypeio.anytype.data.auth.repo

interface DebugSettingsCache {
    suspend fun enableAnytypeContextMenu()
    suspend fun disableAnytypeContextMenu()
    suspend fun getAnytypeContextMenu(): Boolean
}