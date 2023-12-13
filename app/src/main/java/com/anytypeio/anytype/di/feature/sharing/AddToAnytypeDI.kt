package com.anytypeio.anytype.di.feature.sharing

import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.core_utils.di.scope.PerDialog
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.sharing.AddToAnytypeViewModel
import com.anytypeio.anytype.ui.sharing.SharingFragment
import dagger.Binds
import dagger.Component
import dagger.Module

@Component(
    dependencies = [AddToAnytypeDependencies::class],
    modules = [
        AddToAnytypeModule::class,
        AddToAnytypeModule.Declarations::class
    ]
)
@PerDialog
interface AddToAnytypeComponent {
    @Component.Factory
    interface Factory {
        fun create(dependency: AddToAnytypeDependencies): AddToAnytypeComponent
    }

    fun inject(fragment: SharingFragment)
}

@Module
object AddToAnytypeModule {
    @Module
    interface Declarations {
        @PerDialog
        @Binds
        fun factory(factory: AddToAnytypeViewModel.Factory): ViewModelProvider.Factory
    }
}

interface AddToAnytypeDependencies : ComponentDependencies {
    fun blockRepo(): BlockRepository
    fun spaceManager(): SpaceManager
    fun dispatchers(): AppCoroutineDispatchers
    fun userSettings(): UserSettingsRepository
    fun configStorage(): ConfigStorage
}