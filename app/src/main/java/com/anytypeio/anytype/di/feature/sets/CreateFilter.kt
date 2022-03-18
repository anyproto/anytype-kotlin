package com.anytypeio.anytype.di.feature.sets;

import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_utils.di.scope.PerModal
import com.anytypeio.anytype.domain.`object`.ObjectTypesProvider
import com.anytypeio.anytype.domain.dataview.interactor.SearchObjects
import com.anytypeio.anytype.domain.dataview.interactor.UpdateDataViewViewer
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.presentation.sets.ObjectSet
import com.anytypeio.anytype.presentation.sets.ObjectSetSession
import com.anytypeio.anytype.presentation.sets.filter.FilterViewModel
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.ui.sets.modals.filter.CreateFilterFromInputFieldValueFragment
import com.anytypeio.anytype.ui.sets.modals.filter.CreateFilterFromSelectedValueFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import kotlinx.coroutines.flow.StateFlow

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
        state: StateFlow<ObjectSet>,
        session: ObjectSetSession,
        dispatcher: Dispatcher<Payload>,
        updateDataViewViewer: UpdateDataViewViewer,
        searchObjects: SearchObjects,
        urlBuilder: UrlBuilder,
        objectTypesProvider: ObjectTypesProvider,
        analytics: Analytics
    ): FilterViewModel.Factory = FilterViewModel.Factory(
        objectSetState = state,
        session = session,
        dispatcher = dispatcher,
        updateDataViewViewer = updateDataViewViewer,
        searchObjects = searchObjects,
        urlBuilder = urlBuilder,
        objectTypesProvider = objectTypesProvider,
        analytics = analytics
    )
}
