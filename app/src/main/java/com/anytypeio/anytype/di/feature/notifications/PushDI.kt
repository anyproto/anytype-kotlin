package com.anytypeio.anytype.di.feature.notifications

import android.content.SharedPreferences
import com.anytypeio.anytype.device.AnytypePushService
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.domain.device.DeviceTokenStoringService
import com.anytypeio.anytype.presentation.notifications.NotificationPermissionManager
import com.anytypeio.anytype.presentation.notifications.NotificationPermissionManagerImpl
import com.anytypeio.anytype.presentation.notifications.DecryptionPushContentService
import dagger.Component
import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Singleton

@Singleton
@Component(
    dependencies = [PushContentDependencies::class],
    modules = [PushContentModule::class],
)
interface PushContentComponent {
    @Component.Factory
    interface Factory {
        fun create(dependency: PushContentDependencies): PushContentComponent
    }

    fun inject(service: AnytypePushService)
}

@Module
object PushContentModule {

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

interface PushContentDependencies : ComponentDependencies {
    fun deviceTokenSavingService(): DeviceTokenStoringService
    fun decryptionService(): DecryptionPushContentService
    @Named("default") fun providePrefs(): SharedPreferences
}