package com.agileburo.anytype.di.feature

import com.agileburo.anytype.core_utils.di.scope.PerScreen
import com.agileburo.anytype.domain.auth.interactor.GetCurrentAccount
import com.agileburo.anytype.domain.auth.interactor.Logout
import com.agileburo.anytype.domain.auth.repo.AuthRepository
import com.agileburo.anytype.domain.block.repo.BlockRepository
import com.agileburo.anytype.domain.misc.UrlBuilder
import com.agileburo.anytype.presentation.profile.ProfileViewModelFactory
import com.agileburo.anytype.ui.profile.ProfileFragment
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
class ProfileModule {

    @Provides
    @PerScreen
    fun provideProfileViewModelFactory(
        logout: Logout,
        getCurrentAccount: GetCurrentAccount
    ): ProfileViewModelFactory = ProfileViewModelFactory(
        logout = logout,
        getCurrentAccount = getCurrentAccount
    )

    @Provides
    @PerScreen
    fun provideLogoutUseCase(
        repository: AuthRepository
    ): Logout = Logout(repository)

    @Provides
    @PerScreen
    fun provideGetAccountUseCase(
        repo: BlockRepository,
        builder: UrlBuilder
    ): GetCurrentAccount = GetCurrentAccount(repo = repo, builder = builder)
}