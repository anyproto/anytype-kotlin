package com.agileburo.anytype.di.feature

import com.agileburo.anytype.core_utils.di.scope.PerScreen
import com.agileburo.anytype.domain.auth.interactor.GetCurrentAccount
import com.agileburo.anytype.domain.auth.repo.AuthRepository
import com.agileburo.anytype.domain.block.repo.BlockRepository
import com.agileburo.anytype.domain.config.GetConfig
import com.agileburo.anytype.domain.dashboard.interactor.CloseDashboard
import com.agileburo.anytype.domain.dashboard.interactor.ObserveHomeDashboard
import com.agileburo.anytype.domain.dashboard.interactor.OpenDashboard
import com.agileburo.anytype.domain.image.ImageLoader
import com.agileburo.anytype.domain.image.LoadImage
import com.agileburo.anytype.domain.page.CreatePage
import com.agileburo.anytype.presentation.desktop.HomeDashboardViewModelFactory
import com.agileburo.anytype.ui.desktop.HomeDashboardFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import kotlinx.coroutines.Dispatchers


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
        createPage: CreatePage,
        closeDashboard: CloseDashboard,
        getConfig: GetConfig,
        observeHomeDashboard: ObserveHomeDashboard
    ): HomeDashboardViewModelFactory = HomeDashboardViewModelFactory(
        getCurrentAccount = getCurrentAccount,
        loadImage = loadImage,
        openDashboard = openDashboard,
        createPage = createPage,
        closeDashboard = closeDashboard,
        getConfig = getConfig,
        observeHomeDashboard = observeHomeDashboard
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

    @Provides
    @PerScreen
    fun getConfigUseCase(
        repo: BlockRepository
    ): GetConfig = GetConfig(
        repo = repo
    )

    @Provides
    @PerScreen
    fun provideObserveHomeDashboardUseCase(
        repo: BlockRepository
    ): ObserveHomeDashboard = ObserveHomeDashboard(
        context = Dispatchers.IO,
        repo = repo
    )
}