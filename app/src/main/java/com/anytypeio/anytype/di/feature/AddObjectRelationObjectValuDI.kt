package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.core_utils.di.scope.PerDialog
import com.anytypeio.anytype.domain.`object`.ObjectTypesProvider
import com.anytypeio.anytype.domain.dataview.interactor.SearchObjects
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.presentation.relations.RelationObjectValueAddViewModel
import com.anytypeio.anytype.presentation.relations.providers.ObjectRelationProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectValueProvider
import com.anytypeio.anytype.ui.relations.RelationObjectValueAddFragment
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

    fun inject(fragment: RelationObjectValueAddFragment)
}

@Module
object AddObjectRelationObjectValueModule {

    @JvmStatic
    @Provides
    @PerDialog
    fun provideViewModelFactory(
        relations: ObjectRelationProvider,
        values: ObjectValueProvider,
        objectTypesProvider: ObjectTypesProvider,
        searchObjects: SearchObjects,
        urlBuilder: UrlBuilder
    ): RelationObjectValueAddViewModel.Factory =
        RelationObjectValueAddViewModel.Factory(
            relations, values, searchObjects, urlBuilder, objectTypesProvider
        )
}