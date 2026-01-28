package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.core_models.UrlBuilder
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import com.anytypeio.anytype.presentation.moving.MoveToViewModel
import com.anytypeio.anytype.presentation.moving.MoveToViewModelFactory
import com.anytypeio.anytype.ui.moving.MoveToFragment
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides

@Component(
    modules = [MoveToModule::class],
    dependencies = [MoveToDependencies::class]
)
@PerScreen
interface MoveToComponent {

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance
            vmParams: MoveToViewModel.VmParams,
            dependencies: MoveToDependencies
        ): MoveToComponent
    }

    fun inject(fragment: MoveToFragment)
}

interface MoveToDependencies : ComponentDependencies {
    fun urlBuilder(): UrlBuilder
    fun searchObjects(): SearchObjects
    fun analytics(): Analytics
    fun analyticSpaceHelperDelegate(): AnalyticSpaceHelperDelegate
    fun  fieldParser(): FieldParser
    fun storeOfObjectTypes(): StoreOfObjectTypes
}

@Module
object MoveToModule {

    @JvmStatic
    @PerScreen
    @Provides
    fun provideMoveToViewModelFactory(
        vmParams: MoveToViewModel.VmParams,
        urlBuilder: UrlBuilder,
        searchObjects: SearchObjects,
        analytics: Analytics,
        analyticSpaceHelperDelegate: AnalyticSpaceHelperDelegate,
        fieldParser: FieldParser,
        storeOfObjectTypes: StoreOfObjectTypes
    ): MoveToViewModelFactory = MoveToViewModelFactory(
        vmParams = vmParams,
        urlBuilder = urlBuilder,
        searchObjects = searchObjects,
        analytics = analytics,
        analyticSpaceHelperDelegate = analyticSpaceHelperDelegate,
        fieldParser = fieldParser,
        storeOfObjectTypes = storeOfObjectTypes
    )
}