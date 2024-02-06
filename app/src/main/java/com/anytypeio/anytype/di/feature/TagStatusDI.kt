package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_utils.di.scope.PerModal
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.debugging.Logger
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.`object`.UpdateDetail
import com.anytypeio.anytype.domain.relations.DeleteRelationOptions
import com.anytypeio.anytype.domain.search.SubscriptionEventChannel
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.editor.Editor
import com.anytypeio.anytype.presentation.relations.providers.ObjectRelationProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectValueProvider
import com.anytypeio.anytype.presentation.relations.value.tagstatus.SUB_MY_OPTIONS
import com.anytypeio.anytype.presentation.relations.value.tagstatus.TagOrStatusValueViewModel
import com.anytypeio.anytype.presentation.relations.value.tagstatus.TagOrStatusValueViewModelFactory
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.ui.relations.value.TagOrStatusValueFragment
import dagger.BindsInstance
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import javax.inject.Named

//region OBJECT (layout BASIC, TASK etc.)
@PerModal
@Subcomponent(
    modules = [TagOrStatusValueObjectModule::class]
)
interface TagOrStatusValueObjectComponent {

    @Subcomponent.Builder
    interface Builder {
        @BindsInstance
        fun params(params: TagOrStatusValueViewModel.ViewModelParams): Builder
        fun build(): TagOrStatusValueObjectComponent
    }

    fun inject(fragment: TagOrStatusValueFragment)
}

@Module
object TagOrStatusValueObjectModule {

    @JvmStatic
    @Provides
    @PerModal
    @Named(SUB_MY_OPTIONS)
    fun provideStoreLessSubscriptionContainer(
        repo: BlockRepository,
        channel: SubscriptionEventChannel,
        dispatchers: AppCoroutineDispatchers,
        logger: Logger
    ): StorelessSubscriptionContainer = StorelessSubscriptionContainer.Impl(
        repo = repo,
        channel = channel,
        dispatchers = dispatchers,
        logger = logger
    )

    @JvmStatic
    @Provides
    @PerModal
    fun provideDeleteRelationOptions(
        repo: BlockRepository,
        dispatchers: AppCoroutineDispatchers
    ): DeleteRelationOptions = DeleteRelationOptions(
        repo = repo,
        dispatchers = dispatchers
    )

    @JvmStatic
    @Provides
    @PerModal
    fun provideFactory(
        @Named(ObjectRelationProvider.INTRINSIC_PROVIDER_TYPE) relations: ObjectRelationProvider,
        @Named(ObjectRelationProvider.INTRINSIC_PROVIDER_TYPE) values: ObjectValueProvider,
        storage: Editor.Storage,
        setObjectDetails: UpdateDetail,
        dispatcher: Dispatcher<Payload>,
        analytics: Analytics,
        spaceManager: SpaceManager,
        params: TagOrStatusValueViewModel.ViewModelParams,
        @Named(SUB_MY_OPTIONS) subscription: StorelessSubscriptionContainer,
        deleteRelationOptions: DeleteRelationOptions
    ): TagOrStatusValueViewModelFactory = TagOrStatusValueViewModelFactory(
        params = params,
        values = values,
        storage = storage,
        relations = relations,
        setObjectDetails = setObjectDetails,
        dispatcher = dispatcher,
        analytics = analytics,
        spaceManager = spaceManager,
        subscription = subscription,
        deleteRelationOptions = deleteRelationOptions
    )
}
//endregion

//region SET or COLLECTION (layout SET, COLLECTION )
@PerModal
@Subcomponent(
    modules = [TagOrStatusValueSetModule::class]
)
interface TagOrStatusValueSetComponent {

    @Subcomponent.Builder
    interface Builder {
        @BindsInstance
        fun params(params: TagOrStatusValueViewModel.ViewModelParams): Builder
        fun build(): TagOrStatusValueSetComponent
    }

    fun inject(fragment: TagOrStatusValueFragment)
}

@Module
object TagOrStatusValueSetModule {

