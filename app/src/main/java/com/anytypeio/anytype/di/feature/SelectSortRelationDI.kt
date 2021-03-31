package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_utils.di.scope.PerModal
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.dataview.interactor.AddDataViewViewerSort
import com.anytypeio.anytype.presentation.sets.ObjectSet
import com.anytypeio.anytype.presentation.sets.ObjectSetSession
import com.anytypeio.anytype.presentation.sets.SelectSortRelationViewModel
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.ui.sets.modals.sort.SelectSortRelationFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import kotlinx.coroutines.flow.StateFlow

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
        state: StateFlow<ObjectSet>,
        session: ObjectSetSession,
        dispatcher: Dispatcher<Payload>,
        addDataViewViewerSort: AddDataViewViewerSort
    ): SelectSortRelationViewModel.Factory = SelectSortRelationViewModel.Factory(
        state = state,
        session = session,
        dispatcher = dispatcher,
        addDataViewViewerSort = addDataViewViewerSort
    )

    @JvmStatic
    @Provides
    @PerModal
    fun provideAddDataViewViewerSort(
        repo: BlockRepository
    ): AddDataViewViewerSort = AddDataViewViewerSort(repo = repo)
}