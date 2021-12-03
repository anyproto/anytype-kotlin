package com.anytypeio.anytype.di.feature.sets.viewer

import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_utils.di.scope.PerModal
import com.anytypeio.anytype.domain.dataview.interactor.UpdateDataViewViewer
import com.anytypeio.anytype.presentation.sets.ObjectSet
import com.anytypeio.anytype.presentation.sets.ObjectSetSession
import com.anytypeio.anytype.presentation.sets.viewer.ViewerImagePreviewSelectViewModel
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.ui.sets.modals.viewer.ViewerImagePreviewSelectFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import kotlinx.coroutines.flow.StateFlow

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
        state: StateFlow<ObjectSet>,
        session: ObjectSetSession,
        dispatcher: Dispatcher<Payload>,
        updateDataViewViewer: UpdateDataViewViewer
    ): ViewerImagePreviewSelectViewModel.Factory = ViewerImagePreviewSelectViewModel.Factory(
        objectSetState = state,
        session = session,
        dispatcher = dispatcher,
        updateDataViewViewer = updateDataViewViewer
    )
}