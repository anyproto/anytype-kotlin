package com.anytypeio.anytype.di.main

import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.objects.DefaultStoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.DefaultStoreOfRelations
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.search.ObjectTypesSubscriptionContainer
import com.anytypeio.anytype.domain.search.ObjectTypesSubscriptionManager
import com.anytypeio.anytype.domain.search.RelationsSubscriptionContainer
import com.anytypeio.anytype.domain.search.RelationsSubscriptionManager
import com.anytypeio.anytype.domain.search.SubscriptionEventChannel
import com.anytypeio.anytype.domain.workspace.SpaceManager
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
object SubscriptionsModule {

    @JvmStatic
    @Provides
    @Singleton
    fun relationsSubscriptionContainer(
        repo: BlockRepository,
        channel: SubscriptionEventChannel,
        dispatchers: AppCoroutineDispatchers,
        store: StoreOfRelations
    ): RelationsSubscriptionContainer = RelationsSubscriptionContainer(
        repo = repo,
        channel = channel,
        store = store,
        dispatchers = dispatchers
    )

    @JvmStatic
    @Provides
    @Singleton
    fun objectTypesSubscriptionContainer(
        repo: BlockRepository,
        channel: SubscriptionEventChannel,
        dispatchers: AppCoroutineDispatchers,
        store: StoreOfObjectTypes
    ): ObjectTypesSubscriptionContainer = ObjectTypesSubscriptionContainer(
        repo = repo,
        channel = channel,
        store = store,
        dispatchers = dispatchers
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
        spaceManager: SpaceManager
    ): RelationsSubscriptionManager = RelationsSubscriptionManager(
        subscription = subscription,
        spaceManager = spaceManager
    )

    @JvmStatic
    @Provides
    @Singleton
    fun objectTypesSubscriptionManager(
        subscription: ObjectTypesSubscriptionContainer,
        spaceManager: SpaceManager
    ): ObjectTypesSubscriptionManager = ObjectTypesSubscriptionManager(
        subscription = subscription,
        spaceManager = spaceManager
    )
}