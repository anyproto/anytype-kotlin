package com.anytypeio.anytype.di.feature.oswidgets

import android.content.Context
import com.anytypeio.anytype.core_models.UrlBuilder
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.feature_os_widgets.presentation.ObjectShortcutWidgetConfigViewModel
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
    fun provideObjectShortcutViewModelFactory(
        context: Context,
        spaceViews: SpaceViewSubscriptionContainer,
        urlBuilder: UrlBuilder,
        searchObjects: SearchObjects
    ): ObjectShortcutWidgetConfigViewModel.Factory = ObjectShortcutWidgetConfigViewModel.Factory(
        context = context,
        spaceViews = spaceViews,
        urlBuilder = urlBuilder,
        searchObjects = searchObjects
    )
}

interface ObjectShortcutWidgetConfigDependencies : ComponentDependencies {
    fun context(): Context
    fun spaceViewSubscriptionContainer(): SpaceViewSubscriptionContainer
    fun urlBuilder(): UrlBuilder
    fun searchObjects(): SearchObjects
}
