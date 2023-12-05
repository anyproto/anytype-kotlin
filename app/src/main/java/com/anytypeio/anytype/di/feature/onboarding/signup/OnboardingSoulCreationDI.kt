package com.anytypeio.anytype.di.feature.onboarding.signup

import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.CrashReporter
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.domain.auth.interactor.CreateAccount
import com.anytypeio.anytype.domain.auth.interactor.SetupWallet
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.device.PathProvider
import com.anytypeio.anytype.domain.misc.LocaleProvider
import com.anytypeio.anytype.domain.`object`.SetupMobileUseCaseSkip
import com.anytypeio.anytype.domain.platform.MetricsProvider
import com.anytypeio.anytype.domain.search.ObjectTypesSubscriptionManager
import com.anytypeio.anytype.domain.search.RelationsSubscriptionManager
import com.anytypeio.anytype.domain.spaces.SpaceDeletedStatusWatcher
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.onboarding.signup.OnboardingSetProfileNameViewModel
import com.anytypeio.anytype.presentation.spaces.SpaceGradientProvider
import dagger.Binds
import dagger.Component
import dagger.Module
import dagger.Provides
import javax.inject.Scope

@Component(
    dependencies = [OnboardingSoulCreationDependencies::class],
    modules = [
        OnboardingSoulCreationModule::class,
        OnboardingSoulCreationModule.Declarations::class
    ]
)
@SoulCreationScreenScope
interface OnboardingSoulCreationComponent {

    @Component.Factory
    interface Builder {
        fun create(dependencies: OnboardingSoulCreationDependencies): OnboardingSoulCreationComponent
    }

    fun getViewModel(): OnboardingSetProfileNameViewModel
}

@Module
object OnboardingSoulCreationModule {

    @JvmStatic
    @Provides
    @SoulCreationScreenScope
    fun gradientProvider(): SpaceGradientProvider = SpaceGradientProvider.Default

    @JvmStatic
    @Provides
    @SoulCreationScreenScope
    fun provideCreateAccountUseCase(
        authRepository: AuthRepository,
        configStorage: ConfigStorage,
        metricsProvider: MetricsProvider
    ): CreateAccount = CreateAccount(
        repository = authRepository,
        configStorage = configStorage,
        metricsProvider = metricsProvider
    )

    @JvmStatic
    @Provides
    @SoulCreationScreenScope
    fun provideSetupWalletUseCase(
        authRepository: AuthRepository
    ): SetupWallet = SetupWallet(
        repository = authRepository
    )

    @JvmStatic
    @Provides
    @SoulCreationScreenScope
    fun provideSetupSkipUseCase(
        repository: BlockRepository,
        dispatchers: AppCoroutineDispatchers
    ) = SetupMobileUseCaseSkip(
        repo = repository,
        dispatchers = dispatchers
    )

    @Module
    interface Declarations {
        @Binds
        @SoulCreationScreenScope
        fun bindViewModelFactory(factory: OnboardingSetProfileNameViewModel.Factory): ViewModelProvider.Factory
    }
}

interface OnboardingSoulCreationDependencies : ComponentDependencies {
    fun blockRepository(): BlockRepository
    fun dispatchers(): AppCoroutineDispatchers
    fun configStorage(): ConfigStorage
    fun analytics(): Analytics
    fun authRepository(): AuthRepository
    fun userSettings(): UserSettingsRepository
    fun relationsSubscriptionManager(): RelationsSubscriptionManager
    fun objectTypesSubscriptionManager(): ObjectTypesSubscriptionManager
    fun pathProvider(): PathProvider
    fun metricsProvider(): MetricsProvider
    fun crashReporter(): CrashReporter
    fun spaceManager(): SpaceManager
    fun spaceStatusWatcher(): SpaceDeletedStatusWatcher
    fun localeProvider(): LocaleProvider
}

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class SoulCreationScreenScope