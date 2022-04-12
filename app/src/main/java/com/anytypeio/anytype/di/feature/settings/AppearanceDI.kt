package com.anytypeio.anytype.di.feature.settings

import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.theme.GetTheme
import com.anytypeio.anytype.domain.theme.SetTheme
import com.anytypeio.anytype.ui.settings.AppearanceFragment
import com.anytypeio.anytype.ui_settings.appearance.AppearanceViewModel
import com.anytypeio.anytype.ui_settings.appearance.ThemeApplicator
import com.anytypeio.anytype.ui_settings.appearance.ThemeApplicatorImpl
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@Subcomponent(modules = [AppearanceModule::class])
@PerScreen
interface AppearanceSubComponent {

    @Subcomponent.Builder
    interface Builder {
        fun module(module: AppearanceModule): Builder
        fun build(): AppearanceSubComponent
    }

    fun inject(fragment: AppearanceFragment)
}

@Module
object AppearanceModule {
    @JvmStatic
    @Provides
    @PerScreen
    fun provideViewModelFactory(
        getTheme: GetTheme,
        setTheme: SetTheme,
        themeApplicator: ThemeApplicator
    ): AppearanceViewModel.Factory = AppearanceViewModel.Factory(
        getTheme,
        setTheme,
        themeApplicator
    )

    @JvmStatic
    @PerScreen
    @Provides
    fun provideThemeApplicator(): ThemeApplicator = ThemeApplicatorImpl()

    @JvmStatic
    @Provides
    @PerScreen
    fun provideGetThemeUseCase(repo: UserSettingsRepository): GetTheme = GetTheme(repo)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideSetThemeUseCase(repo: UserSettingsRepository): SetTheme = SetTheme(repo)
}