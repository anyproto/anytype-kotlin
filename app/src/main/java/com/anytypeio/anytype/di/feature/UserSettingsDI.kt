package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.launch.GetDefaultPageType
import com.anytypeio.anytype.domain.launch.SetDefaultPageType
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
    fun provideGetDefaultPageType(repo: UserSettingsRepository): GetDefaultPageType =
        GetDefaultPageType(repo)

    @JvmStatic
    @PerScreen
    @Provides
    fun provideSetDefaultPageType(repo: UserSettingsRepository): SetDefaultPageType =
        SetDefaultPageType(repo)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideUserSettingsFabric(
        getDefaultPageType: GetDefaultPageType,
        setDefaultPageType: SetDefaultPageType,
        analytics: Analytics
    ): UserSettingsViewModel.Factory =
        UserSettingsViewModel.Factory(getDefaultPageType, setDefaultPageType, analytics)
}