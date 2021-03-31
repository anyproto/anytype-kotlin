package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.core_utils.di.scope.PerDialog
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.dataview.interactor.SearchObjects
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.presentation.relations.RelationFileValueAddViewModel
import com.anytypeio.anytype.presentation.relations.providers.ObjectRelationProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectValueProvider
import com.anytypeio.anytype.ui.relations.RelationFileValueAddFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@Subcomponent(modules = [RelationFileValueAddModule::class])
@PerDialog
interface RelationFileValueAddSubComponent {

    @Subcomponent.Builder
    interface Builder {
        fun module(module: RelationFileValueAddModule): Builder
        fun build(): RelationFileValueAddSubComponent
    }

    fun inject(fragment: RelationFileValueAddFragment)
}

@Module
object RelationFileValueAddModule {

    @JvmStatic
    @Provides
    @PerDialog
    fun provideViewModelFactory(
        relations: ObjectRelationProvider,
        values: ObjectValueProvider,
        searchObjects: SearchObjects,
        urlBuilder: UrlBuilder
    ): RelationFileValueAddViewModel.Factory =
        RelationFileValueAddViewModel.Factory(
            relations, values, searchObjects, urlBuilder
        )

    @JvmStatic
    @Provides
    @PerDialog
    fun provideSearchObjectsUseCase(
        repo: BlockRepository
    ): SearchObjects = SearchObjects(repo = repo)
}