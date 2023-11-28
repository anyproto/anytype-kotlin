package com.anytypeio.anytype.di.feature.objects

import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.objects.CreateObjectOfTypeViewModel
import com.anytypeio.anytype.ui.objects.creation.CreateObjectOfTypeFragment
import dagger.Binds
import dagger.Component
import dagger.Module

@Component(
    dependencies = [CreateObjectOfTypeDependencies::class],
    modules = [
        CreateObjectOfTypeModule::class,
        CreateObjectOfTypeModule.Declarations::class
    ]
)
@PerScreen
interface CreateObjectOfTypeComponent {
    @Component.Factory
    interface Builder {
        fun create(dependencies: CreateObjectOfTypeDependencies): CreateObjectOfTypeComponent
    }

        fun inject(fragment: CreateObjectOfTypeFragment)
}

@Module
object CreateObjectOfTypeModule {
    @Module
    interface Declarations {
        @Binds
        @PerScreen
        fun bindViewModelFactory(factory: CreateObjectOfTypeViewModel.Factory): ViewModelProvider.Factory
    }
}

interface CreateObjectOfTypeDependencies : ComponentDependencies {
    fun repo(): BlockRepository
    fun analytics(): Analytics
    fun dispatchers(): AppCoroutineDispatchers
    fun spaceManager(): SpaceManager
}