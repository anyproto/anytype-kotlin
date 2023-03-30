package com.anytypeio.anytype.di.feature.auth

import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.domain.account.DateHelper
import com.anytypeio.anytype.domain.account.RestoreAccount
import com.anytypeio.anytype.domain.auth.interactor.Logout
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.misc.AppActionManager
import com.anytypeio.anytype.domain.search.RelationsSubscriptionManager
import com.anytypeio.anytype.ext.DefaultDateHelper
import com.anytypeio.anytype.presentation.auth.account.DeletedAccountViewModel
import com.anytypeio.anytype.ui.auth.account.DeletedAccountFragment
import dagger.Binds
import dagger.Component
import dagger.Module
import dagger.Provides

@Component(
    dependencies = [DeletedAccountDependencies::class],
    modules = [
        DeletedAccountModule::class,
        DeletedAccountModule.Declarations::class
    ]
)
@PerScreen
interface DeletedAccountComponent {

    @Component.Factory
    interface Factory {
        fun create(dependencies: DeletedAccountDependencies): DeletedAccountComponent
    }

    fun inject(fragment: DeletedAccountFragment)
}

@Module
object DeletedAccountModule {

    @JvmStatic
    @Provides
    @PerScreen
    fun provideRestoreAccount(
        repo: AuthRepository,
        dispatchers: AppCoroutineDispatchers
    ): RestoreAccount = RestoreAccount(
        repo = repo,
        dispatchers = dispatchers
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideLogoutUseCase(
        repo: AuthRepository,
        provider: ConfigStorage,
        dispatchers: AppCoroutineDispatchers,
        user: UserSettingsRepository
    ): Logout = Logout(
        repo = repo,
        config = provider,
        user = user,
        dispatchers = dispatchers
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideDateHelper(): DateHelper = DefaultDateHelper()

    @Module
    interface Declarations {

        @PerScreen
        @Binds
        fun bindViewModelFactory(
            factory: DeletedAccountViewModel.Factory
        ): ViewModelProvider.Factory

    }
}

interface DeletedAccountDependencies : ComponentDependencies {
    fun analytics(): Analytics
    fun appActionManager(): AppActionManager
    fun relationsSubscriptionManager(): RelationsSubscriptionManager
    fun dispatchers(): AppCoroutineDispatchers
    fun configStorage(): ConfigStorage
    fun authRepository(): AuthRepository
    fun userSettingsRepository(): UserSettingsRepository
}