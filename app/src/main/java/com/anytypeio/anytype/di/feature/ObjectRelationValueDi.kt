package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_utils.di.scope.PerModal
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.`object`.UpdateDetail
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.relations.AddFileToObject
import com.anytypeio.anytype.presentation.relations.providers.ObjectDetailProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectRelationProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectRelationProvider.Companion.DATA_VIEW_PROVIDER_TYPE
import com.anytypeio.anytype.presentation.relations.providers.ObjectRelationProvider.Companion.INTRINSIC_PROVIDER_TYPE
import com.anytypeio.anytype.presentation.relations.providers.ObjectValueProvider
import com.anytypeio.anytype.presentation.sets.RelationValueDVViewModel
import com.anytypeio.anytype.presentation.sets.RelationValueViewModel
import com.anytypeio.anytype.presentation.util.CopyFileToCacheDirectory
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.ui.relations.RelationStatusValueFragment
import com.anytypeio.anytype.ui.relations.RelationValueDVFragment
import com.anytypeio.anytype.ui.relations.RelationValueFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import javax.inject.Named

@Subcomponent(modules = [DataViewRelationValueModule::class])
@PerModal
interface DataViewObjectRelationValueSubComponent {
    @Subcomponent.Builder
    interface Builder {
        fun module(module: DataViewRelationValueModule): Builder
        fun build(): DataViewObjectRelationValueSubComponent
    }

    fun inject(fragment: RelationValueDVFragment)

    fun addObjectRelationValueComponent(): AddObjectRelationValueSubComponent.Builder
    fun addObjectRelationObjectValueComponent(): AddObjectRelationSubComponent.Builder
    fun addRelationFileValueAddComponent() : AddFileRelationSubComponent.Builder

    fun addDataViewRelationOptionValueComponent(): AddDataViewRelationOptionValueSubComponent.Builder
    fun addDataViewRelationObjectValueComponent(): AddDataViewRelationObjectValueSubComponent.Builder
}

@Subcomponent(modules = [SetOrCollectionRelationValueModule::class])
@PerModal
interface SetOrCollectionRelationValueSubComponent {
    @Subcomponent.Builder
    interface Builder {
        fun module(module: SetOrCollectionRelationValueModule): Builder
        fun build(): SetOrCollectionRelationValueSubComponent
    }

    fun inject(fragment: RelationValueDVFragment)

    fun addObjectRelationValueComponent(): AddObjectRelationValueSubComponent.Builder
    fun addObjectRelationObjectValueComponent(): AddObjectRelationSubComponent.Builder
    fun addRelationFileValueAddComponent() : AddFileRelationSubComponent.Builder
}

@Subcomponent(modules = [ObjectObjectRelationValueModule::class])
@PerModal
interface ObjectObjectRelationValueSubComponent {
    @Subcomponent.Builder
    interface Builder {
        fun module(module: ObjectObjectRelationValueModule): Builder
        fun build(): ObjectObjectRelationValueSubComponent
    }

    fun inject(fragment: RelationValueFragment)
    fun inject(fragment: RelationStatusValueFragment)

    fun addObjectRelationValueComponent(): AddObjectRelationValueSubComponent.Builder
    fun addObjectRelationObjectValueComponent(): AddObjectRelationSubComponent.Builder
    fun addRelationFileValueAddComponent() : AddFileRelationSubComponent.Builder
}

@Module
object DataViewRelationValueModule {

    @JvmStatic
    @Provides
    @PerModal
    fun provideViewModelFactoryForDataView(
        @Named(DATA_VIEW_PROVIDER_TYPE) relations: ObjectRelationProvider,
        @Named(DATA_VIEW_PROVIDER_TYPE) values: ObjectValueProvider,
        details: ObjectDetailProvider,
        storeOfObjectTypes: StoreOfObjectTypes,
        urlBuilder: UrlBuilder,
        setObjectDetails: UpdateDetail,
        addFileToObject: AddFileToObject,
        copyFileToCacheDirectory: CopyFileToCacheDirectory,
        dispatcher: Dispatcher<Payload>,
        analytics: Analytics
    ): RelationValueDVViewModel.Factory = RelationValueDVViewModel.Factory(
        relations = relations,
        values = values,
        details = details,
        storeOfObjectTypes = storeOfObjectTypes,
        urlBuilder = urlBuilder,
        addFileToObject = addFileToObject,
        copyFileToCache = copyFileToCacheDirectory,
        setObjectDetails = setObjectDetails,
        dispatcher = dispatcher,
        analytics = analytics
    )
}

@Module
object SetOrCollectionRelationValueModule {

    @JvmStatic
    @Provides
    @PerModal
    fun provideViewModelFactoryForDataView(
        @Named(INTRINSIC_PROVIDER_TYPE) relations: ObjectRelationProvider,
        @Named(INTRINSIC_PROVIDER_TYPE)  values: ObjectValueProvider,
        details: ObjectDetailProvider,
        storeOfObjectTypes: StoreOfObjectTypes,
        urlBuilder: UrlBuilder,
        setObjectDetails: UpdateDetail,
        addFileToObject: AddFileToObject,
        copyFileToCacheDirectory: CopyFileToCacheDirectory,
        dispatcher: Dispatcher<Payload>,
        analytics: Analytics
    ): RelationValueDVViewModel.Factory = RelationValueDVViewModel.Factory(
        relations = relations,
        values = values,
        details = details,
        storeOfObjectTypes = storeOfObjectTypes,
        urlBuilder = urlBuilder,
        addFileToObject = addFileToObject,
        copyFileToCache = copyFileToCacheDirectory,
        setObjectDetails = setObjectDetails,
        dispatcher = dispatcher,
        analytics = analytics
    )
}

@Module
object ObjectObjectRelationValueModule {

    @JvmStatic
    @Provides
    @PerModal
    fun provideViewModelFactoryForObject(
        @Named(INTRINSIC_PROVIDER_TYPE) relations: ObjectRelationProvider,
        @Named(INTRINSIC_PROVIDER_TYPE) values: ObjectValueProvider,
        details: ObjectDetailProvider,
        storeOfObjectTypes: StoreOfObjectTypes,
        urlBuilder: UrlBuilder,
        dispatcher: Dispatcher<Payload>,
        updateDetail: UpdateDetail,
        addFileToObject: AddFileToObject,
        copyFileToCacheDirectory: CopyFileToCacheDirectory,
        analytics: Analytics
    ): RelationValueViewModel.Factory = RelationValueViewModel.Factory(
        relations = relations,
        values = values,
        details = details,
        storeOfObjectTypes = storeOfObjectTypes,
        urlBuilder = urlBuilder,
        dispatcher = dispatcher,
        updateDetail = updateDetail,
        addFileToObject = addFileToObject,
        copyFileToCache = copyFileToCacheDirectory,
        analytics = analytics
    )
}