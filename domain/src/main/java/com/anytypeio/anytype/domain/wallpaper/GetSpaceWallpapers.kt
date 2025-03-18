package com.anytypeio.anytype.domain.wallpaper

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Wallpaper
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import javax.inject.Inject

class GetSpaceWallpapers @Inject constructor(
    private val repo: UserSettingsRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<Unit, Map<Id, Wallpaper>>(dispatchers.io) {

    override suspend fun doWork(params: Unit): Map<Id, Wallpaper> {
        return repo.getWallpapers()
    }
}