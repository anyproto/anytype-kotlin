package com.anytypeio.anytype.domain.wallpaper

import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.config.UserSettingsRepository

class RestoreWallpaper(
    private val repo: UserSettingsRepository
) : BaseUseCase<Unit, BaseUseCase.None>() {
    override suspend fun run(params: None) = safe {
        val restored = repo.getWallpaper()
        WallpaperStore.Default.set(restored)
    }
}