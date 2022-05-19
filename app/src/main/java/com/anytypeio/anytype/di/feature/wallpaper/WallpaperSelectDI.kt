package com.anytypeio.anytype.di.feature.wallpaper

import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.device.DefaultGradientCollectionProvider
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.cover.GetCoverGradientCollection
import com.anytypeio.anytype.domain.wallpaper.SetWallpaper
import com.anytypeio.anytype.presentation.wallpaper.WallpaperSelectViewModel
import com.anytypeio.anytype.ui.dashboard.WallpaperSelectFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@Subcomponent(
    modules = [WallpaperSelectModule::class]
)
@PerScreen
interface WallpaperSelectSubComponent {

    @Subcomponent.Builder
    interface Builder {
        fun module(module: WallpaperSelectModule): Builder
        fun build(): WallpaperSelectSubComponent
    }

    fun inject(fragment: WallpaperSelectFragment)
}

@Module
object WallpaperSelectModule {

    @JvmStatic
    @Provides
    @PerScreen
    fun provideViewModelFactory(
        setWallpaper: SetWallpaper,
        analytics: Analytics
    ): WallpaperSelectViewModel.Factory = WallpaperSelectViewModel.Factory(
        setWallpaper = setWallpaper,
        analytics = analytics,
        getGradients = GetCoverGradientCollection(DefaultGradientCollectionProvider)
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideSetWallpaper(repo: UserSettingsRepository) : SetWallpaper = SetWallpaper(repo)
}