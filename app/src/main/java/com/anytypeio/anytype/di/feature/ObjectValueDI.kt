package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_utils.di.scope.PerModal
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.`object`.DuplicateObject
import com.anytypeio.anytype.domain.`object`.UpdateDetail
import com.anytypeio.anytype.domain.objects.SetObjectListIsArchived
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.relations.providers.ObjectRelationProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectValueProvider
import com.anytypeio.anytype.presentation.relations.value.`object`.ObjectValueViewModel
import com.anytypeio.anytype.presentation.relations.value.`object`.ObjectValueViewModelFactory
import com.anytypeio.anytype.presentation.spaces.SpaceGradientProvider
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.ui.relations.value.ObjectValueFragment
import dagger.BindsInstance
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import javax.inject.Named

//region OBJECT (layout BASIC, TASK etc.)
@PerModal
@Subcomponent(
    modules = [ObjectValueObjectModule::class]
)
interface ObjectValueObjectComponent {

    @Subcomponent.Builder
    interface Builder {
        @BindsInstance
        fun params(params: ObjectValueViewModel.ViewModelParams): Builder
        fun build(): ObjectValueObjectComponent
    }

    fun inject(fragment: ObjectValueFragment)
}

@Module
object ObjectValueObjectModule {

    @JvmStatic
    @Provides
    @PerModal
    fun provideSpaceGradientProvider(): SpaceGradientProvider = SpaceGradientProvider.Default

    @JvmStatic
    @PerModal
    @Provides
    fun getSetObjectListIsArchived(
        repo: BlockRepository,
        dispatchers: AppCoroutineDispatchers
    ): SetObjectListIsArchived = SetObjectListIsArchived(repo, dispatchers)

    @JvmStatic
    @Provides
    @PerModal
    fun provideFactory(
        @Named(ObjectRelationProvider.INTRINSIC_PROVIDER_TYPE) relations: ObjectRelationProvider,
        @Named(ObjectRelationProvider.INTRINSIC_PROVIDER_TYPE) values: ObjectValueProvider,
        setObjectDetails: UpdateDetail,
        dispatcher: Dispatcher<Payload>,
        analytics: Analytics,
        spaceManager: SpaceManager,
        params: ObjectValueViewModel.ViewModelParams,
        subscription: StorelessSubscriptionContainer,
        urlBuilder: UrlBuilder,
        storeOfObjectTypes: StoreOfObjectTypes,
        gradientProvider: SpaceGradientProvider,
        objectListIsArchived: SetObjectListIsArchived,
        duplicateObject: DuplicateObject
    ): ObjectValueViewModelFactory = ObjectValueViewModelFactory(
        params = params,
        values = values,
        relations = relations,
        setObjectDetails = setObjectDetails,
        dispatcher = dispatcher,
        analytics = analytics,
        spaceManager = spaceManager,
        subscription = subscription,
        urlBuilder = urlBuilder,
        storeOfObjectTypes = storeOfObjectTypes,
        gradientProvider = gradientProvider,
        objectListIsArchived = objectListIsArchived,
        duplicateObject = duplicateObject
    )
}
//endregion

//region SET or COLLECTION (layout SET, COLLECTION )
@PerModal
@Subcomponent(
    modules = [ObjectValueSetModule::class]
)
interface ObjectValueSetComponent {

    @Subcomponent.Builder
    interface Builder {
        @BindsInstance
        fun params(params: ObjectValueViewModel.ViewModelParams): Builder
        fun build(): ObjectValueSetComponent
    }

    fun inject(fragment: ObjectValueFragment)
}

@Module
object ObjectValueSetModule {

    @JvmStatic
    @Provides
    @PerModal
    fun provideSpaceGradientProvider(): SpaceGradientProvider = SpaceGradientProvider.Default

    @JvmStatic
    @Provides
    @PerModal
    fun provideFactory(
        @Named(ObjectRelationProvider.INTRINSIC_PROVIDER_TYPE) relations: ObjectRelationProvider,
        @Named(ObjectRelationProvider.INTRINSIC_PROVIDER_TYPE) values: ObjectValueProvider,
        setObjectDetails: UpdateDetail,
        dispatcher: Dispatcher<Payload>,
        analytics: Analytics,
        spaceManager: SpaceManager,
        params: ObjectValueViewModel.ViewModelParams,
        subscription: StorelessSubscriptionContainer,
        urlBuilder: UrlBuilder,
        storeOfObjectTypes: StoreOfObjectTypes,
        gradientProvider: SpaceGradientProvider,
        objectListIsArchived: SetObjectListIsArchived,
        duplicateObject: DuplicateObject
    ): ObjectValueViewModelFactory = ObjectValueViewModelFactory(
        params = params,
        values = values,
        relations = relations,
        setObjectDetails = setObjectDetails,
        dispatcher = dispatcher,
        analytics = analytics,
        spaceManager = spaceManager,
        subscription = subscription,
        urlBuilder = urlBuilder,
        storeOfObjectTypes = storeOfObjectTypes,
        gradientProvider = gradientProvider,
        objectListIsArchived = objectListIsArchived,
        duplicateObject = duplicateObject
    )
}
//endregion

//region DATA VIEW
@PerModal
@Subcomponent(
    modules = [ObjectValueDataViewModule::class]
)
interface ObjectValueDataViewComponent {

    @Subcomponent.Builder
    interface Builder {
        @BindsInstance
        fun params(params: ObjectValueViewModel.ViewModelParams): Builder
        fun build(): ObjectValueDataViewComponent
    }

    fun inject(fragment: ObjectValueFragment)
}

@Module
object ObjectValueDataViewModule {

    @JvmStatic
    @Provides
    @PerModal
    fun provideSpaceGradientProvider(): SpaceGradientProvider = SpaceGradientProvider.Default

    @JvmStatic
    @Provides
    @PerModal
    fun provideFactory(
        @Named(ObjectRelationProvider.DATA_VIEW_PROVIDER_TYPE) relations: ObjectRelationProvider,
        @Named(ObjectRelationProvider.DATA_VIEW_PROVIDER_TYPE) values: ObjectValueProvider,
        setObjectDetails: UpdateDetail,
        dispatcher: Dispatcher<Payload>,
        analytics: Analytics,
        spaceManager: SpaceManager,
        params: ObjectValueViewModel.ViewModelParams,
        subscription: StorelessSubscriptionContainer,
        urlBuilder: UrlBuilder,
        storeOfObjectTypes: StoreOfObjectTypes,
        gradientProvider: SpaceGradientProvider,
        objectListIsArchived: SetObjectListIsArchived,
        duplicateObject: DuplicateObject
    ): ObjectValueViewModelFactory = ObjectValueViewModelFactory(
        params = params,
        values = values,
        relations = relations,
        setObjectDetails = setObjectDetails,
        dispatcher = dispatcher,
        analytics = analytics,
        spaceManager = spaceManager,
        subscription = subscription,
        urlBuilder = urlBuilder,
        storeOfObjectTypes = storeOfObjectTypes,
        gradientProvider = gradientProvider,
        objectListIsArchived = objectListIsArchived,
        duplicateObject = duplicateObject
    )
}
//endregion