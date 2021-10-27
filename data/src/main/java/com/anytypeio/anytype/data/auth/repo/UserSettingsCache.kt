package com.anytypeio.anytype.data.auth.repo

interface UserSettingsCache {
    suspend fun setDefaultObjectType(type: String, name: String)
    suspend fun getDefaultObjectType(): Pair<String?, String?>
}