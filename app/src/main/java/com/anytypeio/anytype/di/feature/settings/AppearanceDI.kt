package com.anytypeio.anytype.di.feature.settings

import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.theme.GetTheme
import com.anytypeio.anytype.domain.theme.SetTheme
import com.anytypeio.anytype.ui.settings.AppearanceFragment
import com.anytypeio.anytype.ui_settings.appearance.AppearanceViewModel
import dagger.Component
import com.anytypeio.anytype.ui_settings.appearance.ThemeApplicator
import com.anytypeio.anytype.ui_settings.appearance.ThemeApplicatorImpl
import dagger.Binds
import dagger.Module
import dagger.Provides

@Component(
    dependencies = [AppearanceDependencies::class],
    modules = [
        AppearanceModule::class,
        AppearanceModule.Declarations::class
    ]
)
@PerScreen
interface AppearanceComponent {

    @Component.Factory
    interface Factory {
        fun create(dependencies: AppearanceDependencies): AppearanceComponent
    }

    fun inject(fragment: AppearanceFragment)
}

@Module
object AppearanceModule {

    @JvmStatic
    @Provides
    @PerScreen
    fun provideGetThemeUseCase(repo: UserSettingsRepository): GetTheme = GetTheme(repo)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideSetThemeUseCase(repo: UserSettingsRepository): SetTheme = SetTheme(repo)

    @Module
    interface Declarations {

        @PerScreen
        @Binds
        fun bindViewModelFactory(
            factory: AppearanceViewModel.Factory
        ): ViewModelProvider.Factory

        @PerScreen
        @Binds
        fun bindThemeApplicator(applicator: ThemeApplicatorImpl): ThemeApplicator
    }
}

interface AppearanceDependencies : ComponentDependencies {
    fun userUserSettingsRepository(): UserSettingsRepository
}