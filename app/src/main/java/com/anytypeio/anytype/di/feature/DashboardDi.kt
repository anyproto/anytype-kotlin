package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.domain.auth.interactor.GetProfile
import com.anytypeio.anytype.domain.block.interactor.Move
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.GetConfig
import com.anytypeio.anytype.domain.config.GetDebugSettings
import com.anytypeio.anytype.domain.config.GetFlavourConfig
import com.anytypeio.anytype.domain.config.InfrastructureRepository
import com.anytypeio.anytype.domain.dashboard.interactor.*
import com.anytypeio.anytype.domain.event.interactor.EventChannel
import com.anytypeio.anytype.domain.event.interactor.InterceptEvents
import com.anytypeio.anytype.domain.icon.DocumentEmojiIconProvider
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.page.CreatePage
import com.anytypeio.anytype.presentation.desktop.HomeDashboardEventConverter
import com.anytypeio.anytype.presentation.desktop.HomeDashboardViewModelFactory
import com.anytypeio.anytype.ui.desktop.HomeDashboardFragment
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
object HomeDashboardModule {

    @JvmStatic
    @Provides
    @PerScreen
    fun provideDesktopViewModelFactory(
        getProfile: GetProfile,
        openDashboard: OpenDashboard,
        createPage: CreatePage,
        closeDashboard: CloseDashboard,
        getConfig: GetConfig,
        move: Move,
        interceptEvents: InterceptEvents,
        eventConverter: HomeDashboardEventConverter,
        getDebugSettings: GetDebugSettings,
        analytics: Analytics,
        searchArchivedObjects: SearchArchivedObjects,
        searchRecentObjects: SearchRecentObjects,
        searchInboxObjects: SearchInboxObjects,
        searchObjectSets: SearchObjectSets,
        getFlavourConfig: GetFlavourConfig
    ): HomeDashboardViewModelFactory = HomeDashboardViewModelFactory(
        getProfile = getProfile,
        openDashboard = openDashboard,
        createPage = createPage,
        closeDashboard = closeDashboard,
        getConfig = getConfig,
        move = move,
        interceptEvents = interceptEvents,
        eventConverter = eventConverter,
        getDebugSettings = getDebugSettings,
        searchArchivedObjects = searchArchivedObjects,
        searchRecentObjects = searchRecentObjects,
        searchInboxObjects = searchInboxObjects,
        searchObjectSets = searchObjectSets,
        analytics = analytics,
        getFlavourConfig = getFlavourConfig
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideGetProfileUseCase(
        repository: BlockRepository
    ): GetProfile = GetProfile(
        repo = repository
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideOpenDashboardUseCase(
        repo: BlockRepository
    ): OpenDashboard = OpenDashboard(
        repo = repo
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideCloseDashboardUseCase(
        repo: BlockRepository
    ): CloseDashboard = CloseDashboard(
        repo = repo
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideCreatePageUseCase(
        repo: BlockRepository,
        documentEmojiIconProvider: DocumentEmojiIconProvider
    ): CreatePage = CreatePage(
        repo = repo,
        documentEmojiIconProvider = documentEmojiIconProvider
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun getConfigUseCase(
        repo: BlockRepository
    ): GetConfig = GetConfig(
        repo = repo
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideMoveUseCase(
        repo: BlockRepository
    ): Move = Move(
        repo = repo
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideInterceptEvents(
        channel: EventChannel
    ): InterceptEvents = InterceptEvents(
        context = Dispatchers.IO,
        channel = channel
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideEventConverter(builder: UrlBuilder): HomeDashboardEventConverter {
        return HomeDashboardEventConverter.DefaultConverter(builder)
    }

    @JvmStatic
    @Provides
    @PerScreen
    fun provideGetDebugSettings(
        repo: InfrastructureRepository
    ) : GetDebugSettings = GetDebugSettings(
        repo = repo
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideSearchArchivedObjectsUseCase(
        repo: BlockRepository
    ) : SearchArchivedObjects = SearchArchivedObjects(
        repo = repo
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideSearchRecentObjectsUseCase(
        repo: BlockRepository
    ) : SearchRecentObjects = SearchRecentObjects(
        repo = repo
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideSearchInboxObjectsUseCase(
        repo: BlockRepository
    ) : SearchInboxObjects = SearchInboxObjects(
        repo = repo
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideSearchObjectSetsUseCase(
        repo: BlockRepository
    ) : SearchObjectSets = SearchObjectSets(
        repo = repo
    )
}