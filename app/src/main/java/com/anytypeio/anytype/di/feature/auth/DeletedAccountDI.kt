package com.anytypeio.anytype.di.feature.auth

import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.domain.account.DateHelper
import com.anytypeio.anytype.domain.account.RestoreAccount
import com.anytypeio.anytype.domain.auth.interactor.Logout
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.ext.DefaultDateHelper
import com.anytypeio.anytype.presentation.auth.account.DeletedAccountViewModel
import com.anytypeio.anytype.ui.auth.account.DeletedAccountFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@Subcomponent(modules = [DeletedAccountModule::class])
@PerScreen
interface DeletedAccountSubcomponent {
    @Subcomponent.Builder
    interface Builder {
        fun module(module: DeletedAccountModule): Builder
        fun build(): DeletedAccountSubcomponent
    }

    fun inject(fragment: DeletedAccountFragment)
}

@Module
object DeletedAccountModule {
    @JvmStatic
    @Provides
    @PerScreen
    fun provideViewModelFactory(
        restoreAccount: RestoreAccount,
        logout: Logout,
        helper: DateHelper,
        analytics: Analytics
    ): DeletedAccountViewModel.Factory = DeletedAccountViewModel.Factory(
        restoreAccount = restoreAccount,
        logout = logout,
        helper = helper,
        analytics = analytics
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideRestoreAccount(repo: AuthRepository): RestoreAccount = RestoreAccount(
        repo = repo
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideLogout(
        repo: AuthRepository,
        provider: ConfigStorage
    ): Logout = Logout(
        repo = repo,
        provider = provider
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideDateHelper(): DateHelper = DefaultDateHelper()
}