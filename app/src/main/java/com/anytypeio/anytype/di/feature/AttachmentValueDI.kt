package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_utils.di.scope.PerModal
import com.anytypeio.anytype.domain.`object`.UpdateDetail
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.relations.providers.ObjectRelationProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectValueProvider
import com.anytypeio.anytype.presentation.relations.value.attachment.AttachmentValueViewModel
import com.anytypeio.anytype.presentation.relations.value.attachment.AttachmentValueViewModelFactory
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.ui.relations.value.AttachmentValueFragment
import dagger.BindsInstance
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import javax.inject.Named

//region OBJECT (layout BASIC, TASK etc.)
@PerModal
@Subcomponent(
    modules = [AttachmentValueObjectModule::class]
)
interface AttachmentValueObjectComponent {

    @Subcomponent.Builder
    interface Builder {
        @BindsInstance
        fun params(params: AttachmentValueViewModel.ViewModelParams): Builder
        fun build(): AttachmentValueObjectComponent
    }

    fun inject(fragment: AttachmentValueFragment)
}

@Module
object AttachmentValueObjectModule {

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
        params: AttachmentValueViewModel.ViewModelParams
    ): AttachmentValueViewModelFactory = AttachmentValueViewModelFactory(
        params = params,
        values = values,
        relations = relations,
        setObjectDetails = setObjectDetails,
        dispatcher = dispatcher,
        analytics = analytics,
        spaceManager = spaceManager
    )
}
//endregion

//region SET or COLLECTION (layout SET, COLLECTION )
@PerModal
@Subcomponent(
    modules = [AttachmentValueSetModule::class]
)
interface AttachmentValueSetComponent {

    @Subcomponent.Builder
    interface Builder {
        @BindsInstance
        fun params(params: AttachmentValueViewModel.ViewModelParams): Builder
        fun build(): AttachmentValueSetComponent
    }

    fun inject(fragment: AttachmentValueFragment)
}

@Module
object AttachmentValueSetModule {

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
        params: AttachmentValueViewModel.ViewModelParams
    ): AttachmentValueViewModelFactory = AttachmentValueViewModelFactory(
        params = params,
        values = values,
        relations = relations,
        setObjectDetails = setObjectDetails,
        dispatcher = dispatcher,
        analytics = analytics,
        spaceManager = spaceManager
    )
}
//endregion

//region DATA VIEW
@PerModal
@Subcomponent(
    modules = [AttachmentValueDataViewModule::class]
)
interface AttachmentValueDataViewComponent {

    @Subcomponent.Builder
    interface Builder {
        @BindsInstance
        fun params(params: AttachmentValueViewModel.ViewModelParams): Builder
        fun build(): AttachmentValueDataViewComponent
    }

    fun inject(fragment: AttachmentValueFragment)
}

@Module
object AttachmentValueDataViewModule {

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
        params: AttachmentValueViewModel.ViewModelParams
    ): AttachmentValueViewModelFactory = AttachmentValueViewModelFactory(
        params = params,
        values = values,
        relations = relations,
        setObjectDetails = setObjectDetails,
        dispatcher = dispatcher,
        analytics = analytics,
        spaceManager = spaceManager
    )
}
//endregion