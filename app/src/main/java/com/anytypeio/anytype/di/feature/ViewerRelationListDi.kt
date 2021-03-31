package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_utils.di.scope.PerModal
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.dataview.interactor.ModifyDataViewViewerRelationOrder
import com.anytypeio.anytype.domain.dataview.interactor.UpdateDataViewViewer
import com.anytypeio.anytype.presentation.relations.ViewerRelationsViewModel
import com.anytypeio.anytype.presentation.sets.ObjectSet
import com.anytypeio.anytype.presentation.sets.ObjectSetSession
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.ui.sets.modals.ViewerRelationsFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import kotlinx.coroutines.flow.StateFlow

@Subcomponent(modules = [ViewerRelationsModule::class])
@PerModal
interface ViewerRelationsSubComponent {
    @Subcomponent.Builder
    interface Builder {
        fun module(module: ViewerRelationsModule): Builder
        fun build(): ViewerRelationsSubComponent
    }

    fun inject(fragment: ViewerRelationsFragment)
}

@Module
object ViewerRelationsModule {

    @JvmStatic
    @Provides
    @PerModal
    fun provideViewerRelationsListViewModelFactory(
        state: StateFlow<ObjectSet>,
        session: ObjectSetSession,
        dispatcher: Dispatcher<Payload>,
        modifyViewerRelationOrder: ModifyDataViewViewerRelationOrder,
        updateDataViewViewer: UpdateDataViewViewer
    ): ViewerRelationsViewModel.Factory = ViewerRelationsViewModel.Factory(
        state = state,
        session = session,
        dispatcher = dispatcher,
        modifyViewerRelationOrder = modifyViewerRelationOrder,
        updateDataViewViewer = updateDataViewViewer
    )

    @JvmStatic
    @Provides
    @PerModal
    fun provideModifyViewerRelationOrderUseCase(
        repo: BlockRepository
    ): ModifyDataViewViewerRelationOrder = ModifyDataViewViewerRelationOrder(repo = repo)
}