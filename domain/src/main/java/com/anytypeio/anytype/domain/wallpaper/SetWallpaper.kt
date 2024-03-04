package com.anytypeio.anytype.domain.wallpaper

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Wallpaper
import com.anytypeio.anytype.domain.base.Interactor
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import javax.inject.Inject

class SetWallpaper @Inject constructor(
    private val repo: UserSettingsRepository,
    private val store: WallpaperStore
) : Interactor<SetWallpaper.Params>() {

    override suspend fun run(params: Params) {
        when (params) {
            is Params.Gradient -> {
                repo.setWallpaper(
                    space = params.space,
                    wallpaper = Wallpaper.Gradient(params.code)
                )
                store.set(Wallpaper.Gradient(params.code))
            }
            is Params.SolidColor -> {
                repo.setWallpaper(
                    space = params.space,
                    wallpaper = Wallpaper.Color(params.code)
                )
               store.set(Wallpaper.Color(params.code))
            }
        }
    }

    sealed class Params {
        data class SolidColor(val space: Id, val code: String) : Params()
        data class Gradient(val space: Id, val code: String) : Params()
    }
}