package com.anytypeio.anytype.di.feature.sets;

import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_utils.di.scope.PerModal
import com.anytypeio.anytype.domain.dataview.interactor.UpdateDataViewViewer
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.objects.options.GetOptions
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.domain.workspace.WorkspaceManager
import com.anytypeio.anytype.presentation.sets.ObjectSetDatabase
import com.anytypeio.anytype.presentation.sets.filter.FilterViewModel
import com.anytypeio.anytype.presentation.sets.state.ObjectState
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.ui.sets.modals.filter.CreateFilterFromInputFieldValueFragment
import com.anytypeio.anytype.ui.sets.modals.filter.CreateFilterFromSelectedValueFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import kotlinx.coroutines.flow.MutableStateFlow

@Subcomponent(modules = [CreateFilterModule::class])
@PerModal
interface CreateFilterSubComponent {
    @Subcomponent.Builder
    interface Builder {
        fun module(module: CreateFilterModule): Builder
        fun build(): CreateFilterSubComponent
    }

    fun inject(fragment: CreateFilterFromSelectedValueFragment)
    fun inject(fragment: CreateFilterFromInputFieldValueFragment)
    fun createPickConditionComponent(): PickFilterConditionSubComponent.Builder
}

@Module
object CreateFilterModule {

    @JvmStatic
    @Provides
    @PerModal
    fun provideViewModelFactory(
        state: MutableStateFlow<ObjectState>,
        dispatcher: Dispatcher<Payload>,
        updateDataViewViewer: UpdateDataViewViewer,
        searchObjects: SearchObjects,
        urlBuilder: UrlBuilder,
        storeOfObjectTypes: StoreOfObjectTypes,
        storeOfRelations: StoreOfRelations,
        objectSetDatabase: ObjectSetDatabase,
        analytics: Analytics,
        getOptions: GetOptions,
        workspaceManager: WorkspaceManager
    ): FilterViewModel.Factory = FilterViewModel.Factory(
        objectState = state,
        dispatcher = dispatcher,
        updateDataViewViewer = updateDataViewViewer,
        searchObjects = searchObjects,
        urlBuilder = urlBuilder,
        storeOfObjectTypes = storeOfObjectTypes,
        storeOfRelations = storeOfRelations,
        objectSetDatabase = objectSetDatabase,
        analytics = analytics,
        getOptions = getOptions,
        workspaceManager = workspaceManager
    )
}
