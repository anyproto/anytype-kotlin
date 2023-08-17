package com.anytypeio.anytype.di.feature.spaces

import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.presentation.spaces.SelectSpaceViewModel
import com.anytypeio.anytype.ui.spaces.SelectSpaceFragment
import dagger.Binds
import dagger.Component
import dagger.Module

@Component(
    dependencies = [SelectSpaceDependencies::class],
    modules = [
        SelectSpaceModule::class,
        SelectSpaceModule.Declarations::class
    ]
)
@PerScreen
interface SelectSpaceComponent {
    @Component.Factory
    interface Builder {
        fun create(dependencies: SelectSpaceDependencies): SelectSpaceComponent
    }

    fun inject(fragment: SelectSpaceFragment)
}

@Module
object SelectSpaceModule {
    @Module
    interface Declarations {
        @Binds
        @PerScreen
        fun bindViewModelFactory(factory: SelectSpaceViewModel.Factory): ViewModelProvider.Factory
    }
}

interface SelectSpaceDependencies : ComponentDependencies {
    fun analytics(): Analytics
}