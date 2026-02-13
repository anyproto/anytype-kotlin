package com.anytypeio.anytype.di.feature.oswidgets

import com.anytypeio.anytype.core_models.UrlBuilder
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.ui.oswidgets.ObjectShortcutWidgetConfigActivity
import dagger.Component

@Component(
    dependencies = [ObjectShortcutWidgetConfigDependencies::class]
)
@PerScreen
interface ObjectShortcutWidgetConfigComponent {

    fun inject(activity: ObjectShortcutWidgetConfigActivity)

    @Component.Factory
    interface Factory {
        fun create(dependencies: ObjectShortcutWidgetConfigDependencies): ObjectShortcutWidgetConfigComponent
    }
}

interface ObjectShortcutWidgetConfigDependencies : ComponentDependencies {
    fun spaceViewSubscriptionContainer(): SpaceViewSubscriptionContainer
    fun urlBuilder(): UrlBuilder
    fun searchObjects(): SearchObjects
}
