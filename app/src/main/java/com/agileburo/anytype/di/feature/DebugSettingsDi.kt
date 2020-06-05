package com.agileburo.anytype.di.feature

import com.agileburo.anytype.core_utils.di.scope.PerScreen
import com.agileburo.anytype.domain.config.GetDebugSettings
import com.agileburo.anytype.domain.config.InfrastructureRepository
import com.agileburo.anytype.domain.config.UseCustomContextMenu
import com.agileburo.anytype.ui.settings.DebugSettingsFragment
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
}