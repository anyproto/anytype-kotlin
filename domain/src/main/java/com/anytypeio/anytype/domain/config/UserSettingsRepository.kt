package com.anytypeio.anytype.domain.config

interface UserSettingsRepository {
    suspend fun setDefaultObjectType(type: String, name: String)
    suspend fun getDefaultObjectType(): Pair<String?, String?>
}