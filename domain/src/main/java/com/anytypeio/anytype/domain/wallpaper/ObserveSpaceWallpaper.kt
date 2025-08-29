package com.anytypeio.anytype.domain.wallpaper

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Wallpaper
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.FlowInteractor
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import javax.inject.Inject

class ObserveSpaceWallpaper @Inject constructor(
    private val userSettingsRepository: UserSettingsRepository,
    dispatchers: AppCoroutineDispatchers
) : FlowInteractor<ObserveSpaceWallpaper.Params, Wallpaper>(dispatchers.io) {

    override fun build(): Flow<Wallpaper> {
        return userSettingsRepository.observeWallpaper("")
            .catch { 
                emit(Wallpaper.Default)
            }
    }

    override fun build(params: Params): Flow<Wallpaper> {
        return userSettingsRepository.observeWallpaper(params.space)
    }

    data class Params(
        val space: Id
    )
}