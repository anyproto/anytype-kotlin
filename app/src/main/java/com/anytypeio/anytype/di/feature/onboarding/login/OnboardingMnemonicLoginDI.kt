package com.anytypeio.anytype.di.feature.onboarding.login

import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.CrashReporter
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.domain.account.AwaitAccountStartManager
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.device.PathProvider
import com.anytypeio.anytype.domain.misc.LocaleProvider
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider
import com.anytypeio.anytype.domain.platform.MetricsProvider
import com.anytypeio.anytype.domain.search.ObjectTypesSubscriptionManager
import com.anytypeio.anytype.domain.search.RelationsSubscriptionManager
import com.anytypeio.anytype.domain.spaces.SpaceDeletedStatusWatcher
import com.anytypeio.anytype.presentation.onboarding.login.OnboardingMnemonicLoginViewModel
import dagger.Binds
import dagger.Component
import dagger.Module

@Component(
    dependencies = [OnboardingMnemonicLoginDependencies::class],
    modules = [
        OnboardingMnemonicLoginModule::class,
        OnboardingMnemonicLoginModule.Declarations::class
    ]
)
@PerScreen
interface OnboardingMnemonicLoginComponent {
    @Component.Factory
    interface Builder {
        fun create(
            dependencies: OnboardingMnemonicLoginDependencies
        ): OnboardingMnemonicLoginComponent
    }

    fun getViewModel(): OnboardingMnemonicLoginViewModel
}

@Module
object OnboardingMnemonicLoginModule {
    @Module
    interface Declarations {
        @Binds
        @PerScreen
        fun bindViewModelFactory(
            factory: OnboardingMnemonicLoginViewModel.Factory
        ): ViewModelProvider.Factory
    }
}

interface OnboardingMnemonicLoginDependencies : ComponentDependencies {
    fun authRepository(): AuthRepository
    fun analytics(): Analytics
    fun pathProvider(): PathProvider
    fun crashReporter(): CrashReporter
    fun configStorage(): ConfigStorage
    fun metricsProvider(): MetricsProvider
    fun objectTypesSubscriptionManager(): ObjectTypesSubscriptionManager
    fun relationsSubscriptionManager(): RelationsSubscriptionManager
    fun spaceStatusWatcher(): SpaceDeletedStatusWatcher
    fun localeProvider(): LocaleProvider
    fun awaitAccountStartManager(): AwaitAccountStartManager
    fun userPermissionProvider() : UserPermissionProvider
}