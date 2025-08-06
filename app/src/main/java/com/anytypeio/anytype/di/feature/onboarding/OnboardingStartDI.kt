package com.anytypeio.anytype.di.feature.onboarding

import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.CrashReporter
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.di.feature.onboarding.signup.SoulCreationScreenScope
import com.anytypeio.anytype.domain.account.AwaitAccountStartManager
import com.anytypeio.anytype.domain.auth.interactor.CreateAccount
import com.anytypeio.anytype.domain.auth.interactor.SetupWallet
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.deeplink.PendingIntentStore
import com.anytypeio.anytype.domain.device.PathProvider
import com.anytypeio.anytype.domain.misc.LocaleProvider
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider
import com.anytypeio.anytype.domain.`object`.ImportGetStartedUseCase
import com.anytypeio.anytype.domain.platform.InitialParamsProvider
import com.anytypeio.anytype.domain.resources.StringResourceProvider
import com.anytypeio.anytype.domain.search.RelationsSubscriptionManager
import com.anytypeio.anytype.domain.spaces.SpaceDeletedStatusWatcher
import com.anytypeio.anytype.domain.subscriptions.GlobalSubscriptionManager
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.onboarding.OnboardingStartViewModel
import com.anytypeio.anytype.presentation.spaces.SpaceGradientProvider
import dagger.Binds
import dagger.Component
import dagger.Module
import dagger.Provides
import javax.inject.Scope

@Component(
    dependencies = [OnboardingStartDependencies::class],
    modules = [
        OnboardingStartModule::class,
        OnboardingStartModule.Declarations::class
    ]
)
@AuthScreenScope
interface OnboardingStartComponent {
    @Component.Factory
    interface Builder {
        fun create(dependencies: OnboardingStartDependencies): OnboardingStartComponent
    }

    fun getViewModel(): OnboardingStartViewModel
}

@Module
object OnboardingStartModule {

    @JvmStatic
    @Provides
    @AuthScreenScope
    fun gradientProvider(): SpaceGradientProvider = SpaceGradientProvider.Default

    @JvmStatic
    @Provides
    @AuthScreenScope
    fun provideCreateAccountUseCase(
        authRepository: AuthRepository,
        configStorage: ConfigStorage,
        initialParamsProvider: InitialParamsProvider,
        awaitAccountStartManager: AwaitAccountStartManager,
        spaceManager: SpaceManager,
        dispatchers: AppCoroutineDispatchers
    ): CreateAccount = CreateAccount(
        repository = authRepository,
        configStorage = configStorage,
        initialParamsProvider = initialParamsProvider,
        dispatcher = dispatchers,
        awaitAccountStartManager = awaitAccountStartManager,
        spaceManager = spaceManager
    )

    @JvmStatic
    @Provides
    @AuthScreenScope
    fun provideSetupWalletUseCase(
        authRepository: AuthRepository,
        dispatchers: AppCoroutineDispatchers
    ): SetupWallet = SetupWallet(
        repository = authRepository,
        dispatchers = dispatchers
    )

    @JvmStatic
    @Provides
    @AuthScreenScope
    fun provideSetupSkipUseCase(
        repository: BlockRepository,
        dispatchers: AppCoroutineDispatchers
    ) = ImportGetStartedUseCase(
        repo = repository,
        dispatchers = dispatchers
    )

    @Module
    interface Declarations {
        @Binds
        @AuthScreenScope
        fun bindViewModelFactory(factory: OnboardingStartViewModel.Factory): ViewModelProvider.Factory
    }
}

interface OnboardingStartDependencies : ComponentDependencies {
    fun analytics(): Analytics
    fun blockRepository(): BlockRepository
    fun dispatchers(): AppCoroutineDispatchers
    fun configStorage(): ConfigStorage
    fun authRepository(): AuthRepository
    fun userSettings(): UserSettingsRepository
    fun relationsSubscriptionManager(): RelationsSubscriptionManager
    fun pathProvider(): PathProvider
    fun metricsProvider(): InitialParamsProvider
    fun crashReporter(): CrashReporter
    fun spaceManager(): SpaceManager
    fun spaceStatusWatcher(): SpaceDeletedStatusWatcher
    fun localeProvider(): LocaleProvider
    fun userPermissionProvider(): UserPermissionProvider
    fun awaitAccountStartManager(): AwaitAccountStartManager
    fun globalSubscriptionManager(): GlobalSubscriptionManager
    fun stringResourceProvider(): StringResourceProvider
    fun providePendingIntentStore(): PendingIntentStore
}

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class AuthScreenScope