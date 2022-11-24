package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_utils.di.scope.PerModal
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.dataview.interactor.ModifyDataViewViewerRelationOrder
import com.anytypeio.anytype.domain.dataview.interactor.UpdateDataViewViewer
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.relations.DeleteRelationFromDataView
import com.anytypeio.anytype.presentation.relations.ObjectSetSettingsViewModel
import com.anytypeio.anytype.presentation.sets.ObjectSet
import com.anytypeio.anytype.presentation.sets.ObjectSetSession
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.ui.sets.modals.ObjectSetSettingsFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import kotlinx.coroutines.flow.StateFlow

@Subcomponent(modules = [ObjectSetSettingsModule::class])
@PerModal
interface ObjectSetSettingsSubComponent {
    @Subcomponent.Builder
    interface Builder {
        fun module(module: ObjectSetSettingsModule): Builder
        fun build(): ObjectSetSettingsSubComponent
    }

    fun inject(fragment: ObjectSetSettingsFragment)
}

@Module
object ObjectSetSettingsModule {

    @JvmStatic
    @Provides
    @PerModal
    fun provideViewModelFactory(
        state: StateFlow<ObjectSet>,
        session: ObjectSetSession,
        dispatcher: Dispatcher<Payload>,
        modifyViewerRelationOrder: ModifyDataViewViewerRelationOrder,
        updateDataViewViewer: UpdateDataViewViewer,
        deleteRelationFromDataView: DeleteRelationFromDataView,
        store: StoreOfRelations,
        analytics: Analytics
    ): ObjectSetSettingsViewModel.Factory = ObjectSetSettingsViewModel.Factory(
        state = state,
        session = session,
        dispatcher = dispatcher,
        modifyViewerRelationOrder = modifyViewerRelationOrder,
        updateDataViewViewer = updateDataViewViewer,
        deleteRelationFromDataView = deleteRelationFromDataView,
        analytics = analytics,
        store = store,
    )

    @JvmStatic
    @Provides
    @PerModal
    fun provideModifyViewerRelationOrderUseCase(
        repo: BlockRepository
    ): ModifyDataViewViewerRelationOrder = ModifyDataViewViewerRelationOrder(repo = repo)
}