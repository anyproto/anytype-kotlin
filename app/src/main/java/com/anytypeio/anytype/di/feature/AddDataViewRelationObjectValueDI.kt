package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.core_utils.di.scope.PerDialog
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.relations.add.AddObjectRelationViewModel
import com.anytypeio.anytype.presentation.relations.providers.ObjectRelationProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectValueProvider
import com.anytypeio.anytype.ui.relations.add.AddObjectRelationFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import javax.inject.Named

@Subcomponent(modules = [AddDataViewRelationObjectValueModule::class])
@PerDialog
interface AddDataViewRelationObjectValueSubComponent {

    @Subcomponent.Builder
    interface Builder {
        fun module(module: AddDataViewRelationObjectValueModule): Builder
        fun build(): AddDataViewRelationObjectValueSubComponent
    }

    fun inject(fragment: AddObjectRelationFragment)
}

@Module
object AddDataViewRelationObjectValueModule {

    @JvmStatic
    @Provides
    @PerDialog
    fun provideViewModelFactory(
        @Named(ObjectRelationProvider.DATA_VIEW_PROVIDER_TYPE) relations: ObjectRelationProvider,
        @Named(ObjectRelationProvider.DATA_VIEW_PROVIDER_TYPE) values: ObjectValueProvider,
        storeOfObjectTypes: StoreOfObjectTypes,
        searchObjects: SearchObjects,
        urlBuilder: UrlBuilder,
        spaceManager: SpaceManager
    ): AddObjectRelationViewModel.Factory = AddObjectRelationViewModel.Factory(
        relations = relations,
        values = values,
        searchObjects = searchObjects,
        urlBuilder = urlBuilder,
        storeOfObjectTypes = storeOfObjectTypes,
        spaceManager = spaceManager
    )
}

