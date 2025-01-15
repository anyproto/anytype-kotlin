package com.anytypeio.anytype.di.feature.chats

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.core_utils.tools.FeatureToggles
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.chats.ChatEventChannel
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.debugging.Logger
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.ActiveSpaceMemberSubscriptionContainer
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.feature_discussions.presentation.ChatViewModel
import com.anytypeio.anytype.feature_discussions.presentation.ChatViewModelFactory
import com.anytypeio.anytype.middleware.EventProxy
import com.anytypeio.anytype.presentation.util.CopyFileToCacheDirectory
import com.anytypeio.anytype.presentation.util.DefaultCopyFileToCacheDirectory
import com.anytypeio.anytype.ui.chats.ChatFragment
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides

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
        fun withParams(params: ChatViewModel.Params): Builder
        fun withDependencies(dependencies: DiscussionComponentDependencies): Builder
        fun build(): DiscussionComponent
    }
    fun inject(fragment: ChatFragment)
}

@Component(
    dependencies = [DiscussionComponentDependencies::class],
    modules = [
        DiscussionModule::class,
        DiscussionModule.Declarations::class
    ]
)
@PerScreen
interface SpaceLevelChatComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun withParams(params: ChatViewModel.Params): Builder
        fun withDependencies(dependencies: DiscussionComponentDependencies): Builder
        fun build(): SpaceLevelChatComponent
    }

    fun getViewModel(): ChatViewModel
}

@Module
object DiscussionModule {

    @JvmStatic
    @Provides
    @PerScreen
    fun provideCopyFileToCache(
        context: Context
    ): CopyFileToCacheDirectory = DefaultCopyFileToCacheDirectory(context)

    @Module
    interface Declarations {
        @PerScreen
        @Binds
        fun bindViewModelFactory(
            factory: ChatViewModelFactory
        ): ViewModelProvider.Factory
    }
}

interface DiscussionComponentDependencies : ComponentDependencies {
    fun blockRepository(): BlockRepository
    fun authRepo(): AuthRepository
    fun appCoroutineDispatchers(): AppCoroutineDispatchers
    fun analytics(): Analytics
    fun urlBuilder(): UrlBuilder
    fun userPermissionProvider(): UserPermissionProvider
    fun eventProxy(): EventProxy
    fun featureToggles(): FeatureToggles
    fun userSettings(): UserSettingsRepository
    fun chatEventChannel(): ChatEventChannel
    fun logger(): Logger
    fun members(): ActiveSpaceMemberSubscriptionContainer
    fun spaceViewSubscriptionContainer(): SpaceViewSubscriptionContainer
    fun storeOfObjectTypes(): StoreOfObjectTypes
    fun context(): Context
}