package com.agileburo.anytype.di.feature

import com.agileburo.anytype.core_utils.di.scope.PerScreen
import com.agileburo.anytype.domain.auth.interactor.Logout
import com.agileburo.anytype.domain.auth.repo.AuthRepository
import com.agileburo.anytype.domain.desktop.interactor.GetAccount
import com.agileburo.anytype.domain.image.ImageLoader
import com.agileburo.anytype.domain.image.LoadImage
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
        loadImage: LoadImage,
        getAccount: GetAccount
    ): ProfileViewModelFactory = ProfileViewModelFactory(
        logout = logout,
        loadImage = loadImage,
        getAccount = getAccount
    )

    @Provides
    @PerScreen
    fun provideLogoutUseCase(
        repository: AuthRepository
    ): Logout = Logout(repository)

    @Provides
    @PerScreen
    fun provideLoadImageUseCase(
        loader: ImageLoader
    ): LoadImage = LoadImage(
        loader = loader
    )

    @Provides
    @PerScreen
    fun provideGetAccountUseCase(
        authRepository: AuthRepository
    ): GetAccount = GetAccount(
        repository = authRepository
    )
}