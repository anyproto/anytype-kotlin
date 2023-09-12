package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_utils.di.scope.PerModal
import com.anytypeio.anytype.domain.dataview.interactor.UpdateDataViewViewer
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.presentation.sets.SelectSortRelationViewModel
import com.anytypeio.anytype.presentation.sets.state.ObjectState
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.ui.sets.modals.sort.SelectSortRelationFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import kotlinx.coroutines.flow.MutableStateFlow

@Subcomponent(modules = [SelectSortRelationModule::class])
@PerModal
interface SelectSortRelationSubComponent {
    @Subcomponent.Builder
    interface Builder {
        fun module(module: SelectSortRelationModule): Builder
        fun build(): SelectSortRelationSubComponent
    }

    fun inject(fragment: SelectSortRelationFragment)
}

@Module
object SelectSortRelationModule {
    @JvmStatic
    @Provides
    @PerModal
    fun provideSelectSortRelationViewModelFactory(
        state: MutableStateFlow<ObjectState>,
        dispatcher: Dispatcher<Payload>,
        updateDataViewViewer: UpdateDataViewViewer,
        storeOfRelations: StoreOfRelations,
        analytics: Analytics
    ): SelectSortRelationViewModel.Factory = SelectSortRelationViewModel.Factory(
        objectState = state,
        dispatcher = dispatcher,
        updateDataViewViewer = updateDataViewViewer,
        storeOfRelations = storeOfRelations,
        analytics = analytics
    )
}