package com.anytypeio.anytype.di.feature

import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.domain.all_content.RestoreAllContentState
import com.anytypeio.anytype.domain.all_content.UpdateAllContentState
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.debugging.Logger
import com.anytypeio.anytype.domain.launch.GetDefaultObjectType
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.LocaleProvider
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.page.CreateObject
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.domain.search.SubscriptionEventChannel
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.feature_allcontent.presentation.AllContentViewModel
import com.anytypeio.anytype.feature_allcontent.presentation.AllContentViewModelFactory
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import com.anytypeio.anytype.ui.allcontent.AllContentFragment
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides

@Component(
    dependencies = [AllContentDependencies::class],
    modules = [
        AllContentModule::class,
        AllContentModule.Declarations::class
    ]
)
@PerScreen
interface AllContentComponent {
    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance vmParams: AllContentViewModel.VmParams,
            dependencies: AllContentDependencies
        ): AllContentComponent
    }

    fun inject(fragment: AllContentFragment)
}

@Module
object AllContentModule {

    @JvmStatic
    @Provides
    @PerScreen
    fun provideStoreLessSubscriptionContainer(
        repo: BlockRepository,
        channel: SubscriptionEventChannel,
        dispatchers: AppCoroutineDispatchers,
        logger: Logger
    ): StorelessSubscriptionContainer = StorelessSubscriptionContainer.Impl(
        repo = repo,
        channel = channel,
        dispatchers = dispatchers,
        logger = logger
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideUpdateAllContentState(
        dispatchers: AppCoroutineDispatchers,
        userSettingsRepository: UserSettingsRepository
    ): UpdateAllContentState = UpdateAllContentState(
        dispatchers = dispatchers,
        settings = userSettingsRepository
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideRestoreAllContentState(
        dispatchers: AppCoroutineDispatchers,
        userSettingsRepository: UserSettingsRepository
    ): RestoreAllContentState = RestoreAllContentState(
        dispatchers = dispatchers,
        settings = userSettingsRepository
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun searchObjects(
        repo: BlockRepository
    ): SearchObjects = SearchObjects(repo = repo)

    @JvmStatic
    @Provides
    @PerScreen
    fun createObject(
        repo: BlockRepository,
        getDefaultObjectType: GetDefaultObjectType,
        dispatchers: AppCoroutineDispatchers,
        spaceManager: SpaceManager,
    ): CreateObject = CreateObject(
        repo = repo,
        getDefaultObjectType = getDefaultObjectType,
        dispatchers = dispatchers,
        spaceManager = spaceManager
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideGetDefaultPageType(
        userSettingsRepository: UserSettingsRepository,
        blockRepository: BlockRepository,
        dispatchers: AppCoroutineDispatchers,
        spaceManager: SpaceManager,
        configStorage: ConfigStorage
    ): GetDefaultObjectType = GetDefaultObjectType(
        userSettingsRepository = userSettingsRepository,
        blockRepository = blockRepository,
        dispatchers = dispatchers,
        spaceManager = spaceManager,
        configStorage = configStorage
    )

    @Module
    interface Declarations {
        @PerScreen
        @Binds
        fun bindViewModelFactory(
            factory: AllContentViewModelFactory
        ): ViewModelProvider.Factory

    }
}

interface AllContentDependencies : ComponentDependencies {
    fun blockRepository(): BlockRepository
    fun analytics(): Analytics
    fun urlBuilder(): UrlBuilder
    fun dispatchers(): AppCoroutineDispatchers
    fun storeOfObjectTypes(): StoreOfObjectTypes
    fun analyticsHelper(): AnalyticSpaceHelperDelegate
    fun userSettingsRepository(): UserSettingsRepository
    fun subEventChannel(): SubscriptionEventChannel
    fun logger(): Logger
    fun localeProvider(): LocaleProvider
    fun spaceManager(): SpaceManager
    fun config(): ConfigStorage
}