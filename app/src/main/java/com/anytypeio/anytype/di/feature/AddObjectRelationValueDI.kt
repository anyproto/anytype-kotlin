package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_utils.di.scope.PerDialog
import com.anytypeio.anytype.domain.`object`.UpdateDetail
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.objects.options.GetOptions
import com.anytypeio.anytype.domain.relations.CreateRelationOption
import com.anytypeio.anytype.presentation.relations.add.AddOptionsRelationDVViewModel
import com.anytypeio.anytype.presentation.relations.add.AddOptionsRelationProvider
import com.anytypeio.anytype.presentation.relations.add.AddOptionsRelationViewModel
import com.anytypeio.anytype.presentation.relations.providers.ObjectDetailProvider
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
        createRelationOption: CreateRelationOption,
        analytics: Analytics,
        setObjectDetail: UpdateDetail,
        detailsProvider: ObjectDetailProvider,
        getOptions: GetOptions
    ): AddOptionsRelationDVViewModel.Factory = AddOptionsRelationDVViewModel.Factory(
        relations = relations,
        values = values,
        dispatcher = dispatcher,
        createRelationOption = createRelationOption,
        optionsProvider = AddOptionsRelationProvider(),
        analytics = analytics,
        setObjectDetail = setObjectDetail,
        detailsProvider = detailsProvider,
        getOptions = getOptions
    )

    @JvmStatic
    @Provides
    @PerDialog
    fun provideViewModelFactoryForObjects(
        relations: ObjectRelationProvider,
        values: ObjectValueProvider,
        dispatcher: Dispatcher<Payload>,
        createRelationOption: CreateRelationOption,
        updateDetail: UpdateDetail,
        analytics: Analytics,
        detailsProvider: ObjectDetailProvider,
        getOptions: GetOptions
    ): AddOptionsRelationViewModel.Factory = AddOptionsRelationViewModel.Factory(
        relations = relations,
        values = values,
        dispatcher = dispatcher,
        createRelationOption = createRelationOption,
        updateDetail = updateDetail,
        analytics = analytics,
        optionsProvider = AddOptionsRelationProvider(),
        detailProvider = detailsProvider,
        getOptions = getOptions
    )

    @JvmStatic
    @Provides
    @PerDialog
    fun createRelationOption(
        repo: BlockRepository
    ): CreateRelationOption = CreateRelationOption(repo = repo)

    @JvmStatic
    @Provides
    @PerDialog
    fun getOptions(repo: BlockRepository) = GetOptions(repo)
}