package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_utils.di.scope.PerModal
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.`object`.UpdateDetail
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.options.GetOptions
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.relations.providers.ObjectDetailProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectRelationProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectValueProvider
import com.anytypeio.anytype.presentation.relations.value.tagstatus.TagStatusViewModel
import com.anytypeio.anytype.presentation.relations.value.tagstatus.TagStatusViewModelFactory
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.ui.relations.value.TagStatusValueFragment
import dagger.BindsInstance
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import javax.inject.Named

@PerModal
@Subcomponent(
    modules = [TagStatusObjectModule::class]
)
interface TagStatusObjectComponent {

    @Subcomponent.Builder
    interface Builder {
        @BindsInstance
        fun params(params: TagStatusViewModel.Params): Builder
        fun build(): TagStatusObjectComponent
    }

    fun inject(fragment: TagStatusValueFragment)
}

@Module
object TagStatusObjectModule {

    @JvmStatic
    @Provides
    @PerModal
    fun provideFactory(
        @Named(ObjectRelationProvider.INTRINSIC_PROVIDER_TYPE) relations: ObjectRelationProvider,
        @Named(ObjectRelationProvider.INTRINSIC_PROVIDER_TYPE) values: ObjectValueProvider,
        details: ObjectDetailProvider,
        storeOfObjectTypes: StoreOfObjectTypes,
        urlBuilder: UrlBuilder,
        setObjectDetails: UpdateDetail,
        dispatcher: Dispatcher<Payload>,
        analytics: Analytics,
        getOptions: GetOptions,
        spaceManager: SpaceManager,
        params: TagStatusViewModel.Params
    ): TagStatusViewModelFactory = TagStatusViewModelFactory(
        params = params,
        values = values,
        details = details,
        relations = relations,
        storeOfObjectTypes = storeOfObjectTypes,
        urlBuilder = urlBuilder,
        setObjectDetails = setObjectDetails,
        dispatcher = dispatcher,
        analytics = analytics,
        getOptions = getOptions,
        spaceManager = spaceManager
    )
}