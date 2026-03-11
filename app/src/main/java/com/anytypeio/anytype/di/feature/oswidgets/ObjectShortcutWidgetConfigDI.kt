package com.anytypeio.anytype.di.feature.oswidgets

import android.content.Context
import com.anytypeio.anytype.core_models.UrlBuilder
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.search.SearchObjects
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
    fun provideObjectShortcutViewModelFactory(
        spaceViews: SpaceViewSubscriptionContainer,
        urlBuilder: UrlBuilder,
        searchObjects: SearchObjects,
        configStore: ObjectShortcutWidgetConfigStore,
        iconCache: ObjectShortcutIconCache,
        widgetUpdater: ObjectShortcutWidgetUpdater
    ): ObjectShortcutWidgetConfigViewModel.Factory = ObjectShortcutWidgetConfigViewModel.Factory(
        spaceViews = spaceViews,
        urlBuilder = urlBuilder,
        searchObjects = searchObjects,
        configStore = configStore,
        iconCache = iconCache,
        widgetUpdater = widgetUpdater
    )
}

interface ObjectShortcutWidgetConfigDependencies : ComponentDependencies {
    fun context(): Context
    fun spaceViewSubscriptionContainer(): SpaceViewSubscriptionContainer
    fun urlBuilder(): UrlBuilder
    fun searchObjects(): SearchObjects
}
