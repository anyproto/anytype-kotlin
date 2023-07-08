package com.anytypeio.anytype.di.feature.onboarding

import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.presentation.onboarding.OnboardingStartViewModel
import dagger.Binds
import dagger.Component
import dagger.Module
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
    @Module
    interface Declarations {
        @Binds
        @AuthScreenScope
        fun bindViewModelFactory(factory: OnboardingStartViewModel.Factory): ViewModelProvider.Factory
    }
}

interface OnboardingStartDependencies : ComponentDependencies {
    fun analytics(): Analytics
}

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class AuthScreenScope