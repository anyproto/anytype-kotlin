package com.anytypeio.anytype.di.feature.onboarding.signup

import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.CrashReporter
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.domain.account.AwaitAccountStartManager
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.deeplink.PendingIntentStore
import com.anytypeio.anytype.domain.device.PathProvider
import com.anytypeio.anytype.domain.misc.LocaleProvider
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider
import com.anytypeio.anytype.domain.payments.SetMembershipEmail
import com.anytypeio.anytype.domain.platform.InitialParamsProvider
import com.anytypeio.anytype.domain.resources.StringResourceProvider
import com.anytypeio.anytype.domain.search.ProfileSubscriptionManager
import com.anytypeio.anytype.domain.search.RelationsSubscriptionManager
import com.anytypeio.anytype.domain.spaces.SpaceDeletedStatusWatcher
import com.anytypeio.anytype.domain.subscriptions.GlobalSubscriptionManager
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.onboarding.signup.OnboardingSetProfileNameViewModel
import com.anytypeio.anytype.presentation.onboarding.signup.OnboardingEmailAndSelectionViewModel
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
    fun getEmailAndSelectionViewModel(): OnboardingEmailAndSelectionViewModel
}

@Module
object OnboardingSoulCreationModule {

    @JvmStatic
    @Provides
    @SoulCreationScreenScope
    fun provideSetMembershipEmail(
        repo: BlockRepository,
        dispatchers: AppCoroutineDispatchers
    ): SetMembershipEmail = SetMembershipEmail(repo = repo, dispatchers = dispatchers)

    @Module
    interface Declarations {
        @Binds
        @SoulCreationScreenScope
        fun bindViewModelFactory(factory: OnboardingSetProfileNameViewModel.Factory): ViewModelProvider.Factory
        
        @Binds
        @SoulCreationScreenScope
        fun bindEmailAndSelectionViewModelFactory(factory: OnboardingEmailAndSelectionViewModel.Factory): ViewModelProvider.Factory
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
    fun pathProvider(): PathProvider
    fun metricsProvider(): InitialParamsProvider
    fun crashReporter(): CrashReporter
    fun spaceManager(): SpaceManager
    fun spaceStatusWatcher(): SpaceDeletedStatusWatcher
    fun localeProvider(): LocaleProvider
    fun userPermissionProvider(): UserPermissionProvider
    fun awaitAccountStartManager(): AwaitAccountStartManager
    fun globalSubscriptionManager(): GlobalSubscriptionManager
    fun stringResourceProvider(): StringResourceProvider
    fun providePendingIntentStore(): PendingIntentStore
    fun provideProfileContainer() :ProfileSubscriptionManager
}

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class SoulCreationScreenScope