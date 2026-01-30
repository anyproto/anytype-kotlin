package com.anytypeio.anytype.di.feature

import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.core_utils.date.DateFormatter
import com.anytypeio.anytype.core_utils.di.scope.PerDialog
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.core_models.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.publishtoweb.MySitesViewModel
import com.anytypeio.anytype.ui.publishtoweb.MySitesFragment
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module

@Component(
    dependencies = [MySitesDependencies::class],
    modules = [
        MySitesModule::class,
        MySitesModule.Declarations::class
    ]
)
@PerDialog
interface MySitesComponent {

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance vmParams: MySitesViewModel.VmParams,
            dependency: MySitesDependencies
        ): MySitesComponent
    }

    fun inject(fragment: MySitesFragment)
}

@Module
object MySitesModule {

    @Module
    interface Declarations {
        @PerDialog
        @Binds
        fun factory(factory: MySitesViewModel.Factory): ViewModelProvider.Factory

        @PerDialog
        @Binds
        fun dateFormatter(formatter: DateFormatter.Basic): DateFormatter
    }
}

interface MySitesDependencies : ComponentDependencies {
    fun repo(): BlockRepository
    fun auth(): AuthRepository
    fun dispatchers(): AppCoroutineDispatchers
    fun urlBuilder(): UrlBuilder
    fun spaceViews(): SpaceViewSubscriptionContainer
    fun spaceManager(): SpaceManager
}