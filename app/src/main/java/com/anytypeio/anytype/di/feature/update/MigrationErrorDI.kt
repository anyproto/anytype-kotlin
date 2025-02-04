package com.anytypeio.anytype.di.feature.update

import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.device.PathProvider
import com.anytypeio.anytype.presentation.auth.account.MigrationHelperDelegate
import com.anytypeio.anytype.presentation.update.MigrationErrorViewModel
import com.anytypeio.anytype.ui.update.MigrationErrorFragment
import dagger.Binds
import dagger.Component
import dagger.Module

@Component(
    dependencies = [MigrationErrorDependencies::class],
    modules = [
        MigrationErrorModule::class,
        MigrationErrorModule.Declarations::class
    ]
)
@PerScreen
interface MigrationErrorComponent {

    @Component.Factory
    interface Factory {
        fun create(dependencies: MigrationErrorDependencies): MigrationErrorComponent
    }

    fun inject(fragment: MigrationErrorFragment)
}

@Module
object MigrationErrorModule {

    @Module
    interface Declarations {

        @PerScreen
        @Binds
        fun bindViewModelFactory(
            factory: MigrationErrorViewModel.Factory
        ): ViewModelProvider.Factory

        @Binds
        @PerScreen
        fun bindMigrationHelperDelegate(
            impl: MigrationHelperDelegate.Impl
        ): MigrationHelperDelegate
    }
}

interface MigrationErrorDependencies : ComponentDependencies {
    fun analytics(): Analytics
    fun auth(): AuthRepository
    fun path(): PathProvider
    fun dispatcher(): AppCoroutineDispatchers
}