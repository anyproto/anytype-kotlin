package com.anytypeio.anytype.domain.wallpaper

import com.anytypeio.anytype.core_models.Wallpaper
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.base.FlowUseCase
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveWallpaper @Inject constructor () : FlowUseCase<Wallpaper, BaseUseCase.None>() {
    override fun build(params: BaseUseCase.None?): Flow<Wallpaper> {
        return WallpaperStore.Default.observe()
    }
}