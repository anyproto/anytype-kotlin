package com.anytypeio.anytype.di.feature.settings

import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.presentation.settings.ExperimentalFeaturesViewModel
import com.anytypeio.anytype.ui.settings.ExperimentalFeaturesFragment
import dagger.Binds
import dagger.Component
import dagger.Module

@Component(
    dependencies = [ExperimentalFeaturesDependencies::class],
    modules = [
        ExperimentalFeaturesModule::class,
        ExperimentalFeaturesModule.Declarations::class
    ]
)
@PerScreen
interface ExperimentalFeaturesComponent {

    @Component.Factory
    interface Factory {
        fun create(dependencies: ExperimentalFeaturesDependencies): ExperimentalFeaturesComponent
    }

    fun inject(fragment: ExperimentalFeaturesFragment)
}

@Module
object ExperimentalFeaturesModule {
    @Module
    interface Declarations {
        @PerScreen
        @Binds
        fun bindViewModelFactory(
            factory: ExperimentalFeaturesViewModel.Factory
        ): ViewModelProvider.Factory
    }
}

interface ExperimentalFeaturesDependencies : ComponentDependencies {
    fun userSettingsRepository(): UserSettingsRepository
}
