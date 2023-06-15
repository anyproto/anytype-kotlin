package com.anytypeio.anytype.di.feature.onboarding

import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.domain.auth.interactor.CreateAccount
import com.anytypeio.anytype.domain.auth.interactor.SetupWallet
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.device.PathProvider
import com.anytypeio.anytype.domain.platform.MetricsProvider
import com.anytypeio.anytype.domain.search.ObjectTypesSubscriptionManager
import com.anytypeio.anytype.domain.search.RelationsSubscriptionManager
import com.anytypeio.anytype.domain.workspace.WorkspaceManager
import com.anytypeio.anytype.presentation.spaces.SpaceGradientProvider
import com.anytypeio.anytype.ui.onboarding.OnboardingAuthViewModel
import dagger.Binds
import dagger.Component
import dagger.Module
import dagger.Provides
import javax.inject.Scope

@Component(
    dependencies = [OnboardingAuthDependencies::class],
    modules = [
        OnboardingAuthModule::class,
        OnboardingAuthModule.Declarations::class
    ]
)
@AuthScreenScope
interface OnboardingAuthComponent {

    @Component.Factory
    interface Builder {
        fun create(dependencies: OnboardingAuthDependencies): OnboardingAuthComponent
    }

    fun getViewModel(): OnboardingAuthViewModel
}

@Module
object OnboardingAuthModule {

    @JvmStatic
    @Provides
    @AuthScreenScope
    fun gradientProvider(): SpaceGradientProvider = SpaceGradientProvider.Impl()

    @JvmStatic
    @Provides
    @AuthScreenScope
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
    @AuthScreenScope
    fun provideSetupWalletUseCase(
        authRepository: AuthRepository
    ): SetupWallet = SetupWallet(
        repository = authRepository
    )

    @Module
    interface Declarations {

        @Binds
        @AuthScreenScope
        fun bindViewModelFactory(factory: OnboardingAuthViewModel.Factory): ViewModelProvider.Factory

    }
}

interface OnboardingAuthDependencies : ComponentDependencies {
    fun authRepository(): AuthRepository
    fun configStorage(): ConfigStorage
    fun workspaceManager(): WorkspaceManager
    fun relationsSubscriptionManager(): RelationsSubscriptionManager
    fun objectTypesSubscriptionManager(): ObjectTypesSubscriptionManager
    fun pathProvider(): PathProvider
    fun metricsProvider(): MetricsProvider
}

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class AuthScreenScope