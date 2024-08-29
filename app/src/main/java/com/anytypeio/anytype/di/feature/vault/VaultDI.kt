package com.anytypeio.anytype.di.feature.vault

import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.vault.VaultViewModel
import com.anytypeio.anytype.ui.vault.VaultFragment
import dagger.Binds
import dagger.Component
import dagger.Module

@Component(
    dependencies = [VaultComponentDependencies::class],
    modules = [
        VaultModule::class,
        VaultModule.Declarations::class
    ]
)
@PerScreen
interface VaultComponent {

    fun inject(fragment: VaultFragment)

    @Component.Factory
    interface Factory {
        fun create(dependencies: VaultComponentDependencies): VaultComponent
    }
}

@Module
object VaultModule {
    @Module
    interface Declarations {
        @PerScreen
        @Binds
        fun bindViewModelFactory(
            factory: VaultViewModel.Factory
        ): ViewModelProvider.Factory
    }
}

interface VaultComponentDependencies : ComponentDependencies {
    fun blockRepository(): BlockRepository
    fun appCoroutineDispatchers(): AppCoroutineDispatchers
    fun analytics(): Analytics
    fun urlBuilder(): UrlBuilder
    fun spaceViewSubscriptionContainer(): SpaceViewSubscriptionContainer
    fun userSettingsRepository(): UserSettingsRepository
    fun spaceManager(): SpaceManager
}