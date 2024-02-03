package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_utils.di.scope.PerModal
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.debugging.Logger
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.`object`.UpdateDetail
import com.anytypeio.anytype.domain.search.SubscriptionEventChannel
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.editor.Editor
import com.anytypeio.anytype.presentation.relations.providers.ObjectRelationProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectValueProvider
import com.anytypeio.anytype.presentation.relations.value.tagstatus.SUB_MY_OPTIONS
import com.anytypeio.anytype.presentation.relations.value.tagstatus.TagStatusViewModel
import com.anytypeio.anytype.presentation.relations.value.tagstatus.TagStatusViewModelFactory
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.ui.relations.value.TagStatusFragment
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
        fun params(params: TagStatusViewModel.ViewModelParams): Builder
        fun build(): TagStatusObjectComponent
    }

    fun inject(fragment: TagStatusFragment)
}

@Module
object TagStatusObjectModule {

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
    fun provideFactory(
        @Named(ObjectRelationProvider.INTRINSIC_PROVIDER_TYPE) relations: ObjectRelationProvider,
        @Named(ObjectRelationProvider.INTRINSIC_PROVIDER_TYPE) values: ObjectValueProvider,
        storage: Editor.Storage,
        setObjectDetails: UpdateDetail,
        dispatcher: Dispatcher<Payload>,
        analytics: Analytics,
        spaceManager: SpaceManager,
        params: TagStatusViewModel.ViewModelParams,
        @Named(SUB_MY_OPTIONS) subscription: StorelessSubscriptionContainer
    ): TagStatusViewModelFactory = TagStatusViewModelFactory(
        params = params,
        values = values,
        storage = storage,
        relations = relations,
        setObjectDetails = setObjectDetails,
        dispatcher = dispatcher,
        analytics = analytics,
        spaceManager = spaceManager,
        subscription = subscription
    )
}