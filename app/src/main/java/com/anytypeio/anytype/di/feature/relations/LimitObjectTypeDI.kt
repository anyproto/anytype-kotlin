package com.anytypeio.anytype.di.feature.relations

import com.anytypeio.anytype.core_utils.di.scope.PerDialog
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.presentation.relations.LimitObjectTypeViewModel
import com.anytypeio.anytype.presentation.relations.model.CreateFromScratchState
import com.anytypeio.anytype.presentation.relations.model.StateHolder
import com.anytypeio.anytype.ui.relations.LimitObjectTypeFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@Subcomponent(modules = [LimitObjectTypeModule::class])
@PerDialog
interface LimitObjectTypeSubComponent {
    @Subcomponent.Builder
    interface Builder {
        fun module(module: LimitObjectTypeModule): Builder
        fun build(): LimitObjectTypeSubComponent
    }

    fun inject(fragment: LimitObjectTypeFragment)
}

@Module
object LimitObjectTypeModule {

    @JvmStatic
    @Provides
    @PerDialog
    fun provideViewModelFactory(
        searchObjects: SearchObjects,
        urlBuilder: UrlBuilder,
        state: StateHolder<CreateFromScratchState>
    ): LimitObjectTypeViewModel.Factory = LimitObjectTypeViewModel.Factory(
        searchObjects = searchObjects,
        urlBuilder = urlBuilder,
        state = state
    )
}