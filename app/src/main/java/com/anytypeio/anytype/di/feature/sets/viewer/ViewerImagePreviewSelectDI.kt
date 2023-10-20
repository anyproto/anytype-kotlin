package com.anytypeio.anytype.di.feature.sets.viewer

import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_utils.di.scope.PerModal
import com.anytypeio.anytype.domain.dataview.interactor.UpdateDataViewViewer
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.presentation.sets.state.ObjectState
import com.anytypeio.anytype.presentation.sets.viewer.ViewerImagePreviewSelectViewModel
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.ui.sets.modals.viewer.ViewerImagePreviewSelectFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import kotlinx.coroutines.flow.MutableStateFlow

@Subcomponent(modules = [ViewerImagePreviewSelectModule::class])
@PerModal
interface ViewerImagePreviewSelectSubcomponent {
    @Subcomponent.Builder
    interface Builder {
        fun module(module: ViewerImagePreviewSelectModule): Builder
        fun build(): ViewerImagePreviewSelectSubcomponent
    }

    fun inject(fragment: ViewerImagePreviewSelectFragment)
}

@Module
object ViewerImagePreviewSelectModule {

    @JvmStatic
    @Provides
    @PerModal
    fun provideViewModelFactory(
        state: MutableStateFlow<ObjectState>,
        dispatcher: Dispatcher<Payload>,
        updateDataViewViewer: UpdateDataViewViewer,
        storeOfRelations: StoreOfRelations
    ): ViewerImagePreviewSelectViewModel.Factory = ViewerImagePreviewSelectViewModel.Factory(
        objectState = state,
        dispatcher = dispatcher,
        updateDataViewViewer = updateDataViewViewer,
        storeOfRelations = storeOfRelations
    )
}