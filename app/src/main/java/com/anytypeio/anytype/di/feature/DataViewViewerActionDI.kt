package com.anytypeio.anytype.di.feature;

import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_utils.di.scope.PerModal
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.dataview.interactor.DeleteDataViewViewer
import com.anytypeio.anytype.domain.dataview.interactor.DuplicateDataViewViewer
import com.anytypeio.anytype.domain.dataview.interactor.RenameDataViewViewer
import com.anytypeio.anytype.presentation.sets.DataViewViewerActionViewModel
import com.anytypeio.anytype.presentation.sets.ObjectSet
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.ui.sets.modals.DataViewViewerActionFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import kotlinx.coroutines.flow.StateFlow

@Subcomponent(modules = [DataViewViewerActionModule::class])
@PerModal
interface DataViewViewerActionSubComponent {
    @Subcomponent.Builder
    interface Builder {
        fun module(module: DataViewViewerActionModule): Builder
        fun build(): DataViewViewerActionSubComponent
    }

    fun inject(fragment: DataViewViewerActionFragment)
}

@Module
object DataViewViewerActionModule {

    @JvmStatic
    @Provides
    @PerModal
    fun provideEditDataViewViewerViewModelFactory(
        duplicateDataViewViewer: DuplicateDataViewViewer,
        deleteDataViewViewer: DeleteDataViewViewer,
        dispatcher: Dispatcher<Payload>,
        objectSetState: StateFlow<ObjectSet>
    ): DataViewViewerActionViewModel.Factory = DataViewViewerActionViewModel.Factory(
        duplicateDataViewViewer = duplicateDataViewViewer,
        deleteDataViewViewer = deleteDataViewViewer,
        dispatcher = dispatcher,
        objectSetState = objectSetState
    )

    @JvmStatic
    @Provides
    @PerModal
    fun provideRenameDataViewViewerUseCase(
        repo: BlockRepository
    ): RenameDataViewViewer = RenameDataViewViewer(repo = repo)

    @JvmStatic
    @Provides
    @PerModal
    fun provideDuplicateDataViewViewerUseCase(
        repo: BlockRepository
    ): DuplicateDataViewViewer = DuplicateDataViewViewer(repo = repo)

    @JvmStatic
    @Provides
    @PerModal
    fun provideDeleteDataViewViewerUseCase(
        repo: BlockRepository
    ): DeleteDataViewViewer = DeleteDataViewViewer(repo = repo)
}