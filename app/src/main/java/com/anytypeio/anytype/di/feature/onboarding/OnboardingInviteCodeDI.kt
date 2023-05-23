package com.anytypeio.anytype.di.feature.onboarding

import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.domain.auth.interactor.CreateAccount
import com.anytypeio.anytype.domain.auth.interactor.SetupWallet
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.device.PathProvider
import com.anytypeio.anytype.domain.search.ObjectTypesSubscriptionManager
import com.anytypeio.anytype.domain.search.RelationsSubscriptionManager
import com.anytypeio.anytype.domain.workspace.WorkspaceManager
import com.anytypeio.anytype.presentation.spaces.SpaceGradientProvider
import com.anytypeio.anytype.ui.onboarding.OnboardingInviteCodeViewModel
import dagger.Binds
import dagger.Component
import dagger.Module
import dagger.Provides
import javax.inject.Scope

@Component(
    dependencies = [OnboardingInviteCodeDependencies::class],
    modules = [
        OnboardingInviteCodeModule::class,
        OnboardingInviteCodeModule.Declarations::class
    ]
)
@InviteCodeScreenScope
interface OnboardingInviteCodeComponent {

    @Component.Factory
    interface Builder {
        fun create(dependencies: OnboardingInviteCodeDependencies): OnboardingInviteCodeComponent
    }

    fun getViewModel(): OnboardingInviteCodeViewModel
}

@Module
object OnboardingInviteCodeModule {

    @JvmStatic
    @Provides
    @InviteCodeScreenScope
    fun gradientProvider(): SpaceGradientProvider = SpaceGradientProvider.Impl()

    @JvmStatic
    @Provides
    @InviteCodeScreenScope
    fun provideCreateAccountUseCase(
        authRepository: AuthRepository,
        configStorage: ConfigStorage,
        workspaceManager: WorkspaceManager
    ): CreateAccount = CreateAccount(
        repository = authRepository,
        configStorage = configStorage,
        workspaceManager = workspaceManager
    )

    @JvmStatic
    @Provides
    @InviteCodeScreenScope
    fun provideSetupWalletUseCase(
        authRepository: AuthRepository
    ): SetupWallet = SetupWallet(
        repository = authRepository
    )

    @Module
    interface Declarations {

        @Binds
        @InviteCodeScreenScope
        fun bindViewModelFactory(factory: OnboardingInviteCodeViewModel.Factory): ViewModelProvider.Factory

    }
}

interface OnboardingInviteCodeDependencies : ComponentDependencies {
    fun authRepository(): AuthRepository
    fun configStorage(): ConfigStorage
    fun workspaceManager(): WorkspaceManager
    fun relationsSubscriptionManager(): RelationsSubscriptionManager
    fun objectTypesSubscriptionManager(): ObjectTypesSubscriptionManager
    fun pathProvider(): PathProvider
}

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class InviteCodeScreenScope