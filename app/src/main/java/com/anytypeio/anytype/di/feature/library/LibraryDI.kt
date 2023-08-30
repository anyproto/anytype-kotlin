package com.anytypeio.anytype.di.feature.library

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.launch.GetDefaultPageType
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.`object`.SetObjectDetails
import com.anytypeio.anytype.domain.page.CreateObject
import com.anytypeio.anytype.domain.search.SubscriptionEventChannel
import com.anytypeio.anytype.domain.templates.GetTemplates
import com.anytypeio.anytype.domain.workspace.AddObjectToWorkspace
import com.anytypeio.anytype.domain.workspace.RemoveObjectsFromWorkspace
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.domain.workspace.WorkspaceManager
import com.anytypeio.anytype.presentation.library.LibraryListDelegate
import com.anytypeio.anytype.presentation.library.LibraryResourceManager
import com.anytypeio.anytype.presentation.library.LibraryViewModel
import com.anytypeio.anytype.presentation.library.delegates.LibraryRelationsDelegate
import com.anytypeio.anytype.presentation.library.delegates.LibraryTypesDelegate
import com.anytypeio.anytype.presentation.library.delegates.MyRelationsDelegate
import com.anytypeio.anytype.presentation.library.delegates.MyTypesDelegate
import com.anytypeio.anytype.ui.library.LibraryFragment
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides

@Component(
    dependencies = [LibraryDependencies::class],
    modules = [
        LibraryModule::class,
        LibraryModule.Declarations::class
    ]
)
@PerScreen
interface LibraryComponent {

    @Component.Builder
    interface Builder {
        fun withDependencies(dependencies: LibraryDependencies): Builder

        @BindsInstance
        fun withContext(context: Context): Builder

        fun build(): LibraryComponent
    }

    fun inject(fragment: LibraryFragment)
}

@Module
object LibraryModule {

    @PerScreen
    @Provides
    @JvmStatic
    fun provideMyTypesDelegate(
        container: StorelessSubscriptionContainer,
        spaceManager: SpaceManager,
        urlBuilder: UrlBuilder,
        dispatchers: AppCoroutineDispatchers
    ): LibraryListDelegate {
        return MyTypesDelegate(
            container = container,
            spaceManager = spaceManager,
            urlBuilder = urlBuilder,
            dispatchers = dispatchers
        )
    }

    @PerScreen
    @Provides
    @JvmStatic
    fun provideLibTypesDelegate(
        container: StorelessSubscriptionContainer,
        urlBuilder: UrlBuilder,
        dispatchers: AppCoroutineDispatchers
    ): LibraryListDelegate {
        return LibraryTypesDelegate(container, urlBuilder, dispatchers)
    }

    @PerScreen
    @Provides
    @JvmStatic
    fun provideMyRelationsDelegate(
        container: StorelessSubscriptionContainer,
        spaceManager: SpaceManager,
        urlBuilder: UrlBuilder,
        dispatchers: AppCoroutineDispatchers
    ): LibraryListDelegate {
        return MyRelationsDelegate(
            container = container,
            spaceManager = spaceManager,
            urlBuilder = urlBuilder,
            dispatchers = dispatchers
        )
    }

    @PerScreen
    @Provides
    @JvmStatic
    fun provideLibRelationsDelegate(
        container: StorelessSubscriptionContainer,
        urlBuilder: UrlBuilder,
        dispatchers: AppCoroutineDispatchers
    ): LibraryListDelegate {
        return LibraryRelationsDelegate(container, urlBuilder, dispatchers)
    }

    @Provides
    @PerScreen
    @JvmStatic
    fun addObjectToWorkspace(
        repo: BlockRepository,
        dispatchers: AppCoroutineDispatchers
    ): AddObjectToWorkspace = AddObjectToWorkspace(
        repo = repo,
        dispatchers = dispatchers
    )

    @Provides
    @PerScreen
    @JvmStatic
    fun removeObjectFromWorkspace(
        repo: BlockRepository,
        dispatchers: AppCoroutineDispatchers
    ): RemoveObjectsFromWorkspace = RemoveObjectsFromWorkspace(repo, dispatchers)

    @JvmStatic
    @Provides
    @PerScreen
    fun getCreateObject(
        repo: BlockRepository,
        getTemplates: GetTemplates,
        getDefaultPageType: GetDefaultPageType,
        dispatchers: AppCoroutineDispatchers,
        spaceManager: SpaceManager,
        configStorage: ConfigStorage
    ): CreateObject = CreateObject(
        repo = repo,
        getTemplates = getTemplates,
        getDefaultPageType = getDefaultPageType,
        dispatchers = dispatchers,
        spaceManager = spaceManager,
        configStorage = configStorage
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideGetDefaultPageType(
        userSettingsRepository: UserSettingsRepository,
        blockRepository: BlockRepository,
        workspaceManager: WorkspaceManager,
        dispatchers: AppCoroutineDispatchers,
        spaceManager: SpaceManager,
        configStorage: ConfigStorage
    ): GetDefaultPageType = GetDefaultPageType(
        userSettingsRepository = userSettingsRepository,
        blockRepository = blockRepository,
        workspaceManager = workspaceManager,
        dispatchers = dispatchers,
        spaceManager = spaceManager,
        configStorage = configStorage
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideGetTemplates(
        repo: BlockRepository,
        dispatchers: AppCoroutineDispatchers
    ): GetTemplates = GetTemplates(
        repo = repo,
        dispatchers = dispatchers
    )

    @Provides
    @PerScreen
    @JvmStatic
    fun objectSetDetails(
        repo: BlockRepository,
        dispatchers: AppCoroutineDispatchers
    ): SetObjectDetails = SetObjectDetails(repo = repo, dispatchers = dispatchers)

    @Module
    interface Declarations {

        @PerScreen
        @Binds
        fun bindViewModelFactory(factory: LibraryViewModel.Factory): ViewModelProvider.Factory

        @PerScreen
        @Binds
        fun bindContainer(container: StorelessSubscriptionContainer.Impl): StorelessSubscriptionContainer

        @PerScreen
        @Binds
        fun bindResourceManager(manager: LibraryResourceManager.Impl): LibraryResourceManager

    }

}

interface LibraryDependencies : ComponentDependencies {
    fun blockRepository(): BlockRepository
    fun workspaceManager(): WorkspaceManager
    fun urlBuilder(): UrlBuilder
    fun channel(): SubscriptionEventChannel
    fun dispatchers(): AppCoroutineDispatchers
    fun userSettingsRepository(): UserSettingsRepository
    fun analytics(): Analytics
    fun spaceManager(): SpaceManager
    fun config(): ConfigStorage
}