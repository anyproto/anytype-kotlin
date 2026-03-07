package com.anytypeio.anytype.di.feature.oswidgets

import android.content.Context
import com.anytypeio.anytype.core_models.UrlBuilder
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.feature_os_widgets.persistence.OsWidgetCreateObjectEntity
import com.anytypeio.anytype.feature_os_widgets.persistence.OsWidgetIconCache
import com.anytypeio.anytype.feature_os_widgets.persistence.OsWidgetSpaceShortcutEntity
import com.anytypeio.anytype.feature_os_widgets.persistence.OsWidgetsDataStore
import com.anytypeio.anytype.feature_os_widgets.presentation.CreateObjectWidgetConfigStore
import com.anytypeio.anytype.feature_os_widgets.presentation.CreateObjectWidgetConfigViewModel
import com.anytypeio.anytype.feature_os_widgets.presentation.CreateObjectWidgetUpdater
import com.anytypeio.anytype.feature_os_widgets.presentation.SpaceShortcutIconCache
import com.anytypeio.anytype.feature_os_widgets.presentation.SpaceShortcutWidgetConfigStore
import com.anytypeio.anytype.feature_os_widgets.presentation.SpaceShortcutWidgetConfigViewModel
import com.anytypeio.anytype.feature_os_widgets.presentation.SpaceShortcutWidgetUpdater
import com.anytypeio.anytype.feature_os_widgets.ui.OsCreateObjectWidgetUpdater
import com.anytypeio.anytype.feature_os_widgets.ui.OsSpaceShortcutWidgetUpdater
import com.anytypeio.anytype.ui.oswidgets.CreateObjectWidgetConfigActivity
import com.anytypeio.anytype.ui.oswidgets.SpaceShortcutWidgetConfigActivity
import dagger.Component
import dagger.Module
import dagger.Provides

@Component(
    dependencies = [CreateObjectWidgetConfigDependencies::class],
    modules = [CreateObjectWidgetConfigModule::class]
)
@PerScreen
interface CreateObjectWidgetConfigComponent {

    fun inject(activity: CreateObjectWidgetConfigActivity)
    fun inject(activity: SpaceShortcutWidgetConfigActivity)

    @Component.Factory
    interface Factory {
        fun create(dependencies: CreateObjectWidgetConfigDependencies): CreateObjectWidgetConfigComponent
    }
}

@Module
object CreateObjectWidgetConfigModule {

    @JvmStatic
    @Provides
    @PerScreen
    fun provideCreateObjectStore(context: Context): CreateObjectWidgetConfigStore {
        val appContext = context.applicationContext
        val dataStore = OsWidgetsDataStore(appContext)
        return object : CreateObjectWidgetConfigStore {
            override suspend fun save(config: OsWidgetCreateObjectEntity) {
                dataStore.saveCreateObjectConfig(config)
            }
        }
    }

    @JvmStatic
    @Provides
    @PerScreen
    fun provideCreateObjectWidgetUpdater(context: Context): CreateObjectWidgetUpdater {
        val appContext = context.applicationContext
        return CreateObjectWidgetUpdater { appWidgetId ->
            OsCreateObjectWidgetUpdater.update(appContext, appWidgetId)
        }
    }

    @JvmStatic
    @Provides
    @PerScreen
    fun provideSpaceShortcutStore(context: Context): SpaceShortcutWidgetConfigStore {
        val appContext = context.applicationContext
        val dataStore = OsWidgetsDataStore(appContext)
        return object : SpaceShortcutWidgetConfigStore {
            override suspend fun save(config: OsWidgetSpaceShortcutEntity) {
                dataStore.saveSpaceShortcutConfig(config)
            }
        }
    }

    @JvmStatic
    @Provides
    @PerScreen
    fun provideSpaceShortcutIconCache(context: Context): SpaceShortcutIconCache {
        val appContext = context.applicationContext
        val iconCache = OsWidgetIconCache(appContext)
        return object : SpaceShortcutIconCache {
            override suspend fun cacheForWidget(url: String, appWidgetId: Int): String? {
                return iconCache.cacheShortcutIcon(
                    url = url,
                    widgetId = appWidgetId,
                    prefix = OsWidgetIconCache.PREFIX_SPACE
                )
            }
        }
    }

    @JvmStatic
    @Provides
    @PerScreen
    fun provideSpaceShortcutWidgetUpdater(context: Context): SpaceShortcutWidgetUpdater {
        val appContext = context.applicationContext
        return SpaceShortcutWidgetUpdater { appWidgetId ->
            OsSpaceShortcutWidgetUpdater.update(appContext, appWidgetId)
        }
    }

    @JvmStatic
    @Provides
    @PerScreen
    fun provideSpaceShortcutViewModelFactory(
        spaceViews: SpaceViewSubscriptionContainer,
        urlBuilder: UrlBuilder,
        configStore: SpaceShortcutWidgetConfigStore,
        iconCache: SpaceShortcutIconCache,
        widgetUpdater: SpaceShortcutWidgetUpdater
    ): SpaceShortcutWidgetConfigViewModel.Factory = SpaceShortcutWidgetConfigViewModel.Factory(
        spaceViews = spaceViews,
        urlBuilder = urlBuilder,
        configStore = configStore,
        iconCache = iconCache,
        widgetUpdater = widgetUpdater
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideCreateObjectViewModelFactory(
        spaceViews: SpaceViewSubscriptionContainer,
        urlBuilder: UrlBuilder,
        configStore: CreateObjectWidgetConfigStore,
        widgetUpdater: CreateObjectWidgetUpdater
    ): CreateObjectWidgetConfigViewModel.Factory = CreateObjectWidgetConfigViewModel.Factory(
        spaceViews = spaceViews,
        urlBuilder = urlBuilder,
        configStore = configStore,
        widgetUpdater = widgetUpdater
    )
}

interface CreateObjectWidgetConfigDependencies : ComponentDependencies {
    fun context(): Context
    fun spaceViewSubscriptionContainer(): SpaceViewSubscriptionContainer
    fun urlBuilder(): UrlBuilder
}
