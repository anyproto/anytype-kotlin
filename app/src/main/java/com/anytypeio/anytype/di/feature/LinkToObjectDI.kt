package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.domain.block.interactor.sets.GetObjectTypes
import com.anytypeio.anytype.domain.misc.DateProvider
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import com.anytypeio.anytype.presentation.linking.LinkToObjectViewModelFactory
import com.anytypeio.anytype.presentation.search.ObjectSearchViewModel
import com.anytypeio.anytype.ui.linking.LinkToObjectFragment
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides

@Component(
    modules = [LinkToObjectModule::class],
    dependencies = [LinkToObjectDependencies::class]
)
@PerScreen
interface LinkToObjectComponent {

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance
            params: ObjectSearchViewModel.VmParams,
            dependencies: LinkToObjectDependencies
        ) : LinkToObjectComponent
    }

    fun inject(fragment: LinkToObjectFragment)
}

interface LinkToObjectDependencies: ComponentDependencies {
    fun urlBuilder(): UrlBuilder
    fun getObjectTypes(): GetObjectTypes
    fun searchObjects(): SearchObjects
    fun analytics(): Analytics
    fun analyticSpaceHelperDelegate(): AnalyticSpaceHelperDelegate
    fun dateProvider(): DateProvider
    fun fieldParser(): FieldParser
    fun storeOfObjectTypes(): StoreOfObjectTypes
    fun spaceViews(): SpaceViewSubscriptionContainer
}

@Module
object LinkToObjectModule {

    @JvmStatic
    @PerScreen
    @Provides
    fun provideLinkToObjectViewModelFactory(
        vmParams: ObjectSearchViewModel.VmParams,
        urlBuilder: UrlBuilder,
        getObjectTypes: GetObjectTypes,
        searchObjects: SearchObjects,
        analytics: Analytics,
        analyticSpaceHelperDelegate: AnalyticSpaceHelperDelegate,
        fieldParser: FieldParser,
        storeOfObjectTypes: StoreOfObjectTypes,
        spaceViews: SpaceViewSubscriptionContainer
    ): LinkToObjectViewModelFactory = LinkToObjectViewModelFactory(
        vmParams = vmParams,
        urlBuilder = urlBuilder,
        getObjectTypes = getObjectTypes,
        searchObjects = searchObjects,
        analytics = analytics,
        analyticSpaceHelperDelegate = analyticSpaceHelperDelegate,
        fieldParser = fieldParser,
        storeOfObjectTypes = storeOfObjectTypes,
        spaceViews = spaceViews
    )
}
