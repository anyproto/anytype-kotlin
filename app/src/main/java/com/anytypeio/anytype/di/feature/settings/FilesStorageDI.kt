package com.anytypeio.anytype.di.feature.settings

import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.device.BuildProvider
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.device.ClearFileCache
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.search.SubscriptionEventChannel
import com.anytypeio.anytype.domain.workspace.FileLimitsEventChannel
import com.anytypeio.anytype.domain.workspace.FileSpaceUsage
import com.anytypeio.anytype.domain.workspace.InterceptFileLimitEvents
import com.anytypeio.anytype.presentation.settings.FilesStorageViewModel
import com.anytypeio.anytype.presentation.spaces.SpaceGradientProvider
import com.anytypeio.anytype.ui.settings.FilesStorageFragment
import dagger.Binds
import dagger.Component
import dagger.Module
import dagger.Provides

@PerScreen
@Component(
    dependencies = [FilesStorageDependencies::class],
    modules = [
        FilesStorageModule::class,
        FilesStorageModule.Declarations::class
    ]
)
interface FilesStorageComponent {

    @Component.Builder
    interface Builder {

        fun withDependencies(dependency: FilesStorageDependencies): Builder
        fun build(): FilesStorageComponent
    }

    fun inject(fragment: FilesStorageFragment)
}

@Module
object FilesStorageModule {

    @JvmStatic
    @Provides
    @PerScreen
    fun provideStoreLessSubscriptionContainer(
        repo: BlockRepository,
        channel: SubscriptionEventChannel,
        dispatchers: AppCoroutineDispatchers
    ): StorelessSubscriptionContainer = StorelessSubscriptionContainer.Impl(
        repo = repo,
        channel = channel,
        dispatchers = dispatchers
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideSpaceGradientProvider(): SpaceGradientProvider = SpaceGradientProvider.Impl()

    @JvmStatic
    @Provides
    @PerScreen
    fun clearFileCache(repo: BlockRepository): ClearFileCache = ClearFileCache(repo)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideSpaceUsage(
        repo: BlockRepository,
        dispatchers: AppCoroutineDispatchers
    ): FileSpaceUsage = FileSpaceUsage(repo, dispatchers)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideFileLimitEvents(
        channel: FileLimitsEventChannel,
        dispatchers: AppCoroutineDispatchers
    ) : InterceptFileLimitEvents = InterceptFileLimitEvents(channel, dispatchers)

    @Module
    interface Declarations {

        @PerScreen
        @Binds
        fun bindViewModelFactory(factory: FilesStorageViewModel.Factory): ViewModelProvider.Factory
    }
}

interface FilesStorageDependencies : ComponentDependencies {
    fun blockRepo(): BlockRepository
    fun urlBuilder(): UrlBuilder
    fun dispatchers(): AppCoroutineDispatchers
    fun analytics(): Analytics
    fun configStorage(): ConfigStorage
    fun channel(): SubscriptionEventChannel
    fun fileEventsChannel(): FileLimitsEventChannel
    fun buildProvider(): BuildProvider
}