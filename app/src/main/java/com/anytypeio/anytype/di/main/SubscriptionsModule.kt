package com.anytypeio.anytype.di.main

import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.di.main.ConfigModule.DEFAULT_APP_COROUTINE_SCOPE
import com.anytypeio.anytype.domain.account.AwaitAccountStartManager
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.interactor.sets.GetObjectTypes
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.debugging.DebugAccountSelectTrace
import com.anytypeio.anytype.domain.debugging.Logger
import com.anytypeio.anytype.domain.event.interactor.SpaceSyncAndP2PStatusProvider
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.multiplayer.ActiveSpaceMemberSubscriptionContainer
import com.anytypeio.anytype.domain.multiplayer.DefaultUserPermissionProvider
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider
import com.anytypeio.anytype.domain.objects.DefaultStoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.DefaultStoreOfRelations
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.search.ObjectTypesSubscriptionContainer
import com.anytypeio.anytype.domain.search.ObjectTypesSubscriptionManager
import com.anytypeio.anytype.domain.search.ProfileSubscriptionManager
import com.anytypeio.anytype.domain.search.RelationsSubscriptionContainer
import com.anytypeio.anytype.domain.search.RelationsSubscriptionManager
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.domain.search.SubscriptionEventChannel
import com.anytypeio.anytype.domain.spaces.SpaceDeletedStatusWatcher
import com.anytypeio.anytype.domain.subscriptions.GlobalSubscriptionManager
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.domain.workspace.SyncAndP2PStatusChannel
import com.anytypeio.anytype.presentation.sync.SpaceSyncAndP2PStatusProviderImpl
import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope

@Module
object SubscriptionsModule {

    @JvmStatic
    @Provides
    @Singleton
    fun relationsSubscriptionContainer(
        repo: BlockRepository,
        channel: SubscriptionEventChannel,
        dispatchers: AppCoroutineDispatchers,
        store: StoreOfRelations,
        logger: Logger
    ): RelationsSubscriptionContainer = RelationsSubscriptionContainer(
        repo = repo,
        channel = channel,
        store = store,
        dispatchers = dispatchers,
        logger = logger
    )

    @JvmStatic
    @Provides
    @Singleton
    fun objectTypesSubscriptionContainer(
        repo: BlockRepository,
        channel: SubscriptionEventChannel,
        dispatchers: AppCoroutineDispatchers,
        store: StoreOfObjectTypes,
        logger: Logger
    ): ObjectTypesSubscriptionContainer = ObjectTypesSubscriptionContainer(
        repo = repo,
        channel = channel,
        store = store,
        dispatchers = dispatchers,
        logger = logger
    )

    @JvmStatic
    @Provides
    @Singleton
    fun relationsStore(): StoreOfRelations = DefaultStoreOfRelations()

    @JvmStatic
    @Provides
    @Singleton
    fun objectTypesStore(): StoreOfObjectTypes = DefaultStoreOfObjectTypes()

    @JvmStatic
    @Provides
    @Singleton
    fun relationsSubscriptionManager(
        subscription: RelationsSubscriptionContainer,
        spaceManager: SpaceManager,
    ): RelationsSubscriptionManager = RelationsSubscriptionManager(
        container = subscription,
        spaceManager = spaceManager
    )

    @JvmStatic
    @Provides
    @Singleton
    fun objectTypesSubscriptionManager(
        subscription: ObjectTypesSubscriptionContainer,
        spaceManager: SpaceManager
    ): ObjectTypesSubscriptionManager = ObjectTypesSubscriptionManager(
        container = subscription,
        spaceManager = spaceManager
    )

    @JvmStatic
    @Provides
    @Singleton
    fun spaceStatusWatcher(
        spaceManager: SpaceManager,
        dispatchers: AppCoroutineDispatchers,
        @Named(DEFAULT_APP_COROUTINE_SCOPE) scope: CoroutineScope,
        configStorage: ConfigStorage,
        container: StorelessSubscriptionContainer,
        logger: Logger
    ): SpaceDeletedStatusWatcher = SpaceDeletedStatusWatcher(
        spaceManager = spaceManager,
        dispatchers = dispatchers,
        configStorage = configStorage,
        scope = scope,
        container = container,
        logger = logger
    )

