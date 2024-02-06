package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_utils.di.scope.PerModal
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.`object`.SetObjectDetails
import com.anytypeio.anytype.domain.relations.CreateRelationOption
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.relations.option.CreateOrEditOptionViewModel
import com.anytypeio.anytype.presentation.relations.option.CreateOrEditOptionViewModelFactory
import com.anytypeio.anytype.presentation.relations.providers.ObjectRelationProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectValueProvider
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.ui.relations.value.CreateOrEditOptionFragment
import dagger.BindsInstance
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import javax.inject.Named

//region OBJECT (layout BASIC, TASK etc.)
@PerModal
@Subcomponent(
    modules = [CreateOrEditOptionObjectModule::class]
)
interface CreateOrEditOptionObjectComponent {

    @Subcomponent.Builder
    interface Builder {
        @BindsInstance
        fun params(params: CreateOrEditOptionViewModel.ViewModelParams): Builder
        fun build(): CreateOrEditOptionObjectComponent
    }

    fun inject(fragment: CreateOrEditOptionFragment)
}

@Module
object CreateOrEditOptionObjectModule {

    @JvmStatic
    @Provides
    @PerModal
    fun createRelationOption(
        repo: BlockRepository
    ): CreateRelationOption = CreateRelationOption(repo = repo)

    @JvmStatic
    @Provides
    @PerModal
    fun provideFactory(
        params: CreateOrEditOptionViewModel.ViewModelParams,
        @Named(ObjectRelationProvider.INTRINSIC_PROVIDER_TYPE) values: ObjectValueProvider,
        setObjectDetails: SetObjectDetails,
        dispatcher: Dispatcher<Payload>,
        spaceManager: SpaceManager,
        analytics: Analytics,
        createOption: CreateRelationOption
    ): CreateOrEditOptionViewModelFactory = CreateOrEditOptionViewModelFactory(
        params = params,
        values = values,
        createOption = createOption,
        setObjectDetails = setObjectDetails,
        dispatcher = dispatcher,
        spaceManager = spaceManager,
        analytics = analytics,
    )
}
//endregion

//region SET or COLLECTION (layout SET, COLLECTION )
@PerModal
@Subcomponent(
    modules = [CreateOrEditOptionSetModule::class]
)
interface CreateOrEditOptionSetComponent {

    @Subcomponent.Builder
    interface Builder {
        @BindsInstance
        fun params(params: CreateOrEditOptionViewModel.ViewModelParams): Builder
        fun build(): CreateOrEditOptionSetComponent
    }

    fun inject(fragment: CreateOrEditOptionFragment)
}

@Module
object CreateOrEditOptionSetModule {

    @JvmStatic
    @Provides
    @PerModal
    fun createRelationOption(
        repo: BlockRepository
    ): CreateRelationOption = CreateRelationOption(repo = repo)

    @JvmStatic
    @Provides
    @PerModal
    fun provideFactory(
        params: CreateOrEditOptionViewModel.ViewModelParams,
        @Named(ObjectRelationProvider.INTRINSIC_PROVIDER_TYPE) values: ObjectValueProvider,
        setObjectDetails: SetObjectDetails,
        dispatcher: Dispatcher<Payload>,
        spaceManager: SpaceManager,
        analytics: Analytics,
        createOption: CreateRelationOption
    ): CreateOrEditOptionViewModelFactory = CreateOrEditOptionViewModelFactory(
        params = params,
        values = values,
        createOption = createOption,
        setObjectDetails = setObjectDetails,
        dispatcher = dispatcher,
        spaceManager = spaceManager,
        analytics = analytics,
    )
}
//endregion

//region DATA VIEW
@PerModal
@Subcomponent(
    modules = [CreateOrEditOptionDataViewModule::class]
)
interface CreateOrEditOptionDataViewComponent {

    @Subcomponent.Builder
    interface Builder {
        @BindsInstance
        fun params(params: CreateOrEditOptionViewModel.ViewModelParams): Builder
        fun build(): CreateOrEditOptionDataViewComponent
    }

    fun inject(fragment: CreateOrEditOptionFragment)
}

@Module
object CreateOrEditOptionDataViewModule {

    @JvmStatic
    @Provides
    @PerModal
    fun createRelationOption(
        repo: BlockRepository
    ): CreateRelationOption = CreateRelationOption(repo = repo)

    @JvmStatic
    @Provides
    @PerModal
    fun provideFactory(
        params: CreateOrEditOptionViewModel.ViewModelParams,
        @Named(ObjectRelationProvider.DATA_VIEW_PROVIDER_TYPE) values: ObjectValueProvider,
        setObjectDetails: SetObjectDetails,
        dispatcher: Dispatcher<Payload>,
        spaceManager: SpaceManager,
        analytics: Analytics,
        createOption: CreateRelationOption
    ): CreateOrEditOptionViewModelFactory = CreateOrEditOptionViewModelFactory(
        params = params,
        values = values,
        createOption = createOption,
        setObjectDetails = setObjectDetails,
        dispatcher = dispatcher,
        spaceManager = spaceManager,
        analytics = analytics,
    )
}
//endregion