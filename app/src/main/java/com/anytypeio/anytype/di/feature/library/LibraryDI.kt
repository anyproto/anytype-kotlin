package com.anytypeio.anytype.di.feature.library

import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.presentation.library.LibraryViewModel
import com.anytypeio.anytype.ui.library.LibraryFragment
import dagger.Binds
import dagger.Component
import dagger.Module

@Component(
    dependencies = [LibraryDependencies::class],
    modules = [
        LibraryModule::class,
        LibraryModule.Declarations::class
    ]
)
@PerScreen
interface LibraryComponent {

    @Component.Factory
    interface Factory {
        fun create(dependencies: LibraryDependencies): LibraryComponent
    }

    fun inject(fragment: LibraryFragment)
}

@Module
object LibraryModule {

    @Module
    interface Declarations {

        @PerScreen
        @Binds
        fun bindViewModelFactory(factory: LibraryViewModel.Factory): ViewModelProvider.Factory

    }

}

interface LibraryDependencies : ComponentDependencies