package com.anytypeio.anytype.di.feature;

import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_utils.di.scope.PerModal
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.dataview.interactor.AddDataViewViewer
import com.anytypeio.anytype.presentation.sets.CreateDataViewViewerViewModel
import com.anytypeio.anytype.presentation.sets.state.ObjectState
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.ui.sets.modals.CreateDataViewViewerFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import kotlinx.coroutines.flow.MutableStateFlow

@Subcomponent(modules = [CreateDataViewViewerModule::class])
@PerModal
interface CreateDataViewViewerSubComponent {
    @Subcomponent.Builder
    interface Builder {
        fun module(module: CreateDataViewViewerModule): Builder
        fun build(): CreateDataViewViewerSubComponent
    }

    fun inject(fragment: CreateDataViewViewerFragment)
}

@Module
object CreateDataViewViewerModule {

    @JvmStatic
    @Provides
    @PerModal
    fun provideCreateDataViewViewerViewModelFactory(
        dispatcher: Dispatcher<Payload>,
        addDataViewViewer: AddDataViewViewer,
        analytics: Analytics,
        objectState: MutableStateFlow<ObjectState>
    ): CreateDataViewViewerViewModel.Factory = CreateDataViewViewerViewModel.Factory(
        dispatcher = dispatcher,
        addDataViewViewer = addDataViewViewer,
        analytics = analytics,
        objectState = objectState
    )

    @JvmStatic
    @Provides
    @PerModal
    fun provideAddDataViewViewerUseCase(
        repo: BlockRepository
    ): AddDataViewViewer = AddDataViewViewer(repo = repo)
}