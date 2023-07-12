package com.anytypeio.anytype.di.feature.onboarding.signup

import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.domain.auth.interactor.GetMnemonic
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.presentation.onboarding.signup.OnboardingMnemonicViewModel
import dagger.Binds
import dagger.Component
import dagger.Module
import dagger.Provides
import javax.inject.Scope

@Component(
    dependencies = [OnboardingMnemonicDependencies::class],
    modules = [
        OnboardingMnemonicModule::class,
        OnboardingMnemonicModule.Declarations::class
    ]
)
@MnemonicScreenScope
interface OnboardingMnemonicComponent {

    @Component.Factory
    interface Builder {
        fun create(dependencies: OnboardingMnemonicDependencies): OnboardingMnemonicComponent
    }

    fun getViewModel(): OnboardingMnemonicViewModel
}

@Module
object OnboardingMnemonicModule {

    @JvmStatic
    @Provides
    @MnemonicScreenScope
    fun provideGetMnemonicUseCase(
        authRepository: AuthRepository,
    ): GetMnemonic = GetMnemonic(
        repository = authRepository
    )
    
    @Module
    interface Declarations {

        @Binds
        @MnemonicScreenScope
        fun bindViewModelFactory(factory: OnboardingMnemonicViewModel.Factory): ViewModelProvider.Factory

    }
}

interface OnboardingMnemonicDependencies : ComponentDependencies {
    fun authRepository(): AuthRepository
    fun analytics(): Analytics
}

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class MnemonicScreenScope