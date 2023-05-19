package com.anytypeio.anytype.di.feature.onboarding

import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.ui.onboarding.OnboardingAuthViewModel
import dagger.Binds
import dagger.Component
import dagger.Module
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

    @Module
    interface Declarations {

        @Binds
        @AuthScreenScope
        fun bindViewModelFactory(factory: OnboardingAuthViewModel.Factory): ViewModelProvider.Factory

    }
}

interface OnboardingAuthDependencies : ComponentDependencies

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class AuthScreenScope