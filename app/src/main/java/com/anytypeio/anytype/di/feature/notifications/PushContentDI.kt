package com.anytypeio.anytype.di.feature.notifications

import android.content.Context
import android.content.SharedPreferences
import com.anytypeio.anytype.data.notifications.PushNotificationRepositoryImpl
import com.anytypeio.anytype.device.notifications.AnytypeFirebaseMessagingService
import com.anytypeio.anytype.device.notifications.DecryptionPushContentService
import com.anytypeio.anytype.device.notifications.DecryptionPushContentServiceProtocol
import com.anytypeio.anytype.device.notifications.PushNotificationService
import com.anytypeio.anytype.di.common.ComponentDependencies
import com.anytypeio.anytype.domain.debugging.Logger
import com.anytypeio.anytype.domain.notifications.PushNotificationRepository
import dagger.Binds
import dagger.Component
import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Singleton

@Singleton
@Component(
    dependencies = [PushContentDependencies::class],
    modules = [
        PushContentModule::class,
        PushContentModule.Declarations::class],
)
interface PushContentComponent {
    @Component.Factory
    interface Factory {
        fun create(dependency: PushContentDependencies): PushContentComponent
    }

    fun inject(service: AnytypeFirebaseMessagingService)
}

@Module
object PushContentModule {

    @Singleton
    @Provides
    fun providePushNotificationService(
        repository: PushNotificationRepository
    ): PushNotificationService {
        return PushNotificationService(repository)
    }

    @Singleton
    @Provides
    fun provideDecryptionPushContentService(
        @Named("encrypted") sharedPreferences: SharedPreferences
    ): DecryptionPushContentServiceProtocol {
        return DecryptionPushContentService(
            encryptedPrefs = sharedPreferences,
        )
    }

    @Module
    interface Declarations {
        @Binds
        fun bindPushNotificationRepository(
            impl: PushNotificationRepositoryImpl
        ): PushNotificationRepository

    }
}

interface PushContentDependencies : ComponentDependencies {
    // Define any necessary dependencies here
    fun context(): Context
    fun logger(): Logger

    @Named("encrypted")
    fun sharedPreferences(): SharedPreferences
}