package com.anytypeio.anytype.domain.wallpaper

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Wallpaper
import com.anytypeio.anytype.core_models.Wallpaper.Color
import com.anytypeio.anytype.core_models.Wallpaper.Gradient
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import javax.inject.Inject

class SetWallpaper @Inject constructor(
    private val repo: UserSettingsRepository,
    private val store: WallpaperStore,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<SetWallpaper.Params, Unit>(dispatchers.io) {

    override suspend fun doWork(params: Params) {
        when (params) {
            is Params.Gradient -> {
                repo.setWallpaper(
                    space = params.space,
                    wallpaper = Gradient(params.code)
                )
                store.set(Gradient(params.code))
            }

            is Params.SolidColor -> {
                repo.setWallpaper(
                    space = params.space,
                    wallpaper = Color(params.code)
                )
                store.set(Color(params.code))
            }

            is Params.Clear -> {
                repo.setWallpaper(
                    space = params.space,
                    wallpaper = Wallpaper.Default
                )
                store.set(Wallpaper.Default)
            }
        }
    }

    sealed class Params {
        data class Clear(val space: Id) : Params()
        data class SolidColor(val space: Id, val code: String) : Params()
        data class Gradient(val space: Id, val code: String) : Params()
    }
}