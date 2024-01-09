package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.domain.account.AccountStatusChannel
import com.anytypeio.anytype.domain.account.AwaitAccountStartManager
import com.anytypeio.anytype.domain.account.InterceptAccountStatus
import com.anytypeio.anytype.domain.auth.interactor.CheckAuthorizationStatus
import com.anytypeio.anytype.domain.auth.interactor.Logout
import com.anytypeio.anytype.domain.auth.interactor.ResumeAccount
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.config.FeaturesConfigProvider
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.device.PathProvider
import com.anytypeio.anytype.domain.misc.LocaleProvider
import com.anytypeio.anytype.domain.platform.MetricsProvider
import com.anytypeio.anytype.domain.search.ObjectTypesSubscriptionManager
import com.anytypeio.anytype.domain.search.RelationsSubscriptionManager
import com.anytypeio.anytype.domain.spaces.SpaceDeletedStatusWatcher
import com.anytypeio.anytype.domain.theme.GetTheme
import com.anytypeio.anytype.domain.wallpaper.ObserveWallpaper
import com.anytypeio.anytype.domain.wallpaper.RestoreWallpaper
import com.anytypeio.anytype.domain.wallpaper.WallpaperStore
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.main.MainViewModelFactory
import com.anytypeio.anytype.ui.main.MainActivity
import com.anytypeio.anytype.ui_settings.appearance.ThemeApplicator
import com.anytypeio.anytype.ui_settings.appearance.ThemeApplicatorImpl
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

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
        resumeAccount: ResumeAccount,
        analytics: Analytics,
        observeWallpaper: ObserveWallpaper,
        restoreWallpaper: RestoreWallpaper,
        interceptAccountStatus: InterceptAccountStatus,
        logout: Logout,
        relationsSubscriptionManager: RelationsSubscriptionManager,
        objectTypesSubscriptionManager: ObjectTypesSubscriptionManager,
        checkAuthorizationStatus: CheckAuthorizationStatus,
        configStorage: ConfigStorage,
        spaceDeletedStatusWatcher: SpaceDeletedStatusWatcher,
        localeProvider: LocaleProvider
    ): MainViewModelFactory = MainViewModelFactory(
        resumeAccount = resumeAccount,
        analytics = analytics,
        observeWallpaper = observeWallpaper,
        restoreWallpaper = restoreWallpaper,
        interceptAccountStatus = interceptAccountStatus,
        logout = logout,
        relationsSubscriptionManager = relationsSubscriptionManager,
        objectTypesSubscriptionManager = objectTypesSubscriptionManager,
        checkAuthorizationStatus = checkAuthorizationStatus,
        configStorage = configStorage,
        spaceDeletedStatusWatcher = spaceDeletedStatusWatcher,
        localeProvider = localeProvider
    )

    @JvmStatic
    @PerScreen
    @Provides
    fun provideResumeAccountUseCase(
        authRepository: AuthRepository,
        pathProvider: PathProvider,
        configStorage: ConfigStorage,
        featuresConfigProvider: FeaturesConfigProvider,
        metricsProvider: MetricsProvider,
        awaitAccountStartManager: AwaitAccountStartManager
    ): ResumeAccount = ResumeAccount(
        repository = authRepository,
        pathProvider = pathProvider,
        configStorage = configStorage,
        featuresConfigProvider = featuresConfigProvider,
        metricsProvider = metricsProvider,
        awaitAccountStartManager = awaitAccountStartManager
    )

    @JvmStatic
    @PerScreen
    @Provides
    fun provideObserveWallpaperUseCase(): ObserveWallpaper = ObserveWallpaper()

    @JvmStatic
    @PerScreen
    @Provides
    fun provideRestoreWallpaperUseCase(
        repo: UserSettingsRepository,
        spaceManager: SpaceManager,
        dispatchers: AppCoroutineDispatchers
    ): RestoreWallpaper = RestoreWallpaper(
        repo = repo,
        spaceManager = spaceManager,
        store = WallpaperStore.Default,
        dispatchers = dispatchers
    )

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
        channel: AccountStatusChannel,
        dispatchers: AppCoroutineDispatchers
    ): InterceptAccountStatus = InterceptAccountStatus(
        channel = channel,
        dispatchers = dispatchers
    )

    @JvmStatic
    @PerScreen
    @Provides
    fun provideLogoutUseCase(
        repo: AuthRepository,
        provider: ConfigStorage,
        user: UserSettingsRepository,
        dispatchers: AppCoroutineDispatchers,
        spaceManager: SpaceManager,
        awaitAccountStartManager: AwaitAccountStartManager
    ): Logout = Logout(
        repo = repo,
        user = user,
        config = provider,
        dispatchers = dispatchers,
        spaceManager = spaceManager,
        awaitAccountStartManager = awaitAccountStartManager
    )

    @JvmStatic
    @PerScreen
    @Provides
    fun provideCheckAuthStatus(
        repo: AuthRepository
    ): CheckAuthorizationStatus = CheckAuthorizationStatus(repo)
}