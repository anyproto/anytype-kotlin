package com.anytypeio.anytype.di.feature;

import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_utils.di.scope.PerModal
import com.anytypeio.anytype.domain.dataview.interactor.UpdateDataViewViewer
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.presentation.sets.sort.ViewerSortViewModel
import com.anytypeio.anytype.presentation.sets.state.ObjectState
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.ui.sets.modals.sort.ViewerSortFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import kotlinx.coroutines.flow.MutableStateFlow

@Subcomponent(modules = [ViewerSortModule::class])
@PerModal
interface ViewerSortSubComponent {
    @Subcomponent.Builder
    interface Builder {
        fun module(module: ViewerSortModule): Builder
        fun build(): ViewerSortSubComponent
    }

    fun inject(fragment: ViewerSortFragment)
}

@Module
object ViewerSortModule {
    @JvmStatic
    @Provides
    @PerModal
    fun provideViewModelFactory(
        state: MutableStateFlow<ObjectState>,
        updateDataViewViewer: UpdateDataViewViewer,
        dispatcher: Dispatcher<Payload>,
        analytics: Analytics,
        storeOfRelations: StoreOfRelations
    ): ViewerSortViewModel.Factory = ViewerSortViewModel.Factory(
        state = state,
        updateDataViewViewer = updateDataViewViewer,
        dispatcher = dispatcher,
        analytics = analytics,
        storeOfRelations = storeOfRelations
    )
}
