package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.domain.account.AccountStatusChannel
import com.anytypeio.anytype.domain.account.InterceptAccountStatus
import com.anytypeio.anytype.domain.auth.interactor.LaunchAccount
import com.anytypeio.anytype.domain.auth.interactor.Logout
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.config.FeaturesConfigProvider
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.device.PathProvider
import com.anytypeio.anytype.domain.theme.GetTheme
import com.anytypeio.anytype.domain.wallpaper.ObserveWallpaper
import com.anytypeio.anytype.domain.wallpaper.RestoreWallpaper
import com.anytypeio.anytype.presentation.main.MainViewModelFactory
import com.anytypeio.anytype.ui.main.MainActivity
import com.anytypeio.anytype.ui_settings.appearance.ThemeApplicator
import com.anytypeio.anytype.ui_settings.appearance.ThemeApplicatorImpl
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import kotlinx.coroutines.Dispatchers

@Subcomponent(
    modules = [MainEntryModule::class]
)
@PerScreen
interface MainEntrySubComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): MainEntrySubComponent
        fun module(module: MainEntryModule): Builder
    }

    fun inject(activity: MainActivity)
}

@Module
object MainEntryModule {
    @JvmStatic
    @PerScreen
    @Provides
    fun provideMainViewModelFactory(
        launchAccount: LaunchAccount,
        analytics: Analytics,
        observeWallpaper: ObserveWallpaper,
        restoreWallpaper: RestoreWallpaper,
        interceptAccountStatus: InterceptAccountStatus,
        logout: Logout
    ): MainViewModelFactory = MainViewModelFactory(
        launchAccount = launchAccount,
        analytics = analytics,
        observeWallpaper = observeWallpaper,
        restoreWallpaper = restoreWallpaper,
        interceptAccountStatus = interceptAccountStatus,
        logout = logout
    )

    @JvmStatic
    @PerScreen
    @Provides
    fun provideLaunchAccountUseCase(
        authRepository: AuthRepository,
        pathProvider: PathProvider,
        featuresConfigProvider: FeaturesConfigProvider
    ): LaunchAccount = LaunchAccount(
        repository = authRepository,
        pathProvider = pathProvider,
        featuresConfigProvider = featuresConfigProvider
    )

    @JvmStatic
    @PerScreen
    @Provides
    fun provideObserveWallpaperUseCase(): ObserveWallpaper = ObserveWallpaper()

    @JvmStatic
    @PerScreen
    @Provides
    fun provideRestoreWallpaperUseCase(
        repo: UserSettingsRepository
    ): RestoreWallpaper = RestoreWallpaper(repo)

    @JvmStatic
    @PerScreen
    @Provides
    fun provideSetupThemeUseCase(
        repo: UserSettingsRepository
    ): GetTheme = GetTheme(repo)

    @JvmStatic
    @PerScreen
    @Provides
    fun provideThemeApplicator(): ThemeApplicator = ThemeApplicatorImpl()

    @JvmStatic
    @PerScreen
    @Provides
    fun provideInterceptAccountStatus(
        channel: AccountStatusChannel
    ): InterceptAccountStatus = InterceptAccountStatus(
        channel = channel,
        dispatchers = AppCoroutineDispatchers(
            io = Dispatchers.IO,
            computation = Dispatchers.Default,
            main = Dispatchers.Main
        )
    )

    @JvmStatic
    @PerScreen
    @Provides
    fun provideLogoutUseCase(repo: AuthRepository): Logout = Logout(
        repo = repo
    )
}