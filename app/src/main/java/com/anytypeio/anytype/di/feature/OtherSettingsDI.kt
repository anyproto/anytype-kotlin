package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.device.ClearFileCache
import com.anytypeio.anytype.domain.launch.GetDefaultEditorType
import com.anytypeio.anytype.domain.launch.SetDefaultEditorType
import com.anytypeio.anytype.domain.misc.AppActionManager
import com.anytypeio.anytype.presentation.settings.OtherSettingsViewModel
import com.anytypeio.anytype.ui.settings.OtherSettingsFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@PerScreen
@Subcomponent(modules = [OtherSettingsModule::class])
interface OtherSettingsSubComponent {

    @Subcomponent.Builder
    interface Builder {
        fun module(module: OtherSettingsModule): Builder
        fun build(): OtherSettingsSubComponent
    }

    fun inject(fragment: OtherSettingsFragment)
}

@Module
object OtherSettingsModule {

    @JvmStatic
    @PerScreen
    @Provides
    fun provideGetDefaultPageType(
        repo: UserSettingsRepository,
        dispatchers: AppCoroutineDispatchers
    ): GetDefaultEditorType =
        GetDefaultEditorType(repo, dispatchers)

    @JvmStatic
    @PerScreen
    @Provides
    fun provideSetDefaultPageType(repo: UserSettingsRepository): SetDefaultEditorType =
        SetDefaultEditorType(repo)

    @JvmStatic
    @PerScreen
    @Provides
    fun provideClearFileCache(repo: BlockRepository): ClearFileCache = ClearFileCache(repo)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideOtherSettingsFactory(
        getDefaultEditorType: GetDefaultEditorType,
        setDefaultEditorType: SetDefaultEditorType,
        clearFileCache: ClearFileCache,
        appActionManager: AppActionManager,
        analytics: Analytics
    ): OtherSettingsViewModel.Factory = OtherSettingsViewModel.Factory(
        getDefaultEditorType = getDefaultEditorType,
        setDefaultEditorType = setDefaultEditorType,
        clearFileCache = clearFileCache,
        appActionManager = appActionManager,
        analytics = analytics
    )
}