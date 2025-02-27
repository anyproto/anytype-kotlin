package com.anytypeio.anytype.di.feature.settings

import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.domain.device.PathProvider
import com.anytypeio.anytype.presentation.settings.DebugViewModel
import com.anytypeio.anytype.ui.settings.DebugFragment
import dagger.Binds
import dagger.Component
import dagger.Module

@Component(
    dependencies = [DebugDependencies::class],
    modules = [
        DebugModule::class,
        DebugModule.Declarations::class
    ]
)
@PerScreen
interface DebugComponent {

    @Component.Factory
    interface Factory {
        fun create(dependencies: DebugDependencies): DebugComponent
    }

    fun inject(fragment: DebugFragment)
}

@Module
object DebugModule {
    @Module
    interface Declarations {
        @PerScreen
        @Binds
        fun bindViewModelFactory(
            factory: DebugViewModel.Factory
        ): ViewModelProvider.Factory
    }
}

interface DebugDependencies : ComponentDependencies {
    fun path(): PathProvider
}