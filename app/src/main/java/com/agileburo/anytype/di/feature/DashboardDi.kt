package com.agileburo.anytype.di.feature

import com.agileburo.anytype.core_utils.di.scope.PerScreen
import com.agileburo.anytype.domain.auth.interactor.GetCurrentAccount
import com.agileburo.anytype.domain.auth.repo.AuthRepository
import com.agileburo.anytype.domain.block.repo.BlockRepository
import com.agileburo.anytype.domain.dashboard.interactor.CloseDashboard
import com.agileburo.anytype.domain.dashboard.interactor.ObserveDashboardBlocks
import com.agileburo.anytype.domain.dashboard.interactor.OpenDashboard
import com.agileburo.anytype.domain.image.ImageLoader
import com.agileburo.anytype.domain.image.LoadImage
import com.agileburo.anytype.domain.page.CreatePage
import com.agileburo.anytype.presentation.desktop.HomeDashboardViewModelFactory
import com.agileburo.anytype.ui.desktop.HomeDashboardFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent


@Subcomponent(
    modules = [HomeDashboardModule::class]
)
@PerScreen
interface HomeDashboardSubComponent {

    @Subcomponent.Builder
    interface Builder {
        fun homeDashboardModule(module: HomeDashboardModule): Builder
        fun build(): HomeDashboardSubComponent
    }

    fun inject(fragment: HomeDashboardFragment)
}

@Module
class HomeDashboardModule {

    @Provides
    @PerScreen
    fun provideDesktopViewModelFactory(
        getCurrentAccount: GetCurrentAccount,
        loadImage: LoadImage,
        openDashboard: OpenDashboard,
        observeDashboardBlocks: ObserveDashboardBlocks,
        createPage: CreatePage,
        closeDashboard: CloseDashboard
    ): HomeDashboardViewModelFactory = HomeDashboardViewModelFactory(
        getCurrentAccount = getCurrentAccount,
        loadImage = loadImage,
        openDashboard = openDashboard,
        observeDashboardBlocks = observeDashboardBlocks,
        createPage = createPage,
        closeDashboard = closeDashboard
    )

    @Provides
    @PerScreen
    fun provideGetAccountUseCase(
        repository: AuthRepository
    ): GetCurrentAccount =
        GetCurrentAccount(
            repository = repository
        )

    @Provides
    @PerScreen
    fun provideLoadImageUseCase(
        loader: ImageLoader
    ): LoadImage = LoadImage(
        loader = loader
    )

    @Provides
    @PerScreen
    fun provideOpenDashboardUseCase(
        repo: BlockRepository
    ): OpenDashboard = OpenDashboard(
        repo = repo
    )

    @Provides
    @PerScreen
    fun provideObserveDashboardBlocksUseCase(
        repo: BlockRepository
    ): ObserveDashboardBlocks = ObserveDashboardBlocks(
        repository = repo
    )

    @Provides
    @PerScreen
    fun provideCloseDashboardUseCase(
        repo: BlockRepository
    ): CloseDashboard = CloseDashboard(
        repo = repo
    )

    @Provides
    @PerScreen
    fun provideCreatePageUseCase(
        repo: BlockRepository
    ): CreatePage = CreatePage(
        repo = repo
    )
}