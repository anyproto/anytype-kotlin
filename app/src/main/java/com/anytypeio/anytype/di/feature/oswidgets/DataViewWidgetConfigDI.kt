package com.anytypeio.anytype.di.feature.oswidgets

import android.content.Context
import com.anytypeio.anytype.core_models.UrlBuilder
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.`object`.GetObject
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.feature_os_widgets.persistence.DataViewItemsFetcher
import com.anytypeio.anytype.feature_os_widgets.persistence.OsWidgetsDataStore
import com.anytypeio.anytype.feature_os_widgets.presentation.DataViewWidgetConfigViewModel
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
    fun provideDataStore(context: Context): OsWidgetsDataStore = OsWidgetsDataStore(context)

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
    fun provideDataViewWidgetViewModelFactory(
        context: Context,
        spaceViews: SpaceViewSubscriptionContainer,
        urlBuilder: UrlBuilder,
        searchObjects: SearchObjects,
        getObject: GetObject,
        dataStore: OsWidgetsDataStore,
        itemsFetcher: DataViewItemsFetcher
    ): DataViewWidgetConfigViewModel.Factory = DataViewWidgetConfigViewModel.Factory(
        context = context,
        spaceViews = spaceViews,
        urlBuilder = urlBuilder,
        searchObjects = searchObjects,
        getObject = getObject,
        dataStore = dataStore,
        itemsFetcher = itemsFetcher
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
}
