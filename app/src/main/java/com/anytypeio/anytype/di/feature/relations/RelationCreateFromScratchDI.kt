package com.anytypeio.anytype.di.feature.relations

import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_utils.di.scope.PerDialog
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.relations.AddNewRelationToObject
import com.anytypeio.anytype.presentation.relations.RelationCreateFromScratchForObjectViewModel
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.ui.relations.RelationCreateFromScratchFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@Subcomponent(modules = [RelationCreateFromScratchModule::class])
@PerDialog
interface RelationCreateFromScratchSubComponent {
    @Subcomponent.Builder
    interface Builder {
        fun module(module: RelationCreateFromScratchModule): Builder
        fun build(): RelationCreateFromScratchSubComponent
    }

    fun inject(fragment: RelationCreateFromScratchFragment)
}

@Module
object RelationCreateFromScratchModule {
    @JvmStatic
    @Provides
    @PerDialog
    fun provideViewModelFactory(
        addNewRelationToObject: AddNewRelationToObject,
        dispatcher: Dispatcher<Payload>
    ): RelationCreateFromScratchForObjectViewModel.Factory =
        RelationCreateFromScratchForObjectViewModel.Factory(
            addNewRelationToObject = addNewRelationToObject,
            dispatcher = dispatcher
        )

    @JvmStatic
    @Provides
    @PerDialog
    fun provideAddNewRelationToObjectUseCase(
        repo: BlockRepository
    ): AddNewRelationToObject = AddNewRelationToObject(repo)
}