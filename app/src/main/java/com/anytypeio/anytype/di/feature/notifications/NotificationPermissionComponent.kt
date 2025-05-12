package com.anytypeio.anytype.di.feature.notifications

import android.content.SharedPreferences
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.presentation.notifications.NotificationPermissionManager
import com.anytypeio.anytype.presentation.notifications.NotificationPermissionManagerImpl
import com.anytypeio.anytype.ui.notifications.NotificationPermissionRequestDialog
import dagger.Component
import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Singleton

@Singleton
@Component(
    dependencies = [NotificationPermissionDependencies::class],
    modules = [NotificationPermissionModule::class],
)
interface NotificationPermissionComponent {
    @Component.Factory
    interface Factory {
        fun create(dependency: NotificationPermissionDependencies): NotificationPermissionComponent
    }

    fun inject(fragment: NotificationPermissionRequestDialog)
}

@Module
object NotificationPermissionModule {

    @Provides
    @Singleton
    @JvmStatic
    fun provideNotificationPermissionManager(
        @Named("default") prefs: SharedPreferences
    ): NotificationPermissionManager {
        return NotificationPermissionManagerImpl(
            sharedPreferences = prefs
        )
    }
}

interface NotificationPermissionDependencies : ComponentDependencies {
    @Named("default")
    fun providePrefs(): SharedPreferences
}