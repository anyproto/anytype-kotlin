package com.anytypeio.anytype.di.feature.sets

import com.anytypeio.anytype.core_utils.di.scope.PerModal
import com.anytypeio.anytype.presentation.sets.ObjectSet
import com.anytypeio.anytype.presentation.sets.ObjectSetSession
import com.anytypeio.anytype.presentation.sets.SelectFilterRelationViewModel
import com.anytypeio.anytype.ui.sets.modals.filter.SelectFilterRelationFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import kotlinx.coroutines.flow.StateFlow

@Subcomponent(modules = [SelectFilterRelationModule::class])
@PerModal
interface SelectFilterRelationSubComponent {
    @Subcomponent.Builder
    interface Builder {
        fun module(module: SelectFilterRelationModule): Builder
        fun build(): SelectFilterRelationSubComponent
    }

    fun inject(fragment: SelectFilterRelationFragment)
}

@Module
object SelectFilterRelationModule {
    @JvmStatic
    @Provides
    @PerModal
    fun provideSelectSortRelationViewModelFactory(
        state: StateFlow<ObjectSet>,
        session: ObjectSetSession
    ): SelectFilterRelationViewModel.Factory = SelectFilterRelationViewModel.Factory(
        state = state,
        session = session
    )
}