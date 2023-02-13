package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.GetDebugSettings
import com.anytypeio.anytype.domain.config.InfrastructureRepository
import com.anytypeio.anytype.domain.config.UseCustomContextMenu
import com.anytypeio.anytype.domain.debugging.DebugLocalStore
import com.anytypeio.anytype.domain.debugging.DebugSync
import com.anytypeio.anytype.ui.settings.DebugSettingsFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@Subcomponent(modules = [DebugSettingsModule::class])
@PerScreen
interface DebugSettingsSubComponent {

    @Subcomponent.Builder
    interface Builder {
        fun module(module: DebugSettingsModule): Builder
        fun build(): DebugSettingsSubComponent
    }

    fun inject(fragment: DebugSettingsFragment)
}

@Module
class DebugSettingsModule {

    @Provides
    @PerScreen
    fun provideUseCustomContextMenu(repo: InfrastructureRepository): UseCustomContextMenu =
        UseCustomContextMenu(repo)

    @Provides
    @PerScreen
    fun provideGetDebugSettings(
        repo: InfrastructureRepository
    ): GetDebugSettings = GetDebugSettings(
        repo = repo
    )

    @Provides
    @PerScreen
    fun provideDebugSync(
        repo: BlockRepository,
        dispatchers: AppCoroutineDispatchers
    ): DebugSync = DebugSync(repo = repo, dispatchers = dispatchers)

    @Provides
    @PerScreen
    fun provideDebugLocalStore(repo: BlockRepository) : DebugLocalStore = DebugLocalStore(repo)
}