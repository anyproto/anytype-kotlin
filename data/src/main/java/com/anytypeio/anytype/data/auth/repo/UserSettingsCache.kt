package com.anytypeio.anytype.data.auth.repo

interface UserSettingsCache {
    suspend fun setDefaultPageType(type: String)
    suspend fun getDefaultPageType(): String?
}