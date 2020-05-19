package com.agileburo.anytype.di.feature

import com.agileburo.anytype.core_utils.di.scope.PerScreen
import com.agileburo.anytype.domain.auth.interactor.GetCurrentAccount
import com.agileburo.anytype.domain.auth.repo.AuthRepository
import com.agileburo.anytype.domain.block.interactor.DragAndDrop
import com.agileburo.anytype.domain.block.repo.BlockRepository
import com.agileburo.anytype.domain.config.GetConfig
import com.agileburo.anytype.domain.dashboard.interactor.CloseDashboard
import com.agileburo.anytype.domain.dashboard.interactor.OpenDashboard
import com.agileburo.anytype.domain.event.interactor.EventChannel
import com.agileburo.anytype.domain.event.interactor.InterceptEvents
import com.agileburo.anytype.domain.misc.UrlBuilder
import com.agileburo.anytype.domain.page.CreatePage
import com.agileburo.anytype.presentation.desktop.HomeDashboardEventConverter
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
        openDashboard: OpenDashboard,
        createPage: CreatePage,
        closeDashboard: CloseDashboard,
        getConfig: GetConfig,
        dnd: DragAndDrop,
        interceptEvents: InterceptEvents,
        eventConverter: HomeDashboardEventConverter
    ): HomeDashboardViewModelFactory = HomeDashboardViewModelFactory(
        getCurrentAccount = getCurrentAccount,
        openDashboard = openDashboard,
        createPage = createPage,
        closeDashboard = closeDashboard,
        getConfig = getConfig,
        dnd = dnd,
        interceptEvents = interceptEvents,
        eventConverter = eventConverter
    )

    @Provides
    @PerScreen
    fun provideGetAccountUseCase(
        repository: BlockRepository,
        builder: UrlBuilder
    ): GetCurrentAccount = GetCurrentAccount(
            repo = repository,
            builder = builder
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
    fun provideDragAndDropUseCase(
        repo: BlockRepository
    ): DragAndDrop = DragAndDrop(
        repo = repo
    )

    @Provides
    @PerScreen
    fun provideInterceptEvents(
        channel: EventChannel
    ): InterceptEvents = InterceptEvents(
        context = Dispatchers.IO,
        channel = channel
    )

    @Provides
    @PerScreen
    fun provideEventConverter(): HomeDashboardEventConverter {
        return HomeDashboardEventConverter.DefaultConverter()
    }
}