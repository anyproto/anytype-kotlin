package com.anytypeio.anytype.di.feature.onboarding.login

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.CrashReporter
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.data.auth.event.EventProcessMigrationDateChannel
import com.anytypeio.anytype.data.auth.event.EventProcessMigrationRemoteChannel
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.domain.account.AwaitAccountStartManager
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.debugging.DebugAccountSelectTrace
import com.anytypeio.anytype.domain.debugging.Logger
import com.anytypeio.anytype.domain.device.PathProvider
import com.anytypeio.anytype.domain.misc.LocaleProvider
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider
import com.anytypeio.anytype.domain.platform.InitialParamsProvider
import com.anytypeio.anytype.domain.spaces.SpaceDeletedStatusWatcher
import com.anytypeio.anytype.domain.subscriptions.GlobalSubscriptionManager
import com.anytypeio.anytype.domain.workspace.EventProcessMigrationChannel
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.middleware.EventProxy
import com.anytypeio.anytype.middleware.interactor.EventProcessMigrationMiddlewareChannel
import com.anytypeio.anytype.presentation.auth.account.MigrationHelperDelegate
import com.anytypeio.anytype.presentation.onboarding.login.OnboardingMnemonicLoginViewModel
import com.anytypeio.anytype.presentation.util.downloader.UriFileProvider
import com.anytypeio.anytype.providers.DefaultUriFileProvider
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
        fun bindUriFileProvider(
            defaultProvider: DefaultUriFileProvider
        ): UriFileProvider

        @Binds
        @PerScreen
        fun bindMigrationHelperDelegate(
            impl: MigrationHelperDelegate.Impl
        ): MigrationHelperDelegate

        @Binds
        @PerScreen
        fun bindMigrationChannel(
            impl: EventProcessMigrationDateChannel
        ): EventProcessMigrationChannel

        @Binds
        @PerScreen
        fun bindMigrationRemoteChannel(
            impl: EventProcessMigrationMiddlewareChannel
        ): EventProcessMigrationRemoteChannel

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
    fun metricsProvider(): InitialParamsProvider
    fun spaceStatusWatcher(): SpaceDeletedStatusWatcher
    fun localeProvider(): LocaleProvider
    fun awaitAccountStartManager(): AwaitAccountStartManager
    fun userPermissionProvider() : UserPermissionProvider
    fun provideAppCoroutineDispatchers(): AppCoroutineDispatchers
    fun provideBlockRepository(): BlockRepository
    fun provideContext(): Context
    fun userSettings(): UserSettingsRepository
    fun spaceManager(): SpaceManager
    fun globalSubscriptionManager(): GlobalSubscriptionManager
    fun debugAccountSelectTrace(): DebugAccountSelectTrace
    fun logger(): Logger
    fun eventProxy(): EventProxy
}