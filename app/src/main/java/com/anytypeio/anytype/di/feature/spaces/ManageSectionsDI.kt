package com.anytypeio.anytype.di.feature.spaces

import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.spaces.ManageSectionsViewModel
import com.anytypeio.anytype.ui.settings.space.ManageSectionsFragment
import dagger.Binds
import dagger.Component
import dagger.Module

@Component(
    dependencies = [ManageSectionsDependencies::class],
    modules = [ManageSectionsModule.Bindings::class]
)
@PerScreen
interface ManageSectionsComponent {
    @Component.Factory
    interface Factory {
        fun create(
            dependencies: ManageSectionsDependencies
        ): ManageSectionsComponent
    }

    fun inject(fragment: ManageSectionsFragment)
}

@Module
object ManageSectionsModule {

    @Module
    interface Bindings {
        @PerScreen
        @Binds
        fun factory(factory: ManageSectionsViewModel.Factory): ViewModelProvider.Factory
    }
}

interface ManageSectionsDependencies : ComponentDependencies {
    fun spaceManager(): SpaceManager
    fun settings(): UserSettingsRepository
    fun analytics(): Analytics
    fun dispatchers(): AppCoroutineDispatchers
}
