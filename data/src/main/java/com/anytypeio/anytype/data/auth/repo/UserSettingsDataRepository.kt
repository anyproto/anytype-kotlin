package com.anytypeio.anytype.data.auth.repo

import com.anytypeio.anytype.domain.config.UserSettingsRepository

class UserSettingsDataRepository(private val cache: UserSettingsCache) : UserSettingsRepository {

    override suspend fun setDefaultPageType(type: String) {
        cache.setDefaultPageType(type)
    }

    override suspend fun getDefaultPageType(): String? = cache.getDefaultPageType()
}