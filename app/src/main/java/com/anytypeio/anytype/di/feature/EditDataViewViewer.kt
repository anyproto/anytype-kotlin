package com.anytypeio.anytype.di.feature;

import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_utils.di.scope.PerModal
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.dataview.interactor.*
import com.anytypeio.anytype.presentation.sets.EditDataViewViewerViewModel
import com.anytypeio.anytype.presentation.sets.ObjectSetPaginator
import com.anytypeio.anytype.presentation.sets.ObjectSetSession
import com.anytypeio.anytype.presentation.sets.state.ObjectState
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.ui.sets.modals.EditDataViewViewerFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import kotlinx.coroutines.flow.MutableStateFlow
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
        dispatcher: Dispatcher<Payload>,
        state: MutableStateFlow<ObjectState>,
        objectSetSession: ObjectSetSession,
        paginator: ObjectSetPaginator,
        analytics: Analytics
    ): EditDataViewViewerViewModel.Factory = EditDataViewViewerViewModel.Factory(
        dispatcher = dispatcher,
        objectState = state,
        objectSetSession = objectSetSession,
        paginator = paginator,
        analytics = analytics
    )
}