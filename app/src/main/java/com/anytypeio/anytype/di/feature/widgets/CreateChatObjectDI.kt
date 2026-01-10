package com.anytypeio.anytype.di.feature.widgets

import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.presentation.widgets.CreateChatObjectViewModel
import com.anytypeio.anytype.ui.widgets.CreateChatObjectFragment
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module

@Component(
    dependencies = [CreateChatObjectDependencies::class],
    modules = [
        CreateChatObjectModule::class,
        CreateChatObjectModule.Declarations::class
    ]
)
@PerScreen
interface CreateChatObjectComponent {
    @Component.Factory
    interface Builder {
        fun create(
            @BindsInstance vmParams: CreateChatObjectViewModel.VmParams,
            dependencies: CreateChatObjectDependencies
        ): CreateChatObjectComponent
    }

    fun inject(fragment: CreateChatObjectFragment)
}

@Module
object CreateChatObjectModule {
    @Module
    interface Declarations {
        @Binds
        @PerScreen
        fun bindViewModelFactory(factory: CreateChatObjectViewModel.Factory): ViewModelProvider.Factory
    }
}

interface CreateChatObjectDependencies : ComponentDependencies {
    fun analytics(): Analytics
    fun repo(): BlockRepository
    fun dispatchers(): AppCoroutineDispatchers
    fun settings(): UserSettingsRepository
}
