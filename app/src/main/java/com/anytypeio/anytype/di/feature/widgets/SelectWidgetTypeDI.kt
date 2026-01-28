package com.anytypeio.anytype.di.feature.widgets

import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_utils.di.scope.PerModal
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.core_models.UrlBuilder
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.widgets.UpdateWidget
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.presentation.widgets.SelectWidgetTypeViewModel
import com.anytypeio.anytype.presentation.widgets.WidgetDispatchEvent
import com.anytypeio.anytype.ui.widgets.SelectWidgetTypeFragment
import dagger.Component
import dagger.Module
import dagger.Provides

@Component(
    modules = [SelectWidgetTypeModule::class],
    dependencies = [SelectWidgetTypeDependencies::class]
)
@PerModal
interface SelectWidgetTypeComponent {

    @Component.Factory
    interface Factory {
        fun create(dependencies: SelectWidgetTypeDependencies): SelectWidgetTypeComponent
    }

    fun inject(fragment: SelectWidgetTypeFragment)
}

interface SelectWidgetTypeDependencies : ComponentDependencies {
    fun dispatcherWidgets(): Dispatcher<WidgetDispatchEvent>
    fun dispatcherPayload(): Dispatcher<Payload>
    fun blockRepository(): BlockRepository
    fun analytics(): Analytics
    fun urlBuilder(): UrlBuilder
    fun dispatchers(): AppCoroutineDispatchers
    fun storeOfObjectTypes(): StoreOfObjectTypes
}

@Module
object SelectWidgetTypeModule {

    @JvmStatic
    @PerModal
    @Provides
    fun factory(
        payloadDispatcher: Dispatcher<Payload>,
        widgetDispatcher: Dispatcher<WidgetDispatchEvent>,
        updateWidget: UpdateWidget,
        appCoroutineDispatchers: AppCoroutineDispatchers,
        analytics: Analytics
    ): SelectWidgetTypeViewModel.Factory = SelectWidgetTypeViewModel.Factory(
        payloadDispatcher = payloadDispatcher,
        widgetDispatcher = widgetDispatcher,
        updateWidget = updateWidget,
        appCoroutineDispatchers = appCoroutineDispatchers,
        analytics = analytics
    )
}