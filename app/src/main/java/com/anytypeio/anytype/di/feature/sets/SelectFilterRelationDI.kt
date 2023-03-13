package com.anytypeio.anytype.di.feature.sets

import com.anytypeio.anytype.core_utils.di.scope.PerModal
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.presentation.sets.ObjectSetSession
import com.anytypeio.anytype.presentation.sets.SelectFilterRelationViewModel
import com.anytypeio.anytype.presentation.sets.state.ObjectState
import com.anytypeio.anytype.ui.sets.modals.filter.SelectFilterRelationFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import kotlinx.coroutines.flow.MutableStateFlow

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
        state: MutableStateFlow<ObjectState>,
        session: ObjectSetSession,
        storeOfRelations: StoreOfRelations
    ): SelectFilterRelationViewModel.Factory = SelectFilterRelationViewModel.Factory(
        objectState = state,
        session = session,
        storeOfRelations = storeOfRelations
    )
}