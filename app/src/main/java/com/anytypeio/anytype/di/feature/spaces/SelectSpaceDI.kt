package com.anytypeio.anytype.di.feature.spaces

import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.search.SubscriptionEventChannel
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.spaces.SelectSpaceViewModel
import com.anytypeio.anytype.presentation.spaces.SpaceGradientProvider
import com.anytypeio.anytype.ui.spaces.SelectSpaceFragment
import dagger.Binds
import dagger.Component
import dagger.Module

@Component(
    dependencies = [SelectSpaceDependencies::class],
    modules = [
        SelectSpaceModule::class,
        SelectSpaceModule.Declarations::class
    ]
)
@PerScreen
interface SelectSpaceComponent {
    @Component.Factory
    interface Builder {
        fun create(dependencies: SelectSpaceDependencies): SelectSpaceComponent
    }

    fun inject(fragment: SelectSpaceFragment)
}

@Module
object SelectSpaceModule {
    @Module
    interface Declarations {
        @PerScreen
        @Binds
        fun container(container: StorelessSubscriptionContainer.Impl): StorelessSubscriptionContainer
        @Binds
        @PerScreen
        fun bindViewModelFactory(factory: SelectSpaceViewModel.Factory): ViewModelProvider.Factory
        @PerScreen
        @Binds
        fun factory(provider: SpaceGradientProvider.Impl): SpaceGradientProvider
    }
}

interface SelectSpaceDependencies : ComponentDependencies {
    fun repo(): BlockRepository
    fun subscriptionEventChannel(): SubscriptionEventChannel
    fun analytics(): Analytics
    fun dispatchers(): AppCoroutineDispatchers
    fun spaceManager(): SpaceManager
    fun urlBuilder(): UrlBuilder
    fun userSettings(): UserSettingsRepository
}