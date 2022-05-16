package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_utils.di.scope.PerDialog
import com.anytypeio.anytype.domain.`object`.UpdateDetail
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.dataview.interactor.AddDataViewRelationOption
import com.anytypeio.anytype.domain.dataview.interactor.AddStatusToDataViewRecord
import com.anytypeio.anytype.domain.dataview.interactor.AddTagToDataViewRecord
import com.anytypeio.anytype.domain.relations.AddObjectRelationOption
import com.anytypeio.anytype.presentation.relations.add.AddOptionsRelationDVViewModel
import com.anytypeio.anytype.presentation.relations.add.AddOptionsRelationProvider
import com.anytypeio.anytype.presentation.relations.add.AddOptionsRelationViewModel
import com.anytypeio.anytype.presentation.relations.providers.ObjectRelationProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectValueProvider
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.ui.relations.add.AddOptionsRelationDVFragment
import com.anytypeio.anytype.ui.relations.add.AddOptionsRelationFragment
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

    fun inject(fragment: AddOptionsRelationDVFragment)
    fun inject(fragment: AddOptionsRelationFragment)
}

@Module
object AddObjectRelationValueModule {

    @JvmStatic
    @Provides
    @PerDialog
    fun provideViewModelFactoryForSets(
        relations: ObjectRelationProvider,
        values: ObjectValueProvider,
        dispatcher: Dispatcher<Payload>,
        addDataViewRelationOption: AddDataViewRelationOption,
        addTagToDataViewRecord: AddTagToDataViewRecord,
        addStatusToDataViewRecord: AddStatusToDataViewRecord,
    ): AddOptionsRelationDVViewModel.Factory = AddOptionsRelationDVViewModel.Factory(
        relations = relations,
        values = values,
        dispatcher = dispatcher,
        addDataViewRelationOption = addDataViewRelationOption,
        addTagToDataViewRecord = addTagToDataViewRecord,
        addStatusToDataViewRecord = addStatusToDataViewRecord,
        optionsProvider = AddOptionsRelationProvider()
    )

    @JvmStatic
    @Provides
    @PerDialog
    fun provideViewModelFactoryForObjects(
        relations: ObjectRelationProvider,
        values: ObjectValueProvider,
        dispatcher: Dispatcher<Payload>,
        addObjectRelationOption: AddObjectRelationOption,
        updateDetail: UpdateDetail,
        analytics: Analytics
    ): AddOptionsRelationViewModel.Factory = AddOptionsRelationViewModel.Factory(
        relations = relations,
        values = values,
        dispatcher = dispatcher,
        addObjectRelationOption = addObjectRelationOption,
        updateDetail = updateDetail,
        analytics = analytics,
        optionsProvider = AddOptionsRelationProvider()
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