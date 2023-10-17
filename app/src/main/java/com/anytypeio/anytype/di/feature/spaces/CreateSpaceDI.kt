package com.anytypeio.anytype.di.feature.spaces

import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.spaces.CreateSpaceViewModel
import com.anytypeio.anytype.presentation.spaces.SpaceGradientProvider
import com.anytypeio.anytype.ui.spaces.CreateSpaceFragment
import dagger.Binds
import dagger.Component
import dagger.Module

@Component(
    dependencies = [CreateSpaceDependencies::class],
    modules = [
        CreateSpaceModule::class,
        CreateSpaceModule.Declarations::class
    ]
)
@PerScreen
interface CreateSpaceComponent {
    @Component.Factory
    interface Builder {
        fun create(dependencies: CreateSpaceDependencies): CreateSpaceComponent
    }

    fun inject(fragment: CreateSpaceFragment)
}

@Module
object CreateSpaceModule {
    @Module
    interface Declarations {
        @Binds
        @PerScreen
        fun bindViewModelFactory(factory: CreateSpaceViewModel.Factory): ViewModelProvider.Factory

        @Binds
        @PerScreen
        fun spaceGradientProvider(provider: SpaceGradientProvider.Impl): SpaceGradientProvider
    }
}

interface CreateSpaceDependencies : ComponentDependencies {
    fun repo(): BlockRepository
    fun analytics(): Analytics
    fun dispatchers(): AppCoroutineDispatchers
    fun spaceManager(): SpaceManager
}