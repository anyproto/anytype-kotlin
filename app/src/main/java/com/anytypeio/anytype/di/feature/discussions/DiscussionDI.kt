package com.anytypeio.anytype.di.feature.discussions

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.core_models.UrlBuilder
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.chats.ChatEventChannel
import com.anytypeio.anytype.domain.debugging.Logger
import com.anytypeio.anytype.domain.misc.DateProvider
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.multiplayer.ActiveSpaceMemberSubscriptionContainer
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.feature_discussions.presentation.DiscussionViewModel
import com.anytypeio.anytype.feature_discussions.presentation.DiscussionViewModelFactory
import com.anytypeio.anytype.presentation.util.CopyFileToCacheDirectory
import com.anytypeio.anytype.presentation.util.defaultCopyFileToCacheDirectory
import com.anytypeio.anytype.ui.discussions.DiscussionFragment
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides

@Component(
    dependencies = [DiscussionComponentDependencies::class],
    modules = [
        DiscussionModule.Declarations::class,
        DiscussionModule.Providers::class
    ]
)
@PerScreen
interface DiscussionComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun withParams(params: DiscussionViewModel.Params): Builder
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

    @Module
    object Providers {
        @JvmStatic
        @Provides
        @PerScreen
        fun provideCopyFileToCache(
            context: Context
        ): CopyFileToCacheDirectory = defaultCopyFileToCacheDirectory(context)
    }
}

interface DiscussionComponentDependencies : ComponentDependencies {
    fun blockRepository(): BlockRepository
    fun authRepo(): AuthRepository
    fun appCoroutineDispatchers(): AppCoroutineDispatchers
    fun urlBuilder(): UrlBuilder
    fun chatEventChannel(): ChatEventChannel
    fun logger(): Logger
    fun members(): ActiveSpaceMemberSubscriptionContainer
    fun storelessSubscriptionContainer(): StorelessSubscriptionContainer
    fun dateProvider(): DateProvider
    fun storeOfObjectTypes(): StoreOfObjectTypes
    fun fieldParser(): FieldParser
    fun context(): Context
}
