package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_utils.di.scope.PerModal
import com.anytypeio.anytype.domain.dataview.interactor.UpdateDataViewViewer
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.relations.DeleteRelationFromDataView
import com.anytypeio.anytype.presentation.relations.ObjectSetSettingsViewModel
import com.anytypeio.anytype.presentation.sets.state.ObjectState
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.ui.sets.modals.ObjectSetSettingsFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import kotlinx.coroutines.flow.MutableStateFlow

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
        state: MutableStateFlow<ObjectState>,
        dispatcher: Dispatcher<Payload>,
        updateDataViewViewer: UpdateDataViewViewer,
        store: StoreOfRelations,
        analytics: Analytics,
        deleteRelationFromDataView: DeleteRelationFromDataView,
    ): ObjectSetSettingsViewModel.Factory = ObjectSetSettingsViewModel.Factory(
        objectState = state,
        dispatcher = dispatcher,
        updateDataViewViewer = updateDataViewViewer,
        analytics = analytics,
        store = store,
        deleteRelationFromDataView = deleteRelationFromDataView
    )
}