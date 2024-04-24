package com.anytypeio.anytype.di.feature.objects

import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import com.anytypeio.anytype.presentation.objects.SelectObjectTypeViewModel
import com.anytypeio.anytype.ui.objects.creation.SelectObjectTypeFragment
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module

@Component(
    dependencies = [SelectObjectTypeDependencies::class],
    modules = [
        SelectObjectTypeModule::class,
        SelectObjectTypeModule.Declarations::class
    ]
)
@PerScreen
interface SelectObjectTypeComponent {
    fun inject(fragment: SelectObjectTypeFragment)
    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance params: SelectObjectTypeViewModel.Params,
            dependencies: SelectObjectTypeDependencies
        ): SelectObjectTypeComponent
    }
}

@Module
object SelectObjectTypeModule {
    @Module
    interface Declarations {
        @Binds
        @PerScreen
        fun bindViewModelFactory(factory: SelectObjectTypeViewModel.Factory): ViewModelProvider.Factory
    }
}

interface SelectObjectTypeDependencies : ComponentDependencies {
    fun repo(): BlockRepository
    fun analytics(): Analytics
    fun dispatchers(): AppCoroutineDispatchers
    fun configStorage(): ConfigStorage
    fun analyticSpaceHelper(): AnalyticSpaceHelperDelegate
}