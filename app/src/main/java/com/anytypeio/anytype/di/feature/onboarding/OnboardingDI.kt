package com.anytypeio.anytype.di.feature.onboarding

import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.presentation.onboarding.OnboardingViewModel
import com.anytypeio.anytype.ui.onboarding.OnboardingFragment
import dagger.Binds
import dagger.Component
import dagger.Module

@Component(
    dependencies = [OnboardingDependencies::class],
    modules = [
        OnboardingModule::class,
        OnboardingModule.Declarations::class
    ]
)
@PerScreen
interface OnboardingComponent {
    @Component.Factory
    interface Builder {
        fun create(dependencies: OnboardingDependencies): OnboardingComponent
    }

    fun inject(fragment: OnboardingFragment)
}

@Module
object OnboardingModule {
    @Module
    interface Declarations {
        @Binds
        @AuthScreenScope
        fun bindViewModelFactory(factory: OnboardingViewModel.Factory): ViewModelProvider.Factory
    }
}

interface OnboardingDependencies : ComponentDependencies {
    fun analytics(): Analytics
}