package com.anytypeio.anytype.di.feature.onboarding.login

import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.CrashReporter
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.config.FeaturesConfigProvider
import com.anytypeio.anytype.domain.device.PathProvider
import com.anytypeio.anytype.domain.platform.MetricsProvider
import com.anytypeio.anytype.domain.search.ObjectTypesSubscriptionManager
import com.anytypeio.anytype.domain.search.RelationsSubscriptionManager
import com.anytypeio.anytype.domain.workspace.WorkspaceManager
import com.anytypeio.anytype.presentation.onboarding.login.OnboardingLoginSetupViewModel
import dagger.Binds
import dagger.Component
import dagger.Module

@Component(
    dependencies = [OnboardingLoginSetupDependencies::class],
    modules = [
        OnboardingLoginSetupModule::class,
        OnboardingLoginSetupModule.Declarations::class
    ]
)
@PerScreen
interface OnboardingLoginSetupComponent {
    @Component.Factory
    interface Builder {
        fun create(
            dependencies: OnboardingLoginSetupDependencies
        ): OnboardingLoginSetupComponent
    }

    fun getViewModel(): OnboardingLoginSetupViewModel
}

@Module
object OnboardingLoginSetupModule {
    @Module
    interface Declarations {
        @Binds
        @PerScreen
        fun bindViewModelFactory(
            factory: OnboardingLoginSetupViewModel.Factory
        ): ViewModelProvider.Factory
    }
}

interface OnboardingLoginSetupDependencies : ComponentDependencies {
    fun authRepository(): AuthRepository
    fun analytics(): Analytics
    fun pathProvider(): PathProvider
    fun crashReporter(): CrashReporter
    fun configStorage(): ConfigStorage
    fun workspaceManager(): WorkspaceManager
    fun featureConfigProvider(): FeaturesConfigProvider
    fun metricsProvider(): MetricsProvider
    fun objectTypesSubscriptionManager(): ObjectTypesSubscriptionManager
    fun relationsSubscriptionManager(): RelationsSubscriptionManager
}