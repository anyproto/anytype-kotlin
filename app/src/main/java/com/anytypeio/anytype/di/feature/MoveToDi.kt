package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.domain.block.interactor.sets.GetObjectTypes
import com.anytypeio.anytype.domain.misc.DateProvider
import com.anytypeio.anytype.domain.misc.UrlBuilder
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
    fun getObjectTypes(): GetObjectTypes
    fun searchObjects(): SearchObjects
    fun analytics(): Analytics
    fun analyticSpaceHelperDelegate(): AnalyticSpaceHelperDelegate
    fun dateProvider(): DateProvider
}

@Module
object MoveToModule {

    @JvmStatic
    @PerScreen
    @Provides
    fun provideMoveToViewModelFactory(
        vmParams: MoveToViewModel.VmParams,
        urlBuilder: UrlBuilder,
        getObjectTypes: GetObjectTypes,
        searchObjects: SearchObjects,
        analytics: Analytics,
        analyticSpaceHelperDelegate: AnalyticSpaceHelperDelegate,
        dateProvider: DateProvider
    ): MoveToViewModelFactory = MoveToViewModelFactory(
        vmParams = vmParams,
        urlBuilder = urlBuilder,
        getObjectTypes = getObjectTypes,
        searchObjects = searchObjects,
        analytics = analytics,
        analyticSpaceHelperDelegate = analyticSpaceHelperDelegate,
        dateProvider = dateProvider
    )
}