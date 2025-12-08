package com.anytypeio.anytype.di.feature.sharing

import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_utils.di.scope.PerDialog
import com.anytypeio.anytype.data.auth.event.EventProcessDropFilesDateChannel
import com.anytypeio.anytype.data.auth.event.EventProcessDropFilesRemoteChannel
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.domain.account.AwaitAccountStartManager
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.device.FileSharer
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider
import com.anytypeio.anytype.domain.workspace.EventProcessDropFilesChannel
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.middleware.EventProxy
import com.anytypeio.anytype.middleware.interactor.EventProcessDropFilesMiddlewareChannel
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import com.anytypeio.anytype.presentation.sharing.AddToAnytypeViewModel
import com.anytypeio.anytype.ui.sharing.SharingFragment
import dagger.Binds
import dagger.Component
import dagger.Module
import dagger.Provides

@Component(
    dependencies = [AddToAnytypeDependencies::class],
    modules = [
        AddToAnytypeModule::class,
        AddToAnytypeModule.Declarations::class
    ]
)
@PerDialog
interface AddToAnytypeComponent {
    @Component.Factory
    interface Factory {
        fun create(dependency: AddToAnytypeDependencies): AddToAnytypeComponent
    }

}

@Module
object AddToAnytypeModule {

    @Provides
    @PerDialog
    fun provideEventProcessRemoteChannel(
        proxy: EventProxy
    ): EventProcessDropFilesRemoteChannel = EventProcessDropFilesMiddlewareChannel(events = proxy)

    @Provides
    @PerDialog
    fun provideEventProcessDateChannel(
        channel: EventProcessDropFilesRemoteChannel
    ): EventProcessDropFilesChannel = EventProcessDropFilesDateChannel(channel = channel)

    @Module
    interface Declarations {
        @PerDialog
        @Binds
        fun factory(factory: AddToAnytypeViewModel.Factory): ViewModelProvider.Factory
    }
}

interface AddToAnytypeDependencies : ComponentDependencies {
    fun blockRepo(): BlockRepository
    fun spaceManager(): SpaceManager
    fun dispatchers(): AppCoroutineDispatchers
    fun userSettings(): UserSettingsRepository
    fun container(): StorelessSubscriptionContainer
    fun urlBuilder(): UrlBuilder
    fun awaitAccountStartedManager(): AwaitAccountStartManager
    fun analytics(): Analytics
    fun fileSharer(): FileSharer
    fun permissions(): UserPermissionProvider
    fun analyticSpaceHelper(): AnalyticSpaceHelperDelegate
    fun eventProxy(): EventProxy
    fun spaceViewSubscriptionContainer(): SpaceViewSubscriptionContainer
}