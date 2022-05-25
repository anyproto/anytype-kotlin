package com.anytypeio.anytype.di.feature;

import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.dataview.interactor.UpdateDataViewViewer
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.presentation.sets.ObjectSet
import com.anytypeio.anytype.presentation.sets.ObjectSetSession
import com.anytypeio.anytype.presentation.sets.filter.ViewerFilterViewModel
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.ui.sets.ViewerFilterFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Scope

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
        state: StateFlow<ObjectSet>,
        session: ObjectSetSession,
        dispatcher: Dispatcher<Payload>,
        updateDataViewViewer: UpdateDataViewViewer,
        urlBuilder: UrlBuilder,
        analytics: Analytics
    ): ViewerFilterViewModel.Factory = ViewerFilterViewModel.Factory(
        state = state,
        session = session,
        dispatcher = dispatcher,
        updateDataViewViewer = updateDataViewViewer,
        urlBuilder = urlBuilder,
        analytics = analytics
    )
}

@Scope
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class ViewerFilterByScope