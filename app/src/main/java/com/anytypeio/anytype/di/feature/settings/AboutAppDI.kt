package com.anytypeio.anytype.di.feature.settings

import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.domain.auth.interactor.GetAccount
import com.anytypeio.anytype.domain.auth.interactor.GetLibraryVersion
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.ui.settings.AboutAppFragment
import com.anytypeio.anytype.ui_settings.about.AboutAppViewModel
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@Subcomponent(modules = [AboutAppModule::class])
@PerScreen
interface AboutAppSubComponent {

    @Subcomponent.Builder
    interface Builder {
        fun module(module: AboutAppModule): Builder
        fun build(): AboutAppSubComponent
    }

    fun inject(fragment: AboutAppFragment)
}

@Module
object AboutAppModule {

    @JvmStatic
    @Provides
    @PerScreen
    fun provideViewModelFactory(
        getAccount: GetAccount,
        getLibraryVersion: GetLibraryVersion,
        analytics: Analytics
    ): AboutAppViewModel.Factory = AboutAppViewModel.Factory(
        getAccount = getAccount,
        getLibraryVersion = getLibraryVersion,
        analytics = analytics
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideGetAccountUseCase(
        repo: AuthRepository
    ): GetAccount = GetAccount(repo = repo)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideGetVersion(
        repo: AuthRepository
    ): GetLibraryVersion = GetLibraryVersion(repo)
}