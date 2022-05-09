package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_utils.di.scope.PerDialog
import com.anytypeio.anytype.domain.`object`.ObjectTypesProvider
import com.anytypeio.anytype.domain.`object`.UpdateDetail
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.dataview.interactor.AddDataViewRelationOption
import com.anytypeio.anytype.domain.dataview.interactor.AddStatusToDataViewRecord
import com.anytypeio.anytype.domain.dataview.interactor.AddTagToDataViewRecord
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.relations.AddObjectRelationOption
import com.anytypeio.anytype.presentation.relations.RelationOptionValueAddViewModel
import com.anytypeio.anytype.presentation.relations.RelationOptionValueDVAddViewModel
import com.anytypeio.anytype.presentation.relations.providers.ObjectDetailProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectRelationProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectValueProvider
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.ui.relations.RelationOptionValueAddFragment
import com.anytypeio.anytype.ui.relations.RelationOptionValueDVAddFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@Subcomponent(modules = [AddObjectRelationValueModule::class])
@PerDialog
interface AddObjectRelationValueSubComponent {
    @Subcomponent.Builder
    interface Builder {
        fun module(module: AddObjectRelationValueModule): Builder
        fun build(): AddObjectRelationValueSubComponent
    }

    fun inject(fragment: RelationOptionValueDVAddFragment)
    fun inject(fragment: RelationOptionValueAddFragment)
}

@Module
object AddObjectRelationValueModule {

    @JvmStatic
    @Provides
    @PerDialog
    fun provideViewModelFactoryForSets(
        relations: ObjectRelationProvider,
        values: ObjectValueProvider,
        details: ObjectDetailProvider,
        types: ObjectTypesProvider,
        dispatcher: Dispatcher<Payload>,
        addDataViewRelationOption: AddDataViewRelationOption,
        addTagToDataViewRecord: AddTagToDataViewRecord,
        addStatusToDataViewRecord: AddStatusToDataViewRecord,
        urlBuilder: UrlBuilder,
    ): RelationOptionValueDVAddViewModel.Factory = RelationOptionValueDVAddViewModel.Factory(
        relations = relations,
        values = values,
        details = details,
        types = types,
        urlBuilder = urlBuilder,
        dispatcher = dispatcher,
        addDataViewRelationOption = addDataViewRelationOption,
        addTagToDataViewRecord = addTagToDataViewRecord,
        addStatusToDataViewRecord = addStatusToDataViewRecord,
    )

    @JvmStatic
    @Provides
    @PerDialog
    fun provideViewModelFactoryForObjects(
        relations: ObjectRelationProvider,
        values: ObjectValueProvider,
        details: ObjectDetailProvider,
        types: ObjectTypesProvider,
        dispatcher: Dispatcher<Payload>,
        addObjectRelationOption: AddObjectRelationOption,
        updateDetail: UpdateDetail,
        urlBuilder: UrlBuilder,
        analytics: Analytics
    ): RelationOptionValueAddViewModel.Factory = RelationOptionValueAddViewModel.Factory(
        relations = relations,
        values = values,
        details = details,
        types = types,
        urlBuilder = urlBuilder,
        dispatcher = dispatcher,
        addObjectRelationOption = addObjectRelationOption,
        updateDetail = updateDetail,
        analytics = analytics
    )

    @JvmStatic
    @Provides
    @PerDialog
    fun provideAddObjectRelationOptionUseCase(
        repo: BlockRepository
    ): AddObjectRelationOption = AddObjectRelationOption(repo = repo)

    @JvmStatic
    @Provides
    @PerDialog
    fun provideAddStatusToDataViewRecordUseCase(
        repo: BlockRepository
    ): AddStatusToDataViewRecord = AddStatusToDataViewRecord(repo)
}