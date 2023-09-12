package com.anytypeio.anytype.di.feature.sets.viewer

import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_utils.di.scope.PerModal
import com.anytypeio.anytype.domain.dataview.interactor.UpdateDataViewViewer
import com.anytypeio.anytype.presentation.sets.state.ObjectState
import com.anytypeio.anytype.presentation.sets.viewer.ViewerCardSizeSelectViewModel
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.ui.sets.modals.viewer.ViewerCardSizeSelectFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import kotlinx.coroutines.flow.MutableStateFlow

@Subcomponent(modules = [ViewerCardSizeSelectModule::class])
@PerModal
interface ViewerCardSizeSelectSubcomponent {
    @Subcomponent.Builder
    interface Builder {
        fun module(module: ViewerCardSizeSelectModule): Builder
        fun build(): ViewerCardSizeSelectSubcomponent
    }

    fun inject(fragment: ViewerCardSizeSelectFragment)
}

@Module
object ViewerCardSizeSelectModule {

    @JvmStatic
    @Provides
    @PerModal
    fun provideViewModelFactory(
        state: MutableStateFlow<ObjectState>,
        dispatcher: Dispatcher<Payload>,
        updateDataViewViewer: UpdateDataViewViewer
    ): ViewerCardSizeSelectViewModel.Factory = ViewerCardSizeSelectViewModel.Factory(
        objectState = state,
        dispatcher = dispatcher,
        updateDataViewViewer = updateDataViewViewer
    )
}