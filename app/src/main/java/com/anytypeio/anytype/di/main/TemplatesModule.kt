package com.anytypeio.anytype.di.main

import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer.Companion.SUBSCRIPTION_TEMPLATES
import com.anytypeio.anytype.domain.search.SubscriptionEventChannel
import com.anytypeio.anytype.domain.workspace.WorkspaceManager
import com.anytypeio.anytype.presentation.templates.DefaultObjectTypeTemplatesContainer
import com.anytypeio.anytype.presentation.templates.ObjectTypeTemplatesContainer
import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Singleton

@Module
object TemplatesModule {

    @JvmStatic
    @Provides
    @Singleton
    fun provideStorelessContainer(
        repo: BlockRepository,
        channel: SubscriptionEventChannel,
        dispatchers: AppCoroutineDispatchers,
    ): StorelessSubscriptionContainer =
        StorelessSubscriptionContainer.Impl(repo, channel, dispatchers)

    @JvmStatic
    @Provides
    @Singleton
    fun provideTemplatesContainer(
        storage: StorelessSubscriptionContainer,
        workspaceManager: WorkspaceManager
    ): ObjectTypeTemplatesContainer =
        DefaultObjectTypeTemplatesContainer(
            storage = storage,
            workspaceManager = workspaceManager
        )
}