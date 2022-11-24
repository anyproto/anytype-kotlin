package com.anytypeio.anytype.di.main

import com.anytypeio.anytype.domain.workspace.WorkspaceManager
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
object WorkspaceModule {
    @Provides
    @Singleton
    fun manager() : WorkspaceManager = WorkspaceManager.DefaultWorkspaceManager()
}