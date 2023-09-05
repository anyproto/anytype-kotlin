package com.anytypeio.anytype.di.feature;

import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_utils.di.scope.PerModal
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.dataview.interactor.DeleteDataViewViewer
import com.anytypeio.anytype.domain.dataview.interactor.SetDataViewViewerPosition
import com.anytypeio.anytype.presentation.sets.ManageViewerViewModel
import com.anytypeio.anytype.presentation.sets.ObjectSetSession
import com.anytypeio.anytype.presentation.sets.state.ObjectState
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.ui.sets.modals.ManageViewerFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import kotlinx.coroutines.flow.MutableStateFlow

@Subcomponent(modules = [ManageViewerModule::class])
@PerModal
interface ManageViewerSubComponent {
    @Subcomponent.Builder
    interface Builder {
        fun module(module: ManageViewerModule): Builder
        fun build(): ManageViewerSubComponent
    }

    fun inject(fragment: ManageViewerFragment)
}

@Module
object ManageViewerModule {

    @JvmStatic
    @Provides
    @PerModal
    fun provideManageViewerViewModelFactory(
        state: MutableStateFlow<ObjectState>,
        session: ObjectSetSession,
        dispatcher: Dispatcher<Payload>,
        analytics: Analytics,
        deleteDataViewViewer: DeleteDataViewViewer,
        setDataViewViewerPosition: SetDataViewViewerPosition
    ): ManageViewerViewModel.Factory = ManageViewerViewModel.Factory(
        objectState = state,
        session = session,
        dispatcher = dispatcher,
        analytics = analytics,
        deleteDataViewViewer = deleteDataViewViewer,
        setDataViewViewerPosition = setDataViewViewerPosition
    )

    @JvmStatic
    @Provides
    @PerModal
    fun provideDeleteDataViewViewerUseCase(
        repo: BlockRepository
    ): DeleteDataViewViewer = DeleteDataViewViewer(repo = repo)

    @JvmStatic
    @Provides
    @PerModal
    fun provideSetDataViewViewerPosition(
        repo: BlockRepository,
        dispatchers: AppCoroutineDispatchers
    ): SetDataViewViewerPosition = SetDataViewViewerPosition(
        repo = repo,
        dispatchers = dispatchers
    )
}