package com.anytypeio.anytype.di.feature;

import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.dataview.interactor.UpdateDataViewViewer
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.presentation.sets.ObjectSetDatabase
import com.anytypeio.anytype.presentation.sets.filter.ViewerFilterViewModel
import com.anytypeio.anytype.presentation.sets.state.ObjectState
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.ui.sets.ViewerFilterFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import javax.inject.Scope
import kotlinx.coroutines.flow.MutableStateFlow

@Subcomponent(modules = [ViewerFilterModule::class])
@ViewerFilterByScope
interface ViewerFilterSubComponent {
    @Subcomponent.Builder
    interface Builder {
        fun module(module: ViewerFilterModule): Builder
        fun build(): ViewerFilterSubComponent
    }

    fun inject(fragment: ViewerFilterFragment)
}

@Module
object ViewerFilterModule {

    @JvmStatic
    @Provides
    @ViewerFilterByScope
    fun provideViewerFilterViewModelFactory(
        state: MutableStateFlow<ObjectState>,
        dispatcher: Dispatcher<Payload>,
        updateDataViewViewer: UpdateDataViewViewer,
        urlBuilder: UrlBuilder,
        analytics: Analytics,
        storeOfRelations: StoreOfRelations,
        db: ObjectSetDatabase
    ): ViewerFilterViewModel.Factory = ViewerFilterViewModel.Factory(
        state = state,
        dispatcher = dispatcher,
        updateDataViewViewer = updateDataViewViewer,
        urlBuilder = urlBuilder,
        analytics = analytics,
        storeOfRelations = storeOfRelations,
        objectSetDatabase = db
    )
}

@Scope
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class ViewerFilterByScope