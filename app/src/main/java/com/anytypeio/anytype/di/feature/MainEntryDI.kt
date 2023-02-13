package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.domain.account.AccountStatusChannel
import com.anytypeio.anytype.domain.account.InterceptAccountStatus
import com.anytypeio.anytype.domain.auth.interactor.Logout
import com.anytypeio.anytype.domain.auth.interactor.ResumeAccount
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.config.FeaturesConfigProvider
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.device.PathProvider
import com.anytypeio.anytype.domain.search.ObjectTypesSubscriptionManager
import com.anytypeio.anytype.domain.search.RelationsSubscriptionManager
import com.anytypeio.anytype.domain.theme.GetTheme
import com.anytypeio.anytype.domain.wallpaper.ObserveWallpaper
import com.anytypeio.anytype.domain.wallpaper.RestoreWallpaper
import com.anytypeio.anytype.domain.workspace.WorkspaceManager
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
        resumeAccount: ResumeAccount,
        analytics: Analytics,
        observeWallpaper: ObserveWallpaper,
        restoreWallpaper: RestoreWallpaper,
        interceptAccountStatus: InterceptAccountStatus,
        logout: Logout,
        relationsSubscriptionManager: RelationsSubscriptionManager,
        objectTypesSubscriptionManager: ObjectTypesSubscriptionManager
    ): MainViewModelFactory = MainViewModelFactory(
        resumeAccount = resumeAccount,
        analytics = analytics,
        observeWallpaper = observeWallpaper,
        restoreWallpaper = restoreWallpaper,
        interceptAccountStatus = interceptAccountStatus,
        logout = logout,
        relationsSubscriptionManager = relationsSubscriptionManager,
        objectTypesSubscriptionManager = objectTypesSubscriptionManager
    )

    @JvmStatic
    @PerScreen
    @Provides
    fun provideResumeAccountUseCase(
        authRepository: AuthRepository,
        pathProvider: PathProvider,
        configStorage: ConfigStorage,
        featuresConfigProvider: FeaturesConfigProvider,
        workspaceManager: WorkspaceManager
    ): ResumeAccount = ResumeAccount(
        repository = authRepository,
        pathProvider = pathProvider,
        configStorage = configStorage,
        featuresConfigProvider = featuresConfigProvider,
        workspaceManager = workspaceManager
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
        dispatchers: AppCoroutineDispatchers
    ): Logout = Logout(
        repo,
        provider,
        dispatchers
    )
}