package com.anytypeio.anytype.di.feature.oswidgets

import android.content.Context
import com.anytypeio.anytype.core_models.UrlBuilder
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.domain.account.AwaitAccountStartManager
import com.anytypeio.anytype.domain.auth.interactor.LaunchAccount
import com.anytypeio.anytype.domain.auth.interactor.LaunchWallet
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.device.PathProvider
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.`object`.GetObject
import com.anytypeio.anytype.domain.platform.InitialParamsProvider
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.feature_os_widgets.persistence.DataViewItemsFetcher
import com.anytypeio.anytype.feature_os_widgets.persistence.OsWidgetDataViewEntity
import com.anytypeio.anytype.feature_os_widgets.persistence.OsWidgetsDataStore
import com.anytypeio.anytype.feature_os_widgets.presentation.DataViewWidgetConfigStore
import com.anytypeio.anytype.feature_os_widgets.presentation.DataViewWidgetUpdater
import com.anytypeio.anytype.feature_os_widgets.presentation.DataViewWidgetConfigViewModel
import com.anytypeio.anytype.feature_os_widgets.ui.OsDataViewWidgetUpdater
import com.anytypeio.anytype.ui.oswidgets.DataViewWidgetConfigActivity
import dagger.Component
import dagger.Module
import dagger.Provides

@Component(
    dependencies = [DataViewWidgetConfigDependencies::class],
    modules = [DataViewWidgetConfigModule::class]
)
@PerScreen
interface DataViewWidgetConfigComponent {

    fun inject(activity: DataViewWidgetConfigActivity)

    @Component.Factory
    interface Factory {
        fun create(dependencies: DataViewWidgetConfigDependencies): DataViewWidgetConfigComponent
    }
}

@Module
object DataViewWidgetConfigModule {

    @JvmStatic
    @Provides
    @PerScreen
    fun provideDataStore(context: Context): DataViewWidgetConfigStore {
        val appContext = context.applicationContext
        val dataStore = OsWidgetsDataStore(appContext)
        return object : DataViewWidgetConfigStore {
            override suspend fun save(config: OsWidgetDataViewEntity) {
                dataStore.saveDataViewConfig(config)
            }
        }
    }

    @JvmStatic
    @Provides
    @PerScreen
    fun provideDataViewWidgetUpdater(context: Context): DataViewWidgetUpdater {
        val appContext = context.applicationContext
        return DataViewWidgetUpdater { appWidgetId ->
            OsDataViewWidgetUpdater.update(appContext, appWidgetId)
        }
    }

    @JvmStatic
    @Provides
    @PerScreen
    fun provideDataViewItemsFetcher(
        getObject: GetObject,
        searchObjects: SearchObjects,
        blockRepository: BlockRepository
    ): DataViewItemsFetcher = DataViewItemsFetcher(
        getObject = getObject,
        searchObjects = searchObjects,
        blockRepository = blockRepository
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideLaunchWallet(
        authRepository: AuthRepository,
        pathProvider: PathProvider
    ): LaunchWallet = LaunchWallet(
        repository = authRepository,
        pathProvider = pathProvider
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideLaunchAccount(
        authRepository: AuthRepository,
        pathProvider: PathProvider,
        configStorage: ConfigStorage,
        spaceManager: SpaceManager,
        initialParamsProvider: InitialParamsProvider,
        userSettings: UserSettingsRepository,
        awaitAccountStartManager: AwaitAccountStartManager
    ): LaunchAccount = LaunchAccount(
        repository = authRepository,
        pathProvider = pathProvider,
        configStorage = configStorage,
        spaceManager = spaceManager,
        initialParamsProvider = initialParamsProvider,
        settings = userSettings,
        awaitAccountStartManager = awaitAccountStartManager
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideDataViewWidgetViewModelFactory(
        spaceViews: SpaceViewSubscriptionContainer,
        urlBuilder: UrlBuilder,
        searchObjects: SearchObjects,
        getObject: GetObject,
        dataStore: DataViewWidgetConfigStore,
        itemsFetcher: DataViewItemsFetcher,
        widgetUpdater: DataViewWidgetUpdater,
        launchWallet: LaunchWallet,
        launchAccount: LaunchAccount
    ): DataViewWidgetConfigViewModel.Factory = DataViewWidgetConfigViewModel.Factory(
        spaceViews = spaceViews,
        urlBuilder = urlBuilder,
        searchObjects = searchObjects,
        getObject = getObject,
        dataStore = dataStore,
        itemsFetcher = itemsFetcher,
        widgetUpdater = widgetUpdater,
        launchWallet = launchWallet,
        launchAccount = launchAccount
    )
}

interface DataViewWidgetConfigDependencies : ComponentDependencies {
    fun context(): Context
    fun spaceViewSubscriptionContainer(): SpaceViewSubscriptionContainer
    fun urlBuilder(): UrlBuilder
    fun searchObjects(): SearchObjects
    fun blockRepository(): BlockRepository
    fun userSettingsRepository(): UserSettingsRepository
    fun dispatchers(): AppCoroutineDispatchers
    fun authRepository(): AuthRepository
    fun pathProvider(): PathProvider
    fun configStorage(): ConfigStorage
    fun spaceManager(): SpaceManager
    fun metricsProvider(): InitialParamsProvider
    fun awaitAccountStartManager(): AwaitAccountStartManager
}
