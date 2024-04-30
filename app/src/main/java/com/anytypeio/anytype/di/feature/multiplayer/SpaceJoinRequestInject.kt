package com.anytypeio.anytype.di.feature.multiplayer

import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_utils.di.scope.PerDialog
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import com.anytypeio.anytype.presentation.multiplayer.SpaceJoinRequestViewModel
import com.anytypeio.anytype.ui.multiplayer.SpaceJoinRequestFragment
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module

@Component(
    dependencies = [SpaceJoinRequestDependencies::class],
    modules = [
        SpaceJoinRequestModule::class,
        SpaceJoinRequestModule.Declarations::class
    ]
)
@PerDialog
interface SpaceJoinRequestComponent {

    @Component.Builder
    interface Builder {
        fun withDependencies(dependencies: SpaceJoinRequestDependencies): Builder
        @BindsInstance
        fun withParams(params: SpaceJoinRequestViewModel.Params): Builder
        fun build(): SpaceJoinRequestComponent
    }

    fun inject(fragment: SpaceJoinRequestFragment)
}

@Module
object SpaceJoinRequestModule {

    @Module
    interface Declarations {
        @PerScreen
        @Binds
        fun bindViewModelFactory(factory: SpaceJoinRequestViewModel.Factory): ViewModelProvider.Factory
    }

}

interface SpaceJoinRequestDependencies : ComponentDependencies {
    fun blockRepository(): BlockRepository
    fun urlBuilder(): UrlBuilder
    fun dispatchers(): AppCoroutineDispatchers
    fun spaceManager(): SpaceManager
    fun analytics(): Analytics
    fun analyticSpaceHelper(): AnalyticSpaceHelperDelegate
}