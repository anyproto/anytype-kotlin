package com.anytypeio.anytype.di.main

import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.debugging.Logger
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.domain.workspace.WorkspaceManager
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
object WorkspaceModule {
    @Provides
    @Singleton
    fun manager() : WorkspaceManager = WorkspaceManager.DefaultWorkspaceManager()

    @Provides
    @Singleton
    fun spaces(
        repo: BlockRepository,
        dispatchers: AppCoroutineDispatchers,
        configStorage: ConfigStorage,
        logger: Logger
    ) : SpaceManager = SpaceManager.Impl(
        dispatchers = dispatchers,
        repo = repo,
        configStorage = configStorage,
        logger = logger
    )
}