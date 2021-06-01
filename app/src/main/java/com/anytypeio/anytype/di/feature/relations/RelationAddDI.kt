package com.anytypeio.anytype.di.feature.relations

import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_utils.di.scope.PerDialog
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.dataview.interactor.ObjectRelationList
import com.anytypeio.anytype.domain.relations.AddRelationToObject
import com.anytypeio.anytype.presentation.relations.RelationAddToObjectViewModel
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.ui.relations.RelationAddFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@Subcomponent(modules = [RelationAddModule::class])
@PerDialog
interface RelationAddSubComponent {

    @Subcomponent.Builder
    interface Builder {
        fun module(module: RelationAddModule): Builder
        fun build(): RelationAddSubComponent
    }

    fun inject(fragment: RelationAddFragment)
}

@Module
object RelationAddModule {
    @JvmStatic
    @Provides
    @PerDialog
    fun provideViewModelFactory(
        addRelationToObject: AddRelationToObject,
        objectRelationList: ObjectRelationList,
        dispatcher: Dispatcher<Payload>
    ): RelationAddToObjectViewModel.Factory = RelationAddToObjectViewModel.Factory(
        objectRelationList = objectRelationList,
        addRelationToObject = addRelationToObject,
        dispatcher = dispatcher
    )

    @JvmStatic
    @Provides
    @PerDialog
    fun provideObjectRelationListUseCase(
        repo: BlockRepository
    ): ObjectRelationList = ObjectRelationList(repo)

    @JvmStatic
    @Provides
    @PerDialog
    fun provideAddRelationToObjectUseCase(
        repo: BlockRepository
    ): AddRelationToObject = AddRelationToObject(repo)
}