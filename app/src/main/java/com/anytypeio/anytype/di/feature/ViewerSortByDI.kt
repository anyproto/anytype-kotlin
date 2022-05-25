package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.presentation.sets.ObjectSet
import com.anytypeio.anytype.presentation.sets.ViewerSortByViewModel
import com.anytypeio.anytype.ui.sets.ViewerSortByFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Scope

@Subcomponent(modules = [ViewerSortByModule::class])
@ViewerSortByScope
interface ViewerSortBySubComponent {

    @Subcomponent.Builder
    interface Builder {
        fun module(module: ViewerSortByModule): Builder
        fun build(): ViewerSortBySubComponent
    }

    fun inject(fragment: ViewerSortByFragment)
}

@Module
object ViewerSortByModule {

    @JvmStatic
    @Provides
    @ViewerSortByScope
    fun provideViewerSortByViewModelFactory(
        state: StateFlow<ObjectSet>
    ): ViewerSortByViewModel.Factory = ViewerSortByViewModel.Factory(state)
}

@Scope
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class ViewerSortByScope