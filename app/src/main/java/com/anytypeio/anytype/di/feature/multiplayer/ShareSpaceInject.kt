package com.anytypeio.anytype.di.feature.multiplayer

import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_utils.di.scope.PerDialog
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.config.TechSpaceProvider
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import com.anytypeio.anytype.presentation.membership.provider.MembershipProvider
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
        fun withParams(params: ShareSpaceViewModel.VmParams): Builder
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
    fun auth() : AuthRepository
    fun urlBuilder(): UrlBuilder
    fun spaceManager(): SpaceManager
    fun dispatchers(): AppCoroutineDispatchers
    fun container(): StorelessSubscriptionContainer
    fun config(): ConfigStorage
    fun techSpaceProvider(): TechSpaceProvider
    fun permissions(): UserPermissionProvider
    fun analytics(): Analytics
    fun analyticSpaceHelper(): AnalyticSpaceHelperDelegate
    fun provideMembershipProvider(): MembershipProvider
    fun spaceViewSubscriptionContainer(): SpaceViewSubscriptionContainer
}