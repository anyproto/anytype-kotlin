package com.anytypeio.anytype.di.feature.oswidgets

import com.anytypeio.anytype.core_models.UrlBuilder
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.ui.oswidgets.CreateObjectWidgetConfigActivity
import dagger.Component

@Component(
    dependencies = [CreateObjectWidgetConfigDependencies::class]
)
@PerScreen
interface CreateObjectWidgetConfigComponent {

    fun inject(activity: CreateObjectWidgetConfigActivity)

    @Component.Factory
    interface Factory {
        fun create(dependencies: CreateObjectWidgetConfigDependencies): CreateObjectWidgetConfigComponent
    }
}

interface CreateObjectWidgetConfigDependencies : ComponentDependencies {
    fun spaceViewSubscriptionContainer(): SpaceViewSubscriptionContainer
    fun urlBuilder(): UrlBuilder
}
