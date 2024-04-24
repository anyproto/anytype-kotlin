package com.anytypeio.anytype.di.feature.spaces

import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.presentation.spaces.SpaceListViewModel
import com.anytypeio.anytype.ui.spaces.SpaceListFragment
import dagger.Binds
import dagger.Component
import dagger.Module

@Component(
    dependencies = [SpaceListDependencies::class],
    modules = [
        SpaceListModule::class,
        SpaceListModule.Declarations::class
    ]
)
@PerScreen
interface SpaceListComponent {
    @Component.Factory
    interface Builder {
        fun create(dependencies: SpaceListDependencies): SpaceListComponent
    }
    fun inject(fragment: SpaceListFragment)
}

@Module
object SpaceListModule {
    @Module
    interface Declarations {
        @Binds
        @PerScreen
        fun bindViewModelFactory(factory: SpaceListViewModel.Factory): ViewModelProvider.Factory
    }
}

interface SpaceListDependencies : ComponentDependencies {
    fun spaceViewContainer(): SpaceViewSubscriptionContainer
}