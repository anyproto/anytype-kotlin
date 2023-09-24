package com.anytypeio.anytype.di.feature

import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.CrashReporter
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.core_utils.tools.FeatureToggles
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.domain.auth.interactor.CheckAuthorizationStatus
import com.anytypeio.anytype.domain.auth.interactor.GetLastOpenedObject
import com.anytypeio.anytype.domain.auth.interactor.LaunchAccount
import com.anytypeio.anytype.domain.auth.interactor.LaunchWallet
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.config.FeaturesConfigProvider
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.device.PathProvider
import com.anytypeio.anytype.domain.launch.GetDefaultPageType
import com.anytypeio.anytype.domain.launch.SetDefaultObjectType
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.page.CreateObject
import com.anytypeio.anytype.domain.platform.MetricsProvider
import com.anytypeio.anytype.domain.search.ObjectTypesSubscriptionManager
import com.anytypeio.anytype.domain.search.RelationsSubscriptionManager
import com.anytypeio.anytype.domain.templates.GetTemplates
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.domain.workspace.WorkspaceManager
import com.anytypeio.anytype.presentation.splash.SplashViewModelFactory
import com.anytypeio.anytype.ui.splash.SplashFragment
import dagger.Binds
import dagger.Component
import dagger.Module
import dagger.Provides

@Component(
    dependencies = [SplashDependencies::class],
    modules = [
        SplashModule::class,
        SplashModule.Declarations::class
    ]
)
@PerScreen
interface SplashComponent {

    @Component.Factory
    interface Factory {
        fun create(dependencies: SplashDependencies): SplashComponent
    }

    fun inject(fragment: SplashFragment)
}

@Module
object SplashModule {

    @JvmStatic
    @PerScreen
    @Provides
    fun provideCheckAuthorizationStatusUseCase(
        authRepository: AuthRepository
    ): CheckAuthorizationStatus = CheckAuthorizationStatus(
        repository = authRepository
    )

    @JvmStatic
    @PerScreen
    @Provides
    fun provideLaunchAccountUseCase(
        authRepository: AuthRepository,
        pathProvider: PathProvider,
        featuresConfigProvider: FeaturesConfigProvider,
        configStorage: ConfigStorage,
        spaceManager: SpaceManager,
        metricsProvider: MetricsProvider,
        userSettings: UserSettingsRepository
    ): LaunchAccount = LaunchAccount(
        repository = authRepository,
        pathProvider = pathProvider,
        featuresConfigProvider = featuresConfigProvider,
        configStorage = configStorage,
        spaceManager = spaceManager,
        metricsProvider = metricsProvider,
        settings = userSettings
    )

    @JvmStatic
    @PerScreen
    @Provides
    fun provideLaunchWalletUseCase(
        authRepository: AuthRepository,
        pathProvider: PathProvider
    ): LaunchWallet =
        LaunchWallet(
            repository = authRepository,
            pathProvider = pathProvider
        )

    @JvmStatic
    @Provides
    @PerScreen
    fun getLastOpenedObject(
        repo: BlockRepository,
        auth: AuthRepository
    ): GetLastOpenedObject = GetLastOpenedObject(
        authRepo = auth,
        blockRepo = repo
    )

    @JvmStatic
    @PerScreen
    @Provides
    fun provideGetDefaultPageType(
        userSettingsRepository: UserSettingsRepository,
        blockRepository: BlockRepository,
        dispatchers: AppCoroutineDispatchers,
        spaceManager: SpaceManager,
        configStorage: ConfigStorage
    ): GetDefaultPageType = GetDefaultPageType(
        userSettingsRepository = userSettingsRepository,
        blockRepository = blockRepository,
        spaceManager = spaceManager,
        dispatchers = dispatchers,
        configStorage = configStorage
    )

    @JvmStatic
    @PerScreen
    @Provides
    fun provideSetDefaultPageType(
        repo: UserSettingsRepository,
        dispatchers: AppCoroutineDispatchers
    ): SetDefaultObjectType = SetDefaultObjectType(
        repo = repo,
        dispatchers = dispatchers
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun getCreateObject(
        repo: BlockRepository,
        getDefaultPageType: GetDefaultPageType,
        dispatchers: AppCoroutineDispatchers,
        spaceManager: SpaceManager
    ): CreateObject = CreateObject(
        repo = repo,
        getDefaultPageType = getDefaultPageType,
        dispatchers = dispatchers,
        spaceManager = spaceManager
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideGetTemplates(
        repo: BlockRepository,
        dispatchers: AppCoroutineDispatchers
    ): GetTemplates = GetTemplates(
        repo = repo,
        dispatchers = dispatchers
    )

    @Module
    interface Declarations {
        @PerScreen
        @Binds
        fun bindViewModelFactory(factory: SplashViewModelFactory): ViewModelProvider.Factory
    }

}

interface SplashDependencies : ComponentDependencies {
    fun blockRepository(): BlockRepository
    fun workspaceManager(): WorkspaceManager
    fun urlBuilder(): UrlBuilder
    fun analytics(): Analytics
    fun relationsSubscriptionManager(): RelationsSubscriptionManager
    fun objectTypesSubscriptionManager(): ObjectTypesSubscriptionManager
    fun authRepository(): AuthRepository
    fun pathProvider(): PathProvider
    fun featuresConfigProvider(): FeaturesConfigProvider
    fun featureToggles(): FeatureToggles
    fun configStorage(): ConfigStorage
    fun userSettingsRepository(): UserSettingsRepository
    fun dispatchers(): AppCoroutineDispatchers
    fun crashReporter(): CrashReporter
    fun metricsProvider(): MetricsProvider
    fun spaceManager(): SpaceManager
}