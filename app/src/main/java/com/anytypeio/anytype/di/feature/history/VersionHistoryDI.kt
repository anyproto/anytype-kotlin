package com.anytypeio.anytype.di.feature.history

import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.misc.DateProvider
import com.anytypeio.anytype.domain.misc.LocaleProvider
import com.anytypeio.anytype.presentation.history.VersionHistoryVMFactory
import com.anytypeio.anytype.presentation.history.VersionHistoryViewModel
import com.anytypeio.anytype.ui.history.VersionHistoryFragment
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module

@Component(
    dependencies = [VersionHistoryComponentDependencies::class],
    modules = [
        VersionHistoryModule::class,
        VersionHistoryModule.Declarations::class
    ]
)
@PerScreen
interface VersionHistoryComponent {

    @Component.Factory
    interface Factory {
        fun create(
            dependencies: VersionHistoryComponentDependencies,
            @BindsInstance vmParams: VersionHistoryViewModel.VmParams
        ): VersionHistoryComponent
    }

    fun inject(fragment: VersionHistoryFragment)
}

@Module
object VersionHistoryModule {

    @Module
    interface Declarations {

        @PerScreen
        @Binds
        fun bindViewModelFactory(
            factory: VersionHistoryVMFactory
        ): ViewModelProvider.Factory
    }
}

interface VersionHistoryComponentDependencies : ComponentDependencies {
    fun analytics(): Analytics
    fun blockRepository(): BlockRepository
    fun appCoroutineDispatchers(): AppCoroutineDispatchers
    fun dateProvider(): DateProvider
    fun localeProvider(): LocaleProvider
}

