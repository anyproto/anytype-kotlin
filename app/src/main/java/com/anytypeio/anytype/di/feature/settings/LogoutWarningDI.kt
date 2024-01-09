package com.anytypeio.anytype.di.feature.settings

import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.domain.account.AwaitAccountStartManager
import com.anytypeio.anytype.domain.auth.interactor.Logout
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.misc.AppActionManager
import com.anytypeio.anytype.domain.search.ObjectTypesSubscriptionManager
import com.anytypeio.anytype.domain.search.RelationsSubscriptionManager
import com.anytypeio.anytype.domain.spaces.SpaceDeletedStatusWatcher
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.ui.settings.LogoutWarningFragment
import com.anytypeio.anytype.ui_settings.account.LogoutWarningViewModel
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@Subcomponent(modules = [LogoutWarningModule::class])
@PerScreen
interface LogoutWarningSubComponent {

    @Subcomponent.Builder
    interface Builder {
        fun module(module: LogoutWarningModule): Builder
        fun build(): LogoutWarningSubComponent
    }

    fun inject(fragment: LogoutWarningFragment)
}

@Module
object LogoutWarningModule {

    @JvmStatic
    @Provides
    @PerScreen
    fun provideViewModelFactory(
        logout: Logout,
        analytics: Analytics,
        relationsSubscriptionManager: RelationsSubscriptionManager,
        objectTypesSubscriptionManager: ObjectTypesSubscriptionManager,
        spaceDeletedStatusWatcher: SpaceDeletedStatusWatcher,
        appActionManager: AppActionManager,
    ): LogoutWarningViewModel.Factory = LogoutWarningViewModel.Factory(
        logout = logout,
        analytics = analytics,
        relationsSubscriptionManager = relationsSubscriptionManager,
        appActionManager = appActionManager,
        spaceDeletedStatusWatcher = spaceDeletedStatusWatcher,
        objectTypesSubscriptionManager = objectTypesSubscriptionManager
    )

    @JvmStatic
    @PerScreen
    @Provides
    fun provideLogoutUseCase(
        repo: AuthRepository,
        provider: ConfigStorage,
        user: UserSettingsRepository,
        dispatchers: AppCoroutineDispatchers,
        spaceManager: SpaceManager,
        awaitAccountStartManager: AwaitAccountStartManager
    ): Logout = Logout(
        repo = repo,
        config = provider,
        user = user,
        dispatchers = dispatchers,
        spaceManager = spaceManager,
        awaitAccountStartManager = awaitAccountStartManager
    )
}