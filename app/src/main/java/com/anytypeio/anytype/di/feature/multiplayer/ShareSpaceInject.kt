package com.anytypeio.anytype.di.feature.multiplayer

import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.core_utils.di.scope.CreateFromScratch
import com.anytypeio.anytype.core_utils.di.scope.PerDialog
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.di.feature.BacklinkOrAddToObjectComponent
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.multiplayer.ShareSpaceViewModel
import com.anytypeio.anytype.ui.multiplayer.ShareSpaceFragment
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module

@Component(
    dependencies = [ShareSpaceDependencies::class],
    modules = [
        ShareSpaceModule::class,
        ShareSpaceModule.Declarations::class
    ]
)
@PerDialog
interface ShareSpaceComponent {

    @Component.Builder
    interface Builder {
        fun withDependencies(dependencies: ShareSpaceDependencies): Builder
        @BindsInstance
        fun withParams(params: ShareSpaceViewModel.Params): Builder
        fun build(): ShareSpaceComponent
    }

    fun inject(fragment: ShareSpaceFragment)
}

@Module
object ShareSpaceModule {

    @Module
    interface Declarations {
        @PerScreen
        @Binds
        fun bindViewModelFactory(factory: ShareSpaceViewModel.Factory): ViewModelProvider.Factory
    }

}

interface ShareSpaceDependencies : ComponentDependencies {
    fun blockRepository(): BlockRepository
    fun urlBuilder(): UrlBuilder
    fun spaceManager(): SpaceManager
}