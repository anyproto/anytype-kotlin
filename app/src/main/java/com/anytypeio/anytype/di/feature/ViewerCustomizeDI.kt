package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.presentation.sets.ObjectSet
import com.anytypeio.anytype.presentation.sets.ViewerCustomizeViewModel
import com.anytypeio.anytype.ui.sets.modals.ViewerCustomizeFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Scope

@Subcomponent(modules = [ViewerCustomizeModule::class])
@ViewerCustomizeScope
interface ViewerCustomizeSubComponent {

    @Subcomponent.Builder
    interface Builder {
        fun module(module: ViewerCustomizeModule): Builder
        fun build(): ViewerCustomizeSubComponent
    }

    fun inject(fragment: ViewerCustomizeFragment)

}

@Module
object ViewerCustomizeModule {

    @JvmStatic
    @Provides
    @ViewerCustomizeScope
    fun provideViewerCustomizeViewModelFactory(
        state: StateFlow<ObjectSet>
    ): ViewerCustomizeViewModel.Factory = ViewerCustomizeViewModel.Factory(
        state = state
    )
}

@Scope
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class ViewerCustomizeScope