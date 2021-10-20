package com.anytypeio.anytype.domain.config

interface UserSettingsRepository {
    suspend fun setDefaultPageType(type: String)
    suspend fun getDefaultPageType(): String?
}