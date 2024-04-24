package com.anytypeio.anytype.di.feature.notifications

import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_utils.di.scope.PerDialog
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import com.anytypeio.anytype.presentation.notifications.NotificationsProvider
import com.anytypeio.anytype.presentation.notifications.NotificationsViewModelFactory
import com.anytypeio.anytype.ui.notifications.NotificationsFragment
import dagger.Binds
import dagger.Component
import dagger.Module

@Component(
    dependencies = [NotificationDependencies::class],
    modules = [
        NotificationsModule::class,
        NotificationsModule.Declarations::class
    ]
)
@PerDialog
interface NotificationComponent {
    @Component.Factory
    interface Factory {
        fun create(dependency: NotificationDependencies): NotificationComponent
    }

    fun inject(fragment: NotificationsFragment)
}

@Module
object NotificationsModule {
    @Module
    interface Declarations {
        @PerDialog
        @Binds
        fun factory(factory: NotificationsViewModelFactory): ViewModelProvider.Factory
    }
}

interface NotificationDependencies : ComponentDependencies {
    fun repo(): BlockRepository
    fun analytics(): Analytics
    fun notificationsProvider(): NotificationsProvider
    fun spaceManager(): SpaceManager
    fun userSettingsRepository(): UserSettingsRepository
    fun provideDispatchers(): AppCoroutineDispatchers
    fun analyticSpaceHelper(): AnalyticSpaceHelperDelegate
}