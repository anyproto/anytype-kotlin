package com.anytypeio.anytype.domain.wallpaper

import com.anytypeio.anytype.core_models.Wallpaper
import com.anytypeio.anytype.domain.base.Interactor
import com.anytypeio.anytype.domain.config.UserSettingsRepository

class SetWallpaper(
    private val repo: UserSettingsRepository
) : Interactor<SetWallpaper.Params>() {

    override suspend fun run(params: Params) {
        when (params) {
            is Params.Gradient -> {
                repo.setWallpaper(
                    Wallpaper.Gradient(params.code)
                )
                WallpaperStore.Default.set(Wallpaper.Gradient(params.code))
            }
            is Params.SolidColor -> {
                repo.setWallpaper(
                    Wallpaper.Color(params.code)
                )
                WallpaperStore.Default.set(Wallpaper.Color(params.code))
            }
        }
    }

    sealed class Params {
        data class SolidColor(val code: String) : Params()
        data class Gradient(val code: String) : Params()
    }
}