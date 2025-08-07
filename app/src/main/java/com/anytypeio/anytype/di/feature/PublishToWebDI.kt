package com.anytypeio.anytype.di.feature

import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.core_utils.di.scope.PerDialog
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.presentation.publishtoweb.PublishToWebViewModel
import com.anytypeio.anytype.ui.publishtoweb.PublishToWebFragment
import dagger.Binds
import dagger.Component
import dagger.Module

@Component(
    dependencies = [PublishToWebDependencies::class],
    modules = [
        PublishToWebModule::class,
        PublishToWebModule.Declarations::class
    ]
)
@PerDialog
interface PublishToWebComponent {
    @Component.Factory
    interface Factory {
        fun create(dependency: PublishToWebDependencies): PublishToWebComponent
    }

    fun inject(fragment: PublishToWebFragment)
}

@Module
object PublishToWebModule {

    @Module
    interface Declarations {
        @PerDialog
        @Binds
        fun factory(factory: PublishToWebViewModel.Factory): ViewModelProvider.Factory
    }
}

interface PublishToWebDependencies : ComponentDependencies {
    fun repo(): BlockRepository
}