    @JvmStatic
    @Provides
    @Singleton
    fun userPermissionProvider(
        dispatchers: AppCoroutineDispatchers,
        @Named(DEFAULT_APP_COROUTINE_SCOPE) scope: CoroutineScope,
        container: StorelessSubscriptionContainer,
        repo: AuthRepository,
        logger: Logger,
        spaceViewSubscriptionContainer: SpaceViewSubscriptionContainer
    ): UserPermissionProvider = DefaultUserPermissionProvider(
        dispatchers = dispatchers,
        scope = scope,
        container = container,
        repo = repo,
        logger = logger,
        spaceViewSubscriptionContainer = spaceViewSubscriptionContainer
    )

    @JvmStatic
    @Provides
    @Singleton
    fun spaceViewSubscriptionContainer(
        dispatchers: AppCoroutineDispatchers,
        @Named(DEFAULT_APP_COROUTINE_SCOPE) scope: CoroutineScope,
        container: StorelessSubscriptionContainer,
        awaitAccountStartManager: AwaitAccountStartManager,
        logger: Logger,
        configStorage: ConfigStorage
    ): SpaceViewSubscriptionContainer = SpaceViewSubscriptionContainer.Default(
        dispatchers = dispatchers,
        scope = scope,
        container = container,
        awaitAccountStart = awaitAccountStartManager,
        logger = logger,
        config = configStorage
    )

    @JvmStatic
    @Provides
    @Singleton
    fun profileSubscriptionManager(
        dispatchers: AppCoroutineDispatchers,
        @Named(DEFAULT_APP_COROUTINE_SCOPE) scope: CoroutineScope,
        container: StorelessSubscriptionContainer,
        awaitAccountStartManager: AwaitAccountStartManager,
        configStorage: ConfigStorage,
        logger: Logger
    ): ProfileSubscriptionManager = ProfileSubscriptionManager.Default(
        dispatchers = dispatchers,
        scope = scope,
        container = container,
        awaitAccountStartManager = awaitAccountStartManager,
        configStorage = configStorage,
        logger = logger
    )

    @JvmStatic
    @Provides
    @Singleton
    fun activeSpaceMemberSubscriptionContainer(
        dispatchers: AppCoroutineDispatchers,
        @Named(DEFAULT_APP_COROUTINE_SCOPE) scope: CoroutineScope,
        container: StorelessSubscriptionContainer,
        spaceManager: SpaceManager,
        awaitAccountStartManager: AwaitAccountStartManager,
        logger: Logger
    ): ActiveSpaceMemberSubscriptionContainer = ActiveSpaceMemberSubscriptionContainer.Default(
        dispatchers = dispatchers,
        scope = scope,
        container = container,
        manager = spaceManager,
        awaitAccountStart = awaitAccountStartManager,
        logger = logger
    )

    @JvmStatic
    @Provides
    @Singleton
    fun provideSpaceSyncStatusProvider(
        syncChannel: SyncAndP2PStatusChannel,
        spaceManager: SpaceManager
    ): SpaceSyncAndP2PStatusProvider = SpaceSyncAndP2PStatusProviderImpl(
        spaceManager = spaceManager,
        spaceSyncStatusChannel = syncChannel
    )

    @JvmStatic
    @Provides
    @Singleton
    fun globalSubscriptionManager(
        types: ObjectTypesSubscriptionManager,
        relations: RelationsSubscriptionManager,
        permissions: UserPermissionProvider,
        isSpaceDeleted: SpaceDeletedStatusWatcher,
        profileSubscriptionManager: ProfileSubscriptionManager
    ): GlobalSubscriptionManager = GlobalSubscriptionManager.Default(
        types = types,
        relations = relations,
        permissions = permissions,
        isSpaceDeleted = isSpaceDeleted,
        profile = profileSubscriptionManager
    )

    @JvmStatic
    @Provides
    @Singleton
    fun searchObjects(
        repo: BlockRepository
    ): SearchObjects = SearchObjects(repo = repo)

    @JvmStatic
    @PerScreen
    @Singleton
    fun getObjectTypes(
        repo: BlockRepository,
        dispatchers: AppCoroutineDispatchers
    ): GetObjectTypes = GetObjectTypes(repo, dispatchers)

    @JvmStatic
    @Provides
    @Singleton
    fun provideDebugAccountSelectTrace(
        repo: BlockRepository,
        dispatchers: AppCoroutineDispatchers
    ): DebugAccountSelectTrace =
        DebugAccountSelectTrace(
            repo = repo,
            dispatchers = dispatchers
        )
}