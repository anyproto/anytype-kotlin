package com.anytypeio.anytype.di.feature;

import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_utils.di.scope.PerModal
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.dataview.interactor.RenameDataViewViewer
import com.anytypeio.anytype.presentation.sets.EditDataViewViewerViewModel
import com.anytypeio.anytype.presentation.sets.ObjectSet
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.ui.sets.modals.EditDataViewViewerFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import kotlinx.coroutines.flow.StateFlow

@Subcomponent(modules = [EditDataViewViewerModule::class])
@PerModal
interface EditDataViewViewerSubComponent {
    @Subcomponent.Builder
    interface Builder {
        fun module(module: EditDataViewViewerModule): Builder
        fun build(): EditDataViewViewerSubComponent
    }

    fun inject(fragment: EditDataViewViewerFragment)
}

@Module
object EditDataViewViewerModule {

    @JvmStatic
    @Provides
    @PerModal
    fun provideEditDataViewViewerViewModelFactory(
        renameDataViewViewer: RenameDataViewViewer,
        dispatcher: Dispatcher<Payload>,
        objectSetState: StateFlow<ObjectSet>
    ): EditDataViewViewerViewModel.Factory = EditDataViewViewerViewModel.Factory(
        renameDataViewViewer = renameDataViewViewer,
        dispatcher = dispatcher,
        objectSetState = objectSetState
    )

    @JvmStatic
    @Provides
    @PerModal
    fun provideRenameDataViewViewerUseCase(
        repo: BlockRepository
    ): RenameDataViewViewer = RenameDataViewViewer(repo = repo)
}