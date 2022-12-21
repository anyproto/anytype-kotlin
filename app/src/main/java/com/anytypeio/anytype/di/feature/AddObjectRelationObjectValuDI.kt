package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.core_utils.di.scope.PerDialog
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.domain.workspace.WorkspaceManager
import com.anytypeio.anytype.presentation.relations.add.AddObjectRelationViewModel
import com.anytypeio.anytype.presentation.relations.providers.ObjectRelationProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectValueProvider
import com.anytypeio.anytype.ui.relations.add.AddObjectRelationFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@Subcomponent(modules = [AddObjectRelationModule::class])
@PerDialog
interface AddObjectRelationSubComponent {

    @Subcomponent.Builder
    interface Builder {
        fun module(module: AddObjectRelationModule): Builder
        fun build(): AddObjectRelationSubComponent
    }

    fun inject(fragment: AddObjectRelationFragment)
}

@Module
object AddObjectRelationModule {

    @JvmStatic
    @Provides
    @PerDialog
    fun provideViewModelFactory(
        relations: ObjectRelationProvider,
        values: ObjectValueProvider,
        storeOfObjectTypes: StoreOfObjectTypes,
        searchObjects: SearchObjects,
        urlBuilder: UrlBuilder,
        workspaceManager: WorkspaceManager
    ): AddObjectRelationViewModel.Factory =
        AddObjectRelationViewModel.Factory(
            relations, values, searchObjects, urlBuilder, storeOfObjectTypes, workspaceManager
        )
}