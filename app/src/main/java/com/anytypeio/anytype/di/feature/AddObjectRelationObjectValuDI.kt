package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.core_utils.di.scope.PerDialog
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.dataview.interactor.SearchObjects
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.presentation.relations.AddObjectRelationObjectValueViewModel
import com.anytypeio.anytype.presentation.relations.providers.ObjectRelationProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectValueProvider
import com.anytypeio.anytype.ui.relations.AddObjectRelationObjectValueFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@Subcomponent(modules = [AddObjectRelationObjectValueModule::class])
@PerDialog
interface AddObjectRelationObjectValueSubComponent {

    @Subcomponent.Builder
    interface Builder {
        fun module(module: AddObjectRelationObjectValueModule): Builder
        fun build(): AddObjectRelationObjectValueSubComponent
    }

    fun inject(fragment: AddObjectRelationObjectValueFragment)
}

@Module
object AddObjectRelationObjectValueModule {

    @JvmStatic
    @Provides
    @PerDialog
    fun provideViewModelFactory(
        relations: ObjectRelationProvider,
        values: ObjectValueProvider,
        searchObjects: SearchObjects,
        urlBuilder: UrlBuilder
    ): AddObjectRelationObjectValueViewModel.Factory =
        AddObjectRelationObjectValueViewModel.Factory(
            relations, values, searchObjects, urlBuilder
        )

    @JvmStatic
    @Provides
    @PerDialog
    fun provideSearchObjectsUseCase(
        repo: BlockRepository
    ): SearchObjects = SearchObjects(repo = repo)
}