    @JvmStatic
    @Provides
    @PerModal
    @Named(SUB_MY_OPTIONS)
    fun provideStoreLessSubscriptionContainer(
        repo: BlockRepository,
        channel: SubscriptionEventChannel,
        dispatchers: AppCoroutineDispatchers,
        logger: Logger
    ): StorelessSubscriptionContainer = StorelessSubscriptionContainer.Impl(
        repo = repo,
        channel = channel,
        dispatchers = dispatchers,
        logger = logger
    )

    @JvmStatic
    @Provides
    @PerModal
    fun provideDeleteRelationOptions(
        repo: BlockRepository,
        dispatchers: AppCoroutineDispatchers
    ): DeleteRelationOptions = DeleteRelationOptions(
        repo = repo,
        dispatchers = dispatchers
    )

    @JvmStatic
    @Provides
    @PerModal
    fun provideFactory(
        @Named(ObjectRelationProvider.INTRINSIC_PROVIDER_TYPE) relations: ObjectRelationProvider,
        @Named(ObjectRelationProvider.INTRINSIC_PROVIDER_TYPE) values: ObjectValueProvider,
        storage: Editor.Storage,
        setObjectDetails: UpdateDetail,
        dispatcher: Dispatcher<Payload>,
        analytics: Analytics,
        spaceManager: SpaceManager,
        params: TagOrStatusValueViewModel.ViewModelParams,
        @Named(SUB_MY_OPTIONS) subscription: StorelessSubscriptionContainer,
        deleteRelationOptions: DeleteRelationOptions
    ): TagOrStatusValueViewModelFactory = TagOrStatusValueViewModelFactory(
        params = params,
        values = values,
        storage = storage,
        relations = relations,
        setObjectDetails = setObjectDetails,
        dispatcher = dispatcher,
        analytics = analytics,
        spaceManager = spaceManager,
        subscription = subscription,
        deleteRelationOptions = deleteRelationOptions
    )
}
//endregion

//region DATA VIEW
@PerModal
@Subcomponent(
    modules = [TagOrStatusValueDataViewModule::class]
)
interface TagOrStatusValueDataViewComponent {

    @Subcomponent.Builder
    interface Builder {
        @BindsInstance
        fun params(params: TagOrStatusValueViewModel.ViewModelParams): Builder
        fun build(): TagOrStatusValueDataViewComponent
    }

    fun inject(fragment: TagOrStatusValueFragment)
}

@Module
object TagOrStatusValueDataViewModule {

    @JvmStatic
    @Provides
    @PerModal
    @Named(SUB_MY_OPTIONS)
    fun provideStoreLessSubscriptionContainer(
        repo: BlockRepository,
        channel: SubscriptionEventChannel,
        dispatchers: AppCoroutineDispatchers,
        logger: Logger
    ): StorelessSubscriptionContainer = StorelessSubscriptionContainer.Impl(
        repo = repo,
        channel = channel,
        dispatchers = dispatchers,
        logger = logger
    )

    @JvmStatic
    @Provides
    @PerModal
    fun provideDeleteRelationOptions(
        repo: BlockRepository,
        dispatchers: AppCoroutineDispatchers
    ): DeleteRelationOptions = DeleteRelationOptions(
        repo = repo,
        dispatchers = dispatchers
    )

    @JvmStatic
    @Provides
    @PerModal
    fun provideFactory(
        @Named(ObjectRelationProvider.DATA_VIEW_PROVIDER_TYPE) relations: ObjectRelationProvider,
        @Named(ObjectRelationProvider.DATA_VIEW_PROVIDER_TYPE) values: ObjectValueProvider,
        storage: Editor.Storage,
        setObjectDetails: UpdateDetail,
        dispatcher: Dispatcher<Payload>,
        analytics: Analytics,
        spaceManager: SpaceManager,
        params: TagOrStatusValueViewModel.ViewModelParams,
        @Named(SUB_MY_OPTIONS) subscription: StorelessSubscriptionContainer,
        deleteRelationOptions: DeleteRelationOptions
    ): TagOrStatusValueViewModelFactory = TagOrStatusValueViewModelFactory(
        params = params,
        values = values,
        storage = storage,
        relations = relations,
        setObjectDetails = setObjectDetails,
        dispatcher = dispatcher,
        analytics = analytics,
        spaceManager = spaceManager,
        subscription = subscription,
        deleteRelationOptions = deleteRelationOptions
    )
}
//endregion