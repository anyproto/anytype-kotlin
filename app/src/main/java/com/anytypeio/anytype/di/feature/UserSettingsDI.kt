package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.launch.GetDefaultEditorType
import com.anytypeio.anytype.domain.launch.SetDefaultEditorType
import com.anytypeio.anytype.presentation.settings.UserSettingsViewModel
import com.anytypeio.anytype.ui.settings.UserSettingsFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@PerScreen
@Subcomponent(modules = [UserSettingsModule::class])
interface UserSettingsSubComponent {

    @Subcomponent.Builder
    interface Builder {
        fun module(module: UserSettingsModule): Builder
        fun build(): UserSettingsSubComponent
    }

    fun inject(fragment: UserSettingsFragment)
}

@Module
object UserSettingsModule {

    @JvmStatic
    @PerScreen
    @Provides
    fun provideGetDefaultPageType(repo: UserSettingsRepository): GetDefaultEditorType =
        GetDefaultEditorType(repo)

    @JvmStatic
    @PerScreen
    @Provides
    fun provideSetDefaultPageType(repo: UserSettingsRepository): SetDefaultEditorType =
        SetDefaultEditorType(repo)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideUserSettingsFabric(
        getDefaultEditorType: GetDefaultEditorType,
        setDefaultEditorType: SetDefaultEditorType,
        analytics: Analytics
    ): UserSettingsViewModel.Factory =
        UserSettingsViewModel.Factory(getDefaultEditorType, setDefaultEditorType, analytics)
}