package com.anytypeio.anytype.di.feature.settings

import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.domain.auth.interactor.Logout
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
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
        analytics: Analytics
    ): LogoutWarningViewModel.Factory = LogoutWarningViewModel.Factory(logout, analytics)

    @JvmStatic
    @Provides
    @PerScreen
    fun logout(repo: AuthRepository): Logout = Logout(repo)
}