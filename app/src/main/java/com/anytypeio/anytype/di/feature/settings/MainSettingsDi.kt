package com.anytypeio.anytype.di.feature.settings

import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.presentation.settings.MainSettingsViewModel
import com.anytypeio.anytype.ui.settings.MainSettingFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@Subcomponent(modules = [MainSettingsModule::class])
@PerScreen
interface MainSettingsSubComponent {

    @Subcomponent.Builder
    interface Builder {
        fun module(module: MainSettingsModule): Builder
        fun build(): MainSettingsSubComponent
    }

    fun inject(fragment: MainSettingFragment)
}

@Module
object MainSettingsModule {

    @JvmStatic
    @Provides
    @PerScreen
    fun provideViewModelFactory(
        analytics: Analytics
    ): MainSettingsViewModel.Factory = MainSettingsViewModel.Factory(analytics)
}