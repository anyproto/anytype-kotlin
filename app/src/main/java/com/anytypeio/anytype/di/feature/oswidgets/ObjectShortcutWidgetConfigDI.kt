package com.anytypeio.anytype.di.feature.oswidgets

import android.content.Context
import com.anytypeio.anytype.core_models.UrlBuilder
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.domain.account.AwaitAccountStartManager
import com.anytypeio.anytype.domain.auth.interactor.LaunchAccount
import com.anytypeio.anytype.domain.auth.interactor.LaunchWallet
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.device.PathProvider
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.platform.InitialParamsProvider
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.feature_os_widgets.persistence.OsWidgetIconCache
import com.anytypeio.anytype.feature_os_widgets.persistence.OsWidgetObjectShortcutEntity
import com.anytypeio.anytype.feature_os_widgets.persistence.OsWidgetsDataStore
import com.anytypeio.anytype.feature_os_widgets.presentation.ObjectShortcutIconCache
import com.anytypeio.anytype.feature_os_widgets.presentation.ObjectShortcutWidgetConfigStore
import com.anytypeio.anytype.feature_os_widgets.presentation.ObjectShortcutWidgetConfigViewModel
import com.anytypeio.anytype.feature_os_widgets.presentation.ObjectShortcutWidgetUpdater
import com.anytypeio.anytype.feature_os_widgets.ui.OsObjectShortcutWidgetUpdater
import com.anytypeio.anytype.ui.oswidgets.ObjectShortcutWidgetConfigActivity
import dagger.Component
import dagger.Module
import dagger.Provides

@Component(
    dependencies = [ObjectShortcutWidgetConfigDependencies::class],
    modules = [ObjectShortcutWidgetConfigModule::class]
)
@PerScreen
interface ObjectShortcutWidgetConfigComponent {

    fun inject(activity: ObjectShortcutWidgetConfigActivity)

    @Component.Factory
    interface Factory {
        fun create(dependencies: ObjectShortcutWidgetConfigDependencies): ObjectShortcutWidgetConfigComponent
    }
}

@Module
object ObjectShortcutWidgetConfigModule {

    @JvmStatic
    @Provides
    @PerScreen
    fun provideObjectShortcutStore(context: Context): ObjectShortcutWidgetConfigStore {
        val appContext = context.applicationContext
        val dataStore = OsWidgetsDataStore(appContext)
        return object : ObjectShortcutWidgetConfigStore {
            override suspend fun save(config: OsWidgetObjectShortcutEntity) {
                dataStore.saveObjectShortcutConfig(config)
            }
        }
    }

    @JvmStatic
    @Provides
    @PerScreen
    fun provideObjectShortcutIconCache(context: Context): ObjectShortcutIconCache {
        val appContext = context.applicationContext
        val iconCache = OsWidgetIconCache(appContext)
        return object : ObjectShortcutIconCache {
            override suspend fun cacheForWidget(url: String, appWidgetId: Int): String? {
                return iconCache.cacheShortcutIcon(
                    url = url,
                    widgetId = appWidgetId,
                    prefix = OsWidgetIconCache.PREFIX_OBJECT
                )
            }
        }
    }

    @JvmStatic
    @Provides
    @PerScreen
    fun provideObjectShortcutWidgetUpdater(context: Context): ObjectShortcutWidgetUpdater {
        val appContext = context.applicationContext
        return ObjectShortcutWidgetUpdater { appWidgetId ->
            OsObjectShortcutWidgetUpdater.update(appContext, appWidgetId)
        }
    }

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
    fun provideObjectShortcutViewModelFactory(
        spaceViews: SpaceViewSubscriptionContainer,
        urlBuilder: UrlBuilder,
        searchObjects: SearchObjects,
        configStore: ObjectShortcutWidgetConfigStore,
        iconCache: ObjectShortcutIconCache,
        widgetUpdater: ObjectShortcutWidgetUpdater,
        launchWallet: LaunchWallet,
        launchAccount: LaunchAccount
    ): ObjectShortcutWidgetConfigViewModel.Factory = ObjectShortcutWidgetConfigViewModel.Factory(
        spaceViews = spaceViews,
        urlBuilder = urlBuilder,
        searchObjects = searchObjects,
        configStore = configStore,
        iconCache = iconCache,
        widgetUpdater = widgetUpdater,
        launchWallet = launchWallet,
        launchAccount = launchAccount
    )
}

interface ObjectShortcutWidgetConfigDependencies : ComponentDependencies {
    fun context(): Context
    fun spaceViewSubscriptionContainer(): SpaceViewSubscriptionContainer
    fun urlBuilder(): UrlBuilder
    fun searchObjects(): SearchObjects
    fun authRepository(): AuthRepository
    fun pathProvider(): PathProvider
    fun configStorage(): ConfigStorage
    fun spaceManager(): SpaceManager
    fun metricsProvider(): InitialParamsProvider
    fun userSettingsRepository(): UserSettingsRepository
    fun awaitAccountStartManager(): AwaitAccountStartManager
}
