package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.domain.`object`.ObjectTypesProvider
import com.anytypeio.anytype.domain.auth.interactor.GetProfile
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.interactor.Move
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.*
import com.anytypeio.anytype.domain.dashboard.interactor.CloseDashboard
import com.anytypeio.anytype.domain.dashboard.interactor.OpenDashboard
import com.anytypeio.anytype.domain.dataview.interactor.SearchObjects
import com.anytypeio.anytype.domain.event.interactor.EventChannel
import com.anytypeio.anytype.domain.event.interactor.InterceptEvents
import com.anytypeio.anytype.domain.launch.GetDefaultEditorType
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.objects.DeleteObjects
import com.anytypeio.anytype.domain.objects.ObjectStore
import com.anytypeio.anytype.domain.objects.SetObjectListIsArchived
import com.anytypeio.anytype.domain.page.CreatePage
import com.anytypeio.anytype.domain.search.CancelSearchSubscription
import com.anytypeio.anytype.domain.search.ObjectSearchSubscriptionContainer
import com.anytypeio.anytype.domain.search.SubscriptionEventChannel
import com.anytypeio.anytype.presentation.dashboard.HomeDashboardEventConverter
import com.anytypeio.anytype.presentation.dashboard.HomeDashboardViewModelFactory
import com.anytypeio.anytype.ui.dashboard.DashboardFragment
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

    fun inject(fragment: DashboardFragment)
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
        searchObjects: SearchObjects,
        getDefaultEditorType: GetDefaultEditorType,
        urlBuilder: UrlBuilder,
        setObjectListIsArchived: SetObjectListIsArchived,
        deleteObjects: DeleteObjects,
        flavourConfigProvider: FlavourConfigProvider,
        objectSearchSubscriptionContainer: ObjectSearchSubscriptionContainer,
        cancelSearchSubscription: CancelSearchSubscription,
        objectStore: ObjectStore
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
        searchObjects = searchObjects,
        analytics = analytics,
        urlBuilder = urlBuilder,
        setObjectListIsArchived = setObjectListIsArchived,
        deleteObjects = deleteObjects,
        getDefaultEditorType = getDefaultEditorType,
        flavourConfigProvider = flavourConfigProvider,
        objectSearchSubscriptionContainer = objectSearchSubscriptionContainer,
        cancelSearchSubscription = cancelSearchSubscription,
        objectStore = objectStore
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideGetProfileUseCase(
        repository: BlockRepository,
        subscriptionEventChannel: SubscriptionEventChannel
    ): GetProfile = GetProfile(
        repo = repository,
        channel = subscriptionEventChannel
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideOpenDashboardUseCase(
        repo: BlockRepository,
        auth: AuthRepository
    ): OpenDashboard = OpenDashboard(
        repo = repo,
        auth = auth
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
        repo: BlockRepository
    ): CreatePage = CreatePage(
        repo = repo
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
    fun provideEventConverter(
        builder: UrlBuilder,
        objectTypesProvider: ObjectTypesProvider
    ): HomeDashboardEventConverter {
        return HomeDashboardEventConverter.DefaultConverter(
            builder = builder,
            objectTypesProvider = objectTypesProvider
        )
    }

    @JvmStatic
    @Provides
    @PerScreen
    fun provideGetDebugSettings(
        repo: InfrastructureRepository
    ): GetDebugSettings = GetDebugSettings(
        repo = repo
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideSearchObjects(
        repo: BlockRepository
    ): SearchObjects = SearchObjects(
        repo = repo
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideGetDefaultPageType(repo: UserSettingsRepository): GetDefaultEditorType =
        GetDefaultEditorType(repo)

    @JvmStatic
    @Provides
    @PerScreen
    fun deleteObjects(
        repo: BlockRepository
    ): DeleteObjects = DeleteObjects(
        repo = repo
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun setObjectListIsArchived(
        repo: BlockRepository
    ): SetObjectListIsArchived = SetObjectListIsArchived(
        repo = repo
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun objectSearchSubscriptionContainer(
        repo: BlockRepository,
        channel: SubscriptionEventChannel,
        store: ObjectStore
    ): ObjectSearchSubscriptionContainer = ObjectSearchSubscriptionContainer(
        repo = repo,
        channel = channel,
        store = store,
        dispatchers = AppCoroutineDispatchers(
            io = Dispatchers.IO,
            computation = Dispatchers.Default,
            main = Dispatchers.Main
        )
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun cancelSearchSubscription(
        repo: BlockRepository,
        store: ObjectStore
    ): CancelSearchSubscription = CancelSearchSubscription(
        repo = repo,
        store = store
    )
}