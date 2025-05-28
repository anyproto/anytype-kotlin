package com.anytypeio.anytype.di.main

import com.anytypeio.anytype.data.auth.status.SyncAndP2PStatusDataChannel
import com.anytypeio.anytype.data.auth.status.SyncAndP2PStatusEventsStore
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.debugging.Logger
import com.anytypeio.anytype.domain.notifications.NotificationBuilder
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.domain.workspace.SyncAndP2PStatusChannel
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
object WorkspaceModule {

    @JvmStatic
    @Provides
    @Singleton
    fun spaces(
        repo: BlockRepository,
        dispatchers: AppCoroutineDispatchers,
        logger: Logger,
        notificationBuilder: NotificationBuilder
    ): SpaceManager = SpaceManager.Impl(
        dispatchers = dispatchers,
        repo = repo,
        logger = logger,
        notificationBuilder = notificationBuilder
    )

    @JvmStatic
    @Provides
    @Singleton
    fun spaceSyncStatusChannel(
        store: SyncAndP2PStatusEventsStore
    ): SyncAndP2PStatusChannel = SyncAndP2PStatusDataChannel(store)
}