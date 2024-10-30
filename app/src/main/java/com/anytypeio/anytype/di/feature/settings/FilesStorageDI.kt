package com.anytypeio.anytype.di.feature.settings

import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.device.BuildProvider
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.domain.account.DeleteAccount
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.debugging.Logger
import com.anytypeio.anytype.domain.device.ClearFileCache
import com.anytypeio.anytype.domain.workspace.FileLimitsEventChannel
import com.anytypeio.anytype.domain.workspace.InterceptFileLimitEvents
import com.anytypeio.anytype.domain.workspace.SpacesUsageInfo
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import com.anytypeio.anytype.presentation.settings.FilesStorageViewModel
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
    fun clearFileCache(repo: BlockRepository): ClearFileCache = ClearFileCache(repo)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideSpaceUsage(
        repo: BlockRepository,
        dispatchers: AppCoroutineDispatchers
    ): SpacesUsageInfo = SpacesUsageInfo(repo, dispatchers)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideFileLimitEvents(
        channel: FileLimitsEventChannel,
        dispatchers: AppCoroutineDispatchers
    ): InterceptFileLimitEvents = InterceptFileLimitEvents(channel, dispatchers)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideDeleteAccountUseCase(
        repo: AuthRepository
    ): DeleteAccount = DeleteAccount(repo = repo)

    @Module
    interface Declarations {

        @PerScreen
        @Binds
        fun bindViewModelFactory(factory: FilesStorageViewModel.Factory): ViewModelProvider.Factory
    }
}

interface FilesStorageDependencies : ComponentDependencies {
    fun blockRepo(): BlockRepository
    fun dispatchers(): AppCoroutineDispatchers
    fun analytics(): Analytics
    fun fileEventsChannel(): FileLimitsEventChannel
    fun buildProvider(): BuildProvider
    fun authRepo(): AuthRepository
    fun logger(): Logger
    fun analyticSpaceHelper(): AnalyticSpaceHelperDelegate
}