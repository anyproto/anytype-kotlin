package com.agileburo.anytype.di.feature

import com.agileburo.anytype.core_utils.di.scope.PerScreen
import com.agileburo.anytype.domain.auth.interactor.GetCurrentAccount
import com.agileburo.anytype.domain.block.interactor.Move
import com.agileburo.anytype.domain.block.repo.BlockRepository
import com.agileburo.anytype.domain.config.GetConfig
import com.agileburo.anytype.domain.config.GetDebugSettings
import com.agileburo.anytype.domain.config.InfrastructureRepository
import com.agileburo.anytype.domain.dashboard.interactor.CloseDashboard
import com.agileburo.anytype.domain.dashboard.interactor.OpenDashboard
import com.agileburo.anytype.domain.event.interactor.EventChannel
import com.agileburo.anytype.domain.event.interactor.InterceptEvents
import com.agileburo.anytype.domain.icon.DocumentEmojiIconProvider
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
        move: Move,
        interceptEvents: InterceptEvents,
        eventConverter: HomeDashboardEventConverter,
        getDebugSettings: GetDebugSettings
    ): HomeDashboardViewModelFactory = HomeDashboardViewModelFactory(
        getCurrentAccount = getCurrentAccount,
        openDashboard = openDashboard,
        createPage = createPage,
        closeDashboard = closeDashboard,
        getConfig = getConfig,
        move = move,
        interceptEvents = interceptEvents,
        eventConverter = eventConverter,
        getDebugSettings = getDebugSettings
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
        repo: BlockRepository,
        documentEmojiIconProvider: DocumentEmojiIconProvider
    ): CreatePage = CreatePage(
        repo = repo,
        documentEmojiIconProvider = documentEmojiIconProvider
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
    fun provideMoveUseCase(
        repo: BlockRepository
    ): Move = Move(
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

    @Provides
    @PerScreen
    fun provideGetDebugSettings(
        repo: InfrastructureRepository
    ) : GetDebugSettings = GetDebugSettings(
        repo = repo
    )
}