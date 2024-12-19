package com.anytypeio.anytype.di.feature

import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.debugging.Logger
import com.anytypeio.anytype.domain.event.interactor.EventChannel
import com.anytypeio.anytype.domain.event.interactor.InterceptEvents
import com.anytypeio.anytype.domain.event.interactor.SpaceSyncAndP2PStatusProvider
import com.anytypeio.anytype.domain.launch.GetDefaultObjectType
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.DateProvider
import com.anytypeio.anytype.domain.misc.LocaleProvider
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider
import com.anytypeio.anytype.domain.`object`.OpenObject
import com.anytypeio.anytype.domain.`object`.SetObjectDetails
import com.anytypeio.anytype.domain.objects.ObjectWatcher
import com.anytypeio.anytype.domain.objects.SetObjectListIsArchived
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.page.CreateObject
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.domain.search.SubscriptionEventChannel
import com.anytypeio.anytype.feature_object_type.viewmodel.ObjectTypeVMFactory
import com.anytypeio.anytype.feature_object_type.viewmodel.ObjectTypeVmParams
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import com.anytypeio.anytype.presentation.navigation.DeepLinkToObjectDelegate
import com.anytypeio.anytype.presentation.widgets.DefaultObjectViewReducer
import com.anytypeio.anytype.ui.primitives.ObjectTypeFragment
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.Dispatchers

@Component(
    dependencies = [ObjectTypeDependencies::class],
    modules = [
        ObjectTypeModule::class,
        ObjectTypeModule.Declarations::class
    ]
)
@PerScreen
interface ObjectTypeComponent {
    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance vmParams: ObjectTypeVmParams,
            dependencies: ObjectTypeDependencies
        ): ObjectTypeComponent
    }

    fun inject(fragment: ObjectTypeFragment)
}

@Module
object ObjectTypeModule {

    @JvmStatic
    @Provides
    @PerScreen
    fun openObject(
        repo: BlockRepository,
        dispatchers: AppCoroutineDispatchers,
        settings: UserSettingsRepository,
    ): OpenObject = OpenObject(
        repo = repo,
        dispatchers = dispatchers,
        settings = settings
    )

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
    ): CreateObject = CreateObject(
        repo = repo,
        getDefaultObjectType = getDefaultObjectType,
        dispatchers = dispatchers
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideGetDefaultPageType(
        userSettingsRepository: UserSettingsRepository,
        blockRepository: BlockRepository,
        dispatchers: AppCoroutineDispatchers
    ): GetDefaultObjectType = GetDefaultObjectType(
        userSettingsRepository = userSettingsRepository,
        blockRepository = blockRepository,
        dispatchers = dispatchers
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideUpdateDetailUseCase(
        repository: BlockRepository,
        dispatchers: AppCoroutineDispatchers
    ): SetObjectDetails = SetObjectDetails(repository, dispatchers)

    @JvmStatic
    @PerScreen
    @Provides
    fun getSetObjectListIsArchived(
        repo: BlockRepository,
        dispatchers: AppCoroutineDispatchers
    ): SetObjectListIsArchived = SetObjectListIsArchived(repo, dispatchers)

    @JvmStatic
    @PerScreen
    @Provides
    fun provideObjectWatcher(
        repo: BlockRepository,
        events: EventChannel,
        reducer: ObjectWatcher.Reducer
    ): ObjectWatcher = ObjectWatcher(repo, events, reducer)

    @Module
    interface Declarations {
        @PerScreen
        @Binds
        fun bindViewModelFactory(
            factory: ObjectTypeVMFactory
        ): ViewModelProvider.Factory

        @PerScreen
        @Binds
        fun objectWatcherReducer(
            default: DefaultObjectViewReducer
        ): ObjectWatcher.Reducer

        @PerScreen
        @Binds
        fun deepLinkToObjectDelegate(
            default: DeepLinkToObjectDelegate.Default
        ) : DeepLinkToObjectDelegate
    }
}

interface ObjectTypeDependencies : ComponentDependencies {
    fun blockRepository(): BlockRepository
    fun analytics(): Analytics
    fun urlBuilder(): UrlBuilder
    fun dispatchers(): AppCoroutineDispatchers
    fun storeOfObjectTypes(): StoreOfObjectTypes
    fun analyticsHelper(): AnalyticSpaceHelperDelegate
    fun subEventChannel(): SubscriptionEventChannel
    fun logger(): Logger
    fun localeProvider(): LocaleProvider
    fun config(): ConfigStorage
    fun userPermissionProvider(): UserPermissionProvider
    fun provideStoreOfRelations(): StoreOfRelations
    fun provideDateProvider(): DateProvider
    fun provideSpaceSyncAndP2PStatusProvider(): SpaceSyncAndP2PStatusProvider
    fun provideUserSettingsRepository(): UserSettingsRepository
    fun fieldParser(): FieldParser
    fun provideEventChannel(): EventChannel
}