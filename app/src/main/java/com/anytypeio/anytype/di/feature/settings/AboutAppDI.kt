package com.anytypeio.anytype.di.feature.settings

import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.domain.auth.interactor.GetCurrentAccount
import com.anytypeio.anytype.domain.auth.interactor.GetLibraryVersion
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.misc.UrlBuilder
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
        getCurrentAccount: GetCurrentAccount,
        getLibraryVersion: GetLibraryVersion
    ): AboutAppViewModel.Factory = AboutAppViewModel.Factory(
        getCurrentAccount = getCurrentAccount,
        getLibraryVersion = getLibraryVersion
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideGetAccountUseCase(
        repo: BlockRepository,
        builder: UrlBuilder
    ): GetCurrentAccount = GetCurrentAccount(repo = repo, builder = builder)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideGetVersion(
        repo: AuthRepository
    ): GetLibraryVersion = GetLibraryVersion(repo)
}