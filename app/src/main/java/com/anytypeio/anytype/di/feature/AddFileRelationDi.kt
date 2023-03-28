package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.core_utils.di.scope.PerDialog
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.presentation.relations.add.AddFileRelationViewModel
import com.anytypeio.anytype.presentation.relations.providers.ObjectRelationProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectRelationProvider.Companion.INTRINSIC_PROVIDER_TYPE
import com.anytypeio.anytype.presentation.relations.providers.ObjectValueProvider
import com.anytypeio.anytype.ui.relations.add.AddFileRelationFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import javax.inject.Named

@Subcomponent(modules = [AddFileRelationModule::class])
@PerDialog
interface AddFileRelationSubComponent {

    @Subcomponent.Builder
    interface Builder {
        fun module(module: AddFileRelationModule): Builder
        fun build(): AddFileRelationSubComponent
    }

    fun inject(fragment: AddFileRelationFragment)
}

@Module
object AddFileRelationModule {

    @JvmStatic
    @Provides
    @PerDialog
    fun provideViewModelFactory(
        @Named(INTRINSIC_PROVIDER_TYPE) relations: ObjectRelationProvider,
        values: ObjectValueProvider,
        searchObjects: SearchObjects,
        urlBuilder: UrlBuilder
    ): AddFileRelationViewModel.Factory = AddFileRelationViewModel.Factory(
            relations, values, searchObjects, urlBuilder
    )
}