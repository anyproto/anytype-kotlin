package com.anytypeio.anytype.di.feature.widgets

import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_utils.di.scope.PerModal
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.interactor.sets.GetObjectTypes
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import com.anytypeio.anytype.presentation.search.ObjectSearchViewModel
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.presentation.widgets.SelectWidgetSourceViewModel
import com.anytypeio.anytype.presentation.widgets.WidgetDispatchEvent
import com.anytypeio.anytype.ui.widgets.SelectWidgetSourceFragment
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides

@Component(
    modules = [SelectWidgetSourceModule::class],
    dependencies = [SelectWidgetSourceDependencies::class]
)
@PerModal
interface SelectWidgetSourceComponent {

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance
            vmParams: ObjectSearchViewModel.VmParams,
            dependencies: SelectWidgetSourceDependencies
        ): SelectWidgetSourceComponent
    }

    fun inject(fragment: SelectWidgetSourceFragment)
}

interface SelectWidgetSourceDependencies : ComponentDependencies {
    fun dispatcherWidgets(): Dispatcher<WidgetDispatchEvent>
    fun blockRepository(): BlockRepository
    fun analytics(): Analytics
    fun urlBuilder(): UrlBuilder
    fun dispatchers(): AppCoroutineDispatchers
    fun storeOfObjectTypes(): StoreOfObjectTypes
    fun analyticsHelper(): AnalyticSpaceHelperDelegate
    fun searchObjects(): SearchObjects
    fun fieldParser(): FieldParser
}

@Module
object SelectWidgetSourceModule {

    @JvmStatic
    @PerModal
    @Provides
    fun factory(
        vmParams: ObjectSearchViewModel.VmParams,
        urlBuilder: UrlBuilder,
        analytics: Analytics,
        searchObjects: SearchObjects,
        getObjectTypes: GetObjectTypes,
        dispatcher: Dispatcher<WidgetDispatchEvent>,
        analyticsHelper: AnalyticSpaceHelperDelegate,
        fieldParser: FieldParser
    ): SelectWidgetSourceViewModel.Factory = SelectWidgetSourceViewModel.Factory(
        vmParams = vmParams,
        urlBuilder = urlBuilder,
        searchObjects = searchObjects,
        analytics = analytics,
        getObjectTypes = getObjectTypes,
        dispatcher = dispatcher,
        analyticSpaceHelperDelegate = analyticsHelper,
        fieldParser = fieldParser
    )
}