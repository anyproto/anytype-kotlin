package com.anytypeio.anytype.di.feature.multiplayer

import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_utils.di.scope.PerDialog
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.debugging.Logger
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.SpaceInviteResolver
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.other.DefaultSpaceInviteResolver
import com.anytypeio.anytype.presentation.multiplayer.RequestJoinSpaceViewModel
import com.anytypeio.anytype.ui.multiplayer.RequestJoinSpaceFragment
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides

@Component(
    dependencies = [RequestJoinSpaceDependencies::class],
    modules = [
        RequestJoinSpaceModule::class,
        RequestJoinSpaceModule.Declarations::class
    ]
)
@PerDialog
interface RequestJoinSpaceComponent {

    @Component.Builder
    interface Builder {
        fun withDependencies(dependencies: RequestJoinSpaceDependencies): Builder
        @BindsInstance
        fun withParams(params: RequestJoinSpaceViewModel.Params): Builder
        fun build(): RequestJoinSpaceComponent
    }

    fun inject(fragment: RequestJoinSpaceFragment)
}

@Module
object RequestJoinSpaceModule {
    @Module
    interface Declarations {
        @PerDialog
        @Binds
        fun bindViewModelFactory(factory: RequestJoinSpaceViewModel.Factory): ViewModelProvider.Factory
    }

    @PerDialog
    @Provides
    fun provideSpaceInviteResolver() : SpaceInviteResolver = DefaultSpaceInviteResolver
}

interface RequestJoinSpaceDependencies : ComponentDependencies {
    fun blockRepository(): BlockRepository
    fun auth(): AuthRepository
    fun settings(): UserSettingsRepository
    fun urlBuilder(): UrlBuilder
    fun dispatchers(): AppCoroutineDispatchers
    fun spaceManager(): SpaceManager
    fun logger(): Logger
    fun anallytics(): Analytics
}