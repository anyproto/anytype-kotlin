package com.agileburo.anytype.di.feature

import com.agileburo.anytype.core_utils.di.scope.PerScreen
import com.agileburo.anytype.domain.auth.repo.AuthRepository
import com.agileburo.anytype.domain.desktop.interactor.GetCurrentAccount
import com.agileburo.anytype.domain.image.ImageLoader
import com.agileburo.anytype.domain.image.LoadImage
import com.agileburo.anytype.presentation.desktop.DesktopViewModelFactory
import com.agileburo.anytype.ui.desktop.DesktopFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent


@Subcomponent(
    modules = [DesktopModule::class]
)
@PerScreen
interface DesktopSubComponent {

    @Subcomponent.Builder
    interface Builder {
        fun desktopModule(module: DesktopModule): Builder
        fun build(): DesktopSubComponent
    }

    fun inject(fragment: DesktopFragment)
}

@Module
class DesktopModule {

    @Provides
    @PerScreen
    fun provideDesktopViewModelFactory(
        getCurrentAccount: GetCurrentAccount,
        loadImage: LoadImage
    ): DesktopViewModelFactory = DesktopViewModelFactory(
        getCurrentAccount = getCurrentAccount,
        loadImage = loadImage
    )

    @Provides
    @PerScreen
    fun provideGetAccountUseCase(
        repository: AuthRepository
    ): GetCurrentAccount = GetCurrentAccount(
        repository = repository
    )

    @Provides
    @PerScreen
    fun provideLoadImageUseCase(
        loader: ImageLoader
    ): LoadImage = LoadImage(
        loader = loader
    )
}