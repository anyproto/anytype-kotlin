package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.domain.auth.interactor.GetCurrentAccount
import com.anytypeio.anytype.domain.auth.interactor.GetLibraryVersion
import com.anytypeio.anytype.domain.auth.interactor.Logout
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.presentation.profile.ProfileViewModelFactory
import com.anytypeio.anytype.ui.profile.ProfileFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent


@Subcomponent(
    modules = [ProfileModule::class]
)
@PerScreen
interface ProfileSubComponent {

    @Subcomponent.Builder
    interface Builder {
        fun profileModule(module: ProfileModule): Builder
        fun build(): ProfileSubComponent
    }

    fun inject(fragment: ProfileFragment)
}

@Module
object ProfileModule {

    @JvmStatic
    @Provides
    @PerScreen
    fun provideProfileViewModelFactory(
        logout: Logout,
        getCurrentAccount: GetCurrentAccount,
        analytics: Analytics,
        getLibraryVersion: GetLibraryVersion
    ): ProfileViewModelFactory = ProfileViewModelFactory(
        logout = logout,
        getCurrentAccount = getCurrentAccount,
        analytics = analytics,
        getLibraryVersion = getLibraryVersion
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideLogoutUseCase(
        repository: AuthRepository
    ): Logout = Logout(repository)

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