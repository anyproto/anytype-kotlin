package com.anytypeio.anytype.di.feature.discussions

import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.core_utils.tools.FeatureToggles
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.di.feature.EditorSubComponent.Builder
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider
import com.anytypeio.anytype.feature_discussions.presentation.DiscussionViewModelFactory
import com.anytypeio.anytype.middleware.EventProxy
import com.anytypeio.anytype.presentation.common.BaseViewModel
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module

@Component(
    dependencies = [DiscussionComponentDependencies::class],
    modules = [
        DiscussionModule::class,
        DiscussionModule.Declarations::class
    ]
)
@PerScreen
interface DiscussionComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun withParams(params: BaseViewModel.DefaultParams): Builder
        fun withDependencies(dependencies: DiscussionComponentDependencies): Builder
        fun build(): DiscussionComponent
    }
    fun inject(fragment: DiscussionFragment)
}

@Module
object DiscussionModule {
    @Module
    interface Declarations {
        @PerScreen
        @Binds
        fun bindViewModelFactory(
            factory: DiscussionViewModelFactory
        ): ViewModelProvider.Factory

    }
}

interface DiscussionComponentDependencies : ComponentDependencies {
    fun blockRepository(): BlockRepository
    fun appCoroutineDispatchers(): AppCoroutineDispatchers
    fun analytics(): Analytics
    fun urlBuilder(): UrlBuilder
    fun userPermissionProvider(): UserPermissionProvider
    fun eventProxy(): EventProxy
    fun featureToggles(): FeatureToggles
    fun userSettings(): UserSettingsRepository
}