package com.anytypeio.anytype.di.feature.onboarding.signup

import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.CrashReporter
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.domain.auth.interactor.CreateAccount
import com.anytypeio.anytype.domain.auth.interactor.SetupWallet
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.device.PathProvider
import com.anytypeio.anytype.domain.`object`.SetupMobileUseCaseSkip
import com.anytypeio.anytype.domain.platform.MetricsProvider
import com.anytypeio.anytype.domain.search.ObjectTypesSubscriptionManager
import com.anytypeio.anytype.domain.search.RelationsSubscriptionManager
import com.anytypeio.anytype.domain.workspace.WorkspaceManager
import com.anytypeio.anytype.presentation.onboarding.signup.OnboardingVoidViewModel
import com.anytypeio.anytype.presentation.spaces.SpaceGradientProvider
import dagger.Binds
import dagger.Component
import dagger.Module
import dagger.Provides

@Component(
    dependencies = [OnboardingVoidDependencies::class],
    modules = [
        OnboardingVoidModule::class,
        OnboardingVoidModule.Declarations::class
    ]
)
@PerScreen
interface OnboardingVoidComponent {

    @Component.Factory
    interface Builder {
        fun create(dependencies: OnboardingVoidDependencies): OnboardingVoidComponent
    }

    fun getViewModel(): OnboardingVoidViewModel
}

@Module
object OnboardingVoidModule {

    @JvmStatic
    @Provides
    @PerScreen
    fun gradientProvider(): SpaceGradientProvider = SpaceGradientProvider.Default

    @JvmStatic
    @Provides
    @PerScreen
    fun provideCreateAccountUseCase(
        authRepository: AuthRepository,
        configStorage: ConfigStorage,
        workspaceManager: WorkspaceManager,
        metricsProvider: MetricsProvider
    ): CreateAccount = CreateAccount(
        repository = authRepository,
        configStorage = configStorage,
        workspaceManager = workspaceManager,
        metricsProvider = metricsProvider
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideSetupWalletUseCase(
        authRepository: AuthRepository
    ): SetupWallet = SetupWallet(
        repository = authRepository
    )

    @JvmStatic
    @Provides
    @PerScreen
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
        @PerScreen
        fun bindViewModelFactory(factory: OnboardingVoidViewModel.Factory): ViewModelProvider.Factory
    }
}

interface OnboardingVoidDependencies : ComponentDependencies {
    fun authRepository(): AuthRepository
    fun userSettings(): UserSettingsRepository
    fun blockRepository(): BlockRepository
    fun configStorage(): ConfigStorage
    fun workspaceManager(): WorkspaceManager
    fun relationsSubscriptionManager(): RelationsSubscriptionManager
    fun objectTypesSubscriptionManager(): ObjectTypesSubscriptionManager
    fun pathProvider(): PathProvider
    fun metricsProvider(): MetricsProvider
    fun dispatchers(): AppCoroutineDispatchers
    fun analytics(): Analytics
    fun crashReporter(): CrashReporter
}