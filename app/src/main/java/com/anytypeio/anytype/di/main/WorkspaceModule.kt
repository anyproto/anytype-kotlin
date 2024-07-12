package com.anytypeio.anytype.di.main

import com.anytypeio.anytype.data.auth.status.P2PStatusDataChannel
import com.anytypeio.anytype.data.auth.status.P2PStatusRemoteChannel
import com.anytypeio.anytype.data.auth.status.SpaceStatusDataChannel
import com.anytypeio.anytype.data.auth.status.SpaceStatusRemoteChannel
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.debugging.Logger
import com.anytypeio.anytype.domain.workspace.P2PStatusChannel
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.domain.workspace.SpaceSyncStatusChannel
import com.anytypeio.anytype.domain.workspace.WorkspaceManager
import com.anytypeio.anytype.middleware.EventProxy
import com.anytypeio.anytype.middleware.interactor.P2PStatusRemoteChannelImpl
import com.anytypeio.anytype.middleware.interactor.SpaceSyncStatusRemoteChannelImpl
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
object WorkspaceModule {
    @Provides
    @Singleton
    fun manager(): WorkspaceManager = WorkspaceManager.DefaultWorkspaceManager()

    @Provides
    @Singleton
    fun spaces(
        repo: BlockRepository,
        dispatchers: AppCoroutineDispatchers,
        configStorage: ConfigStorage,
        logger: Logger
    ): SpaceManager = SpaceManager.Impl(
        dispatchers = dispatchers,
        repo = repo,
        configStorage = configStorage,
        logger = logger
    )

    @JvmStatic
    @Provides
    @Singleton
    fun provideSyncStatusRemoteChannel(
        proxy: EventProxy
    ): SpaceStatusRemoteChannel = SpaceSyncStatusRemoteChannelImpl(events = proxy)

    @Provides
    @Singleton
    fun spaceSyncStatusChannel(
        channel: SpaceStatusRemoteChannel
    ): SpaceSyncStatusChannel = SpaceStatusDataChannel(channel)

    @JvmStatic
    @Provides
    @Singleton
    fun p2pStatusRemoteChannel(
        proxy: EventProxy
    ): P2PStatusRemoteChannel = P2PStatusRemoteChannelImpl(events = proxy)

    @JvmStatic
    @Provides
    @Singleton
    fun p2pStatusChannel(
        channel: P2PStatusRemoteChannel
    ): P2PStatusChannel = P2PStatusDataChannel(channel)
}